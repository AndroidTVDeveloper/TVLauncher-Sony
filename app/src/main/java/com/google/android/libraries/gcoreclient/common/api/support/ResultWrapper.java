package com.google.android.libraries.gcoreclient.common.api.support;

import com.google.android.gms.common.api.Result;
import com.google.android.libraries.gcoreclient.common.api.GcoreResult;

@Deprecated
public interface ResultWrapper<GR extends GcoreResult, R extends Result> {
    GR wrap(Result result);
}
