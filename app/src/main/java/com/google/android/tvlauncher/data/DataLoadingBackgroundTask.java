package com.google.android.tvlauncher.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataLoadingBackgroundTask implements Runnable {
    private static final int CORE_THREAD_POOL_SIZE = Math.min(CPU_COUNT - 1, 2);
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final boolean DEBUG = false;
    static final int MAX_POOL_SIZE = 15;
    private static final int MAX_THREAD_POOL_SIZE = Math.max(CPU_COUNT - 1, 1);
    private static final Object POOL_SYNC = new Object();
    private static final String TAG = "DLBackgroundTask";
    private static final int THREAD_KEEP_ALIVE_SECONDS = 5;
    private static final int WORK_QUEUE_CAPACITY = 256;
    private static final Executor defaultExecutor = new ThreadPoolExecutor(CORE_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE, 5, TimeUnit.SECONDS, workQueue);
    private static Executor executor = defaultExecutor;
    private static InternalHandler handler = new InternalHandler();
    private static DataLoadingBackgroundTask pool;
    private static int poolSize = 0;
    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue(256);
    private volatile Callbacks callbacks;
    private volatile boolean canceled;
    private volatile ContentResolver contentResolver;
    private volatile Object extraParam;
    private volatile Object extraResult;
    private DataLoadingBackgroundTask next;
    private volatile String[] projection;
    private boolean recycled;
    private volatile Cursor result;
    private volatile String selection;
    private volatile String[] selectionArgs;
    private volatile String[] singleSelectionArg;
    private volatile String sortOrder;
    private volatile long tag;
    private volatile Uri uri;

    interface Callbacks {
        void onTaskCanceled(DataLoadingBackgroundTask dataLoadingBackgroundTask);

        void onTaskCompleted(DataLoadingBackgroundTask dataLoadingBackgroundTask);

        void onTaskFailed(DataLoadingBackgroundTask dataLoadingBackgroundTask, Throwable th);

        void onTaskPostProcess(DataLoadingBackgroundTask dataLoadingBackgroundTask);
    }

    static DataLoadingBackgroundTask getPool() {
        return pool;
    }

    static int getPoolSize() {
        return poolSize;
    }

    static void clearPool() {
        pool = null;
        poolSize = 0;
    }

    static void setExecutor(Executor executor2) {
        executor = executor2;
    }

    static void resetExecutor() {
        executor = defaultExecutor;
    }

    static DataLoadingBackgroundTask obtain(Context context) {
        synchronized (POOL_SYNC) {
            if (pool == null) {
                return new DataLoadingBackgroundTask(context.getApplicationContext());
            }
            DataLoadingBackgroundTask t = pool;
            pool = t.next;
            t.next = null;
            t.recycled = false;
            poolSize--;
            return t;
        }
    }

    private DataLoadingBackgroundTask(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        synchronized (POOL_SYNC) {
            if (poolSize < 15 && pool != this && this.next == null) {
                resetFields();
                this.next = pool;
                pool = this;
                poolSize++;
            }
            this.recycled = true;
        }
    }

    private void resetFields() {
        this.canceled = false;
        this.tag = 0;
        this.callbacks = null;
        this.uri = null;
        this.projection = null;
        this.selection = null;
        this.selectionArgs = null;
        this.sortOrder = null;
        this.extraParam = null;
        this.result = null;
        this.extraResult = null;
    }

    /* access modifiers changed from: package-private */
    public void execute() {
        checkRequiredFields();
        try {
            executor.execute(this);
        } catch (RejectedExecutionException ex) {
            this.callbacks.onTaskFailed(this, ex);
            recycle();
        }
    }

    private void checkRequiredFields() {
        if (this.recycled) {
            throw new IllegalStateException("Can't execute after been recycled. Use DataLoadingBackgroundTask.obtain(Context) to get a new task");
        } else if (this.callbacks == null) {
            throw new IllegalArgumentException("Callbacks must not be null");
        } else if (this.uri == null) {
            throw new IllegalArgumentException("Uri must not be null");
        }
    }

    /* access modifiers changed from: package-private */
    public void cancel() {
        this.canceled = true;
    }

    public void run() {
        Process.setThreadPriority(10);
        if (isCanceled()) {
            postFinish();
            return;
        }
        try {
            performQuery();
            Binder.flushPendingCommands();
            if (isCanceled()) {
                postFinish();
                return;
            }
            this.callbacks.onTaskPostProcess(this);
            postFinish();
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while executing ContentResolver query", t);
        }
    }

    private void postFinish() {
        handler.obtainMessage(0, this).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void finish() {
        if (isCanceled()) {
            this.callbacks.onTaskCanceled(this);
        } else {
            this.callbacks.onTaskCompleted(this);
        }
        recycle();
    }

    private void performQuery() {
        this.result = this.contentResolver.query(this.uri, this.projection, this.selection, this.selectionArgs, this.sortOrder);
        if (this.result != null) {
            this.result.getCount();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCanceled() {
        return this.canceled;
    }

    /* access modifiers changed from: package-private */
    public long getTag() {
        return this.tag;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setTag(long tag2) {
        this.tag = tag2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Callbacks getCallbacks() {
        return this.callbacks;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setCallbacks(Callbacks callbacks2) {
        if (callbacks2 != null) {
            this.callbacks = callbacks2;
            return this;
        }
        throw new IllegalArgumentException("Callbacks must not be null");
    }

    /* access modifiers changed from: package-private */
    public Uri getUri() {
        return this.uri;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setUri(Uri uri2) {
        if (uri2 != null) {
            this.uri = uri2;
            return this;
        }
        throw new IllegalArgumentException("Uri must not be null");
    }

    /* access modifiers changed from: package-private */
    public String[] getProjection() {
        return this.projection;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setProjection(String[] projection2) {
        this.projection = projection2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public String getSelection() {
        return this.selection;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setSelection(String selection2) {
        this.selection = selection2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public String[] getSelectionArgs() {
        return this.selectionArgs;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setSelectionArgs(String[] args) {
        this.selectionArgs = args;
        return this;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setSelectionArg(String arg) {
        if (this.singleSelectionArg == null) {
            this.singleSelectionArg = new String[1];
        }
        this.singleSelectionArg[0] = arg;
        this.selectionArgs = this.singleSelectionArg;
        return this;
    }

    /* access modifiers changed from: package-private */
    public String getSortOrder() {
        return this.sortOrder;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setSortOrder(String sortOrder2) {
        this.sortOrder = sortOrder2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Object getExtraParam() {
        return this.extraParam;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setExtraParam(Object extraParam2) {
        this.extraParam = extraParam2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Cursor getResult() {
        return this.result;
    }

    /* access modifiers changed from: package-private */
    public Object getExtraResult() {
        return this.extraResult;
    }

    /* access modifiers changed from: package-private */
    public DataLoadingBackgroundTask setExtraResult(Object extra) {
        this.extraResult = extra;
        return this;
    }

    public String toString() {
        long j = this.tag;
        String valueOf = String.valueOf(this.uri);
        String str = this.selection;
        String arrays = Arrays.toString(this.selectionArgs);
        String str2 = this.sortOrder;
        StringBuilder sb = new StringBuilder(valueOf.length() + 101 + String.valueOf(str).length() + arrays.length() + String.valueOf(str2).length());
        sb.append("DataLoadingBackgroundTask{tag=");
        sb.append(j);
        sb.append(", uri=");
        sb.append(valueOf);
        sb.append(", selection='");
        sb.append(str);
        sb.append('\'');
        sb.append(", selectionArgs=");
        sb.append(arrays);
        sb.append(", sortOrder='");
        sb.append(str2);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }

    private static class InternalHandler extends Handler {
        InternalHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            ((DataLoadingBackgroundTask) msg.obj).finish();
        }
    }
}
