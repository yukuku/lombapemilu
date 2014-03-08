package lomba.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.OutlineContainer;
import yuku.afw.V;

public class CalegActivity extends Activity {
	public static final String TAG = CalegActivity.class.getSimpleName();

	JazzyViewPager jazzy;
	String id;
	InfoAdapter adapter;

	public static Intent create(String id) {
		Intent res = new Intent(App.context, CalegActivity.class);
		res.putExtra("id", id);
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

	class InfoAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			TextView res = new TextView(CalegActivity.this);
			res.setGravity(Gravity.CENTER);
			res.setTextSize(30);
			res.setTextColor(Color.WHITE);
			res.setText("Page " + position + " ID=" + id);
			res.setPadding(30, 30, 30, 30);
			int bg = Color.rgb((int) Math.floor(Math.random()*128)+64,
			(int) Math.floor(Math.random()*128)+64,
			(int) Math.floor(Math.random()*128)+64);
			res.setBackgroundColor(bg);

			container.addView(res, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
