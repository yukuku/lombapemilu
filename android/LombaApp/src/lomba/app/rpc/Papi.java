package lomba.app.rpc;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.thnkld.calegstore.app.BuildConfig;
import lomba.app.util.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Papi {
	public static final String TAG = Papi.class.getSimpleName();
	private static final String APIKEY = "201042adb488aef2eb0efe21bdd3ca7f";

	// static String BASE = "http://" + getprop("server", "cs2.anwong.com") + "/lombapemilu/server/api.php";
	static String BASE_V2 = "http://" + getprop("server", "cs2.anwong.com") + "/lombapemilu/server/public/api";

	static OkHttpClient client = new OkHttpClient();
	static Handler mainHandler = new Handler(Looper.getMainLooper());
	static AtomicInteger cnt = new AtomicInteger();
	static AtomicInteger noseri = new AtomicInteger();

	public static class Saklar {
		boolean cancelled;
		void cancel() {
			cancelled = true;
		}
	}

	static String getprop(String key, final String def) {
		try {
			Class clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getDeclaredMethod("get", String.class);
			String prop = (String)method.invoke(null, key);
			if (prop == null || prop.length() == 0) {
				return def;
			}
			return prop;
		} catch (Exception e) {
			Log.e(TAG, "ga bisa getprop", e);
			return def;
		}
	}

	public static class ApiObject___<T> {
		public Data___<T> data;
	}

	public static class Data___<T> {
		public T results;
	}

	public static class Areas___ {
		public Area[] areas;
	}

	public static class Calegs___ {
		public Caleg[] caleg;
	}

	public static class Partais___ {
		public Partai[] partai;
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
		void failed(Throwable e);
	}

	public interface Hasil {
		void success(String s);
		void failed(Throwable e);
	}

	public static class Comment {
		public int id;
		public String title;
		public String user_email;
		public String content;
		public int is_up;
		public int sum;

		// ini dipake buat listview ajaa... custom data gitu
		public boolean _jempol_atas;
		public boolean _jempol_bawah;
	}

	static Saklar get(String url, RequestParams params, final Hasil hasil) {
		final Saklar saklar = new Saklar();

		final int noseri = Papi.noseri.incrementAndGet();
		try {
			final URL url2;
			String paramstring = params == null? "": params.toString();
			if (url.contains("?")) {
				if (paramstring.length() > 0) {
					url2 = new URL(url + "&" + paramstring);
				} else {
					url2 = new URL(url);
				}
			} else {
				if (paramstring.length() > 0) {
					url2 = new URL(url + "?" + paramstring);
				} else {
					url2 = new URL(url);
				}
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					final HttpURLConnection conn = client.open(url2);
					conn.setConnectTimeout(20000);
					conn.setReadTimeout(20000);

					InputStream stream = null;
					Throwable e = null;
					byte[] responseBody = null;
					boolean error = false;
					try {
						conn.getResponseCode(); // trigger io
						stream = conn.getInputStream();

						if (stream == null) {
							throw new IOException("no input stream");
						}

						ByteArrayOutputStream os = new ByteArrayOutputStream();
						byte[] buf = new byte[1024];
						while (true) {
							final int read = stream.read(buf);
							if (read < 0) break;
							os.write(buf, 0, read);
						}
						responseBody = os.toByteArray();
					} catch (IOException e1) {
						error = true;
						e = e1;
						if (stream == null) {
							stream = conn.getErrorStream();
						}

						if (stream != null) {
							try {
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								byte[] buf = new byte[1024];
								while (true) {
									final int read = stream.read(buf);
									if (read < 0) break;
									os.write(buf, 0, read);
								}
								responseBody = os.toByteArray();
							} catch (IOException e2) {
								Log.e(TAG, "still error, oh no", e);
							}
						}
					}

					if (!error) {
						String response = null;
						try {
							response = getResponseString(responseBody, "utf-8");
							if (saklar.cancelled) return;
							final String responsefinal = response;
							mainHandler.post(new Runnable() {
								@Override
								public void run() {
									hasil.success(responsefinal);
								}
							});
						} finally {
							final int c = cnt.decrementAndGet();
							if (BuildConfig.DEBUG) {
								Log.d(TAG, "[get " + noseri + " onSuccess] connections: " + c);
								Log.d(TAG, ("[" + noseri + "] ") + (saklar.cancelled? "- cancelled": ("- response: " + response)));
							}
						}
					} else {
						try {
							if (saklar.cancelled) return;
							final Throwable efinal = e;
							mainHandler.post(new Runnable() {
								@Override
								public void run() {
									hasil.failed(efinal);
								}
							});
						} finally {
							final int c = cnt.decrementAndGet();
							if (BuildConfig.DEBUG) {
								Log.d(TAG, "[get " + noseri + " onFailure] connections: " + c);
								if (saklar.cancelled) {
									Log.d(TAG, ("[" + noseri + "] ") + "- cancelled");
								} else {
									String response = getResponseString(responseBody, "utf-8");
									Log.d(TAG, ("[" + noseri + "] ") + "- error: " + error);
									Log.d(TAG, ("[" + noseri + "] ") + "- response: " + response);
								}
							}
						}
					}
				}

				public String getResponseString(byte[] stringBytes, String charset) {
					try {
						return stringBytes == null? null: new String(stringBytes, charset);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}).start();

			return saklar;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			final int c = cnt.incrementAndGet();
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "[get " + noseri + " start] connections: " + c);
				Log.d(TAG, ("[" + noseri + "] ") + "- url: " + url);
				Log.d(TAG, ("[" + noseri + "] ") + "- params: " + params);
			}
		}
	}

	public static Saklar rateComment(String email, int rating_id, int is_up) {
		return get(BASE_V2 + "/rate_comment", new RequestParams("user_email", email, "rating_id", rating_id, "is_up", is_up), new Hasil() {
			@Override
			public void success(final String s) {}

			@Override
			public void failed(final Throwable e) {}
		});
	}

	public static Saklar comments(String caleg_id, String user_email, String order_by, final Clbk<Comment[]> clbk) {
		return get(BASE_V2 + "/comments", new RequestParams("caleg_id", caleg_id, "user_email", user_email, "order_by", order_by), new Hasil() {
			@Override
			public void success(final String s) {
				Log.d(TAG, "response: " + s);
				Comment[] comments = new Gson().fromJson(s, Comment[].class);
				clbk.success(comments);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar postComment(String caleg_id, double rating, String title, String content, String email, final Clbk<Object> clbk) {
		return get(BASE_V2 + "/rate_comment_caleg", new RequestParams("caleg_id", caleg_id, "rating", rating, "title", title, "content", content, "user_email", email), new Hasil() {
			@Override
			public void success(final String s) {
				clbk.success(s);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar geographic_point(double lat, double lng, final Clbk<Area[]> clbk) {
		// http://api.pemiluapi.org/geographic/api/point?apiKey=201042adb488aef2eb0efe21bdd3ca7f&lat=-6.87315&long=107.58682
		return get("http://api.pemiluapi.org/geographic/api/point", new RequestParams("apiKey", APIKEY, "lat", lat, "long", lng), new Hasil() {
			@Override
			public void success(final String s) {
				Log.d(TAG, "response: " + s);

				final ApiObject___<Areas___> o = new Gson().fromJson(s, new TypeToken<ApiObject___<Areas___>>(){}.getType());
				clbk.success(o.data.results.areas);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar candidate_caleg_detail(String caleg_id, final Clbk<Caleg> clbk) {
		return get(BASE_V2 + "/caleg", new RequestParams("caleg_id", caleg_id), new Hasil() {
			@Override
			public void success(final String s) {
				final ApiObject___<Calegs___> o = new Gson().fromJson(s, new TypeToken<ApiObject___<Calegs___>>() {}.getType());
				final Caleg caleg = o.data.results.caleg[0];
				clbk.success(caleg);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar candidate_caleg2(String dapil, String lembaga, String partai, final Clbk<Caleg[]> clbk) {
		return get(BASE_V2 + "/calegs_by_dapil", new RequestParams("dapil", dapil, "lembaga", lembaga, "partai", partai), new Hasil() {
			@Override
			public void success(final String s) {
				final Caleg[] calegs = new Gson().fromJson(s, Caleg[].class);
				clbk.success(calegs);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar candidate_partai(final Clbk<Partai[]> clbk) {
		// http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e
		return get("http://api.pemiluapi.org/candidate/api/partai?apiKey=06ec082d057daa3d310b27483cc3962e", null, new Hasil() {
			@Override
			public void success(final String s) {
				final ApiObject___<Partais___> o = new Gson().fromJson(s, new TypeToken<ApiObject___<Partais___>>(){}.getType());
				clbk.success(o.data.results.partai);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar get_beranda(String dapil, final String lembaga, final Clbk<Beranda> clbk) {
		return get(BASE_V2 + "/beranda", new RequestParams("dapil", dapil, "lembaga", lembaga), new Hasil() {
			@Override
			public void success(final String s) {
				final Beranda beranda = new Gson().fromJson(s, Beranda.class);
				clbk.success(beranda);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	public static Saklar subscribe(String dapils, String registration_id, String installation_id, final Clbk<Void> clbk) {
		return get(BASE_V2 + "/subscribe", new RequestParams("dapils", dapils, "registration_id", registration_id, "installation_id", installation_id), new Hasil() {
			@Override
			public void success(final String s) {
				Log.d(TAG, "@@subscribe response: " + s);
				clbk.success(null);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	/** jangan peduli lg dengan ini */
	public static void lupakan(Saklar saklar) {
		if (saklar == null) return;
		saklar.cancel();
	}

	public static Saklar ganti(Saklar lama, Saklar baru) {
		lupakan(lama);
		return baru;
	}
}
