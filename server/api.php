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
	$qf = "select avg(rating) as avg from caleg_rating where caleg_id = '%s'";
	$result = mysql_query(sprintf($qf, $caleg_id));
	$return = mysql_fetch_assoc($result);
	
	if(empty($return['avg'])) {
		generate_caleg_ratings($caleg_id);
	} else {
		return $return;
	}
	
	
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
function generate_caleg_ratings($caleg_id, $force = false) {
	if($force === false) {
		$res = mysql_query("select * from caleg_rating where caleg_id = '" . $caleg_id . "' limit 10");
		if(mysql_num_rows($res) >= 0) {
			return;
		}
	}
	
	for($i = 0; $i < 10; $i++) {
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
function generate_comments($caleg_id, $force = false) {
	$comments = C::$comments;
	
	if($force === false) {
		$res = mysql_query("select * from comment where caleg_id = '" . $caleg_id . "' limit 1");
		if(mysql_num_rows($res) > 0) {
			return;
		}
	}
	
	for($i = 0; $i < 20; $i++) {
		$qf = "insert into comment (title, content, user_email, caleg_id, created) values ('%s', '%s', '%s', '%s', %d)";
		$result = mysql_query(sprintf($qf, "Judul", $comments[rand(0, count($comments) - 1)], rand_email(), $caleg_id, time()));
	}
}

if($method == 'generate_comments') {
	generate_comments($_GET['caleg_id']);
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
		"comment.id = comment_rating.comment_id and comment.user_email = comment_rating.user_email where caleg_id = '%s'";
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
			$r->rating = $max_avg['mar'];
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
			$most_cmtd->rating = $rating['avg'];
			break;
		}
	}

	//featured
	$featured = $results[rand(0, count($results) - 1)];
	$rate = get_caleg_rating($featured->id);
	$featured->rating = $rate['avg'];
	
	print_r(array(
		'top_rated' => $max_avg, 'most_commented' => $most_cmtd, 'featured' => $featured
	));
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
		"A Bird In The Hand Is Worth Two In The Bush ",
"A Blessing In Disguise ",
"A Chip On Your Shoulder ",
"A Dime A Dozen ",
"A Doubting Thomas ",
"A Drop in the Bucket ",
"A very small part of something big or whole.",
"A Fool And His Money Are Easily Parted ",
"It's easy for a foolish person to lose his/her money. ",
"A House Divided Against Itself Cannot Stand ",
"Everyone involved must unify and function together or it will not work out. ",
"A Leopard Can't Change His Spots ",
"You cannot change who you are.",
"A Penny Saved Is A Penny Earned ",
"By not spending money, you are saving money (little by little).",
"A Picture Paints a Thousand Words ",
"A visual presentation is far more descriptive than words.",
"A Piece of Cake ",
"A task that can be accomplished very easily.",
"A Slap on the Wrist ",
"A very mild punishment.",
"A Taste Of Your Own Medicine ",
"When you are mistreated the same way you mistreat others.",
"A Toss-Up ",
"A result that is still unclear and can go either way.",
"Actions Speak Louder Than Words ",
"It's better to actually do something than just talk about it.",
"Add Fuel To The Fire ",
"Whenever something is done to make a bad situation even worse than it is.",
"Against The Clock ",
"Rushed and short on time.",
"All Bark And No Bite ",
"When someone is threatening and/or aggressive but not willing to engage in a fight.",
"All Greek to me ",
"Meaningless and incomprehensible like someone who cannot read, speak, or understand any of the Greek language would be.",
"All In The Same Boat ",
"When everyone is facing the same challenges.",
"An Arm And A Leg ",
"Very expensive. A large amount of money.",
"An Axe To Grind ",
"To have a dispute with someone.",
"Apple of My Eye ",
"Someone who is cherished above all others.",
"As High As A Kite ",
"Anything that is high up in the sky.",
"At The Drop Of A Hat ",
"Willing to do something immediately.",
"Back Seat Driver ",
"People who criticize from the sidelines, much like someone giving unwanted advice from the back seat of a vehicle to the driver.",
"Back To Square One ",
"Having to start all over again.",
"Back To The Drawing Board ",
"When an attempt fails and it's time to start all over.",
"Baker's Dozen ",
"Thirteen.",
"Barking Up The Wrong Tree ",
"A mistake made in something you are trying to achieve.",
"Beat A Dead Horse ",
"To force an issue that has already ended.",
"Beating Around The Bush ",
"Avoiding the main topic. Not speaking directly about the issue.",
"Bend Over Backwards ",
"Do whatever it takes to help. Willing to do anything.",
"Between A Rock And A Hard Place ",
"Stuck between two very bad options.",
"Bite Off More Than You Can Chew ",
"To take on a task that is way to big.",
"Bite Your Tongue ",
"To avoid talking.",
"Blood Is Thicker Than Water ",
"The family bond is closer than anything else.",
"Blue Moon ",
"A rare event or occurance. ",
"Break A Leg ",
"A superstitious way to say 'good luck' without saying 'good luck', but rather the opposite.",
"Buy A Lemon ",
"To purchase a vehicle that constantly gives problems or stops running after you drive it away.",
"Can't Cut The Mustard  ",
"Someone who isn't adequate enough to compete or participate.",
"Cast Iron Stomach ",
"Someone who has no problems, complications or ill effects with eating anything or drinking anything.",
"Charley Horse ",
"Stiffness in the leg / A leg cramp. ",
"Chew someone out ",
"Verbally scold someone.",
"Chip on his Shoulder ",
"Angry today about something that occured in the past.",
"Chow Down ",
"To eat.",
"Close but no Cigar ",
"To be very near and almost accomplish a goal, but fall short. ",
"Cock and Bull Story ",
"An unbelievable tale.",
"Come Hell Or High Water ",
"Any difficult situation or obstacle.",
"Crack Someone Up ",
"To make someone laugh.",
"Cross Your Fingers ",
"To hope that something happens the way you want it to.",
"Cry Over Spilt Milk ",
"When you complain about a loss from the past.",
"Cry Wolf ",
"Intentionally raise a false alarm.",
"Cup Of Joe ",
"A cup of coffee.",
"Curiosity Killed The Cat ",
"Being Inquisitive can lead you into a dangerous situation.",
"Cut to the Chase ",
"Leave out all the unnecessary details and just get to the point.",
"Dark Horse ",
"One who was previously unknown and is now prominent.",
"Dead Ringer ",
"100% identical. A duplicate.",
"Devil's Advocate ",
"Someone who takes a position for the sake of argument without believing in that particular side of the arguement. It can also mean one who presents a counter argument for a position they do believe in, to another debater.",
"Dog Days of Summer ",
"The hottest days of the summer season.",
"Don't count your chickens before they hatch ",
"Don't rely on it until your sure of it.",
"Don't Look A Gift Horse In The Mouth ",
"When someone gives you a gift, don't be ungrateful.",
"Don't Put All Your Eggs In One Basket ",
"Do not put all your resources in one possibility.",
"Doozy ",
"Something outstanding.",
"Down To The Wire ",
"Something that ends at the last minute or last few seconds.",
"Drastic Times Call For Drastic Measures ",
"When you are extremely desperate you need to take extremely desperate actions.",
"Drink like a fish ",
"To drink very heavily.",
"Drive someone up the wall ",
"To irritate and/or annoy very much.",
"Dropping Like Flies ",
"A large number of people either falling ill or dying.",
"Dry Run ",
"Rehearsal.",
"Eighty Six ",
"A certain item is no longer available. Or this idiom can also mean, to throw away.",
"Elvis has left the building ",
"The show has come to an end. It's all over.",
"Ethnic Cleansing ",
"Killing of a certain ethnic or religious group on a massive scale.",
"Every Cloud Has A Silver Lining ",
"Be optomistic, even difficult times will lead to better days.",
"Everything But The Kitchen Sink ",
"Almost everything and anything has been included.",
"Excuse my French ",
"Please forgive me for cussing.",
"Cock and Bull Story ",
"An unbelievable tale.",
"Cock and Bull Story ",
"An unbelievable tale.",
"Feeding Frenzy ",
"An aggressive attack on someone by a group. ",
"Field Day ",
"An enjoyable day or circumstance. ",
"Finding Your Feet ",
"To become more comfortable in whatever you are doing. ",
"Finger lickin' good ",
"A very tasty food or meal. ",
"Fixed In Your Ways ",
"Not willing or wanting to change from your normal way of doing something. ",
"Flash In The Pan ",
"Something that shows potential or looks promising in the beginning but fails to deliver anything in the end. ",
"Flea Market ",
"A swap meet. A place where people gather to buy and sell inexpensive goods. ",
"Flesh and Blood ",
"This idiom can mean living material of which people are made of, or it can refer to someone's family. ",
"Flip The Bird ",
"To raise your middle finger at someone. ",
"Foam at the Mouth ",
"To be enraged and show it. ",
"Fools' Gold ",
"Iron pyrites, a worthless rock that resembles real gold. ",
"French Kiss ",
"An open mouth kiss where tongues touch. ",
"From Rags To Riches ",
"To go from being very poor to being very wealthy.",
"Fuddy-duddy ",
"An old-fashioned and foolish type of person. ",
"Full Monty ",
"Funny Farm ",
"A mental institutional facility.",
"Get Down to Brass Tacks ",
"To become serious about something.",
"Get Over It ",
"To move beyond something that is bothering you.",
"Get Up On The Wrong Side Of The Bed ",
"Someone who is having a horrible day. ",
"Get Your Walking Papers ",
"Get fired from a job.",
"Give Him The Slip ",
"To get away from. To escape. ",
"Go Down Like A Lead Balloon ",
"To be received badly by an audience.",
"Go For Broke ",
"To gamble everything you have.",
"Go Out On A Limb ",
"Put yourself in a tough position in order to support someone/something.",
"Go The Extra Mile ",
"Going above and beyond whatever is required for the task at hand.",
"Good Samaritan ",
"Someone who helps others when they are in need, with no discussion for compensation, and no thought of a reward.",
"Graveyard Shift ",
"Working hours from about 1200 am to 800 am. The time of the day when most other people are sleeping.",
"Great Minds Think Alike ",
"Intelligent people think like each other.",
"Green Room ",
"The waiting room, especially for those who are about to go on a tv or radio show.",
"Gut Feeling ",
"A personal intuition you get, especially when feel something may not be right. ",
"Haste Makes Waste ",
"Quickly doing things results in a poor ending. ",
"Hat Trick ",
"When one player scores three goals in the same hockey game. This idiom can also mean three scores in any other sport, such as 3 homeruns, 3 touchdowns, 3 soccer goals, etc. ",
"Have an Axe to Grind ",
"To have a dispute with someone. ",
"He Lost His Head ",
"Angry and overcome by emotions. ",
"Head Over Heels ",
"Very excited and/or joyful, especially when in love. ",
"Hell in a Handbasket ",
"Deteriorating and headed for complete disaster. ",
"High Five ",
"Slapping palms above each others heads as celebration gesture. ",
"High on the Hog ",
"Living in Luxury.",
"Hit The Books ",
"To study, especially for a test or exam. ",
"Hit The Hay ",
"Go to bed or go to sleep. ",
"Hit The Nail on the Head ",
"Do something exactly right or say something exactly right. ",
"Hit The Sack ",
"Go to bed or go to sleep.",
"Hocus Pocus ",
"In general, a term used in magic or trickery.",
"Hold Your Horses ",
"Be patient.",
"Icing On The Cake ",
"When you already have it good and get something on top of what you already have.",
"Idle Hands Are The Devil's Tools ",
"You are more likely to get in trouble if you have nothing to do.",
"If It's Not One Thing, It's Another ",
"When one thing goes wrong, then another, and another...",
"In Like Flynn ",
"To be easily successful, especially when sexual or romantic.",
"In The Bag ",
"To have something secured.",
"In The Buff ",
"Nude.",
"In The Heat Of The Moment ",
"Overwhelmed by what is happening in the moment.",
"In Your Face ",
"An aggressive and bold confrontation.",
"It Takes Two To Tango ",
"A two person conflict where both people are at fault.",
"It's A Small World ",
"You frequently see the same people in different places.",
"Its Anyone's Call ",
"A competition where the outcome is difficult to judge or predict.",
"Ivy League ",
"Since 1954 the Ivy League has been the following universities Columbia, Brown, Cornell, Dartmouth, Yale, Pennsylvania, Princeton, and Harvard.",
"Jaywalk ",
"Crossing the street (from the middle) without using the crosswalk.",
"Joshing Me ",
"Tricking me.",
"Keep An Eye On Him ",
"You should carefully watch him.",
"Keep body and soul together ",
"To earn a sufficient amount of money in order to keep yourself alive .",
"Keep your chin up ",
"To remain joyful in a tough situation.",
"Kick The Bucket ",
"Die.",
"Kitty-corner ",
"Diagonally across. Sometimes called Catty-Corner as well.",
"Knee Jerk Reaction ",
"A quick and automatic response.",
"Knock On Wood ",
"Knuckle tapping on wood in order to avoid some bad luck.",
"Know the Ropes ",
"To understand the details.",
"Last but not least ",
"An introduction phrase to let the audience know that the last person mentioned is no less important than those introduced before him/her.",
"Lend Me Your Ear ",
"To politely ask for someone's full attention.",
"Let Bygones Be Bygones ",
"To forget about a disagreement or arguement.",
"Let Sleeping Dogs Lie ",
"To avoid restarting a conflict.",
"Let The Cat Out Of The Bag ",
"To share a secret that wasn't suppose to be shared.",
"Level playing field ",
"A fair competition where no side has an advantage.",
"Like a chicken with its head cut off ",
"To act in a frenzied manner.",
"liquor someone up ",
"To get someone drunk.",
"Long in the Tooth ",
"Old people (or horses).",
"Loose Cannon ",
"Someone who is unpredictable and can cause damage if not kept in check.",
"Make No Bones About ",
"To state a fact so there are no doubts or objections.",
"Method To My Madness ",
"Strange or crazy actions that appear meaningless but in the end are done for a good reason.",
"Mumbo Jumbo ",
"Nonsense or meaningless speech.",
"Mum's the word ",
"To keep quiet. To say nothing.",
"Nest Egg ",
"Savings set aside for future use.",
"Never Bite The Hand That Feeds You ",
"Don't hurt anyone that helps you.",
"New kid on the block ",
"Someone new to the group or area.",
"New York Minute ",
"A minute that seems to go by quickly, especially in a fast paced environment.",
"No Dice ",
"To not agree. To not accept a proposition.",
"No Room to Swing a Cat ",
"An unsually small or confined space.",
"Not Playing With a Full Deck ",
"Someone who lacks intelligence.",
"Off On The Wrong Foot ",
"Getting a bad start on a relationship or task.",
"Off The Hook ",
"No longer have to deal with a tough situation.",
"Off the Record ",
"Something said in confidence that the one speaking doesn't want attributed to him/her.",
"On Pins And Needles ",
"Anxious or nervous, especially in anticipation of something.",
"On The Fence ",
"Undecided.",
"On The Same Page ",
"When multiple people all agree on the same thing.",
"Out Of The Blue ",
"Something that suddenly and unexpectedly occurs.",
"Out On A Limb ",
"When someone puts themself in a risky situation.",
"Out On The Town ",
"To enjoy yourself by going out.",
"Over My Dead Body ",
"When you absolutely will not allow something to happen.",
"Over the Top ",
"Very excessive.",
"Pass The Buck ",
"Avoid responsibility by giving it to someone else.",
"Pedal to the metal ",
"To go full speed, especially while driving a vehicle.",
"Peeping Tom ",
"Someone who observes people in the nude or sexually active people, mainly for his own gratification.",
"Pick up your ears ",
"To listen very carefully.",
"Pig In A Poke ",
"A deal that is made without first examining it.",
"Pig Out  ",
"To eat alot and eat it quickly.",
"Pipe Down ",
"To shut-up or be quiet.",
"Practice Makes Perfect ",
"By constantly practicing, you will become better.",
"Pull the plug ",
"To stop something. To bring something to an end.",
"Pulling Your Leg ",
"Tricking someone as a joke.",
"Put a sock in it ",
"To tell noisy person or a group to be quiet.",
"Queer the pitch ",
"Destroy or ruin a plan.",
"Raincheck ",
"An offer or deal that is declined right now but willing to accept later.",
"Raining Cats and Dogs ",
"A very loud and noisy rain storm.",
"Ring Fencing ",
"Seperated usual judgement to guarantee protection, especially project funds.",
"Rise and Shine ",
"Time to get out of bed and get ready for work/school.",
"Rome Was Not Built In One Day ",
"If you want something to be completely properly, then its going to take time.",
"Rule Of Thumb ",
"A rough estimate.",
"Run out of steam ",
"To be completely out of energy.",
"Saved By The Bell ",
"Saved at the last possible moment.",
"Scapegoat ",
"Someone else who takes the blame.",
"Scot-free ",
"To escape and not have to pay.",
"Sick As A Dog ",
"To be very sick (with the flu or a cold).",
"Sitting Shotgun ",
"Riding in the front passenger seat of a car.",
"Sixth Sense ",
"A paranormal sense that allows you to communicate with the dead.",
"Skid Row ",
"The rundown area of a city where the homeless and drug users live.",
"Smell A Rat ",
"To detect somone in the group is betraying the others.",
"Smell Something Fishy ",
"Detecting that something isn't right and there might be a reason for it.",
"Son of a Gun ",
"A scamp.",
"Southpaw ",
"Someone who is left-handed.",
"Spitting Image ",
"The exact likeness or kind.",
"Start From Scratch ",
"To do it all over again from the beginning.",
"The Ball Is In Your Court ",
"It is your decision this time.",
"The Best Of Both Worlds ",
"There are two choices and you have them both.",
"The Bigger They Are The Harder They Fall ",
"While the bigger and stronger opponent might be alot more difficult to beat, when you do they suffer a much bigger loss.",
"The Last Straw ",
"When one small burden after another creates an unbearable situation, the last straw is the last small burden that one can take.",
"The Whole Nine Yards ",
"Everything. All of it.",
"Third times a charm ",
"After no success the first two times, the third try is a lucky one.",
"Tie the knot ",
"To get married.",
"Til the cows come home ",
"A long time.",
"To Make A Long Story Short ",
"Something someone would say during a long and boring story in order to keep his/her audience from losing attention. Usually the story isn't shortened.",
"To Steal Someone's Thunder ",
"To take the credit for something someone else did.",
"Tongue-in-cheek ",
"humor, not to be taken serious.",
"Turn A Blind Eye ",
"Refuse to acknowledge something you know is real or legit.",
"Twenty three skidoo ",
"To be turned away.",
"Under the weather ",
"Feeling ill or sick.",
"Up a blind alley ",
"Going down a course of action that leads to a bad outcome.",
"Use Your Loaf ",
"Use your head. Think smart.",
"Van Gogh's ear for music ",
"Tone deaf.",
"Variety Is The Spice Of Life ",
"The more experiences you try the more exciting life can be.",
"Wag the Dog ",
"A diversion away from something of greater importance.",
"Water Under The Bridge ",
"Anything from the past that isn't significant or important anymore.",
"Wear Your Heart On Your Sleeve ",
"To openly and freely express your emotions.",
"When It Rains, It Pours ",
"Since it rarely rains, when it does it will be a huge storm.",
"When Pigs Fly  ",
"Something that will never ever happen.",
"Wild and Woolly ",
"Uncultured and without laws.",
"Wine and Dine ",
"When somebody is treated to an expensive meal.",
"Without A Doubt ",
"For certain.",
"X marks the spot ",
"A phrase that is said when someone finds something he/she has been looking for.",
"You Are What You Eat ",
"In order to stay healthy you must eat healthy foods.",
"You Can't Judge A Book By Its Cover ",
"Decisions shouldn't be made primarily on appearance.",
"You Can't Take it With You ",
"Enjoy what you have and not what you don't have, since when you die you cannot take things (such as money) with you.",
"Your Guess Is As Good As Mine ",
"I have no idea.",
"Zero Tolerance ",
"No crime or law breaking big or small will be overlooked.",
);
};