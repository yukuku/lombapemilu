package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import yuku.afw.V;
import yuku.afw.storage.Preferences;

public class SplashActivity extends Activity {
	Handler h = new Handler();
	int cekbrpkali = 0;
	TextView tStatus;
	Button bRetry;

	Runnable ceklokasi = new Runnable() {
		@Override
		public void run() {
			cekbrpkali++;

			if (cekbrpkali > 3) {
				Preferences.setFloat(Prefkey.loc_lat, -6.87315f);
				Preferences.setFloat(Prefkey.loc_lng, 107.58682f);
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
					for (final Papi.Area area : areas) {
						if ("DPR".equals(area.lembaga)) {
							Preferences.setString(Prefkey.dapil_dpr, area.id);

							tStatus.setText("Daerah pemilihan:\n" + area.nama);

							h.postDelayed(masukmain, 2000);
						}
					}
				}

				@Override
				public void failed(final Throwable ex) {
					tStatus.setText("Gagal mendapatkan area");
					bRetry.setVisibility(View.VISIBLE);
				}
			});
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
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

		h.postDelayed(ceklokasi, 2000);
	}
}

