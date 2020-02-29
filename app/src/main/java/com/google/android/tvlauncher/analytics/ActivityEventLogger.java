package com.google.android.tvlauncher.analytics;

import android.app.Activity;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;

class ActivityEventLogger implements EventLogger {
    private final Activity activity;
    private final String name;
    private final VisualElementTag visualElementTag;

    ActivityEventLogger(Activity activity2, String name2) {
        this(activity2, name2, null);
    }

    ActivityEventLogger(Activity activity2, String name2, VisualElementTag visualElementTag2) {
        this.activity = activity2;
        this.name = name2;
        this.visualElementTag = visualElementTag2;
    }

    public void log(LogEvent event) {
        VisualElementTag visualElementTag2 = this.visualElementTag;
        if (visualElementTag2 != null) {
            event.pushParentVisualElementTag(visualElementTag2);
        }
        AppEventLogger logger = AppEventLogger.getInstance();
        if (logger != null) {
            logger.log(event);
        }
    }

    public void onResume() {
        AppEventLogger.getInstance().setName(this.activity, this.name);
    }
}
