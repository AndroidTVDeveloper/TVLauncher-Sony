package com.google.android.tvlauncher.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.google.android.tvrecommendations.shared.util.AppUtil;
import java.util.List;

public class BootCompletedActivityHelper {
    private static final String ACTION_TV_BOOT_COMPLETED = "android.intent.action.TV_BOOT_COMPLETED";
    private static final String PREF_BOOT_COMPLETED_BOOT_COUNT = "boot.completed.boot.count";
    private final Context context;
    private boolean done;

    public BootCompletedActivityHelper(Context context2) {
        this.context = context2;
    }

    public boolean isBootCompletedActivityDone() {
        if (!this.done) {
            this.done = PreferenceManager.getDefaultSharedPreferences(this.context).getInt(PREF_BOOT_COMPLETED_BOOT_COUNT, -1) >= getBootCount();
        }
        return this.done;
    }

    public void onBootCompletedActivityDone() {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putInt(PREF_BOOT_COMPLETED_BOOT_COUNT, getBootCount()).apply();
        this.done = true;
    }

    public Intent getBootCompletedIntent() {
        List<ResolveInfo> infoList = this.context.getPackageManager().queryIntentActivities(new Intent(ACTION_TV_BOOT_COMPLETED), 0);
        if (infoList == null) {
            return null;
        }
        for (ResolveInfo resolveInfo : infoList) {
            if (AppUtil.isSystemApp(resolveInfo)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                return intent;
            }
        }
        return null;
    }

    private int getBootCount() {
        return Settings.Global.getInt(this.context.getContentResolver(), "boot_count", 0);
    }
}
