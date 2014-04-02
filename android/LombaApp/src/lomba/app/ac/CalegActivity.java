package lomba.app.ac;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import com.squareup.picasso.Picasso;
import com.thnkld.calegstore.app.R;
import lomba.app.App;
import lomba.app.U;
import lomba.app.ac.base.BaseActivity;
import lomba.app.rpc.Papi;
import lomba.app.widget.FontButton;
import lomba.app.widget.FontEditTextView;
import lomba.app.widget.FontTextView;
import lomba.app.widget.RatingView2;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalegActivity extends BaseActivity {
	public static final String TAG = CalegActivity.class.getSimpleName();

	ViewPager jazzy;
	String id;
	InfoAdapter adapter;
	Papi.Caleg info;
	private CommentAdapter commentsAdapter;
	private String accountName;
	private PostCommentFragment postCommentFragment;
	private ImageButton bP1;
	private ImageButton bP2;
	private ImageButton bP3;
	private ImageButton bP4;
	private ImageButton bP5;
	private ImageButton bP6;
	int orderbycode = 1;
	Papi.Saklar commentloader;
	Papi.Saklar lengkaploader;
	List<Pair<LinearLayout, JenisTimeline>> willupdatewhendetailinfocompleteds = new ArrayList<>();
	private FontTextView tTotalRating;
	private FontTextView tTotalVoter;

	enum JenisTimeline {
		pendidikan, pekerjaan, organisasi;
	}

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
		// jazzy.setTransitionEffect(JazzyViewPager.TransitionEffect.CubeIn);
		accountName = U.getPrimaryAccountName();

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

		loadComments();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Papi.lupakan(commentloader);
		Papi.lupakan(lengkaploader);
	}

	void loadComments() {
		String order_by = orderbycode == 1? "jempoled": "updated";
		commentloader = Papi.ganti(commentloader, Papi.comments(info.id, accountName, order_by, new Papi.Clbk<Papi.Comment[]>() {
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
								loadComments();
							}
						});
					}
				}).start();
			}
		}));
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
		lengkaploader = Papi.ganti(lengkaploader, Papi.candidate_caleg_detail(id, new Papi.Clbk<Papi.Caleg>() {
			@Override
			public void success(final Papi.Caleg caleg) {
				Log.d(TAG, "@@success diperbarui");
				info.riwayat_pendidikan = caleg.riwayat_pendidikan;
				info.riwayat_pekerjaan = caleg.riwayat_pekerjaan;
				info.riwayat_organisasi = caleg.riwayat_organisasi;
				for (final Pair<LinearLayout, JenisTimeline> willupdatewhendetailinfocompleted : willupdatewhendetailinfocompleteds) {
					final Papi.IdRingkasan[] idringkasans;
					switch (willupdatewhendetailinfocompleted.second) {
						case pendidikan:
							idringkasans = info.riwayat_pendidikan;
							break;
						case pekerjaan:
							idringkasans = info.riwayat_pekerjaan;
							break;
						case organisasi:
							idringkasans = info.riwayat_organisasi;
							break;
						default:
							idringkasans = null;
							break;
					}

					displayTimeline(willupdatewhendetailinfocompleted.first, idringkasans);
				}
				info.rating = caleg.rating;
				displayRating();
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
		}));
	}

	View datadiri(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_datadiri, container, false);
		TextView tUrutan = V.get(res, R.id.tUrutan);
		TextView tUsia = V.get(res, R.id.tUsia);
		TextView tAgama = V.get(res, R.id.tAgama);
		TextView tGender = V.get(res, R.id.tGender);
		TextView tKota = V.get(res, R.id.tKota);
		TextView tNama = V.get(res, R.id.tNama);
		TextView tPartai = V.get(res, R.id.tPartai);
		ImageView imgPartai = V.get(res, R.id.imgPartai);
		ImageView imgFoto = V.get(res, R.id.imgFoto);

		tUrutan.setText("" + info.urutan);

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
			if (TextUtils.isEmpty(info.nama_pasangan)) {
				tDengan.setText("");
			} else {
				SpannableStringBuilder sb = new SpannableStringBuilder();
				sb.append("dengan ");
				int sbl = sb.length();
				sb.append(U.bagusinNama(info.nama_pasangan));
				sb.setSpan(new ForegroundColorSpan(0xffffffff), sbl, sb.length(), 0);
				tDengan.setText(sb);
			}
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

		tLokasi.setText(U.toTitleCase(gabungTinggal(info)));

		return res;
	}

	public static String gabungTinggal(Papi.Caleg info) {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(info.kelurahan_tinggal)) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kelurahan_tinggal);
		}
		if (!TextUtils.isEmpty(info.kecamatan_tinggal)) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kecamatan_tinggal);
		}
		if (!TextUtils.isEmpty(info.kab_kota_tinggal)) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.kab_kota_tinggal);
		}
		if (!TextUtils.isEmpty(info.provinsi_tinggal)) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(info.provinsi_tinggal);
		}
		return sb.toString();
	}

	View organisasi(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_organisasi, container, false);


		LinearLayout wadah = V.get(res, R.id.wadah);
		final Papi.IdRingkasan[] idringkasans = info.riwayat_organisasi;

		displayTimeline(wadah, idringkasans);
		willupdatewhendetailinfocompleteds.add(Pair.create(wadah, JenisTimeline.organisasi));

		return res;
	}

	View pendidikan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pendidikan, container, false);

		LinearLayout wadah = V.get(res, R.id.wadah);
		final Papi.IdRingkasan[] idringkasans = info.riwayat_pendidikan;

		displayTimeline(wadah, idringkasans);
		willupdatewhendetailinfocompleteds.add(Pair.create(wadah, JenisTimeline.pendidikan));

		return res;
	}

	static Matcher mtoint = Pattern.compile("[^0-9]").matcher("");

	static int toint(String s) {
		if (s == null || s.length() == 0) return 0;

		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			mtoint.reset(s);
			if (mtoint.find()) {
				try {
					return Integer.parseInt(s.substring(0, mtoint.start()));
				} catch (NumberFormatException e1) {
					return 0;
				}
			} else {
				return 0;
			}
		}
	}

	void displayTimeline(final LinearLayout wadah, final Papi.IdRingkasan[] idringkasans) {
		// clear first
		wadah.removeAllViews();

		// case 1: not loaded
		if (idringkasans == null) {
			wadah.addView(getLayoutInflater().inflate(R.layout.item_riwayat_masihloading, wadah, false));
		}
		// case 2: loaded but no data
		else if (idringkasans.length == 0) {
			wadah.addView(getLayoutInflater().inflate(R.layout.item_riwayat_kosong, wadah, false));
		}
		// case 3: loaded and have some data
		else {
			Papi.IdRingkasan[] dua = idringkasans.clone();
			Arrays.sort(dua, Collections.reverseOrder(new Comparator<Papi.IdRingkasan>() {
				@Override
				public int compare(final Papi.IdRingkasan lhs, final Papi.IdRingkasan rhs) {
					final String[] lhpisah = pisah(lhs.ringkasan);
					final String[] rhpisah = pisah(rhs.ringkasan);

					int lhtahun = toint(lhpisah[0]);
					int rhtahun = toint(rhpisah[0]);

					if (lhtahun != rhtahun) {
						return lhtahun - rhtahun;
					}

					// original
					return 0;
				}
			}));

			for (int i = 0; i < dua.length; i++) {
				final Papi.IdRingkasan row = dua[i];
				final View v = getLayoutInflater().inflate(R.layout.item_riwayat, wadah, false);
				TextView tTahun = V.get(v, R.id.tTahun);
				TextView tKet = V.get(v, R.id.tKet);
				ImageView imgBenang = V.get(v, R.id.imgBenang);

				if (i == 0 && i == dua.length - 1) imgBenang.setBackgroundResource(R.drawable.timelinesendirian);
				else if (i == 0) imgBenang.setBackgroundResource(R.drawable.timelineatas);
				else if (i == dua.length - 1) imgBenang.setBackgroundResource(R.drawable.timelinebawah);

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

		displayTimeline(wadah, idringkasans);
		willupdatewhendetailinfocompleteds.add(Pair.create(wadah, JenisTimeline.pekerjaan));

		return res;
	}

	View rating(ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_rating, container, false);

		ListView commentLs = V.get(res, R.id.comment_list);
		View headerView = getLayoutInflater().inflate(R.layout.comment_header, null);
		commentLs.addHeaderView(headerView);

		tTotalRating = V.get(headerView, R.id.total_rating);
		tTotalVoter = V.get(headerView, R.id.total_voter);
		final FontButton bSort = V.get(headerView, R.id.bSort);
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
				showDialogRating(r);

			}
		});
		bSort.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				PopupMenu pop = new PopupMenu(CalegActivity.this, bSort);
				final Menu menu = pop.getMenu();
				menu.add(0, 1, 0, "Komentar Terbaik");
				menu.add(0, 2, 0, "Komentar Terbaru");
				pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final MenuItem item) {
						orderbycode = item.getItemId();
						bSort.setText(item.getTitle());
						loadComments();
						return true;
					}
				});
				pop.show();

			}
		});
		displayRating();
		commentLs.setAdapter(commentsAdapter);

		return res;
	}

	private void displayRating() {
		if (info.rating == null) return;
		if (tTotalRating != null) tTotalRating.setText(String.format("%.1f", info.rating.avg));
		if (tTotalVoter != null) tTotalVoter.setText("(" + info.rating.count + " rating)");
	}

	String grava(String email) {
		String hasil = U.md5(email);

		return "http://www.gravatar.com/avatar/" + hasil + "?s=80&d=identicon";
	}

	static Matcher m1 = Pattern.compile("^([0-9]+(?:-[0-9]+|-\\w+)?)(?:,?\\s*)(.*)$", Pattern.CASE_INSENSITIVE).matcher("");
	// tahun di blk
	static Matcher m2 = Pattern.compile("^(.*?)(?:[,-]?\\s*)\\s*(?:\\(?)([0-9]{4,}(?:\\s*-\\s*[0-9]+|\\s*-\\s*(?:\\w+)?)?)(?:\\)?)\\s*$", Pattern.CASE_INSENSITIVE).matcher("");

	static LruCache<String, String[]> pisahcache = new LruCache<>(100);

	String[] pisah(String r) {
		String[] v = pisahcache.get(r);
		if (v != null) {
			return v;
		}
		v = pisah0(r);
		pisahcache.put(r, v);
		return v;
	}

	String[] pisah0(String r) {
		m1.reset(r);
		if (m1.matches()) {
			return new String[] {m1.group(1).replaceAll("(?i)(SEKARANG|KINI)", "skrg"), U.formatRiwayat(m1.group(2))};
		}

		// try 2
		m2.reset(r);
		if (m2.matches()) {
			return new String[] {m2.group(2).replaceAll("(?i)(SEKARANG|KINI)", "skrg"), U.formatRiwayat(m2.group(1))};
		}

		return new String[] {"", U.formatRiwayat(r)};
	}

	class CommentAdapter extends EasyAdapter {

		private Papi.Comment[] comments;

		CompoundButton.OnCheckedChangeListener thumbsUp_cc = new RadioButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				final Papi.Comment comment = (Papi.Comment) compoundButton.getTag();
				comment._jempol_atas = isChecked;
				if (isChecked) {
					comment._jempol_bawah = false;
				}
				sendRateComment(comment);
				notifyDataSetChanged();
			}
		};

		void sendRateComment(final Papi.Comment comment) {
			Papi.rateComment(accountName, comment.id, comment._jempol_atas? 1: comment._jempol_bawah? -1: 0);
		}

		CompoundButton.OnCheckedChangeListener thumbsDown_cc = new RadioButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				final Papi.Comment comment = (Papi.Comment) compoundButton.getTag();
				comment._jempol_bawah = isChecked;
				if (isChecked) {
					comment._jempol_atas = false;
				}
				sendRateComment(comment);
				notifyDataSetChanged();
			}
		};

		@Override
		public View newView(int position, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.comment_row, parent, false);
		}

		@Override
		public void bindView(View view, final int position, ViewGroup parent) {
			ImageView commentProfile = V.get(view, R.id.gravatar_url);
			TextView commentTitle = V.get(view, R.id.comment_title);
			TextView commentContent = V.get(view, R.id.comment_content);
			final TextView commentRate = V.get(view, R.id.sum_comment_rating);
			final CheckBox thumbsUp = V.get(view, R.id.thumbs_up);
			final CheckBox thumbsDown = V.get(view, R.id.thumbs_down);

			final Papi.Comment comment = comments[position];

			thumbsUp.setOnCheckedChangeListener(null);
			thumbsDown.setOnCheckedChangeListener(null);

			thumbsUp.setChecked(comment._jempol_atas);
			thumbsUp.setTag(comment);

			thumbsDown.setChecked(comment._jempol_bawah);
			thumbsDown.setTag(comment);

			thumbsUp.setOnCheckedChangeListener(thumbsUp_cc);
			thumbsDown.setOnCheckedChangeListener(thumbsDown_cc);

			commentTitle.setText(Html.fromHtml("<b>" + comment.title + "</b>"));
			commentContent.setText(comment.content);

			int sum = comment.sum;
			if (comment.is_up == 1) {
				if (!comment._jempol_atas) sum--;
				if (comment._jempol_bawah) sum--;
			}
			if (comment.is_up == -1) {
				if (!comment._jempol_bawah) sum++;
				if (comment._jempol_atas) sum++;
			}
			if (comment.is_up == 0) {
				if (comment._jempol_atas) sum++;
				if (comment._jempol_bawah) sum--;
			}
			commentRate.setText("" + sum);
			Picasso.with(CalegActivity.this).load(grava(comment.user_email)).into(commentProfile);
		}

		@Override
		public int getCount() {
			return comments == null? 0: comments.length;
		}

		public void setData(Papi.Comment[] comments) {
			this.comments = comments;

			// must make sure jempol betul
			if (comments != null) {
				for (final Papi.Comment comment : comments) {
					if (comment.is_up == 1) comment._jempol_atas = true;
					if (comment.is_up == -1) comment._jempol_bawah = true;
				}
			}

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

			// jazzy.setObjectForPosition(res, position);
			container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return res;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			// container.removeView(jazzy.findViewFromObject(position));
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

		private float rating;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);

			final Bundle args = getArguments();
			rating = args.getFloat("rating");
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.post_comment_dialog, container, false);
			ImageView ownPhoto = V.get(v, R.id.reviewer_img);
			FontTextView ownEmail = V.get(v, R.id.email);
			final FontEditTextView judulK = V.get(v, R.id.judul_k);
			final FontEditTextView isiK = V.get(v, R.id.isi_k);
			final RatingView2 ratingV = V.get(v, R.id.post_rating);
			ratingV.setRating(rating);
			FontButton submitB = V.get(v, R.id.submit);

			submitB.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					final String title = judulK.getText().toString();
					final String content = isiK.getText().toString();

					// validasi isi dulu
					if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
						new AlertDialog.Builder(getDialog().getContext())
						.setMessage("Tulis judul dan isi komentarnya dong, biar berguna bagi yang lain :)")
						.setPositiveButton("OK", null)
						.show();
						return;
					}

					final FontButton fb = (FontButton) view;
					final CharSequence sebelumnya = fb.getText();
					fb.setText("Mengirim...");
					fb.setEnabled(false);

					Papi.postComment(info.id, ratingV.getRating(), title, content, accountName, new Papi.Clbk<Object>() {

						@Override
						public void success(Object o) {
							loadComments();
							loadLengkap();
							postCommentFragment.dismissAllowingStateLoss();

							// ask everyone to refresh
							LocalBroadcastManager.getInstance(App.context).sendBroadcast(new Intent(MainActivity.CALEG_BERUBAH));
						}

						@Override
						public void failed(Throwable ex) {
							fb.setEnabled(true);
							fb.setText(sebelumnya);
						}
					});
				}
			});

			Picasso.with(CalegActivity.this).load(grava(accountName)).into(ownPhoto);
			ownEmail.setText(accountName);
			getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x80000000));

			return v;
		}
	}
	void showDialogRating(float rating) {
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
		final Bundle args = new Bundle();
		args.putFloat("rating", rating);
		postCommentFragment.setArguments(args);
		postCommentFragment.show(ft, "dialog");

	}
}
