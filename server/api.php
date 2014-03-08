<?php
mysql_connect('localhost', 'root', '');
mysql_select_db("lomba");

$method = $_GET['m'];

foreach($_GET as $key => $val) {
	$_GET[$key] = mysql_real_escape_string($val);
}

if(empty($_GET['debug'])) {
	header('Content-type: application/json');
}

function rand_email() {
	return E::$emails[rand(0, count(E::$emails) -1)] . '@gmail.com';
}

/**
 * @param string user_email
 * @param string caleg_id
 */
if($method == 'has_rated') {
	$query = mysql_query("select rating from caleg_rating where caleg_id = '" . $_GET['caleg_id'] . "' and user_email = '" . $_GET['user_email'] . "'");
	$result = array('has_rated' => 0);
	$rating = mysql_fetch_assoc($query);
	if(mysql_num_rows($query) > 0) {
		$result['has_rated'] = 1;
		$result['rate'] = $rating['rating'];
	}
	exit(json_encode($result));
}

/**
 * @param caleg_id
 * @param user_email
 * @param title
 * @param content
 * @param rating
 */
if($method == 'rate_comment_caleg') {
	//ck ud rt blom
	$result = mysql_query("select rating from caleg_rating where caleg_id = '" . $_GET['caleg_id'] . "' and user_email = '" . $_GET['user_email'] . "'");
	
	if(mysql_num_rows($result) > 0) {
		$qf = "update caleg_rating set rating = %f where caleg_id = '%s' and user_email = '%s'";
		$result = mysql_query(sprintf($qf, $_GET['rating'], $_GET['caleg_id'], $_GET['user_email']));
		
		$qf = "update comment set title = '%s', content = '%s', updated = %d where caleg_id = '%s' and user_email = '%s'";
		$result = mysql_query(sprintf($qf, $_GET['title'], $_GET['content'], time(), $_GET['caleg_id'], $_GET['user_email']));
	} else {
		$qf = "insert into caleg_rating(caleg_id, user_email, rating) values('%s', '%s', %f)";
		$result = mysql_query(sprintf($qf, $_GET['caleg_id'], $_GET['user_email'], $_GET['rating']));
		
		$qf = "insert into comment (title, content, caleg_id, user_email, created, updated) values ('%s', '%s', '%s', '%s', %d, %d)";
		$result = mysql_query(sprintf($qf, $_GET['title'], $_GET['content'], $_GET['caleg_id'], $_GET['user_email'], time(), time()));
	}
	
	exit(json_encode(array('status' => $result)));
}

/**
 * @param title
 * @param content
 * @param caleg_id
 * @param user_email
 */
if($method == 'create_comment') {
	$qf = "insert into comment (title, content, caleg_id, user_email, created, updated) values ('%s', '%s', '%s', '%s', %d, %d)";
	$result = mysql_query(sprintf($qf, $_GET['title'], $_GET['content'], $_GET['caleg_id'], $_GET['user_email'], time(), time()));

	exit(json_encode(array('status' => $result)));
}

/**
 * @param caleg_id
 */
function get_caleg_rating($caleg_id) {
	$qf = "select count(rating) as count, avg(rating) as avg from caleg_rating where caleg_id = '%s'";
	$result = mysql_query(sprintf($qf, $caleg_id));
	$return = mysql_fetch_assoc($result);
	
	if(empty($return['avg'])) {
		generate_caleg_ratings($caleg_id, true);
	} else {
		return $return;
	}
	
	
}

if($method == 'get_caleg_rating') {
	$return = get_caleg_rating($_GET['caleg_id']);
	if(!empty($return['avg'])) {
		exit(json_encode($return));
	} else {
		exit(json_encode(array('avg' => 0, 'count' => 0)));
	}
}

function rand_clg_id($caleg_id) {
	return base_convert(substr(md5($caleg_id), 0, 4), 16, 10);
}

/**
 * @param caleg_id
 */
function generate_caleg_ratings($caleg_id, $force = true) {
	if($force === false) {
		$res = mysql_query("select * from caleg_rating where caleg_id = '" . $caleg_id . "' limit 10");
		if(mysql_num_rows($res) >= 0) {
			return;
		}
	}
	
	$comment_count = mt_rand(5, mt_getrandmax() - 1) / mt_getrandmax() * 20;
	for($i = 0; $i < $comment_count; $i++) {
		$rate = mt_rand(1, mt_getrandmax() - 1) / mt_getrandmax() * 2 + (rand_clg_id($caleg_id)%4);
		if($rate < 1) {
			$rate = 1;
		}
		$qf = "insert into caleg_rating (caleg_id, user_email, rating) values('%s', '%s', %f)";
		mysql_query(sprintf($qf, $caleg_id, rand_email(), floor(number_format($rate, 1) * 2) / 2));
	}
}

if($method == 'generate_caleg_ratings') {
	generate_caleg_ratings($_GET['caleg_id']);
}

/**
 * @param caleg_id
 */
function generate_comments($caleg_id, $force = false) {
	$comments = C::$comments;
	
	if($force === false) {
		$res = mysql_query("select * from comment where caleg_id = '" . $caleg_id . "' limit 1");
		if(mysql_num_rows($res) > 0) {
			return;
		}
	}
	
	for($i = 0; $i < 20; $i++) {
		$comment = $comments[rand(0, count($comments) - 1)];
		$qf = "insert into comment (title, content, user_email, caleg_id, created, updated) values ('%s', '%s', '%s', '%s', %d, %d)";
		$result = mysql_query(sprintf($qf, $comment[0], $comment[1], rand_email(), $caleg_id, time(), time()));
	}
}

if($method == 'generate_comments') {
	generate_comments($_GET['caleg_id']);
}

/**
 * @param comment_id
 * @param is_up 0 or 1 or -1 to delete record
 * @param user_email
 */
if($method == 'rate_comment') {
	$qf = "select * from comment_rating where comment_id = %d and user_email = '%s'";
	$result = mysql_query(sprintf($qf, $_GET['comment_id'], $_GET['user_email']));
	
	//up
	if(mysql_num_rows($result) > 0) {
		if($_GET['is_up'] == '0' || $_GET['is_up'] == '1') {
			$qf = "update comment_rating set is_up = %d where user_email = '%s' and comment_id = %d";
			mysql_query(sprintf($qf, $_GET['is_up'], $_GET['user_email'], $_GET['comment_id']));	
		} else {
			$qf = "delete from comment_rating where comment_id = %d and user_email = '%s'";
			mysql_query(sprintf($qf, $_GET['comment_id'], $_GET['user_email']));
		}
	}
	
	//ins
	else {
		$qf = "insert into comment_rating (comment_id, user_email, is_up) values (%d, '%s', %d)";
		mysql_query(sprintf($qf, $_GET['comment_id'], $_GET['user_email'], $_GET['is_up']));
	}
}

/**
 * @param caleg_id
 */
if($method == 'generate_rate_comments') {
	$qf = "select id from comment where caleg_id = '%s'";
	$results = mysql_query(sprintf($qf, $_GET['caleg_id']));
	while($row = mysql_fetch_assoc($results)) {
		$cid = $row['id'];
		for($x = 0; $x < 100; $x++) {
			$qf = "insert into comment_rating (comment_id, user_email, is_up) values(%d, '%s', %d)";
			$query = sprintf($qf, $cid, rand_email(), rand(0, 1));
			mysql_query($query);
		}
	}
}

/**
 * @param caleg_id
 * @param user_email
 * @param sort_by | rating created
 */
if($method == 'get_comments') {
	$qf = 
		"select comment.*, comment_rating.is_up from comment left outer join comment_rating on " .
		"comment.id = comment_rating.comment_id and comment.user_email = comment_rating.user_email and comment_rating.user_email = '%s' where caleg_id = '%s' order by updated desc";
	$results = mysql_query(sprintf($qf, $_GET['user_email'], $_GET['caleg_id']));
	$return = array();
	while($row = mysql_fetch_assoc($results)) {
		$return[] = $row;
	}
	
	exit(json_encode($return));
}

/**
 * @param string partai
 * @param string dapil
 * 
 */
if($method == 'get_calegs_by_dapil') {
	$cache_key = md5($_GET['dapil'] . "|" . $_GET['partai']);
	$cache_path = getcwd() . '/cache/get_calegs_by_dapil_' . $cache_key;
	if(!is_file($cache_path)) {
		$content = file_get_contents('http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&partai=' . $_GET['partai'] . "&dapil=" . $_GET['dapil']);
		file_put_contents($cache_path, $content);
	} else {
		$content = file_get_contents($cache_path);
	}

	$calegs = json_decode($content);
	$calegs = $calegs->data->results->caleg;
	$caleg_ids = array();
	$return = array();
	foreach($calegs as $caleg) {
		if(!empty($_GET['generate_ratings'])) {
			generate_caleg_ratings($caleg->id);
		}
		
		$caleg_ids[] = $caleg->id;
	}
	
	$qf = "select caleg_id, count(rating) as count, avg(rating) as avg from caleg_rating where caleg_id in %s group by (caleg_id) ";
	$results = mysql_query(sprintf($qf, "('" . join("', '", $caleg_ids) . "')"));
	$caleg_ratings = array();
	
	while($result = mysql_fetch_assoc($results)) {
		$caleg_ratings[$result['caleg_id']] = $result;
	}
	foreach($calegs as $caleg) {
		if(!empty($caleg_ratings[$caleg->id])) {
			$caleg->rating = $caleg_ratings[$caleg->id];
		} else {
			$caleg->rating = array('count' => 0, 'avg' => 0);
		}
	}
	
	exit(json_encode($calegs));
}

/**
 * @param $lat
 * @param $lng
 */
if($method == 'get_beranda') {
	$cache_key = md5($_GET['lat'] . '|' . $_GET['lng']);
	$cache_path = getcwd() . '/cache/get_beranda_' . $cache_key;

	if(!is_file($cache_path)) {
		$content = file_get_contents('http://api.pemiluapi.org/geographic/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&lembaga=DPR&lat=' . $_GET['lat'] . '&long=' . $_GET['lng']);
		file_put_contents($cache_path, $content);
	} else {
		$content = file_get_contents($cache_path);
	}
	
	$_results = json_decode($content);
	$results = array();
	$caleg_ids = array();
	foreach($_results->data->results as $result) {
		if($result->kind == 'Dapil') {
			$results = $result->caleg;
			
			//generet rating
			foreach($results as $r) {
				$caleg_ids[] = $r->id;
				generate_caleg_ratings($r->id);
				generate_comments($r->id);
			}
			break;
		}
	}
	
	//avg
	$qf = 
		"select t.caleg_id, max(avg_rate) as mar from " . 
		"( select caleg_id, avg(rating) as avg_rate from caleg_rating group by (caleg_id) ) t " . 
		"where caleg_id in ('%s') " .
		"group by (caleg_id) order by mar desc";
	$results1 = mysql_query(sprintf($qf, join("', '", $caleg_ids)));
	$max_avg = mysql_fetch_assoc($results1);
	foreach($results as $r) {
		if($r->id == $max_avg['caleg_id']) {
			$r->rating = get_caleg_rating($r->id);
			$max_avg = $r;
			break;
		}
	}

	//most komen
	$qf = 
		"select caleg_id, count(*) as cnt from comment where caleg_id in ('%s') group by (caleg_id) ";
	$results2 = mysql_query(sprintf($qf, join("', '", $caleg_ids)));
	$most_cmtd = mysql_fetch_assoc($results2);
	foreach($results as $r) {
		if($r->id == $most_cmtd['caleg_id']) {
			$most_cmtd = $r;
			$rating = get_caleg_rating($r->id);
			$most_cmtd->rating = $rating;
			break;
		}
	}

	//featured
	$featured = $results[rand(0, count($results) - 1)];
	$rate = get_caleg_rating($featured->id);
	$featured->rating = $rate;
	
	exit(json_encode(array(
		'top_rated' => $max_avg, 'most_commented' => $most_cmtd, 'featured' => $featured
	)));
}

/**
 * create table caleg_rating(
	id int(11) primary key auto_increment,
	caleg_id int(11) not null,
	user_email text not null,
	rating double not null
);

create table commment( 
	id int(11) primary key auto_increment,
	title text,
	content text,
	user_email text,
	caleg_id int(11)
);

create table comment_rating (
	id int primary key auto_increment,
	comment_id int(11),
	user_email text not null,
	is_up int(1),
	foreign key(comment_id) references comment(id)
)
 */

class C {
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

class E {
	public static $emails = array(
			"Ababua",
			"abacay",
			"canine",
			"caliber",
			"blades",
			"dollop",
			"eraser",
			"sunder",
			"hunter",
			"flaunt",
			"blackcurrant",
			"yukuku",
			"nescafe",
			"coffee",
			"thailand",
			"indonesia",
			"germany",
			"poland",
			"russian",
			"hourglass",
			"chicken",
			"abduct",
			"eardrop",
			"eardrum",
			"habitual",
			"harlem",
			"thunder",
			"strike",
			"hammer",
			"tablet",
			"chocolate",
			"tissue",
			"serviette",
			"hamburger",
			"design",
			"computer",
			"ablaze",
			"blazing",
			"hamper",
			"blasted",
	);
};
