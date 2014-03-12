package lomba.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import lomba.app.fr.BerandaFragment;
import lomba.app.fr.CalegListFragment;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
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
	TextView tAbTitle;
	Button bAbLembaga;

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
				setAbtitle(getString(R.string.app_name));
			}

			@Override
			public void onDrawerClosed(final View drawerView) {
				super.onDrawerClosed(drawerView);
			}
		};
		drawer.setDrawerListener(drawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setCustomView(R.layout.main_ab_customview);

		tAbTitle = V.get(getActionBar().getCustomView(), R.id.tAbTitle);
		bAbLembaga = V.get(getActionBar().getCustomView(), R.id.bAbLembaga);

		bAbLembaga.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				PopupMenu pop = new PopupMenu(getActionBar().getThemedContext(), bAbLembaga);
				final Menu menu = pop.getMenu();
				menu.add(0, 1, 0, "DPR");
				menu.add(0, 2, 0, "DPRD I");
				pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final MenuItem item) {
						setLembaga(item.getItemId());
						return true;
					}
				});
				pop.show();
			}
		});
		setLembaga(Preferences.getInt(Prefkey.lembaga_aktif, 1));

		// pastikan initial state bener
		if (savedInstanceState == null) {
			updateContent();
		}

		loadPartai();

		setAbtitle(getString(R.string.app_name));
	}

	private void setLembaga(final int lembaga) {
		bAbLembaga.setText(new String[] {null, "DPR", "DPRD I", "DPRD II"}[lembaga]);
		Preferences.setInt(Prefkey.lembaga_aktif, lembaga);

		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("LEMBAGA_BERUBAH"));
	}

	void setAbtitle(String t) {
		tAbTitle.setText(t);
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

		if (selection == 0) {
			setAbtitle(getString(R.string.app_name));
		}
		if (selection >= 1 && selection <= 15) {
			setAbtitle(partais.get(selection - 1).nama);
		}
	}

	class DrawerAdapter extends EasyAdapter {
		final float density = getResources().getDisplayMetrics().density;

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(final int position) {
			return position == 0? 0: 1;
		}

		@Override
		public View newView(final int position, final ViewGroup parent) {
			return getLayoutInflater().inflate(getItemViewType(position) == 0? R.layout.item_partai_beranda: R.layout.item_partai, parent, false);
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
				view.setBackgroundColor(0xffcccccc);
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
