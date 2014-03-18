package lomba.app;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import lomba.app.widget.FontSpan;

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

	public static SpannableStringBuilder wrap(CharSequence s, Typeface f) {
		final SpannableStringBuilder res = s instanceof SpannableStringBuilder? (SpannableStringBuilder) s: new SpannableStringBuilder(s);
		res.setSpan(new FontSpan(f), 0, res.length(), 0);
		return res;
	}
}
