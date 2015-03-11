package lomba.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.lomba.calegstore.R;

public class RatingView extends View {
	public static final String TAG = RatingView.class.getSimpleName();

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
	static Paint aa;

	Rect src = new Rect();
	RectF dst = new RectF();

	public RatingView(final Context context) {
		super(context);
		init();
	}

	public RatingView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RatingView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	void init() {
		if (base == null) {
			base = BitmapFactory.decodeResource(getResources(), R.drawable.rating_base);
		}
		if (over == null) {
			over = BitmapFactory.decodeResource(getResources(), R.drawable.rating_over);
		}
		if (aa == null) {
			aa = new Paint();
			aa.setAntiAlias(true);
			aa.setFilterBitmap(true);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		src.set(0, 0, base.getWidth(), base.getHeight());
		dst.set(0, 0, getWidth(), getHeight());
		canvas.drawBitmap(base, src, dst, aa);

		src.set(0, 0, (int) (base.getWidth() * rating / 5), base.getHeight());
		dst.set(0, 0, getWidth() * rating / 5, getHeight());

		canvas.drawBitmap(over, src, dst, aa);
	}
}
