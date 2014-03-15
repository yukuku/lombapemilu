package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.thnkld.calegstore.app.R;
import lomba.app.rpc.Papi;
import lomba.app.widget.RatingView;
import yuku.afw.V;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class BandingActivity extends Activity {
	public static final String TAG = BandingActivity.class.getSimpleName();

	String id1;
	String id2;
	Papi.Caleg info1;
	Papi.Caleg info2;

	public static Intent create(String id1, byte[] dt1, String id2, byte[] dt2) {
		Intent res = new Intent(App.context, BandingActivity.class);
		res.putExtra("id1", id1);
		res.putExtra("id2", id2);
		res.putExtra("dt1", dt1);
		res.putExtra("dt2", dt2);
		return res;
	}

	static ThreadLocal<DateFormat> dp = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	ImageView imgFoto1;
	ImageView imgFoto2;
	TextView tNama1;
	TextView tNama2;
	TextView tUrutan1;
	TextView tUrutan2;

	RatingView rating1;
	RatingView rating2;
	TextView tRatingCount1;
	TextView tRatingCount2;

	TextView tAgama1;
	TextView tAgama2;
	TextView tGender1;
	TextView tGender2;
	TextView tLokasi1;
	TextView tLokasi2;
	TextView tUsia1;
	TextView tUsia2;
	TextView tStatus1;
	TextView tStatus2;
	TextView tAnak1;
	TextView tAnak2;
	TextView tLokasiJuga1;
	TextView tLokasiJuga2;

	TextView tPendidikan1;
	TextView tPendidikan2;
	TextView tPekerjaan1;
	TextView tPekerjaan2;
	TextView tOrganisasi1;
	TextView tOrganisasi2;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.banding);

		id1 = getIntent().getStringExtra("id1");
		id2 = getIntent().getStringExtra("id2");

		info1 = U.unser(getIntent().getByteArrayExtra("dt1"));
		info2 = U.unser(getIntent().getByteArrayExtra("dt2"));



		imgFoto1 = V.get(this, R.id.imgFoto1);
		imgFoto2 = V.get(this, R.id.imgFoto2);
		tNama1 = V.get(this, R.id.tNama1);
		tNama2 = V.get(this, R.id.tNama2);
		tUrutan1 = V.get(this, R.id.tUrutan1);
		tUrutan2 = V.get(this, R.id.tUrutan2);

		rating1 = V.get(this, R.id.rating1);
		rating2 = V.get(this, R.id.rating2);
		tRatingCount1 = V.get(this, R.id.tRatingCount1);
		tRatingCount2 = V.get(this, R.id.tRatingCount2);

		tAgama1 = V.get(this, R.id.tAgama1);
		tAgama2 = V.get(this, R.id.tAgama2);
		tGender1 = V.get(this, R.id.tGender1);
		tGender2 = V.get(this, R.id.tGender2);
		tLokasi1 = V.get(this, R.id.tLokasi1);
		tLokasi2 = V.get(this, R.id.tLokasi2);
		tUsia1 = V.get(this, R.id.tUsia1);
		tUsia2 = V.get(this, R.id.tUsia2);
		tStatus1 = V.get(this, R.id.tStatus1);
		tStatus2 = V.get(this, R.id.tStatus2);
		tAnak1 = V.get(this, R.id.tAnak1);
		tAnak2 = V.get(this, R.id.tAnak2);
		tLokasiJuga1 = V.get(this, R.id.tLokasiJuga1);
		tLokasiJuga2 = V.get(this, R.id.tLokasiJuga2);

		tPendidikan1 = V.get(this, R.id.tPendidikan1);
		tPendidikan2 = V.get(this, R.id.tPendidikan2);
		tPekerjaan1 = V.get(this, R.id.tPekerjaan1);
		tPekerjaan2 = V.get(this, R.id.tPekerjaan2);
		tOrganisasi1 = V.get(this, R.id.tOrganisasi1);
		tOrganisasi2 = V.get(this, R.id.tOrganisasi2);


		loadLengkap();
		loadLengkap2();

		display();
	}


	void display() {
		Picasso.with(this).load(U.bc(getResources().getDimensionPixelSize(R.dimen.banding_foto_w), getResources().getDimensionPixelSize(R.dimen.banding_foto_h), info1.foto_url)).into(imgFoto1);
		Picasso.with(this).load(U.bc(getResources().getDimensionPixelSize(R.dimen.banding_foto_w), getResources().getDimensionPixelSize(R.dimen.banding_foto_h), info2.foto_url)).into(imgFoto2);

		tNama1.setText(U.bagusinNama(info1.nama));
		tNama2.setText(U.bagusinNama(info2.nama));

		tUrutan1.setText("" + info1.urutan);
		tUrutan2.setText("" + info2.urutan);

		rating1.setRating(info1.rating == null? 0: info1.rating.avg);
		rating2.setRating(info2.rating == null? 0: info2.rating.avg);
		tRatingCount1.setText(info1.rating == null? "(0)": ("(" + info1.rating.count + ")"));
		tRatingCount2.setText(info2.rating == null? "(0)": ("(" + info2.rating.count + ")"));

		tAgama1.setText(U.lower(info1.agama));
		tAgama2.setText(U.lower(info2.agama));

		tGender1.setText("L".equals(info1.jenis_kelamin)? "laki-laki": "perempuan");
		tGender2.setText("L".equals(info2.jenis_kelamin)? "laki-laki": "perempuan");

		tLokasi1.setText(U.lower(info1.tempat_lahir));
		tLokasi2.setText(U.lower(info2.tempat_lahir));

		int umur1 = 0;
		if (info1.tanggal_lahir != null) {
			try {
				final Date d = dp.get().parse(info1.tanggal_lahir);
				int lahir = d.getYear();
				int kini = new Date().getYear();
				umur1 = kini - lahir;
			} catch (ParseException e) {
				Log.e(TAG, "e", e);
			}
		}
		tUsia1.setText(umur1 == 0? "––": ("" + umur1 + " thn"));

		int umur2 = 0;
		if (info2.tanggal_lahir != null) {
			try {
				final Date d = dp.get().parse(info2.tanggal_lahir);
				int lahir = d.getYear();
				int kini = new Date().getYear();
				umur2 = kini - lahir;
			} catch (ParseException e) {
				Log.e(TAG, "e", e);
			}
		}
		tUsia2.setText(umur2 == 0? "––": ("" + umur2 + " thn"));


		tStatus1.setText(U.bagusinNama(info1.status_perkawinan));
		tStatus2.setText(U.bagusinNama(info2.status_perkawinan));

		{
			int anak = 0;
			try {
				anak = Integer.parseInt(info1.jumlah_anak);
			} catch (NumberFormatException e) {}

			tAnak1.setText(info1.jumlah_anak == null? "(Tidak ada data)": anak == 0? "Tidak ada anak": (anak + " anak"));
		}
		{
			int anak = 0;
			try {
				anak = Integer.parseInt(info2.jumlah_anak);
			} catch (NumberFormatException e) {}

			tAnak2.setText(info2.jumlah_anak == null? "(Tidak ada data)": anak == 0? "Tidak ada anak": (anak + " anak"));
		}


		tLokasiJuga1.setText(U.toTitleCase(CalegActivity.gabung(info1)));
		tLokasiJuga2.setText(U.toTitleCase(CalegActivity.gabung(info2)));

		tPendidikan1.setText(sum(info1.riwayat_pendidikan));
		tPendidikan2.setText(sum(info2.riwayat_pendidikan));
		tPekerjaan1.setText(sum(info1.riwayat_pekerjaan));
		tPekerjaan2.setText(sum(info2.riwayat_pekerjaan));
		tOrganisasi1.setText(sum(info1.riwayat_organisasi));
		tOrganisasi2.setText(sum(info2.riwayat_organisasi));
	}

	private String sum(final Papi.IdRingkasan[] idringkasans) {
		StringBuilder sb = new StringBuilder();
		if (idringkasans != null) {

			Papi.IdRingkasan[] dua = idringkasans.clone();
			Arrays.sort(dua, new Comparator<Papi.IdRingkasan>() {
				@Override
				public int compare(final Papi.IdRingkasan lhs, final Papi.IdRingkasan rhs) {
					return rhs.ringkasan.compareTo(lhs.ringkasan);
				}
			});

			for (int i = 0; i < dua.length; i++) {
				final Papi.IdRingkasan row = dua[i];
				if (sb.length() != 0) {
					sb.append("\n\n");
				}
				sb.append(U.toTitleCase(row.ringkasan));
			}
		}
		return sb.toString();
	}

	void loadLengkap2() {
		Papi.candidate_caleg_detail(id2, new Papi.Clbk<Papi.Caleg>() {
			@Override
			public void success(final Papi.Caleg caleg) {
				Log.d(TAG, "@@success diperbarui");
				BandingActivity.this.info2.riwayat_pendidikan = caleg.riwayat_pendidikan;
				BandingActivity.this.info2.riwayat_pekerjaan = caleg.riwayat_pekerjaan;
				BandingActivity.this.info2.riwayat_organisasi = caleg.riwayat_organisasi;
				display();
			}

			@Override
			public void failed(final Throwable ex) {
				// load againnn
				new Thread(new Runnable() {
					@Override
					public void run() {
						SystemClock.sleep(2000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadLengkap2();
							}
						});
					}
				}).start();
			}
		});
	}

	void loadLengkap() {
		Papi.candidate_caleg_detail(id1, new Papi.Clbk<Papi.Caleg>() {
			@Override
			public void success(final Papi.Caleg caleg) {
				Log.d(TAG, "@@success diperbarui");
				BandingActivity.this.info1.riwayat_pendidikan = caleg.riwayat_pendidikan;
				BandingActivity.this.info1.riwayat_pekerjaan = caleg.riwayat_pekerjaan;
				BandingActivity.this.info1.riwayat_organisasi = caleg.riwayat_organisasi;
				display();
			}

			@Override
			public void failed(final Throwable ex) {
				// load againnn
				new Thread(new Runnable() {
					@Override
					public void run() {
						SystemClock.sleep(2000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadLengkap();
							}
						});
					}
				}).start();
			}
		});
	}
}
