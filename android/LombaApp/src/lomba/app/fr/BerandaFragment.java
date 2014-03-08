package lomba.app.fr;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.CalegActivity;
import lomba.app.R;
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

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.inflater = inflater;
		final View res = inflater.inflate(R.layout.fr_beranda, container, false);
		ListView lsBeranda = V.get(res, R.id.lsBeranda);
		lsBeranda.setAdapter(adapter = new BerandaAdapter());

		loadBeranda();

		return res;
	}


	void loadBeranda() {
		Papi.get_beranda(Preferences.getFloat(Prefkey.loc_lat, 0), Preferences.getFloat(Prefkey.loc_lng, 0), new Papi.Clbk<Papi.Beranda>() {
			@Override
			public void success(final Papi.Beranda beranda) {
				BerandaFragment.this.beranda = beranda;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void failed(final Throwable ex) {
				// load againnn
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
		});
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

			tJudul.setText(new String[] {"Featured Caleg", "Most Popular Caleg", "Highest Rated Caleg"}[position]);

			final Papi.Caleg caleg = new Papi.Caleg[] {beranda.featured, beranda.most_commented, beranda.top_rated}[position];

			Picasso.with(getActivity()).load(caleg.foto_url).into(imgFoto);
			tDesc.setText(caleg.nama);
			rating.setRating(caleg.rating == null? 0: caleg.rating.avg);
			tRatingCount.setText(caleg.rating == null? "(0)": ("(" + caleg.rating.count + ")"));

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
	}

}
