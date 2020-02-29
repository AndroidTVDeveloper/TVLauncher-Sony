package com.google.android.tvlauncher.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.android.libraries.gcoreclient.clearcut.impl.GcoreClearcutApiImpl;
import com.google.android.libraries.gcoreclient.clearcut.impl.GcoreClearcutLoggerFactoryImpl;
import com.google.android.libraries.gcoreclient.common.api.impl.GcoreGoogleApiClientImpl;
import com.google.android.libraries.performance.primes.Primes;
import com.google.android.libraries.performance.primes.PrimesApiProvider;
import com.google.android.libraries.performance.primes.PrimesConfigurations;
import com.google.android.libraries.performance.primes.PrimesConfigurationsProvider;
import com.google.android.libraries.performance.primes.PrimesCrashConfigurations;
import com.google.android.libraries.performance.primes.PrimesMemoryConfigurations;
import com.google.android.libraries.performance.primes.PrimesPackageConfigurations;
import com.google.android.libraries.performance.primes.PrimesTimerConfigurations;
import com.google.android.libraries.performance.primes.transmitter.MetricTransmitter;
import com.google.android.libraries.performance.primes.transmitter.impl.ClearcutMetricTransmitter;
import com.google.android.tvlauncher.util.TestUtils;

class PrimesConfiguration {
    private static final String TAG = "PrimesConfiguration";

    private PrimesConfiguration() {
    }

    static void init(final Application application, final PrimesSettings primesSettings) {
        DefaultUncaughtExceptionHandlerVerifier.assertHandlerClass("com.google.android.libraries.social.silentfeedback.SilentFeedbackHandler$BackgroundSilentFeedbackHandler");
        if (primesSettings.isPrimesEnabled()) {
            Primes primes = Primes.initialize(PrimesApiProvider.newInstance(application, new PrimesConfigurationsProvider() {
                public PrimesConfigurations get() {
                    return PrimesConfigurations.newBuilder().setMetricTransmitter(PrimesConfiguration.getPrimesMetricTransmitter(application)).setPackageConfigurations(new PrimesPackageConfigurations(primesSettings.isPackageStatsMetricEnabled())).setMemoryConfigurations(PrimesMemoryConfigurations.newBuilder().setEnabled(primesSettings.isMemoryMetricEnabled()).build()).setCrashConfigurations(PrimesCrashConfigurations.newBuilder().setEnabled(primesSettings.isCrashMetricEnabled()).build()).setTimerConfigurations(PrimesTimerConfigurations.newBuilder().setEnabled(true).build()).build();
                }
            }));
            primes.startMemoryMonitor();
            primes.startCrashMonitor();
            return;
        }
        Log.e(TAG, "PRIMES not enabled");
    }

    /* access modifiers changed from: private */
    public static MetricTransmitter getPrimesMetricTransmitter(Context context) {
        if (TestUtils.isRunningInTest()) {
            return MetricTransmitter.NOOP_TRANSMITTER;
        }
        return new ClearcutMetricTransmitter(context, new GcoreClearcutLoggerFactoryImpl(), new GcoreGoogleApiClientImpl.BuilderFactory(), new GcoreClearcutApiImpl.Builder(), "TV_LAUNCHER_ANDROID_PRIMES");
    }
}
