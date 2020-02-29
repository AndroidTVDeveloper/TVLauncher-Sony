package com.google.android.tvlauncher.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextClock;
import java.text.SimpleDateFormat;

public class ClockView extends TextClock {
    private final ContentObserver formatChangeObserver;
    private BroadcastReceiver intentReceiver;

    public ClockView(Context context) {
        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.intentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.LOCALE_CHANGED".equals(action)) {
                    ClockView.this.updatePatterns();
                }
            }
        };
        this.formatChangeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                ClockView.this.updatePatterns();
            }

            public void onChange(boolean selfChange, Uri uri) {
                ClockView.this.updatePatterns();
            }
        };
        updatePatterns();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerObserver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        getContext().registerReceiver(this.intentReceiver, filter, null, null);
        updatePatterns();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterObserver();
        getContext().unregisterReceiver(this.intentReceiver);
    }

    private void registerObserver() {
        getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, this.formatChangeObserver);
    }

    private void unregisterObserver() {
        getContext().getContentResolver().unregisterContentObserver(this.formatChangeObserver);
    }

    /* access modifiers changed from: private */
    public void updatePatterns() {
        String formatString = ((SimpleDateFormat) DateFormat.getTimeFormat(getContext())).toPattern().replace("a", "").trim();
        setFormat12Hour(formatString);
        setFormat24Hour(formatString);
    }
}
