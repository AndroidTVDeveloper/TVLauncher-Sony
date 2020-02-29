package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.logs.tvlauncher.config.TvLauncherConstants;

public class NotificationsTrayItemView extends LinearLayout {
    private ImageView bigPicture;
    /* access modifiers changed from: private */
    public NotificationTrayButton dismissButton;
    private EventLogger eventLogger;
    private ViewTreeObserver.OnGlobalFocusChangeListener globalFocusChangeListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            boolean childGainedFocus = NotificationsTrayItemView.this.getFocusedChild() == newFocus;
            boolean childLostFocus = NotificationsTrayItemView.this.isFocusableChild(oldFocus);
            if (childGainedFocus && !childLostFocus) {
                NotificationsTrayItemView.this.text.setSelected(true);
                NotificationsTrayItemView.this.title.setSelected(true);
                NotificationsTrayItemView.this.setSelected(true);
                NotificationsTrayItemView.this.dismissButton.setIsNotificationSelected(true);
                NotificationsTrayItemView.this.seeMoreButton.setIsNotificationSelected(true);
            } else if (!childGainedFocus && childLostFocus) {
                NotificationsTrayItemView.this.text.setSelected(false);
                NotificationsTrayItemView.this.title.setSelected(false);
                NotificationsTrayItemView.this.setSelected(false);
                NotificationsTrayItemView.this.dismissButton.setIsNotificationSelected(false);
                NotificationsTrayItemView.this.seeMoreButton.setIsNotificationSelected(false);
            }
        }
    };
    private ImageView icon;
    private TvNotification notification;
    /* access modifiers changed from: private */
    public String notificationKey;
    private View nowPlayingIndicator;
    /* access modifiers changed from: private */
    public NotificationTrayButton seeMoreButton;
    /* access modifiers changed from: private */
    public TextView text;
    /* access modifiers changed from: private */
    public TextView title;

    public NotificationsTrayItemView(Context context) {
        super(context);
    }

    public NotificationsTrayItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationsTrayItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(this.globalFocusChangeListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalFocusChangeListener(this.globalFocusChangeListener);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.bigPicture = (ImageView) findViewById(C1167R.C1170id.big_picture);
        this.icon = (ImageView) findViewById(C1167R.C1170id.small_icon);
        this.title = (TextView) findViewById(C1167R.C1170id.notif_title);
        this.text = (TextView) findViewById(C1167R.C1170id.notif_text);
        this.dismissButton = (NotificationTrayButton) findViewById(C1167R.C1170id.tray_dismiss);
        this.seeMoreButton = (NotificationTrayButton) findViewById(C1167R.C1170id.tray_see_more);
        this.nowPlayingIndicator = findViewById(C1167R.C1170id.now_playing_bars);
        setClipToOutline(true);
    }

    /* access modifiers changed from: private */
    public boolean isFocusableChild(View v) {
        if (v != null && v.getParent() == this) {
            return true;
        }
        return false;
    }

    public void setNotification(TvNotification notif, EventLogger eventLogger2) {
        this.notification = notif;
        this.notificationKey = notif.getNotificationKey();
        this.eventLogger = eventLogger2;
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (am == null || !am.isEnabled()) {
            setFocusable(false);
        } else {
            setFocusable(true);
        }
        if (NotificationsContract.NOW_PLAYING_NOTIF_TAG.equals(notif.getTag()) && notif.getBigPicture() != null) {
            this.bigPicture.setImageBitmap(notif.getBigPicture());
            this.bigPicture.setVisibility(0);
            this.nowPlayingIndicator.setVisibility(0);
            this.icon.setVisibility(8);
        } else if (NotificationsContract.PIP_NOTIF_TAG.equals(notif.getTag())) {
            this.nowPlayingIndicator.setVisibility(8);
            Icon notifSmallIcon = notif.getSmallIcon();
            notifSmallIcon.setTint(getResources().getColor(C1167R.color.notification_icon_tint, null));
            this.icon.setImageIcon(notifSmallIcon);
            this.icon.setVisibility(0);
            this.bigPicture.setVisibility(8);
        } else {
            this.nowPlayingIndicator.setVisibility(8);
            this.bigPicture.setVisibility(8);
            this.icon.setVisibility(8);
        }
        String notifTitle = notif.getTitle();
        if (TextUtils.isEmpty(notifTitle) || !notifTitle.equals(this.title.getText())) {
            this.title.setText(notifTitle);
        }
        String notifText = notif.getText();
        if (TextUtils.isEmpty(notifText)) {
            this.text.setVisibility(8);
        } else {
            if (!notifText.equals(this.text.getText())) {
                this.text.setText(notifText);
            }
            this.text.setVisibility(0);
        }
        if (TextUtils.isEmpty(notifTitle)) {
            setContentDescription(notifText);
        } else if (!TextUtils.isEmpty(notifText)) {
            setContentDescription(String.format(getResources().getString(C1167R.string.notification_content_description_format), notifTitle, notifText));
        } else {
            setContentDescription(notifTitle);
        }
        if (notif.hasContentIntent()) {
            this.seeMoreButton.setVisibility(0);
            this.seeMoreButton.setText(notif.getContentButtonLabel());
            this.seeMoreButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    NotificationsTrayItemView.this.logClickEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_NOTIFICATION);
                    NotificationsUtils.openNotification(NotificationsTrayItemView.this.getContext(), NotificationsTrayItemView.this.notificationKey);
                }
            });
        } else {
            this.seeMoreButton.setVisibility(8);
        }
        this.dismissButton.setText(notif.getDismissButtonLabel());
        if (!notif.isDismissible() || notif.isOngoing()) {
            this.dismissButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    NotificationsTrayItemView.this.logClickEvent(TvlauncherLogEnum.TvLauncherEventCode.HIDE_NOTIFICATION);
                    NotificationsUtils.hideNotification(view.getContext(), NotificationsTrayItemView.this.notificationKey);
                }
            });
        } else {
            this.dismissButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    NotificationsTrayItemView.this.logClickEvent(TvlauncherLogEnum.TvLauncherEventCode.DISMISS_NOTIFICATION);
                    NotificationsUtils.dismissNotification(view.getContext(), NotificationsTrayItemView.this.notificationKey);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void logClickEvent(TvlauncherLogEnum.TvLauncherEventCode eventCode) {
        LogEvent event = new ClickEvent(eventCode).setVisualElementTag(TvLauncherConstants.NOTIFICATION);
        event.getNotification().setPackageName(this.notification.getPackageName()).setImportance(LogEvent.notificationImportance(this.notification.getChannel()));
        if (!TextUtils.isEmpty(this.notification.getTitle())) {
            event.getNotification().setSummary(this.notification.getTitle());
        }
        this.eventLogger.log(event);
    }
}
