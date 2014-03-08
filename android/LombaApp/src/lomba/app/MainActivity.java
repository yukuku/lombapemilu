package lomba.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import lomba.app.fr.BerandaFragment;
import lomba.app.fr.CalegListFragment;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

public class MainActivity extends Activity {

	DrawerLayout drawer;
	ActionBarDrawerToggle drawerToggle;
	ListView navList;
	DrawerAdapter adapter;
	int selection;
	int oldSelection = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		adapter = new DrawerAdapter();
		drawer = V.get(this, R.id.drawer_layout);
		navList = V.get(this, R.id.drawer);
		navList.setAdapter(adapter);
		navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				selection = position;
				drawer.closeDrawer(GravityCompat.START);
				adapter.notifyDataSetChanged();
				updateContent();
			}
		});

		drawerToggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer, R.string.desc_drawer_open, R.string.desc_drawer_close) {
			@Override
			public void onDrawerOpened(final View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(R.string.app_name);
			}

			@Override
			public void onDrawerClosed(final View drawerView) {
				super.onDrawerClosed(drawerView);
			}
		};
		drawer.setDrawerListener(drawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			updateContent();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == android.R.id.home && drawerToggle.isDrawerIndicatorEnabled()) {
			if (drawer.isDrawerVisible(GravityCompat.START)) {
				drawer.closeDrawer(GravityCompat.START);
			} else {
				drawer.openDrawer(GravityCompat.START);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateContent() {
		if (selection != oldSelection) {
			if (selection == 0) {
				FragmentTransaction tx = getFragmentManager().beginTransaction();
				tx.replace(R.id.main, Fragment.instantiate(MainActivity.this, BerandaFragment.class.getName()));
				tx.commit();
			} else if (selection >= 1 && selection <= 10) {
				FragmentTransaction tx = getFragmentManager().beginTransaction();
				tx.replace(R.id.main, CalegListFragment.create("" + selection));
				tx.commit();
			} else if (selection >= 11 && selection <= 12) {
				FragmentTransaction tx = getFragmentManager().beginTransaction();
				tx.replace(R.id.main, CalegListFragment.create("" + (selection + 3)));
				tx.commit();
			}
			oldSelection = selection;
			adapter.notifyDataSetChanged();
		}
	}

	class DrawerAdapter extends EasyAdapter {
		final float density = getResources().getDisplayMetrics().density;

		@Override
		public View newView(final int position, final ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.item_partai, parent, false);
		}

		@Override
		public void bindView(final View view, final int position, final ViewGroup parent) {
			TextView textView = V.get(view, R.id.tPartai);
			textView.setText("Kata-kataa");
			if (selection == position) {
				view.setBackgroundColor(0xffb7793b);
			} else {
				view.setBackgroundColor(0);
			}
		}

		@Override
		public int getCount() {
			return 16;
		}
	}
}
