<?php
/**
 * API controller, all methods are get, get the key by Input::get('key')
 * @author User
 *
 */
class Controller_Api extends Controller_Rest {
	public static $apiKey = '06ec082d057daa3d310b27483cc3962e';
	protected $format = 'json';
	
	/**
	 * @param string partai
	 * @param string dapil
	 *
	 */
	function get_calegs_by_dapil() {
		//First we try to load results from cache, for a given dapil and partai
		$cacheKey = __FUNCTION__ . '_' . md5(Input::get('dapil') . '|' . Input::get('partai'));
		$calegsJson = '';
		
		try {
			$calegsJson = Cache::get($cacheKey);
		} catch(Exception $e) {
			$calegsJson = file_get_contents('http://api.pemiluapi.org/candidate/api/caleg?apiKey=' . self::$apiKey . '&tahun=2014&lembaga=DPR&partai=' . Input::get('partai') . "&dapil=" . Input::get('dapil')); 
			Cache::set($cacheKey, $calegsJson);
		}
	
		//Now we try to decode the json and grab the caleg list from the JSON
		$calegs = json_decode($calegsJson);
		$calegs = $calegs->data->results->caleg;
		
		//Store the ids, to return the caleg details later on
		$calegIds = array();
		$return = array();
		foreach($calegs as $caleg) {
			//Attempt to generate comments, the following call will only attempt to generate if caleg specified by $caleg->id has less than 50 comments / rates
			Util::generateComments($caleg->id);
	
			$calegIds[] = $caleg->id;
		}		

		//Calculate and merge average to $calegs
		$calegRatings = array();
		$results = DB::select('caleg_id', DB::expr('count(rating) as count'), DB::expr('avg(rating) as avg'))
			->from('caleg_rating')->where('caleg_id', 'in', $calegIds)->group_by('caleg_id')->execute();
		$calegRatings = array();
		
		foreach($results as $result) {
			$calegRatings[$result['caleg_id']] = $result;
		}
		
		foreach($calegs as $caleg) {
			$rating = array('count' => 0, 'avg' => 0);
			if(!empty($calegRatings[$caleg->id])) {
				$rating = $calegRatings[$caleg->id];
			} 
			$caleg->rating = $rating;
		}
		
		return $this->response($calegs);
	}
	
	/**
	 * @param $lat
	 * @param $lng
	 */
	function get_beranda() {
		//First we try to load results from cache, for a given lat and lng
		$cacheKey = __FUNCTION__ . '_' . md5(Input::get('lat') . '|' . Input::get('lng'));
		$calegsJson = '';

		try {
			$calegsJson = Cache::get($cacheKey);
		} catch(Exception $e) {
			$calegsJson = file_get_contents('http://api.pemiluapi.org/geographic/api/caleg?apiKey=' . 
					self::$apiKey . '&tahun=2014&lembaga=DPR&'.
					'lat=' . Input::get('lat') . '&long=' . Input::get('lng')); 
			Cache::set($cacheKey, $calegsJson);
		}
		
		//Loop thru the calegs we obtained, then generate comments and rating for each caleg if required
		$results = json_decode($calegsJson);
		if(empty($results->data->results)) {
			//If no results were returned for the given coordinate, retry a fixed coord
			$calegsJson = file_get_contents('http://api.pemiluapi.org/geographic/api/caleg?apiKey=' . self::$apiKey . '&tahun=2014&lembaga=DPR&lat=-6.87315&long=107.58682');
			Cache::set($cacheKey, $calegsJson);				
			$results = json_decode($calegsJson);
		}
		
		$calegs = array();
		$calegIds = array();
		
		foreach($results->data->results as $result) {
			//We only care about Dapil for now
			if($result->kind == 'Dapil') {
				$calegs = $result->caleg;
					
				foreach($calegs as $caleg) {
					$calegIds[] = $caleg->id;
					Util::generateComments($caleg->id);
				}
				
				break;
			}
		}
	
		/*
		 * Top rated, with a subquery, we first get all the average of required calegs
		 * Then outer query fetch the largest average
		 */
		$subQuery = DB::select('caleg_id', DB::expr('avg(rating) as avg_rate'))->from('caleg_rating')->group_by('caleg_id');
		$topRated = DB::select('t.caleg_id', DB::expr('max(avg_rate) as mar'))->from(array($subQuery, 't'))->where('caleg_id', 'in', $calegIds)
			->group_by('caleg_id')->order_by('mar', 'desc')->execute();
		$topRated = $topRated->current();
		
		//Now assign the complete profile from the list of calegs we obtained earlier from api.pemilu
		foreach($calegs as $caleg) {
			if($caleg->id == $topRated['caleg_id']) {
				$caleg->rating = Util::getCalegRating($caleg->id);
				$topRated = $caleg;
				break;
			}
		}
		
		/**
		 * Most commented
		 */
		$mostCommented = DB::select('caleg_id', DB::expr('count(*) as cnt'))->from('comment')->where('caleg_id', 'in', $calegIds)->group_by('caleg_id')
			->order_by('cnt', 'desc')->limit(1)->execute();
		$mostCommented = $mostCommented->current();
		
		//Now assign the complete profile from the list of calegs we obtained earlier from api.pemilu
		foreach($calegs as $caleg) {
			if($caleg->id == $mostCommented['caleg_id']) {
				$caleg->rating = Util::getCalegRating($caleg->id);
				$mostCommented = $caleg;
				break;
			}
		}
		
		/**
		 * Featured, just grab a random dude
		 */
		$featured = $calegs[rand(0, count($calegs) - 1)];
		$rating = Util::getCalegRating($featured->id);
		$featured->rating = $rating;
		$this->response(array('featured' => $featured, 'top_rated' => $topRated, 'most_commented' => $mostCommented));
	}
}
	