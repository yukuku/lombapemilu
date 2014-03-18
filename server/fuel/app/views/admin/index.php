<a class="btn btn-danger" data-target="#hapusSemua" role="button" data-toggle="modal">Hapus semua data</a>

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