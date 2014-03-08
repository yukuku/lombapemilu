package lomba.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.fr.BerandaFragment;
import lomba.app.fr.CalegListFragment;
import lomba.app.rpc.Papi;
import yuku.afw.V;
import yuku.afw.widget.EasyAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

	DrawerLayout drawer;
	ActionBarDrawerToggle drawerToggle;
	ListView navList;
	DrawerAdapter adapter;
	int selection;
	int oldSelection = -1;
	List<Papi.Partai> partais;

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

		loadPartai();
	}

	void loadPartai() {
		Papi.candidate_partai(new Papi.Clbk<Papi.Partai[]>() {
			@Override
			public void success(final Papi.Partai[] partais) {

				Arrays.sort(partais, new Comparator<Papi.Partai>() {
					@Override
					public int compare(final Papi.Partai lhs, final Papi.Partai rhs) {
						return lhs.id - rhs.id;
					}
				});

				MainActivity.this.partais = new ArrayList<>();
				for (final Papi.Partai partai : partais) {
					MainActivity.this.partais.add(partai);
				}
				adapter.notifyDataSetChanged();
			}

			@Override
			public void failed(final Throwable ex) {
				// load againnn
				new Thread(new Runnable() {
					@Override
					public void run() {
						SystemClock.sleep(2000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadPartai();
							}
						});
					}
				}).start();
			}
		});
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
			} else if (selection >= 1 && selection <= 15) {
				FragmentTransaction tx = getFragmentManager().beginTransaction();
				tx.replace(R.id.main, CalegListFragment.create("" + selection));
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
			ImageView imgPartai = V.get(view, R.id.imgPartai);


			String t = position == 0? "Beranda": partais.get(position - 1).nama;
			if (position == 0) {
				Picasso.with(MainActivity.this).load(R.drawable.beranda).into(imgPartai);
			} else {
				Picasso.with(MainActivity.this).load(getResources().getIdentifier("partai_" + (position), "drawable", getPackageName())).into(imgPartai);
			}

			textView.setText(t);

			if (selection == position) {
				view.setBackgroundColor(0xffb7793b);
			} else {
				view.setBackgroundColor(0);
			}
		}

		@Override
		public int getCount() {
			return 1 + (partais == null? 0: partais.size());
		}
	}
}
