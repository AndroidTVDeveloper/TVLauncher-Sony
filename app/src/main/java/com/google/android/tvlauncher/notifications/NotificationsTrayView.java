package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.leanback.widget.HorizontalGridView;
import com.google.android.tvlauncher.C1167R;

public class NotificationsTrayView extends FrameLayout {
    private HorizontalGridView notificationsRow;

    public NotificationsTrayView(Context context) {
        super(context);
    }

    public NotificationsTrayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.notificationsRow = (HorizontalGridView) findViewById(C1167R.C1170id.notifications_row);
        this.notificationsRow.setWindowAlignment(0);
        this.notificationsRow.setWindowAlignmentOffsetPercent(0.0f);
        this.notificationsRow.setWindowAlignmentOffset(getContext().getResources().getDimensionPixelSize(C1167R.dimen.notifications_list_padding_start));
        this.notificationsRow.setItemAlignmentOffsetPercent(0.0f);
    }

    public void updateVisibility() {
        int i;
        if (this.notificationsRow.getAdapter() == null || this.notificationsRow.getAdapter().getItemCount() <= 0) {
            i = 8;
        } else {
            i = 0;
        }
        setVisibility(i);
    }

    public void setTrayAdapter(NotificationsTrayAdapter adapter) {
        this.notificationsRow.setAdapter(adapter);
        updateVisibility();
    }

    public NotificationsTrayAdapter getTrayAdapter() {
        HorizontalGridView horizontalGridView = this.notificationsRow;
        if (horizontalGridView != null) {
            return (NotificationsTrayAdapter) horizontalGridView.getAdapter();
        }
        return null;
    }
}
