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
	return substr(uniqid(rand(), true), 0, 20) . '@gmail.com';
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
	} else {
		$qf = "insert into caleg_rating(caleg_id, user_email, rating) values('%s', '%s', %f)";
		$result = mysql_query(sprintf($qf, $_GET['caleg_id'], $_GET['user_email'], $_GET['rating']));
	}
	
	//komen
	if(!empty($_GET['title']) && !empty($_GET['content'])) {
		$qf = "insert into comment (title, content, caleg_id, user_email, created) values ('%s', '%s', '%s', '%s', %d)";
		$result = mysql_query(sprintf($qf, $_GET['title'], $_GET['content'], $_GET['caleg_id'], $_GET['user_email'], time()));
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
	$qf = "insert into comment (title, content, caleg_id, user_email, created) values ('%s', '%s', '%s', '%s', %d)";
	$result = mysql_query(sprintf($qf, $_GET['title'], $_GET['content'], $_GET['caleg_id'], $_GET['user_email'], time()));

	exit(json_encode(array('status' => $result)));
}

/**
 * @param caleg_id
 */
function get_caleg_rating($caleg_id) {
	$qf = "select avg(rating) as avg from caleg_rating where caleg_id = %d";
	$result = mysql_query(sprintf($qf, $_GET['caleg_id']));
	$return = mysql_fetch_assoc($result);
	
}

if($method == 'get_caleg_rating') {
	$return = get_caleg_rating($_GET['caleg_id']);
	if(!empty($return['avg'])) {
		exit(json_encode(array('avg' => $return['avg'])));
	} else {
		exit(json_encode(array('avg' => 0)));
	}
}

/**
 * @param caleg_id
 */
function generate_caleg_ratings($caleg_id) {
	for($i = 0; $i < 200; $i++) {
		$rate = mt_rand(0, mt_getrandmax() - 1) / mt_getrandmax() * 5;
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
if($method == 'generate_comments') {
	$comments = array("Wah ganteng euy calegnya", "Hebat nih kayaknya pendidikannya", "Jomblo ga ya, naksir nih");
	for($i = 0; $i < 20; $i++) {
		$qf = "insert into comment (title, content, user_email, caleg_id, created) values ('%s', '%s', '%s', '%s', %d)";	
		$result = mysql_query(sprintf($qf, "Judul", $comments[rand(0, 2)], rand_email(), $_GET['caleg_id'], time()));
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
		"comment.id = comment_rating.comment_id and comment.user_email = comment_rating.user_email where caleg_id = %d";
	$results = mysql_query(sprintf($qf, $_GET['caleg_id']));
	$return = array();
	while($row = mysql_fetch_assoc($results)) {
		$return[] = $row;
	}
	
	exit(json_encode($return));
}

/**
 * @param string dapil
 */
if($method == 'get_calegs_by_dapil') {
	$cache_key = md5($_GET['dapil']);
	$cache_path = getcwd() . '/cache/get_calegs_by_dapil_' . $cache_key;
	if(!is_file($cache_path)) {
		$content = file_get_contents('http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&dapil=' . $_GET['dapil']);
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
	
	$qf = "select caleg_id, avg(rating) as avg from caleg_rating where caleg_id in %s group by (caleg_id) ";
	$results = mysql_query(sprintf($qf, "('" . join("', '", $caleg_ids) . "')"));
	$caleg_ratings = array();
	
	while($result = mysql_fetch_assoc($results)) {
		$caleg_ratings[$result['caleg_id']] = $result['avg'];
	}
	foreach($calegs as $caleg) {
		if(!empty($caleg_ratings[$caleg->id])) {
			$caleg->rating = $caleg_ratings[$caleg->id];
		} else {
			$caleg->rating = 0;
		}
	}
	
	exit(json_encode($calegs));
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


