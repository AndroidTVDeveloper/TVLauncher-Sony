package com.google.android.libraries.gcoreclient.phenotype;

@Deprecated
public interface GcoreConfiguration {
    String[] getDeleteFlags();

    int getFlagType();

    GcoreFlag[] getFlags();
}
