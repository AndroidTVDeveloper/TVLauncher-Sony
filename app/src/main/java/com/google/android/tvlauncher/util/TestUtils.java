package com.google.android.tvlauncher.util;

import android.app.ActivityManager;
import android.os.Build;

public class TestUtils {
    public static boolean isRunningInTest() {
        return isRunningInTestHarness() || isRunningRobolectric() || ActivityManager.isUserAMonkey();
    }

    public static boolean isRunningInTestHarness() {
        return ActivityManager.isRunningInTestHarness();
    }

    private static boolean isRunningRobolectric() {
        return "robolectric".equals(Build.FINGERPRINT);
    }
}
