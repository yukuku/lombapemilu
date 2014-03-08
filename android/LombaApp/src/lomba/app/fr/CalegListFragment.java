package lomba.app.fr;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.CalegActivity;
import lomba.app.R;
import lomba.app.U;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.afw.widget.EasyAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yuku on 2014-3-8.
 */
public class CalegListFragment extends Fragment {
	ListView lsCaleg;

	CalegAdapter adapter;

	List<Papi.Caleg> calegs;
	TextView emptyView;
	String partai;
	private LayoutInflater inflater;

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
		emptyView = V.get(res, android.R.id.empty);
		lsCaleg.setEmptyView(emptyView);
		lsCaleg.setAdapter(adapter = new CalegAdapter());
		lsCaleg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				Papi.Caleg caleg = calegs.get(position);
				startActivity(CalegActivity.create(caleg.id, U.ser(caleg)));
			}
		});

		Papi.candidate_caleg2(Preferences.getString(Prefkey.dapil_dpr), "DPR", partai, new Papi.Clbk<Papi.Caleg[]>() {
			@Override
			public void success(final Papi.Caleg[] calegs) {
				CalegListFragment.this.calegs = Arrays.asList(calegs);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void failed(final Throwable ex) {
				emptyView.setText("Gagal memuat daftar caleg.");
			}
		});

		return res;
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

			Papi.Caleg caleg = calegs.get(position);
			tNama.setText(U.bagusinNama(caleg.nama));
			Picasso.with(CalegListFragment.this.getActivity()).load(U.bc(192, 192, caleg.foto_url)).into(imgFoto);
		}

		@Override
		public int getCount() {
			return calegs == null? 0: calegs.size();
		}

	}
}