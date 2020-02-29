package com.google.android.libraries.performance.primes;

import android.app.Application;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.io.Serializable;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.PrimesTraceOuterClass;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

class TraceMetricRecordingService extends AbstractMetricService {
    private static final String TAG = "BaseTraceMetricService";

    static synchronized TraceMetricRecordingService createService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier) {
        TraceMetricRecordingService traceMetricRecordingService;
        synchronized (TraceMetricRecordingService.class) {
            traceMetricRecordingService = new TraceMetricRecordingService(transmitter, application, metricStamperSupplier, executorServiceSupplier);
        }
        return traceMetricRecordingService;
    }

    TraceMetricRecordingService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, MetricRecorder.RunIn.BACKGROUND_THREAD);
    }

    /* access modifiers changed from: package-private */
    public void record(PrimesTraceOuterClass.PrimesTrace trace, ExtensionMetric.MetricExtension metricExtension, String accountableComponentName) {
        SystemHealthProto.SystemHealthMetric message = (SystemHealthProto.SystemHealthMetric) SystemHealthProto.SystemHealthMetric.newBuilder().setPrimesTrace(trace).build();
        Serializable[] serializableArr = new Serializable[2];
        serializableArr[0] = trace.hasTraceId() ? Long.valueOf(trace.getTraceId()) : null;
        serializableArr[1] = trace.getSpans(0).getConstantName();
        PrimesLog.m48d(TAG, "Recording trace %d: %s", serializableArr);
        recordSystemHealthMetric(message, metricExtension, accountableComponentName);
    }

    /* access modifiers changed from: package-private */
    public void shutdownService() {
    }
}
