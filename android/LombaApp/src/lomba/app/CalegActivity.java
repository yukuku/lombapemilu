package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import lomba.app.rpc.Papi;
import yuku.afw.V;

public class CalegActivity extends Activity {
	public static final String TAG = CalegActivity.class.getSimpleName();

	JazzyViewPager jazzy;
	String id;
	InfoAdapter adapter;
	Papi.Caleg info;

	public static Intent create(String id, byte[] dt) {
		Intent res = new Intent(App.context, CalegActivity.class);
		res.putExtra("id", id);
		res.putExtra("dt", dt);
		return res;
	}


	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caleg);

		jazzy = V.get(this, R.id.jazzy);
		jazzy.setAdapter(adapter = new InfoAdapter());
		jazzy.setTransitionEffect(JazzyViewPager.TransitionEffect.CubeIn);

		this.id = getIntent().getStringExtra("id");
		this.info = U.unser(getIntent().getByteArrayExtra("dt"));

		View bP1 = V.get(this, R.id.bP1);
		View bP2 = V.get(this, R.id.bP2);
		View bP3 = V.get(this, R.id.bP3);
		View bP4 = V.get(this, R.id.bP4);
		View bP5 = V.get(this, R.id.bP5);
		View bP6 = V.get(this, R.id.bP6);
		View bP7 = V.get(this, R.id.bP7);

		bP1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(0);
			}
		});
		bP2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(1);
			}
		});
		bP3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(2);
			}
		});
		bP4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(3);
			}
		});
		bP5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(4);
			}
		});
		bP6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(5);
			}
		});
		bP7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				jazzy.setCurrentItem(6);
			}
		});
	}

	View datadiri(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_datadiri, container, false);
		container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		return res;
	}

	View organisasi(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_organisasi, container, false);
		container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		return res;
	}

	View pendidikan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pendidikan, container, false);
		container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		return res;
	}

	View pekerjaan(final ViewGroup container) {
		View res = getLayoutInflater().inflate(R.layout.info_pekerjaan, container, false);
		container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		return res;
	}



	class InfoAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View res;

			if (position == 3) {
				res = pendidikan(container);
			} else if (position == 5) {
				res = organisasi(container);
			} else if (position == 4) {
				res = pekerjaan(container);
			} else {
				res = datadiri(container);
			}

			jazzy.setObjectForPosition(res, position);
			return res;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(jazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			return 7;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			if (view instanceof OutlineContainer) {
				return ((OutlineContainer) view).getChildAt(0) == obj;
			} else {
				return view == obj;
			}
		}
	}
}
