package com.google.android.tvlauncher.appsview.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.tvlauncher.util.PackageUtils;
import com.google.android.tvrecommendations.shared.util.Constants;

public class InstallingLaunchItemsDataHelper {
    private static final String LAUNCHER_VERSION_KEY = "launcher_version";
    private static final String PREFERENCE_FILE_NAME = "installing_launch_items_data";
    private static InstallingLaunchItemsDataHelper installingLaunchItemsDataHelper;
    private final SharedPreferences prefs;

    public static InstallingLaunchItemsDataHelper getInstance(Context context) {
        if (installingLaunchItemsDataHelper == null) {
            synchronized (InstallingLaunchItemsDataHelper.class) {
                if (installingLaunchItemsDataHelper == null) {
                    installingLaunchItemsDataHelper = new InstallingLaunchItemsDataHelper(context.getApplicationContext());
                }
            }
        }
        return installingLaunchItemsDataHelper;
    }

    static void setInstance(InstallingLaunchItemsDataHelper installingLaunchItemsDataHelper2) {
        installingLaunchItemsDataHelper = installingLaunchItemsDataHelper2;
    }

    private InstallingLaunchItemsDataHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
        this.prefs.edit().putInt(LAUNCHER_VERSION_KEY, PackageUtils.getApplicationVersionCode(context, Constants.LAUNCHER_PACKAGE_NAME)).apply();
    }

    /* access modifiers changed from: package-private */
    public void storeDataForPackage(String pkgName, boolean isGame) {
        this.prefs.edit().putBoolean(pkgName, isGame).apply();
    }

    /* access modifiers changed from: package-private */
    public void removeGameFlagForPackage(String pkgName) {
        this.prefs.edit().remove(pkgName).apply();
    }

    /* access modifiers changed from: package-private */
    public boolean getGameFlagForPackage(String packageName) {
        return this.prefs.getBoolean(packageName, false);
    }

    /* access modifiers changed from: package-private */
    public void clearForTesting() {
        this.prefs.edit().clear().putInt(LAUNCHER_VERSION_KEY, this.prefs.getInt(LAUNCHER_VERSION_KEY, -1)).apply();
    }
}
