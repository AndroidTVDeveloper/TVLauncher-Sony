package com.google.android.libraries.gcoreclient.common.api;

import com.google.android.libraries.gcoreclient.common.api.GcoreResult;

@Deprecated
public interface GcoreResultCallback<R extends GcoreResult> {
    void onResult(R r);
}
