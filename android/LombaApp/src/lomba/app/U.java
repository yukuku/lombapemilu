package lomba.app;

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

	public static String getNamaLembaga(final int lembaga) {
		return new String[]{null, "DPR", "DPRDI"}[lembaga];
	}

	public static String getDapilDariLembaga(final int lembaga) {
		return Preferences.getString(lembaga == 1? Prefkey.dapil_dpr: Prefkey.dapil_dprd1);
	}
}
