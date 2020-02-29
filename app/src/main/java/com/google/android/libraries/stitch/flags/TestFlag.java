package com.google.android.libraries.stitch.flags;

public final class TestFlag extends Flag {
    private boolean defaultTestValue;

    public TestFlag(String name) {
        this(name, true);
    }

    public TestFlag(String name, boolean defaultTestValue2) {
        super(name);
        this.defaultTestValue = defaultTestValue2;
    }

    public boolean getDefaultTestValue() {
        return this.defaultTestValue;
    }

    public void setForTesting(boolean defaultTestValue2) {
        this.defaultTestValue = defaultTestValue2;
    }
}
