<a class="btn btn-danger" data-target="#hapusSemua" role="button" data-toggle="modal">Hapus semua data</a>

<div class="content" id="comments">
	<?php foreach($comments as $comment): ?>
		<!-- <?php echo print_r($comment, 1) ?> -->
		<div class="comment">
			<ul>
				<li class="image"><img class="comment-pic" src="<?php echo Util::gravatar($comment['user_email']) ?>"></li>
				<li><span class="glyphicon glyphicon-envelope"></span> <a href="mailto:<?php echo $comment['user_email'] ?>"><?php echo $comment['user_email']?></a></li>
				<li><span class="glyphicon glyphicon-time"></span> <span class="comment-time"><?php echo date("D, d M y - h:i", $comment['created']) ?></span></li>
				<li><span class="glyphicon glyphicon-star"></span> <span class="comment-star"><?php echo $comment['rating'] ?></span></li>
				<li><span class="glyphicon glyphicon-user"></span> 
					<a data-caleg-id="<?php echo $comment['caleg_id'] ?>" class="comment-caleg" href="<?php echo Uri::create('api/caleg')?>"><?php echo $comment['caleg_id']?></a>
				</li>
			</ul>
			<strong class="comment-title"><?php echo empty($comment['title']) ? '(Untitled)' : $comment['title'] ?></strong>
			<p class="comment-content"><?php echo empty($comment['content']) ? '(No comment)' : nl2br($comment['content']) ?></p>
		</div>
	<?php endforeach; ?>
</div>

<div class="modal fade" id="hapusSemua" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title">Konfirmasi</h4>
      </div>
      <div class="modal-body">
      Yakin hapus semua?
      </div>
      <div class="modal-footer">
        <a href="<?php echo Uri::create('api/hapus_data') ?>" role="button" class="btn btn-danger">Yakin!</a>
      </div>
    </div>
  </div>
</div>