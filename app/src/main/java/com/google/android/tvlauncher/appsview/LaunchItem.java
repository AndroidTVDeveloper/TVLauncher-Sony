package com.google.android.tvlauncher.appsview;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

public class LaunchItem implements Comparable<LaunchItem> {
    public static final int PROGRESS_UNKNOWN = -1;
    private static final String TAG = "LaunchItem";
    private String bannerUri;
    private String dataUri;
    private String iconUri;
    private int installProgressPercent = -1;
    private InstallState installState = InstallState.UNKNOWN;
    private Intent intent;
    private boolean isAppLink;
    private boolean isGame;
    private boolean isInitialInstall;
    private CharSequence label;
    private long lastUpdateTime;
    private String pkgName;
    private ResolveInfo resolveInfo;

    public enum InstallState {
        UNKNOWN,
        INSTALL_PENDING,
        UPDATE_PENDING,
        RESTORE_PENDING,
        DOWNLOADING,
        INSTALLING
    }

    public Intent getIntent() {
        return this.intent;
    }

    public CharSequence getLabel() {
        return this.label;
    }

    public String getPackageName() {
        return this.pkgName;
    }

    public String getIconUri() {
        return this.iconUri;
    }

    public String getBannerUri() {
        return this.bannerUri;
    }

    public String getDataUri() {
        return this.dataUri;
    }

    public ResolveInfo getResolveInfo() {
        return this.resolveInfo;
    }

    public int getInstallProgressPercent() {
        return this.installProgressPercent;
    }

    public boolean isInitialInstall() {
        return this.isInitialInstall;
    }

    public boolean isInstalling() {
        return this.installState != InstallState.UNKNOWN;
    }

    public InstallState getInstallState() {
        return this.installState;
    }

    public boolean isGame() {
        return this.isGame;
    }

    public boolean isAppLink() {
        return this.isAppLink;
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public int compareTo(LaunchItem another) {
        CharSequence o1Label = getLabel();
        CharSequence o2Label = another.getLabel();
        if (o1Label == null && o2Label == null) {
            return 0;
        }
        if (o1Label == null) {
            return 1;
        }
        if (o2Label == null) {
            return -1;
        }
        return o1Label.toString().compareToIgnoreCase(o2Label.toString());
    }

    public int hashCode() {
        return this.pkgName.hashCode();
    }

    public String toString() {
        String valueOf = String.valueOf(this.label);
        String packageName = getPackageName();
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 4 + String.valueOf(packageName).length());
        sb.append(valueOf);
        sb.append(" -- ");
        sb.append(packageName);
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LaunchItem)) {
            return false;
        }
        return TextUtils.equals(this.pkgName, ((LaunchItem) other).getPackageName());
    }

    public boolean hasSamePackageName(ResolveInfo info) {
        return TextUtils.equals(this.pkgName, info.activityInfo.packageName);
    }

    public boolean hasSamePackageName(String packageName) {
        return TextUtils.equals(this.pkgName, packageName);
    }

    public void setInitialInstall(boolean initialInstall) {
        this.isInitialInstall = initialInstall;
    }

    public void setInstallProgressPercent(int progressPercent) {
        this.installProgressPercent = progressPercent;
    }

    public void setIntent(Intent intent2) {
        this.intent = intent2;
    }

    public void setLabel(CharSequence label2) {
        this.label = label2;
    }

    public void setPackageName(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public void setIconUri(String iconUri2) {
        this.iconUri = iconUri2;
    }

    public void setBannerUri(String bannerUri2) {
        this.bannerUri = bannerUri2;
    }

    public void setDataUri(String dataUri2) {
        this.dataUri = dataUri2;
    }

    public void setResolveInfo(ResolveInfo resolveInfo2) {
        this.resolveInfo = resolveInfo2;
    }

    public void setIsGame(boolean isGame2) {
        this.isGame = isGame2;
    }

    public void setIsAppLink(boolean isAppLink2) {
        this.isAppLink = isAppLink2;
    }

    public void setInstallState(InstallState state) {
        this.installState = state;
    }

    public void setLastUpdateTime(long lastUpdateTime2) {
        this.lastUpdateTime = lastUpdateTime2;
    }

    public void recycle() {
        this.installProgressPercent = -1;
        this.installState = InstallState.UNKNOWN;
        this.intent = null;
        this.label = null;
        this.pkgName = null;
        this.isInitialInstall = false;
        this.isGame = false;
        this.resolveInfo = null;
        this.iconUri = null;
        this.bannerUri = null;
        this.isAppLink = false;
        this.dataUri = null;
    }
}
