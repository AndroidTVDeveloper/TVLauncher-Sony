package com.google.android.tvlauncher.appsview.transformation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;
import java.util.Arrays;

public final class AppBannerInstallingIconTransformation extends BitmapTransformation {
    private static final byte[] baseKeyBytes = AppBannerInstallingIconTransformation.class.getName().getBytes(CHARSET);
    private final int backgroundColor;
    private final int bannerHeight;
    private final int bannerWidth;
    private int hashCode;
    private boolean hashCodeInitialized = false;
    private final float iconRoundingRadius;
    private final byte[] keyBytes;
    private final Paint paint;

    public AppBannerInstallingIconTransformation(int backgroundColor2, float darkenFactor, float saturation, int bannerWidth2, int bannerHeight2, float iconRoundingRadius2) {
        int i = backgroundColor2;
        int i2 = bannerWidth2;
        int i3 = bannerHeight2;
        this.backgroundColor = i;
        this.bannerWidth = i2;
        this.bannerHeight = i3;
        this.iconRoundingRadius = iconRoundingRadius2;
        byte[] bArr = baseKeyBytes;
        this.keyBytes = new byte[(bArr.length + 9)];
        System.arraycopy(bArr, 0, this.keyBytes, 0, bArr.length);
        byte[] bArr2 = this.keyBytes;
        bArr2[bArr2.length - 9] = (byte) (i2 & 15);
        bArr2[bArr2.length - 8] = (byte) (i3 & 15);
        bArr2[bArr2.length - 7] = Float.valueOf(iconRoundingRadius2).byteValue();
        byte[] bArr3 = this.keyBytes;
        bArr3[bArr3.length - 6] = Float.valueOf(saturation).byteValue();
        byte[] bArr4 = this.keyBytes;
        bArr4[bArr4.length - 5] = Float.valueOf(darkenFactor).byteValue();
        byte[] bArr5 = this.keyBytes;
        bArr5[bArr5.length - 4] = (byte) ((i >> 24) & 15);
        bArr5[bArr5.length - 3] = (byte) ((i >> 16) & 15);
        bArr5[bArr5.length - 2] = (byte) ((i >> 8) & 15);
        bArr5[bArr5.length - 1] = (byte) (i & 15);
        ColorMatrix darkenMatrix = new ColorMatrix(new float[]{darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, darkenFactor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        ColorMatrix colorTransformMatrix = new ColorMatrix();
        colorTransformMatrix.setSaturation(saturation);
        colorTransformMatrix.postConcat(darkenMatrix);
        this.paint = new Paint();
        this.paint.setColorFilter(new ColorMatrixColorFilter(colorTransformMatrix));
    }

    /* access modifiers changed from: protected */
    public Bitmap transform(BitmapPool pool, Bitmap icon, int iconBoundingBoxWidth, int iconBoundingBoxHeight) {
        int i = iconBoundingBoxWidth;
        int i2 = iconBoundingBoxHeight;
        Bitmap.Config config = icon.getConfig() != null ? icon.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap transformedIcon = createIconBitmap(pool, config, icon, iconBoundingBoxWidth, iconBoundingBoxHeight);
        Bitmap bitmap = pool.getDirty(this.bannerWidth, this.bannerHeight, config);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(this.backgroundColor);
        BitmapShader shader = new BitmapShader(transformedIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setShader(shader);
        canvas.translate((float) ((this.bannerWidth - i) / 2), (float) ((this.bannerHeight - i2) / 2));
        RectF rectF = new RectF(0.0f, 0.0f, (float) i, (float) i2);
        float f = this.iconRoundingRadius;
        canvas.drawRoundRect(rectF, f, f, paint2);
        return bitmap;
    }

    private Bitmap createIconBitmap(BitmapPool pool, Bitmap.Config config, Bitmap icon, int width, int height) {
        int i = width;
        int i2 = height;
        float minPercentage = Math.min(((float) i) / ((float) icon.getWidth()), ((float) i2) / ((float) icon.getHeight()));
        Bitmap toReuse = pool.getDirty(i, i2, config);
        Matrix matrix = new Matrix();
        matrix.postScale(minPercentage, minPercentage);
        matrix.postTranslate(((float) (i - ((int) (((float) icon.getWidth()) * minPercentage)))) / 2.0f, ((float) (i2 - ((int) (((float) icon.getHeight()) * minPercentage)))) / 2.0f);
        new Canvas(toReuse).drawBitmap(icon, matrix, this.paint);
        return toReuse;
    }

    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(this.keyBytes);
    }

    public int hashCode() {
        if (this.hashCodeInitialized) {
            return this.hashCode;
        }
        this.hashCodeInitialized = true;
        this.hashCode = Arrays.hashCode(this.keyBytes);
        return this.hashCode;
    }
}
