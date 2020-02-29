package com.google.android.libraries.performance.primes.battery;

import android.content.SharedPreferences;
import com.google.android.libraries.performance.primes.persistent.PersistentStorage;
import com.google.android.libraries.performance.proto.primes.persistent.PersistentFormat;
import logs.proto.wireless.performance.mobile.BatteryMetric;
import logs.proto.wireless.performance.mobile.ExtensionMetric;

public final class StatsStorage {
    private static final String STATS_KEY = "primes.battery.snapshot";
    private static final String TAG = "PersistentBatteryStats";
    private final PersistentStorage storage;

    public static final class StatsRecord {
        /* access modifiers changed from: private */
        public final Long currentTime;
        /* access modifiers changed from: private */
        public final String customEventName;
        /* access modifiers changed from: private */
        public final Long elapsedTime;
        /* access modifiers changed from: private */
        public final Boolean isEventNameConstant;
        /* access modifiers changed from: private */
        public final ExtensionMetric.MetricExtension metricExtension;
        /* access modifiers changed from: private */
        public final Long primesVersion;
        /* access modifiers changed from: private */
        public final BatteryMetric.UidHealthProto proto;
        /* access modifiers changed from: private */
        public final BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo;
        /* access modifiers changed from: private */
        public final Long versionNameHash;

        public StatsRecord(BatteryMetric.UidHealthProto proto2, Long elapsedTime2, Long currentTime2, Long primesVersion2, Long versionNameHash2, BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo2, String customEventName2, Boolean isEventNameConstant2, ExtensionMetric.MetricExtension metricExtension2) {
            this.proto = proto2;
            this.elapsedTime = elapsedTime2;
            this.currentTime = currentTime2;
            this.primesVersion = primesVersion2;
            this.versionNameHash = versionNameHash2;
            this.sampleInfo = sampleInfo2;
            this.customEventName = customEventName2;
            this.isEventNameConstant = isEventNameConstant2;
            this.metricExtension = metricExtension2;
        }

        public BatteryMetric.UidHealthProto getProto() {
            return this.proto;
        }

        public Long getElapsedTime() {
            return this.elapsedTime;
        }

        public Long getCurrentTime() {
            return this.currentTime;
        }

        public Long getPrimesVersion() {
            return this.primesVersion;
        }

        public Long getVersionNameHash() {
            return this.versionNameHash;
        }

        public BatteryMetric.BatteryStatsDiff.SampleInfo getSampleInfo() {
            return this.sampleInfo;
        }

        public String getCustomEventName() {
            return this.customEventName;
        }

        public Boolean isEventNameConstant() {
            return this.isEventNameConstant;
        }

        public ExtensionMetric.MetricExtension getMetricExtension() {
            return this.metricExtension;
        }

        public String toString() {
            return String.format("StatsRecord:\n  elapsed: %d\n  current: %d\n  Primes version: %d\n  version name #: %d\n  customName: %s\n", this.elapsedTime, this.currentTime, this.primesVersion, this.versionNameHash, this.customEventName);
        }
    }

    public StatsStorage(SharedPreferences sharedPreferences) {
        this.storage = new PersistentStorage(sharedPreferences);
    }

    public StatsRecord readStatsRecord() {
        PersistentFormat.BatterySnapshot data = (PersistentFormat.BatterySnapshot) this.storage.readProto(STATS_KEY, PersistentFormat.BatterySnapshot.parser());
        ExtensionMetric.MetricExtension metricExtension = null;
        if (data == null) {
            return null;
        }
        BatteryMetric.BatteryStatsDiff.SampleInfo sampleInfo = null;
        if (data.hasSampleInfo() && (sampleInfo = BatteryMetric.BatteryStatsDiff.SampleInfo.forNumber(data.getSampleInfo())) == null) {
            sampleInfo = BatteryMetric.BatteryStatsDiff.SampleInfo.UNKNOWN;
        }
        BatteryMetric.UidHealthProto uidHealthProto = data.getUidHealthProto();
        Long valueOf = data.hasElapsedTime() ? Long.valueOf(data.getElapsedTime()) : null;
        Long valueOf2 = data.hasCurrentTime() ? Long.valueOf(data.getCurrentTime()) : null;
        Long valueOf3 = data.hasPrimesVersion() ? Long.valueOf(data.getPrimesVersion()) : null;
        Long valueOf4 = data.hasVersionNameHash() ? Long.valueOf(data.getVersionNameHash()) : null;
        String customEventName = data.hasCustomEventName() ? data.getCustomEventName() : null;
        Boolean valueOf5 = data.hasIsEventNameConstant() ? Boolean.valueOf(data.getIsEventNameConstant()) : null;
        if (data.hasMetricExtension()) {
            metricExtension = data.getMetricExtension();
        }
        return new StatsRecord(uidHealthProto, valueOf, valueOf2, valueOf3, valueOf4, sampleInfo, customEventName, valueOf5, metricExtension);
    }

    public boolean writeStatsRecord(StatsRecord statsRecord) {
        PersistentFormat.BatterySnapshot.Builder data = PersistentFormat.BatterySnapshot.newBuilder();
        if (statsRecord.proto != null) {
            data.setUidHealthProto(statsRecord.proto);
        }
        if (statsRecord.elapsedTime != null) {
            data.setElapsedTime(statsRecord.elapsedTime.longValue());
        }
        if (statsRecord.currentTime != null) {
            data.setCurrentTime(statsRecord.currentTime.longValue());
        }
        if (statsRecord.primesVersion != null) {
            data.setPrimesVersion(statsRecord.primesVersion.longValue());
        }
        if (statsRecord.versionNameHash != null) {
            data.setVersionNameHash(statsRecord.versionNameHash.longValue());
        }
        if (statsRecord.sampleInfo != null) {
            data.setSampleInfo(statsRecord.sampleInfo.getNumber());
        }
        if (statsRecord.customEventName != null) {
            data.setCustomEventName(statsRecord.customEventName);
        }
        if (statsRecord.isEventNameConstant != null) {
            data.setIsEventNameConstant(statsRecord.isEventNameConstant.booleanValue());
        }
        if (statsRecord.metricExtension != null) {
            data.setMetricExtension(statsRecord.metricExtension);
        }
        return this.storage.writeProto(STATS_KEY, (PersistentFormat.BatterySnapshot) data.build());
    }

    public void clear() {
        this.storage.remove(STATS_KEY);
    }
}
