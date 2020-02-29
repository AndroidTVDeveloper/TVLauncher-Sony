package com.google.android.tvlauncher.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.UserManager;
import android.util.Log;
import java.util.List;

public class ProfilesManager {
    private static final String RESTRICTED_PROFILE_LAUNCHER_ENTRY_ACTIVITY = "com.android.tv.settings.users.RestrictedProfileActivityLauncherEntry";
    private static final String TAG = "ProfilesManager";
    private static ProfilesManager instance;
    private Context context;
    private UserManager userManager;

    public static ProfilesManager getInstance(Context context2) {
        if (instance == null) {
            instance = new ProfilesManager(context2);
        }
        return instance;
    }

    ProfilesManager(Context context2) {
        this.context = context2.getApplicationContext();
        this.userManager = (UserManager) context2.getSystemService("user");
    }

    public boolean hasRestrictedProfile() {
        return getProfilesInfo() != null;
    }

    public Intent getProfileIntent() {
        ResolveInfo info = getProfilesInfo();
        if (info != null) {
            return getLaunchIntent(info);
        }
        return null;
    }

    public boolean isRestrictedProfile() {
        if (Build.VERSION.SDK_INT >= 28) {
            return this.userManager.isRestrictedProfile();
        }
        try {
            return ((Boolean) this.userManager.getClass().getMethod("isLinkedUser", new Class[0]).invoke(this.userManager, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "Fail to check restricted profile", e);
            return false;
        }
    }

    private Intent getLaunchIntent(ResolveInfo info) {
        ComponentName componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(componentName);
        intent.addFlags(270565376);
        return intent;
    }

    private ResolveInfo getProfilesInfo() {
        Intent mainIntent = new Intent("android.intent.action.MAIN");
        mainIntent.addCategory("android.intent.category.LEANBACK_SETTINGS");
        List<ResolveInfo> rawLaunchPoints = this.context.getPackageManager().queryIntentActivities(mainIntent, 129);
        if (rawLaunchPoints == null) {
            return null;
        }
        for (ResolveInfo info : rawLaunchPoints) {
            boolean z = true;
            if ((info.activityInfo.applicationInfo.flags & 1) == 0) {
                z = false;
            }
            boolean system = z;
            if (info.activityInfo != null && system && RESTRICTED_PROFILE_LAUNCHER_ENTRY_ACTIVITY.equals(info.activityInfo.name)) {
                return info;
            }
        }
        return null;
    }
}
