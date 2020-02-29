package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.database.Cursor;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogEventParameters;

public class NotificationsTrayAdapter extends RecyclerView.Adapter<NotificationsTrayViewHolder> {
    private static final boolean DEBUG = false;
    public static final String TAG = "NotifsTrayAdapter";
    private final Context context;
    private Cursor cursor;
    private final EventLogger eventLogger;

    public NotificationsTrayAdapter(Context context2, EventLogger eventLogger2, Cursor cursor2) {
        this.context = context2;
        this.cursor = cursor2;
        this.eventLogger = eventLogger2;
        setHasStableIds(true);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public NotificationsTrayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationsTrayViewHolder(LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.notification_tray_item, parent, false));
    }

    public void onBindViewHolder(NotificationsTrayViewHolder holder, int position) {
        if (this.cursor.moveToPosition(position)) {
            holder.setNotification(TvNotification.fromCursor(this.cursor), this.eventLogger);
            return;
        }
        StringBuilder sb = new StringBuilder(43);
        sb.append("Index out of bounds for cursor: ");
        sb.append(position);
        throw new IllegalStateException(sb.toString());
    }

    public int getItemCount() {
        Cursor cursor2 = this.cursor;
        if (cursor2 == null || cursor2.isClosed()) {
            return 0;
        }
        return this.cursor.getCount();
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

    public static class NotificationsTrayViewHolder extends RecyclerView.ViewHolder {
        public NotificationsTrayViewHolder(View itemView) {
            super(itemView);
        }

        public void setNotification(TvNotification notification, EventLogger eventLogger) {
            ((NotificationsTrayItemView) this.itemView).setNotification(notification, eventLogger);
        }
    }

    public void changeCursor(Cursor newCursor) {
        this.cursor = newCursor;
        notifyDataSetChanged();
        LogEvent event = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.TRAY_NOTIFICATION_COUNT);
        Cursor cursor2 = this.cursor;
        if (!(cursor2 == null || cursor2.getCount() == 0)) {
            event.getNotificationCollection().setMaxPriorityCount(this.cursor.getCount());
        }
        this.eventLogger.log(event);
    }
}
