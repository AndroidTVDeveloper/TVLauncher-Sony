package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.TestsBuildCompat;

public class FadingEdgeContainer extends FrameLayout {
    private static final boolean DEBUG = false;
    private static final int[] FADE_COLORS_LTR = new int[21];
    private static final int[] FADE_COLORS_RTL = new int[21];
    private static final float[] FADE_COLOR_POSITIONS = new float[21];
    private static final int GRADIENT_CURVE_STEEPNESS = 100;
    private static final int GRADIENT_STEPS = 20;
    private static final String TAG = "FadingEdgeContainer";
    private boolean fadeEnabled = true;
    private int fadeWidth;
    private Paint gradientPaint;
    private Rect gradientRect;

    static {
        FADE_COLORS_LTR[0] = 0;
        for (int i = 1; i <= 20; i++) {
            double d = (double) i;
            Double.isNaN(d);
            float alpha = (float) Math.pow(100.0d, (d / 20.0d) - 1.0d);
            if (TestsBuildCompat.isAtLeastO()) {
                FADE_COLORS_LTR[i] = Color.argb(alpha, 0.0f, 0.0f, 0.0f);
            } else {
                FADE_COLORS_LTR[i] = Color.rgb(0, 0, 0);
            }
            FADE_COLOR_POSITIONS[i] = ((float) i) / 20.0f;
        }
        int i2 = 0;
        while (true) {
            int[] iArr = FADE_COLORS_LTR;
            if (i2 < iArr.length) {
                FADE_COLORS_RTL[(iArr.length - i2) - 1] = iArr[i2];
                i2++;
            } else {
                return;
            }
        }
    }

    public FadingEdgeContainer(Context context) {
        super(context);
        init();
    }

    public FadingEdgeContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FadingEdgeContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.fadeWidth = getContext().getResources().getDimensionPixelOffset(C1167R.dimen.channel_items_list_fade_width);
        this.gradientRect = new Rect();
    }

    public void setFadeEnabled(boolean enabled) {
        this.fadeEnabled = enabled;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void}
     arg types: [int, int, float, int, int[], float[], android.graphics.Shader$TileMode]
     candidates:
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long, long, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void} */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void}
     arg types: [float, int, float, int, int[], float[], android.graphics.Shader$TileMode]
     candidates:
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long, long, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void} */
    private void setUpPaint(int layoutWidth) {
        LinearGradient gradient;
        this.gradientPaint = new Paint();
        if (getLayoutDirection() == 0) {
            gradient = new LinearGradient(0.0f, 0.0f, (float) this.fadeWidth, 0.0f, FADE_COLORS_LTR, FADE_COLOR_POSITIONS, Shader.TileMode.CLAMP);
        } else {
            gradient = new LinearGradient((float) (layoutWidth - this.fadeWidth), 0.0f, (float) layoutWidth, 0.0f, FADE_COLORS_RTL, FADE_COLOR_POSITIONS, Shader.TileMode.CLAMP);
        }
        this.gradientPaint.setShader(gradient);
        this.gradientPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        if (this.fadeEnabled && this.gradientPaint == null) {
            setUpPaint(width);
        }
        if (getLayoutDirection() == 0) {
            if (this.fadeEnabled) {
                canvas.saveLayer(0.0f, 0.0f, (float) this.fadeWidth, (float) height, null);
                super.dispatchDraw(canvas);
                this.gradientRect.set(0, 0, this.fadeWidth, height);
                canvas.drawRect(this.gradientRect, this.gradientPaint);
                canvas.restore();
            }
            canvas.clipRect(this.fadeWidth, 0, width, height);
            super.dispatchDraw(canvas);
            return;
        }
        if (this.fadeEnabled) {
            canvas.saveLayer(0.0f, 0.0f, (float) width, (float) height, null);
            canvas.clipRect(width - this.fadeWidth, 0, width, height);
            super.dispatchDraw(canvas);
            this.gradientRect.set(width - this.fadeWidth, 0, width, height);
            canvas.drawRect(this.gradientRect, this.gradientPaint);
            canvas.restore();
        }
        canvas.clipRect(0, 0, width - this.fadeWidth, height);
        super.dispatchDraw(canvas);
    }
}
