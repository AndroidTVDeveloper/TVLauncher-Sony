package com.google.android.tvlauncher.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.p001v4.content.IntentCompat;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.data.PackagesWithChannelsObserver;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.model.ChannelPackage;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.ArrayList;
import java.util.List;

class AppModel {
    private Context context;
    private LoadAppsCallback loadAppsCallback;
    final PackagesWithChannelsObserver packagesObserver = new PackagesWithChannelsObserver() {
        public void onPackagesChange() {
            AppModel.this.onPackagesDataLoaded();
        }
    };
    private TvDataManager tvDataManager;

    interface LoadAppsCallback {
        void onAppsChanged();

        void onAppsLoaded(List<AppInfo> list);
    }

    static class AppInfo implements Comparable<AppInfo> {
        ChannelPackage channelPackage;
        final String packageName;
        ResolveInfo resolveInfo;
        CharSequence title;

        AppInfo(String packageName2, ChannelPackage channelPackage2, ApplicationInfo applicationInfo, PackageManager packageManager) {
            this(packageName2, channelPackage2, packageManager.getApplicationLabel(applicationInfo), AppModel.getResolveInfo(packageName2, packageManager));
        }

        AppInfo(String packageName2, ChannelPackage channelPackage2, CharSequence title2, ResolveInfo resolveInfo2) {
            this.packageName = packageName2;
            this.channelPackage = channelPackage2;
            this.title = title2;
            this.resolveInfo = resolveInfo2;
        }

        public int compareTo(AppInfo o) {
            CharSequence charSequence = this.title;
            if (charSequence == null) {
                return o.title != null ? 1 : 0;
            }
            return charSequence.toString().compareToIgnoreCase(o.title.toString());
        }
    }

    AppModel(Context context2) {
        this.context = context2;
        this.tvDataManager = TvDataManager.getInstance(context2);
        this.tvDataManager.registerPackagesWithChannelsObserver(this.packagesObserver);
    }

    AppModel(Context context2, TvDataManager tvDataManager2) {
        this.context = context2;
        this.tvDataManager = tvDataManager2;
    }

    /* access modifiers changed from: package-private */
    public void onPause() {
        this.loadAppsCallback = null;
        this.tvDataManager.unregisterPackagesWithChannelsObserver(this.packagesObserver);
    }

    /* access modifiers changed from: package-private */
    public void loadApps(LoadAppsCallback callback) {
        this.loadAppsCallback = callback;
        this.tvDataManager.registerPackagesWithChannelsObserver(this.packagesObserver);
        if (this.tvDataManager.isPackagesWithChannelsDataLoaded()) {
            onPackagesDataLoaded();
        } else {
            this.tvDataManager.loadPackagesWithChannelsData();
        }
    }

    /* access modifiers changed from: private */
    public void onPackagesDataLoaded() {
        if (this.loadAppsCallback != null) {
            List<ChannelPackage> channelPackages = this.tvDataManager.getPackagesWithChannels();
            List<AppInfo> applicationInfos = new ArrayList<>(channelPackages.size());
            PackageManager packageManager = this.context.getPackageManager();
            for (ChannelPackage channelPackage : channelPackages) {
                String packageName = channelPackage.getPackageName();
                if (Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME.equals(packageName)) {
                    applicationInfos.add(new AppInfo(packageName, channelPackage, this.context.getString(C1167R.string.promotional_channel_setting_panel_title), (ResolveInfo) null));
                } else {
                    ApplicationInfo applicationInfo = getApplicationInfo(packageName, packageManager);
                    if (applicationInfo != null) {
                        applicationInfos.add(new AppInfo(packageName, channelPackage, applicationInfo, packageManager));
                    }
                }
            }
            this.loadAppsCallback.onAppsLoaded(applicationInfos);
        }
    }

    private ApplicationInfo getApplicationInfo(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static ResolveInfo getResolveInfo(String packageName, PackageManager packageManager) {
        Intent mainIntent = new Intent("android.intent.action.MAIN");
        mainIntent.setPackage(packageName).addCategory(IntentCompat.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> infos = packageManager.queryIntentActivities(mainIntent, 1);
        if (infos == null || infos.size() <= 0) {
            return null;
        }
        return infos.get(0);
    }
}
