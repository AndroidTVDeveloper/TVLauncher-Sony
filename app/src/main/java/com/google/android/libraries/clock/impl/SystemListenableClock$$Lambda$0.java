package com.google.android.libraries.clock.impl;

import com.google.android.libraries.clock.ListenableClock;
import com.google.android.libraries.clock.impl.ListenerManager;

final /* synthetic */ class SystemListenableClock$$Lambda$0 implements ListenerManager.Dispatcher {
    static final ListenerManager.Dispatcher $instance = new SystemListenableClock$$Lambda$0();

    private SystemListenableClock$$Lambda$0() {
    }

    public void dispatch(Object obj) {
        ((ListenableClock.TimeResetListener) obj).onTimeReset();
    }
}
