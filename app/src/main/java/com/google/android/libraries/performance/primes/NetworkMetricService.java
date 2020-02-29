package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class NetworkMetricService extends AbstractMetricService implements AppLifecycleListener.OnAppToBackground {
    private static final String TAG = "NetworkMetricService";
    private final int batchSize;
    private final List<NetworkEvent> batchedMetric;
    private final Object lock = new Object();
    private final NetworkMetricCollector metricCollector;
    private final AtomicInteger pendingRecords;

    static NetworkMetricService createService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, PrimesNetworkConfigurations networkConfigurations, boolean enableAutoSanitization) {
        return createService(transmitter, application, metricStamperSupplier, executorServiceSupplier, networkConfigurations, enableAutoSanitization, MetricRecorder.RunIn.SAME_THREAD);
    }

    static NetworkMetricService createService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, PrimesNetworkConfigurations configs, boolean enableAutoSanitization, MetricRecorder.RunIn runIn) {
        return new NetworkMetricService(transmitter, application, metricStamperSupplier, executorServiceSupplier, runIn, Integer.MAX_VALUE, configs.batchSize(), enableAutoSanitization, configs.getUrlSanitizer(), configs.getMetricExtensionProvider());
    }

    NetworkMetricService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, MetricRecorder.RunIn runIn, int sampleRate, int batchSize2, boolean enableAutoSanitization, UrlSanitizer urlSanitizer, Optional<NetworkMetricExtensionProvider> metricExtensionProvider) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, runIn, sampleRate);
        this.batchSize = batchSize2;
        this.batchedMetric = new ArrayList(batchSize2);
        this.metricCollector = new NetworkMetricCollector(enableAutoSanitization, urlSanitizer, metricExtensionProvider);
        this.pendingRecords = new AtomicInteger();
        AppLifecycleMonitor.getInstance(getApplication()).register(this);
    }

    public void onAppToBackground(Activity activity) {
        sendPendingEvents();
    }

    /* access modifiers changed from: package-private */
    public void recordEvent(NetworkEvent event) {
        if (!shouldRecord()) {
            return;
        }
        if (!event.isReadyToRecord()) {
            PrimesLog.m56w(TAG, "skip logging NetworkEvent due to empty bandwidth/latency data");
            return;
        }
        this.pendingRecords.incrementAndGet();
        PrimesExecutors.handleFuture(getListeningScheduledExecutorService().submit((Runnable) new NetworkMetricService$$Lambda$0(this, event)));
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$recordEvent$0$NetworkMetricService(NetworkEvent event) {
        try {
            doRecordNetwork(event);
        } finally {
            this.pendingRecords.decrementAndGet();
        }
    }

    private void doRecordNetwork(NetworkEvent event) {
        event.onRecord(getApplication());
        NetworkEvent[] batchToRecord = null;
        synchronized (this.lock) {
            this.batchedMetric.add(event);
            if (this.batchedMetric.size() >= this.batchSize) {
                batchToRecord = (NetworkEvent[]) this.batchedMetric.toArray(new NetworkEvent[this.batchedMetric.size()]);
                this.batchedMetric.clear();
            }
        }
        if (batchToRecord != null) {
            recordSystemHealthMetric(this.metricCollector.getMetric(batchToRecord));
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
     arg types: [com.google.android.libraries.performance.primes.NetworkMetricService$$Lambda$1, int, java.util.concurrent.TimeUnit]
     candidates:
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
      ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
    /* access modifiers changed from: package-private */
    public void sendPendingEvents() {
        if (this.pendingRecords.get() > 0) {
            PrimesExecutors.handleFuture(getListeningScheduledExecutorService().schedule((Runnable) new NetworkMetricService$$Lambda$1(this), 1L, TimeUnit.SECONDS));
            return;
        }
        NetworkEvent[] batchToRecord = null;
        synchronized (this.lock) {
            if (!this.batchedMetric.isEmpty()) {
                batchToRecord = (NetworkEvent[]) this.batchedMetric.toArray(new NetworkEvent[this.batchedMetric.size()]);
                this.batchedMetric.clear();
            }
        }
        if (batchToRecord != null) {
            PrimesExecutors.handleFuture(getListeningScheduledExecutorService().submit((Runnable) new NetworkMetricService$$Lambda$2(this, batchToRecord)));
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$sendPendingEvents$1$NetworkMetricService(NetworkEvent[] batch) {
        recordSystemHealthMetric(this.metricCollector.getMetric(batch));
    }

    /* access modifiers changed from: package-private */
    public void shutdownService() {
        AppLifecycleMonitor.getInstance(getApplication()).unregister(this);
        synchronized (this.lock) {
            this.batchedMetric.clear();
        }
    }
}
