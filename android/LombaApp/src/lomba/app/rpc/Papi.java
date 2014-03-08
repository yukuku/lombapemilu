package lomba.app.rpc;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONObject;

public class Papi {
	public static final String TAG = Papi.class.getSimpleName();
	private static final String APIKEY = "201042adb488aef2eb0efe21bdd3ca7f";

	static AsyncHttpClient client = new AsyncHttpClient();

	public static class Area {
		public String id;
		public String nama;
		public String lembaga;
		public String kind;
	}

	public interface Clbk<R> {
		void success(R r);
		void failed(Throwable ex);
	}

	public static void geographic_point(double lat, double lng, final Clbk<Area[]> clbk) {
		// http://api.pemiluapi.org/geographic/api/point?apiKey=201042adb488aef2eb0efe21bdd3ca7f&lat=-6.87315&long=107.58682
		client.get("http://api.pemiluapi.org/geographic/api/point", new RequestParams("apiKey", APIKEY, "lat", lat, "long", lng), new JsonHttpResponseHandler("utf-8") {
			@Override
			public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
				final JSONObject data = response.optJSONObject("data");
				final JSONObject r = data.optJSONObject("results");

				final Area[] areas = new Gson().fromJson(r.toString(), Area[].class);
				clbk.success(areas);
			}

			@Override
			public void onFailure(final Throwable e, final JSONObject errorResponse) {
				clbk.failed(e);
			}
		});
	}
}
