package com.google.android.tvlauncher.appsview.palette;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Util;

public class InstallingItemPaletteBitmapResource implements Resource<InstallingItemPaletteBitmapContainer> {
    private final BitmapPool bitmapPool;
    private final InstallingItemPaletteBitmapContainer paletteBitmapContainer;

    public InstallingItemPaletteBitmapResource(InstallingItemPaletteBitmapContainer paletteBitmapContainer2, BitmapPool bitmapPool2) {
        this.paletteBitmapContainer = paletteBitmapContainer2;
        this.bitmapPool = bitmapPool2;
    }

    public Class<InstallingItemPaletteBitmapContainer> getResourceClass() {
        return InstallingItemPaletteBitmapContainer.class;
    }

    public InstallingItemPaletteBitmapContainer get() {
        return this.paletteBitmapContainer;
    }

    public int getSize() {
        return Util.getBitmapByteSize(this.paletteBitmapContainer.getBitmap());
    }

    public void recycle() {
        this.bitmapPool.put(this.paletteBitmapContainer.getBitmap());
    }
}
