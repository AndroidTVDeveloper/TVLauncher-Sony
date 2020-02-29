package com.google.android.tvlauncher.home.util;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Util;

class ProgramPreviewImageResource implements Resource<ProgramPreviewImageData> {
    private final BitmapPool bitmapPool;
    private final ProgramPreviewImageData programPreviewImageData;

    ProgramPreviewImageResource(ProgramPreviewImageData programPreviewImageData2, BitmapPool bitmapPool2) {
        this.programPreviewImageData = programPreviewImageData2;
        this.bitmapPool = bitmapPool2;
    }

    public Class<ProgramPreviewImageData> getResourceClass() {
        return ProgramPreviewImageData.class;
    }

    public ProgramPreviewImageData get() {
        return this.programPreviewImageData;
    }

    public int getSize() {
        return Util.getBitmapByteSize(this.programPreviewImageData.getBitmap());
    }

    public void recycle() {
        this.bitmapPool.put(this.programPreviewImageData.getBitmap());
    }
}
