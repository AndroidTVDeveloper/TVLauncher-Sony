package com.google.android.libraries.stitch.flags;

public final class DebugFlag extends Flag {
    private boolean defaultDebugValue;

    public DebugFlag(String name) {
        this(name, true);
    }

    public DebugFlag(String name, boolean defaultDebugValue2) {
        super(name);
        this.defaultDebugValue = defaultDebugValue2;
    }

    public boolean getDefaultDebugValue() {
        return this.defaultDebugValue;
    }

    public void setForTesting(boolean defaultDebugValue2) {
        this.defaultDebugValue = defaultDebugValue2;
    }
}
