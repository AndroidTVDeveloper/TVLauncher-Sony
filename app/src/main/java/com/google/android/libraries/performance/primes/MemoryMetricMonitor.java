package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import logs.proto.wireless.performance.mobile.MemoryMetric;

final class MemoryMetricMonitor {
    static final int MEMORY_LOG_DELAY_IN_SECONDS = 10;
    private static final String TAG = "MemoryMetricMonitor";
    private final AppLifecycleMonitor appLifecycleMonitor;
    /* access modifiers changed from: private */
    public final Callback callback;
    /* access modifiers changed from: private */
    public final Supplier<ListeningScheduledExecutorService> executorServiceSupplier;
    /* access modifiers changed from: private */
    public ScheduledFuture<?> futureMemoryBackgroundTask;
    /* access modifiers changed from: private */
    public ScheduledFuture<?> futureMemoryForegroundTask;
    private final AtomicBoolean hasMemoryMonitorStarted;
    private final AppLifecycleListener.OnAppToBackground onAppToBackground;
    private final AppLifecycleListener.OnAppToForeground onAppToForeground;

    interface Callback {
        void onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode memoryEventCode, String str);
    }

    MemoryMetricMonitor(Callback callback2, Application appToMonitor, Supplier<ListeningScheduledExecutorService> executorServiceSupplier2) {
        this(callback2, appToMonitor, executorServiceSupplier2, AppLifecycleMonitor.getInstance(appToMonitor));
    }

    MemoryMetricMonitor(Callback callback2, Application appToMonitor, Supplier<ListeningScheduledExecutorService> executorServiceSupplier2, AppLifecycleMonitor appLifecycleMonitor2) {
        this.hasMemoryMonitorStarted = new AtomicBoolean(false);
        this.onAppToBackground = new AppLifecycleListener.OnAppToBackground() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
             arg types: [com.google.android.libraries.performance.primes.MemoryMetricMonitor$1$1, int, java.util.concurrent.TimeUnit]
             candidates:
              com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
              ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
              ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
              com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
            public void onAppToBackground(Activity activity) {
                final String activityName = activity.getClass().getSimpleName();
                MemoryMetricMonitor.this.callback.onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode.APP_TO_BACKGROUND, activityName);
                MemoryMetricMonitor.this.cancelFutureTasksIfAny();
                MemoryMetricMonitor memoryMetricMonitor = MemoryMetricMonitor.this;
                ScheduledFuture unused = memoryMetricMonitor.futureMemoryBackgroundTask = ((ListeningScheduledExecutorService) memoryMetricMonitor.executorServiceSupplier.get()).schedule((Runnable) new Runnable() {
                    public void run() {
                        MemoryMetricMonitor.this.callback.onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode.APP_IN_BACKGROUND_FOR_SECONDS, activityName);
                    }
                }, 10L, TimeUnit.SECONDS);
            }
        };
        this.onAppToForeground = new AppLifecycleListener.OnAppToForeground() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
             arg types: [com.google.android.libraries.performance.primes.MemoryMetricMonitor$2$1, int, java.util.concurrent.TimeUnit]
             candidates:
              com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
              ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
              ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
              com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
            public void onAppToForeground(Activity activity) {
                final String activityName = activity.getClass().getSimpleName();
                MemoryMetricMonitor.this.callback.onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode.APP_TO_FOREGROUND, activityName);
                MemoryMetricMonitor.this.cancelFutureTasksIfAny();
                MemoryMetricMonitor memoryMetricMonitor = MemoryMetricMonitor.this;
                ScheduledFuture unused = memoryMetricMonitor.futureMemoryForegroundTask = ((ListeningScheduledExecutorService) memoryMetricMonitor.executorServiceSupplier.get()).schedule((Runnable) new Runnable() {
                    public void run() {
                        MemoryMetricMonitor.this.callback.onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode.APP_IN_FOREGROUND_FOR_SECONDS, activityName);
                    }
                }, 10L, TimeUnit.SECONDS);
            }
        };
        this.callback = callback2;
        this.executorServiceSupplier = executorServiceSupplier2;
        this.appLifecycleMonitor = appLifecycleMonitor2;
    }

    /* access modifiers changed from: package-private */
    public void start() {
        if (this.hasMemoryMonitorStarted.getAndSet(true)) {
            PrimesLog.m56w(TAG, "Memory Monitor has already started. This MemoryMetricMonitor.start() is ignored.");
            return;
        }
        this.appLifecycleMonitor.register(this.onAppToBackground);
        this.appLifecycleMonitor.register(this.onAppToForeground);
    }

    /* access modifiers changed from: private */
    public void cancelFutureTasksIfAny() {
        ScheduledFuture<?> scheduledFuture = this.futureMemoryForegroundTask;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            this.futureMemoryForegroundTask = null;
        }
        ScheduledFuture<?> scheduledFuture2 = this.futureMemoryBackgroundTask;
        if (scheduledFuture2 != null) {
            scheduledFuture2.cancel(true);
            this.futureMemoryBackgroundTask = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        this.appLifecycleMonitor.unregister(this.onAppToBackground);
        this.appLifecycleMonitor.unregister(this.onAppToForeground);
    }
}
