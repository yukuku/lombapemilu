package lomba.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.net.Uri;
import android.text.TextUtils;
import lomba.app.storage.Prefkey;
import yuku.afw.storage.Preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class U {
	public static final String TAG = U.class.getSimpleName();

	public static String bagusinNama(String s) {
		if (s == null) return null;

		final String[] split = s.split(" ");
		for (int i = 0; i < split.length; i++) {
			String k = split[i];

			if (k.length() <= 3) continue;

			k = k.charAt(0) + k.substring(1).toLowerCase();
			split[i] = k;
		}

		return TextUtils.join(" ", split);
	}

	public static byte[] ser(Serializable s) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			oos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String lower(String s) {
		if (s == null) return null;
		return s.toLowerCase();
	}

	public static <T> T unser(byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			final Object o = ois.readObject();
			return (T) o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String bc(int w, int h, String u) {
		return "http://pemilu-backend.appspot.com/remote/image_bottom_crop?w=" + w + "&h=" + h + "&url=" + Uri.encode(u);
	}

	public static String toTitleCase(String input) {
		if (input == null) return null;

		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			} else {
				c = Character.toLowerCase(c);
			}

			titleCase.append(c);
		}

		return titleCase.toString();
	}

	public static String formatRiwayat(String input) {
		// cek apakah semua huruf besar kalo iya pake toTitleCase
		boolean semuakapital = true;
		for (final char c : input.toCharArray()) {
			if (c >= 'a' && c <= 'z') { // has lowercase
				semuakapital = false;
				break;
			}
		}
		if (semuakapital) {
			return toTitleCase(input);
		}
		return input;
	}

	public static String getNamaLembaga(final int lembaga) {
		return new String[]{null, "DPR", "DPRDI"}[lembaga];
	}

	public static String getDapilDariLembaga(final int lembaga) {
		return Preferences.getString(lembaga == 1? Prefkey.dapil_dpr: Prefkey.dapil_dprd1);
	}

	private static MessageDigest md5;

	public static String md5(final String s) {
		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("md5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		byte[] bb = md5.digest(s.trim().toLowerCase().getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : bb) {
			if (b >= 0 && b < 16) sb.append("0").append(Integer.toHexString(b));
			else sb.append(Integer.toHexString(b & 0xff));
		}
		return sb.toString();
	}

	public static String getPrimaryAccountName() {
		final Account[] accounts = AccountManager.get(App.context).getAccountsByType("com.google");
		if (accounts != null) {
			return accounts[0].name;
		}
		return null;
	}
}
