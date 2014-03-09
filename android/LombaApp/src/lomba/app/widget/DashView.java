package lomba.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DashView extends View {
	public static final String TAG = DashView.class.getSimpleName();

	Paint p = new Paint();
	{
		p.setColor(0xaaffffff);
	}

	public DashView(final Context context) {
		super(context);
	}

	public DashView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public DashView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		float density = getResources().getDisplayMetrics().density;
		final int w = getWidth();
		for (int i = 0, h = getHeight(); i < h; i += (int) (density * 4)) {
			canvas.drawRect(0, i, w, i + 2, p);
		}
	}
}
