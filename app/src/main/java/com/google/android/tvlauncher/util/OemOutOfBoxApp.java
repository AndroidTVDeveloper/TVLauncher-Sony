package com.google.android.tvlauncher.util;

import com.google.android.tvlauncher.util.OemAppBase;

public class OemOutOfBoxApp extends OemAppBase {
    private final boolean canHide;
    private final boolean canMove;

    private OemOutOfBoxApp(Builder builder) {
        super(builder);
        this.canMove = builder.canMove;
        this.canHide = builder.canHide;
    }

    public static final class Builder extends OemAppBase.Builder<Builder> {
        boolean canHide;
        boolean canMove;

        public void setCanMove(boolean canMove2) {
            this.canMove = canMove2;
        }

        public void setCanHide(boolean canHide2) {
            this.canHide = canHide2;
        }

        public OemOutOfBoxApp build() {
            return new OemOutOfBoxApp(this);
        }
    }

    public boolean canMove() {
        return this.canMove;
    }

    public boolean canHide() {
        return this.canHide;
    }
}
