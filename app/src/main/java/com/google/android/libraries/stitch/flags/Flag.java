package com.google.android.libraries.stitch.flags;

public abstract class Flag {
    private final String name;

    protected Flag(String name2) {
        this.name = name2;
    }

    /* access modifiers changed from: protected */
    public final String getName() {
        return this.name;
    }
}
