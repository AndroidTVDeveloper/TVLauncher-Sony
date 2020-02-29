package com.google.android.tvlauncher.application;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.os.Process;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.util.ClearGlideCacheOnLocaleChangeReceiver;
import com.google.android.tvlauncher.util.GoogleConfigurationManager;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;
import com.google.android.tvlauncher.util.TestUtils;
import com.google.android.tvlauncher.wallpaper.WallpaperInstaller;
import java.util.List;

public abstract class TvLauncherApplicationBase extends Application {
    private static final String TAG = "TvLauncherApplicationBase";
    private static final String YOUTUBE_PROCESS_NAME = ":youtube";
    private GoogleConfigurationManager googleConfigurationManager;
    private final Object googleConfigurationManagerLock = new Object();
    private IntentLaunchDispatcher intentLauncher;
    private final Object intentLauncherLock = new Object();

    public void onCreate() {
        super.onCreate();
        if (!Build.TYPE.equals("unknown") && !isRemoteYoutubePlayerProcess() && !TestUtils.isRunningInTest()) {
            LaunchItemsManagerProvider.getInstance(this).registerUpdateListeners();
            if (getSystemService("wallpaper") != null) {
                WallpaperInstaller.getInstance().installWallpaperIfNeeded(this);
            }
            registerReceiver(new ClearGlideCacheOnLocaleChangeReceiver(), ClearGlideCacheOnLocaleChangeReceiver.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRemoteYoutubePlayerProcess() {
        List<ActivityManager.RunningAppProcessInfo> processes;
        int pid = Process.myPid();
        ActivityManager am = (ActivityManager) getSystemService(ActivityManager.class);
        if (am == null || (processes = am.getRunningAppProcesses()) == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.pid == pid && processInfo.processName != null && processInfo.processName.contains(YOUTUBE_PROCESS_NAME)) {
                return true;
            }
        }
        return false;
    }

    public GoogleConfigurationManager getGoogleConfigurationManager() {
        if (this.googleConfigurationManager == null) {
            synchronized (this.googleConfigurationManagerLock) {
                if (this.googleConfigurationManager == null) {
                    this.googleConfigurationManager = new GoogleConfigurationManager(this);
                }
            }
        }
        return this.googleConfigurationManager;
    }

    public IntentLaunchDispatcher getIntentLauncher() {
        if (this.intentLauncher == null) {
            synchronized (this.intentLauncherLock) {
                if (this.intentLauncher == null) {
                    this.intentLauncher = new IntentLaunchDispatcher(this);
                }
            }
        }
        return this.intentLauncher;
    }
}
