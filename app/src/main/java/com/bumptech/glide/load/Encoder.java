package com.bumptech.glide.load;

import java.io.File;

public interface Encoder<T> {
    boolean encode(Object obj, File file, Options options);
}
