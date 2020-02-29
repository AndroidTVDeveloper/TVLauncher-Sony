package com.google.android.libraries.gcoreclient.phenotype;

@Deprecated
public interface GcoreFlag {
    boolean getBoolean();

    byte[] getBytes();

    double getDouble();

    String getFlagName();

    int getFlagStorageType();

    int getFlagValueType();

    long getLong();

    String getString();

    int getValueTypeBoolean();

    int getValueTypeBytes();

    int getValueTypeDouble();

    int getValueTypeLong();

    int getValueTypeString();
}
