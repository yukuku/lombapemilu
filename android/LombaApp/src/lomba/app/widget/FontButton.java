package lomba.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import lomba.app.F;

public class FontButton extends Button {
	public static final String TAG = FontButton.class.getSimpleName();

	public FontButton(final Context context) {
		super(context);
		init();
	}

	public FontButton(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FontButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		setTypeface(F.reg());
	}
}
