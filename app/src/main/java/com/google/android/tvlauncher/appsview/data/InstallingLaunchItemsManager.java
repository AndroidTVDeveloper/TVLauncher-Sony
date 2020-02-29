package com.google.android.tvlauncher.appsview.data;

import android.content.Context;
import com.google.android.tvlauncher.appsview.LaunchItem;

class InstallingLaunchItemsManager implements InstallingLaunchItemListener {
    private static InstallingLaunchItemsManager installingLaunchItemsManager;
    private final Context context;
    private InstallingLaunchItemListener installingLaunchItemListener;

    public static InstallingLaunchItemsManager getInstance(Context context2) {
        if (installingLaunchItemsManager == null) {
            synchronized (InstallingLaunchItemsManager.class) {
                if (installingLaunchItemsManager == null) {
                    installingLaunchItemsManager = new InstallingLaunchItemsManager(context2);
                }
            }
        }
        return installingLaunchItemsManager;
    }

    private InstallingLaunchItemsManager(Context context2) {
        this.context = context2.getApplicationContext();
    }

    public void onInstallingLaunchItemAdded(LaunchItem launchItem) {
    }

    public void onInstallingLaunchItemChanged(LaunchItem launchItem) {
    }

    public void onInstallingLaunchItemRemoved(LaunchItem launchItem, boolean success) {
    }

    public void setInstallingLaunchItemListener(InstallingLaunchItemListener listener) {
        this.installingLaunchItemListener = listener;
    }
}
