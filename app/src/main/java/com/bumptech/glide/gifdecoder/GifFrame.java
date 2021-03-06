package com.bumptech.glide.gifdecoder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class GifFrame {
    static final int DISPOSAL_BACKGROUND = 2;
    static final int DISPOSAL_NONE = 1;
    static final int DISPOSAL_PREVIOUS = 3;
    static final int DISPOSAL_UNSPECIFIED = 0;
    int bufferFrameStart;
    int delay;
    int dispose;

    /* renamed from: ih */
    int f50ih;
    boolean interlace;

    /* renamed from: iw */
    int f51iw;

    /* renamed from: ix */
    int f52ix;

    /* renamed from: iy */
    int f53iy;
    int[] lct;
    int transIndex;
    boolean transparency;

    @Retention(RetentionPolicy.SOURCE)
    private @interface GifDisposalMethod {
    }

    GifFrame() {
    }
}
