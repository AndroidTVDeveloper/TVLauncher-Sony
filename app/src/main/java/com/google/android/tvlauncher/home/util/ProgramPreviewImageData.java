package com.google.android.tvlauncher.home.util;

import android.graphics.Bitmap;
import androidx.palette.graphics.Palette;

public class ProgramPreviewImageData {
    private final Bitmap bitmap;
    private final Bitmap blurredBitmap;
    private final Palette palette;

    ProgramPreviewImageData(Bitmap bitmap2, Bitmap blurredBitmap2, Palette palette2) {
        this.bitmap = bitmap2;
        this.blurredBitmap = blurredBitmap2;
        this.palette = palette2;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public Bitmap getBlurredBitmap() {
        return this.blurredBitmap;
    }

    public Palette getPalette() {
        return this.palette;
    }
}
