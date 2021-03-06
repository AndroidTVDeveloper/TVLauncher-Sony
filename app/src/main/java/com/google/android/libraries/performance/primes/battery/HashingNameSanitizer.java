package com.google.android.libraries.performance.primes.battery;

import com.google.android.libraries.performance.primes.Hashing;
import com.google.android.libraries.performance.primes.PrimesLog;
import com.google.android.libraries.stitch.util.Preconditions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import logs.proto.wireless.performance.mobile.BatteryMetric;

final class HashingNameSanitizer {
    private static final String MALFORMED_SYNC_SANITIZATION = "MALFORMED";
    private static final String SYNC_WAKELOCK_PREFIX = "*sync*/";
    private static final Pattern SYSTEM_TASK_PATTERN = Pattern.compile("^(\\*[a-z]+\\*).*");
    private static final String TAG = "HashingNameSanitizer";
    private static boolean disabledForTestOnly;
    final ConcurrentHashMap<Long, Long> hashHashMap = new ConcurrentHashMap<>();

    enum NameType {
        WAKELOCK,
        SYNC,
        JOB,
        PROCESS,
        SENSOR
    }

    static void disablePrimesLogging() {
        disabledForTestOnly = true;
    }

    /* renamed from: com.google.android.libraries.performance.primes.battery.HashingNameSanitizer$1 */
    static /* synthetic */ class C11121 {

        /* renamed from: $SwitchMap$com$google$android$libraries$performance$primes$battery$HashingNameSanitizer$NameType */
        static final /* synthetic */ int[] f129x7ad60374 = new int[NameType.values().length];

        static {
            try {
                f129x7ad60374[NameType.WAKELOCK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f129x7ad60374[NameType.SYNC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f129x7ad60374[NameType.JOB.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f129x7ad60374[NameType.PROCESS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                f129x7ad60374[NameType.SENSOR.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    static String sanitizeName(String name, NameType type) {
        int i = C11121.f129x7ad60374[type.ordinal()];
        if (i == 1) {
            return sanitizeWakelockName(name);
        }
        if (i == 2) {
            return sanitizeSyncName(name);
        }
        if (i != 3) {
            return name;
        }
        return "--";
    }

    static String sanitizeSyncName(String name) {
        String[] chunks = name.split("/");
        if (chunks != null && chunks.length == 3) {
            return chunks[0];
        }
        if (disabledForTestOnly) {
            return MALFORMED_SYNC_SANITIZATION;
        }
        PrimesLog.m48d(TAG, "malformed sync name: %s", name);
        return MALFORMED_SYNC_SANITIZATION;
    }

    static String sanitizeWakelockName(String name) {
        Matcher matcher = SYSTEM_TASK_PATTERN.matcher(name);
        if (!matcher.matches()) {
            if (!disabledForTestOnly) {
                PrimesLog.m48d(TAG, "wakelock: %s", name);
            }
            return name;
        } else if (name.startsWith(SYNC_WAKELOCK_PREFIX)) {
            String valueOf = SYNC_WAKELOCK_PREFIX;
            String valueOf2 = String.valueOf(sanitizeSyncName(name.substring(SYNC_WAKELOCK_PREFIX.length())));
            return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf;
        } else {
            String task = matcher.group(1);
            if (!disabledForTestOnly) {
                PrimesLog.m48d(TAG, "non-sync system task wakelock: %s", task);
            }
            return task;
        }
    }

    HashingNameSanitizer() {
    }

    /* access modifiers changed from: package-private */
    public BatteryMetric.Timer hashRawTimerName(NameType type, BatteryMetric.Timer timer) {
        if (!timer.getName().hasUnhashedName()) {
            return timer;
        }
        BatteryMetric.HashedString.Builder name = (BatteryMetric.HashedString.Builder) timer.getName().toBuilder();
        return (BatteryMetric.Timer) ((BatteryMetric.Timer.Builder) timer.toBuilder()).setName(name.setHash(rawHashFor(name.getUnhashedName(), type)).clearUnhashedName()).build();
    }

    /* access modifiers changed from: package-private */
    public BatteryMetric.Timer sanitizeHashedTimerName(NameType type, BatteryMetric.Timer timer) {
        if (!timer.getName().hasHash()) {
            return timer;
        }
        BatteryMetric.HashedString.Builder name = (BatteryMetric.HashedString.Builder) timer.getName().toBuilder();
        return (BatteryMetric.Timer) ((BatteryMetric.Timer.Builder) timer.toBuilder()).setName(name.setHash(((Long) Preconditions.checkNotNull(this.hashHashMap.get(Long.valueOf(name.getHash())))).longValue())).build();
    }

    /* access modifiers changed from: package-private */
    public long rawHashFor(String name, NameType type) {
        long rawHash = ((Long) Preconditions.checkNotNull(Hashing.hash(name))).longValue();
        if (!this.hashHashMap.containsKey(Long.valueOf(rawHash))) {
            String sanitizedName = sanitizeName(name, type);
            Long sanitizedHash = Hashing.hash(sanitizedName);
            if (!disabledForTestOnly) {
                PrimesLog.m48d(TAG, "Sanitized Hash: [%s] %s -> %s", type, sanitizedName, sanitizedHash);
                PrimesLog.m54v(TAG, "Raw Hash: [%s] %s -> %s", type, name, Long.valueOf(rawHash));
            }
            if (sanitizedHash != null) {
                this.hashHashMap.putIfAbsent(Long.valueOf(rawHash), sanitizedHash);
            }
        }
        return rawHash;
    }

    /* access modifiers changed from: package-private */
    public long sanitizedHashFor(String name, NameType type) {
        return ((Long) Preconditions.checkNotNull(this.hashHashMap.get(Long.valueOf(rawHashFor(name, type))))).longValue();
    }
}
