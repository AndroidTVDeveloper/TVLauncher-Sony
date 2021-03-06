package com.google.android.libraries.performance.primes;

import com.google.android.libraries.performance.primes.TimerEvent;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.lang.Thread;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.PrimesTraceOuterClass;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

final class NoopPrimesApi implements PrimesApi {
    static final MetricTransmitter noopTransmitter = new MetricTransmitter() {
        public void send(SystemHealthProto.SystemHealthMetric message) {
        }
    };

    NoopPrimesApi() {
    }

    public void executeAfterInitialized(Runnable runnable) {
    }

    public void startMemoryMonitor() {
    }

    public void recordMemory(String customEventName, boolean isEventNameConstant) {
    }

    public void recordMemory(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
    }

    public void startMemoryDiffMeasurement(String customEventName) {
    }

    public void stopMemoryDiffMeasurement(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
    }

    public void cancelMemoryDiffMeasurement(String customEventName) {
    }

    public void recordNetwork(NetworkEvent networkEvent) {
    }

    public void sendPendingNetworkEvents() {
    }

    public void startGlobalTimer(String customEventName) {
    }

    public void stopGlobalTimer(String customEventName, boolean isEventNameConstant, TimerEvent.TimerStatus endStatus) {
    }

    public void stopGlobalTimer(String customEventName, String newEventName, boolean isEventNameConstant, TimerEvent.TimerStatus endStatus) {
    }

    public void stopGlobalTimer(String customEventName, String newEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension, TimerEvent.TimerStatus endStatus) {
    }

    public void cancelGlobalTimer(String customEventName) {
    }

    public void recordDuration(WhitelistToken whitelistToken, String customEventName, boolean isEventNameConstant, long startMs, long endMs, ExtensionMetric.MetricExtension metricExtension) {
    }

    public TimerEvent startTimer() {
        return TimerEvent.EMPTY_TIMER;
    }

    public void stopTimer(TimerEvent event, String customEventName, boolean isEventNameConstant, TimerEvent.TimerStatus timerStatus) {
    }

    public void stopTimer(TimerEvent event, String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension, TimerEvent.TimerStatus timerStatus) {
    }

    public void startCrashMonitor() {
    }

    public Thread.UncaughtExceptionHandler wrapCrashReportingIntoUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        return handler;
    }

    public void startJankRecorder(String eventName) {
    }

    public void stopJankRecorder(String eventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
    }

    public void cancelJankRecorder(String eventName) {
    }

    public MetricTransmitter getTransmitter() {
        return noopTransmitter;
    }

    public boolean isNetworkEnabled() {
        return false;
    }

    public void watchForMemoryLeak(Object object) {
    }

    public void recordTrace(TikTokWhitelistToken token, PrimesTraceOuterClass.PrimesTrace primesTrace, ExtensionMetric.MetricExtension metricExtension) {
    }

    public void recordPackageStats() {
    }

    public void recordBatterySnapshot(String customEventName, boolean isEventNameConstant) {
    }

    public void startBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant) {
    }

    public void stopBatteryDiffMeasurement(String customEventName, boolean isEventNameConstant, ExtensionMetric.MetricExtension metricExtension) {
    }

    public void cancelBatteryDiffMeasurement(String customEventName) {
    }

    public void recordBatterySnapshotOnForegroundServiceStart() {
    }

    public void recordBatterySnapshotOnForegroundServiceStop() {
    }

    public void addValueToCounter(String counterName, String componentName, boolean areNamesConstant, long value) {
    }

    public Counter startCounter() {
        return Counter.EMPTY_COUNTER;
    }

    public void stopCounter(Counter counter, String counterName, String componentName, boolean areNamesConstant) {
    }

    public void flushCounterData() {
    }

    public void shutdown() {
    }

    public Supplier<ListeningScheduledExecutorService> getExecutorServiceSupplier() {
        return null;
    }

    public boolean registerShutdownListener(ShutdownListener listener) {
        return false;
    }

    public void sendCustomLaunchedEvent() {
    }
}
