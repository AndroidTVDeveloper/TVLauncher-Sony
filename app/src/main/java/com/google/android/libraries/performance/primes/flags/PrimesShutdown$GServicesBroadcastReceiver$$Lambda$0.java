package com.google.android.libraries.performance.primes.flags;

import com.google.android.libraries.performance.primes.flags.PrimesShutdown;

final /* synthetic */ class PrimesShutdown$GServicesBroadcastReceiver$$Lambda$0 implements Runnable {
    private final PrimesShutdown.GServicesBroadcastReceiver arg$1;

    PrimesShutdown$GServicesBroadcastReceiver$$Lambda$0(PrimesShutdown.GServicesBroadcastReceiver gServicesBroadcastReceiver) {
        this.arg$1 = gServicesBroadcastReceiver;
    }

    public void run() {
        this.arg$1.lambda$onReceive$0$PrimesShutdown$GServicesBroadcastReceiver();
    }
}
