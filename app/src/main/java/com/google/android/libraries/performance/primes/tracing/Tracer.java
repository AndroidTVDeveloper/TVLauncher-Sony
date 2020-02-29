package com.google.android.libraries.performance.primes.tracing;

import android.text.TextUtils;
import com.google.android.libraries.performance.primes.NoPiiString;
import com.google.android.libraries.performance.primes.PrimesLog;
import com.google.android.libraries.performance.primes.PrimesToken;
import com.google.android.libraries.performance.primes.tracing.SpanEvent;
import com.google.android.libraries.stitch.util.Preconditions;
import com.google.android.libraries.stitch.util.ThreadUtil;
import java.util.concurrent.atomic.AtomicReference;
import logs.proto.wireless.performance.mobile.PrimesTraceOuterClass;

public final class Tracer {
    private static final String TAG = "Tracer";
    private static int maxBufferSize = 0;
    private static int minSpanDurationMs = 5;
    private static final AtomicReference<TraceData> traceData = new AtomicReference<>(null);

    public static boolean start(PrimesToken token, String rootSpanName, int minSpanDurationMs2, int maxBufferSize2) {
        Preconditions.checkNotNull(token);
        Preconditions.checkNotNull(rootSpanName);
        if (traceData.get() != null) {
            PrimesLog.m48d(TAG, "Ignore Tracer.start(), current active trace...");
            return false;
        } else if (!traceData.compareAndSet(null, new TraceData(rootSpanName))) {
            PrimesLog.m48d(TAG, "Ignore Tracer.start(), current active trace...");
            return false;
        } else {
            minSpanDurationMs = minSpanDurationMs2;
            maxBufferSize = maxBufferSize2;
            PrimesLog.m48d(TAG, "Start tracing with buffer: %d", Integer.valueOf(maxBufferSize2));
            return true;
        }
    }

    public static TraceData stop(PrimesToken token, String spanName) {
        Preconditions.checkNotNull(token);
        Preconditions.checkState(!TextUtils.isEmpty(spanName));
        TraceData traceToFlush = traceData.getAndSet(null);
        if (traceToFlush != null) {
            traceToFlush.updateRootSpanName(spanName);
        }
        PrimesLog.m48d(TAG, "Stop trace: %s", spanName);
        return traceToFlush;
    }

    public static void cancel(PrimesToken token) {
        Preconditions.checkNotNull(token);
        TraceData traceDataRef = traceData.getAndSet(null);
        if (traceDataRef != null) {
            PrimesLog.m48d(TAG, "Cancel trace: %s", traceDataRef.getRootSpan().spanName);
        }
    }

    public static SpanEvent beginSpan(NoPiiString spanName) {
        if (spanName == null) {
            return SpanEvent.EMPTY_SPAN;
        }
        return beginSpanInternal(spanName.toString(), SpanEvent.EventNameType.CONSTANT);
    }

    public static SpanEvent beginSpan(String spanName) {
        if (spanName == null) {
            return SpanEvent.EMPTY_SPAN;
        }
        return beginSpanInternal(spanName, SpanEvent.EventNameType.CONSTANT);
    }

    private static SpanEvent beginSpanInternal(String spanName, SpanEvent.EventNameType eventNameType) {
        TraceData traceDataRef = traceData.get();
        if (traceDataRef == null) {
            return SpanEvent.EMPTY_SPAN;
        }
        return traceDataRef.pushThreadLocalSpan(spanName, eventNameType);
    }

    public static void endSpan(SpanEvent span) {
        if (span != null && !span.equals(SpanEvent.EMPTY_SPAN)) {
            span.closeSpan();
            TraceData traceDataRef = traceData.get();
            if (traceDataRef != null) {
                if (span != traceDataRef.popThreadLocalSpan()) {
                    PrimesLog.m56w(TAG, "Incorrect Span passed. Ignore...");
                } else if (span.getDurationMs() >= ((long) minSpanDurationMs)) {
                    if (traceDataRef.incrementAndGetSpanCount() >= maxBufferSize) {
                        PrimesLog.m56w(TAG, "Dropping trace as max buffer size is hit. Size: %d", Integer.valueOf(traceDataRef.getSpanCount()));
                        clearTrace();
                        return;
                    }
                    traceDataRef.linkToParent(span);
                }
            }
        }
    }

    public static PrimesTraceOuterClass.Span[] flush(PrimesToken token, TraceData traceToFlush) {
        Preconditions.checkNotNull(token);
        ThreadUtil.ensureBackgroundThread();
        if (traceToFlush.getSpanCount() == 0) {
            return null;
        }
        return SpanProtoGenerator.create(traceToFlush.linkTraceAndGetRootSpan()).generate();
    }

    public static void sideLoadSpan(PrimesToken token, String spanName, long startMs, long durationMs) {
        Preconditions.checkNotNull(token);
        TraceData traceDataRef = traceData.get();
        if (traceDataRef != null) {
            traceDataRef.sideLoadSpan(spanName, startMs, durationMs);
        }
    }

    private static void clearTrace() {
        traceData.set(null);
    }

    public static void shutdown(PrimesToken token) {
        Preconditions.checkNotNull(token);
        clearTrace();
    }

    public static int getMinSpanDurationMs() {
        return minSpanDurationMs;
    }

    static void endSpanForByteCodeManipulationOnly() {
    }
}
