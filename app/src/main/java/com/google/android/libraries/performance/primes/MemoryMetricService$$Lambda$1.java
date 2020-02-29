package com.google.android.libraries.performance.primes;

import java.util.concurrent.Callable;

final /* synthetic */ class MemoryMetricService$$Lambda$1 implements Callable {
    private final MemoryMetricService arg$1;

    MemoryMetricService$$Lambda$1(MemoryMetricService memoryMetricService) {
        this.arg$1 = memoryMetricService;
    }

    public Object call() {
        return this.arg$1.lambda$scheduleSnapshot$1$MemoryMetricService();
    }
}
