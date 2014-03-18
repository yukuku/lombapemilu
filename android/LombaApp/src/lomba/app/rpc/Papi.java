package lomba.app.rpc;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thnkld.calegstore.app.BuildConfig;
import org.apache.http.Header;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Papi {
	public static final String TAG = Papi.class.getSimpleName();
	private static final String APIKEY = "201042adb488aef2eb0efe21bdd3ca7f";

	// static String BASE = "http://" + getprop("server", "cs2.anwong.com") + "/lombapemilu/server/api.php";
	static String BASE_V2 = "http://" + getprop("server", "cs2.anwong.com") + "/lombapemilu/server/public/api";

	static AsyncHttpClient client = new AsyncHttpClient();
	static {
		client.setMaxRetriesAndTimeout(1, 20000);
	}

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
			client.get(url, params, new AsyncHttpResponseHandler() {
				public String getResponseString(byte[] stringBytes, String charset) {
					try {
						return stringBytes == null? null: new String(stringBytes, charset);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public void onSuccess(final int statusCode, final Header[] headers, final byte[] responseBody) {
					String response = null;
					try {
						if (saklar.cancelled) return;
						response = getResponseString(responseBody, "utf-8");
						hasil.success(response);
					} finally {
						final int c = cnt.decrementAndGet();
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "[get " + noseri + " onSuccess] connections: " + c);
							Log.d(TAG, ("[" + noseri + "] ") + (saklar.cancelled? "- cancelled": ("- response: " + response)));
						}
					}
				}

				@Override
				public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBody, final Throwable error) {
					try {
						if (saklar.cancelled) return;
						hasil.failed(error);
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
			});
			return saklar;
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

	public static Saklar candidate_caleg_detail(String id, final Clbk<Caleg> clbk) {
		return get("http://api.pemiluapi.org/candidate/api/caleg/" + id, new RequestParams("apiKey", APIKEY), new Hasil() {
			@Override
			public void success(final String s) {
				final ApiObject___<Calegs___> o = new Gson().fromJson(s, new TypeToken<ApiObject___<Calegs___>>(){}.getType());
				final Caleg caleg = o.data.results.caleg[0];

				if (BuildConfig.DEBUG) {
					if (caleg.riwayat_pendidikan == null || caleg.riwayat_pendidikan.length == 0) {
						caleg.riwayat_pendidikan = ubek(new IdRingkasan[] {
						new IdRingkasan(1, "1957-1963, SD, SEKOLAH RAKYAT NEGERI, ACEH (placeholder)"),
						new IdRingkasan(2, "1963-1966, SLTP, SMP NEGERI 1, BANDA ACEH (placeholder)"),
						new IdRingkasan(3, "1963-1966 SLTP I NEGERI 1 BANDA ACEH (placeholder)"),
						new IdRingkasan(4, "1966-1967, SMA NEGERI I BANDA ACEH (placeholder)"),
						new IdRingkasan(5, "1967-1968, SLTA, SMA YPU, BANDUNG (placeholder)"),
						new IdRingkasan(6, "1969-1971, FAKULTAS PUBLISTIK UNIVERSITAS PADJAJARAN, BANDUNG (placeholder)"),
						new IdRingkasan(7, "1972-1984 STUDI ILMU KOMUNIKASI, ILMU POLITIK DAN SOSIOLOGI, WESTFAELISCHE - WILHELMS-UNIVERSITAET, MUENSTER, REP. FEDERAL JERMAN (placeholder)"),
						new IdRingkasan(8, "1983 S3, DR. PHIL. UNIVERSITAET, MUENSTER, REP. FEDERAL JERMAN (placeholder)"),
						}, 0.0);
					}

					if (caleg.riwayat_pekerjaan == null || caleg.riwayat_pekerjaan.length == 0) {
						caleg.riwayat_pekerjaan = ubek(new IdRingkasan[] {
						new IdRingkasan(1, "1998-1998, FKP DPR RI, ANGGOTA TIM AHLI, JAKARTA (placeholder)"),
						new IdRingkasan(2, "1998-1998, MPR RI, TIM AHLI, JAKARTA (placeholder)"),
						new IdRingkasan(3, "2000-2005, TIM PENASEHAT PRESIDEN URUSAN ACEH ANGGOTA, JAKARTA (placeholder)"),
						new IdRingkasan(4, "2000-2002, KEMENTRIAN POLKAM, PENASEHAT, JAKARTA (placeholder)"),
						new IdRingkasan(5, "2005-2007, PEMERINTAHAN, TIM AHLI DPR RI, JAKARTA (placeholder)"),
						new IdRingkasan(6, "2002-2005, PEMERINTAHAN, DUTA BESAR MESIR, MESIR (placeholder)"),
						}, 0.5);
					}

					if (caleg.riwayat_organisasi == null || caleg.riwayat_organisasi.length == 0) {
						caleg.riwayat_organisasi = ubek(new IdRingkasan[] {
						new IdRingkasan(1,"2013-SEKARANG, PARTAI NASDEM, KETUA DEWAN PAKAR DPP PARTAI NASDEM, JAKARTA (placeholder)"),
						new IdRingkasan(2,"2010-SEKARANG, ORMAS NASIONAL DEMOKRAT, ANGGOTA DEWAN PERTIMBANGAN, JAKARTA (placeholder)"),
						new IdRingkasan(3,"2007-SEKARANG, PENGURUS FORUM DUTA BESAR RI, JAKARTA (placeholder)"),
						new IdRingkasan(4,"2009-2013, FISIP UI, KETUA DEWAN GURU BESAR JAKARTA (placeholder)"),
						new IdRingkasan(5,"2010-2013, KOMITE PROFESOR UNTUK PERPUSTAKAAN UI, KETUA, JAKARTA (placeholder)"),
						new IdRingkasan(6,"2011-2014, PERHIMPUNAN ALUMNI JERMAN, WAKIL KETUA DEWAN KEHORMATAN (placeholder)"),
						}, 0.9);
					}
				}

				clbk.success(caleg);
			}

			@Override
			public void failed(final Throwable e) {
				clbk.failed(e);
			}
		});
	}

	private static IdRingkasan[] ubek(final IdRingkasan[] idRingkasans, double peluang) {
		List<IdRingkasan> res = new ArrayList<>();
		for (final IdRingkasan idRingkasan : idRingkasans) {
			if (Math.random() < peluang) {
				res.add(idRingkasan);
			}
		}
		return res.toArray(new IdRingkasan[res.size()]);
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
