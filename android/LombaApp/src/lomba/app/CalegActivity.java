package lomba.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import com.squareup.picasso.Picasso;
import lomba.app.rpc.Papi;
import lomba.app.widget.FontButton;
import lomba.app.widget.FontEditTextView;
import lomba.app.widget.FontTextView;
import lomba.app.widget.RatingView2;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private MessageDigest md5;
	private static Account[] accountsByType;
	private PostCommentFragment postCommentFragment;
	private ImageButton bP1;
	private ImageButton bP2;
	private ImageButton bP3;
	private ImageButton bP4;
	private ImageButton bP5;
	private ImageButton bP6;

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
		accountsByType = AccountManager.get(this).getAccountsByType("com.google");

		jazzy.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(final int position) {
				updikon(position);
			}

			@Override
			public void onPageScrollStateChanged(final int state) {

			}
		});

		commentsAdapter = new CommentAdapter();

		this.id = getIntent().getStringExtra("id");
		this.info = U.unser(getIntent().getByteArrayExtra("dt"));

		bP1 = V.get(this, R.id.bP1);
		bP2 = V.get(this, R.id.bP2);
		bP3 = V.get(this, R.id.bP3);
		bP4 = V.get(this, R.id.bP4);
		bP5 = V.get(this, R.id.bP5);
		bP6 = V.get(this, R.id.bP6);

		bP1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(0);
			}
		});
		bP2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(1);
			}
		});
		bP3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(2);
			}
		});
		bP4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(3);
			}
		});
		bP5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(4);
			}
		});
		bP6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				gotopage(5);
			}
		});

		gotopage(0);

		loadLengkap();
		loadLengkap2();
	}

	void loadLengkap2() {

		Papi.comments(info.id, accountsByType[0].name, new Papi.Clbk<Papi.Comment[]>() {
			@Override
			public void success(final Papi.Comment[] comments) {
				commentsAdapter.setData(comments);
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

	void gotopage(int p) {
		jazzy.setCurrentItem(p);
		updikon(p);
	}

	private void updikon(final int p) {
		ImageButton[] bb = {bP1, bP2, bP3, bP4, bP5, bP6};
		for (int i = 0; i < bb.length; i++) {
			final ImageButton b = bb[i];
			b.setAlpha(i == p? 1.0f: 0.4f);
		}
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
		tUsia.setText(umur == 0? "––": ("" + umur + " thn"));

		tKota.setText(U.lower(info.tempat_lahir));
		tGender.setText("L".equals(info.jenis_kelamin)? "laki-laki": "perempuan");

		tAgama.setText(U.lower(info.agama));

		SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append(info.partai.nama);
		sb.setSpan(new ForegroundColorSpan(0xffffffff), 0, sb.length(), 0);
		sb.append(" no. urut ");
		int sbl = sb.length();
		sb.append("" + info.urutan);
		sb.setSpan(new ForegroundColorSpan(0xffffffff), sbl, sb.length(), 0);
		tPartai.setText(sb);

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

		tLokasi.setText(U.toTitleCase(gabung(info)));

		return res;
	}

	public static String gabung(Papi.Caleg info) {
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
		return sb.toString();
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
		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("md5");
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "e", e);
			}
		}

		byte[] bb = md5.digest(email.trim().toLowerCase().getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : bb) {
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
			return new String[] {m.group(1).replace("SEKARANG", "KINI"), U.toTitleCase(m.group(2))};
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
		public void bindView(View view, final int position, ViewGroup parent) {
			ImageView commentProfile = V.get(view, R.id.gravatar_url);
			TextView commentTitle = V.get(view, R.id.comment_title);
			TextView commentContent = V.get(view, R.id.comment_content);
			final CheckBox thumbsUp = V.get(view, R.id.thumbs_up);
			final CheckBox thumbsDown = V.get(view, R.id.thumbs_down);

			thumbsUp.setChecked(false);
			thumbsDown.setChecked(false);
			if ("1".equals(comments[position].is_up)) {
				thumbsUp.setChecked(true);
			}

			if ("0".equals(comments[position].is_up)) {
				thumbsDown.setChecked(true);
			}

			thumbsUp.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
					if(isChecked) {
						Papi.rateComment(accountsByType[0].name, comments[position].id, 1);
						thumbsDown.setChecked(false);
					}
				}
			});

			thumbsDown.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
					if(isChecked) {
						Papi.rateComment(accountsByType[0].name, comments[position].id, 0);
						thumbsUp.setChecked(false);
					}
				}
			});

			commentTitle.setText(Html.fromHtml("<b>" + comments[position].title + "</b>"));
			commentContent.setText(comments[position].content);
			Picasso.with(CalegActivity.this).load(grava(comments[position].user_email)).into(commentProfile);
		}

		@Override
		public int getCount() {
			return comments == null? 0: comments.length;
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
			final RatingView2 rv = V.get(headerView, R.id.rating);
			rv.setRating(info.rating.avg);
			rv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					final float lastdown = rv.getLastdown();
					int r = (int) (lastdown * 5) + 1;
					if (r < 1) r = 1;
					if (r > 5) r = 5;
					rv.setRating(r);
					showDialog();

				}
			});
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

	public class PostCommentFragment extends DialogFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.post_comment_dialog, container, false);
			ImageView ownPhoto = V.get(v, R.id.reviewer_img);
			FontTextView ownEmail = V.get(v, R.id.email);
			final FontEditTextView judulK = V.get(v, R.id.judul_k);
			final FontEditTextView isiK = V.get(v, R.id.isi_k);
			final RatingView2 ratingV = V.get(v, R.id.post_rating);
			FontButton submitB = V.get(v, R.id.submit);

			submitB.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					FontButton fb = (FontButton) view;
					fb.setText("Mengirim...");
					fb.setClickable(false);
					Papi.postComment(info.id, ratingV.getRating(), judulK.getText().toString(), isiK.getText().toString(), accountsByType[0].name, new Papi.Clbk<Object>() {

						@Override
						public void success(Object o) {

							loadLengkap2();
							postCommentFragment.dismissAllowingStateLoss();

						}

						@Override
						public void failed(Throwable ex) {

						}
					});
				}
			});

			Picasso.with(CalegActivity.this).load(grava(accountsByType[0].name)).into(ownPhoto);
			ownEmail.setText(accountsByType[0].name);
			getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));

			return v;
		}
	}
	void showDialog() {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		postCommentFragment = new PostCommentFragment();
		postCommentFragment.show(ft, "dialog");
	}
}
