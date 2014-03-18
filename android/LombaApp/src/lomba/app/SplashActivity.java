package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.thnkld.calegstore.app.R;
import lomba.app.data.Dapil;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.afw.widget.EasyAdapter;

import java.util.List;

public class SplashActivity extends Activity {
	Handler h = new Handler();
	int lokasicekbrpkali = 0;
	TextView tStatus;
	Button bRetry;
	ImageView iProgress;
	Spinner cbDapilDpr;
	Spinner cbDapilDprd1;
	View bSave;

	Runnable ceklokasi = new Runnable() {
		@Override
		public void run() {
			tStatus.setText("Mengecek lokasi");

			lokasicekbrpkali++;

			if (lokasicekbrpkali > 5) {
				if (Preferences.getString(Prefkey.dapil_dpr) == null || Preferences.getString(Prefkey.dapil_dprd1) == null) {
					h.postDelayed(pilihmanual, 1000);
				} else {
					// sudah diset sebelumnya
					h.postDelayed(masukmain, 1000);
				}

				return;
			}

			double lat = Preferences.getFloat(Prefkey.loc_lat, 0);
			double lng = Preferences.getFloat(Prefkey.loc_lng, 0);

			if (lat == 0 && lng == 0) {
				h.postDelayed(ceklokasi, 2000);
			} else {
				h.postDelayed(cariarea, 100);
			}
		}
	};

	Runnable pilihmanual = new Runnable() {
		@Override
		public void run() {
			tStatus.setText("Daerah pemilihan:");
			DapilAdapter dprAdapter = new DapilAdapter(1);
			DapilAdapter dprd1Adapter = new DapilAdapter(2);
			iProgress.clearAnimation();
			iProgress.setVisibility(View.INVISIBLE);
			cbDapilDpr.setAdapter(dprAdapter);
			cbDapilDprd1.setAdapter(dprd1Adapter);
			cbDapilDpr.setVisibility(View.VISIBLE);
			cbDapilDprd1.setVisibility(View.VISIBLE);
			bSave.setVisibility(View.VISIBLE);
			bSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					int posDpr = cbDapilDpr.getSelectedItemPosition() - 1;
					int posDprd1 = cbDapilDprd1.getSelectedItemPosition() - 1;

					if (posDpr == -1 || posDprd1 == -1) {
						// jangan ngapa2in
						return;
					}

					Preferences.setString(Prefkey.dapil_dpr, Dapil.getKode(1, posDpr));
					Preferences.setString(Prefkey.dapil_dprd1, Dapil.getKode(2, posDprd1));

					h.postDelayed(masukmain, 100);
				}
			});
		}

		class DapilAdapter extends EasyAdapter {
			private final List<Dapil.Row> rows;
			private final int lembaga;

			public DapilAdapter(final int lembaga) {
				this.lembaga = lembaga;
				rows = Dapil.getRows(lembaga);
			}

			@Override
			public View newView(final int position, final ViewGroup parent) {
				return getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
			}

			@Override
			public View newDropDownView(final int position, final ViewGroup parent) {
				return getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			}

			@Override
			public void bindView(final View view, final int position, final ViewGroup parent) {
				TextView textView = (TextView) view;
				if (position == 0) {
					textView.setText(F.wrap("Pilih Dapil " + (lembaga == 1? "DPR": "DPRD I"), F.reg()));
				} else {
					final Dapil.Row row = rows.get(position - 1);
					textView.setText(F.wrap(row.desc, F.reg()));
				}
			}

			@Override
			public int getCount() {
				return 1 + rows.size();
			}
		}
	};

	Runnable masukmain = new Runnable() {
		@Override
		public void run() {
			startActivity(new Intent(App.context, MainActivity.class));
			finish();
		}
	};

	Runnable cariarea = new Runnable() {
		@Override
		public void run() {
			double lat = Preferences.getFloat(Prefkey.loc_lat, 0);
			double lng = Preferences.getFloat(Prefkey.loc_lng, 0);
			Papi.geographic_point(lat, lng, new Papi.Clbk<Papi.Area[]>() {
				@Override
				public void success(final Papi.Area[] areas) {
					sukses(areas);
				}

				private void sukses(final Papi.Area[] areas) {
					// cek luar negeri
					if (areas.length == 0) {
						Papi.Area[] areaspalsu = {new Papi.Area(), new Papi.Area()};
						// DKI JKT II: 3102-00-0000
						areaspalsu[0].nama = "Luar Negeri";
						areaspalsu[0].id = "3102-00-0000";
						areaspalsu[0].lembaga = "DPR";
						areaspalsu[0].kind = "Dapil";
						// Jakarta 2: 3100-02-0000
						areaspalsu[1].nama = "Luar Negeri";
						areaspalsu[1].id = "3100-02-0000";
						areaspalsu[1].lembaga = "DPRDI";
						areaspalsu[1].kind = "Dapil";

						sukses(areaspalsu);
						return;
					}

					int masuk = 0;
					for (final Papi.Area area : areas) {
						if ("DPR".equals(area.lembaga)) {
							Preferences.setString(Prefkey.dapil_dpr, area.id);
							iProgress.clearAnimation();
							iProgress.setImageResource(R.drawable.ic_action_accept);
							tStatus.setText(Html.fromHtml("Daerah pemilihan:<br><b>" + area.nama + "</b>"));

							masuk++;
						}
						if ("DPRDI".equals(area.lembaga)) {
							Preferences.setString(Prefkey.dapil_dprd1, area.id);
							masuk++;
						}
					}

					if (masuk >= 2) {
						h.postDelayed(masukmain, 2000);
					}
				}

				@Override
				public void failed(final Throwable ex) {
					tStatus.setText("Gagal mendapatkan daerah");
					bRetry.setVisibility(View.VISIBLE);
				}
			});
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		iProgress = V.get(this, R.id.progressBar);
		iProgress.postDelayed(new Runnable() {
			@Override
			public void run() {
				RotateAnimation anim = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setRepeatCount(999);
				anim.setDuration(1000);
				iProgress.startAnimation(anim);
			}

		}, 100);
		tStatus = V.get(this, R.id.tStatus);
		bRetry = V.get(this, R.id.bRetry);
		bRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				h.post(ceklokasi);
				bRetry.setVisibility(View.INVISIBLE);
			}
		});
		bRetry.setVisibility(View.INVISIBLE);

		cbDapilDpr = V.get(this, R.id.cbDapilDpr);
		cbDapilDprd1 = V.get(this, R.id.cbDapilDprd1);
		bSave = V.get(this, R.id.bSave);

		h.postDelayed(ceklokasi, 2000);
	}
}