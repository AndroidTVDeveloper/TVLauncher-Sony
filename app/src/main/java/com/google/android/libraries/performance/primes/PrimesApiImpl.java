package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.TimerEvent;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.android.libraries.stitch.util.Preconditions;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.PrimesTraceOuterClass;

final class PrimesApiImpl implements PrimesApi {
    private static final String TAG = "Primes";
    static final AtomicInteger instanceCounter = new AtomicInteger();
    private final Application application;
    private final AtomicBoolean crashMonitorStarted = new AtomicBoolean();
    private final Supplier<ListeningScheduledExecutorService> executorServiceSupplier;
    /* access modifiers changed from: private */
    public final CountDownLatch initializationDoneSignal = new CountDownLatch(1);
    private final AtomicReference<PrimesApi> primesApiRef = new AtomicReference<>();

    PrimesApiImpl(Application application2, Supplier<ListeningScheduledExecutorService> executorServiceSupplier2, boolean earlyTimerSupport) {
        Preconditions.checkState(isPrimesSupported());
        this.application = (Application) Preconditions.checkNotNull(application2);
        this.executorServiceSupplier = (Supplier) Preconditions.checkNotNull(executorServiceSupplier2);
        instanceCounter.incrementAndGet();
        this.primesApiRef.set(new PreInitPrimesApi(earlyTimerSupport));
    }

    /* access modifiers changed from: package-private */
    public PrimesApi api() {
        return this.primesApiRef.get();
    }

    static class FirstActivityCreateListener implements AppLifecycleListener.OnActivityCreated, ShutdownListener {
        private boolean activityCreated;
        private final AppLifecycleMonitor appLifecycleMonitor;
        private final List<PrimesStartupListener> startupListeners = new ArrayList();

        FirstActivityCreateListener(AppLifecycleMonitor appLifecycleMonitor2) {
            this.appLifecycleMonitor = appLifecycleMonitor2;
            appLifecycleMonitor2.register(this);
        }

        public void onShutdown() {
            this.appLifecycleMonitor.unregister(this);
        }

        public void onActivityCreated(Activity activity, Bundle bundle) {
            synchronized (this) {
                this.activityCreated = true;
            }
            this.appLifecycleMonitor.unregister(this);
            for (PrimesStartupListener listener : this.startupListeners) {
                listener.onFirstActivityCreated();
            }
        }

        /* access modifiers changed from: package-private */
        public void registerOrRun(PrimesStartupListener launchListener) {
            synchronized (this) {
                if (!this.activityCreated) {
                    this.startupListeners.add(launchListener);
                } else {
                    launchListener.onFirstActivityCreated();
                }
            }
        }
    }

    static class FirstAppToBackgroundListener implements AppLifecycleListener.OnAppToBackground, ShutdownListener {
        private final AppLifecycleMonitor appLifecycleMonitor;
        private boolean appToBackground;
        private final Supplier<ListeningScheduledExecutorService> executorServiceSupplier;
        private final ArrayList<Runnable> firstToBackgroundTasks = new ArrayList<>();

        FirstAppToBackgroundListener(AppLifecycleMonitor appLifecycleMonitor2, Supplier<ListeningScheduledExecutorService> executorServiceSupplier2) {
            this.appLifecycleMonitor = appLifecycleMonitor2;
            this.executorServiceSupplier = executorServiceSupplier2;
            appLifecycleMonitor2.register(this);
        }

        public void onShutdown() {
            this.appLifecycleMonitor.unregister(this);
        }

        public void onAppToBackground(Activity activity) {
            synchronized (this.firstToBackgroundTasks) {
                if (!this.appToBackground) {
                    this.appToBackground = true;
                    this.appLifecycleMonitor.unregister(this);
                    Iterator<Runnable> it = this.firstToBackgroundTasks.iterator();
                    while (it.hasNext()) {
                        PrimesExecutors.handleFuture(this.executorServiceSupplier.get().submit(it.next()));
                    }
                    this.firstToBackgroundTasks.clear();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void registerTask(Runnable task) {
            synchronized (this.firstToBackgroundTasks) {
                if (this.appToBackground) {
                    PrimesExecutors.handleFuture(this.executorServiceSupplier.get().submit(task));
                } else {
                    this.firstToBackgroundTasks.add(task);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Runnable createInitTriggerTask(ExecutorService initExecutor, PrimesConfigurationsProvider configurationsProvider, Supplier<PrimesFlags> flagsSupplier, Supplier<SharedPreferences> sharedPreferencesSupplier, Supplier<Shutdown> shutdownSupplier, boolean shutdownInitExecutor) {
        return wrapInitTask(initExecutor, createInitTask(this, configurationsProvider, flagsSupplier, sharedPreferencesSupplier, shutdownSupplier), shutdownInitExecutor);
    }

    private Runnable wrapInitTask(final ExecutorService initExecutor, final Runnable initTask, final boolean shutdownInitExecutor) {
        return oneOffRunnable(new Runnable() {
            public void run() {
                PrimesApiImpl.scheduleInitialization(initExecutor, PrimesApiImpl.this, initTask);
                if (shutdownInitExecutor) {
                    initExecutor.shutdown();
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public Runnable createAndRegisterInitTriggerTask(ExecutorService initExecutor, PrimesConfigurationsProvider configurationsProvider, Supplier<PrimesFlags> flagsSupplier, Supplier<SharedPreferences> sharedPreferencesSupplier, Supplier<Shutdown> shutdownSupplier, boolean shutdownInitExecutor) {
        Runnable initTask = createInitTask(this, configurationsProvider, flagsSupplier, sharedPreferencesSupplier, shutdownSupplier);
        registerInitTask(initTask);
        return wrapInitTask(initExecutor, initTask, shutdownInitExecutor);
    }

    /* access modifiers changed from: package-private */
    public void registerInitTask(Runnable initTask) {
        PrimesApi currentApi = api();
        if (currentApi instanceof PreInitPrimesApi) {
            ((PreInitPrimesApi) currentApi).setPrimesInitTask(initTask, this.initializationDoneSignal);
            PrimesLog.m52i(TAG, "init task registered");
            return;
        }
        PrimesLog.m56w(TAG, "could not register init task - current api: %s", currentApi);
    }

    /* access modifiers changed from: private */
    public static void scheduleInitialization(ExecutorService initExecutor, PrimesApiImpl primesApiImpl, Runnable initTask) {
        try {
            PrimesExecutors.handleFuture(initExecutor.submit(initTask));
        } catch (RuntimeException e) {
            PrimesLog.m55w(TAG, "Primes failed to initialize", e);
            primesApiImpl.shutdown();
        }
    }

    static Runnable oneOffRunnable(final Runnable wrappedTask) {
        return new Runnable() {
            private final AtomicReference<Runnable> taskRef = new AtomicReference<>(wrappedTask);

            public void run() {
                Runnable task = this.taskRef.getAndSet(null);
                if (task != null) {
                    task.run();
                }
            }
        };
    }

    static Runnable createInitTask(PrimesApiImpl primesApiImpl, PrimesConfigurationsProvider configurationsProvider, Supplier<PrimesFlags> flagsSupplier, Supplier<SharedPreferences> sharedPreferencesSupplier, Supplier<Shutdown> shutdownSupplier) {
        final FirstActivityCreateListener firstActivityCreateListener = new FirstActivityCreateListener(AppLifecycleMonitor.getInstance(primesApiImpl.application));
        final FirstAppToBackgroundListener firstAppToBackgroundListener = new FirstAppToBackgroundListener(AppLifecycleMonitor.getInstance(primesApiImpl.application), primesApiImpl.executorServiceSupplier);
        final PrimesConfigurationsProvider primesConfigurationsProvider = configurationsProvider;
        final Supplier<PrimesFlags> supplier = flagsSupplier;
        final Supplier<SharedPreferences> supplier2 = sharedPreferencesSupplier;
        final Supplier<Shutdown> supplier3 = shutdownSupplier;
        return oneOffRunnable(new Runnable() {
            public void run() {
                try {
                    PrimesLog.m56w(PrimesApiImpl.TAG, "background initialization");
                    PrimesApiImpl.initializeInBackground(PrimesApiImpl.this, primesConfigurationsProvider, supplier, supplier2, supplier3, firstActivityCreateListener, firstAppToBackgroundListener);
                } catch (RuntimeException e) {
                    PrimesLog.m55w(PrimesApiImpl.TAG, "Primes failed to initialize in the background", e);
                    PrimesApiImpl.this.shutdown();
                } catch (Throwable th) {
                    PrimesApiImpl.this.initializationDoneSignal.countDown();
                    throw th;
                }
                PrimesApiImpl.this.initializationDoneSignal.countDown();
            }
        });
    }

    /* access modifiers changed from: private */
    public static void initializeInBackground(PrimesApiImpl primesApiImpl, PrimesConfigurationsProvider configurationsProvider, Supplier<PrimesFlags> flagsSupplier, Supplier<SharedPreferences> sharedPreferencesSupplier, Supplier<Shutdown> shutdownSupplier, FirstActivityCreateListener firstActivityCreateListener, FirstAppToBackgroundListener firstAppToBackgroundListener) {
        PrimesApiImpl primesApiImpl2 = primesApiImpl;
        FirstActivityCreateListener firstActivityCreateListener2 = firstActivityCreateListener;
        FirstAppToBackgroundListener firstAppToBackgroundListener2 = firstAppToBackgroundListener;
        PrimesForPrimesMeasurements.initializationMeasurement().onInitStart();
        Preconditions.checkNotNull(Integer.valueOf(C1103R.string.primes_marker));
        Application application2 = primesApiImpl2.application;
        Supplier<ListeningScheduledExecutorService> executorServiceSupplier2 = primesApiImpl2.executorServiceSupplier;
        AtomicReference<PrimesApi> primesApiRef2 = primesApiImpl2.primesApiRef;
        Shutdown shutdown = shutdownSupplier.get();
        shutdown.registerShutdownListener(firstActivityCreateListener2);
        shutdown.registerShutdownListener(firstAppToBackgroundListener2);
        shutdown.init(application2, sharedPreferencesSupplier, executorServiceSupplier2);
        if (shutdown.isShutdown()) {
            primesApiImpl.shutdown();
            return;
        }
        PrimesForPrimesMeasurements.initializationMeasurement().onShutdownInitialized();
        SharedPreferences sharedPreferences = sharedPreferencesSupplier.get();
        PrimesConfigurations validConfigs = PrimesConfigurations.lazyValid((PrimesConfigurations) Preconditions.checkNotNull(configurationsProvider.get()));
        PrimesForPrimesMeasurements.initializationMeasurement().onConfigsCreated();
        PrimesFlags primesFlags = (PrimesFlags) Preconditions.checkNotNull(flagsSupplier.get());
        if (shutdown.isShutdown()) {
            primesApiImpl.shutdown();
            return;
        }
        PrimesForPrimesMeasurements.initializationMeasurement().onFlagsCreated();
        PrimesApi api = primesApiRef2.get();
        PreInitPrimesApi preInitApi = api instanceof PreInitPrimesApi ? (PreInitPrimesApi) api : null;
        if (api != null) {
            final LazyMetricServices lazyMetricServices = new LazyMetricServices(application2, executorServiceSupplier2, validConfigs, primesFlags, sharedPreferences, shutdown, preInitApi.getTimerEvents());
            ConfiguredPrimesApi configuredPrimesApi = new ConfiguredPrimesApi(lazyMetricServices, application2.getPackageName());
            if (shutdown.isShutdown()) {
                primesApiImpl.shutdown();
                return;
            }
            PrimesApi prevApi = primesApiRef2.get();
            if (!(prevApi instanceof PreInitPrimesApi) || !primesApiRef2.compareAndSet(prevApi, configuredPrimesApi)) {
                PrimesLog.m48d(TAG, "Primes shutdown during initialization");
                configuredPrimesApi.shutdown();
            } else {
                for (PrimesStartupListener listener : configuredPrimesApi.initAndGetServices()) {
                    listener.onPrimesInitialize();
                    firstActivityCreateListener2.registerOrRun(listener);
                }
                if (!shutdown.isShutdown()) {
                    ((PreInitPrimesApi) prevApi).executeScheduledCallsOn(configuredPrimesApi);
                    firstAppToBackgroundListener2.registerTask(new Runnable() {
                        public void run() {
                            LazyMetricServices.this.primesForPrimesQueue().enqueueMessage(PrimesForPrimesLogger.primesInitMetricsSupplier());
                        }
                    });
                }
                prevApi.shutdown();
            }
            if (validConfigs.memoryLeakConfigurations().isPresent() || primesFlags.isLeakDetectionEnabled() || primesFlags.isLeakDetectionV2Enabled()) {
                PrimesHprofFile.deleteHeapDumpIfExists(application2);
                PrimesHprofFile.deleteMiniHeapDumpHprofIfExists(application2);
            }
            PrimesForPrimesMeasurements.initializationMeasurement().onInitEnd();
        }
    }

    public void shutdown() {
        this.primesApiRef.getAndSet(new NoopPrimesApi()).shutdown();
        try {
            AppLifecycleMonitor.shutdownInstance(this.application);
        } catch (RuntimeException e) {
            PrimesLog.m56w(TAG, "Failed to shutdown app lifecycle monitor");
        }
    }

    static boolean isPrimesSupported() {
        if (Build.VERSION.SDK_INT >= 16) {
            return true;
        }
        PrimesLog.m56w(TAG, "Primes calls will be ignored. API's < 16 are not supported.");
        return false;
    }

    public boolean registerShutdownListener(ShutdownListener shutdownListener) {
        return api().registerShutdownListener(shutdownListener);
    }

    public MetricTransmitter getTransmitter() {
        return api().getTransmitter();
    }

    public Supplier<ListeningScheduledExecutorService> getExecutorServiceSupplier() {
        Supplier<ListeningScheduledExecutorService> currentSupplier = api().getExecutorServiceSupplier();
        return currentSupplier != null ? currentSupplier : this.executorServiceSupplier;
    }

    public void executeAfterInitialized(Runnable runnable) {
        api().executeAfterInitialized(runnable);
    }

    public void startMemoryMonitor() {
        api().startMemoryMonitor();
    }

    public void recordMemory(String eventName, boolean isEventNameConstant) {
        api().recordMemory(eventName, isEventNameConstant);
    }

    public void recordMemory(String eventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        api().recordMemory(eventName, isEventNameConstant, metricExtension);
    }

    public void startMemoryDiffMeasurement(String customEventName) {
        api().startMemoryDiffMeasurement(customEventName);
    }

    public void stopMemoryDiffMeasurement(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        api().stopMemoryDiffMeasurement(customEventName, isEventNameConstant, metricExtension);
    }

    public void cancelMemoryDiffMeasurement(String customEventName) {
        api().cancelMemoryDiffMeasurement(customEventName);
    }

    public void recordNetwork(NetworkEvent networkEvent) {
        api().recordNetwork(networkEvent);
    }

    public void sendPendingNetworkEvents() {
        api().sendPendingNetworkEvents();
    }

    public void startGlobalTimer(String eventName) {
        api().startGlobalTimer(eventName);
    }

    public void stopGlobalTimer(String eventName, boolean isEventNameConstant, TimerEvent.TimerStatus timerEndStatus) {
        api().stopGlobalTimer(eventName, isEventNameConstant, timerEndStatus);
    }

    public void stopGlobalTimer(String eventName, String newEventName, boolean isEventNameConstant, TimerEvent.TimerStatus timerEndStatus) {
        api().stopGlobalTimer(eventName, newEventName, isEventNameConstant, timerEndStatus);
    }

    public void stopGlobalTimer(String customEventName, String newEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension, TimerEvent.TimerStatus timerEndStatus) {
        api().stopGlobalTimer(customEventName, newEventName, isEventNameConstant, metricExtension, timerEndStatus);
    }

    public void cancelGlobalTimer(String eventName) {
        api().cancelGlobalTimer(eventName);
    }

    public void recordDuration(WhitelistToken whitelistToken, String customEventName, boolean isEventNameConstant, long startMs, long endMs, ExtensionMetric.MetricExtension metricExtension) {
        api().recordDuration(whitelistToken, customEventName, isEventNameConstant, startMs, endMs, metricExtension);
    }

    public TimerEvent startTimer() {
        return api().startTimer();
    }

    public void stopTimer(TimerEvent event, String eventName, boolean isEventNameConstant, TimerEvent.TimerStatus timerStatuss) {
        api().stopTimer(event, eventName, isEventNameConstant, timerStatuss);
    }

    public void stopTimer(TimerEvent event, String eventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension, TimerEvent.TimerStatus timerStatus) {
        api().stopTimer(event, eventName, isEventNameConstant, metricExtension, timerStatus);
    }

    public void startCrashMonitor() {
        if (!this.crashMonitorStarted.getAndSet(true)) {
            api().startCrashMonitor();
        }
    }

    public Thread.UncaughtExceptionHandler wrapCrashReportingIntoUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        return api().wrapCrashReportingIntoUncaughtExceptionHandler(handler);
    }

    public void startJankRecorder(String eventName) {
        api().startJankRecorder(eventName);
    }

    public void stopJankRecorder(String eventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        api().stopJankRecorder(eventName, isEventNameConstant, metricExtension);
    }

    public void cancelJankRecorder(String eventName) {
        api().cancelJankRecorder(eventName);
    }

    public void watchForMemoryLeak(Object object) {
        api().watchForMemoryLeak(object);
    }

    public void recordPackageStats() {
        api().recordPackageStats();
    }

    public void recordTrace(TikTokWhitelistToken token, PrimesTraceOuterClass.PrimesTrace primesTrace, ExtensionMetric.MetricExtension metricExtension) {
        api().recordTrace(token, primesTrace, metricExtension);
    }

    public boolean isNetworkEnabled() {
        return api().isNetworkEnabled();
    }

    public void recordBatterySnapshot(String eventName, boolean isConstantEventName) {
        api().recordBatterySnapshot(eventName, isConstantEventName);
    }

    public void startBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant) {
        api().startBatteryDiffMeasurement(customEventName, isEventNameConstant);
    }

    public void stopBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        api().stopBatteryDiffMeasurement(customEventName, isEventNameConstant, metricExtension);
    }

    public void cancelBatteryDiffMeasurement(String customEventName) {
        api().cancelBatteryDiffMeasurement(customEventName);
    }

    public void recordBatterySnapshotOnForegroundServiceStart() {
        api().recordBatterySnapshotOnForegroundServiceStart();
    }

    public void recordBatterySnapshotOnForegroundServiceStop() {
        api().recordBatterySnapshotOnForegroundServiceStop();
    }

    public void addValueToCounter(String counterName, String componentName, boolean areNamesConstant, long value) {
        api().addValueToCounter(counterName, componentName, areNamesConstant, value);
    }

    public Counter startCounter() {
        return api().startCounter();
    }

    public void stopCounter(Counter counter, String counterName, String componentName, boolean areNamesConstant) {
        api().stopCounter(counter, counterName, componentName, areNamesConstant);
    }

    public void flushCounterData() {
        api().flushCounterData();
    }

    public void sendCustomLaunchedEvent() {
        api().sendCustomLaunchedEvent();
    }
}
