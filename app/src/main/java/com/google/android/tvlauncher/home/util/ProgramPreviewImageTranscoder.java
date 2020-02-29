package com.google.android.tvlauncher.home.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class ProgramPreviewImageTranscoder implements ResourceTranscoder<Bitmap, ProgramPreviewImageData> {
    private static final float BLURRED_BITMAP_SCALE = 0.5f;
    private static final float BLUR_RADIUS = 8.0f;
    private final BitmapPool bitmapPool;
    private ScriptIntrinsicBlur blur;
    private final Context context;
    private RenderScript renderScript;

    public ProgramPreviewImageTranscoder(Context context2) {
        this.context = context2.getApplicationContext();
        this.bitmapPool = Glide.get(context2).getBitmapPool();
    }

    public Resource<ProgramPreviewImageData> transcode(Resource<Bitmap> toTranscode, Options options) {
        Bitmap bitmap = toTranscode.get();
        return new ProgramPreviewImageResource(new ProgramPreviewImageData(bitmap, generateBlurredBitmap(bitmap), Palette.from(bitmap).generate()), this.bitmapPool);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap}
     arg types: [android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, int]
     candidates:
      ClspMth{android.graphics.Bitmap.createBitmap(android.util.DisplayMetrics, int[], int, int, int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap}
      ClspMth{android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap} */
    private Bitmap generateBlurredBitmap(Bitmap originalBitmap) {
        if (this.renderScript == null) {
            this.renderScript = RenderScript.create(this.context);
            RenderScript renderScript2 = this.renderScript;
            this.blur = ScriptIntrinsicBlur.create(renderScript2, Element.U8_4(renderScript2));
            this.blur.setRadius(8.0f);
        }
        Matrix m = new Matrix();
        m.setScale(BLURRED_BITMAP_SCALE, BLURRED_BITMAP_SCALE);
        Bitmap inputBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), m, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        Allocation inputAllocation = Allocation.createFromBitmap(this.renderScript, inputBitmap);
        Allocation outputAllocation = Allocation.createFromBitmap(this.renderScript, outputBitmap);
        this.blur.setInput(inputAllocation);
        this.blur.forEach(outputAllocation);
        outputAllocation.copyTo(outputBitmap);
        return outputBitmap;
    }
}
