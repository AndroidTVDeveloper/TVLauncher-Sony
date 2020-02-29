package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.tvlauncher.C1167R;

public class ChannelViewMainLinearLayout extends LinearLayout {
    int channelLogoKeylineOffset;
    boolean isSponsored;
    private View logo;
    private View zoomedOutLogoTitle;
    private boolean zoomedOutState = false;

    public ChannelViewMainLinearLayout(Context context) {
        super(context);
    }

    public ChannelViewMainLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelViewMainLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.logo = findViewById(C1167R.C1170id.channel_logo);
        this.zoomedOutLogoTitle = findViewById(C1167R.C1170id.logo_title_zoomed_out);
    }

    /* access modifiers changed from: package-private */
    public void setIsSponsored(boolean isSponsored2) {
        this.isSponsored = isSponsored2;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!this.zoomedOutState) {
            int titleTop = (((this.logo.getTop() + (this.logo.getHeight() / 2)) - (this.zoomedOutLogoTitle.getHeight() / 2)) - this.channelLogoKeylineOffset) + ((ViewGroup.MarginLayoutParams) this.zoomedOutLogoTitle.getLayoutParams()).topMargin;
            View view = this.zoomedOutLogoTitle;
            view.layout(view.getLeft(), titleTop, this.zoomedOutLogoTitle.getRight(), this.zoomedOutLogoTitle.getHeight() + titleTop);
        }
    }

    public void setZoomedOutState(boolean zoomedOutState2) {
        this.zoomedOutState = zoomedOutState2;
    }

    public void setChannelLogoKeylineOffset(int channelLogoKeylineOffset2) {
        this.channelLogoKeylineOffset = channelLogoKeylineOffset2;
    }
}
