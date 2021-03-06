package com.bumptech.glide.load.model;

import com.bumptech.glide.load.model.LazyHeaders;
import java.util.Collections;
import java.util.Map;

public interface Headers {
    Headers DEFAULT = new LazyHeaders.Builder().build();
    @Deprecated
    Headers NONE = new Headers() {
        public Map<String, String> getHeaders() {
            return Collections.emptyMap();
        }
    };

    Map<String, String> getHeaders();
}
