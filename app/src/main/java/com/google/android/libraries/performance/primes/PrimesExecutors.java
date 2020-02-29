package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.os.Process;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.PrimesListeningScheduledExecutorService;
import com.google.android.libraries.performance.primes.PrimesThreadsConfigurations;
import com.google.android.libraries.performance.primes.Supplier;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimesExecutors {
    private static final String TAG = "PrimesExecutors";

    private PrimesExecutors() {
    }

    static Supplier<ListeningScheduledExecutorService> newPrimesExecutorSupplier(PrimesThreadsConfigurations threadsConfigurations) {
        return new Supplier.Lazy(new PrimesExecutors$$Lambda$0(threadsConfigurations.getPrimesExecutorService(), threadsConfigurations.getPrimesMetricExecutorPriority(), threadsConfigurations.getPrimesMetricExecutorPoolSize()));
    }

    static final /* synthetic */ ListeningScheduledExecutorService lambda$newPrimesExecutorSupplier$0$PrimesExecutors(ListeningScheduledExecutorService executorService, int priority, int poolSize) {
        ListeningScheduledExecutorService listeningScheduledExecutorService;
        if (executorService != null) {
            listeningScheduledExecutorService = executorService;
        } else {
            listeningScheduledExecutorService = newDefaultExecutor(priority, poolSize);
        }
        return wrap(listeningScheduledExecutorService);
    }

    private static final class OnResumeListener implements Executor, AppLifecycleListener.OnActivityResumed {
        private volatile Activity activity;
        private final PrimesThreadsConfigurations.ActivityResumedCallback activityResumedCallback;
        private boolean done;
        private final AppLifecycleMonitor lifecycleMonitor;
        private boolean resumed;
        private Runnable task;

        OnResumeListener(AppLifecycleMonitor lifecycleMonitor2, PrimesThreadsConfigurations.ActivityResumedCallback activityResumedCallback2) {
            this.lifecycleMonitor = lifecycleMonitor2;
            this.activityResumedCallback = activityResumedCallback2;
        }

        public void onActivityResumed(Activity activity2) {
            this.lifecycleMonitor.unregister(this);
            synchronized (this) {
                this.activity = activity2;
                if (this.task != null) {
                    runTask(this.task);
                    this.task = null;
                } else {
                    this.resumed = true;
                }
            }
        }

        public void execute(Runnable task2) {
            synchronized (this) {
                if (!this.resumed) {
                    if (this.lifecycleMonitor.getActivityResumedCount() <= 0) {
                        this.task = task2;
                    }
                }
                runTask(task2);
            }
        }

        private void runTask(Runnable task2) {
            if (!this.done) {
                this.done = true;
                PrimesThreadsConfigurations.ActivityResumedCallback activityResumedCallback2 = this.activityResumedCallback;
                if (activityResumedCallback2 == null) {
                    task2.run();
                } else {
                    activityResumedCallback2.onActivityResumed(this.activity, task2);
                }
            }
        }
    }

    static Executor onActivityResumedTrigger(AppLifecycleMonitor lifecycleMonitor, PrimesThreadsConfigurations.ActivityResumedCallback activityResumedCallback) {
        OnResumeListener listener = new OnResumeListener(lifecycleMonitor, activityResumedCallback);
        lifecycleMonitor.register(listener);
        return listener;
    }

    static ExecutorService initExecutor(PrimesThreadsConfigurations threadsConfigurations) {
        if (threadsConfigurations.getPrimesExecutorService() != null) {
            return threadsConfigurations.getPrimesExecutorService();
        }
        return newInitExecutor(threadsConfigurations.getPrimesInitializationPriority());
    }

    private static ExecutorService newInitExecutor(int priority) {
        return Executors.newSingleThreadExecutor(new PrimesThreadFactory("Primes-init", priority));
    }

    private static ListeningScheduledExecutorService newDefaultExecutor(int priority, int poolSize) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize, new PrimesThreadFactory(priority), new DefaultRejectedExecutionHandler());
        executor.setMaximumPoolSize(poolSize);
        return MoreExecutors.listeningDecorator((ScheduledExecutorService) executor);
    }

    private static ListeningScheduledExecutorService wrap(ListeningScheduledExecutorService serviceToWrap) {
        return new PrimesListeningScheduledExecutorService(serviceToWrap, new DefaultFailureCallback());
    }

    public static void handleFuture(Future<?> future) {
    }

    public static ExecutorService newBackgroundJobExecutor() {
        return Executors.newSingleThreadExecutor(new PrimesThreadFactory("Primes-backgroundJob", 1));
    }

    private static final class DefaultFailureCallback implements PrimesListeningScheduledExecutorService.FailureCallback {
        private DefaultFailureCallback() {
        }

        public void onFailure(Throwable t) {
            PrimesLog.m55w(PrimesExecutors.TAG, "Background task failed", t);
        }
    }

    private static final class DefaultRejectedExecutionHandler implements RejectedExecutionHandler {
        private DefaultRejectedExecutionHandler() {
        }

        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            String valueOf = String.valueOf(runnable);
            StringBuilder sb = new StringBuilder(valueOf.length() + 30);
            sb.append("Service rejected execution of ");
            sb.append(valueOf);
            PrimesLog.m48d(PrimesExecutors.TAG, sb.toString());
        }
    }

    private static final class PrimesThreadFactory implements ThreadFactory {
        private final AtomicInteger count;
        private final String prefix;
        private final int priority;

        PrimesThreadFactory(int priority2) {
            this("Primes", priority2);
        }

        PrimesThreadFactory(String prefix2, int priority2) {
            this.count = new AtomicInteger(1);
            this.priority = priority2;
            this.prefix = prefix2;
        }

        public Thread newThread(Runnable runnable) {
            PrimesExecutors$PrimesThreadFactory$$Lambda$0 primesExecutors$PrimesThreadFactory$$Lambda$0 = new PrimesExecutors$PrimesThreadFactory$$Lambda$0(this, runnable);
            String str = this.prefix;
            int andIncrement = this.count.getAndIncrement();
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 12);
            sb.append(str);
            sb.append("-");
            sb.append(andIncrement);
            Thread newThread = new Thread(primesExecutors$PrimesThreadFactory$$Lambda$0, sb.toString());
            if (newThread.isDaemon()) {
                newThread.setDaemon(false);
            }
            return newThread;
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$newThread$0$PrimesExecutors$PrimesThreadFactory(Runnable runnable) {
            int i = this.priority;
            if (i != 0) {
                Process.setThreadPriority(i);
            }
            runnable.run();
        }
    }
}
