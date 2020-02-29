package com.google.android.libraries.performance.primes;

import com.google.android.libraries.stitch.util.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class PrimesListeningScheduledExecutorService implements ListeningScheduledExecutorService {
    private final ListeningScheduledExecutorService executor;
    /* access modifiers changed from: private */
    public final FailureCallback failureCallback;

    interface FailureCallback {
        void onFailure(Throwable th);
    }

    PrimesListeningScheduledExecutorService(ListeningScheduledExecutorService listeningScheduledExecutorService, FailureCallback failureCallback2) {
        this.executor = (ListeningScheduledExecutorService) Preconditions.checkNotNull(listeningScheduledExecutorService);
        this.failureCallback = (FailureCallback) Preconditions.checkNotNull(failureCallback2);
    }

    private Runnable wrap(Runnable runnable) {
        return new PrimesListeningScheduledExecutorService$$Lambda$0(this, runnable);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$wrap$0$PrimesListeningScheduledExecutorService(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            this.failureCallback.onFailure(t);
            throw t;
        }
    }

    private <V> Callable<V> wrap(final Callable<V> callable) {
        return new Callable<V>() {
            public V call() throws Exception {
                try {
                    return callable.call();
                } catch (Throwable t) {
                    PrimesListeningScheduledExecutorService.this.failureCallback.onFailure(t);
                    throw t;
                }
            }
        };
    }

    private <V> List<? extends Callable<V>> wrapAll(Collection<? extends Callable<V>> tasks) {
        List<Callable<V>> ret = new ArrayList<>();
        for (Callable<V> callable : tasks) {
            ret.add(wrap(callable));
        }
        return ret;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
     arg types: [java.lang.Runnable, long, java.util.concurrent.TimeUnit]
     candidates:
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
      ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
    public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return this.executor.schedule(wrap(command), delay, unit);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
     arg types: [java.util.concurrent.Callable<V>, long, java.util.concurrent.TimeUnit]
     candidates:
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
      ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V> */
    public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return this.executor.schedule((Callable) wrap(callable), delay, unit);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
     arg types: [java.lang.Runnable, long, long, java.util.concurrent.TimeUnit]
     candidates:
      ClspMth{java.util.concurrent.ScheduledExecutorService.scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
    public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return this.executor.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
     arg types: [java.lang.Runnable, long, long, java.util.concurrent.TimeUnit]
     candidates:
      ClspMth{java.util.concurrent.ScheduledExecutorService.scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
    public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return this.executor.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }

    public void execute(Runnable command) {
        this.executor.execute(wrap(command));
    }

    public ListenableFuture<?> submit(Runnable task) {
        return this.executor.submit(wrap(task));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningExecutorService.submit(java.lang.Runnable, java.lang.Object):com.google.common.util.concurrent.ListenableFuture<T>
     arg types: [java.lang.Runnable, V]
     candidates:
      ClspMth{<T> java.util.concurrent.ExecutorService.submit(java.lang.Runnable, java.lang.Object):java.util.concurrent.Future<T>}
      ClspMth{<T> java.util.concurrent.ExecutorService.submit(java.lang.Runnable, java.lang.Object):java.util.concurrent.Future<T>}
      com.google.common.util.concurrent.ListeningExecutorService.submit(java.lang.Runnable, java.lang.Object):com.google.common.util.concurrent.ListenableFuture<T> */
    public <V> ListenableFuture<V> submit(Runnable task, V result) {
        return this.executor.submit(wrap(task), (Object) result);
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [java.util.concurrent.Callable, java.util.concurrent.Callable<V>] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <V> com.google.common.util.concurrent.ListenableFuture<V> submit(java.util.concurrent.Callable<V> r3) {
        /*
            r2 = this;
            com.google.common.util.concurrent.ListeningScheduledExecutorService r0 = r2.executor
            java.util.concurrent.Callable r1 = r2.wrap(r3)
            com.google.common.util.concurrent.ListenableFuture r0 = r0.submit(r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.performance.primes.PrimesListeningScheduledExecutorService.submit(java.util.concurrent.Callable):com.google.common.util.concurrent.ListenableFuture");
    }

    public void shutdown() {
        this.executor.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return this.executor.shutdownNow();
    }

    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.executor.awaitTermination(timeout, unit);
    }

    public <V> V invokeAny(Collection<? extends Callable<V>> tasks) throws InterruptedException, ExecutionException {
        return this.executor.invokeAny(wrapAll(tasks));
    }

    public <V> V invokeAny(Collection<? extends Callable<V>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.executor.invokeAny(wrapAll(tasks), timeout, unit);
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [java.util.Collection<? extends java.util.concurrent.Callable<V>>, java.util.Collection] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <V> java.util.List<java.util.concurrent.Future<V>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<V>> r3) throws java.lang.InterruptedException {
        /*
            r2 = this;
            com.google.common.util.concurrent.ListeningScheduledExecutorService r0 = r2.executor
            java.util.List r1 = r2.wrapAll(r3)
            java.util.List r0 = r0.invokeAll(r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.performance.primes.PrimesListeningScheduledExecutorService.invokeAll(java.util.Collection):java.util.List");
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [java.util.Collection<? extends java.util.concurrent.Callable<V>>, java.util.Collection] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <V> java.util.List<java.util.concurrent.Future<V>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<V>> r3, long r4, java.util.concurrent.TimeUnit r6) throws java.lang.InterruptedException {
        /*
            r2 = this;
            com.google.common.util.concurrent.ListeningScheduledExecutorService r0 = r2.executor
            java.util.List r1 = r2.wrapAll(r3)
            java.util.List r0 = r0.invokeAll(r1, r4, r6)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.performance.primes.PrimesListeningScheduledExecutorService.invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit):java.util.List");
    }
}
