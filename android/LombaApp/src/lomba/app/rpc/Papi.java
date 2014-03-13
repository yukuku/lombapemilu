package lomba.app.rpc;

import android.util.Log;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Papi {
	public static final String TAG = Papi.class.getSimpleName();
	private static final String APIKEY = "201042adb488aef2eb0efe21bdd3ca7f";

	static String BASE = "http://" + "192.168.2.6"  /*getprop("server")*/ + "/lomba_git/server/public/api/";

	static AsyncHttpClient client = new AsyncHttpClient();
	static {
		Log.d(TAG, BASE);
		client.setMaxRetriesAndTimeout(1, 20000);
	}

	static String getprop(String key) {
		try {
			Class clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getDeclaredMethod("get", String.class);
			String prop = (String)method.invoke(null, key);
			return prop;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Area {
		public String id;
		public String nama;
		public String lembaga;
		public String kind;
	}

	public static class IdNama implements Serializable {
		public String id;
		public String nama;
	}

	public static class IdRingkasan implements Serializable {

		public IdRingkasan(final int id, final String ringkasan) {
			this.id = id;
			this.ringkasan = ringkasan;
		}

		public int id;
		public String ringkasan;
	}

	public static class Rating implements Serializable {
		public int count;
		public float avg;
	}

	public static class Caleg implements Serializable {
		public String agama;
		public IdNama dapil;
		public String foto_url;
		public String id;
		public String jenis_kelamin;
		public String jumlah_anak;
		public String kab_kota_tinggal;
		public String kecamatan_tinggal;
		public String kelurahan_tinggal;
		public String lembaga;
		public String nama;
		public String nama_pasangan;
		public IdNama partai;
		public IdNama provinsi;
		public String provinsi_tinggal;
		public String status_perkawinan;
		public int tahun;
		public String tanggal_lahir;
		public String tempat_lahir;
		public int urutan;

		public IdRingkasan[] riwayat_pendidikan;
		public IdRingkasan[] riwayat_pekerjaan;
		public IdRingkasan[] riwayat_organisasi;

		public Rating rating;
	}

	public static class Partai {
		public String nama;
		public String nama_lengkap;
		public String url_facebook;
		public String url_twitter;
		public String url_logo_medium;
		public String url_logo_small;
		public String url_situs;
		public int id;
	}

	public static class Beranda {
		public Caleg top_rated;
		public Caleg most_commented;
		public Caleg featured;
	}

	public interface Clbk<R> {
		void success(R r);
		void failed(Throwable ex);
	}

	public static class Comment {
		public int id;
		public String title;
		public String user_email;
		public String content;
		public String is_up;
		public int sum;
	}

	public static void rateComment(String email, int id, int is_up) {
		Log.d(TAG, BASE + "?m=rate_comment&user_email=" + email + "&comment_id=" + id + "&is_up=" + is_up);
		client.get(BASE, new RequestParams("m", "rate_comment", "user_email", email, "comment_id", id, "is_up", is_up), new JsonHttpResponseHandler());
	}

	public static void comments(String calegId, String user_email, final Clbk<Comment[]> clbk) {
		Log.d(TAG, BASE + "?m=get_comments&caleg_id=" + calegId + "&user_email=" + user_email);
		client.get(BASE, new RequestParams("m", "get_comments", "apiKey", APIKEY, "caleg_id", calegId, "user_email", user_email), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONArray response) {
				Log.d(TAG, "response: " + response.toString());
				Comment[] comments = new Gson().fromJson(response.toString(), Comment[].class);
				clbk.success(comments);
			}

			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			};
		});
	}

	public static void postComment(String caleg_id, double rating, String title, String content, String email, final Clbk<Object> clbk) {
		Log.d(TAG, "postComment calegId=" + caleg_id + " rating=" + rating + " title=" + title + " content=" + content + " user_email=" + email);
		client.get(BASE, new RequestParams("m", "rate_comment_caleg", "caleg_id", caleg_id, "rating", rating, "title", title, "content", content, "user_email", email), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				clbk.success(response);
			}

			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}

		});
	}

	public static void geographic_point(double lat, double lng, final Clbk<Area[]> clbk) {
		Log.d(TAG, "@@geographic_point lat=" + lat + " lng=" + lng);
		// http://api.pemiluapi.org/geographic/api/point?apiKey=201042adb488aef2eb0efe21bdd3ca7f&lat=-6.87315&long=107.58682
		client.get("http://api.pemiluapi.org/geographic/api/point", new RequestParams("apiKey", APIKEY, "lat", lat, "long", lng), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				Log.d(TAG, "response: " + response.toString());

				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");
				final JSONArray a = r.optJSONArray("areas");

				//Kalo data kosong dari latlng, minta Bandung
				//TODO: minta jakarta II, buat pemilih di luar indonesia
				//lat=6.87315&long=107.58682
				Log.d(TAG, String.valueOf(a.length()));
				if(a.length() == 0) {
					client.get("http://api.pemiluapi.org/geographic/api/point", new RequestParams("apiKey", APIKEY, "lat", -6.87315, "long", 107.58682), new JsonHttpResponseHandler2("utf-8") {
						@Override
						void onSegalaFailure(Throwable e) {
							clbk.failed(e);
						}

						@Override
						public void onSuccess(JSONObject response) {
							super.onSuccess(response);

							final JSONObject data = response.optJSONObject("data");
							final JSONObject r = data.optJSONObject("results");
							final JSONArray a = r.optJSONArray("areas");


							final Area[] areas = new Gson().fromJson(a.toString(), Area[].class);
							clbk.success(areas);
						}
					});
				} else {
					final Area[] areas = new Gson().fromJson(a.toString(), Area[].class);
					clbk.success(areas);
				}
			}

			@Override
			void onSegalaFailure(Throwable e) {
				clbk.failed(e);

			}
		});
	}

	public static void candidate_caleg(String dapil, String lembaga, String partai, final Clbk<Caleg[]> clbk) {
		Log.d(TAG, "@@candidate_caleg dapil=" + dapil + " lembaga=" + lembaga + " partai=" + partai);
		// http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&dapil=3201-00-0000
		client.get("http://api.pemiluapi.org/candidate/api/caleg", new RequestParams("apiKey", APIKEY, "tahun", 2014, "dapil", dapil, "lembaga", lembaga, "partai", partai), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");
				final JSONArray a = r.optJSONArray("caleg");

				Log.d(TAG, "caleg(s): " + a.toString());

				final Caleg[] calegs = new Gson().fromJson(a.toString(), Caleg[].class);
				clbk.success(calegs);
			}

			@Override
			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static void candidate_caleg_detail(String id, final Clbk<Caleg> clbk) {
		Log.d(TAG, "@@candidate_caleg_detail id=" + id);
		client.get("http://api.pemiluapi.org/candidate/api/caleg/" + id, new RequestParams("apiKey", APIKEY), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");
				final JSONArray a = r.optJSONArray("caleg");

				Log.d(TAG, "caleg(s): " + a.toString());

				final JSONObject o = a.optJSONObject(0);

				final Caleg caleg = new Gson().fromJson(o.toString(), Caleg.class);


				if (caleg.riwayat_pendidikan == null || caleg.riwayat_pendidikan.length == 0) {
					caleg.riwayat_pendidikan = ubek(new IdRingkasan[] {
					new IdRingkasan(1, "1957-1963, SD, SEKOLAH RAKYAT NEGERI, ACEH"),
					new IdRingkasan(2, "1963-1966, SLTP, SMP NEGERI 1, BANDA ACEH"),
					new IdRingkasan(3, "1963-1966 SLTP I NEGERI 1 BANDA ACEH"),
					new IdRingkasan(4, "1966-1967, SMA NEGERI I BANDA ACEH"),
					new IdRingkasan(5, "1967-1968, SLTA, SMA YPU, BANDUNG"),
					new IdRingkasan(6, "1969-1971, FAKULTAS PUBLISTIK UNIVERSITAS PADJAJARAN, BANDUNG"),
					new IdRingkasan(7, "1972-1984 STUDI ILMU KOMUNIKASI, ILMU POLITIK DAN SOSIOLOGI, WESTFAELISCHE - WILHELMS-UNIVERSITAET, MUENSTER, REP. FEDERAL JERMAN"),
					new IdRingkasan(8, "1983 S3, DR. PHIL. UNIVERSITAET, MUENSTER, REP. FEDERAL JERMAN"),
					});
				}

				if (caleg.riwayat_pekerjaan == null || caleg.riwayat_pekerjaan.length == 0) {
					caleg.riwayat_pekerjaan = ubek(new IdRingkasan[] {
					new IdRingkasan(1, "1998-1998, FKP DPR RI, ANGGOTA TIM AHLI, JAKARTA"),
					new IdRingkasan(2, "1998-1998, MPR RI, TIM AHLI, JAKARTA"),
					new IdRingkasan(3, "2000-2005, TIM PENASEHAT PRESIDEN URUSAN ACEH ANGGOTA, JAKARTA"),
					new IdRingkasan(4, "2000-2002, KEMENTRIAN POLKAM, PENASEHAT, JAKARTA"),
					new IdRingkasan(5, "2005-2007, PEMERINTAHAN, TIM AHLI DPR RI, JAKARTA"),
					new IdRingkasan(6, "2002-2005, PEMERINTAHAN, DUTA BESAR MESIR, MESIR"),
					});
				}

				if (caleg.riwayat_organisasi == null || caleg.riwayat_organisasi.length == 0) {
					caleg.riwayat_organisasi = ubek(new IdRingkasan[] {
					new IdRingkasan(1,"2013-SEKARANG, PARTAI NASDEM, KETUA DEWAN PAKAR DPP PARTAI NASDEM, JAKARTA"),
					new IdRingkasan(2,"2010-SEKARANG, ORMAS NASIONAL DEMOKRAT, ANGGOTA DEWAN PERTIMBANGAN, JAKARTA"),
					new IdRingkasan(3,"2007-SEKARANG, PENGURUS FORUM DUTA BESAR RI, JAKARTA"),
					new IdRingkasan(4,"2009-2013, FISIP UI, KETUA DEWAN GURU BESAR JAKARTA"),
					new IdRingkasan(5,"2010-2013, KOMITE PROFESOR UNTUK PERPUSTAKAAN UI, KETUA, JAKARTA"),
					new IdRingkasan(6,"2011-2014, PERHIMPUNAN ALUMNI JERMAN, WAKIL KETUA DEWAN KEHORMATAN"),
					});
				}

				clbk.success(caleg);
			}

			@Override
			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}

		});
	}

	private static IdRingkasan[] ubek(final IdRingkasan[] idRingkasans) {
		List<IdRingkasan> res = new ArrayList<>();
		for (final IdRingkasan idRingkasan : idRingkasans) {
			if (Math.random() < 0.75f) {
				res.add(idRingkasan);
			}
		}
		return res.toArray(new IdRingkasan[res.size()]);
	}

	public static void candidate_caleg2(String dapil, String lembaga, String partai, final Clbk<Caleg[]> clbk) {
		Log.d(TAG, "@@candidate_caleg dapil=" + dapil + " lembaga=" + lembaga + " partai=" + partai);
		// http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&dapil=3201-00-0000
		client.get(BASE, new RequestParams("m", "get_calegs_by_dapil", "apiKey", APIKEY, "tahun", 2014, "dapil", dapil, "lembaga", lembaga, "partai", partai), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONArray response) {
				Log.d(TAG, "response array len: " + response.length());

				final Caleg[] calegs = new Gson().fromJson(response.toString(), Caleg[].class);
				clbk.success(calegs);
			}

			@Override
			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}

		});
	}

	public static void candidate_partai(final Clbk<Partai[]> clbk) {
		Log.d(TAG, "@@candidate_partai");
		// http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e
		client.get("http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e", new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");
				final JSONArray a = r.optJSONArray("partai");

				Log.d(TAG, "partai(s): " + a.toString());

				final Partai[] partais = new Gson().fromJson(a.toString(), Partai[].class);
				clbk.success(partais);
			}

			@Override
			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}

		});
	}

	static abstract class JsonHttpResponseHandler2 extends JsonHttpResponseHandler {
		JsonHttpResponseHandler2(String encoding) {
			super(encoding);
		}

		AtomicBoolean udagagal = new AtomicBoolean();

		abstract void onSegalaFailure(Throwable e);

		@Override
		public void onFailure(final Throwable e, final JSONObject errorResponse) {
			super.onFailure(e, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(e);
		}

		@Override
		public void onFailure(final Throwable e, final JSONArray errorResponse) {
			super.onFailure(e, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(e);
		}

		@Override
		public void onFailure(final int statusCode, final Throwable e, final JSONArray errorResponse) {
			super.onFailure(statusCode, e, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(e);
		}

		@Override
		public void onFailure(final int statusCode, final Throwable e, final JSONObject errorResponse) {
			super.onFailure(statusCode, e, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(e);
		}

		@Override
		public void onFailure(final int statusCode, final Header[] headers, final String responseString, final Throwable throwable) {
			super.onFailure(statusCode, headers, responseString, throwable);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(throwable);
		}

		@Override
		public void onFailure(final int statusCode, final Header[] headers, final Throwable throwable, final JSONObject errorResponse) {
			super.onFailure(statusCode, headers, throwable, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(throwable);
		}

		@Override
		public void onFailure(final int statusCode, final Header[] headers, final Throwable throwable, final JSONArray errorResponse) {
			super.onFailure(statusCode, headers, throwable, errorResponse);
			if (udagagal.compareAndSet(false, true)) onSegalaFailure(throwable);
		}
	}


	public static void get_beranda(double lat, double lng, final String lembaga, final Clbk<Beranda> clbk) {
		Log.d(TAG, "@@get_beranda lat=" + lat + " lng=" + lng);
		// http://192.168.43.238/lomba_git/server/api.php?m=get_beranda&lat=-6.87315&lng=107.58682
		client.get(BASE + "beranda", new RequestParams("lat", lat, "lng", lng, "lembaga", lembaga), new JsonHttpResponseHandler2("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				Log.d(TAG, "response: " + response.toString());

				final Beranda beranda = new Gson().fromJson(response.toString(), Beranda.class);
				clbk.success(beranda);
			}

			@Override
			public void onSegalaFailure(final Throwable e) {
				clbk.failed(e);
			}
		});
	}
}
