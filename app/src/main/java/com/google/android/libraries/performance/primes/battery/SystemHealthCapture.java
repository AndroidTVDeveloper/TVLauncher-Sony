package com.google.android.libraries.performance.primes.battery;

import android.content.Context;
import android.os.health.HealthStats;
import android.os.health.SystemHealthManager;
import logs.proto.wireless.performance.mobile.BatteryMetric;

public class SystemHealthCapture {
    private final Context context;
    private final HashingNameSanitizer hashingNameSanitizer = new HashingNameSanitizer();

    public SystemHealthCapture(Context context2) {
        this.context = context2;
    }

    public HealthStats uidHealthStats() {
        SystemHealthManager manager = (SystemHealthManager) this.context.getSystemService("systemhealth");
        if (manager != null) {
            return manager.takeMyUidSnapshot();
        }
        return null;
    }

    public BatteryMetric.UidHealthProto convertStats(HealthStats healthStats) {
        BatteryMetric.UidHealthProto.Builder proto = (BatteryMetric.UidHealthProto.Builder) HealthStatsProtos.convert(healthStats).toBuilder();
        HealthStatsProtos.hashNames(proto, this.hashingNameSanitizer);
        return (BatteryMetric.UidHealthProto) proto.build();
    }

    public BatteryMetric.UidHealthProto diffStats(BatteryMetric.UidHealthProto currentProto, BatteryMetric.UidHealthProto prevProto) {
        BatteryMetric.UidHealthProto diffProto = HealthStatsProtos.subtract(currentProto, prevProto);
        if (diffProto == null) {
            return null;
        }
        BatteryMetric.UidHealthProto.Builder diffBuilder = (BatteryMetric.UidHealthProto.Builder) diffProto.toBuilder();
        HealthStatsProtos.sanitizeHashedNames(diffBuilder, this.hashingNameSanitizer);
        return (BatteryMetric.UidHealthProto) diffBuilder.build();
    }
}
