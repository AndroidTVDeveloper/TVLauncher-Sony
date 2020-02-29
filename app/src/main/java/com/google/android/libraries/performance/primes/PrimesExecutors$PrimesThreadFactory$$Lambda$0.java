package com.google.android.libraries.performance.primes;

import com.google.android.libraries.performance.primes.PrimesExecutors;

final /* synthetic */ class PrimesExecutors$PrimesThreadFactory$$Lambda$0 implements Runnable {
    private final PrimesExecutors.PrimesThreadFactory arg$1;
    private final Runnable arg$2;

    PrimesExecutors$PrimesThreadFactory$$Lambda$0(PrimesExecutors.PrimesThreadFactory primesThreadFactory, Runnable runnable) {
        this.arg$1 = primesThreadFactory;
        this.arg$2 = runnable;
    }

    public void run() {
        this.arg$1.lambda$newThread$0$PrimesExecutors$PrimesThreadFactory(this.arg$2);
    }
}
