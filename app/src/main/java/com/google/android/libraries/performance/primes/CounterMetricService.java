package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.aggregation.MetricAggregatorIdentifier;
import com.google.android.libraries.performance.primes.aggregation.impl.MetricAggregatorStore;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

public final class CounterMetricService extends AbstractMetricService implements AppLifecycleListener.OnAppToBackground {
    private final AppLifecycleMonitor lifecycleMonitor;
    private final MetricAggregatorStore metricAggregatorStore = new MetricAggregatorStore();

    public /* bridge */ /* synthetic */ void onShutdown() {
        super.onShutdown();
    }

    static CounterMetricService createService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier) {
        return new CounterMetricService(transmitter, application, AppLifecycleMonitor.getInstance(application), metricStamperSupplier, executorServiceSupplier);
    }

    CounterMetricService(MetricTransmitter transmitter, Application application, AppLifecycleMonitor lifecycleMonitor2, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, MetricRecorder.RunIn.BACKGROUND_THREAD);
        this.lifecycleMonitor = lifecycleMonitor2;
        lifecycleMonitor2.register(this);
    }

    /* access modifiers changed from: package-private */
    public void shutdownService() {
        this.lifecycleMonitor.unregister(this);
    }

    public void onAppToBackground(Activity activity) {
        SystemHealthProto.SystemHealthMetric metric = this.metricAggregatorStore.flushMetrics();
        if (metric != null) {
            recordSystemHealthMetric(metric);
        }
    }

    /* access modifiers changed from: package-private */
    public void addValueToCounter(String counterName, String componentName, boolean areNamesConstant, long value) {
        if (areNamesConstant) {
            this.metricAggregatorStore.getAggregation(MetricAggregatorIdentifier.forCustomCounter(counterName, componentName)).recordValue(value);
        }
    }

    /* access modifiers changed from: package-private */
    public Counter startCounter() {
        return new Counter();
    }

    /* access modifiers changed from: package-private */
    public void stopCounter(Counter counter, String counterName, String componentName, boolean areNamesConstant) {
        if (areNamesConstant) {
            this.metricAggregatorStore.getAggregation(MetricAggregatorIdentifier.forCustomCounter(counterName, componentName)).recordAllFrom(counter.asSummaryAggregator());
        }
    }

    /* access modifiers changed from: package-private */
    public void flushCounterData() {
        SystemHealthProto.SystemHealthMetric metric = this.metricAggregatorStore.flushMetrics();
        if (metric != null) {
            recordSystemHealthMetric(metric);
        }
    }
}
