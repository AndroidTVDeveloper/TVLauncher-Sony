package com.google.android.libraries.performance.primes.battery;

import android.os.health.HealthStats;
import com.google.android.libraries.performance.primes.BatteryMetricExtensionProvider;
import com.google.android.libraries.performance.primes.MetricStamper;
import com.google.android.libraries.performance.primes.PrimesLog;
import com.google.android.libraries.performance.primes.Supplier;
import com.google.android.libraries.performance.primes.battery.StatsStorage;
import com.google.android.libraries.stitch.util.Preconditions;
import java.util.Objects;
import logs.proto.wireless.performance.mobile.BatteryMetric;
import logs.proto.wireless.performance.mobile.ExtensionMetric;
import logs.proto.wireless.performance.mobile.SystemHealthProto;

public final class BatteryCapture {
    private static final int DURATION_EPS = 25;
    private static final double MAX_REL_TOL = 3.472222222222222E-5d;
    private static final String TAG = "BatteryCapture";
    private final BatteryMetricExtensionProvider metricExtensionProvider;
    private final Supplier<MetricStamper> metricStamperSupplier;
    private final TimeCapture systemClockElapsedRealtimeCapture;
    private final TimeCapture systemCurrentTimeCapture;
    /* access modifiers changed from: private */
    public final SystemHealthCapture systemHealthCapture;

    public interface TimeCapture {
        long getTime();
    }

    /* access modifiers changed from: private */
    public long versionNameHash() {
        String versionName = this.metricStamperSupplier.get().getVersionName();
        if (versionName == null) {
            return 0;
        }
        return (long) versionName.hashCode();
    }

    /* access modifiers changed from: private */
    public final Long primesVersion() {
        return this.metricStamperSupplier.get().getPrimesVersion();
    }

    public BatteryCapture(Supplier<MetricStamper> metricStamperSupplier2, SystemHealthCapture systemHealthCapture2, TimeCapture systemCurrentTimeCapture2, TimeCapture systemClockElapsedRealtimeCapture2, BatteryMetricExtensionProvider metricExtensionProvider2) {
        this.systemHealthCapture = systemHealthCapture2;
        this.systemCurrentTimeCapture = systemCurrentTimeCapture2;
        this.systemClockElapsedRealtimeCapture = systemClockElapsedRealtimeCapture2;
        this.metricExtensionProvider = metricExtensionProvider2;
        this.metricStamperSupplier = metricStamperSupplier2;
    }

    public final class Snapshot {
        final Long currentTime;
        final String customEventName;
        final Long elapsedTime;
        final HealthStats healthStats;
        final Boolean isEventNameConstant;
        final ExtensionMetric.MetricExtension metricExtension;
        final BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo;

        private Snapshot(Long elapsedTime2, Long currentTime2, HealthStats healthStats2, BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo2, String customEventName2, Boolean isEventNameConstant2, ExtensionMetric.MetricExtension metricExtension2) {
            this.elapsedTime = elapsedTime2;
            this.currentTime = currentTime2;
            this.healthStats = healthStats2;
            this.sampleInfo = sampleInfo2;
            this.customEventName = customEventName2;
            this.isEventNameConstant = isEventNameConstant2;
            this.metricExtension = metricExtension2;
        }

        public StatsStorage.StatsRecord toStatsRecord() {
            return new StatsStorage.StatsRecord(BatteryCapture.this.systemHealthCapture.convertStats(this.healthStats), this.elapsedTime, this.currentTime, BatteryCapture.this.primesVersion(), Long.valueOf(BatteryCapture.this.versionNameHash()), this.sampleInfo, this.customEventName, this.isEventNameConstant, this.metricExtension);
        }
    }

    public Snapshot takeSnapshot(BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo, String customEventName, boolean isEventNameConstant) {
        return new Snapshot(Long.valueOf(this.systemClockElapsedRealtimeCapture.getTime()), Long.valueOf(this.systemCurrentTimeCapture.getTime()), this.systemHealthCapture.uidHealthStats(), sampleInfo, customEventName, Boolean.valueOf(isEventNameConstant), this.metricExtensionProvider.getMetricExtension(customEventName, sampleInfo));
    }

    public SystemHealthProto.SystemHealthMetric createBatteryMetric(StatsStorage.StatsRecord start, StatsStorage.StatsRecord end) {
        if (!consistentRecords(start, end)) {
            PrimesLog.m48d(TAG, "inconsistent stats");
            return null;
        }
        BatteryMetric.UidHealthProto diffProto = this.systemHealthCapture.diffStats(end.getProto(), start.getProto());
        if (diffProto == null) {
            PrimesLog.m48d(TAG, "null diff");
            return null;
        } else if (!diffProto.hasRealtimeBatteryMs() || diffProto.getRealtimeBatteryMs() <= 0) {
            PrimesLog.m48d(TAG, "invalid realtime");
            return null;
        } else {
            BatteryMetric.BatteryStatsDiff.Builder batteryStatsDiff = BatteryMetric.BatteryStatsDiff.newBuilder().setDurationMs(((Long) Preconditions.checkNotNull(end.getElapsedTime())).longValue() - ((Long) Preconditions.checkNotNull(start.getElapsedTime())).longValue());
            if (start.getSampleInfo() != null) {
                batteryStatsDiff.setStartInfo((BatteryMetric.BatteryStatsDiff.SampleInfo) Preconditions.checkNotNull(start.getSampleInfo()));
            }
            if (start.getCustomEventName() != null) {
                if (((Boolean) Preconditions.checkNotNull(start.isEventNameConstant())).booleanValue()) {
                    batteryStatsDiff.setStartConstantEventName((String) Preconditions.checkNotNull(start.getCustomEventName()));
                } else {
                    batteryStatsDiff.setStartCustomEventName((String) Preconditions.checkNotNull(start.getCustomEventName()));
                }
            }
            if (start.getMetricExtension() != null) {
                batteryStatsDiff.setStartMetricExtension((ExtensionMetric.MetricExtension) Preconditions.checkNotNull(start.getMetricExtension()));
            }
            if (end.getSampleInfo() != null) {
                batteryStatsDiff.setEndInfo((BatteryMetric.BatteryStatsDiff.SampleInfo) Preconditions.checkNotNull(end.getSampleInfo()));
            }
            if (end.getElapsedTime() != null) {
                batteryStatsDiff.setElapedRealtimeMs(((Long) Preconditions.checkNotNull(end.getElapsedTime())).longValue());
            }
            batteryStatsDiff.setUidHealthProtoDiff(diffProto);
            return (SystemHealthProto.SystemHealthMetric) SystemHealthProto.SystemHealthMetric.newBuilder().setBatteryUsageMetric(BatteryMetric.BatteryUsageMetric.newBuilder().setBatteryStatsDiff(batteryStatsDiff)).build();
        }
    }

    private static boolean consistentRecords(StatsStorage.StatsRecord stats, StatsStorage.StatsRecord otherStats) {
        return stats != null && otherStats != null && matchesVersion(stats, otherStats) && similarDuration(stats, otherStats);
    }

    private static boolean matchesVersion(StatsStorage.StatsRecord stats, StatsStorage.StatsRecord otherStats) {
        return Objects.equals(stats.getPrimesVersion(), otherStats.getPrimesVersion()) && Objects.equals(stats.getVersionNameHash(), otherStats.getVersionNameHash());
    }

    private static boolean similarDuration(StatsStorage.StatsRecord stats, StatsStorage.StatsRecord otherStats) {
        if (stats.getElapsedTime() == null || stats.getCurrentTime() == null || otherStats.getElapsedTime() == null || otherStats.getCurrentTime() == null) {
            return false;
        }
        long elapsedDuration = ((Long) Preconditions.checkNotNull(otherStats.getElapsedTime())).longValue() - ((Long) Preconditions.checkNotNull(stats.getElapsedTime())).longValue();
        long currentDuration = ((Long) Preconditions.checkNotNull(otherStats.getCurrentTime())).longValue() - ((Long) Preconditions.checkNotNull(stats.getCurrentTime())).longValue();
        if (currentDuration <= 0) {
            return false;
        }
        long durationDiff = Math.abs(elapsedDuration - currentDuration);
        if (durationDiff >= 25) {
            double d = (double) durationDiff;
            double d2 = (double) currentDuration;
            Double.isNaN(d);
            Double.isNaN(d2);
            return d / d2 <= MAX_REL_TOL;
        }
        return true;
    }
}
