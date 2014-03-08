package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import com.squareup.picasso.Picasso;
import lomba.app.rpc.Papi;
import yuku.afw.V;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalegActivity extends Activity {
	public static final String TAG = CalegActivity.class.getSimpleName();

	JazzyViewPager jazzy;
	String id;
	InfoAdapter adapter;
	Papi.Caleg info;

	public static Intent create(String id, byte[] dt) {
		Intent res = new Intent(App.context, CalegActivity.class);
		res.putExtra("id", id);
		res.putExtra("dt", dt);
		return res;
	}

	static ThreadLocal<DateFormat> dp = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caleg);

		jazzy = V.get(this, R.id.jazzy);
		jazzy.setAdapter(adapter = new InfoAdapter());
		jazzy.setTransitionEffect(JazzyViewPager.TransitionEffect.CubeIn);

		this.id = getIntent().getStringExtra("id");
		this.info = U.unser(getIntent().getByteArrayExtra("dt"));

		View bP1 = V.get(this, R.id.bP1);
		View bP2 = V.get(this, R.id.bP2);
		View bP3 = V.get(this, R.id.bP3);
		View bP4 = V.get(this, R.id.bP4);
		View bP5 = V.get(this, R.id.bP5);
		View bP6 = V.get(this, R.id.bP6);

		bP1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(0);
			}
		});
		bP2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(1);
			}
		});
		bP3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(2);
			}
		});
		bP4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(3);
			}
		});
		bP5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(4);
			}
		});
		bP6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(5);
			}
		});
	}

	View datadiri(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_datadiri, container, false);
		TextView tUsia = V.get(res, R.id.tUsia);
		TextView tAgama = V.get(res, R.id.tAgama);
		TextView tGender = V.get(res, R.id.tGender);
		TextView tKota = V.get(res, R.id.tKota);
		TextView tNama = V.get(res, R.id.tNama);
		TextView tPartai = V.get(res, R.id.tPartai);
		ImageView imgPartai = V.get(res, R.id.imgPartai);
		ImageView imgFoto = V.get(res, R.id.imgFoto);

		int umur = 0;
		if (info.tanggal_lahir != null) {
			try {
				final Date d = dp.get().parse(info.tanggal_lahir);
				int lahir = d.getYear();
				int kini = new Date().getYear();
				umur = kini - lahir;
			} catch (ParseException e) {
				Log.e(TAG, "e", e);
			}
		}
		tUsia.setText(umur == 0? "––": ("" + umur));

		tKota.setText(U.lower(info.tempat_lahir));
		tGender.setText("L".equals(info.jenis_kelamin)? "laki-laki": "perempuan");

		tAgama.setText(U.lower(info.agama));

		tPartai.setText(info.partai.nama);
		imgPartai.setImageResource(getResources().getIdentifier("partai_" + (info.partai.id), "drawable", getPackageName()));

		tNama.setText(U.bagusinNama(info.nama));

		Picasso.with(this).load(U.bc(320, 400, info.foto_url)).into(imgFoto);

		return res;
	}

	View keluarga(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_keluarga, container, false);
		TextView tStatus = V.get(res, R.id.tStatus);
		TextView tAnak = V.get(res, R.id.tAnak);
		TextView tLokasi = V.get(res, R.id.tLokasi);
		ImageView imgStatus = V.get(res, R.id.imgStatus);
		LinearLayout anakcontainer = V.get(res, R.id.anakcontainer);

		tStatus.setText(U.bagusinNama(info.status_perkawinan));
		imgStatus.setImageResource(("KAWIN".equals(info.status_perkawinan) || "MENIKAH".equals(info.status_perkawinan))? R.drawable.kawin: ("L".equals(info.jenis_kelamin)? R.drawable.belumkawin1: R.drawable.belumkawin2));

		int anak = 0;
		try {
			anak = Integer.parseInt(info.jumlah_anak);
		} catch (NumberFormatException e) {}

		tAnak.setText(info.jumlah_anak == null? "(Tidak ada data)": anak == 0? "Tidak ada anak": (anak + " anak"));

		for (int i = 0; i < anak; i++) {
			ImageView img = new ImageView(this);
			img.setImageResource(R.drawable.anak);
			anakcontainer.addView(img);
		}

		StringBuilder sb = new StringBuilder();
		if (info.kelurahan_tinggal != null) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kelurahan_tinggal);
		}
		if (info.kecamatan_tinggal != null) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kecamatan_tinggal);
		}
		if (info.kab_kota_tinggal != null) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kab_kota_tinggal);
		}
		if (info.provinsi_tinggal != null) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.provinsi_tinggal);
		}

		tLokasi.setText(sb);

		return res;
	}

	View organisasi(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_organisasi, container, false);
		return res;
	}

	View pendidikan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pendidikan, container, false);
		return res;
	}

	View pekerjaan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pekerjaan, container, false);
		return res;
	}



	class InfoAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View res;

			if (position == 3) {
				res = pendidikan(container);
			} else if (position == 1) {
				res = keluarga(container);
			} else if (position == 5) {
				res = organisasi(container);
			} else if (position == 4) {
				res = pekerjaan(container);
			} else {
				res = datadiri(container);
			}

			jazzy.setObjectForPosition(res, position);
			container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return res;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(jazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			return 7;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			if (view instanceof OutlineContainer) {
				return ((OutlineContainer) view).getChildAt(0) == obj;
			} else {
				return view == obj;
			}
		}
	}
}
