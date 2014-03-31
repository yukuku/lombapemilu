<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"> 
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Panel Admin Caleg Store</title>
	<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
	<?php echo Casset::render_css() ?>
</head>
<body>
	<div class="container">
		<h2>Caleg Store Management Page</h2>
		<?php echo $content ?>
	</div>
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<script>var baseUrl = '<?php echo Uri::base() ?>';</script>
	<?php echo Casset::render_js() ?>
</body>
</html>