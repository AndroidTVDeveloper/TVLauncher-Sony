package com.google.android.tvlauncher.appsview.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class InstallingLaunchItemTranscoder implements ResourceTranscoder<Bitmap, InstallingItemPaletteBitmapContainer> {
    private final BitmapPool bitmapPool;
    private final Paint paint = new Paint();

    public InstallingLaunchItemTranscoder(Context context, float darkenFactor, float saturation) {
        this.bitmapPool = Glide.get(context).getBitmapPool();
        ColorMatrix darkenMatrix = new ColorMatrix(new float[]{darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        ColorMatrix colorTransformMatrix = new ColorMatrix();
        colorTransformMatrix.setSaturation(saturation);
        colorTransformMatrix.postConcat(darkenMatrix);
        this.paint.setColorFilter(new ColorMatrixColorFilter(colorTransformMatrix));
    }

    public Resource<InstallingItemPaletteBitmapContainer> transcode(Resource<Bitmap> toTranscode, Options options) {
        Bitmap bitmap = toTranscode.get();
        return new InstallingItemPaletteBitmapResource(new InstallingItemPaletteBitmapContainer(applyBitmapTransform(bitmap), Palette.from(bitmap).generate()), this.bitmapPool);
    }

    public Bitmap applyBitmapTransform(Bitmap original) {
        Bitmap transformed = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.RGB_565);
        new Canvas(transformed).drawBitmap(original, 0.0f, 0.0f, this.paint);
        return transformed;
    }
}
