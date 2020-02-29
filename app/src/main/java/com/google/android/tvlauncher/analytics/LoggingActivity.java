package com.google.android.tvlauncher.analytics;

import android.app.Activity;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;

public abstract class LoggingActivity extends Activity implements EventLoggerProvider {
    protected final ActivityEventLogger activityEventLogger;

    public LoggingActivity(String name) {
        this.activityEventLogger = new ActivityEventLogger(this, name);
    }

    public LoggingActivity(String name, VisualElementTag visualElementTag) {
        this.activityEventLogger = new ActivityEventLogger(this, name, visualElementTag);
    }

    public EventLogger getEventLogger() {
        return this.activityEventLogger;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.activityEventLogger.onResume();
    }
}
