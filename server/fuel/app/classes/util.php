<?php
class Util {
	public static function setRatingGeneration($state = false) {
		Cache::set('auto_generate_caleg_rating', $state);
	}
	
	public static function generateComments($calegId, $force = false) {
		//Check config
		$isEnabled = 0;
		try {
			$isEnabled = Cache::get('auto_generate_caleg_rating');
		} catch(Exception $e) {
			Cache::set('auto_generate_caleg_rating', 0);
		}
		
		//Do nothing 
		if($isEnabled == 0) return;
		
		//If we are not forced to generate, check how many comments this caleg has, only generate if less than 50
		if($force === false) {
			//First count how many comments this caleg has
			$result = DB::select(DB::expr('COUNT(*) as count'))->from('caleg_rating')->where('caleg_id', $calegId)->limit(GENERATE_THRESHOLD)->execute();
			$result = $result->current();
			
			if($result['count'] > GENERATE_THRESHOLD) {
				return;
			}
		}
	
		//Generate the rating and comment
		$commentCount = mt_rand(5, mt_getrandmax() - 1) / mt_getrandmax() * GENERATE_THRESHOLD + 5;
		$rating = 1;
		for($i = 0; $i < $commentCount; $i++) {
			//Generate some random rating, also based on the caleg id
			$rating = mt_rand(1, mt_getrandmax() - 1) / mt_getrandmax() * 2 + (self::randByCalegId($calegId) % 5);
			
			//Clamp the rating
			if($rating > 5) $rating = 5;
			if($rating < 1) {
				$rating = 1;
			}
	
			//Get random email and comment, then insert it
			$email = RdmEmails::pick();
			$comment = RdmComments::pick();

			DB::insert('caleg_rating')->set(array(
				'rating' => floor(number_format($rating, 1) * 2) / 2, //Generate 1 decimal place, round up to nearest 0.05
				'title' => $comment[0],
				'content' => $comment[1],
				'user_email' => $email,
				'caleg_id' => $calegId,
				'created' => time(),
				'updated' => time()
			))->execute();
		}
	}
	
	//Generate a random integer out of a given caleg id
	public static function randByCalegId($calegId) {
		return base_convert(substr(md5($calegId), 0, 4), 16, 10);
	}

	//Get rating along with the number of vote of a caleg
	public static function getCalegRating($calegId) {
		$result = DB::select(DB::expr('count(rating) as count'), DB::expr('coalesce(avg(rating), 0) as avg'))
			->from('caleg_rating')->where('caleg_id', $calegId)->execute();
		
		return $result->current();
	}
}

define('SECOND', 1);
define('MINUTE', 60 * SECOND);
define('HOUR', 60 * MINUTE);
define('DAY', 24 * HOUR);
define('GENERATE_THRESHOLD', 50);

class RdmComments {
	public static function pick() {
		return self::$comments[rand(0, count(self::$comments) -1)];
		
	}
	
	public static $comments = array(
		array('Bagus!', 'Saya kenal beliau secara pribadi, orangnya lugas dan jujur'),
		array('Pilihlah beliau', 'Sangat rekomended! Baik dan sederhana!'),
		array('Harapan kita semua.', 'Anaknya banyak, semoga bisa mengatur rakyat sebaik mengatur anak'),
		array('Kami puas', 'Semoga menang pak, kami sekeluarga mendukung.'),
		array('Majulah!', 'Maju terus!'), array('Berpendidikan', "Dengan pendidikan yang sangat tinggi, saya yakin anda dapat menjadi anggota DPR yang berkualitas!"),
		array('Jangan dipilih!!!', 'Dengan begitu banyaknya pemberitaan tentang bapak di media - media besar, seperti media ayam jago bahwa bapak berkorupsi, saya tidak akan memilih bapak'),
		array('Jempol', 'Semoga bapak berhati nurani dan dapat mencintai rakyat'), array('Kami dukung sepenuhnya!!!!', 'Maju terus bu, jangan biarkan orang lain memandang rendah ibu karena ibu seorang wanita'),
		array('Semoga sukses', 'Doa kami beserta bapak, semoga sukses!')
	);
};

class RdmEmails {
	public static function pick() {
		return self::$emails[rand(0, count(self::$emails) -1)] . '@gmail.com';
		
	}
	public static $emails = array(
		"Ababua", "abacay", "canine", "caliber", "blades", "dollop",
		"eraser", "sunder", "hunter", "flaunt", "blackcurrant", "yukuku",
		"nescafe", "coffee", "thailand", "indonesia", "germany", "poland",
		"russian", "hourglass", "chicken", "abduct", "eardrop", "eardrum",
		"habitual", "harlem", "thunder", "strike", "hammer", "tablet",
		"chocolate", "tissue", "serviette", "hamburger", "design", "computer",
		"ablaze", "blazing", "hamper", "blasted",
	);
};
