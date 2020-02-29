package com.google.android.tvlauncher.util.palette;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Util;

class PaletteBitmapResource implements Resource<PaletteBitmapContainer> {
    private final BitmapPool bitmapPool;
    private final PaletteBitmapContainer paletteBitmapContainer;

    PaletteBitmapResource(PaletteBitmapContainer paletteBitmapContainer2, BitmapPool bitmapPool2) {
        this.paletteBitmapContainer = paletteBitmapContainer2;
        this.bitmapPool = bitmapPool2;
    }

    public Class<PaletteBitmapContainer> getResourceClass() {
        return PaletteBitmapContainer.class;
    }

    public PaletteBitmapContainer get() {
        return this.paletteBitmapContainer;
    }

    public int getSize() {
        return Util.getBitmapByteSize(this.paletteBitmapContainer.getBitmap());
    }

    public void recycle() {
        this.bitmapPool.put(this.paletteBitmapContainer.getBitmap());
    }
}
