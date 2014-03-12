package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
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
	ImageView iProgress;


	Runnable ceklokasi = new Runnable() {
		@Override
		public void run() {
			tStatus.setText("Mengecek daerah pemilihan");

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
					boolean masuk = false;
					for (final Papi.Area area : areas) {
						if ("DPR".equals(area.lembaga)) {
							Preferences.setString(Prefkey.dapil_dpr, area.id);
							iProgress.clearAnimation();
							iProgress.setImageResource(R.drawable.ic_action_accept);
							tStatus.setText(Html.fromHtml("Daerah pemilihan:<br><b>" + area.nama + "</b>"));

							masuk = true;
						}
						if ("DPRDI".equals(area.lembaga)) {
							Preferences.setString(Prefkey.dapil_dprd1, area.id);
							masuk = true;
						}
					}
					if (masuk) {
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

		h.postDelayed(ceklokasi, 2000);
	}
}