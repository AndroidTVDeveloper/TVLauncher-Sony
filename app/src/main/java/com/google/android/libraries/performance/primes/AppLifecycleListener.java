package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.os.Bundle;

public interface AppLifecycleListener {

    interface OnActivityCreated extends AppLifecycleListener {
        void onActivityCreated(Activity activity, Bundle bundle);
    }

    interface OnActivityDestroyed extends AppLifecycleListener {
        void onActivityDestroyed(Activity activity);
    }

    interface OnActivityPaused extends AppLifecycleListener {
        void onActivityPaused(Activity activity);
    }

    interface OnActivityResumed extends AppLifecycleListener {
        void onActivityResumed(Activity activity);
    }

    interface OnActivitySaveInstanceState extends AppLifecycleListener {
        void onActivitySaveInstanceState(Activity activity, Bundle bundle);
    }

    interface OnActivityStarted extends AppLifecycleListener {
        void onActivityStarted(Activity activity);
    }

    interface OnActivityStopped extends AppLifecycleListener {
        void onActivityStopped(Activity activity);
    }

    interface OnAppToBackground extends AppLifecycleListener {
        void onAppToBackground(Activity activity);
    }

    interface OnAppToForeground extends AppLifecycleListener {
        void onAppToForeground(Activity activity);
    }

    interface OnTrimMemory extends AppLifecycleListener {
        void onTrimMemory(int i);
    }
}
