package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import com.google.android.tvlauncher.C1167R;

public class NotificationTrayButton extends Button {
    private static final int[] STATE_NOTIFICATION_SELECTED = {C1167R.attr.state_notification_selected};
    private boolean isNotificationSelected;

    public NotificationTrayButton(Context context) {
        super(context);
    }

    public NotificationTrayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationTrayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        if (!this.isNotificationSelected) {
            return super.onCreateDrawableState(extraSpace);
        }
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        mergeDrawableStates(drawableState, STATE_NOTIFICATION_SELECTED);
        return drawableState;
    }

    public void setIsNotificationSelected(boolean isNotificationSelected2) {
        this.isNotificationSelected = isNotificationSelected2;
        refreshDrawableState();
    }
}
