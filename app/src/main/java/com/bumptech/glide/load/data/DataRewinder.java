package com.bumptech.glide.load.data;

import java.io.IOException;

public interface DataRewinder<T> {

    public interface Factory<T> {
        DataRewinder<T> build(Object obj);

        Class<T> getDataClass();
    }

    void cleanup();

    T rewindAndGet() throws IOException;
}
