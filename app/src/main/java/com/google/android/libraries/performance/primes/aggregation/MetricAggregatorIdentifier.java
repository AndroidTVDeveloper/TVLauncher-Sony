package com.google.android.libraries.performance.primes.aggregation;

import android.text.TextUtils;
import java.util.Arrays;
import logs.proto.wireless.performance.mobile.AggregatedMetricProto;

public final class MetricAggregatorIdentifier {
    private final String componentName;
    private final String customCounterName;
    private final AggregatedMetricProto.AggregatedMetric.Identifier.Metric metric;

    public static final MetricAggregatorIdentifier forMetricAndComponent(AggregatedMetricProto.AggregatedMetric.Identifier.Metric metricType, String componentName2) {
        return new MetricAggregatorIdentifier(metricType, componentName2, null);
    }

    public static final MetricAggregatorIdentifier forCustomCounter(String counterName) {
        return new MetricAggregatorIdentifier(AggregatedMetricProto.AggregatedMetric.Identifier.Metric.CUSTOM_COUNTER, null, counterName);
    }

    public static final MetricAggregatorIdentifier forCustomCounter(String counterName, String componentName2) {
        return new MetricAggregatorIdentifier(AggregatedMetricProto.AggregatedMetric.Identifier.Metric.CUSTOM_COUNTER, componentName2, counterName);
    }

    private MetricAggregatorIdentifier(AggregatedMetricProto.AggregatedMetric.Identifier.Metric metricType, String componentName2, String customCounterName2) {
        this.metric = metricType;
        this.componentName = componentName2;
        this.customCounterName = customCounterName2;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MetricAggregatorIdentifier)) {
            return false;
        }
        MetricAggregatorIdentifier o = (MetricAggregatorIdentifier) other;
        return this.metric == o.metric && TextUtils.equals(this.componentName, o.componentName) && TextUtils.equals(this.customCounterName, o.customCounterName);
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{this.metric, this.componentName, this.customCounterName});
    }

    public AggregatedMetricProto.AggregatedMetric.Identifier toProto() {
        AggregatedMetricProto.AggregatedMetric.Identifier.Builder identifierProto = AggregatedMetricProto.AggregatedMetric.Identifier.newBuilder();
        String str = this.componentName;
        if (str != null) {
            identifierProto.setComponentName(str);
        }
        String str2 = this.customCounterName;
        if (str2 != null) {
            identifierProto.setCustomCounterName(str2);
        }
        return (AggregatedMetricProto.AggregatedMetric.Identifier) identifierProto.setMetric(this.metric).build();
    }
}
