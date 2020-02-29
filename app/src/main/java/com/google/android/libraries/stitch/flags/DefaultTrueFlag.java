package com.google.android.libraries.stitch.flags;

public final class DefaultTrueFlag extends Flag {
    private boolean defaultValue = true;

    public DefaultTrueFlag(String name) {
        super(name);
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public void setForTesting(boolean defaultValue2) {
        this.defaultValue = defaultValue2;
    }
}
