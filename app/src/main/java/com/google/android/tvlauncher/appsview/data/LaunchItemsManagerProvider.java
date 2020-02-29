package com.google.android.tvlauncher.appsview.data;

import android.content.Context;

public class LaunchItemsManagerProvider {
    private static LaunchItemsManager launchItemsManager;

    public static LaunchItemsManager getInstance(Context context) {
        if (launchItemsManager == null) {
            launchItemsManager = new FlavorSpecificLaunchItemsManager(context.getApplicationContext());
        }
        return launchItemsManager;
    }

    public static void setInstance(LaunchItemsManager launchItemsManager2) {
        launchItemsManager = launchItemsManager2;
    }
}
