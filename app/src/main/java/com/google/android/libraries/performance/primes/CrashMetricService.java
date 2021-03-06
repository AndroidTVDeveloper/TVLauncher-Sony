package com.google.android.libraries.performance.primes;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;
import com.google.android.libraries.performance.primes.AppLifecycleListener;
import com.google.android.libraries.performance.primes.MetricRecorder;
import com.google.android.libraries.performance.primes.metriccapture.ProcessStatsCapture;
import com.google.android.libraries.performance.primes.sampling.ProbabilitySampler;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.android.libraries.performance.primes.transmitter.StackTraceTransmitter;
import com.google.android.libraries.stitch.util.Preconditions;
import com.google.android.libraries.stitch.util.ThreadUtil;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.protobuf.ExtensionRegistryLite;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread;
import java.util.concurrent.atomic.AtomicBoolean;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.ProcessProto;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

final class CrashMetricService extends AbstractMetricService implements PrimesStartupListener {
    private static final String CRASH_STORE_FILE_NAME = "primes_crash";
    private static final String TAG = "CrashMetricService";
    volatile NoPiiString activeComponentName;
    volatile ActivityTracker activityNameTracker;
    private final AppLifecycleMonitor appLifecycleMonitor;
    /* access modifiers changed from: private */
    public final AtomicBoolean deferPrimesStats;
    private volatile SystemHealthProto.CrashMetric deferredPrevCrash;
    private final int estimatedCount;
    private final AtomicBoolean isPrimesExceptionHandlerDefaultHandler = new AtomicBoolean();
    /* access modifiers changed from: private */
    public final CrashMetricExtensionProvider metricExtensionProvider;
    /* access modifiers changed from: private */
    public final boolean persistCrashStatsEnabled;
    /* access modifiers changed from: private */
    public final boolean sendStackTraces;
    private final boolean shouldSendStartupMetric;
    /* access modifiers changed from: private */
    public final StackTraceTransmitter stackTraceTransmitter;

    interface ActivityTracker extends AppLifecycleListener.OnAppToBackground, AppLifecycleListener.OnActivityStarted {
    }

    static CrashMetricService createService(MetricTransmitter metricTransmitter, Application application, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, PrimesCrashConfigurations crashConfigs, boolean deferPrimesStats2, boolean persistentCrashStatsEnabled) {
        return new CrashMetricService(metricTransmitter, crashConfigs.getMetricExtensionProvider(), crashConfigs.getStackTraceTransmitter(), crashConfigs.isSendStackTraces(), metricStamperSupplier, executorServiceSupplier, application, crashConfigs.getStartupSamplePercentage(), deferPrimesStats2, persistentCrashStatsEnabled);
    }

    CrashMetricService(MetricTransmitter transmitter, CrashMetricExtensionProvider metricExtensionProvider2, StackTraceTransmitter stackTraceTransmitter2, boolean sendStackTraces2, Supplier<MetricStamper> metricStamperSupplier, Supplier<ListeningScheduledExecutorService> executorServiceSupplier, Application application, float startupSampling, boolean deferPrimesStats2, boolean persistCrashStatsEnabled2) {
        super(transmitter, application, metricStamperSupplier, executorServiceSupplier, MetricRecorder.RunIn.SAME_THREAD);
        Preconditions.checkNotNull(stackTraceTransmitter2);
        Preconditions.checkArgument(startupSampling > 0.0f && startupSampling <= 100.0f, "StartupSamplePercentage should be a floating number > 0 and <= 100.");
        this.appLifecycleMonitor = AppLifecycleMonitor.getInstance(application);
        this.shouldSendStartupMetric = new ProbabilitySampler(startupSampling / 100.0f).isSampleAllowed();
        this.estimatedCount = (int) (100.0f / startupSampling);
        this.metricExtensionProvider = metricExtensionProvider2;
        this.stackTraceTransmitter = stackTraceTransmitter2;
        this.sendStackTraces = sendStackTraces2;
        this.deferPrimesStats = new AtomicBoolean(deferPrimesStats2);
        this.persistCrashStatsEnabled = persistCrashStatsEnabled2;
    }

    /* access modifiers changed from: package-private */
    public void setPrimesExceptionHandlerAsDefaultHandler() {
        if (this.isPrimesExceptionHandlerDefaultHandler.compareAndSet(false, true)) {
            Thread.setDefaultUncaughtExceptionHandler(wrapUncaughtExceptionHandlerWithPrimesHandler(Thread.getDefaultUncaughtExceptionHandler()));
        }
    }

    /* access modifiers changed from: package-private */
    public Thread.UncaughtExceptionHandler wrapUncaughtExceptionHandlerWithPrimesHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        return new PrimesUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    class PrimesUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        /* access modifiers changed from: private */
        public final Thread.UncaughtExceptionHandler handlerToWrap;

        PrimesUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handlerToWrap2) {
            this.handlerToWrap = handlerToWrap2;
        }

        public void uncaughtException(Thread thread, Throwable throwable) {
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
            FileOutputStream fileOutputStream = null;
            StrictMode.ThreadPolicy prevThreadPolicy = null;
            try {
                if (CrashMetricService.this.shouldRecord()) {
                    if (CrashMetricService.this.persistCrashStatsEnabled) {
                        prevThreadPolicy = StrictMode.allowThreadDiskWrites();
                        try {
                            fileOutputStream = CrashMetricService.this.getApplication().openFileOutput(CrashMetricService.CRASH_STORE_FILE_NAME, 0);
                            fileOutputStream.flush();
                        } catch (IOException e) {
                            PrimesLog.m56w(CrashMetricService.TAG, "IO failure creating empty file.");
                        }
                    }
                    SystemHealthProto.CrashMetric crash = CrashMetricService.this.createCrashMetric(thread.getName(), throwable);
                    if (CrashMetricService.this.persistCrashStatsEnabled && fileOutputStream != null) {
                        try {
                            fileOutputStream.write(crash.toByteArray());
                        } catch (IOException e2) {
                            PrimesLog.m56w(CrashMetricService.TAG, "IO failure storing crash.");
                        }
                    }
                    SystemHealthProto.SystemHealthMetric.Builder metric = SystemHealthProto.SystemHealthMetric.newBuilder().setCrashMetric(crash);
                    if (CrashMetricService.this.metricExtensionProvider != null) {
                        try {
                            ExtensionMetric.MetricExtension metricExtension = CrashMetricService.this.metricExtensionProvider.getMetricExtension();
                            if (metricExtension != null) {
                                metric.setMetricExtension(metricExtension);
                            }
                        } catch (Exception e3) {
                            PrimesLog.m55w(CrashMetricService.TAG, "Exception while getting crash metric extension!", e3);
                        }
                    }
                    CrashMetricService.this.maybeSendDeferredPrimesStats();
                    CrashMetricService.this.recordSystemHealthMetric((SystemHealthProto.SystemHealthMetric) metric.build());
                    if (!CrashMetricService.this.isShutdown() && CrashMetricService.this.sendStackTraces) {
                        CrashMetricService.this.stackTraceTransmitter.send(throwable, CrashMetricService.this.activeComponentName);
                    }
                }
                PrimesHprofFile.deleteHeapDumpIfExists(CrashMetricService.this.getApplication());
                PrimesHprofFile.deleteMiniHeapDumpHprofIfExists(CrashMetricService.this.getApplication());
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4) {
                        PrimesLog.m56w(CrashMetricService.TAG, "Could not close file.");
                    }
                }
                if (prevThreadPolicy != null) {
                    StrictMode.setThreadPolicy(prevThreadPolicy);
                }
                uncaughtExceptionHandler = this.handlerToWrap;
                if (uncaughtExceptionHandler == null) {
                    return;
                }
            } catch (Exception e5) {
                PrimesLog.m55w(CrashMetricService.TAG, "Failed to record crash.", e5);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e6) {
                        PrimesLog.m56w(CrashMetricService.TAG, "Could not close file.");
                    }
                }
                if (prevThreadPolicy != null) {
                    StrictMode.setThreadPolicy(prevThreadPolicy);
                }
                uncaughtExceptionHandler = this.handlerToWrap;
                if (uncaughtExceptionHandler == null) {
                    return;
                }
            } catch (Throwable th) {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e7) {
                        PrimesLog.m56w(CrashMetricService.TAG, "Could not close file.");
                    }
                }
                if (prevThreadPolicy != null) {
                    StrictMode.setThreadPolicy(prevThreadPolicy);
                }
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler2 = this.handlerToWrap;
                if (uncaughtExceptionHandler2 != null) {
                    uncaughtExceptionHandler2.uncaughtException(thread, throwable);
                }
                throw th;
            }
            uncaughtExceptionHandler.uncaughtException(thread, throwable);
        }
    }

    /* access modifiers changed from: private */
    public void maybeSendDeferredPrimesStats() {
        if (this.deferPrimesStats.getAndSet(false)) {
            recordStartupEvent(SystemHealthProto.PrimesStats.PrimesEvent.PRIMES_CRASH_MONITORING_INITIALIZED, this.deferredPrevCrash);
            recordStartupEvent(SystemHealthProto.PrimesStats.PrimesEvent.PRIMES_FIRST_ACTIVITY_LAUNCHED, null);
        }
    }

    static SystemHealthProto.CrashMetric.CrashType crashType(Class<? extends Throwable> crashType) {
        if (crashType == OutOfMemoryError.class) {
            return SystemHealthProto.CrashMetric.CrashType.OUT_OF_MEMORY_ERROR;
        }
        if (NullPointerException.class.isAssignableFrom(crashType)) {
            return SystemHealthProto.CrashMetric.CrashType.NULL_POINTER_EXCEPTION;
        }
        if (RuntimeException.class.isAssignableFrom(crashType)) {
            return SystemHealthProto.CrashMetric.CrashType.OTHER_RUNTIME_EXCEPTION;
        }
        if (Error.class.isAssignableFrom(crashType)) {
            return SystemHealthProto.CrashMetric.CrashType.OTHER_ERROR;
        }
        return SystemHealthProto.CrashMetric.CrashType.UNKNOWN;
    }

    /* access modifiers changed from: private */
    public SystemHealthProto.CrashMetric createCrashMetric(String threadName, Throwable throwable) {
        SystemHealthProto.CrashMetric.Builder crash = SystemHealthProto.CrashMetric.newBuilder();
        String componentName = NoPiiString.safeToString(this.activeComponentName);
        if (componentName != null) {
            crash.setActiveComponentName(componentName);
        }
        crash.setHasCrashed(true).setThreadName(threadName).setCrashType(crashType(throwable.getClass())).setCrashClassName(throwable.getClass().getName());
        try {
            Long hashedStackTrace = Hashing.hash(StackTraceSanitizer.getSanitizedStackTrace(throwable));
            if (hashedStackTrace != null) {
                crash.setHashedStackTrace(hashedStackTrace.longValue());
            }
        } catch (Exception e) {
            String valueOf = String.valueOf(e);
            StringBuilder sb = new StringBuilder(valueOf.length() + 38);
            sb.append("Failed to generate hashed stack trace.");
            sb.append(valueOf);
            PrimesLog.m56w(TAG, sb.toString());
        }
        try {
            crash.setProcessStats(ProcessProto.ProcessStats.newBuilder().setAndroidProcessStats(ProcessStatsCapture.getAndroidProcessStats(getApplication())));
        } catch (Exception e2) {
            PrimesLog.m55w(TAG, "Failed to get process stats.", e2);
        }
        return (SystemHealthProto.CrashMetric) crash.build();
    }

    private SystemHealthProto.CrashMetric readAndClearStoredCrash() {
        ThreadUtil.ensureBackgroundThread();
        File crashFile = new File(getApplication().getFilesDir(), CRASH_STORE_FILE_NAME);
        try {
            if (!crashFile.exists()) {
                return null;
            }
            PrimesLog.m48d(TAG, "found persisted crash");
            SystemHealthProto.CrashMetric.Builder ret = SystemHealthProto.CrashMetric.newBuilder();
            if (readAndDeleteStoredCrash(crashFile, ret)) {
                return (SystemHealthProto.CrashMetric) ret.build();
            }
            PrimesLog.m56w(TAG, "could not delete crash file");
            return null;
        } catch (IOException ex) {
            PrimesLog.m47d(TAG, "IO failure", ex);
            return null;
        } catch (SecurityException se) {
            PrimesLog.m47d(TAG, "Unexpected SecurityException", se);
            return null;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(byte[], com.google.protobuf.ExtensionRegistryLite):BuilderType
     arg types: [byte[], com.google.protobuf.ExtensionRegistryLite]
     candidates:
      com.google.protobuf.GeneratedMessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.AbstractMessageLite$Builder
      com.google.protobuf.GeneratedMessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):BuilderType
      com.google.protobuf.GeneratedMessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(com.google.protobuf.ByteString, com.google.protobuf.ExtensionRegistryLite):BuilderType
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):BuilderType
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(java.io.InputStream, com.google.protobuf.ExtensionRegistryLite):BuilderType
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(com.google.protobuf.ByteString, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(java.io.InputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(byte[], com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.MessageLite.Builder.mergeFrom(com.google.protobuf.ByteString, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.MessageLite.Builder.mergeFrom(com.google.protobuf.CodedInputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.MessageLite.Builder.mergeFrom(java.io.InputStream, com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.MessageLite.Builder.mergeFrom(byte[], com.google.protobuf.ExtensionRegistryLite):com.google.protobuf.MessageLite$Builder
      com.google.protobuf.AbstractMessageLite.Builder.mergeFrom(byte[], com.google.protobuf.ExtensionRegistryLite):BuilderType */
    private static boolean readAndDeleteStoredCrash(File crashFile, SystemHealthProto.CrashMetric.Builder dst) throws IOException {
        FileInputStream fis = null;
        try {
            long fileLength = crashFile.length();
            if (fileLength <= 0 || fileLength >= 2147483647L) {
                dst.setHasCrashed(true);
            } else {
                int len = (int) fileLength;
                byte[] content = new byte[len];
                fis = new FileInputStream(crashFile);
                for (int read = 0; read < len; read += fis.read(content, read, len - read)) {
                }
                dst.mergeFrom(content, ExtensionRegistryLite.getGeneratedRegistry());
            }
            return crashFile.delete();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public void onPrimesInitialize() {
        PrimesLog.m48d(TAG, "onPrimesInitialize");
        SystemHealthProto.CrashMetric prevCrash = null;
        if (this.persistCrashStatsEnabled) {
            PrimesLog.m48d(TAG, "persistent crash enabled.");
            try {
                prevCrash = readAndClearStoredCrash();
            } catch (RuntimeException re) {
                PrimesLog.m55w(TAG, "Unexpected failure: ", re);
            }
        }
        if (this.deferPrimesStats.get()) {
            this.deferredPrevCrash = prevCrash;
        } else if (!shouldRecord() || (prevCrash == null && !this.shouldSendStartupMetric)) {
            PrimesLog.m52i(TAG, "Startup metric for 'PRIMES_CRASH_MONITORING_INITIALIZED' dropped.");
        } else {
            recordStartupEvent(SystemHealthProto.PrimesStats.PrimesEvent.PRIMES_CRASH_MONITORING_INITIALIZED, prevCrash);
        }
    }

    /* access modifiers changed from: package-private */
    public void setActiveComponentName(NoPiiString activeComponentName2) {
        String valueOf = String.valueOf(NoPiiString.safeToString(activeComponentName2));
        PrimesLog.m48d(TAG, valueOf.length() != 0 ? "activeComponentName: ".concat(valueOf) : "activeComponentName: ");
        this.activeComponentName = activeComponentName2;
    }

    private ActivityTracker initActivityNameTracker() {
        return new ActivityTracker() {
            public void onActivityStarted(Activity activity) {
                CrashMetricService.this.setActiveComponentName(NoPiiString.fromClass(activity.getClass()));
            }

            public void onAppToBackground(Activity activity) {
                CrashMetricService.this.setActiveComponentName(null);
                if (CrashMetricService.this.deferPrimesStats.get()) {
                    PrimesExecutors.handleFuture(CrashMetricService.this.getListeningScheduledExecutorService().submit((Runnable) new CrashMetricService$1$$Lambda$0(this)));
                }
            }

            /* access modifiers changed from: package-private */
            public final /* synthetic */ void lambda$onAppToBackground$0$CrashMetricService$1() {
                CrashMetricService.this.maybeSendDeferredPrimesStats();
            }
        };
    }

    public void onFirstActivityCreated() {
        PrimesLog.m48d(TAG, "onFirstActivityCreated");
        if (!this.deferPrimesStats.get()) {
            sendActivityCreatedEvent();
        }
        this.activityNameTracker = initActivityNameTracker();
        this.appLifecycleMonitor.register(this.activityNameTracker);
    }

    /* access modifiers changed from: package-private */
    public void sendCustomLaunchedEvent() {
        sendStartupCountEvent(SystemHealthProto.PrimesStats.PrimesEvent.PRIMES_CUSTOM_LAUNCHED);
    }

    private void sendActivityCreatedEvent() {
        sendStartupCountEvent(SystemHealthProto.PrimesStats.PrimesEvent.PRIMES_FIRST_ACTIVITY_LAUNCHED);
    }

    private void sendStartupCountEvent(SystemHealthProto.PrimesStats.PrimesEvent countEvent) {
        if (!shouldRecord() || !this.shouldSendStartupMetric) {
            PrimesLog.m52i(TAG, "Startup metric for '%s' dropped.", countEvent);
        } else if (ThreadUtil.isMainThread()) {
            PrimesExecutors.handleFuture(getListeningScheduledExecutorService().submit((Runnable) new CrashMetricService$$Lambda$0(this, countEvent)));
        } else {
            recordStartupEvent(countEvent, null);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$sendStartupCountEvent$0$CrashMetricService(SystemHealthProto.PrimesStats.PrimesEvent countEvent) {
        recordStartupEvent(countEvent, null);
    }

    /* access modifiers changed from: package-private */
    public void recordStartupEvent(SystemHealthProto.PrimesStats.PrimesEvent event, SystemHealthProto.CrashMetric previousCrash) {
        SystemHealthProto.SystemHealthMetric.Builder metric = SystemHealthProto.SystemHealthMetric.newBuilder();
        SystemHealthProto.PrimesStats.Builder primesStats = SystemHealthProto.PrimesStats.newBuilder().setEstimatedCount(this.estimatedCount).setPrimesEvent(event);
        if (previousCrash != null) {
            primesStats.setPrimesDebugMessage((SystemHealthProto.PrimesStats.PrimesDebugMessage) SystemHealthProto.PrimesStats.PrimesDebugMessage.newBuilder().setPreviousCrash(previousCrash).build());
        }
        metric.setPrimesStats(primesStats);
        recordSystemHealthMetric((SystemHealthProto.SystemHealthMetric) metric.build());
    }

    /* access modifiers changed from: package-private */
    public void shutdownService() {
        if (this.activityNameTracker != null) {
            this.appLifecycleMonitor.unregister(this.activityNameTracker);
            this.activityNameTracker = null;
        }
        if (this.isPrimesExceptionHandlerDefaultHandler.get() && (Thread.getDefaultUncaughtExceptionHandler() instanceof PrimesUncaughtExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(((PrimesUncaughtExceptionHandler) Thread.getDefaultUncaughtExceptionHandler()).handlerToWrap);
        }
    }
}
