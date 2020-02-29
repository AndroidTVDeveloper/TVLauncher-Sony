package com.google.android.tvlauncher.home;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.home.view.ConfigureChannelsRowView;
import com.google.android.tvlauncher.util.ScaleFocusHandler;
import com.google.android.tvlauncher.util.Util;

class ConfigureChannelsRowController implements HomeRow {
    private final View button;
    private final int buttonSelectedBackgroundColor;
    private final float buttonSelectedScale;
    private final int buttonUnselectedBackgroundColor;
    private final int channelActionsStartMargin;
    private final int defaultBelowAppsRowTopMargin;
    private final int defaultStartMargin;
    private final int defaultTopMargin;
    private final int moveChannelStartMargin;
    private OnHomeRowSelectedListener onHomeRowSelectedListener;
    private final ConfigureChannelsRowView view;
    private final int zoomedOutStartMargin;
    private final int zoomedOutTopMargin;

    ConfigureChannelsRowController(ConfigureChannelsRowView v) {
        this.view = v;
        this.button = v.getButton();
        this.button.setOnClickListener(ConfigureChannelsRowController$$Lambda$0.$instance);
        Resources resources = v.getContext().getResources();
        this.buttonSelectedScale = resources.getFraction(C1167R.fraction.home_configure_channels_button_focused_scale, 1, 1);
        View.OnFocusChangeListener buttonOnFocusChangeListener = new ConfigureChannelsRowController$$Lambda$1(this);
        if (Util.areHomeScreenAnimationsEnabled(this.view.getContext())) {
            this.button.setOnFocusChangeListener(buttonOnFocusChangeListener);
        } else {
            ScaleFocusHandler focusHandler = new ScaleFocusHandler(resources.getInteger(C1167R.integer.home_configure_channels_button_focused_animation_duration_ms), this.buttonSelectedScale, 0.0f, 1);
            focusHandler.setView(this.button);
            focusHandler.setOnFocusChangeListener(buttonOnFocusChangeListener);
        }
        this.defaultTopMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_row_margin_top);
        this.defaultBelowAppsRowTopMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_row_below_apps_row_margin_top);
        this.zoomedOutTopMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_row_zoomed_out_margin_top);
        this.defaultStartMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_button_margin_default);
        this.zoomedOutStartMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_button_margin_zoomed_out);
        this.channelActionsStartMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_button_margin_channel_actions);
        this.moveChannelStartMargin = resources.getDimensionPixelSize(C1167R.dimen.home_configure_channels_button_margin_move_channel);
        this.buttonSelectedBackgroundColor = v.getContext().getColor(C1167R.color.home_configure_channels_button_focused_background_color);
        this.buttonUnselectedBackgroundColor = v.getContext().getColor(C1167R.color.home_configure_channels_button_unfocused_background_color);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$1$ConfigureChannelsRowController(View view2, boolean hasFocus) {
        if (hasFocus) {
            this.onHomeRowSelectedListener.onHomeRowSelected(this);
        }
    }

    public void setOnHomeStateChangeListener(OnHomeStateChangeListener listener) {
    }

    public void setOnHomeRowRemovedListener(OnHomeRowRemovedListener listener) {
    }

    public void setHomeIsFastScrolling(boolean homeIsFastScrolling) {
    }

    public void setOnHomeRowSelectedListener(OnHomeRowSelectedListener listener) {
        this.onHomeRowSelectedListener = listener;
    }

    public View getView() {
        return this.view;
    }

    /* access modifiers changed from: package-private */
    public void bind(int homeState, boolean selected, boolean isBelowAppsRow) {
        int marginStart = this.defaultStartMargin;
        int marginTop = this.defaultTopMargin;
        if (homeState == 0) {
            marginStart = this.defaultStartMargin;
            marginTop = isBelowAppsRow ? this.defaultBelowAppsRowTopMargin : this.defaultTopMargin;
        } else if (homeState == 1) {
            marginStart = this.zoomedOutStartMargin;
            marginTop = this.zoomedOutTopMargin;
        } else if (homeState == 2) {
            marginStart = this.channelActionsStartMargin;
            marginTop = this.zoomedOutTopMargin;
        } else if (homeState == 3) {
            marginStart = this.moveChannelStartMargin;
            marginTop = this.zoomedOutTopMargin;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.view.getLayoutParams();
        lp.setMarginStart(marginStart);
        this.view.setLayoutParams(lp);
        ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) this.button.getLayoutParams();
        lp2.topMargin = marginTop;
        this.button.setLayoutParams(lp2);
        if (selected) {
            this.button.setBackgroundColor(this.buttonSelectedBackgroundColor);
            this.button.setScaleX(this.buttonSelectedScale);
            this.button.setScaleY(this.buttonSelectedScale);
        } else {
            this.button.setBackgroundColor(this.buttonUnselectedBackgroundColor);
            this.button.setScaleX(1.0f);
            this.button.setScaleY(1.0f);
        }
        int width = this.button.getLayoutParams().width;
        int height = this.button.getLayoutParams().height;
        if (width <= 0 || height <= 0) {
            width = this.button.getWidth();
            height = this.button.getHeight();
        }
        if (width > 0 && height > 0) {
            if (this.button.getLayoutDirection() == 1) {
                this.button.setPivotX((float) width);
            } else {
                this.button.setPivotX(0.0f);
            }
            this.button.setPivotY((float) height);
        }
        this.view.setDescriptionVisibility(selected ? 0 : 4);
    }
}
