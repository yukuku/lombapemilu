package lomba.app;

import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import lomba.app.storage.Prefkey;
import yuku.afw.storage.Preferences;

import java.util.UUID;

public class App extends yuku.afw.App implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	public static final String TAG = App.class.getSimpleName();

	private LocationClient client;

	public void onCreate() {
		super.onCreate();

		client = new LocationClient(this, this, this);
		client.connect();
	}

	@Override
	public void onConnected(final Bundle bundle) {
		final Location loc = client.getLastLocation();
		if (loc != null) {
			final double lat = loc.getLatitude();
			final double lng = loc.getLongitude();

			Preferences.setFloat(Prefkey.loc_lat, (float) lat);
			Preferences.setFloat(Prefkey.loc_lng, (float) lng);
		}

		final LocationRequest request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
		client.requestLocationUpdates(request, this);
	}

	@Override
	public void onDisconnected() {

	}

	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(final Location loc) {
		if (loc != null) {
			final double lat = loc.getLatitude();
			final double lng = loc.getLongitude();

			Preferences.setFloat(Prefkey.loc_lat, (float) lat);
			Preferences.setFloat(Prefkey.loc_lng, (float) lng);
		}
	}

	public synchronized static String getInstallationId() {
		String res = Preferences.getString(Prefkey.installation_id, null);
		if (res == null) {
			res = "u2:" + UUID.randomUUID().toString();
			Preferences.setString(Prefkey.installation_id, res);
		}
		return res;
	}
}
