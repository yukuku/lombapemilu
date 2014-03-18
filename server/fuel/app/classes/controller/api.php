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
	 * @param string h
	 */
	function get_hapus_data() {
		if(Input::get('hes') != 'inihesyangdatangnyadarijs') {
		} else {
			DB::query('truncate table caleg_rating')->execute();
			DB::query('truncate table comment_rating')->execute();
		}
		Response::redirect('admin/index');
	}
	
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
			Cache::set($cacheKey, $calegsJson, 3600);
		}
	
		//Now we try to decode the json and grab the caleg list from the JSON
		$calegs = json_decode($calegsJson);
		$calegs = $calegs->data->results->caleg;
		
		//If no calegs returned, return empty array to the client
		if(empty($calegs)) {
			exit("[]"); // Not using $this->response(array()) as it returns empty content and 204, which doesn't work well yet on android
			return;
		}
		
		//Store the ids, to return the caleg details later on
		$calegIds = array();
		$return = array();
		foreach($calegs as $caleg) {
			//Attempt to generate comments, the following call will only attempt to generate if caleg specified by $caleg->id has less than 50 comments / rates
			$calegIds[] = $caleg->id;
			Util::generateComments($caleg->id);
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
	
	function get_set_autogenerate() {
		$state = Input::get('state', 0);
		Util::setRatingGeneration($state);
		$this->response(array('status' => true));
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
				Cache::set($cacheKey, $calegsJson, 3600);
			}
		}
		
		//Loop thru the calegs we obtained, then generate comments and rating for each caleg if required
		$results = json_decode($calegsJson);
		$calegs = array();
		$calegIds = array();

		$calegs = $results->data->results->caleg;
		
		//If no calegs returned, return empty array to the client
		if(empty($calegs)) {
			exit("[]"); // Not using $this->response(array()) as it returns empty content and 204, which doesn't work well yet on android
			return;
		}
		
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
		
		$results = DB::select('*')->from('caleg_rating')->
			join(array($sumRatingOnComment, 't'), 'left outer')->on('caleg_rating.id', '=', 't.rating_id')->
			join(array($didUserRateComment, 'u'), 'left outer')->on('caleg_rating.id', '=', 'u.rating_id')->
			where('caleg_rating.caleg_id', Input::get('caleg_id'))->order_by($orderBy, 'desc')->execute();
		
		$return = $results->as_array();
		
		if(empty($return)) {
			exit("[]");
		}
		
		foreach($return as $i => $result) {
			if(empty($return[$i]['sum'])) {
				//Sum of thumbs up / down
				$return[$i]['sum'] = 0;
			}
			
			if(empty($return[$i]['is_up'])) {
				//Whether $userEmail has alread upped/downed this comment
				$return[$i]['is_up'] = 0;
			}

			unset($return[$i]['rating_id']);
		}
		
		if($orderBy == 't.sum') {
			usort($return, function($a, $b) {
				return $a['sum'] < $b['sum'];
			});
		}
		
		$this->response($return);
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
		if($result->count() > 0) {
			$result = DB::update('caleg_rating')->set(array(
				'title' => $title, 'content' => $content, 'updated' => time(), 'rating' => $rating
			))->where('caleg_id', $calegId)->where('user_email', $userEmail)->execute();
		} else {
			$result = DB::insert('caleg_rating')->set(array(
				'caleg_id' => $calegId, 'user_email' => $userEmail, 'rating' => $rating,
				'title' => $title, 'content' => $content, 'created' => time(), 'updated' => time()
			))->execute();
		}
		
		Log::debug(__FUNCTION__ . ': ' . $result);
	
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
	
	/**
	 * @param rating_id
	 * @param is_up -1, 0, 1
	 * @param user_email
	 */
	function get_rate_comment() {
		$ratingId = Input::get('rating_id');
		$userEmail = Input::get('user_email');
		$rate = Input::get('is_up');
		$result = DB::select('*')->from('comment_rating')->where('rating_id', $ratingId)->where('user_email', $userEmail)->execute();
		
		if($result->count() > 0) {
			DB::update('comment_rating')->value('is_up', $rate)->where('user_email', $userEmail)->where('rating_id', $ratingId)->execute();
		} else {
			DB::insert('comment_rating')->set(array(
				'rating_id' => $ratingId, 'user_email' => $userEmail, 'is_up' => $rate
			))->execute();
		}
		
		$this->response(array('status' => true));
	}
	
}
	