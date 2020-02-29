package com.google.android.tvrecommendations.shared.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.exoplayer2.C0847C;
import com.google.android.tvrecommendations.shared.oemconfiguration.LaunchOemOnStartConfiguration;

public class OemAppLauncher {
    private static final String ACTION_FORCE_LAUNCH_ON_BOOT = "com.android.tv.action.FORCE_LAUNCH_ON_BOOT";
    private static final String CURRENT_SDK_INT_KEY = "current_sdk_int";
    private static final boolean DEBUG = false;
    private static final String GOOGLE_PLAY_EXTRA_CALLER_ID = "callerId";
    private static final String GOOGLE_PLAY_PARAM_ID = "id";
    private static final String GOOGLE_PLAY_URI = "market://details";
    private static final String LAUNCH_BOOT_COUNT_KEY = "launch_boot_count";
    private static final String TAG = "OemAppLauncher";
    private static boolean cachedIsFirstBootOrPostOta = false;
    private static boolean cachedIsFirstLaunchAfterBoot = false;
    private long lastSleepTime = 0;
    private String launcherPackage;

    public void onSleep(Context context, LaunchOemOnStartConfiguration oemConfig) {
        if (oemConfig.shouldLaunchOnWake()) {
            this.lastSleepTime = System.currentTimeMillis();
            Intent homeIntent = new Intent("android.intent.action.MAIN");
            homeIntent.addCategory("android.intent.category.HOME");
            homeIntent.setFlags(C0847C.ENCODING_PCM_MU_LAW);
            ComponentName resolvedActivity = homeIntent.resolveActivity(context.getPackageManager());
            if (resolvedActivity != null) {
                this.launcherPackage = resolvedActivity.getPackageName();
                context.startActivity(homeIntent);
            }
        }
    }

    public void onWake(Context context, LaunchOemOnStartConfiguration oemConfig, String topmostPackageAtSleep, String topmostPackageAtWake) {
        Intent launchIntent;
        if (oemConfig.shouldLaunchOnWake() && topmostPackageAtWake.equals(this.launcherPackage)) {
            long timeAsleep = System.currentTimeMillis() - this.lastSleepTime;
            String oemPackage = oemConfig.getPackageNameLaunchAfterBoot();
            if (timeAsleep >= ((long) (oemConfig.getMinimumSleepForLaunchOnWakeSeconds() * 1000))) {
                launchOemPackage(context, oemPackage, oemConfig.shouldLaunchOemUseMainIntent());
            } else if (!topmostPackageAtSleep.equals(this.launcherPackage) && (launchIntent = context.getPackageManager().getLeanbackLaunchIntentForPackage(topmostPackageAtSleep)) != null) {
                launchIntent.addFlags(270532608);
                context.startActivity(launchIntent);
            }
        }
    }

    public boolean onBoot(Context context, String lastOpenPackageBeforeShutdown, String oemPackageLaunchAfterBoot, boolean shouldForceLaunchPackageAfterBoot, boolean shouldLaunchOemUseMainIntent) {
        if (!readAndStoreIsFirstLaunchAfterBoot(context) || TextUtils.isEmpty(oemPackageLaunchAfterBoot)) {
            return false;
        }
        boolean foregroundActivityWasOemPackage = oemPackageLaunchAfterBoot.equals(lastOpenPackageBeforeShutdown);
        boolean isFirstBootOrPostOta = readAndStoreIsFirstBootOrPostOta(context);
        if ((!OemUtils.isOperatorTierDevice(context) || !shouldForceLaunchPackageAfterBoot) && (isFirstBootOrPostOta || !foregroundActivityWasOemPackage)) {
            return false;
        }
        return launchOemPackage(context, oemPackageLaunchAfterBoot, shouldLaunchOemUseMainIntent);
    }

    private static boolean readAndStoreIsFirstLaunchAfterBoot(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int storedBootCount = prefs.getInt(LAUNCH_BOOT_COUNT_KEY, -1);
        int currentBootCount = getBootCount(context);
        if (storedBootCount < currentBootCount) {
            prefs.edit().putInt(LAUNCH_BOOT_COUNT_KEY, currentBootCount).apply();
            cachedIsFirstLaunchAfterBoot = true;
        }
        return cachedIsFirstLaunchAfterBoot;
    }

    private static boolean readAndStoreIsFirstBootOrPostOta(Context context) {
        SharedPreferences bootPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (bootPrefs.getInt(CURRENT_SDK_INT_KEY, -1) != Build.VERSION.SDK_INT) {
            bootPrefs.edit().putInt(CURRENT_SDK_INT_KEY, Build.VERSION.SDK_INT).apply();
            cachedIsFirstBootOrPostOta = true;
        }
        return cachedIsFirstBootOrPostOta;
    }

    private static boolean launchOemPackage(Context context, String packageName, boolean shouldLaunchOemUseMainIntent) {
        Intent intent = buildLaunchIntent(context, packageName, shouldLaunchOemUseMainIntent);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        } else if (isPackageInstalledAndEnabled(context, packageName)) {
            StringBuilder sb = new StringBuilder(99);
            sb.append("Launch on wake/boot is misconfigured: use_main_intent is ");
            sb.append(shouldLaunchOemUseMainIntent);
            sb.append(" but no matching intent filter exists");
            Log.e(TAG, sb.toString());
            return false;
        } else {
            StringBuilder sb2 = new StringBuilder(String.valueOf(packageName).length() + 45);
            sb2.append("Package to launch on wake/boot ");
            sb2.append(packageName);
            sb2.append(" was not found");
            Log.w(TAG, sb2.toString());
            return openPlayStoreForPackage(context, packageName);
        }
    }

    private static Intent buildLaunchIntent(Context context, String packageName, boolean shouldLaunchOemUseMainIntent) {
        Intent intent;
        if (shouldLaunchOemUseMainIntent) {
            intent = context.getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
        } else {
            intent = new Intent(ACTION_FORCE_LAUNCH_ON_BOOT);
            intent.setPackage(packageName);
        }
        intent.addFlags(270532608);
        return intent;
    }

    private static boolean openPlayStoreForPackage(Context context, String packageName) {
        Intent flags = new Intent("android.intent.action.VIEW").setFlags(C0847C.ENCODING_PCM_MU_LAW);
        String valueOf = "market://details?id=";
        String valueOf2 = String.valueOf(packageName);
        Intent playStoreIntent = flags.setData(Uri.parse(valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf)).putExtra(GOOGLE_PLAY_EXTRA_CALLER_ID, context.getPackageName()).setPackage("com.android.vending");
        if (playStoreIntent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        context.startActivity(playStoreIntent);
        return true;
    }

    private static boolean isPackageInstalledAndEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static int getBootCount(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "boot_count", 0);
    }
}
