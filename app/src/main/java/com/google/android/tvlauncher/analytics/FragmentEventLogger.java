package com.google.android.tvlauncher.analytics;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;

public class FragmentEventLogger implements EventLogger {
    private static final String TAG = "EventLogger";
    private final Fragment fragment;

    public FragmentEventLogger(Fragment fragment2) {
        this.fragment = fragment2;
    }

    public void log(LogEvent event) {
        Activity activity = this.fragment.getActivity();
        if (activity == null) {
            String valueOf = String.valueOf(event);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 60);
            sb.append("Cannot log fragment event: not attached to activity. Event: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
        } else if (!(activity instanceof EventLoggerProvider)) {
            String valueOf2 = String.valueOf(event);
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 74);
            sb2.append("Cannot log fragment event: activity is not an EventLoggerProvider. Event: ");
            sb2.append(valueOf2);
            Log.e(TAG, sb2.toString());
        } else {
            ((EventLoggerProvider) activity).getEventLogger().log(event);
        }
    }
}
