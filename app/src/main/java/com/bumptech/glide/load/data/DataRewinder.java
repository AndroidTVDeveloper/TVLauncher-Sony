package com.bumptech.glide.load.data;

import java.io.IOException;

public interface DataRewinder<T> {

    interface Factory<T> {
        DataRewinder<T> build(Object obj);

        Class<T> getDataClass();
    }

    void cleanup();

    T rewindAndGet() throws IOException;
}
