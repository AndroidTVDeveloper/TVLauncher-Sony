package com.google.android.libraries.performance.primes;

import logs.proto.wireless.performance.mobile.SystemHealthProto;

final /* synthetic */ class CrashMetricService$$Lambda$0 implements Runnable {
    private final CrashMetricService arg$1;
    private final SystemHealthProto.PrimesStats.PrimesEvent arg$2;

    CrashMetricService$$Lambda$0(CrashMetricService crashMetricService, SystemHealthProto.PrimesStats.PrimesEvent primesEvent) {
        this.arg$1 = crashMetricService;
        this.arg$2 = primesEvent;
    }

    public void run() {
        this.arg$1.lambda$sendStartupCountEvent$0$CrashMetricService(this.arg$2);
    }
}
