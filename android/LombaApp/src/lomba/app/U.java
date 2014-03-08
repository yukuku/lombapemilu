package lomba.app;

import android.text.TextUtils;

public class U {
	public static final String TAG = U.class.getSimpleName();

	public static String bagusinNama(String s) {
		final String[] split = s.split(" ");
		for (int i = 0; i < split.length; i++) {
			String k = split[i];

			if (k.length() <= 3) continue;

			k = k.charAt(0) + k.substring(1).toLowerCase();
			split[i] = k;
		}

		return TextUtils.join(" ", split);
	}
}
