package com.google.android.tvlauncher.home.operatorbackground;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.p001v4.view.ViewCompat;

class BitmapBackgroundTransitionDrawable extends Drawable {
    private int alpha = 255;
    private long animateDuration;
    private long animateStart;
    private boolean animating = false;
    private Bitmap bitmap;
    private Paint paint;

    BitmapBackgroundTransitionDrawable() {
    }

    /* access modifiers changed from: package-private */
    public void animateFadeIn(Bitmap bitmap2, long durationMs) {
        this.bitmap = bitmap2;
        this.animateStart = SystemClock.uptimeMillis();
        this.animateDuration = durationMs;
        this.animating = true;
        this.paint = new Paint();
    }

    public void draw(Canvas canvas) {
        if (this.animating) {
            long time = SystemClock.uptimeMillis() - this.animateStart;
            long j = this.animateDuration;
            if (time >= j) {
                this.animating = false;
                this.alpha = 255;
            } else {
                this.alpha = (int) ((255 * time) / j);
            }
        }
        if (this.alpha != 255 || this.bitmap == null) {
            canvas.drawColor((int) ViewCompat.MEASURED_STATE_MASK);
        }
        int i = this.alpha;
        if (!(i == 0 || this.bitmap == null)) {
            this.paint.setAlpha(i);
            canvas.drawBitmap(this.bitmap, 0.0f, 0.0f, this.paint);
        }
        if (this.animating) {
            invalidateSelf();
        }
    }

    public void setAlpha(int alpha2) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return -1;
    }
}
