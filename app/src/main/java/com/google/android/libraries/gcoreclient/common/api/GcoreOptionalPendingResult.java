package com.google.android.libraries.gcoreclient.common.api;

import com.google.android.libraries.gcoreclient.common.api.GcoreResult;

@Deprecated
public interface GcoreOptionalPendingResult<GR extends GcoreResult> extends GcorePendingResult<GR> {
    GR get();

    boolean isDone();
}
