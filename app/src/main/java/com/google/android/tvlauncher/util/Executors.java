package com.google.android.tvlauncher.util;

import android.os.AsyncTask;
import java.util.concurrent.Executor;

public final class Executors {
    private static Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;

    public static void setThreadPoolExecutorForTesting(Executor executor2) {
        executor = executor2;
    }

    public static Executor getThreadPoolExecutor() {
        return executor;
    }

    private Executors() {
    }
}
