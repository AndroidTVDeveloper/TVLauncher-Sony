package com.google.android.libraries.performance.primes;

import com.google.android.libraries.performance.primes.MemoryMetricMonitor;
import logs.proto.wireless.performance.mobile.MemoryMetric;

final /* synthetic */ class MemoryMetricService$$Lambda$0 implements MemoryMetricMonitor.Callback {
    private final MemoryMetricService arg$1;

    MemoryMetricService$$Lambda$0(MemoryMetricService memoryMetricService) {
        this.arg$1 = memoryMetricService;
    }

    public void onEvent(MemoryMetric.MemoryUsageMetric.MemoryEventCode memoryEventCode, String str) {
        this.arg$1.lambda$startMonitoring$0$MemoryMetricService(memoryEventCode, str);
    }
}
