package com.google.android.tvlauncher.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;
import java.util.Arrays;

public final class AddBackgroundColorTransformation extends BitmapTransformation {
    private static final byte[] baseKeyBytes = "com.google.android.tvLauncher.util.AddBackgroundColorTransformation".getBytes(CHARSET);
    private final int backgroundColor;
    private int hashCode;
    private boolean hashCodeInitialized = false;
    private final byte[] keyBytes;
    private boolean useTargetDimensions;

    public AddBackgroundColorTransformation(int color, boolean useTargetDimensions2) {
        this.backgroundColor = color;
        byte[] bArr = baseKeyBytes;
        this.keyBytes = new byte[(bArr.length + 5)];
        System.arraycopy(bArr, 0, this.keyBytes, 0, bArr.length);
        byte[] bArr2 = this.keyBytes;
        bArr2[bArr2.length - 5] = useTargetDimensions2 ? (byte) 1 : 0;
        bArr2[bArr2.length - 4] = (byte) ((color >> 24) & 15);
        bArr2[bArr2.length - 3] = (byte) ((color >> 16) & 15);
        bArr2[bArr2.length - 2] = (byte) ((color >> 8) & 15);
        bArr2[bArr2.length - 1] = (byte) (color & 15);
        this.useTargetDimensions = useTargetDimensions2;
    }

    /* access modifiers changed from: protected */
    public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (this.useTargetDimensions) {
            return transformUsingTargetDimens(pool, toTransform, outWidth, outHeight);
        }
        return transformUsingOriginalDimens(pool, toTransform);
    }

    public boolean equals(Object o) {
        return (o instanceof AddBackgroundColorTransformation) && ((AddBackgroundColorTransformation) o).backgroundColor == this.backgroundColor;
    }

    public int hashCode() {
        if (this.hashCodeInitialized) {
            return this.hashCode;
        }
        this.hashCodeInitialized = true;
        this.hashCode = Arrays.hashCode(this.keyBytes);
        return this.hashCode;
    }

    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(this.keyBytes);
    }

    private Bitmap transformUsingOriginalDimens(BitmapPool pool, Bitmap toTransform) {
        if (!toTransform.hasAlpha()) {
            return toTransform;
        }
        Bitmap result = pool.getDirty(toTransform.getWidth(), toTransform.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(this.backgroundColor);
        canvas.drawBitmap(toTransform, 0.0f, 0.0f, (Paint) null);
        return result;
    }

    private Bitmap transformUsingTargetDimens(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        if (width < outWidth || height < outHeight) {
            Bitmap result = pool.getDirty(outWidth, outHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(this.backgroundColor);
            canvas.drawBitmap(toTransform, (float) ((outWidth - width) / 2), (float) ((outHeight - height) / 2), (Paint) null);
            return result;
        } else if (!toTransform.hasAlpha()) {
            return toTransform;
        } else {
            return transformUsingOriginalDimens(pool, toTransform);
        }
    }
}
