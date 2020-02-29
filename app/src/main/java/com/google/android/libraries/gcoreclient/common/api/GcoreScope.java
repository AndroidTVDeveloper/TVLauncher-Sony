package com.google.android.libraries.gcoreclient.common.api;

@Deprecated
public interface GcoreScope {

    public interface Builder {
        GcoreScope build();

        Builder setScopeUri(String str);
    }
}
