package com.google.android.libraries.gcoreclient.phenotype;

import com.google.android.libraries.gcoreclient.common.api.GcoreResult;

@Deprecated
public interface GcoreExperimentTokensResult extends GcoreResult {
    GcoreExperimentTokens getExperimentTokens();
}
