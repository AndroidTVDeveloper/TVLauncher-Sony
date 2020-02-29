package com.google.android.tvlauncher.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.google.android.tvlauncher.C1167R;

public class NowPlayingIndicatorView extends View {
    private static final int[][] LEVELS = {new int[]{5, 3, 5, 7, 9, 10, 11, 12, 11, 12, 10, 8, 7, 4, 2, 4, 6, 7, 9, 11, 9, 7, 5, 3, 5, 8, 5, 3, 4}, new int[]{12, 11, 10, 11, 12, 11, 9, 7, 9, 11, 12, 10, 8, 10, 12, 11, 9, 5, 3, 5, 8, 10, 12, 10, 9, 8}, new int[]{8, 9, 10, 12, 11, 9, 7, 5, 7, 8, 9, 12, 11, 12, 9, 7, 9, 11, 12, 10, 8, 9, 7, 5, 3}};
    private static final int MAX_LEVEL = 15;
    private static final float PAUSED_LEVEL = 0.5f;
    private static final int TICK_DURATION_MS = 80;
    private final ValueAnimator animator;
    private final int barSeparationPx;
    private final int barWidthPx;
    private final Rect drawRect = new Rect();
    private final Paint paint;
    /* access modifiers changed from: private */
    public float progress;

    /* JADX INFO: finally extract failed */
    public NowPlayingIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray customAttrs = context.obtainStyledAttributes(attrs, C1167R.styleable.NowPlayingIndicatorView, 0, C1167R.style.NowPlayingIndicatorViewStyle);
        try {
            this.barWidthPx = customAttrs.getDimensionPixelSize(C1167R.styleable.NowPlayingIndicatorView_bar_width, 0);
            this.barSeparationPx = customAttrs.getDimensionPixelSize(C1167R.styleable.NowPlayingIndicatorView_bar_separation, 0);
            customAttrs.recycle();
            this.animator = new ValueAnimator();
            this.animator.setInterpolator(new LinearInterpolator());
            this.animator.setRepeatCount(-1);
            this.animator.setDuration(100000000L);
            ValueAnimator valueAnimator = this.animator;
            valueAnimator.setFloatValues(0.0f, (float) (valueAnimator.getDuration() / 80));
            this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float unused = NowPlayingIndicatorView.this.progress = ((Float) animation.getAnimatedValue()).floatValue();
                    NowPlayingIndicatorView.this.invalidate();
                }
            });
            this.paint = new Paint();
            this.paint.setColor(-1);
            setLayerType(1, null);
            setImportantForAccessibility(2);
        } catch (Throwable th) {
            customAttrs.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            startAnimationIfVisible();
        } else {
            stopAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (this.barWidthPx * 3) + (this.barSeparationPx * 2) + getPaddingStart() + getPaddingEnd();
        setMeasuredDimension(width, width);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimationIfVisible();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    public void stopAnimation() {
        this.animator.cancel();
        postInvalidate();
    }

    public void startAnimationIfVisible() {
        if (getVisibility() == 0) {
            this.animator.start();
            postInvalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        drawRectangles(canvas);
    }

    private void drawRectangles(Canvas canvas) {
        for (int barIndex = 0; barIndex < 3; barIndex++) {
            this.drawRect.left = ((this.barWidthPx + this.barSeparationPx) * barIndex) + getPaddingStart();
            Rect rect = this.drawRect;
            rect.right = rect.left + this.barWidthPx;
            this.drawRect.bottom = getHeight() - getPaddingBottom();
            float value = linearlyInterpolateWithWrapping(this.progress, LEVELS[barIndex]);
            int barMaxHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
            this.drawRect.top = (int) (((float) getPaddingTop()) + (((float) barMaxHeight) * (1.0f - (value / 15.0f))));
            canvas.drawRect(this.drawRect, this.paint);
        }
    }

    private static float linearlyInterpolateWithWrapping(float position, int[] array) {
        int positionRoundedDown = (int) position;
        int beforeIndex = positionRoundedDown % array.length;
        float weight = position - ((float) positionRoundedDown);
        return (((float) array[beforeIndex]) * (1.0f - weight)) + (((float) array[(beforeIndex + 1) % array.length]) * weight);
    }
}
