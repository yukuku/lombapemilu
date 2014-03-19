package lomba.app.ac;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import com.thnkld.calegstore.app.R;
import lomba.app.F;
import lomba.app.ac.base.BaseActivity;
import yuku.afw.App;
import yuku.afw.V;

public class AboutActivity extends BaseActivity {
	public static final String TAG = AboutActivity.class.getSimpleName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		TextView tVersi = V.get(this, R.id.tVersi);
		tVersi.setText("versi " + App.getVersionName());

		final ActionBar ab = getActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		setTitle(F.wrap("Tentang", F.reg()));
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
