package com.google.android.libraries.stitch.flags;

public final class DefaultFalseFlag extends Flag {
    private boolean defaultValue;

    public DefaultFalseFlag(String name) {
        super(name);
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public void setForTesting(boolean defaultValue2) {
        this.defaultValue = defaultValue2;
    }
}
