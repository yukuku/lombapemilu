package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.rpc.Papi;
import yuku.afw.V;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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


		loadLengkap();
		loadLengkap2();

		display();
	}


	void display() {
		Picasso.with(this).load(U.bc(280, 340, info1.foto_url)).into(imgFoto1);
		Picasso.with(this).load(U.bc(280, 340, info2.foto_url)).into(imgFoto2);

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
