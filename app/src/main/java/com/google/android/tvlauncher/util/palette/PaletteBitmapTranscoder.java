package com.google.android.tvlauncher.util.palette;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmapContainer> {
    private final BitmapPool bitmapPool;

    PaletteBitmapTranscoder(Context context) {
        this.bitmapPool = Glide.get(context).getBitmapPool();
    }

    public Resource<PaletteBitmapContainer> transcode(Resource<Bitmap> toTranscode, Options options) {
        Bitmap bitmap = toTranscode.get();
        return new PaletteBitmapResource(new PaletteBitmapContainer(bitmap, Palette.from(bitmap).generate()), this.bitmapPool);
    }
}
