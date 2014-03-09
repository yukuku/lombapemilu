package lomba.app.fr;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.BandingActivity;
import lomba.app.CalegActivity;
import lomba.app.R;
import lomba.app.U;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import lomba.app.widget.RatingView;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.afw.widget.EasyAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yuku on 2014-3-8.
 */
public class CalegListFragment extends Fragment {
	ListView lsCaleg;

	CalegAdapter adapter;

	Set<Integer> pili = new HashSet<>();
	List<Papi.Caleg> calegs;
	ImageView loading;
	String partai;
	private LayoutInflater inflater;
	private RotateAnimation anim;

	public static CalegListFragment create(String partai) {
		CalegListFragment res = new CalegListFragment();
		Bundle args = new Bundle();
		args.putString("partai", partai);
		res.setArguments(args);
		return res;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.inflater = inflater;
		final View res = this.inflater.inflate(R.layout.caleg_list, container, false);

		this.partai = getArguments().getString("partai");

		lsCaleg = V.get(res, R.id.lsCaleg);
		loading = V.get(res, R.id.loading);
		anim = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(1000);
		anim.setRepeatCount(999);

		lsCaleg.setEmptyView(loading);
		lsCaleg.setAdapter(adapter = new CalegAdapter());
		lsCaleg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				if (pili.size() == 1 && !pili.contains(position)) {
					final Integer[] poss = pili.toArray(new Integer[0]);

					Papi.Caleg caleg1 = calegs.get(poss[0]);
					Papi.Caleg caleg2 = calegs.get(position);
					startActivity(BandingActivity.create(caleg1.id, U.ser(caleg1), caleg2.id, U.ser(caleg2)));

					pili.clear();

					adapter.notifyDataSetChanged();
				} else {

					Papi.Caleg caleg = calegs.get(position);
					startActivity(CalegActivity.create(caleg.id, U.ser(caleg)));
				}
			}
		});

		lsCaleg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				if (pili.contains(position)) pili.remove(position);
				else pili.add(position);

				if (pili.size() == 2) {
					final Integer[] poss = pili.toArray(new Integer[0]);

					Papi.Caleg caleg1 = calegs.get(poss[0]);
					Papi.Caleg caleg2 = calegs.get(poss[1]);
					startActivity(BandingActivity.create(caleg1.id, U.ser(caleg1), caleg2.id, U.ser(caleg2)));

					pili.clear();
				}

				adapter.notifyDataSetChanged();
				return true;
			}
		});

		loadCaleg();

		return res;
	}

	Handler h = new Handler();

	private void loadCaleg() {
		loading.startAnimation(anim);
		Papi.candidate_caleg2(Preferences.getString(Prefkey.dapil_dpr), "DPR", partai, new Papi.Clbk<Papi.Caleg[]>() {
			@Override
			public void success(final Papi.Caleg[] calegs) {
				CalegListFragment.this.calegs = Arrays.asList(calegs);
				adapter.notifyDataSetChanged();
				loading.clearAnimation();
				loading.setVisibility(View.GONE);
			}

			@Override
			public void failed(final Throwable ex) {
				loading.clearAnimation();
				loading.setVisibility(View.GONE);

				h.postDelayed(new Runnable() {
					@Override
					public void run() {
						loadCaleg();
					}
				}, 2000);
			}
		});
	}

	class CalegAdapter extends EasyAdapter {
		@Override
		public View newView(final int position, final ViewGroup parent) {
			return inflater.inflate(R.layout.item_caleg, parent, false);
		}

		@Override
		public void bindView(final View view, final int position, final ViewGroup parent) {
			TextView tNama = V.get(view, R.id.tNama);
			ImageView imgFoto = V.get(view, R.id.imgFoto);
			RatingView rating = V.get(view, R.id.rating);
			TextView tRatingCount = V.get(view, R.id.tRatingCount);
			View imgCentang = V.get(view, R.id.imgCentang);

			Papi.Caleg caleg = calegs.get(position);

			imgCentang.setVisibility(pili.contains(position)? View.VISIBLE: View.INVISIBLE);
			rating.setRating(caleg.rating == null? 0: caleg.rating.avg);
			tRatingCount.setText(caleg.rating == null? "(0)": ("(" + caleg.rating.count + ")"));

			tNama.setText(U.bagusinNama(caleg.nama));
			Picasso.with(CalegListFragment.this.getActivity()).load(U.bc(192, 192, caleg.foto_url)).into(imgFoto);
		}

		@Override
		public int getCount() {
			return calegs == null? 0: calegs.size();
		}

	}
}