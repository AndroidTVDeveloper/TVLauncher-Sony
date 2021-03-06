package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import java.io.File;

public class BitmapDrawableEncoder implements ResourceEncoder<BitmapDrawable> {
    private final BitmapPool bitmapPool;
    private final ResourceEncoder<Bitmap> encoder;

    public /* bridge */ /* synthetic */ boolean encode(Object obj, File file, Options options) {
        return encode((Resource<BitmapDrawable>) ((Resource) obj), file, options);
    }

    public BitmapDrawableEncoder(BitmapPool bitmapPool2, ResourceEncoder<Bitmap> encoder2) {
        this.bitmapPool = bitmapPool2;
        this.encoder = encoder2;
    }

    public boolean encode(Resource<BitmapDrawable> data, File file, Options options) {
        return this.encoder.encode(new BitmapResource(data.get().getBitmap(), this.bitmapPool), file, options);
    }

    public EncodeStrategy getEncodeStrategy(Options options) {
        return this.encoder.getEncodeStrategy(options);
    }
}
