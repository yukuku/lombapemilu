package lomba.app;

import android.graphics.Typeface;

public class F {
	public static final String TAG = F.class.getSimpleName();

	static Typeface reg;
	static Typeface thin;
	static Typeface bold;

	public static Typeface reg() {
		if (reg == null) {
			reg = Typeface.createFromAsset(App.context.getAssets(), "fonts/alereg.ttf");
		}
		return reg;
	}

	public static Typeface thin() {
		if (thin == null) {
			thin = Typeface.createFromAsset(App.context.getAssets(), "fonts/alethin.ttf");
		}
		return thin;
	}

	public static Typeface bold() {
		if (bold == null) {
			bold = Typeface.createFromAsset(App.context.getAssets(), "fonts/alebold.ttf");
		}
		return bold;
	}

}
