package com.bumptech.glide.load.resource;

import android.graphics.ImageDecoder;
import android.util.Log;
import android.util.Size;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.HardwareConfigState;
import java.io.IOException;

public abstract class ImageDecoderResourceDecoder<T> implements ResourceDecoder<ImageDecoder.Source, T> {
    private static final String TAG = "ImageDecoder";
    final HardwareConfigState hardwareConfigState = HardwareConfigState.getInstance();

    /* access modifiers changed from: protected */
    public abstract Resource<T> decode(ImageDecoder.Source source, int i, int i2, ImageDecoder.OnHeaderDecodedListener onHeaderDecodedListener) throws IOException;

    public final boolean handles(ImageDecoder.Source source, Options options) {
        return true;
    }

    public final Resource<T> decode(ImageDecoder.Source source, int requestedWidth, int requestedHeight, Options options) throws IOException {
        DecodeFormat decodeFormat = (DecodeFormat) options.get(Downsampler.DECODE_FORMAT);
        DownsampleStrategy strategy = (DownsampleStrategy) options.get(DownsampleStrategy.OPTION);
        final boolean isHardwareConfigAllowed = options.get(Downsampler.ALLOW_HARDWARE_CONFIG) != null && ((Boolean) options.get(Downsampler.ALLOW_HARDWARE_CONFIG)).booleanValue();
        final int i = requestedWidth;
        final int i2 = requestedHeight;
        final DecodeFormat decodeFormat2 = decodeFormat;
        final DownsampleStrategy downsampleStrategy = strategy;
        return decode(source, requestedWidth, requestedHeight, new ImageDecoder.OnHeaderDecodedListener() {
            public void onHeaderDecoded(ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source source) {
                if (ImageDecoderResourceDecoder.this.hardwareConfigState.isHardwareConfigAllowed(i, i2, isHardwareConfigAllowed, false)) {
                    decoder.setAllocator(3);
                } else {
                    decoder.setAllocator(1);
                }
                if (decodeFormat2 == DecodeFormat.PREFER_RGB_565) {
                    decoder.setMemorySizePolicy(0);
                }
                decoder.setOnPartialImageListener(new ImageDecoder.OnPartialImageListener(this) {
                    public boolean onPartialImage(ImageDecoder.DecodeException e) {
                        return false;
                    }
                });
                Size size = info.getSize();
                int targetWidth = i;
                if (i == Integer.MIN_VALUE) {
                    targetWidth = size.getWidth();
                }
                int targetHeight = i2;
                if (i2 == Integer.MIN_VALUE) {
                    targetHeight = size.getHeight();
                }
                float scaleFactor = downsampleStrategy.getScaleFactor(size.getWidth(), size.getHeight(), targetWidth, targetHeight);
                int resizeWidth = Math.round(((float) size.getWidth()) * scaleFactor);
                int resizeHeight = Math.round(((float) size.getHeight()) * scaleFactor);
                if (Log.isLoggable(ImageDecoderResourceDecoder.TAG, 2)) {
                    int width = size.getWidth();
                    int height = size.getHeight();
                    StringBuilder sb = new StringBuilder(97);
                    sb.append("Resizing from [");
                    sb.append(width);
                    sb.append("x");
                    sb.append(height);
                    sb.append("] to [");
                    sb.append(resizeWidth);
                    sb.append("x");
                    sb.append(resizeHeight);
                    sb.append("] scaleFactor: ");
                    sb.append(scaleFactor);
                    Log.v(ImageDecoderResourceDecoder.TAG, sb.toString());
                }
                decoder.setTargetSize(resizeWidth, resizeHeight);
            }
        });
    }
}
