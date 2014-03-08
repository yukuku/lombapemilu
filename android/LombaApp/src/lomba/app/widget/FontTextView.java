package lomba.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import lomba.app.F;

public class FontTextView extends TextView {
	public static final String TAG = FontTextView.class.getSimpleName();


	public FontTextView(final Context context) {
		super(context);
		init();
	}

	public FontTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FontTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		setTypeface(F.reg());
	}
}
