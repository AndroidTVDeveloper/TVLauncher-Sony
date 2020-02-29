package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.p004v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.notifications.NotificationsPanelView;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;

public class NotificationsPanelAdapter extends RecyclerView.Adapter<NotificationPanelViewHolder> implements EventLogger, NotificationsPanelView.OnFocusChangedListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotifsPanelAdapter";
    private Cursor cursor;
    private final EventLogger eventLogger;
    private Handler handler = new Handler();
    private VerticalGridView list;
    private final Runnable notifySelectionChangedRunnable = new NotificationsPanelAdapter$$Lambda$0(this);

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$NotificationsPanelAdapter() {
        notifyDataSetChanged();
    }

    public NotificationsPanelAdapter(Context context, Cursor cursor2, EventLogger logger) {
        this.cursor = cursor2;
        this.eventLogger = logger;
        setHasStableIds(true);
    }

    public Cursor getCursor() {
        return this.cursor;
    }

    public int getItemCount() {
        Cursor cursor2 = this.cursor;
        if (cursor2 != null) {
            return cursor2.getCount();
        }
        return 0;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public NotificationPanelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationPanelViewHolder(LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.notification_panel_item_view, parent, false));
    }

    public void onBindViewHolder(NotificationPanelViewHolder holder, int position) {
        if (this.cursor.moveToPosition(position)) {
            onBindViewHolder(holder, this.cursor);
            return;
        }
        StringBuilder sb = new StringBuilder(41);
        sb.append("Can't move cursor to position ");
        sb.append(position);
        throw new IllegalStateException(sb.toString());
    }

    public long getItemId(int position) {
        if (this.cursor.moveToPosition(position)) {
            return (long) this.cursor.getString(0).hashCode();
        }
        StringBuilder sb = new StringBuilder(41);
        sb.append("Can't move cursor to position ");
        sb.append(position);
        Log.wtf(TAG, sb.toString());
        return -1;
    }

    public void onBindViewHolder(NotificationPanelViewHolder viewHolder, Cursor cursor2) {
        viewHolder.setNotification(TvNotification.fromCursor(cursor2), this);
    }

    public void onFocusChanged() {
        this.handler.removeCallbacks(this.notifySelectionChangedRunnable);
        if (this.list.isComputingLayout()) {
            Log.w(TAG, "onFocusChanged: still computing layout => scheduling");
            this.handler.post(this.notifySelectionChangedRunnable);
            return;
        }
        this.notifySelectionChangedRunnable.run();
    }

    public static class NotificationPanelViewHolder extends RecyclerView.ViewHolder {
        public NotificationPanelViewHolder(View itemView) {
            super(itemView);
        }

        public void setNotification(TvNotification notification, EventLogger logger) {
            ((NotificationPanelItemView) this.itemView).setNotification(notification, logger);
        }
    }

    public void changeCursor(Cursor newCursor) {
        this.cursor = newCursor;
        notifyDataSetChanged();
        if (this.cursor != null) {
            logDataLoadedEvent();
        }
    }

    public void log(LogEvent event) {
        this.eventLogger.log(event);
    }

    private void logDataLoadedEvent() {
        int count = this.cursor.getCount();
        LogEvent event = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_NOTIFICATION_PANEL);
        TvlauncherClientLog.NotificationCollection.Builder collection = event.getNotificationCollection();
        int maxImportanceCount = 0;
        if (this.cursor.moveToFirst()) {
            do {
                int channelImportance = this.cursor.getInt(7);
                if (channelImportance == 5) {
                    maxImportanceCount++;
                }
                TvlauncherClientLog.Notification.Builder notification = TvlauncherClientLog.Notification.newBuilder();
                notification.setPackageName(this.cursor.getString(1)).setImportance(LogEvent.notificationImportance(channelImportance));
                String title = this.cursor.getString(2);
                if (!TextUtils.isEmpty(title)) {
                    notification.setSummary(title);
                }
                collection.addNotifications(notification);
            } while (this.cursor.moveToNext());
        }
        collection.setCount(count).setMaxPriorityCount(maxImportanceCount);
        this.eventLogger.log(event);
    }

    /* access modifiers changed from: protected */
    public void setList(VerticalGridView list2) {
        this.list = list2;
        this.list.setItemAnimator(new NotificationPanelItemAnimator());
    }
}
