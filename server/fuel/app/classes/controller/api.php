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
	 * @param string lembaga
	 *
	 */
	function get_calegs_by_dapil() {
		//First we try to load results from cache, for a given dapil and partai
		$cacheKey = __FUNCTION__ . '_' . md5(Input::get('dapil', '') . '|' . Input::get('partai', '') . '|' . Input::get('lembaga', ''));
		$calegsJson = '';
		
		try {
			$calegsJson = Cache::get($cacheKey);
		} catch(Exception $e) {
			$calegsJson = file_get_contents('http://api.pemiluapi.org/candidate/api/caleg?apiKey=' . self::$apiKey . '&tahun=2014&lembaga=' . Input::get('lembaga') . '&partai=' . Input::get('partai') . "&dapil=" . Input::get('dapil')); 
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
// 			Util::generateComments($caleg->id);
	
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
	 * @param $dapil
	 * @param $lembaga
	 * 
	 */
	function get_beranda() {
		//First we try to load results from cache, for a given lat and lng
		$dapil = Input::get('dapil');
		$lembaga = Input::get('lembaga');
		$cacheKey = __FUNCTION__ . '_' . md5($dapil . '|' . $lembaga);

		try {
			$calegsJson = Cache::get($cacheKey);
		} catch(Exception $e) {
			$calegsJson = file_get_contents(
				'http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=' 
				. $lembaga . '&dapil=' . $dapil
			); 

			if(!empty($calegsJson)) {
				Cache::set($cacheKey, $calegsJson);
			}
		}
		
		//Loop thru the calegs we obtained, then generate comments and rating for each caleg if required
		$results = json_decode($calegsJson);
		$calegs = array();
		$calegIds = array();

		$calegs = $results->data->results->caleg;
		foreach($calegs as $caleg) {
			$calegIds[] = $caleg->id;
			Util::generateComments($caleg->id);
		}
	
		/*
		 * Top rated, with a subquery, we first get all the average of required calegs
		 * Then outer query fetch the largest average
		 */
		$subQuery = DB::select('caleg_id', DB::expr('avg(rating) as avg_rate'))->from('caleg_rating')->group_by('caleg_id');
		$topRated = DB::select('t.caleg_id', DB::expr('max(avg_rate) as mar'))->from(array($subQuery, 't'))->where('caleg_id', 'in', $calegIds)
			->group_by('caleg_id')->order_by('mar', 'desc')->execute();
		$topRated = $topRated->current();
		
		if(empty($topRated)) {
			$topRated = $calegs[rand(0, count($calegs) - 1)];
			$topRated->rating = Util::getCalegRating($topRated->id);
		} else {
			//Assign the complete profile from the list of calegs we obtained earlier from api.pemilu
			foreach($calegs as $caleg) {
				if($caleg->id == $topRated['caleg_id']) {
					$caleg->rating = Util::getCalegRating($caleg->id);
					$topRated = $caleg;
					break;
				}
			}
		}
		
		
		/**
		 * Most commented
		 */
		$mostCommented = DB::select('caleg_id', DB::expr('count(*) as cnt'))->from('caleg_rating')->where('caleg_id', 'in', $calegIds)->group_by('caleg_id')
			->order_by('cnt', 'desc')->limit(1)->execute();
		$mostCommented = $mostCommented->current();
		
		//Now assign the complete profile from the list of calegs we obtained earlier from api.pemilu
		if(empty($mostCommented)) {
			$mostCommented = $calegs[rand(0, count($calegs) - 1)];
			$mostCommented->rating = Util::getCalegRating($mostCommented->id);
		} else {
			//Assign the complete profile from the list of calegs we obtained earlier from api.pemilu
			foreach($calegs as $caleg) {
				if($caleg->id == $mostCommented['caleg_id']) {
					$caleg->rating = Util::getCalegRating($caleg->id);
					$mostCommented = $caleg;
					break;
				}
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
	
	//Get comments from the given caleg id, also returns the rate for each comment and whether the logged in user rated already
	/**
	 * @param user_email
	 * @param caleg_id
	 * @param order_by
	 */
	function get_comments() {
		$sumRatingOnComment = DB::select('rating_id', array(DB::expr('sum(is_up)'), 'sum'))->from('comment_rating')->group_by('rating_id');
		$didUserRateComment = DB::select('rating_id', 'is_up')->from('comment_rating')->where('user_email', Input::get('user_email'));
		
		$orderBy = Input::get('order_by', 'updated');
		if($orderBy == 'updated') {
			$orderBy = 'caleg_rating.updated';
		} else {
			$orderBy = 't.sum';	
		}
		
		//Combine all the queries
		// 		"select * from comment left outer join " .
		// 		"(select rating_id, sum(is_up) as sum from comment_rating group by (rating_id)) t on " .
		// 		"comment.id = t.rating_id left outer join " .
		// 		"(select rating_id, is_up from comment_rating where user_email = '%s') u on " .
		// 		"comment.id = u.rating_id where comment.caleg_id = '%s' order by comment.updated desc;";
		$results = DB::select('*')->from('caleg_rating')->
			join(array($sumRatingOnComment, 't'), 'left outer')->on('caleg_rating.id', '=', 't.rating_id')->
			join(array($didUserRateComment, 'u'), 'left outer')->on('caleg_rating.id', '=', 'u.rating_id')->
			where('caleg_rating.caleg_id', Input::get('caleg_id'))->order_by($orderBy, 'desc')->execute();
		
		$this->response($results);
	}
	
	/**
	 * @param caleg_id
	 * @param user_email
	 * @param title
	 * @param content
	 * @param rating
	 */
	function get_rate_comment_caleg() {
		//Check whether this user_email has rated this caleg_id
		$calegId = Input::get('caleg_id');
		$userEmail = Input::get('user_email');
		$result = DB::select('rating')->from('caleg_rating')->where('caleg_id', $calegId)->where('user_email', $userEmail)->execute();
		
		//If user has commented and rated in the past, update, otherwise insert
		$rating = Input::get('rating');
		$title = Input::get('title');
		$content = Input::get('content');
		if($result->count() > 0 && $result) {
			$result = DB::update('caleg_rating')->set(array(
				'title' => $title, 'content' => $content, 'updated' => time(), 'rating' => $rating
			))->where('caleg_id', $calegId)->where('user_email', $userEmail)->execute();
		} else {
			$result = DB::insert('caleg_rating')->set(array(
				'caleg_id' => $calegId, 'user_email' => $userEmail, 'rating' => $rating,
				'title' => $title, 'content' => $content, 'created' => time(), 'updated' => time()
			))->execute();
		}
	
		$this->response(array(
			'status' => !empty($result)
		));
	}
	
	/**
	 * @param string user_email
	 * @param string caleg_id
	 */
	function get_has_rated() {
		$calegId = Input::get('caleg_id');
		$userEmail = Input::get('user_email');
		$result = DB::select('rating')->from('caleg_rating')->where('caleg_id', $calegId)->where('user_email', $userEmail)->execute();

		//If has rated, return the rate
		if($result->count() > 0) {
			$this->response(array('has_rated' => 1, 'rate' => $result->current()));
		} else {
			$this->response(array('has_rated' => 0, 'rate' => 0));
		}
	}
}
	