$('#hapusSemua .modal-footer a').prop('href', $('#hapusSemua .modal-footer a').prop('href') + '?h=' + 'inihesyangdatangnyadarijs');

$('#comments').on('click', '.comment-caleg', function(e) {
	var $that = $(this);
	if($that.next().length == 0) {
		$that.after('<img class="caleg-loader" src="' + baseUrl + 'assets/img/ajax-loader.gif' + '">');
	}
	
	$.get(baseUrl + 'api/caleg', {'caleg_id': $(this).data('calegId') }, function(resp) {
		var caleg = resp['data']['results']['caleg'][0];
		alert("Nama caleg: " + caleg['nama'] + "\n"
			+ "Dapil: " + caleg['dapil']['nama'] + "\n" 
			+ "Partai: " + caleg['partai']['nama'] + "\n"
			+ "Lembaga: " + caleg['lembaga']
		);
		$that.next('img').remove();
	}, 'json');
	e.preventDefault();
});