package com.google.android.libraries.gcoreclient.phenotype;

import com.google.android.libraries.gcoreclient.common.api.GcoreApi;
import com.google.android.libraries.gcoreclient.common.api.GcoreApi.GcoreApiOptions.GcoreNoOptions;

@Deprecated
public interface PhenotypeApi<O extends GcoreApi.GcoreApiOptions.GcoreNoOptions> extends GcoreApi<O> {
    boolean isNull();
}
