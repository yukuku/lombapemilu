package lomba.app.fr;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.thnkld.calegstore.app.R;
import lomba.app.App;
import lomba.app.CalegActivity;
import lomba.app.U;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import lomba.app.widget.RatingView;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.afw.widget.EasyAdapter;

public class BerandaFragment extends Fragment {
	public static final String TAG = BerandaFragment.class.getSimpleName();
	private LayoutInflater inflater;

	BerandaAdapter adapter;

	Papi.Beranda beranda;
	private ImageView loading;
	private RotateAnimation anim;
	Papi.Saklar berandaloader;

	private BroadcastReceiver reload = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			loadBeranda();
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocalBroadcastManager.getInstance(App.context).registerReceiver(reload, new IntentFilter("LEMBAGA_BERUBAH"));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(App.context).unregisterReceiver(reload);
		Papi.lupakan(berandaloader);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.inflater = inflater;
		final View res = inflater.inflate(R.layout.fr_beranda, container, false);
		ListView lsBeranda = V.get(res, R.id.lsBeranda);
		loading = V.get(res, R.id.loading);
		lsBeranda.setAdapter(adapter = new BerandaAdapter());

		anim = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setRepeatCount(999);
		anim.setDuration(1000);

		loadBeranda();

		return res;
	}

	void loadBeranda() {
		final int lembaga_aktif = Preferences.getInt(Prefkey.lembaga_aktif, 1);

		loading.setVisibility(View.VISIBLE);
		loading.startAnimation(anim);

		berandaloader = Papi.ganti(berandaloader, Papi.get_beranda(U.getDapilDariLembaga(lembaga_aktif), U.getNamaLembaga(lembaga_aktif), new Papi.Clbk<Papi.Beranda>() {
			@Override
			public void success(final Papi.Beranda beranda) {
				BerandaFragment.this.beranda = beranda;
				adapter.notifyDataSetChanged();
				loading.clearAnimation();
				loading.setVisibility(View.GONE);
			}

			@Override
			public void failed(final Throwable ex) {
				// load againnn
				loading.clearAnimation();
				loading.setVisibility(View.GONE);
				new Thread(new Runnable() {
					@Override
					public void run() {
						SystemClock.sleep(2000);
						if (getActivity() != null)
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									loadBeranda();
								}
							});
					}
				}).start();
			}
		}));
	}

	class BerandaAdapter extends EasyAdapter {

		@Override
		public View newView(final int position, final ViewGroup parent) {
			return inflater.inflate(R.layout.item_feat, parent, false);
		}

		@Override
		public void bindView(final View view, final int position, final ViewGroup parent) {
			TextView tJudul = V.get(view, R.id.tJudul);
			ImageView imgFoto = V.get(view, R.id.imgFoto);
			TextView tDesc = V.get(view, R.id.tDesc);
			RatingView rating = V.get(view, R.id.rating);
			TextView tRatingCount = V.get(view, R.id.tRatingCount);
			ImageView imgPartai = V.get(view, R.id.imgPartai);
			TextView tPartai = V.get(view, R.id.tPartai);

			tJudul.setText(new String[] {"Featured Caleg", "Most Popular Caleg", "Highest Rated Caleg"}[position]);

			final Papi.Caleg caleg = new Papi.Caleg[] {beranda.featured, beranda.most_commented, beranda.top_rated}[position];

			Picasso.with(getActivity()).load(U.bc(240, 320, caleg.foto_url)).into(imgFoto);
			tDesc.setText(U.bagusinNama(caleg.nama));
			rating.setRating(caleg.rating == null? 0: caleg.rating.avg);
			tRatingCount.setText(caleg.rating == null? "(0)": ("(" + caleg.rating.count + ")"));

			try {
				imgPartai.setImageResource(getResources().getIdentifier("partai_" + (caleg.partai.id), "drawable", App.context.getPackageName()));
				tPartai.setText(caleg.partai.nama);
			} catch (NullPointerException e) {
				Log.e(TAG, "nurupo di partainya caleg");
			}

			View clickable = V.get(view, R.id.clickable);
			clickable.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					startActivity(CalegActivity.create(caleg.id, U.ser(caleg)));
				}
			});

		}

		@Override
		public int getCount() {
			return beranda == null? 0: 3;
		}

		@Override
		public boolean isEnabled(final int position) {
			return false;
		}
	}

}
