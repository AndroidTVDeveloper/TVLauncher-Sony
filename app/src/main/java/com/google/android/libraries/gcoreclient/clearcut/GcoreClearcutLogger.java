package com.google.android.libraries.gcoreclient.clearcut;

import java.util.concurrent.TimeUnit;

@Deprecated
public interface GcoreClearcutLogger {
    boolean flush(long j, TimeUnit timeUnit);

    GcoreClearcutLogEventBuilder newEvent(GcoreClearcutMessageProducer gcoreClearcutMessageProducer);

    GcoreClearcutLogEventBuilder newEvent(byte[] bArr);
}
