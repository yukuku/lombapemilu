package lomba.app.widget;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import lomba.app.R;

public class RatingView2 extends View {
	public static final String TAG = RatingView2.class.getSimpleName();

	public float getRating() {
		return rating;
	}

	public void setRating(final float rating) {
		this.rating = rating;
		invalidate();
	}

	float rating;

	static Bitmap base;
	static Bitmap over;

	public float getLastdown() {
		return lastdown;
	}

	float lastdown = 0.0f;

	Rect src = new Rect();
	RectF dst = new RectF();

	public RatingView2(final Context context) {
		super(context);
		init();
	}

	public RatingView2(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RatingView2(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	void init() {
		if (base == null) {
			base = BitmapFactory.decodeResource(getResources(), R.drawable.rating_base2);
		}
		if (over == null) {
			over = BitmapFactory.decodeResource(getResources(), R.drawable.rating_over2);
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			lastdown = event.getX() / getWidth();
		}

		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		src.set(0, 0, base.getWidth(), base.getHeight());
		dst.set(0, 0, getWidth(), getHeight());
		canvas.drawBitmap(base, src, dst, null);

		src.set(0, 0, (int) (base.getWidth() * rating / 5), base.getHeight());
		dst.set(0, 0, getWidth() * rating / 5, getHeight());

		canvas.drawBitmap(over, src, dst, null);
	}
}
