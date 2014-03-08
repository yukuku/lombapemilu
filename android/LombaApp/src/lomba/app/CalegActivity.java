package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import com.squareup.picasso.Picasso;
import lomba.app.rpc.Papi;
import lomba.app.widget.FontTextView;
import lomba.app.widget.RatingView2;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalegActivity extends Activity {
	public static final String TAG = CalegActivity.class.getSimpleName();

	JazzyViewPager jazzy;
	String id;
	InfoAdapter adapter;
	Papi.Caleg info;
	private CommentAdapter commentsAdapter;

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

	Papi.Comment[] comments;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caleg);

		jazzy = V.get(this, R.id.jazzy);
		jazzy.setAdapter(adapter = new InfoAdapter());
		jazzy.setTransitionEffect(JazzyViewPager.TransitionEffect.CubeIn);

		commentsAdapter = new CommentAdapter();

		this.id = getIntent().getStringExtra("id");
		this.info = U.unser(getIntent().getByteArrayExtra("dt"));

		Papi.comments(info.id, new Papi.Clbk<Papi.Comment[]>() {
			@Override
			public void success(Papi.Comment[] comments) {
				commentsAdapter.setData(comments);
			}

			@Override
			public void failed(Throwable ex) {

			}
		});

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

		loadLengkap();
	}

	void loadLengkap() {
		Papi.candidate_caleg_detail(id, new Papi.Clbk<Papi.Caleg>() {
			@Override
			public void success(final Papi.Caleg caleg) {
				Log.d(TAG, "@@success diperbarui");
				CalegActivity.this.info.riwayat_pendidikan = caleg.riwayat_pendidikan;
				CalegActivity.this.info.riwayat_pekerjaan = caleg.riwayat_pekerjaan;
				CalegActivity.this.info.riwayat_organisasi = caleg.riwayat_organisasi;
				adapter.notifyDataSetChanged();
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
		TextView tDengan = V.get(res, R.id.tDengan);
		TextView tLokasi = V.get(res, R.id.tLokasi);
		ImageView imgStatus = V.get(res, R.id.imgStatus);
		LinearLayout anakcontainer = V.get(res, R.id.anakcontainer);

		tStatus.setText(U.bagusinNama(info.status_perkawinan));
		final boolean nikah = "KAWIN".equals(info.status_perkawinan) || "MENIKAH".equals(info.status_perkawinan);
		imgStatus.setImageResource(nikah? R.drawable.kawin: ("L".equals(info.jenis_kelamin)? R.drawable.belumkawin1: R.drawable.belumkawin2));

		if (nikah) {
			tDengan.setVisibility(View.VISIBLE);
			SpannableStringBuilder sb = new SpannableStringBuilder();
			sb.append("dengan ");
			int sbl = sb.length();
			sb.append(U.bagusinNama(info.nama_pasangan));
			sb.setSpan(new ForegroundColorSpan(0xffffffff), sbl, sb.length(), 0);
			tDengan.setText(sb);
		} else {
			tDengan.setVisibility(View.GONE);
		}

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

		tLokasi.setText(U.toTitleCase(sb.toString()));

		return res;
	}

	View organisasi(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_organisasi, container, false);


		LinearLayout wadah = V.get(res, R.id.wadah);
		final Papi.IdRingkasan[] idringkasans = info.riwayat_organisasi;

		addrows(wadah, idringkasans);

		return res;
	}

	View pendidikan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pendidikan, container, false);

		LinearLayout wadah = V.get(res, R.id.wadah);
		final Papi.IdRingkasan[] idringkasans = info.riwayat_pendidikan;

		addrows(wadah, idringkasans);

		return res;
	}

	private void addrows(final LinearLayout wadah, final Papi.IdRingkasan[] idringkasans) {
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
				final View v = getLayoutInflater().inflate(R.layout.item_riwayat, wadah, false);
				TextView tTahun = V.get(v, R.id.tTahun);
				TextView tKet = V.get(v, R.id.tKet);
				ImageView imgBenang = V.get(v, R.id.imgBenang);

				if (i == 0) imgBenang.setBackgroundResource(R.drawable.timelineatas);
				if (i == dua.length - 1) imgBenang.setBackgroundResource(R.drawable.timelinebawah);

				final String[] pisah = pisah(row.ringkasan);
				tTahun.setText(pisah[0]);
				tKet.setText(pisah[1]);

				wadah.addView(v);
			}
		}
	}

	View pekerjaan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pekerjaan, container, false);

		LinearLayout wadah = V.get(res, R.id.wadah);
		final Papi.IdRingkasan[] idringkasans = info.riwayat_pekerjaan;

		addrows(wadah, idringkasans);

		return res;
	}

	String grava(String email) {
		byte[] bb = email.trim().toLowerCase().getBytes();
		StringBuilder sb = new StringBuilder();
		for (byte b: bb) {
			if (b >= 0 && b < 16) sb.append("0").append(Integer.toHexString(b));
			else sb.append(Integer.toHexString(b & 0xff));
		}
		String hasil = sb.toString();

		return "http://www.gravatar.com/avatar/" + hasil + "?s=80&d=identicon";
	}

	static Matcher m = Pattern.compile("([0-9]+(?:-[0-9]+|-SEKARANG)?)(?:,?\\s*)(.*)").matcher("");

	String[] pisah(String r) {
		m.reset(r);
		if (m.find()) {
			return new String[] {m.group(1).replace("SEKARANG", "KINI"), m.group(2)};
		} else {
			return new String[] {"", r};
		}
	}

	class CommentAdapter extends EasyAdapter {

		private Papi.Comment[] comments;

		@Override
		public View newView(int position, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.comment_row, parent, false);
		}

		@Override
		public void bindView(View view, int position, ViewGroup parent) {
			ImageView commentProfile = V.get(view, R.id.gravatar_url);
			TextView commentTitle = V.get(view, R.id.comment_title);
			TextView commentContent = V.get(view, R.id.comment_content);
			commentTitle.setText(Html.fromHtml("<b>" + comments[position].title + "</b>"));
			commentContent.setText(comments[position].content);
			Picasso.with(CalegActivity.this).load(grava(comments[position].user_email)).into(commentProfile);
		}

		@Override
		public int getCount() {
			return comments == null ? 0 : comments.length;
		}

		public void setData(Papi.Comment[] comments) {
			this.comments = comments;
			notifyDataSetChanged();
		}
	}

	class InfoAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View res;

			if (position == 2) {
				res = pendidikan(container);
			} else if (position == 1) {
				res = keluarga(container);
			} else if (position == 4) {
				res = organisasi(container);
			} else if (position == 3) {
				res = pekerjaan(container);
			} else if (position == 5) {
				res = rating(container);
			} else {
				res = datadiri(container);
			}

			jazzy.setObjectForPosition(res, position);
			container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return res;
		}

		View rating(ViewGroup container) {
			View res = getLayoutInflater().inflate(R.layout.info_rating, container, false);

			ListView commentLs = V.get(res, R.id.comment_list);
			View headerView = getLayoutInflater().inflate(R.layout.comment_header, null);
			commentLs.addHeaderView(headerView);

			FontTextView rate = V.get(headerView, R.id.total_rating);
			FontTextView voter = V.get(headerView, R.id.total_voter);
			RatingView2 rv = V.get(headerView, R.id.rating);
			rv.setRating(info.rating.avg);
			rate.setText(String.format("%.1f", info.rating.avg));
			voter.setText("(" + info.rating.count + " rating)");

			commentLs.setAdapter(commentsAdapter);

			return res;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(jazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			return 6;
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
