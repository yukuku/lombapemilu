package lomba.app.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thnkld.calegstore.app.R;
import lomba.app.U;
import lomba.app.ac.MainActivity;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Got intent");
		Log.d(TAG, "  action: " + intent.getAction());
		Log.d(TAG, "  data uri: " + intent.getData());
		Log.d(TAG, "  component: " + intent.getComponent());
		Log.d(TAG, "  flags: 0x" + Integer.toHexString(intent.getFlags()));
		Log.d(TAG, "  mime: " + intent.getType());
		Bundle extras = intent.getExtras();
		Log.d(TAG, "  extras: " + (extras == null? "null": extras.size()));
		if (extras != null) {
			for (String key: extras.keySet()) {
				Log.d(TAG, "    " + key + " = " + extras.get(key));
			}
		}

		String title = intent.getStringExtra("title");
		String content = intent.getStringExtra("content");
		String kind = intent.getStringExtra("kind");
		String caleg_name = intent.getStringExtra("caleg_name");
		final String caleg_id = intent.getStringExtra("caleg_id");
		String user_email = intent.getStringExtra("user_email");
		String foto_url = intent.getStringExtra("foto_url");
		String partai_nama = intent.getStringExtra("partai_nama");
		String urutan = intent.getStringExtra("urutan");
		float rating = Float.parseFloat(intent.getStringExtra("rating"));

		if (user_email != null && !user_email.equals(U.getPrimaryAccountName())) {
			if ("new_caleg_rating".equals(kind)) {
				StringBuilder stars = new StringBuilder();
				for (int i = 0; i < rating; i++) {
					stars.append('\u2605');
				}
				for (int i = (int) rating; i < 5; i++) {
					stars.append('\u2606');
				}

				final PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

				final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_stat_notif)
				.setContentTitle("[" + partai_nama + " no." + urutan + "] " + U.bagusinNama(caleg_name))
				.setContentText(Html.fromHtml(stars + " <b>" + title + "</b> " + content))
				.setTicker("Rating baru untuk " + U.bagusinNama(caleg_name))
				.setContentIntent(pi)
				.setAutoCancel(true);

				final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				if (!TextUtils.isEmpty(foto_url)) {
					Picasso.with(context).load(U.bc(144, 144, foto_url)).into(new Target() {
						@Override
						public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
							builder.setLargeIcon(bitmap);
							nm.notify("new_caleg_rating:" + caleg_id, 1, builder.build());
						}

						@Override
						public void onBitmapFailed(final Drawable errorDrawable) {
							nm.notify("new_caleg_rating:" + caleg_id, 1, builder.build());
						}

						@Override
						public void onPrepareLoad(final Drawable placeHolderDrawable) {}
					});
				}
			}
		}
	}
}
