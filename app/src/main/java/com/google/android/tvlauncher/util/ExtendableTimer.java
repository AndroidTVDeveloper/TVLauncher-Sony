package com.google.android.tvlauncher.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ExtendableTimer {
    private static final boolean DEBUG = false;
    private static final int MAXIMUM_TIMEOUT_MSG = 2;
    private static final int MAX_POOL_SIZE = 10;
    private static final Object POOL_SYNC = new Object();
    private static final String TAG = "ExtendableTimer";
    private static final int TIMEOUT_MSG = 1;
    private static ExtendableTimer pool;
    private static int poolSize = 0;
    private final InternalHandler handler = new InternalHandler();

    /* renamed from: id */
    private long f163id;
    private Listener listener;
    private long maximumTimeoutMillis;
    private ExtendableTimer next;
    private boolean started;
    private long timeoutMillis;

    public interface Listener {
        void onTimerFired(ExtendableTimer extendableTimer);
    }

    static ExtendableTimer getPool() {
        return pool;
    }

    static int getPoolSize() {
        return poolSize;
    }

    static void clearPool() {
        pool = null;
        poolSize = 0;
    }

    public static ExtendableTimer obtain() {
        synchronized (POOL_SYNC) {
            if (pool == null) {
                return new ExtendableTimer();
            }
            ExtendableTimer t = pool;
            pool = t.next;
            t.next = null;
            poolSize--;
            return t;
        }
    }

    public void recycle() {
        synchronized (POOL_SYNC) {
            if (poolSize < 10 && pool != this && this.next == null) {
                stopTimers();
                resetFields();
                this.next = pool;
                pool = this;
                poolSize++;
            }
        }
    }

    private void resetFields() {
        this.f163id = 0;
        this.timeoutMillis = 0;
        this.maximumTimeoutMillis = 0;
        this.listener = null;
        this.started = false;
    }

    public void start() {
        checkRequiredFields();
        if (this.started) {
            this.handler.removeMessages(1);
            this.handler.sendEmptyMessageDelayed(1, this.timeoutMillis);
            return;
        }
        this.started = true;
        this.handler.sendEmptyMessageDelayed(1, this.timeoutMillis);
        this.handler.sendEmptyMessageDelayed(2, this.maximumTimeoutMillis);
    }

    public void cancel() {
        stopTimers();
    }

    private void checkRequiredFields() {
        long j = this.timeoutMillis;
        if (j > 0) {
            long j2 = this.maximumTimeoutMillis;
            if (j2 > 0) {
                if (j2 <= j) {
                    throw new IllegalArgumentException("Maximum timeout must be larger than timeout");
                } else if (this.listener == null) {
                    throw new IllegalArgumentException("Listener must not be null");
                } else {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Both timeout and maximum timeout must be provided");
    }

    private void stopTimers() {
        if (this.started) {
            this.handler.removeMessages(1);
            this.handler.removeMessages(2);
            this.started = false;
        }
    }

    public void fireTimer() {
        stopTimers();
        this.listener.onTimerFired(this);
    }

    public long getId() {
        return this.f163id;
    }

    public void setId(long id) {
        this.f163id = id;
    }

    /* access modifiers changed from: package-private */
    public long getTimeout() {
        return this.timeoutMillis;
    }

    public void setTimeout(long timeoutMillis2) {
        this.timeoutMillis = timeoutMillis2;
    }

    /* access modifiers changed from: package-private */
    public long getMaximumTimeout() {
        return this.maximumTimeoutMillis;
    }

    public void setMaximumTimeout(long timeoutMillis2) {
        this.maximumTimeoutMillis = timeoutMillis2;
    }

    /* access modifiers changed from: package-private */
    public Listener getListener() {
        return this.listener;
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
        if (this.listener == null) {
            cancel();
        }
    }

    public boolean isStarted() {
        return this.started;
    }

    private class InternalHandler extends Handler {
        InternalHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            ExtendableTimer.this.fireTimer();
        }
    }
}
