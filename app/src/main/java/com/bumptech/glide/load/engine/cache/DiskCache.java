package com.bumptech.glide.load.engine.cache;

import com.bumptech.glide.load.Key;
import java.io.File;

public interface DiskCache {

    interface Factory {
        String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
        int DEFAULT_DISK_CACHE_SIZE = 262144000;

        DiskCache build();
    }

    interface Writer {
        boolean write(File file);
    }

    void clear();

    void delete(Key key);

    File get(Key key);

    void put(Key key, Writer writer);
}
