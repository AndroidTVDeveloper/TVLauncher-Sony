package com.google.android.libraries.performance.primes;

import logs.proto.wireless.performance.mobile.ExtensionMetric;

public interface JankMetricExtensionProvider {
    ExtensionMetric.MetricExtension getMetricExtension();
}
