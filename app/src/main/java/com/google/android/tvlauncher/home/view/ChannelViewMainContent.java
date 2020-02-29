package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.tvlauncher.C1167R;

public class ChannelViewMainContent extends FrameLayout {
    private View actionsHint;
    private int channelLogoKeylineOffset;
    private View channelViewMainLinearLayout;
    private boolean isBranded = true;
    private boolean isSponsored;
    private View logo;
    private View logoTitle;
    private View sponsoredChannelBackground;

    public ChannelViewMainContent(Context context) {
        super(context);
    }

    public ChannelViewMainContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelViewMainContent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.logo = findViewById(C1167R.C1170id.channel_logo);
        this.logoTitle = findViewById(C1167R.C1170id.logo_title);
        this.actionsHint = findViewById(C1167R.C1170id.actions_hint);
        this.sponsoredChannelBackground = findViewById(C1167R.C1170id.sponsored_channel_background);
        this.channelViewMainLinearLayout = findViewById(C1167R.C1170id.main_linear_layout);
    }

    /* access modifiers changed from: package-private */
    public void setIsSponsored(boolean sponsored) {
        this.isSponsored = sponsored;
    }

    /* access modifiers changed from: package-private */
    public void setIsBranded(boolean branded) {
        this.isBranded = branded;
    }

    public void setChannelLogoKeylineOffset(int channelLogoKeylineOffset2) {
        this.channelLogoKeylineOffset = channelLogoKeylineOffset2;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int hintLeft;
        super.onLayout(changed, left, top, right, bottom);
        ViewGroup.MarginLayoutParams titleLayoutParams = (ViewGroup.MarginLayoutParams) this.logoTitle.getLayoutParams();
        if (this.isSponsored) {
            alignSponsoredBackgroundCenterToKeyLine();
            if (this.isBranded) {
                attachSponsoredLogoTitleAboveLogo(titleLayoutParams);
            } else {
                int logoTitleTop = (int) (((((float) this.logo.getTop()) + (((float) this.logo.getMeasuredHeight()) / 2.0f)) - (((float) this.logoTitle.getHeight()) / 2.0f)) - ((float) this.channelLogoKeylineOffset));
                View view = this.logoTitle;
                view.layout(view.getLeft(), logoTitleTop, this.logoTitle.getRight(), this.logoTitle.getMeasuredHeight() + logoTitleTop);
            }
        } else {
            int titleTop = this.logo.getBottom() + titleLayoutParams.topMargin;
            View view2 = this.logoTitle;
            view2.layout(view2.getLeft(), titleTop, this.logoTitle.getRight(), this.logoTitle.getMeasuredHeight() + titleTop);
        }
        ViewGroup.MarginLayoutParams actionsHintLayoutParams = (ViewGroup.MarginLayoutParams) this.actionsHint.getLayoutParams();
        int hintTop = (((this.logo.getTop() + (this.logo.getHeight() / 2)) - (this.actionsHint.getHeight() / 2)) - this.channelLogoKeylineOffset) + actionsHintLayoutParams.topMargin;
        if (getLayoutDirection() == 1) {
            hintLeft = this.logo.getRight() + actionsHintLayoutParams.getMarginEnd();
        } else {
            hintLeft = ((this.channelViewMainLinearLayout.getLeft() + this.logo.getLeft()) - this.actionsHint.getWidth()) - actionsHintLayoutParams.getMarginEnd();
        }
        View view3 = this.actionsHint;
        view3.layout(hintLeft, hintTop, view3.getWidth() + hintLeft, this.actionsHint.getHeight() + hintTop);
    }

    private void alignSponsoredBackgroundCenterToKeyLine() {
        int sponsoredBackgroundTop = (int) (((((float) this.logo.getTop()) + (((float) this.logo.getHeight()) / 2.0f)) - (((float) this.sponsoredChannelBackground.getHeight()) / 2.0f)) - ((float) this.channelLogoKeylineOffset));
        View view = this.sponsoredChannelBackground;
        view.layout(view.getLeft(), sponsoredBackgroundTop, this.sponsoredChannelBackground.getRight(), this.sponsoredChannelBackground.getHeight() + sponsoredBackgroundTop);
    }

    private void attachSponsoredLogoTitleAboveLogo(ViewGroup.MarginLayoutParams titleLayoutParams) {
        int titleLeft = ((this.channelViewMainLinearLayout.getLeft() + this.logo.getLeft()) + (this.logo.getMeasuredWidth() / 2)) - (this.logoTitle.getMeasuredWidth() / 2);
        int titleTop = (this.logo.getTop() - titleLayoutParams.bottomMargin) - this.logoTitle.getMeasuredHeight();
        View view = this.logoTitle;
        view.layout(titleLeft, titleTop, view.getMeasuredWidth() + titleLeft, this.logoTitle.getMeasuredHeight() + titleTop);
    }
}
