package com.google.android.tvlauncher.appsview.palette;

import android.graphics.Bitmap;
import androidx.palette.graphics.Palette;

public class InstallingItemPaletteBitmapContainer {
    private final Bitmap bitmap;
    private final Palette palette;

    public InstallingItemPaletteBitmapContainer(Bitmap bitmap2, Palette palette2) {
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
