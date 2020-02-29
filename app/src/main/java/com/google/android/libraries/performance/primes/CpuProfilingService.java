package com.google.android.libraries.performance.primes;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.support.p001v4.content.ContextCompat;
import com.google.android.libraries.clock.Clock;
import com.google.android.libraries.clock.impl.SystemClockImpl;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.metriccapture.ProcessStats;
import com.google.android.libraries.performance.primes.sampling.SamplingUtils;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import logs.proto.wireless.performance.mobile.CpuProfiling;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

final class CpuProfilingService extends AbstractMetricService implements PrimesStartupListener {
    private static final String TAG = "CpuProfilingService";
    static final String TRACE_DIR_PREFIX = "primes_profiling_";
    /* access modifiers changed from: private */
    public final IntentFilter batteryIntentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
    /* access modifiers changed from: private */
    public final Clock clock;
    /* access modifiers changed from: private */
    public final int frequencyMicro;
    /* access modifiers changed from: private */
    public final int maxBufferSizeBytes;
    /* access modifiers changed from: private */
    public final int sampleDurationMs;
    /* access modifiers changed from: private */
    public final int sampleDurationSkewMs;
    /* access modifiers changed from: private */
    public final double samplesPerEpoch;
    /* access modifiers changed from: private */
    public final AtomicBoolean scheduled = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public ScheduledFuture<?> scheduledFutureCollectCpuUsage;
    private final CpuProfilingServiceScheduler scheduler;
    private WifiManager wifi;

    static CpuProfilingService createService(MetricTransmitter metricTransmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, PrimesProfilingConfigurations config) {
        return new CpuProfilingService(metricTransmitter, application, metricStamperSupplier, executorServiceSupplier, config.getMaxBufferSizeBytes(), config.getSampleFrequencyMicro(), config.getSampleDurationMs(), config.getSampleDurationSkewMs(), config.getSamplesPerEpoch(), new SystemClockImpl());
    }

    CpuProfilingService(MetricTransmitter transmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, int maxBufferSizeBytes2, int frequencyMicro2, int sampleDurationMs2, int sampleDurationSkewMs2, double samplesPerEpoch2, Clock clock2) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, MetricRecorder.RunIn.BACKGROUND_THREAD);
        this.maxBufferSizeBytes = maxBufferSizeBytes2;
        this.frequencyMicro = frequencyMicro2;
        this.sampleDurationMs = sampleDurationMs2;
        this.sampleDurationSkewMs = sampleDurationSkewMs2;
        this.samplesPerEpoch = samplesPerEpoch2;
        this.clock = clock2;
        this.scheduler = new CpuProfilingServiceScheduler(clock2, samplesPerEpoch2, sampleDurationMs2, ProcessStats.getCurrentProcessName(), getApplication());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
     arg types: [com.google.android.libraries.performance.primes.CpuProfilingService$CpuCollectionStartTask, long, java.util.concurrent.TimeUnit]
     candidates:
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
      ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
      ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
      com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void scheduleNextMonitoringWindow() {
        /*
            r9 = this;
            monitor-enter(r9)
            java.util.concurrent.atomic.AtomicBoolean r0 = r9.scheduled     // Catch:{ all -> 0x0050 }
            boolean r0 = r0.get()     // Catch:{ all -> 0x0050 }
            if (r0 == 0) goto L_0x000b
            monitor-exit(r9)
            return
        L_0x000b:
            r0 = 0
        L_0x000c:
            r1 = 5
            if (r0 >= r1) goto L_0x004e
            com.google.android.libraries.performance.primes.CpuProfilingServiceScheduler r1 = r9.scheduler     // Catch:{ all -> 0x0050 }
            java.lang.Long r1 = r1.getNextWindow()     // Catch:{ all -> 0x0050 }
            if (r1 != 0) goto L_0x0019
            monitor-exit(r9)
            return
        L_0x0019:
            long r2 = r1.longValue()     // Catch:{ all -> 0x0050 }
            com.google.android.libraries.clock.Clock r4 = r9.clock     // Catch:{ all -> 0x0050 }
            long r4 = r4.currentTimeMillis()     // Catch:{ all -> 0x0050 }
            long r2 = r2 - r4
            r4 = 0
            int r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r6 > 0) goto L_0x002e
            int r0 = r0 + 1
            goto L_0x000c
        L_0x002e:
            long r4 = r1.longValue()     // Catch:{ all -> 0x0050 }
            int r6 = r9.sampleDurationMs     // Catch:{ all -> 0x0050 }
            long r6 = (long) r6     // Catch:{ all -> 0x0050 }
            long r4 = r4 + r6
            java.util.concurrent.atomic.AtomicBoolean r6 = r9.scheduled     // Catch:{ all -> 0x0050 }
            r7 = 1
            r6.set(r7)     // Catch:{ all -> 0x0050 }
            com.google.common.util.concurrent.ListeningScheduledExecutorService r6 = r9.getListeningScheduledExecutorService()     // Catch:{ all -> 0x0050 }
            com.google.android.libraries.performance.primes.CpuProfilingService$CpuCollectionStartTask r7 = new com.google.android.libraries.performance.primes.CpuProfilingService$CpuCollectionStartTask     // Catch:{ all -> 0x0050 }
            r7.<init>(r4)     // Catch:{ all -> 0x0050 }
            java.util.concurrent.TimeUnit r8 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x0050 }
            com.google.common.util.concurrent.ListenableScheduledFuture r6 = r6.schedule(r7, r2, r8)     // Catch:{ all -> 0x0050 }
            r9.scheduledFutureCollectCpuUsage = r6     // Catch:{ all -> 0x0050 }
        L_0x004e:
            monitor-exit(r9)
            return
        L_0x0050:
            r0 = move-exception
            monitor-exit(r9)
            goto L_0x0054
        L_0x0053:
            throw r0
        L_0x0054:
            goto L_0x0053
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.performance.primes.CpuProfilingService.scheduleNextMonitoringWindow():void");
    }

    private final class CpuCollectionStartTask implements Runnable {
        private final long stopTimeMs;

        CpuCollectionStartTask(long stopTimeMs2) {
            this.stopTimeMs = stopTimeMs2;
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?>
         arg types: [com.google.android.libraries.performance.primes.CpuProfilingService$CpuCollectionEndTask, long, java.util.concurrent.TimeUnit]
         candidates:
          com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<V>
          ClspMth{java.util.concurrent.ScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<?>}
          ClspMth{<V> java.util.concurrent.ScheduledExecutorService.schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit):java.util.concurrent.ScheduledFuture<V>}
          com.google.common.util.concurrent.ListeningScheduledExecutorService.schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit):com.google.common.util.concurrent.ListenableScheduledFuture<?> */
        public void run() {
            long now = CpuProfilingService.this.clock.currentTimeMillis();
            if (this.stopTimeMs <= now) {
                CpuProfilingService.this.scheduleNextMonitoringWindow();
                return;
            }
            Intent batteryStatus = CpuProfilingService.this.getApplication().registerReceiver(null, CpuProfilingService.this.batteryIntentFilter);
            CpuProfiling.DeviceMetadata deviceMetadata = CpuProfilingService.this.createAndInitDeviceMetadata(batteryStatus);
            File traceFile = CpuProfilingService.this.getTraceFile();
            if (traceFile == null) {
                PrimesLog.m56w(CpuProfilingService.TAG, "Can't create file, aborting method sampling");
                return;
            }
            CpuProfilingService.this.clearTraceFile();
            Debug.startMethodTracingSampling(traceFile.getAbsolutePath(), CpuProfilingService.this.maxBufferSizeBytes, CpuProfilingService.this.frequencyMicro);
            CpuProfilingService cpuProfilingService = CpuProfilingService.this;
            ListeningScheduledExecutorService listeningScheduledExecutorService = cpuProfilingService.getListeningScheduledExecutorService();
            CpuProfilingService cpuProfilingService2 = CpuProfilingService.this;
            ScheduledFuture unused = cpuProfilingService.scheduledFutureCollectCpuUsage = listeningScheduledExecutorService.schedule((Runnable) new CpuCollectionEndTask(traceFile, deviceMetadata, Float.valueOf(cpuProfilingService2.getBatteryPercent(batteryStatus)), Long.valueOf(this.stopTimeMs), Long.valueOf(now)), this.stopTimeMs - now, TimeUnit.MILLISECONDS);
        }
    }

    /* access modifiers changed from: private */
    public CpuProfiling.DeviceMetadata createAndInitDeviceMetadata(Intent batteryStatus) {
        return (CpuProfiling.DeviceMetadata) CpuProfiling.DeviceMetadata.newBuilder().setBeforeState(getDeviceState(batteryStatus)).build();
    }

    /* access modifiers changed from: private */
    public CpuProfiling.DeviceState getDeviceState(Intent batteryStatus) {
        CpuProfiling.DeviceState.Builder state = CpuProfiling.DeviceState.newBuilder().setWifiOn(getWifiManager().isWifiEnabled());
        if (ContextCompat.checkSelfPermission(getApplication(), "android.permission.BLUETOOTH") == 0) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            state.setBluetoothOn(bluetoothAdapter != null && bluetoothAdapter.isEnabled());
        }
        return (CpuProfiling.DeviceState) state.setScreenOn(ProcessStats.isScreenOn(getApplication())).setCharging(isCharging(batteryStatus)).build();
    }

    /* access modifiers changed from: private */
    public float getBatteryPercent(Intent batteryStatus) {
        return ((float) batteryStatus.getIntExtra("level", -1)) / ((float) batteryStatus.getIntExtra("scale", -1));
    }

    private boolean isCharging(Intent batteryStatus) {
        int status = batteryStatus.getIntExtra("status", -1);
        return status == 2 || status == 5;
    }

    private final class CpuCollectionEndTask implements Runnable {
        private final Long actualStartTiemMs;
        private final Float batteryPercent;
        private final CpuProfiling.DeviceMetadata deviceMetadata;
        private final Long stopTimeMs;
        private final File traceFile;

        CpuCollectionEndTask(File traceFile2, CpuProfiling.DeviceMetadata deviceMetadata2, Float batteryPercent2, Long stopTimeMs2, Long actualStartTimeMs) {
            this.traceFile = traceFile2;
            this.deviceMetadata = deviceMetadata2;
            this.batteryPercent = batteryPercent2;
            this.stopTimeMs = stopTimeMs2;
            this.actualStartTiemMs = actualStartTimeMs;
        }

        public void run() {
            CpuProfilingService.this.scheduled.set(false);
            Debug.stopMethodTracing();
            Long now = Long.valueOf(CpuProfilingService.this.clock.currentTimeMillis());
            if (now.longValue() >= this.stopTimeMs.longValue() + ((long) CpuProfilingService.this.sampleDurationSkewMs)) {
                CpuProfilingService.this.scheduleNextMonitoringWindow();
                PrimesLog.m56w(CpuProfilingService.TAG, "Missed sample window by %d ms", Long.valueOf(now.longValue() - this.stopTimeMs.longValue()));
                return;
            }
            Intent batteryStatus = CpuProfilingService.this.getApplication().registerReceiver(null, CpuProfilingService.this.batteryIntentFilter);
            CpuProfiling.CpuProfilingMetric.Builder cpuProfilingMetric = CpuProfiling.CpuProfilingMetric.newBuilder().setDeviceMetadata((CpuProfiling.DeviceMetadata) ((CpuProfiling.DeviceMetadata.Builder) this.deviceMetadata.toBuilder()).setAfterState(CpuProfilingService.this.getDeviceState(batteryStatus)).setBatteryDropPercent(this.batteryPercent.floatValue() - CpuProfilingService.this.getBatteryPercent(batteryStatus)).build());
            File file = this.traceFile;
            if (file == null || !file.exists()) {
                PrimesLog.m50e(CpuProfilingService.TAG, "Missing trace file");
            } else {
                try {
                    cpuProfilingMetric.setTraceBlob(ByteString.copyFrom(SamplingUtils.compressBytes(CpuProfilingService.readFile(this.traceFile, CpuProfilingService.this.maxBufferSizeBytes))));
                    CpuProfilingService.this.clearTraceFile();
                } catch (IOException e) {
                    String valueOf = String.valueOf(this.traceFile);
                    StringBuilder sb = new StringBuilder(valueOf.length() + 20);
                    sb.append("Unable to read file ");
                    sb.append(valueOf);
                    PrimesLog.m49e(CpuProfilingService.TAG, sb.toString(), e);
                }
            }
            cpuProfilingMetric.setSamplesPerEpoch(CpuProfilingService.this.samplesPerEpoch).setSampleFrequency(CpuProfilingService.this.frequencyMicro);
            if (now.longValue() - this.actualStartTiemMs.longValue() < 2147483647L) {
                cpuProfilingMetric.setSampleDurationActual((int) (now.longValue() - this.actualStartTiemMs.longValue()));
            } else {
                cpuProfilingMetric.setSampleDurationActual(-1);
            }
            cpuProfilingMetric.setSampleDurationScheduled(CpuProfilingService.this.sampleDurationMs).setSampleBufferSize(CpuProfilingService.this.maxBufferSizeBytes);
            if (cpuProfilingMetric.getTraceBlob().size() > 0) {
                CpuProfilingService.this.recordSystemHealthMetric((SystemHealthProto.SystemHealthMetric) SystemHealthProto.SystemHealthMetric.newBuilder().setCpuProfilingMetric(cpuProfilingMetric).build());
            }
            CpuProfilingService.this.scheduleNextMonitoringWindow();
        }
    }

    private synchronized void shutdownService(boolean mayInterruptIfRunning) {
        if (this.scheduledFutureCollectCpuUsage != null) {
            this.scheduledFutureCollectCpuUsage.cancel(mayInterruptIfRunning);
            this.scheduledFutureCollectCpuUsage = null;
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void shutdownService() {
        shutdownService(true);
    }

    public void onPrimesInitialize() {
        clearTraceFile();
        scheduleNextMonitoringWindow();
    }

    public void onFirstActivityCreated() {
    }

    /* access modifiers changed from: private */
    public synchronized File getTraceFile() {
        String processName = ProcessStats.getCurrentProcessName();
        String filename = String.valueOf(processName).concat(".trace");
        File filesDir = getApplication().getFilesDir();
        String valueOf = TRACE_DIR_PREFIX;
        String valueOf2 = String.valueOf(processName);
        File traceDir = new File(filesDir, valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf);
        if (!traceDir.exists() && !traceDir.mkdir()) {
            return null;
        }
        return new File(traceDir, filename);
    }

    /* access modifiers changed from: private */
    public void clearTraceFile() {
        File traceFile = getTraceFile();
        if (traceFile != null) {
            try {
                if (traceFile.exists()) {
                    traceFile.delete();
                }
            } catch (Exception e) {
            }
        }
    }

    private WifiManager getWifiManager() {
        if (this.wifi == null) {
            this.wifi = (WifiManager) getApplication().getSystemService("wifi");
        }
        return this.wifi;
    }

    /* access modifiers changed from: private */
    public static byte[] readFile(File file, int maxSize) throws IOException {
        FileInputStream fis = null;
        try {
            long fileLength = file.length();
            if (fileLength <= 0 || fileLength > ((long) maxSize)) {
                return new byte[0];
            }
            int len = (int) fileLength;
            byte[] content = new byte[len];
            FileInputStream fis2 = new FileInputStream(file);
            for (int read = 0; read < len; read += fis2.read(content, read, len - read)) {
            }
            fis2.close();
            fis2.close();
            return content;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}
