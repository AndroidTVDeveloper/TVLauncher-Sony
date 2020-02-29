package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogEventParameters;
import com.google.logs.tvlauncher.config.TvLauncherConstants;

public class NotificationsPanelController {
    public static final String NOTIFS_SEEN = "notifs_seen";
    public static final String NOTIF_PANEL_SEEN_STATE = "notif_panel_seen_state";
    private Context context;
    private final EventLogger eventLogger;
    private int notifCount = 0;
    private NotificationsPanelButtonView panelButtonView;
    private boolean seen;

    public NotificationsPanelController(Context context2, EventLogger eventLogger2) {
        this.eventLogger = eventLogger2;
        this.context = context2;
        this.seen = this.context.getSharedPreferences(NOTIF_PANEL_SEEN_STATE, 0).getBoolean(NOTIFS_SEEN, true);
    }

    public void setView(NotificationsPanelButtonView view) {
        if (view != null) {
            this.panelButtonView = view;
            this.panelButtonView.setOnClickListener(new NotificationsPanelController$$Lambda$0(this));
            updateView();
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$setView$0$NotificationsPanelController(View v) {
        logClick();
        NotificationsUtils.openNotificationPanel(this.context);
        this.panelButtonView.setSeenState(true);
    }

    public void updateNotificationsCount(Cursor data) {
        if (data != null && data.moveToFirst()) {
            data.moveToFirst();
            int oldCount = this.notifCount;
            this.notifCount = data.getInt(data.getColumnIndex(NotificationsContract.COLUMN_COUNT));
            boolean oldSeenState = this.seen;
            this.seen = oldCount >= this.notifCount;
            if (oldSeenState != this.seen) {
                storeSeenState();
            }
            LogEvent event = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.NOTIFICATION_INDICATOR_TOTAL);
            event.getNotificationCollection().setCount(this.notifCount).setHasNewNotifications(true ^ this.seen);
            this.eventLogger.log(event);
        }
        updateView();
    }

    public void updateView() {
        NotificationsPanelButtonView notificationsPanelButtonView = this.panelButtonView;
        if (notificationsPanelButtonView == null) {
            return;
        }
        if (this.notifCount == 0) {
            notificationsPanelButtonView.setVisibility(8);
            return;
        }
        notificationsPanelButtonView.setVisibility(0);
        this.panelButtonView.setCount(this.notifCount);
        this.panelButtonView.setSeenState(this.seen);
    }

    private void storeSeenState() {
        this.context.getSharedPreferences(NOTIF_PANEL_SEEN_STATE, 0).edit().putBoolean(NOTIFS_SEEN, this.seen).apply();
    }

    private void logClick() {
        LogEvent event = new ClickEvent(null).setVisualElementTag(TvLauncherConstants.NOTIFICATIONS_INDICATOR_BUTTON);
        event.getNotificationCollection().setCount(this.notifCount).setHasNewNotifications(!this.seen);
        this.eventLogger.log(event);
    }
}
