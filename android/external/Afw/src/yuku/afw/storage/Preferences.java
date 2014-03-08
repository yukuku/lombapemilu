package yuku.afw.storage;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import yuku.afw.App;
import yuku.afw.D;

public class Preferences {
	private static final String TAG = Preferences.class.getSimpleName();
	
	private static SharedPreferences cache;
	private static boolean dirty = true;
	private static Editor currentEditor;
	private static int held = 0;
	
	public static void invalidate() {
		dirty = true;
	}
	
	private static Editor getEditor(SharedPreferences pref) {
		if (currentEditor == null) {
			currentEditor = pref.edit();
		}
		return currentEditor;
	}
	
	public static int getInt(Enum<?> key, int def) {
		return getInt(key.toString(), def);
	}
	
	public static int getInt(String key, int def) {
		SharedPreferences pref = read();
		return pref.getInt(key, def);
	}
	
	public static float getFloat(Enum<?> key, float def) {
		SharedPreferences pref = read();
		return pref.getFloat(key.toString(), def);
	}
	
	public static float getFloat(String key, float def) {
		SharedPreferences pref = read();
		return pref.getFloat(key, def);
	}
	
	public static long getLong(Enum<?> key, long def) {
		return getLong(key.toString(), def);
	}
	
	public static long getLong(String key, long def) {
		SharedPreferences pref = read();
		return pref.getLong(key, def);
	}
	
	public static String getString(Enum<?> key, String def) {
		SharedPreferences pref = read();
		return pref.getString(key.toString(), def);
	}
	
	public static String getString(String key, String def) {
		SharedPreferences pref = read();
		return pref.getString(key, def);
	}
	
	public static String getString(Enum<?> key) {
		SharedPreferences pref = read();
		return pref.getString(key.toString(), null);
	}
	
	public static String getString(String key) {
		SharedPreferences pref = read();
		return pref.getString(key, null);
	}
	
	public static boolean getBoolean(Enum<?> key, boolean def) {
		return getBoolean(key.toString(), def);
	}
	
	public static boolean getBoolean(String key, boolean def) {
		SharedPreferences pref = read();
		return pref.getBoolean(key, def);
	}
	
	public static Object get(String key) {
		SharedPreferences pref = read();
		return pref.getAll().get(key);
	}
	
	public static Map<String, ?> getAll() {
		SharedPreferences pref = read();
		return pref.getAll();
	}

	public static Set<String> getAllKeys() {
		return new HashSet<String>(getAll().keySet());
	}
	
	public static void setInt(Enum<?> key, int val) {
		setInt(key.toString(), val);
	}
	
	public static void setInt(String key, int val) {
		SharedPreferences pref = read();
		getEditor(pref).putInt(key, val);
		commitIfNotHeld();
		Log.d(TAG, key + " = (int) " + val); //$NON-NLS-1$
	}
	
	public static void setFloat(Enum<?> key, float val) {
		setFloat(key.toString(), val);
	}
	
	public static void setFloat(String key, float val) {
		SharedPreferences pref = read();
		getEditor(pref).putFloat(key, val);
		commitIfNotHeld();
		Log.d(TAG, key + " = (float) " + val); //$NON-NLS-1$
	}
	
	public static void setLong(Enum<?> key, long val) {
		setLong(key.toString(), val);
	}
	
	public static void setLong(String key, long val) {
		SharedPreferences pref = read();
		getEditor(pref).putLong(key, val);
		commitIfNotHeld();
		Log.d(TAG, key + " = (long) " + val); //$NON-NLS-1$
	}
	
	public static void setString(Enum<?> key, String val) {
		setString(key.toString(), val);
	}
	
	public static void setString(String key, String val) {
		SharedPreferences pref = read();
		getEditor(pref).putString(key, val);
		commitIfNotHeld();
		Log.d(TAG, key + " = (string) " + val); //$NON-NLS-1$
	}
	
	public static void setBoolean(Enum<?> key, boolean val) {
		setBoolean(key.toString(), val);
	}
	
	public static void setBoolean(String key, boolean val) {
		SharedPreferences pref = read();
		getEditor(pref).putBoolean(key, val);
		commitIfNotHeld();
		Log.d(TAG, key + " = (bool) " + val); //$NON-NLS-1$
	}
	
	public static void remove(Enum<?> key) {
		remove(key.toString());
	}
	
	public static void remove(String key) {
		SharedPreferences pref = read();
		getEditor(pref).remove(key.toString());
		commitIfNotHeld();
		Log.d(TAG, key + " removed"); //$NON-NLS-1$
	}
	
	@TargetApi(9) private synchronized static void commitIfNotHeld() {
		if (held > 0) {
			// don't do anything now
		} else {
			if (currentEditor != null) {
				if (Build.VERSION.SDK_INT >= 9) {
					currentEditor.apply();
				} else {
					currentEditor.commit();
				}
				currentEditor = null;
			}
		}
	}
	
	public synchronized static void hold() {
		held++;
	}
	
	public synchronized static void unhold() {
		if (held <= 0) {
			throw new RuntimeException("unhold called too many times");
		}
		held--;
		if (held == 0) {
			if (currentEditor != null) {
				currentEditor.commit();
				currentEditor = null;
			}
		}
	}

	private synchronized static SharedPreferences read() {
		SharedPreferences res;
		if (dirty || cache == null) {
			long start = 0;
			if (D.EBUG) start = SystemClock.uptimeMillis();
			res = PreferenceManager.getDefaultSharedPreferences(App.context);
			if (D.EBUG) Log.d(TAG, "Preferences was read from disk in ms: " + (SystemClock.uptimeMillis() - start)); //$NON-NLS-1$
			dirty = false;
			cache = res;
		} else {
			res = cache;
		}

		return res;
	}
}
