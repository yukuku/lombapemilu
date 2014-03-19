<?php
namespace Fuel\Tasks;

use \Fuel\Core\Cache;

class Cacher {
	public $apiKey = '06ec082d057daa3d310b27483cc3962e';
	public function run($lembaga = 'DPR') {
		Cache::delete_all();
		
		//First get all provinces, and then cache it
		$provinces = '';
		try {
			$provinces = Cache::get('provinces'); 
		} catch(\CacheNotFoundException $e) {
			$provinces = file_get_contents("http://api.pemiluapi.org/candidate/api/provinsi?apiKey=" . $this->apiKey);			
		}
		
		//Get all partais available, to be used much later, this is just a preparatory step
		try {
			$parties = Cache::get('partais');
		} catch(\CacheNotFoundException $e) {
			$parties = file_get_contents("http://api.pemiluapi.org/candidate/api/partai?apiKey=78cc1abf180f90c46500f924248e6173");
		}
		
		$parties = json_decode($parties);
		$parties = $parties->data->results->partai;
		
		$provinces = json_decode($provinces);
		$provinces = $provinces->data->results->provinsi;
		
		//Get all dapils for every province
		foreach($provinces as $province) {
			echo "Getting dapils from province " . $province->nama_lengkap . " within lembaga " . $lembaga . "\n";
			$provinceId = $province->id;
			
			try {
				$dapils = Cache::get('dapils_' . $provinceId);
			} catch(\CacheNotFoundException $e) {
				$dapils = file_get_contents("http://api.pemiluapi.org/candidate/api/dapil?apiKey=78cc1abf180f90c46500f924248e6173&provinsi=" . $provinceId . "&lembaga=" . $lembaga);
			}

			$dapils = json_decode($dapils);
			$dapils = $dapils->data->results->dapil;
			foreach($dapils as $dapil) {
				foreach($parties as $party) {
					echo "\tGetting all calegs from partai " . $party->nama . ", dapil " . $dapil->nama_lengkap . " within lembaga " . $lembaga . "\n";
					$dapilId = $dapil->id;
					$cacheKey = 'get_calegs_by_dapil_' . md5($dapilId . '|' . $party->id . '|' . $lembaga);
					
					try {
						$calegs = Cache::get($cacheKey);
					} catch(\CacheNotFoundException $e) {
						$calegs = file_get_contents('http://api.pemiluapi.org/candidate/api/caleg?apiKey=' . $this->apiKey . '&partai=' . $party->id . '&tahun=2014&lembaga=' . $lembaga . '&dapil=' . $dapilId);
						Cache::set($cacheKey, $calegs, 3600);
					}
					
				}
			}
		}
	}
}