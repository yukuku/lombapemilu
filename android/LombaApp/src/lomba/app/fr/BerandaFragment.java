package lomba.app.fr;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import lomba.app.CalegActivity;
import lomba.app.R;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

public class BerandaFragment extends Fragment {
	public static final String TAG = BerandaFragment.class.getSimpleName();
	private LayoutInflater inflater;

	BerandaAdapter adapter;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.inflater = inflater;
		final View res = inflater.inflate(R.layout.fr_beranda, container, false);
		ListView lsBeranda = V.get(res, R.id.lsBeranda);
		lsBeranda.setAdapter(adapter = new BerandaAdapter());
		return res;
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

			View clickable = V.get(view, R.id.clickable);
			clickable.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					startActivity(CalegActivity.create("3201-00-0000-0103"));
				}
			});

			tJudul.setText(new String[] {"Featured Caleg", "Most Popular Caleg", "Highest Rated Caleg"}[position]);
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

}
