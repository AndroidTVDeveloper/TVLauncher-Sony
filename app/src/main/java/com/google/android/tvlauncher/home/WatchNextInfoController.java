package com.google.android.tvlauncher.home;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.home.util.ProgramSettings;
import com.google.android.tvlauncher.home.util.ProgramStateUtil;
import com.google.android.tvlauncher.home.util.ProgramUtil;
import com.google.android.tvlauncher.home.view.WatchNextInfoView;
import com.google.android.tvlauncher.util.Util;

class WatchNextInfoController {
    private View container = this.view.getContainer();
    private final float contentContainerFocusedScale;
    private int defaultContainerMarginHorizontal;
    private int defaultIconSize;
    private int defaultMessageMarginTop;
    private int defaultMessageWidth;
    private int defaultTitleMarginStart;
    private float defaultTitleTextSize;
    private int defaultTitleWidth;
    private final int dimmedColor;
    private final int focusedColor;
    private ImageView icon = this.view.getIcon();
    private final float iconDimmedAlpha;
    private final float iconFocusedAlpha;
    private final float iconUnfocusedAlpha;
    private boolean isItemSelected;
    private TextView message = this.view.getMessage();
    private final ProgramSettings programSettings;
    private int programState;
    private int selectedChannelContainerVisualOffset;
    private int selectedChannelIconSize;
    private int selectedChannelInfoViewIgnoredWidthChangeThreshold;
    private int selectedChannelMessageMarginTop;
    private int selectedChannelMessageWidth;
    private int selectedChannelTitleMarginStart;
    private float selectedChannelTitleTextSize;
    private int selectedChannelTitleWidth;
    private TextView title = this.view.getTitle();
    private final int unfocusedColor;
    private WatchNextInfoView view;
    private int zoomedOutIconSize;
    private int zoomedOutMessageMarginTop;
    private int zoomedOutMessageWidth;
    private int zoomedOutTitleMarginStart;
    private float zoomedOutTitleTextSize;
    private int zoomedOutTitleWidth;

    WatchNextInfoController(WatchNextInfoView view2, ProgramSettings programSettings2) {
        this.view = view2;
        this.programSettings = programSettings2;
        Context context = view2.getContext();
        Resources resources = context.getResources();
        this.defaultContainerMarginHorizontal = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_container_default_margin_horizontal);
        this.defaultIconSize = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_default_icon_size);
        this.defaultTitleMarginStart = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_default_title_margin_start);
        this.defaultTitleWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_default_title_width);
        this.defaultTitleTextSize = resources.getDimension(C1167R.dimen.watch_next_info_card_default_title_text_size);
        this.defaultMessageMarginTop = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_default_message_margin_top);
        this.defaultMessageWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_default_message_width);
        this.selectedChannelInfoViewIgnoredWidthChangeThreshold = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_container_width_change_ignored_threshold);
        this.selectedChannelContainerVisualOffset = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_container_selected_margin_offset);
        this.selectedChannelIconSize = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_selected_icon_size);
        this.selectedChannelTitleMarginStart = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_selected_title_margin_start);
        this.selectedChannelTitleWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_selected_title_width);
        this.selectedChannelTitleTextSize = resources.getDimension(C1167R.dimen.watch_next_info_card_selected_title_text_size);
        this.selectedChannelMessageMarginTop = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_selected_message_margin_top);
        this.selectedChannelMessageWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_selected_message_width);
        this.zoomedOutIconSize = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_zoomed_out_icon_size);
        this.zoomedOutTitleMarginStart = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_zoomed_out_title_margin_start);
        this.zoomedOutTitleWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_zoomed_out_title_width);
        this.zoomedOutTitleTextSize = resources.getDimension(C1167R.dimen.watch_next_info_card_zoomed_out_title_text_size);
        this.zoomedOutMessageMarginTop = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_zoomed_out_message_margin_top);
        this.zoomedOutMessageWidth = resources.getDimensionPixelSize(C1167R.dimen.watch_next_info_card_zoomed_out_message_width);
        this.contentContainerFocusedScale = resources.getFraction(C1167R.fraction.watch_next_info_card_content_container_focused_scale, 1, 1);
        this.focusedColor = context.getColor(C1167R.color.watch_next_info_card_focused_color);
        this.unfocusedColor = context.getColor(C1167R.color.watch_next_info_card_unfocused_color);
        this.dimmedColor = context.getColor(C1167R.color.watch_next_info_card_dimmed_color);
        this.iconFocusedAlpha = Util.getFloat(context.getResources(), C1167R.dimen.watch_next_info_icon_focused_alpha);
        this.iconUnfocusedAlpha = Util.getFloat(context.getResources(), C1167R.dimen.watch_next_info_icon_unfocused_alpha);
        this.iconDimmedAlpha = Util.getFloat(context.getResources(), C1167R.dimen.watch_next_info_icon_dimmed_alpha);
    }

    /* access modifiers changed from: package-private */
    public boolean isSelected() {
        return this.isItemSelected;
    }

    /* access modifiers changed from: package-private */
    public void bindState(int programState2) {
        this.programState = programState2;
        ProgramUtil.updateSize(this.view, this.programState, 1.0d, this.programSettings);
        ViewGroup.MarginLayoutParams itemViewLayoutParams = (ViewGroup.MarginLayoutParams) this.view.getLayoutParams();
        if (ProgramStateUtil.isZoomedOutState(this.programState)) {
            itemViewLayoutParams.width = 0;
            itemViewLayoutParams.setMarginEnd(0);
        } else {
            itemViewLayoutParams.width = -2;
        }
        this.view.setLayoutParams(itemViewLayoutParams);
        ViewGroup.MarginLayoutParams iconLayoutParams = (ViewGroup.MarginLayoutParams) this.icon.getLayoutParams();
        ViewGroup.MarginLayoutParams titleLayoutParams = (ViewGroup.MarginLayoutParams) this.title.getLayoutParams();
        ViewGroup.MarginLayoutParams messageLayoutParams = (ViewGroup.MarginLayoutParams) this.message.getLayoutParams();
        int messageViewVisualWidth = 0;
        int i = this.programState;
        switch (i) {
            case 0:
            case 3:
            case 4:
            case 12:
                iconLayoutParams.width = this.selectedChannelIconSize;
                titleLayoutParams.setMarginStart(this.selectedChannelTitleMarginStart);
                titleLayoutParams.width = this.selectedChannelTitleWidth;
                this.title.setTextSize(0, this.selectedChannelTitleTextSize);
                messageLayoutParams.topMargin = this.selectedChannelMessageMarginTop;
                messageViewVisualWidth = this.selectedChannelMessageWidth;
                break;
            case 1:
            case 2:
                iconLayoutParams.width = this.defaultIconSize;
                titleLayoutParams.setMarginStart(this.defaultTitleMarginStart);
                titleLayoutParams.width = this.defaultTitleWidth;
                this.title.setTextSize(0, this.defaultTitleTextSize);
                messageLayoutParams.topMargin = this.defaultMessageMarginTop;
                messageViewVisualWidth = this.defaultMessageWidth;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 10:
                iconLayoutParams.width = this.zoomedOutIconSize;
                titleLayoutParams.setMarginStart(this.zoomedOutTitleMarginStart);
                titleLayoutParams.width = this.zoomedOutTitleWidth;
                this.title.setTextSize(0, this.zoomedOutTitleTextSize);
                messageLayoutParams.topMargin = this.zoomedOutMessageMarginTop;
                messageViewVisualWidth = this.zoomedOutMessageWidth;
                break;
            case 9:
            case 11:
                String valueOf = String.valueOf(ProgramStateUtil.stateToString(i));
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported Watch Next program state: ".concat(valueOf) : new String("Unsupported Watch Next program state: "));
        }
        iconLayoutParams.height = iconLayoutParams.width;
        this.icon.setLayoutParams(iconLayoutParams);
        this.title.setLayoutParams(titleLayoutParams);
        this.message.setLayoutParams(messageLayoutParams);
        float ratio = (((float) messageViewVisualWidth) * 1.0f) / ((float) this.selectedChannelMessageWidth);
        this.message.setScaleX(ratio);
        this.message.setScaleY(ratio);
        if (Util.isRtl(this.view.getContext())) {
            this.message.setPivotX((float) this.selectedChannelMessageWidth);
            this.title.setPivotX((float) titleLayoutParams.width);
        } else {
            this.message.setPivotX(0.0f);
            this.title.setPivotX(0.0f);
        }
        this.message.setPivotY(0.0f);
        this.title.setPivotY(0.0f);
    }

    /* access modifiers changed from: package-private */
    public void updateFocusedState(float unfocusedSelectedStartOffset) {
        this.isItemSelected = this.programState == 3 && this.view.isFocused();
        ViewGroup.MarginLayoutParams containerLayoutParams = (ViewGroup.MarginLayoutParams) this.container.getLayoutParams();
        int i = this.programState;
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 12:
                containerLayoutParams.setMarginStart(this.defaultContainerMarginHorizontal);
                containerLayoutParams.setMarginEnd(this.defaultContainerMarginHorizontal);
                this.container.setScaleX(1.0f);
                this.container.setScaleY(1.0f);
                this.container.setAlpha(1.0f);
                break;
            case 3:
            case 4:
                float unfocusedMarginStart = ((float) this.selectedChannelContainerVisualOffset) + unfocusedSelectedStartOffset;
                if (this.isItemSelected) {
                    containerLayoutParams.setMarginStart(this.defaultContainerMarginHorizontal);
                    float marginEndOffset = ((float) this.selectedChannelMessageWidth) * (this.contentContainerFocusedScale - 1.0f);
                    float totalWidthChange = (((float) this.defaultContainerMarginHorizontal) - unfocusedMarginStart) + marginEndOffset;
                    if (Math.abs(totalWidthChange) < ((float) this.selectedChannelInfoViewIgnoredWidthChangeThreshold)) {
                        marginEndOffset -= totalWidthChange;
                    }
                    containerLayoutParams.setMarginEnd((int) (((float) this.defaultContainerMarginHorizontal) + marginEndOffset));
                    this.container.setScaleX(this.contentContainerFocusedScale);
                    this.container.setScaleY(this.contentContainerFocusedScale);
                } else {
                    containerLayoutParams.setMarginStart((int) unfocusedMarginStart);
                    containerLayoutParams.setMarginEnd(this.defaultContainerMarginHorizontal);
                    this.container.setScaleX(1.0f);
                    this.container.setScaleY(1.0f);
                }
                this.container.setAlpha(1.0f);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 10:
                containerLayoutParams.setMarginStart(0);
                containerLayoutParams.setMarginEnd(0);
                this.container.setScaleX(1.0f);
                this.container.setScaleY(1.0f);
                this.container.setAlpha(0.0f);
                break;
            case 9:
            case 11:
                String valueOf = String.valueOf(ProgramStateUtil.stateToString(i));
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported Watch Next program state: ".concat(valueOf) : new String("Unsupported Watch Next program state: "));
        }
        this.container.setLayoutParams(containerLayoutParams);
        int i2 = this.programState;
        if (i2 == 0 || i2 == 12 || i2 == 1 || i2 == 7) {
            this.icon.setAlpha(this.iconDimmedAlpha);
            this.title.setTextColor(this.dimmedColor);
            this.message.setTextColor(this.dimmedColor);
        } else if (this.isItemSelected) {
            this.icon.setAlpha(this.iconFocusedAlpha);
            this.title.setTextColor(this.focusedColor);
            this.message.setTextColor(this.focusedColor);
        } else {
            this.icon.setAlpha(this.iconUnfocusedAlpha);
            this.title.setTextColor(this.unfocusedColor);
            this.message.setTextColor(this.unfocusedColor);
        }
    }
}
