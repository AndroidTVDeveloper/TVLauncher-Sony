package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.google.android.tvlauncher.C1167R;

public class NotificationDismissButton extends ImageButton {
    private int buttonFocusedIconColor;
    private Drawable icon;
    private int notificationFocusedColor;
    private int notificationUnfocusedColor;

    public NotificationDismissButton(Context context) {
        super(context, null);
    }

    public NotificationDismissButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        this.notificationFocusedColor = res.getColor(C1167R.color.reference_white_40, null);
        this.buttonFocusedIconColor = res.getColor(C1167R.color.notification_panel_background, null);
        this.notificationUnfocusedColor = res.getColor(C1167R.color.reference_white_20, null);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.icon = getDrawable();
        this.icon.mutate();
    }

    public void bind(boolean notifSelected) {
        if (hasFocus()) {
            this.icon.setTint(this.buttonFocusedIconColor);
        } else {
            this.icon.setTint(notifSelected ? this.notificationFocusedColor : this.notificationUnfocusedColor);
        }
    }
}
