package lomba.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import lomba.app.F;

public class FontEditTextView extends EditText {
	public static final String TAG = FontEditTextView.class.getSimpleName();


	public FontEditTextView(final Context context) {
		super(context);
		init();
	}

	public FontEditTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FontEditTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		if (isInEditMode()) return;

		final Object tag = getTag();
		if ("thin".equals(tag)) {
			setTypeface(F.thin());
		} else if ("bold".equals(tag)) {
			setTypeface(F.bold());
		} else {
			setTypeface(F.reg());
		}
	}
}
