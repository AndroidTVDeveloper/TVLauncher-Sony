package com.google.android.tvlauncher.home.operatorbackground;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.android.tvlauncher.C1167R;
import java.security.MessageDigest;
import java.util.Arrays;

public final class CropBackgroundBitmapTransformation extends BitmapTransformation {
    private static final int HASH_CODE = Arrays.hashCode(keyBytes);
    private static final byte[] keyBytes = CropBackgroundBitmapTransformation.class.getName().getBytes(CHARSET);
    private final int viewPortHeight;
    private final int viewPortWidth;

    CropBackgroundBitmapTransformation(Context context) {
        this.viewPortWidth = context.getResources().getDimensionPixelSize(C1167R.dimen.home_background_image_view_port_width);
        this.viewPortHeight = context.getResources().getDimensionPixelSize(C1167R.dimen.home_background_image_view_port_height);
    }

    /* access modifiers changed from: protected */
    public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap result = pool.getDirty(this.viewPortWidth, this.viewPortHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        Rect source = new Rect();
        source.left = (width - this.viewPortWidth) / 2;
        source.top = (height - this.viewPortHeight) / 2;
        source.right = source.left + this.viewPortWidth;
        int i = source.top;
        int i2 = this.viewPortHeight;
        source.bottom = i + i2;
        canvas.drawBitmap(toTransform, source, new Rect(0, 0, this.viewPortWidth, i2), (Paint) null);
        return result;
    }

    public boolean equals(Object o) {
        return o instanceof CropBackgroundBitmapTransformation;
    }

    public int hashCode() {
        return HASH_CODE;
    }

    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(keyBytes);
    }
}
