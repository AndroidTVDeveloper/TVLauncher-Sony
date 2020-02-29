package com.google.android.tvlauncher.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import androidx.palette.graphics.Palette;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.TestsBuildCompat;
import com.google.android.tvlauncher.view.BackgroundTransitionDrawable;
import com.google.android.tvrecommendations.shared.util.ColorUtils;

class HomeBackgroundController {
    private static final int BITMAP_HEIGHT = 540;
    private static final float BITMAP_SCALE = 0.5f;
    private static final int BITMAP_WIDTH = 960;
    private static final float DARK_MODE_COLOR_DARKEN_FACTOR = 0.5f;
    private static final boolean DEBUG = false;
    private static final int FALLBACK_COLOR = -16777216;
    private static final int RADIAL_GRADIENT_VERTICAL_SHIFT = -300;
    private static final float STANDARD_COLOR_DARKEN_FACTOR = 0.4f;
    private static final String TAG = "HomeBackground";
    private static final float TOP_GRADIENT_COLOR_MIX_AMOUNT = 0.2f;
    private static final int TRANSITION_DURATION_MILLIS = 600;
    private int color1;
    private int color2;
    private int color3;
    private final Context context;
    private boolean darkMode = false;
    /* access modifiers changed from: private */
    public GenerateBitmapTask generateBitmapTask;
    private Paint linearGradientPaint;
    private Bitmap overlayBitmap;
    private Paint overlayPaint;
    private Paint radialGradientPaint;
    /* access modifiers changed from: private */
    public BackgroundTransitionDrawable transitionDrawable;

    HomeBackgroundController(View backgroundView) {
        this.context = backgroundView.getContext();
        this.transitionDrawable = new BackgroundTransitionDrawable(new BitmapDrawable[]{createBackgroundDrawable(), createBackgroundDrawable(), createBackgroundDrawable()});
        backgroundView.setBackground(this.transitionDrawable);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.Bitmap.createBitmap(int, int, android.graphics.Bitmap$Config, boolean):android.graphics.Bitmap}
     arg types: [int, int, android.graphics.Bitmap$Config, int]
     candidates:
      ClspMth{android.graphics.Bitmap.createBitmap(android.util.DisplayMetrics, int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap}
      ClspMth{android.graphics.Bitmap.createBitmap(int[], int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap}
      ClspMth{android.graphics.Bitmap.createBitmap(android.graphics.Picture, int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap}
      ClspMth{android.graphics.Bitmap.createBitmap(int, int, android.graphics.Bitmap$Config, boolean):android.graphics.Bitmap} */
    private BitmapDrawable createBackgroundDrawable() {
        Bitmap backgroundBitmap;
        if (TestsBuildCompat.isAtLeastO()) {
            backgroundBitmap = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888, false);
        } else {
            backgroundBitmap = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
        }
        BitmapDrawable drawable = new BitmapDrawable(this.context.getResources(), backgroundBitmap);
        drawable.setAutoMirrored(true);
        return drawable;
    }

    /* access modifiers changed from: package-private */
    public void updateBackground(Palette palette) {
        updateBackground(ColorUtils.darkenColor(palette.getVibrantColor(palette.getMutedColor(-16777216)), STANDARD_COLOR_DARKEN_FACTOR), ColorUtils.darkenColor(palette.getDarkVibrantColor(palette.getDarkMutedColor(-16777216)), STANDARD_COLOR_DARKEN_FACTOR), ColorUtils.darkenColor(palette.getLightVibrantColor(palette.getLightMutedColor(-16777216)), STANDARD_COLOR_DARKEN_FACTOR));
    }

    private void updateBackground(int color12, int color22, int color32) {
        this.darkMode = false;
        if (this.color1 != color12 || this.color2 != color22 || this.color3 != color32) {
            this.color1 = color12;
            this.color2 = color22;
            this.color3 = color32;
            GenerateBitmapTask generateBitmapTask2 = this.generateBitmapTask;
            if (generateBitmapTask2 != null) {
                generateBitmapTask2.cancel(true);
            }
            this.generateBitmapTask = new GenerateBitmapTask();
            this.generateBitmapTask.execute((BitmapDrawable) this.transitionDrawable.getBackBuffer());
        }
    }

    /* access modifiers changed from: package-private */
    public void enterDarkMode() {
        if (!this.darkMode) {
            updateBackground(ColorUtils.darkenColor(this.color1, 0.5f), ColorUtils.darkenColor(this.color2, 0.5f), ColorUtils.darkenColor(this.color3, 0.5f));
            this.darkMode = true;
        }
    }

    private int mixColors(int color12, int color22, float amount) {
        float inverseAmount = 1.0f - amount;
        return Color.rgb((int) ((((float) Color.red(color12)) * amount) + (((float) Color.red(color22)) * inverseAmount)), (int) ((((float) Color.green(color12)) * amount) + (((float) Color.green(color22)) * inverseAmount)), (int) ((((float) Color.blue(color12)) * amount) + (((float) Color.blue(color22)) * inverseAmount)));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void}
     arg types: [int, int, int, int, int, int, android.graphics.Shader$TileMode]
     candidates:
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long, long, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, long[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void} */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.RadialGradient.<init>(float, float, float, int, int, android.graphics.Shader$TileMode):void}
     arg types: [int, int, float, int, int, android.graphics.Shader$TileMode]
     candidates:
      ClspMth{android.graphics.RadialGradient.<init>(float, float, float, long, long, android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.RadialGradient.<init>(float, float, float, int[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.RadialGradient.<init>(float, float, float, long[], float[], android.graphics.Shader$TileMode):void}
      ClspMth{android.graphics.RadialGradient.<init>(float, float, float, int, int, android.graphics.Shader$TileMode):void} */
    /* access modifiers changed from: private */
    public void generateBitmap(BitmapDrawable bitmapDrawable) {
        LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, 540.0f, mixColors(this.color2, this.color1, TOP_GRADIENT_COLOR_MIX_AMOUNT), this.color2, Shader.TileMode.CLAMP);
        if (this.linearGradientPaint == null) {
            this.linearGradientPaint = new Paint();
        }
        this.linearGradientPaint.setShader(linearGradient);
        this.linearGradientPaint.setDither(true);
        RadialGradient radialGradient = new RadialGradient(960.0f, -300.0f, (float) ((int) Math.sqrt(1627200.0d)), this.color3, 0, Shader.TileMode.CLAMP);
        if (this.radialGradientPaint == null) {
            this.radialGradientPaint = new Paint();
        }
        this.radialGradientPaint.setShader(radialGradient);
        this.radialGradientPaint.setDither(true);
        if (this.overlayBitmap == null) {
            this.overlayBitmap = BitmapFactory.decodeResource(this.context.getResources(), C1167R.C1168drawable.home_background_overlay);
            this.overlayPaint = new Paint();
            this.overlayPaint.setShader(new BitmapShader(this.overlayBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        }
        Canvas c = new Canvas(bitmapDrawable.getBitmap());
        c.drawRect(0.0f, 0.0f, 960.0f, 540.0f, this.linearGradientPaint);
        c.drawRect(0.0f, 0.0f, 960.0f, 540.0f, this.radialGradientPaint);
        c.drawRect(0.0f, 0.0f, 960.0f, 540.0f, this.overlayPaint);
    }

    private class GenerateBitmapTask extends AsyncTask<BitmapDrawable, Void, Void> {
        private GenerateBitmapTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(BitmapDrawable... bitmapDrawable) {
            HomeBackgroundController.this.generateBitmap(bitmapDrawable[0]);
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            GenerateBitmapTask unused = HomeBackgroundController.this.generateBitmapTask = null;
            HomeBackgroundController.this.transitionDrawable.animateFadeIn(600);
        }
    }
}
