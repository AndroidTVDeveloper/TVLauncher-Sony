package com.google.android.libraries.performance.primes.jank;

import logs.proto.wireless.performance.mobile.SystemHealthProto;

public interface FrameTimeMeasurement {
    void addFrame(int i, int i2);

    SystemHealthProto.JankMetric getMetric();

    boolean isMetricReadyToBeSent();
}
