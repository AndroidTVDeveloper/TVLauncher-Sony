package com.google.android.libraries.clock.impl;

import android.content.Context;
import com.google.android.libraries.clock.Clock;
import com.google.android.libraries.clock.ListenableClock;
import javax.annotation.Nullable;

public final class SystemListenableClock implements ListenableClock {
    private final Clock delegate = new SystemClockImpl();
    private final Context mContext;
    @Nullable
    private ListenerManager<ListenableClock.TimeResetListener> mListenManagerTimeReset;
    @Nullable
    private ListenerManager<ListenableClock.TimeTickListener> mListenManagerTimeTick;

    public SystemListenableClock(Context context) {
        this.mContext = context;
    }

    public void registerTimeResetListener(ListenableClock.TimeResetListener listener) {
        if (this.mListenManagerTimeReset == null) {
            this.mListenManagerTimeReset = new ListenerManager<>(this.mContext, "android.intent.action.TIME_SET", SystemListenableClock$$Lambda$0.$instance);
        }
        this.mListenManagerTimeReset.registerListener(listener);
    }

    public void unregisterTimeResetListener(ListenableClock.TimeResetListener listener) {
        ListenerManager<ListenableClock.TimeResetListener> listenerManager = this.mListenManagerTimeReset;
        if (listenerManager != null) {
            listenerManager.unRegisterListener(listener);
            if (this.mListenManagerTimeReset.isEmpty()) {
                this.mListenManagerTimeReset = null;
            }
        }
    }

    public void registerTimeTickListener(ListenableClock.TimeTickListener listener) {
        if (this.mListenManagerTimeTick == null) {
            this.mListenManagerTimeTick = new ListenerManager<>(this.mContext, "android.intent.action.TIME_TICK", SystemListenableClock$$Lambda$1.$instance);
        }
        this.mListenManagerTimeTick.registerListener(listener);
    }

    public void unregisterTimeTickListener(ListenableClock.TimeTickListener listener) {
        ListenerManager<ListenableClock.TimeTickListener> listenerManager = this.mListenManagerTimeTick;
        if (listenerManager != null) {
            listenerManager.unRegisterListener(listener);
            if (this.mListenManagerTimeTick.isEmpty()) {
                this.mListenManagerTimeTick = null;
            }
        }
    }

    public long currentTimeMillis() {
        return this.delegate.currentTimeMillis();
    }

    public long nanoTime() {
        return this.delegate.nanoTime();
    }

    public long currentThreadTimeMillis() {
        return this.delegate.currentThreadTimeMillis();
    }

    public long elapsedRealtime() {
        return this.delegate.elapsedRealtime();
    }

    public long elapsedRealtimeNanos() {
        return this.delegate.elapsedRealtimeNanos();
    }

    public long uptimeMillis() {
        return this.delegate.uptimeMillis();
    }
}
