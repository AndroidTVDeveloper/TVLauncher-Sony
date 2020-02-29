package com.google.android.tvrecommendations.shared.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

public class OemUtils {
    private static final String DYNAMIC_CONFIG_FEATURE = "com.google.android.tv.dynamic_config";
    private static final String OPERATOR_TIER_FEATURE = "com.google.android.tv.operator_tier";

    public static String getCustomizationApp(PackageManager pm) {
        Intent intent;
        ResolveInfo appInfo = null;
        if (isDebugMode()) {
            intent = new Intent(Constants.ACTION_OEM_CUSTOMIZATION_TEST);
        } else {
            intent = new Intent(Constants.ACTION_OEM_CUSTOMIZATION);
        }
        List<ResolveInfo> packages = pm.queryBroadcastReceivers(intent, 0);
        if (packages != null) {
            if (!isDebugMode()) {
                for (ResolveInfo info : packages) {
                    if (AppUtil.isSystemApp(info)) {
                        appInfo = info;
                    }
                }
            } else if (!packages.isEmpty()) {
                appInfo = packages.get(0);
            }
        }
        if (appInfo == null) {
            return null;
        }
        return appInfo.activityInfo.packageName;
    }

    public static boolean isOperatorTierDevice(Context context) {
        return hasSystemFeature(context, OPERATOR_TIER_FEATURE);
    }

    public static boolean isDynamicConfigDevice(Context context) {
        return hasSystemFeature(context, DYNAMIC_CONFIG_FEATURE);
    }

    public static boolean hasSystemFeature(Context context, String featureName) {
        FeatureInfo[] features = context.getPackageManager().getSystemAvailableFeatures();
        if (features == null) {
            return false;
        }
        for (FeatureInfo feature : features) {
            if (featureName.equals(feature.name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDebugMode() {
        return false;
    }
}
