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

public class Papi {
	public static final String TAG = Papi.class.getSimpleName();
	private static final String APIKEY = "201042adb488aef2eb0efe21bdd3ca7f";

	static String BASE = "http://192.168.13.144/lomba_git/server/api.php";

	static AsyncHttpClient client = new AsyncHttpClient();

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
	}

	public static void comments(String calegId, final Clbk<Comment[]> clbk) {
		client.get(BASE, new RequestParams("m", "get_comments", "apiKey", APIKEY, "caleg_id", calegId), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONArray response) {
				Log.d(TAG, "response: " + response.toString());
				Comment[] comments = new Gson().fromJson(response.toString(), Comment[].class);
				clbk.success(comments);
			}

			@Override
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
;		});

	}

	public static void geographic_point(double lat, double lng, final Clbk<Area[]> clbk) {
		Log.d(TAG, "@@geographic_point lat=" + lat + " lng=" + lng);
		// http://api.pemiluapi.org/geographic/api/point?apiKey=201042adb488aef2eb0efe21bdd3ca7f&lat=-6.87315&long=107.58682
		client.get("http://api.pemiluapi.org/geographic/api/point", new RequestParams("apiKey", APIKEY, "lat", lat, "long", lng), new JsonHttpResponseHandler("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				Log.d(TAG, "response: " + response.toString());

				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");
				final JSONArray a = r.optJSONArray("areas");

				final Area[] areas = new Gson().fromJson(a.toString(), Area[].class);
				clbk.success(areas);
			}

			@Override
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}

	public static void candidate_caleg(String dapil, String lembaga, String partai, final Clbk<Caleg[]> clbk) {
		Log.d(TAG, "@@candidate_caleg dapil=" + dapil + " lembaga=" + lembaga + " partai=" + partai);
		// http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&dapil=3201-00-0000
		client.get("http://api.pemiluapi.org/candidate/api/caleg", new RequestParams("apiKey", APIKEY, "tahun", 2014, "dapil", dapil, "lembaga", lembaga, "partai", partai), new JsonHttpResponseHandler("utf-8") {
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
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}

	public static void candidate_caleg2(String dapil, String lembaga, String partai, final Clbk<Caleg[]> clbk) {
		Log.d(TAG, "@@candidate_caleg dapil=" + dapil + " lembaga=" + lembaga + " partai=" + partai);
		// http://api.pemiluapi.org/candidate/api/caleg?apiKey=06ec082d057daa3d310b27483cc3962e&tahun=2014&lembaga=DPR&dapil=3201-00-0000
		client.get(BASE, new RequestParams("m", "get_calegs_by_dapil", "apiKey", APIKEY, "tahun", 2014, "dapil", dapil, "lembaga", lembaga, "partai", partai), new JsonHttpResponseHandler("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONArray response) {
				Log.d(TAG, "response array len: " + response.length());

				final Caleg[] calegs = new Gson().fromJson(response.toString(), Caleg[].class);
				clbk.success(calegs);
			}

			@Override
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}

	public static void candidate_partai(final Clbk<Partai[]> clbk) {
		Log.d(TAG, "@@candidate_partai");
		// http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e
		client.get("http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e", new JsonHttpResponseHandler() {
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
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}


	public static void get_beranda(double lat, double lng, final Clbk<Beranda> clbk) {
		Log.d(TAG, "@@get_beranda lat=" + lat + " lng=" + lng);
		// http://192.168.43.238/lomba_git/server/api.php?m=get_beranda&lat=-6.87315&lng=107.58682
		client.get(BASE, new RequestParams("m", "get_beranda", "lat", lat, "lng", lng), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				Log.d(TAG, "response: " + response.toString());

				final Beranda beranda = new Gson().fromJson(response.toString(), Beranda.class);
				clbk.success(beranda);
			}

			@Override
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}
}
