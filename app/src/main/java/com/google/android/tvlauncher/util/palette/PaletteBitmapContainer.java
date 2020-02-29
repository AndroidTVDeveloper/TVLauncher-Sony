package com.google.android.tvlauncher.util.palette;

import android.graphics.Bitmap;
import androidx.palette.graphics.Palette;

public class PaletteBitmapContainer {
    private final Bitmap bitmap;
    private final Palette palette;

    PaletteBitmapContainer(Bitmap bitmap2, Palette palette2) {
        this.bitmap = bitmap2;
        this.palette = palette2;
    }

    public Palette getPalette() {
        return this.palette;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }
}
