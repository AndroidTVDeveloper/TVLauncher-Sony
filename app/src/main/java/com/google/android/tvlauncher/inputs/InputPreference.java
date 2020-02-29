package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.Util;
import java.util.Locale;

class InputPreference extends Preference {
    private final ColorStateList activeIconBackgroundColorStateList;
    private final int activeIconSelectedTint;
    private final int activeIconTint;
    private Uri activeIconUri;
    private boolean applyStandardStyleToInputStateIcons;
    private final String connectedContentDescription;
    private final ColorStateList connectedIconBackgroundColorStateList;
    private final int connectedIconSelectedTint;
    private final int connectedIconTint;
    private final ColorStateList connectedSelectedIconBackgroundColorStateList;
    private final int defaultLabelTextColor;
    private final int defaultParentLabelTextColor;
    private final String disconnectedContentDescription;
    private final ColorStateList disconnectedIconBackgroundColorStateList;
    private final int disconnectedIconTint;
    private final int disconnectedLabelColor;
    private final ColorStateList disconnectedSelectedIconBackgroundColorStateList;
    private final Drawable iconBackground;
    private Uri iconUri;
    private ImageView iconView;
    private final RequestOptions imageRequestOptions;
    private final String inputNameAndStatusFormat;
    private boolean isActive;
    private TextView labelView;
    private final int modifiedIconPadding;
    private final OnPreferenceFocusedListener onPreferenceFocusedListener;
    private final View.OnFocusChangeListener onViewFocusChangeListener = new InputPreference$$Lambda$0(this);
    private TextView parentLabelView;
    private Uri selectedActiveIconUri;
    private final Drawable selectedIconBackground;
    private Uri selectedIconUri;
    private final String standbyContentDescription;
    private int state;

    interface OnPreferenceFocusedListener {
        void onPreferenceFocused(String str);
    }

    InputPreference(Context context, OnPreferenceFocusedListener onPreferenceFocusedListener2) {
        super(context);
        setLayoutResource(C1167R.layout.input_preference);
        this.onPreferenceFocusedListener = onPreferenceFocusedListener2;
        this.defaultLabelTextColor = context.getColor(C1167R.color.input_label_default_text_color);
        this.defaultParentLabelTextColor = context.getColor(C1167R.color.input_parent_label_default_text_color);
        this.disconnectedLabelColor = context.getColor(C1167R.color.input_label_disconnected_text_color);
        this.connectedContentDescription = context.getString(C1167R.string.input_state_connected);
        this.standbyContentDescription = context.getString(C1167R.string.input_state_standby);
        this.disconnectedContentDescription = context.getString(C1167R.string.input_state_disconnected);
        this.inputNameAndStatusFormat = context.getString(C1167R.string.input_name_and_input_status);
        int iconMaxSize = context.getResources().getDimensionPixelSize(C1167R.dimen.input_icon_view_size);
        this.imageRequestOptions = (RequestOptions) ((RequestOptions) new RequestOptions().override(iconMaxSize, iconMaxSize)).centerInside();
        this.modifiedIconPadding = context.getResources().getDimensionPixelOffset(C1167R.dimen.input_modified_icon_view_padding);
        this.activeIconTint = context.getColor(C1167R.color.input_icon_active_tint);
        this.activeIconSelectedTint = context.getColor(C1167R.color.input_icon_active_selected_tint);
        this.activeIconBackgroundColorStateList = context.getColorStateList(C1167R.color.input_icon_background_active_tint);
        this.connectedIconTint = context.getColor(C1167R.color.input_icon_tint);
        this.connectedIconSelectedTint = context.getColor(C1167R.color.input_icon_selected_tint);
        this.connectedIconBackgroundColorStateList = context.getColorStateList(C1167R.color.input_icon_background_tint);
        this.connectedSelectedIconBackgroundColorStateList = context.getColorStateList(C1167R.color.input_icon_background_selected_tint);
        this.disconnectedIconTint = context.getColor(C1167R.color.input_icon_disconnected_tint);
        this.disconnectedIconBackgroundColorStateList = context.getColorStateList(C1167R.color.input_icon_background_disconnected_tint);
        this.disconnectedSelectedIconBackgroundColorStateList = context.getColorStateList(C1167R.color.input_icon_background_disconnected_selected_tint);
        this.iconBackground = context.getDrawable(C1167R.C1168drawable.filled_circle_input_background_black);
        this.selectedIconBackground = context.getDrawable(C1167R.C1168drawable.hollow_circle_input_background_black);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (this.iconView == null) {
            this.iconView = (ImageView) holder.findViewById(16908294);
        }
        if (this.labelView == null) {
            this.labelView = (TextView) holder.findViewById(16908310);
        }
        if (this.parentLabelView == null) {
            this.parentLabelView = (TextView) holder.findViewById(16908304);
        }
        bridge$lambda$0$InputPreference(holder.itemView, holder.itemView.hasFocus());
        holder.itemView.setOnFocusChangeListener(this.onViewFocusChangeListener);
    }

    /* access modifiers changed from: package-private */
    public void setState(int state2) {
        this.state = state2;
    }

    /* access modifiers changed from: package-private */
    public void setIsActive(boolean isActive2) {
        this.isActive = isActive2;
    }

    /* access modifiers changed from: package-private */
    public void setApplyStandardStyleToInputStateIcons(boolean applyStandardStyleToInputStateIcons2) {
        this.applyStandardStyleToInputStateIcons = applyStandardStyleToInputStateIcons2;
    }

    /* access modifiers changed from: package-private */
    public void setIconUri(Uri iconUri2) {
        this.iconUri = iconUri2;
    }

    /* access modifiers changed from: package-private */
    public void setSelectedIconUri(Uri selectedIconUri2) {
        this.selectedIconUri = selectedIconUri2;
    }

    /* access modifiers changed from: package-private */
    public void setActiveIconUri(Uri activeIconUri2) {
        this.activeIconUri = activeIconUri2;
    }

    /* access modifiers changed from: package-private */
    public void setSelectedActiveIconUri(Uri selectedActiveIconUri2) {
        this.selectedActiveIconUri = selectedActiveIconUri2;
    }

    /* access modifiers changed from: private */
    /* renamed from: updateInputUi */
    public void bridge$lambda$0$InputPreference(View view, boolean hasFocus) {
        OnPreferenceFocusedListener onPreferenceFocusedListener2;
        Uri iconUri2;
        ColorStateList colorStateList;
        ColorStateList colorStateList2;
        if (this.isActive) {
            this.labelView.setTextColor(this.defaultLabelTextColor);
            this.parentLabelView.setTextColor(this.defaultParentLabelTextColor);
        } else {
            int i = this.state;
            if (i == 0) {
                this.labelView.setTextColor(this.defaultLabelTextColor);
                this.parentLabelView.setTextColor(this.defaultParentLabelTextColor);
            } else if (i == 1 || i == 2) {
                this.labelView.setTextColor(this.disconnectedLabelColor);
                this.parentLabelView.setTextColor(this.disconnectedLabelColor);
            }
        }
        if (this.applyStandardStyleToInputStateIcons) {
            ImageView imageView = this.iconView;
            int i2 = this.modifiedIconPadding;
            imageView.setPadding(i2, i2, i2, i2);
            if (this.isActive) {
                this.iconView.setColorFilter(hasFocus ? this.activeIconSelectedTint : this.activeIconTint, PorterDuff.Mode.SRC_IN);
                this.iconView.setBackground(this.iconBackground);
                this.iconView.setBackgroundTintList(this.activeIconBackgroundColorStateList);
            } else {
                int i3 = this.state;
                if (i3 == 0 || i3 == 1) {
                    this.iconView.setColorFilter(hasFocus ? this.connectedIconSelectedTint : this.connectedIconTint, PorterDuff.Mode.SRC_IN);
                    this.iconView.setBackground(hasFocus ? this.selectedIconBackground : this.iconBackground);
                    ImageView imageView2 = this.iconView;
                    if (hasFocus) {
                        colorStateList = this.connectedSelectedIconBackgroundColorStateList;
                    } else {
                        colorStateList = this.connectedIconBackgroundColorStateList;
                    }
                    imageView2.setBackgroundTintList(colorStateList);
                } else if (i3 == 2) {
                    this.iconView.setColorFilter(this.disconnectedIconTint, PorterDuff.Mode.SRC_IN);
                    this.iconView.setBackground(hasFocus ? this.selectedIconBackground : this.iconBackground);
                    ImageView imageView3 = this.iconView;
                    if (hasFocus) {
                        colorStateList2 = this.disconnectedSelectedIconBackgroundColorStateList;
                    } else {
                        colorStateList2 = this.disconnectedIconBackgroundColorStateList;
                    }
                    imageView3.setBackgroundTintList(colorStateList2);
                }
            }
        } else {
            this.iconView.setPadding(0, 0, 0, 0);
        }
        String status = null;
        if (this.isActive) {
            status = "Active";
        } else {
            int i4 = this.state;
            if (i4 == 0) {
                status = this.connectedContentDescription;
            } else if (i4 == 1) {
                status = this.standbyContentDescription;
            } else if (i4 == 2) {
                status = this.disconnectedContentDescription;
            }
        }
        this.labelView.setContentDescription(String.format(Locale.getDefault(), this.inputNameAndStatusFormat, this.labelView.getText(), status));
        if (this.iconUri != null) {
            this.iconView.setVisibility(0);
            if (this.isActive) {
                iconUri2 = hasFocus ? this.selectedActiveIconUri : this.activeIconUri;
            } else {
                iconUri2 = hasFocus ? this.selectedIconUri : this.iconUri;
            }
            Context context = view.getContext();
            if (Util.isValidContextForGlide(context)) {
                Glide.with(context).load(iconUri2).apply((BaseRequestOptions<?>) this.imageRequestOptions).into(this.iconView);
            }
        }
        if (hasFocus && (onPreferenceFocusedListener2 = this.onPreferenceFocusedListener) != null) {
            onPreferenceFocusedListener2.onPreferenceFocused(getKey());
        }
    }
}
