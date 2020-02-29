package com.google.android.libraries.gcoreclient.common.api;

@Deprecated
public interface GcoreCommonStatusCodes {
    int getErrorStatusCode();

    int getNetworkErrorStatusCode();

    int getSuccessStatusCode();

    int getTimeoutStatusCode();
}
