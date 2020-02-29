package com.google.android.tvlauncher.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.tvrecommendations.shared.util.OemUtils;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;

public class LaunchOnBootCompletedHelper {
    private static final String CURRENT_SDK_INT_KEY = "current_sdk_int";
    private static final String LAUNCH_BOOT_COUNT_KEY = "launch_boot_count";
    /* access modifiers changed from: private */
    public static final String[] PROJECTION = {"package_name"};
    private static final String TAG = "LaunchOnBootCompletedHelper";
    private boolean checkedLaunchAfterBoot;
    private final Context context;
    private boolean forceLaunchOemPackage;
    private boolean foregroundActivityWasLaunchPackage;
    private boolean isLoaded;
    private boolean isOperatorTier;
    private String lastForegroundPackageBeforeShutdown;
    private String oemPackage;

    public interface OnDataLoadCompleteListener {
        void onDataLoadComplete();
    }

    private interface SetLoadedDataCallback {
        void setLoadedData(boolean z, boolean z2, boolean z3, String str, String str2);
    }

    public LaunchOnBootCompletedHelper(Context context2) {
        this.context = context2;
    }

    public boolean isFirstLaunchAfterBoot() {
        if (!this.checkedLaunchAfterBoot) {
            this.checkedLaunchAfterBoot = PreferenceManager.getDefaultSharedPreferences(this.context).getInt(LAUNCH_BOOT_COUNT_KEY, -1) >= getBootCount();
        }
        return !this.checkedLaunchAfterBoot;
    }

    public boolean tryLaunchingOemPackage() {
        boolean oemPackageWasLaunched = false;
        if (!TextUtils.isEmpty(this.oemPackage) && ((this.isOperatorTier && this.forceLaunchOemPackage) || (!isFirstBootOrPostOta() && this.foregroundActivityWasLaunchPackage))) {
            oemPackageWasLaunched = launchOemPackage(this.oemPackage);
        }
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putInt(LAUNCH_BOOT_COUNT_KEY, getBootCount()).apply();
        this.checkedLaunchAfterBoot = true;
        return oemPackageWasLaunched;
    }

    public void loadLaunchOnBootFlagsAsync(OnDataLoadCompleteListener listener) {
        new LoadLaunchOnBootFlagsTask(new LaunchOnBootCompletedHelper$$Lambda$0(this), listener).execute(this.context);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$loadLaunchOnBootFlagsAsync$0$LaunchOnBootCompletedHelper(boolean forceLaunch, boolean isOperatorTier2, boolean foregroundActivityWasLaunchPackage2, String oemPackage2, String lastForegroundActivityBeforeShutdown) {
        this.forceLaunchOemPackage = forceLaunch;
        this.isOperatorTier = isOperatorTier2;
        this.foregroundActivityWasLaunchPackage = foregroundActivityWasLaunchPackage2;
        this.oemPackage = oemPackage2;
        this.lastForegroundPackageBeforeShutdown = lastForegroundActivityBeforeShutdown;
        this.isLoaded = true;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public String getLastForegroundPackageBeforeShutdown() {
        return this.lastForegroundPackageBeforeShutdown;
    }

    private boolean isFirstBootOrPostOta() {
        SharedPreferences bootPrefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        int currentSdkInt = bootPrefs.getInt(CURRENT_SDK_INT_KEY, -1);
        if (currentSdkInt == -1) {
            currentSdkInt = Build.VERSION.SDK_INT;
            bootPrefs.edit().putInt(CURRENT_SDK_INT_KEY, currentSdkInt).apply();
        }
        boolean postOta = currentSdkInt != Build.VERSION.SDK_INT;
        if (postOta) {
            bootPrefs.edit().putInt(CURRENT_SDK_INT_KEY, currentSdkInt).apply();
        }
        if (postOta || getBootCount() == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean launchOemPackage(String pkgName) {
        Intent intent = IntentUtil.createForceLaunchOnBootIntent(pkgName);
        try {
            this.context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            String valueOf = String.valueOf(intent);
            String valueOf2 = String.valueOf(e);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 40 + String.valueOf(valueOf2).length());
            sb.append("Activity for intent : ");
            sb.append(valueOf);
            sb.append(", was not found : ");
            sb.append(valueOf2);
            Log.e(TAG, sb.toString());
            return false;
        }
    }

    private int getBootCount() {
        return Settings.Global.getInt(this.context.getContentResolver(), "boot_count", 0);
    }

    /* access modifiers changed from: package-private */
    public void setIsOperatorTier(boolean isOperator) {
        this.isOperatorTier = isOperator;
    }

    private static class LoadLaunchOnBootFlagsTask extends AsyncTask<Context, Void, Void> {
        private OnDataLoadCompleteListener dataLoadCompleteListener;
        private SetLoadedDataCallback setLoadedDataCallback;

        LoadLaunchOnBootFlagsTask(SetLoadedDataCallback callback, OnDataLoadCompleteListener listener) {
            this.setLoadedDataCallback = callback;
            this.dataLoadCompleteListener = listener;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Context... contexts) {
            String lastForegroundActivityBeforeShutdown;
            Throwable th;
            boolean foregroundActivityWasLaunchPackage = false;
            Context context = contexts[0];
            OemConfiguration oemConfiguration = OemConfiguration.get(context);
            String oemPackage = oemConfiguration.getPackageNameLaunchAfterBoot();
            boolean forceLaunch = oemConfiguration.shouldForceLaunchPackageAfterBoot();
            String lastForegroundActivityBeforeShutdown2 = null;
            try {
                Cursor cursor = context.getContentResolver().query(ForegroundActivityContract.FOREGROUND_ACTIVITY_URI, LaunchOnBootCompletedHelper.PROJECTION, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            lastForegroundActivityBeforeShutdown2 = cursor.getString(0);
                            foregroundActivityWasLaunchPackage = TextUtils.equals(oemPackage, lastForegroundActivityBeforeShutdown2);
                        }
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Exception e) {
                                e = e;
                                lastForegroundActivityBeforeShutdown2 = null;
                                String valueOf = String.valueOf(e);
                                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 55);
                                sb.append("Error in retrieving foreground activity package name : ");
                                sb.append(valueOf);
                                Log.e(LaunchOnBootCompletedHelper.TAG, sb.toString());
                                lastForegroundActivityBeforeShutdown = lastForegroundActivityBeforeShutdown2;
                                this.setLoadedDataCallback.setLoadedData(forceLaunch, OemUtils.isOperatorTierDevice(context), foregroundActivityWasLaunchPackage, oemPackage, lastForegroundActivityBeforeShutdown);
                                this.dataLoadCompleteListener.onDataLoadComplete();
                                return null;
                            } catch (Throwable th4) {
                                ThrowableExtension.addSuppressed(th, th4);
                            }
                        }
                        throw th3;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                lastForegroundActivityBeforeShutdown = lastForegroundActivityBeforeShutdown2;
            } catch (Exception e2) {
                e = e2;
                String valueOf2 = String.valueOf(e);
                StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 55);
                sb2.append("Error in retrieving foreground activity package name : ");
                sb2.append(valueOf2);
                Log.e(LaunchOnBootCompletedHelper.TAG, sb2.toString());
                lastForegroundActivityBeforeShutdown = lastForegroundActivityBeforeShutdown2;
                this.setLoadedDataCallback.setLoadedData(forceLaunch, OemUtils.isOperatorTierDevice(context), foregroundActivityWasLaunchPackage, oemPackage, lastForegroundActivityBeforeShutdown);
                this.dataLoadCompleteListener.onDataLoadComplete();
                return null;
            }
            this.setLoadedDataCallback.setLoadedData(forceLaunch, OemUtils.isOperatorTierDevice(context), foregroundActivityWasLaunchPackage, oemPackage, lastForegroundActivityBeforeShutdown);
            this.dataLoadCompleteListener.onDataLoadComplete();
            return null;
        }
    }
}
