package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.metriccapture.ProcessStats;
import com.google.android.libraries.stitch.util.Preconditions;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class AppLifecycleTracker {
    private static final String TAG = "AppLifecycleTracker";
    private final Callbacks callbacks = new Callbacks();

    public enum AppForegroundState {
        UNKNOWN,
        FOREGROUND,
        BACKGROUND
    }

    public void attachToApp(Application application) {
        application.registerActivityLifecycleCallbacks(this.callbacks);
        application.registerComponentCallbacks(this.callbacks);
    }

    public void detachFromApp(Application application) {
        application.unregisterActivityLifecycleCallbacks(this.callbacks);
        application.unregisterComponentCallbacks(this.callbacks);
    }

    /* access modifiers changed from: package-private */
    public AppForegroundState getAppForegroundState() {
        Boolean localForegroundState = this.callbacks.lastForegroundState;
        if (localForegroundState == null) {
            return AppForegroundState.UNKNOWN;
        }
        return localForegroundState.booleanValue() ? AppForegroundState.FOREGROUND : AppForegroundState.BACKGROUND;
    }

    /* access modifiers changed from: package-private */
    public String getForegroundActivityName() {
        return this.callbacks.nameOfForegroundActivity;
    }

    /* access modifiers changed from: package-private */
    public int getActivityCreatedCount() {
        return this.callbacks.createdCount.get();
    }

    /* access modifiers changed from: package-private */
    public int getActivityResumedCount() {
        return this.callbacks.resumedCount.get();
    }

    /* access modifiers changed from: package-private */
    public int getActivityStartedCount() {
        return this.callbacks.startedCount.get();
    }

    /* access modifiers changed from: package-private */
    public int getActivityPausedCount() {
        return this.callbacks.pausedCount.get();
    }

    /* access modifiers changed from: package-private */
    public int getActivityStoppedCount() {
        return this.callbacks.stoppedCount.get();
    }

    /* access modifiers changed from: package-private */
    public int getActivityDestroyedCount() {
        return this.callbacks.destroyedCount.get();
    }

    public void register(AppLifecycleListener listener) {
        Preconditions.checkNotNull(listener);
        this.callbacks.lifecycleListeners.add(listener);
    }

    public void unregister(AppLifecycleListener listener) {
        Preconditions.checkNotNull(listener);
        this.callbacks.lifecycleListeners.remove(listener);
    }

    /* access modifiers changed from: package-private */
    public ComponentCallbacks2 getComponentCallbacks2() {
        return this.callbacks;
    }

    /* access modifiers changed from: package-private */
    public Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return this.callbacks;
    }

    private static final class Callbacks implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
        /* access modifiers changed from: private */
        public final AtomicInteger createdCount;
        /* access modifiers changed from: private */
        public final AtomicInteger destroyedCount;
        /* access modifiers changed from: private */
        public Boolean lastForegroundState;
        /* access modifiers changed from: private */
        public final List<AppLifecycleListener> lifecycleListeners;
        /* access modifiers changed from: private */
        public volatile String nameOfForegroundActivity;
        /* access modifiers changed from: private */
        public final AtomicInteger pausedCount;
        /* access modifiers changed from: private */
        public final AtomicInteger resumedCount;
        /* access modifiers changed from: private */
        public final AtomicInteger startedCount;
        private volatile Activity stoppedActivity;
        /* access modifiers changed from: private */
        public final AtomicInteger stoppedCount;

        private Callbacks() {
            this.lifecycleListeners = new CopyOnWriteArrayList();
            this.createdCount = new AtomicInteger();
            this.startedCount = new AtomicInteger();
            this.resumedCount = new AtomicInteger();
            this.pausedCount = new AtomicInteger();
            this.stoppedCount = new AtomicInteger();
            this.destroyedCount = new AtomicInteger();
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            this.createdCount.incrementAndGet();
            this.stoppedActivity = null;
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityCreated) {
                    ((AppLifecycleListener.OnActivityCreated) listener).onActivityCreated(activity, savedInstanceState);
                }
            }
        }

        public void onActivityStarted(Activity activity) {
            this.startedCount.incrementAndGet();
            this.stoppedActivity = null;
            checkVisibilityAndNotifyListeners(activity);
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityStarted) {
                    ((AppLifecycleListener.OnActivityStarted) listener).onActivityStarted(activity);
                }
            }
        }

        public void onActivityResumed(Activity activity) {
            this.resumedCount.incrementAndGet();
            this.stoppedActivity = null;
            this.nameOfForegroundActivity = activity.getClass().getSimpleName();
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityResumed) {
                    ((AppLifecycleListener.OnActivityResumed) listener).onActivityResumed(activity);
                }
            }
        }

        public void onActivityPaused(Activity activity) {
            this.pausedCount.incrementAndGet();
            this.nameOfForegroundActivity = null;
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityPaused) {
                    ((AppLifecycleListener.OnActivityPaused) listener).onActivityPaused(activity);
                }
            }
        }

        public void onActivityStopped(Activity activity) {
            this.stoppedCount.incrementAndGet();
            this.stoppedActivity = activity;
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityStopped) {
                    ((AppLifecycleListener.OnActivityStopped) listener).onActivityStopped(activity);
                }
            }
            checkVisibilityAndNotifyListeners(activity);
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivitySaveInstanceState) {
                    ((AppLifecycleListener.OnActivitySaveInstanceState) listener).onActivitySaveInstanceState(activity, outState);
                }
            }
        }

        public void onActivityDestroyed(Activity activity) {
            this.destroyedCount.incrementAndGet();
            this.stoppedActivity = null;
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnActivityDestroyed) {
                    ((AppLifecycleListener.OnActivityDestroyed) listener).onActivityDestroyed(activity);
                }
            }
        }

        public void onTrimMemory(int level) {
            for (AppLifecycleListener listener : this.lifecycleListeners) {
                if (listener instanceof AppLifecycleListener.OnTrimMemory) {
                    ((AppLifecycleListener.OnTrimMemory) listener).onTrimMemory(level);
                }
            }
            if (level >= 20 && this.stoppedActivity != null) {
                ensureToBackground(this.stoppedActivity);
            }
            this.stoppedActivity = null;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onLowMemory() {
        }

        private void checkVisibilityAndNotifyListeners(Activity activity) {
            setForegroundState(Boolean.valueOf(ProcessStats.isAppInForeground(activity.getApplicationContext())), activity);
        }

        private void ensureToBackground(Activity activity) {
            setForegroundState(false, activity);
        }

        private void setForegroundState(Boolean inForeground, Activity activity) {
            if (inForeground.equals(this.lastForegroundState)) {
                PrimesLog.m52i(AppLifecycleTracker.TAG, "App foreground state unchanged: inForeground ? %b", inForeground);
                return;
            }
            this.lastForegroundState = inForeground;
            if (inForeground.booleanValue()) {
                PrimesLog.m52i(AppLifecycleTracker.TAG, "App transition to foreground", new Object[0]);
                for (AppLifecycleListener listener : this.lifecycleListeners) {
                    if (listener instanceof AppLifecycleListener.OnAppToForeground) {
                        ((AppLifecycleListener.OnAppToForeground) listener).onAppToForeground(activity);
                    }
                }
                return;
            }
            PrimesLog.m52i(AppLifecycleTracker.TAG, "App transition to background", new Object[0]);
            for (AppLifecycleListener listener2 : this.lifecycleListeners) {
                if (listener2 instanceof AppLifecycleListener.OnAppToBackground) {
                    ((AppLifecycleListener.OnAppToBackground) listener2).onAppToBackground(activity);
                }
            }
        }
    }
}
