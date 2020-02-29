package com.google.android.libraries.performance.primes;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

final /* synthetic */ class PrimesExecutors$$Lambda$0 implements Supplier {
    private final ListeningScheduledExecutorService arg$1;
    private final int arg$2;
    private final int arg$3;

    PrimesExecutors$$Lambda$0(ListeningScheduledExecutorService listeningScheduledExecutorService, int i, int i2) {
        this.arg$1 = listeningScheduledExecutorService;
        this.arg$2 = i;
        this.arg$3 = i2;
    }

    public Object get() {
        return PrimesExecutors.lambda$newPrimesExecutorSupplier$0$PrimesExecutors(this.arg$1, this.arg$2, this.arg$3);
    }
}
