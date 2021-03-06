package com.google.android.libraries.performance.primes;

import com.google.android.libraries.performance.primes.sampling.PrimesSampling;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.android.libraries.stitch.util.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

class MetricRecorder {
    private static final String TAG = "MetricRecorder";
    private final Supplier<ListeningScheduledExecutorService> executorServiceSupplier;
    private final PrimesSampling instrumentationSampling;
    private final Supplier<MetricStamper> metricStamperSupplier;
    private final MetricTransmitter metricTransmitter;
    private final RunIn whereToRun;

    public enum RunIn {
        SAME_THREAD,
        BACKGROUND_THREAD
    }

    MetricRecorder(MetricTransmitter metricTransmitter2, Supplier<MetricStamper> metricStamperSupplier2, Supplier<ListeningScheduledExecutorService> executorServiceSupplier2, RunIn whereToRun2, int sampleRatePerSecond) {
        this.metricTransmitter = (MetricTransmitter) Preconditions.checkNotNull(metricTransmitter2);
        this.metricStamperSupplier = (Supplier) Preconditions.checkNotNull(metricStamperSupplier2);
        this.executorServiceSupplier = executorServiceSupplier2;
        this.whereToRun = whereToRun2;
        this.instrumentationSampling = new PrimesSampling(sampleRatePerSecond);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRecord() {
        return !this.instrumentationSampling.isSampleRateExceeded();
    }

    /* access modifiers changed from: package-private */
    public void record(SystemHealthProto.SystemHealthMetric message) {
        record(null, false, message, null, null);
    }

    /* access modifiers changed from: package-private */
    public void record(String customEventName, boolean isEventNameConstant, SystemHealthProto.SystemHealthMetric message, ExtensionMetric.MetricExtension metricExtension, String accountableComponentName) {
        record(this.whereToRun, customEventName, isEventNameConstant, message, metricExtension, accountableComponentName);
    }

    private void record(RunIn where, String customEventName, boolean isEventNameConstant, SystemHealthProto.SystemHealthMetric message, ExtensionMetric.MetricExtension metricExtension, String accountableComponentName) {
        if (where == RunIn.SAME_THREAD) {
            recordInternal(customEventName, isEventNameConstant, message, metricExtension, accountableComponentName);
        } else {
            recordInBackground(customEventName, isEventNameConstant, message, metricExtension, accountableComponentName);
        }
    }

    private void recordInBackground(String customEventName, boolean isEventNameConstant, SystemHealthProto.SystemHealthMetric message, ExtensionMetric.MetricExtension metricExtension, String accountableComponentName) {
        final String str = customEventName;
        final boolean z = isEventNameConstant;
        final SystemHealthProto.SystemHealthMetric systemHealthMetric = message;
        final ExtensionMetric.MetricExtension metricExtension2 = metricExtension;
        final String str2 = accountableComponentName;
        ListenableFuture<?> submit = this.executorServiceSupplier.get().submit((Runnable) new Runnable() {
            public void run() {
                MetricRecorder.this.recordInternal(str, z, systemHealthMetric, metricExtension2, str2);
            }
        });
    }

    /* access modifiers changed from: private */
    public void recordInternal(String customEventName, boolean isEventNameConstant, SystemHealthProto.SystemHealthMetric metric, ExtensionMetric.MetricExtension metricExtension, String accountableComponentName) {
        if (metric == null) {
            String valueOf = String.valueOf(customEventName);
            PrimesLog.m56w(TAG, valueOf.length() != 0 ? "metric is null, skipping recorded metric for event: ".concat(valueOf) : "metric is null, skipping recorded metric for event: ");
            return;
        }
        SystemHealthProto.SystemHealthMetric.Builder metricBuilder = (SystemHealthProto.SystemHealthMetric.Builder) this.metricStamperSupplier.get().stamp(metric).toBuilder();
        if (isEventNameConstant) {
            if (customEventName != null) {
                metricBuilder.setConstantEventName(customEventName);
            } else {
                metricBuilder.clearConstantEventName();
            }
        } else if (customEventName != null) {
            metricBuilder.setCustomEventName(customEventName);
        } else {
            metricBuilder.clearCustomEventName();
        }
        if (metricExtension != null) {
            metricBuilder.setMetricExtension(metricExtension);
        }
        if (accountableComponentName != null) {
            metricBuilder.setAccountableComponent(SystemHealthProto.AccountableComponent.newBuilder().setCustomName(accountableComponentName));
        }
        this.metricTransmitter.send((SystemHealthProto.SystemHealthMetric) metricBuilder.build());
        this.instrumentationSampling.incrementSampleCount();
    }
}
