package com.bumptech.glide.gifdecoder;

import android.graphics.Bitmap;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

public interface GifDecoder {
    int STATUS_FORMAT_ERROR = 1;
    int STATUS_OK = 0;
    int STATUS_OPEN_ERROR = 2;
    int STATUS_PARTIAL_DECODE = 3;
    int TOTAL_ITERATION_COUNT_FOREVER = 0;

    interface BitmapProvider {
        Bitmap obtain(int i, int i2, Bitmap.Config config);

        byte[] obtainByteArray(int i);

        int[] obtainIntArray(int i);

        void release(Bitmap bitmap);

        void release(byte[] bArr);

        void release(int[] iArr);
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface GifDecodeStatus {
    }

    void advance();

    void clear();

    int getByteSize();

    int getCurrentFrameIndex();

    ByteBuffer getData();

    int getDelay(int i);

    int getFrameCount();

    int getHeight();

    @Deprecated
    int getLoopCount();

    int getNetscapeLoopCount();

    int getNextDelay();

    Bitmap getNextFrame();

    int getStatus();

    int getTotalIterationCount();

    int getWidth();

    int read(InputStream inputStream, int i);

    int read(byte[] bArr);

    void resetFrameIndex();

    void setData(GifHeader gifHeader, ByteBuffer byteBuffer);

    void setData(GifHeader gifHeader, ByteBuffer byteBuffer, int i);

    void setData(GifHeader gifHeader, byte[] bArr);

    void setDefaultBitmapConfig(Bitmap.Config config);
}
