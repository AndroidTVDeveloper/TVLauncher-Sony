package com.google.android.libraries.performance.primes;

final /* synthetic */ class PrimesListeningScheduledExecutorService$$Lambda$0 implements Runnable {
    private final PrimesListeningScheduledExecutorService arg$1;
    private final Runnable arg$2;

    PrimesListeningScheduledExecutorService$$Lambda$0(PrimesListeningScheduledExecutorService primesListeningScheduledExecutorService, Runnable runnable) {
        this.arg$1 = primesListeningScheduledExecutorService;
        this.arg$2 = runnable;
    }

    public void run() {
        this.arg$1.lambda$wrap$0$PrimesListeningScheduledExecutorService(this.arg$2);
    }
}
