package com.google.android.tvlauncher.home.util;

public class ChannelStateSettings {
    private int channelItemsTitleMarginBottom;
    private int channelItemsTitleMarginTop;
    private int channelLogoAlignmentOriginMargin;
    private int channelLogoHeight;
    private int channelLogoKeylineOffset;
    private int channelLogoMarginEnd;
    private int channelLogoMarginStart;
    private int channelLogoTitleMarginBottom;
    private int channelLogoWidth;
    private int emptyChannelMessageMarginTop;
    private int itemHeight;
    private int itemMarginBottom;
    private int itemMarginTop;
    private int marginBottom;
    private int marginTop;

    private ChannelStateSettings(Builder builder) {
        this.itemHeight = builder.itemHeight;
        this.itemMarginTop = builder.itemMarginTop;
        this.itemMarginBottom = builder.itemMarginBottom;
        this.marginTop = builder.marginTop;
        this.marginBottom = builder.marginBottom;
        this.channelLogoAlignmentOriginMargin = builder.channelLogoAlignmentOriginMargin;
        this.channelLogoWidth = builder.channelLogoWidth;
        this.channelLogoHeight = builder.channelLogoHeight;
        this.channelLogoMarginStart = builder.channelLogoMarginStart;
        this.channelLogoMarginEnd = builder.channelLogoMarginEnd;
        this.channelLogoKeylineOffset = builder.channelLogoKeylineOffset;
        this.channelLogoTitleMarginBottom = builder.channelLogoTitleMarginBottom;
        this.channelItemsTitleMarginTop = builder.channelItemsTitleMarginTop;
        this.channelItemsTitleMarginBottom = builder.channelItemsTitleMarginBottom;
        this.emptyChannelMessageMarginTop = builder.emptyChannelMessageMarginTop;
    }

    ChannelStateSettings(ChannelStateSettings copy) {
        this.itemHeight = copy.getItemHeight();
        this.itemMarginTop = copy.getItemMarginTop();
        this.itemMarginBottom = copy.getItemMarginBottom();
        this.marginTop = copy.getMarginTop();
        this.marginBottom = copy.getMarginBottom();
        this.channelLogoAlignmentOriginMargin = copy.getChannelLogoAlignmentOriginMargin();
        this.channelLogoWidth = copy.getChannelLogoWidth();
        this.channelLogoHeight = copy.getChannelLogoHeight();
        this.channelLogoMarginStart = copy.getChannelLogoMarginStart();
        this.channelLogoMarginEnd = copy.getChannelLogoMarginEnd();
        this.channelLogoKeylineOffset = copy.getChannelLogoKeylineOffset();
        this.channelLogoTitleMarginBottom = copy.getChannelLogoTitleMarginBottom();
        this.channelItemsTitleMarginTop = copy.getChannelItemsTitleMarginTop();
        this.channelItemsTitleMarginBottom = copy.getChannelItemsTitleMarginBottom();
        this.emptyChannelMessageMarginTop = copy.getEmptyChannelMessageMarginTop();
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    public int getItemMarginTop() {
        return this.itemMarginTop;
    }

    public int getItemMarginBottom() {
        return this.itemMarginBottom;
    }

    public int getMarginTop() {
        return this.marginTop;
    }

    public int getMarginBottom() {
        return this.marginBottom;
    }

    public int getChannelLogoAlignmentOriginMargin() {
        return this.channelLogoAlignmentOriginMargin;
    }

    /* access modifiers changed from: package-private */
    public void setItemHeight(int itemHeight2) {
        this.itemHeight = itemHeight2;
    }

    /* access modifiers changed from: package-private */
    public void setItemMarginTop(int itemMarginTop2) {
        this.itemMarginTop = itemMarginTop2;
    }

    /* access modifiers changed from: package-private */
    public void setItemMarginBottom(int itemMarginBottom2) {
        this.itemMarginBottom = itemMarginBottom2;
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoAlignmentOriginMargin(int channelLogoAlignmentOriginMargin2) {
        this.channelLogoAlignmentOriginMargin = channelLogoAlignmentOriginMargin2;
    }

    public int getChannelLogoWidth() {
        return this.channelLogoWidth;
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoWidth(int channelLogoWidth2) {
        this.channelLogoWidth = channelLogoWidth2;
    }

    public int getChannelLogoHeight() {
        return this.channelLogoHeight;
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoHeight(int channelLogoHeight2) {
        this.channelLogoHeight = channelLogoHeight2;
    }

    public int getChannelLogoMarginStart() {
        return this.channelLogoMarginStart;
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoMarginStart(int channelLogoMarginStart2) {
        this.channelLogoMarginStart = channelLogoMarginStart2;
    }

    public int getChannelLogoMarginEnd() {
        return this.channelLogoMarginEnd;
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoMarginEnd(int channelLogoMarginEnd2) {
        this.channelLogoMarginEnd = channelLogoMarginEnd2;
    }

    public int getChannelLogoKeylineOffset() {
        return this.channelLogoKeylineOffset;
    }

    public int getChannelLogoTitleMarginBottom() {
        return this.channelLogoTitleMarginBottom;
    }

    public int getEmptyChannelMessageMarginTop() {
        return this.emptyChannelMessageMarginTop;
    }

    /* access modifiers changed from: package-private */
    public void setChannelItemsTitleMarginTop(int channelItemsTitleMarginTop2) {
        this.channelItemsTitleMarginTop = channelItemsTitleMarginTop2;
    }

    public int getChannelItemsTitleMarginTop() {
        return this.channelItemsTitleMarginTop;
    }

    /* access modifiers changed from: package-private */
    public void setChannelItemsTitleMarginBottom(int channelItemsTitleMarginBottom2) {
        this.channelItemsTitleMarginBottom = channelItemsTitleMarginBottom2;
    }

    public int getChannelItemsTitleMarginBottom() {
        return this.channelItemsTitleMarginBottom;
    }

    /* access modifiers changed from: package-private */
    public void setEmptyChannelMessageMarginTop(int emptyChannelMessageMarginTop2) {
        this.emptyChannelMessageMarginTop = emptyChannelMessageMarginTop2;
    }

    static class Builder {
        /* access modifiers changed from: private */
        public int channelItemsTitleMarginBottom;
        /* access modifiers changed from: private */
        public int channelItemsTitleMarginTop;
        /* access modifiers changed from: private */
        public int channelLogoAlignmentOriginMargin;
        /* access modifiers changed from: private */
        public int channelLogoHeight;
        /* access modifiers changed from: private */
        public int channelLogoKeylineOffset;
        /* access modifiers changed from: private */
        public int channelLogoMarginEnd;
        /* access modifiers changed from: private */
        public int channelLogoMarginStart;
        /* access modifiers changed from: private */
        public int channelLogoTitleMarginBottom;
        /* access modifiers changed from: private */
        public int channelLogoWidth;
        /* access modifiers changed from: private */
        public int emptyChannelMessageMarginTop;
        /* access modifiers changed from: private */
        public int itemHeight;
        /* access modifiers changed from: private */
        public int itemMarginBottom;
        /* access modifiers changed from: private */
        public int itemMarginTop;
        /* access modifiers changed from: private */
        public int marginBottom;
        /* access modifiers changed from: private */
        public int marginTop;

        Builder() {
        }

        /* access modifiers changed from: package-private */
        public Builder setItemHeight(int itemHeight2) {
            this.itemHeight = itemHeight2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setItemMarginTop(int itemMarginTop2) {
            this.itemMarginTop = itemMarginTop2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setItemMarginBottom(int itemMarginBottom2) {
            this.itemMarginBottom = itemMarginBottom2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setMarginTop(int marginTop2) {
            this.marginTop = marginTop2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setMarginBottom(int marginBottom2) {
            this.marginBottom = marginBottom2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoAlignmentOriginMargin(int channelLogoAlignmentOriginMargin2) {
            this.channelLogoAlignmentOriginMargin = channelLogoAlignmentOriginMargin2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoWidth(int channelLogoWidth2) {
            this.channelLogoWidth = channelLogoWidth2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoHeight(int channelLogoHeight2) {
            this.channelLogoHeight = channelLogoHeight2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoMarginStart(int channelLogoMarginStart2) {
            this.channelLogoMarginStart = channelLogoMarginStart2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoMarginEnd(int channelLogoMarginEnd2) {
            this.channelLogoMarginEnd = channelLogoMarginEnd2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoKeylineOffset(int channelLogoKeylineOffset2) {
            this.channelLogoKeylineOffset = channelLogoKeylineOffset2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelLogoTitleMarginBottom(int channelLogoTitleMarginBottom2) {
            this.channelLogoTitleMarginBottom = channelLogoTitleMarginBottom2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelItemsTitleMarginTop(int channelItemsTitleMarginTop2) {
            this.channelItemsTitleMarginTop = channelItemsTitleMarginTop2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setChannelItemsTitleMarginBottom(int channelItemsTitleMarginBottom2) {
            this.channelItemsTitleMarginBottom = channelItemsTitleMarginBottom2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setEmptyChannelMessageMarginTop(int emptyChannelMessageMarginTop2) {
            this.emptyChannelMessageMarginTop = emptyChannelMessageMarginTop2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public ChannelStateSettings build() {
            return new ChannelStateSettings(this);
        }
    }
}
