package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.util.Log;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.resource.ImageDecoderResourceDecoder;
import java.io.IOException;

public final class BitmapImageDecoderResourceDecoder extends ImageDecoderResourceDecoder<Bitmap> {
    private static final String TAG = "BitmapImageDecoder";
    private final BitmapPool bitmapPool = new BitmapPoolAdapter();

    /* access modifiers changed from: protected */
    public Resource<Bitmap> decode(ImageDecoder.Source source, int requestedResourceWidth, int requestedResourceHeight, ImageDecoder.OnHeaderDecodedListener listener) throws IOException {
        Bitmap result = ImageDecoder.decodeBitmap(source, listener);
        if (Log.isLoggable(TAG, 2)) {
            int width = result.getWidth();
            int height = result.getHeight();
            StringBuilder sb = new StringBuilder(63);
            sb.append("Decoded [");
            sb.append(width);
            sb.append("x");
            sb.append(height);
            sb.append("] for [");
            sb.append(requestedResourceWidth);
            sb.append("x");
            sb.append(requestedResourceHeight);
            sb.append("]");
            Log.v(TAG, sb.toString());
        }
        return new BitmapResource(result, this.bitmapPool);
    }
}
