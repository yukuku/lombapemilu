package lomba.app.ac;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;
import com.lomba.calegstore.R;
import lomba.app.App;
import lomba.app.F;
import lomba.app.ac.base.BaseActivity;
import lomba.app.data.Dapil;
import lomba.app.fr.BerandaFragment;
import lomba.app.fr.CalegListFragment;
import lomba.app.rpc.Papi;
import lomba.app.storage.Prefkey;
import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.afw.widget.EasyAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends BaseActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	public static final String CALEG_BERUBAH = "CALEG_BERUBAH";

	DrawerLayout drawer;
	ActionBarDrawerToggle drawerToggle;
	ListView navList;
	DrawerAdapter adapter;
	int selection;
	int oldSelection = -1;
	List<Papi.Partai> partais;
	TextView tAbTitle;
	Button bAbLembaga;
	Papi.Saklar partailoader;
	Button cbDapilDpr;
	Button cbDapilDprd1;
	Papi.Saklar subscribeloader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		adapter = new DrawerAdapter();
		drawer = V.get(this, R.id.drawer_layout);

		View navListHeader = getLayoutInflater().inflate(R.layout.drawer_header, null);
		View navListFooter = getLayoutInflater().inflate(R.layout.drawer_footer, null);

		navList = V.get(this, R.id.drawer);
		navList.addHeaderView(navListHeader);
		navList.addFooterView(navListFooter);
		navList.setAdapter(adapter);
		navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				selection = position - navList.getHeaderViewsCount();
				drawer.closeDrawer(GravityCompat.START);
				adapter.notifyDataSetChanged();
				updateContent();
			}
		});

		cbDapilDpr = V.get(navListHeader, R.id.cbDapilDpr);
		cbDapilDprd1 = V.get(navListHeader, R.id.cbDapilDprd1);
		navListFooter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent(App.context, AboutActivity.class));
			}
		});

		cbDapilDpr.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				popupDapil(cbDapilDpr, 1);
			}
		});
		cbDapilDprd1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				popupDapil(cbDapilDprd1, 2);
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
				menu.add(0, 1, 0, F.wrap("DPR", F.reg()));
				menu.add(0, 2, 0, F.wrap("DPRD I", F.reg()));
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
		displayDrawerDapil();

		sendGcmRegistration();
	}

	void sendGcmRegistration() {
		final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final String registration_id = gcm.register("928687239792");

					final String dapil_dpr = Preferences.getString(Prefkey.dapil_dpr);
					final String dapil_dprd1 = Preferences.getString(Prefkey.dapil_dprd1);

					if (registration_id != null && dapil_dpr != null && dapil_dprd1 != null) {
						subscribeloader = Papi.ganti(subscribeloader, Papi.subscribe(dapil_dpr + "," + dapil_dprd1, registration_id, App.getInstallationId(), new Papi.Clbk<Void>() {
							@Override
							public void success(final Void v) {

							}

							@Override
							public void failed(final Throwable e) {

							}
						}));
					}
				} catch (IOException e) {
					Log.e(TAG, "gagal register gcm", e);
				}
			}
		}).start();
	}

	private void popupDapil(final Button button, final int lembaga) {
		final PopupMenu pop = new PopupMenu(MainActivity.this, button);
		final List<Dapil.Row> rows = Dapil.getRows(lembaga);
		final Menu menu = pop.getMenu();
		for (int i = 0; i < rows.size(); i++) {
			final Dapil.Row row = rows.get(i);
			SpannableStringBuilder sb = new SpannableStringBuilder(row.desc);
			sb.setSpan(new ForegroundColorSpan(0xffffffff), 0, sb.length(), 0);
			menu.add(0, i, 0, F.wrap(sb, F.reg()));
		}
		pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final MenuItem item) {
				final int pos = item.getItemId();
				final Dapil.Row row = rows.get(pos);
				Preferences.setString(lembaga == 1? Prefkey.dapil_dpr: Prefkey.dapil_dprd1, row.kode);
				displayDrawerDapil();
				LocalBroadcastManager.getInstance(App.context).sendBroadcast(new Intent(CALEG_BERUBAH));
				sendGcmRegistration();
				return true;
			}
		});
		pop.show();
	}

	private void displayDrawerDapil() {
		cbDapilDpr.setText(Dapil.getDesc(1, Preferences.getString(Prefkey.dapil_dpr)));
		cbDapilDprd1.setText(Dapil.getDesc(2, Preferences.getString(Prefkey.dapil_dprd1)));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Papi.lupakan(partailoader);
		Papi.lupakan(subscribeloader);
	}

	private void setLembaga(final int lembaga) {
		bAbLembaga.setText(new String[] {null, "DPR", "DPRD I", "DPRD II"}[lembaga]);
		Preferences.setInt(Prefkey.lembaga_aktif, lembaga);

		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CALEG_BERUBAH));
	}

	void setAbtitle(String t) {
		tAbTitle.setText(t);
	}

	void loadPartai() {
		partailoader = Papi.ganti(partailoader, Papi.candidate_partai(new Papi.Clbk<Papi.Partai[]>() {
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
		}));
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
