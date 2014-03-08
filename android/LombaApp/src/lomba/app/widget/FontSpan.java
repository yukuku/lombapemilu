package lomba.app.widget;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Changes the typeface family of the text to which the span is attached.
 */
public class FontSpan extends MetricAffectingSpan {
	private Typeface typeface;

    public FontSpan(Typeface typeface) {
        this.typeface = typeface;
    }
    
    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint);
    }

    private void apply(Paint paint) {
        paint.setTypeface(typeface);
    }
}
