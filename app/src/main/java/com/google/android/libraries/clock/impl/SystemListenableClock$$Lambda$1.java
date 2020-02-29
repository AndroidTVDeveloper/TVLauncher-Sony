package com.google.android.libraries.clock.impl;

import com.google.android.libraries.clock.ListenableClock;
import com.google.android.libraries.clock.impl.ListenerManager;

final /* synthetic */ class SystemListenableClock$$Lambda$1 implements ListenerManager.Dispatcher {
    static final ListenerManager.Dispatcher $instance = new SystemListenableClock$$Lambda$1();

    private SystemListenableClock$$Lambda$1() {
    }

    public void dispatch(Object obj) {
        ((ListenableClock.TimeTickListener) obj).onTimeTick();
    }
}
