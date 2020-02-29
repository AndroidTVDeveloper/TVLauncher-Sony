package com.google.android.libraries.gcoreclient.clearcut;

import com.google.android.libraries.gcoreclient.common.api.GcoreApi;
import com.google.android.libraries.gcoreclient.common.api.GcoreApi.GcoreApiOptions.GcoreNoOptions;

@Deprecated
public interface GcoreClearcutApi<O extends GcoreApi.GcoreApiOptions.GcoreNoOptions> extends GcoreApi<O> {

    interface Builder {
        <O extends GcoreApi.GcoreApiOptions.GcoreNoOptions> GcoreClearcutApi<O> build();
    }
}
