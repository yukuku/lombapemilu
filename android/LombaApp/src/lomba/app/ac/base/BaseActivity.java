package lomba.app.ac.base;

import android.app.Activity;
import com.google.analytics.tracking.android.EasyTracker;

public abstract class BaseActivity extends Activity {
	public static final String TAG = BaseActivity.class.getSimpleName();

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
}
