package com.google.android.tvlauncher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.google.android.tvlauncher.C1167R;

public class BarRatingView extends View {
    private static final int MAX_RATING = 5;
    private static final float STEP_SIZE = 0.25f;
    private float fillPortion;
    private RectF filledItemRect;
    private Paint filledPaint;
    private Bitmap itemFilledBitmap;
    private Bitmap itemUnfilledBitmap;
    private int layoutHeight;
    private int layoutWidth;
    private final int overallScore;
    private float rating;
    private RectF unfilledItemRect;
    private Paint unfilledPaint;

    public BarRatingView(Context context) {
        this(context, null);
    }

    public BarRatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarRatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        int itemWidth = context.getResources().getDimensionPixelSize(C1167R.dimen.program_meta_rating_size);
        this.layoutHeight = itemWidth;
        this.layoutWidth = itemWidth * 5;
        this.itemFilledBitmap = convertToBitmap(context.getDrawable(C1167R.C1168drawable.ic_channel_view_filled_item_active), itemWidth, this.layoutHeight);
        this.itemUnfilledBitmap = convertToBitmap(context.getDrawable(C1167R.C1168drawable.ic_channel_view_filled_item_inactive), itemWidth, this.layoutHeight);
        this.filledPaint = new Paint();
        this.filledPaint.setShader(new BitmapShader(this.itemFilledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        this.unfilledPaint = new Paint();
        this.unfilledPaint.setShader(new BitmapShader(this.itemUnfilledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        this.overallScore = 20;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.layoutWidth, this.layoutHeight);
    }

    private Bitmap convertToBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setRating(float rating2) {
        int roundedScore;
        this.rating = rating2;
        float score = (rating2 / 5.0f) * ((float) this.overallScore);
        if (rating2 < 0.0f) {
            roundedScore = 0;
        } else if (rating2 > 5.0f) {
            roundedScore = this.overallScore;
        } else if (score <= 0.0f || score >= 1.0f) {
            int roundedScore2 = this.overallScore;
            if (score <= ((float) (roundedScore2 - 1)) || score >= ((float) roundedScore2)) {
                roundedScore = Math.round(score);
            } else {
                roundedScore = roundedScore2 - 1;
            }
        } else {
            roundedScore = 1;
        }
        this.fillPortion = (((float) roundedScore) * 1.0f) / ((float) this.overallScore);
        if (getLayoutDirection() == 1) {
            int i = this.layoutWidth;
            this.filledItemRect = new RectF((1.0f - this.fillPortion) * ((float) i), 0.0f, (float) i, (float) this.layoutHeight);
            this.unfilledItemRect = new RectF(0.0f, 0.0f, (1.0f - this.fillPortion) * ((float) this.layoutWidth), (float) this.layoutHeight);
        } else {
            this.filledItemRect = new RectF(0.0f, 0.0f, this.fillPortion * ((float) this.layoutWidth), (float) this.layoutHeight);
            float f = this.fillPortion;
            int i2 = this.layoutWidth;
            this.unfilledItemRect = new RectF(f * ((float) i2), 0.0f, (float) i2, (float) this.layoutHeight);
        }
        invalidate();
    }

    public float getRating() {
        return this.rating;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        canvas.drawRect(this.unfilledItemRect, this.unfilledPaint);
        canvas.drawRect(this.filledItemRect, this.filledPaint);
    }
}
