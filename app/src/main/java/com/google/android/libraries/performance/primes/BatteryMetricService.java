package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.battery.BatteryCapture;
import com.google.android.libraries.performance.primes.battery.StatsStorage;
import com.google.android.libraries.performance.primes.battery.SystemHealthCapture;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.android.libraries.stitch.util.ThreadUtil;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import logs.proto.wireless.performance.mobile.BatteryMetric;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

final class BatteryMetricService extends AbstractMetricService implements PrimesStartupListener, AppLifecycleListener.OnAppToForeground, AppLifecycleListener.OnAppToBackground {
    static final int MAX_CONCURRENT_MEASUREMENTS = 10;
    static final String TAG = "BatteryMetricService";
    private final BatteryCapture batteryCapture;
    private final List<Future<BatteryCapture.Snapshot>> batteryCaptures;
    final AtomicBoolean inForeground = new AtomicBoolean();
    private final boolean logDeferred;
    private final Object monitorMutex = new Object();
    private volatile boolean monitoring = false;
    final ConcurrentHashMap<String, PrimesBatterySnapshot> startSnapshots = new ConcurrentHashMap<>();
    private final StatsStorage storage;

    static BatteryMetricService createService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, SharedPreferences sharedPreferences, Optional<PrimesBatteryConfigurations> optionalConfigs) {
        PrimesBatteryConfigurations configs = optionalConfigs.mo23381or(PrimesBatteryConfigurations.newBuilder().build());
        return new BatteryMetricService(transmitter, application, metricStamperSupplier, executorServiceSupplier, sharedPreferences, new BatteryCapture(metricStamperSupplier, new SystemHealthCapture(application), BatteryMetricService$$Lambda$0.$instance, BatteryMetricService$$Lambda$1.$instance, configs.getMetricExtensionProvider()), configs.isDeferredLogging());
    }

    BatteryMetricService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, SharedPreferences sharedPreferences, BatteryCapture batteryCapture2, boolean logDeferred2) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, MetricRecorder.RunIn.SAME_THREAD);
        this.storage = new StatsStorage(sharedPreferences);
        this.batteryCapture = batteryCapture2;
        this.logDeferred = logDeferred2;
        this.batteryCaptures = logDeferred2 ? new ArrayList() : null;
    }

    public void onPrimesInitialize() {
    }

    public void onFirstActivityCreated() {
        if (!this.inForeground.get()) {
            onAppToForeground(null);
        }
        startMonitoring();
    }

    public void onAppToForeground(Activity activity) {
        PrimesExecutors.handleFuture(onAppToForeground());
    }

    /* access modifiers changed from: package-private */
    public Future<?> onAppToForeground() {
        if (!this.inForeground.getAndSet(true)) {
            return scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo.BACKGROUND_TO_FOREGROUND);
        }
        PrimesLog.m56w(TAG, "unexpected state onAppToForeground");
        return null;
    }

    public void onAppToBackground(Activity activity) {
        PrimesExecutors.handleFuture(onAppToBackground());
    }

    /* access modifiers changed from: package-private */
    public Future<?> onAppToBackground() {
        if (this.inForeground.getAndSet(false)) {
            return scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo.FOREGROUND_TO_BACKGROUND);
        }
        PrimesLog.m56w(TAG, "unexpected state onAppToBackground");
        return null;
    }

    /* access modifiers changed from: package-private */
    public Future<?> onForegroundServiceStarted() {
        return scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo.FOREGROUND_SERVICE_START);
    }

    /* access modifiers changed from: package-private */
    public Future<?> onForegroundServiceStopped() {
        return scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo.FOREGROUND_SERVICE_STOP);
    }

    /* access modifiers changed from: package-private */
    public Future<?> scheduleManualCapture(String customEventName, boolean isEventNameConstant) {
        return scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo.UNKNOWN, customEventName, isEventNameConstant);
    }

    /* access modifiers changed from: package-private */
    public void startBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant) {
        if (this.startSnapshots.size() < 10) {
            this.startSnapshots.put(customEventName, new PrimesBatterySnapshot(captureCustomDiffSnapshot(BatteryMetric.BatteryStatsDiff.SampleInfo.CUSTOM_MEASURE_START, customEventName, isEventNameConstant)));
        }
    }

    /* access modifiers changed from: package-private */
    public void stopBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        PrimesBatterySnapshot startSnapshot = this.startSnapshots.remove(customEventName);
        if (startSnapshot != null) {
            recordBatteryStatsDiff(startSnapshot, new PrimesBatterySnapshot(captureCustomDiffSnapshot(BatteryMetric.BatteryStatsDiff.SampleInfo.CUSTOM_MEASURE_STOP, customEventName, isEventNameConstant)), customEventName, isEventNameConstant, metricExtension);
            return;
        }
        String valueOf = customEventName;
        PrimesLog.m56w(TAG, valueOf.length() != 0 ? "startBatteryDiffMeasurement() failed for customEventName ".concat(valueOf) : "startBatteryDiffMeasurement() failed for customEventName ");
    }

    /* access modifiers changed from: package-private */
    public void cancelBatteryDiffMeasurement(String customEventName) {
        if (this.startSnapshots.remove(customEventName) != null) {
            String valueOf = customEventName;
            PrimesLog.m56w(TAG, valueOf.length() != 0 ? "Cancel battery diff measurement for customEventName ".concat(valueOf) : "Cancel battery diff measurement for customEventName ");
        }
    }

    private Future<BatteryCapture.Snapshot> captureCustomDiffSnapshot(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        return getListeningScheduledExecutorService().submit((Callable) new BatteryMetricService$$Lambda$2(this, sampleInfo, customEventName, isEventNameConstant));
    }

    /* access modifiers changed from: package-private */
    public void recordBatteryStatsDiff(PrimesBatterySnapshot startSnapshot, PrimesBatterySnapshot endSnapshot, String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        if (!startSnapshot.isEmptySnapshot() && !endSnapshot.isEmptySnapshot()) {
            PrimesExecutors.handleFuture(getListeningScheduledExecutorService().submit((Runnable) new BatteryMetricService$$Lambda$3(this, startSnapshot, endSnapshot, customEventName, isEventNameConstant, metricExtension)));
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$recordBatteryStatsDiff$1$BatteryMetricService(PrimesBatterySnapshot startSnapshot, PrimesBatterySnapshot endSnapshot, String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
        if (startSnapshot.getBatterySnapshot() != null && endSnapshot.getBatterySnapshot() != null) {
            SystemHealthProto.SystemHealthMetric metric = this.batteryCapture.createBatteryMetric(startSnapshot.getBatterySnapshot().toStatsRecord(), endSnapshot.getBatterySnapshot().toStatsRecord());
            if (metric == null || !metric.hasBatteryUsageMetric()) {
                PrimesLog.m56w(TAG, "at least one battery snapshot failed");
            } else {
                recordSystemHealthMetric(customEventName, isEventNameConstant, metric, metricExtension);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startMonitoring() {
        synchronized (this.monitorMutex) {
            if (!this.monitoring) {
                AppLifecycleMonitor.getInstance(getApplication()).register(this);
                this.monitoring = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopMonitoring() {
        synchronized (this.monitorMutex) {
            if (this.monitoring) {
                AppLifecycleMonitor.getInstance(getApplication()).unregister(this);
                this.monitoring = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void shutdownService() {
        stopMonitoring();
        synchronized (this.storage) {
            this.storage.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public StatsStorage.StatsRecord fromStorage() {
        StatsStorage.StatsRecord readStatsRecord;
        ThreadUtil.ensureBackgroundThread();
        synchronized (this.storage) {
            readStatsRecord = this.storage.readStatsRecord();
        }
        return readStatsRecord;
    }

    /* access modifiers changed from: package-private */
    public boolean toStorage(StatsStorage.StatsRecord statsRecord) {
        boolean writeStatsRecord;
        ThreadUtil.ensureBackgroundThread();
        synchronized (this.storage) {
            writeStatsRecord = this.storage.writeStatsRecord(statsRecord);
        }
        return writeStatsRecord;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: captureBattery */
    public BatteryCapture.Snapshot lambda$captureForDeferredLogging$2$BatteryMetricService(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        return this.batteryCapture.takeSnapshot(sampleInfo, customEventName, isEventNameConstant);
    }

    /* access modifiers changed from: package-private */
    public void log(StatsStorage.StatsRecord start, StatsStorage.StatsRecord end) {
        PrimesLog.m54v(TAG, "log start: %s\nend: %s", start, end);
        SystemHealthProto.SystemHealthMetric metric = this.batteryCapture.createBatteryMetric(start, end);
        if (metric != null) {
            recordSystemHealthMetric(end.getCustomEventName(), end.isEventNameConstant().booleanValue(), metric, end.getMetricExtension());
        }
    }

    /* access modifiers changed from: package-private */
    public Future<?> scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo) {
        return scheduleCapture(sampleInfo, null, true);
    }

    private Future<?> scheduleCapture(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        if (this.logDeferred) {
            return captureForDeferredLogging(sampleInfo, customEventName, isEventNameConstant);
        }
        return captureAndLogInstantaneous(sampleInfo, customEventName, isEventNameConstant);
    }

    private Future<?> captureForDeferredLogging(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        Future<BatteryCapture.Snapshot> future = getListeningScheduledExecutorService().submit((Callable) new BatteryMetricService$$Lambda$4(this, sampleInfo, customEventName, isEventNameConstant));
        PrimesLog.m48d(TAG, "adding future BatteryCapture");
        synchronized (this.batteryCaptures) {
            this.batteryCaptures.add(future);
            if (this.inForeground.get()) {
                return future;
            }
            Future<?> logDeferredData = logDeferredData();
            return logDeferredData;
        }
    }

    private Future<?> logDeferredData() {
        List<Future<BatteryCapture.Snapshot>> captures;
        synchronized (this.batteryCaptures) {
            captures = new ArrayList<>(this.batteryCaptures);
            this.batteryCaptures.clear();
        }
        PrimesLog.m52i(TAG, "Logging captures: %d", Integer.valueOf(captures.size()));
        return getListeningScheduledExecutorService().submit((Runnable) new BatteryMetricService$$Lambda$5(this, captures));
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$logDeferredData$3$BatteryMetricService(List captures) {
        StatsStorage.StatsRecord endRecord = fromStorage();
        Iterator it = captures.iterator();
        while (it.hasNext()) {
            try {
                StatsStorage.StatsRecord startRecord = endRecord;
                endRecord = ((BatteryCapture.Snapshot) ((Future) it.next()).get()).toStatsRecord();
                if (startRecord != null) {
                    log(startRecord, endRecord);
                }
            } catch (Exception e) {
                PrimesLog.m49e(TAG, "unpexpected failure", e);
            }
        }
        toStorage(endRecord);
    }

    private Future<?> captureAndLogInstantaneous(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        return getListeningScheduledExecutorService().submit((Runnable) new BatteryMetricService$$Lambda$6(this, sampleInfo, customEventName, isEventNameConstant));
    }

    /* access modifiers changed from: private */
    /* renamed from: captureAndLogLocked */
    public void lambda$captureAndLogInstantaneous$4$BatteryMetricService(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        ThreadUtil.ensureBackgroundThread();
        if (!isShutdown()) {
            synchronized (this.storage) {
                StatsStorage.StatsRecord end = lambda$captureForDeferredLogging$2$BatteryMetricService(sampleInfo, customEventName, isEventNameConstant).toStatsRecord();
                StatsStorage.StatsRecord start = fromStorage();
                if (toStorage(end)) {
                    log(start, end);
                } else {
                    shutdownService();
                    PrimesLog.m56w(TAG, "Failure storing persistent snapshot and helper data");
                }
            }
            return;
        }
        PrimesLog.m48d(TAG, "shutdown - skipping capture");
    }
}
