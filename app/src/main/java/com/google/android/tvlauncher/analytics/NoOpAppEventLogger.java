package com.google.android.tvlauncher.analytics;

import android.app.Activity;

public final class NoOpAppEventLogger extends AppEventLogger {
    public static void init() {
        instance = new NoOpAppEventLogger();
    }

    private NoOpAppEventLogger() {
    }

    public void log(LogEvent event) {
    }

    /* access modifiers changed from: package-private */
    public void setUsageReportingOptedIn(boolean optedIn) {
    }

    /* access modifiers changed from: package-private */
    public void setName(Activity activity, String name) {
    }
}
