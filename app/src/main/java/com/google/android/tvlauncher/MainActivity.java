package com.google.android.tvlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.util.Log;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.home.HomeFragment;
import com.google.android.tvlauncher.home.contentrating.ContentRatingsManager;
import com.google.android.tvlauncher.notifications.NotificationsUtils;
import com.google.android.tvlauncher.phenotype.Flags;
import com.google.android.tvlauncher.trace.AppTrace;
import com.google.android.tvlauncher.util.BootCompletedActivityHelper;
import com.google.android.tvlauncher.util.LaunchOnBootCompletedHelper;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.OemAppLauncher;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.util.Set;

public class MainActivity extends BlockForDataLauncherActivity implements TvHomeDrawnManager.OnFullyDrawnListener {
    private static final long BLOCK_TIMEOUT = 4000;
    private static final int BOOT_COMPLETED_REQUEST = 0;
    private static final long CONTENT_RATINGS_MANAGER_STARTUP_DELAY_MILLIS = 2000;
    private static final boolean DEBUG = false;
    private static final String TAG = "TvLauncherMainActivity";
    private static final ConditionVariable lock = new ConditionVariable(false);
    private final BootCompletedActivityHelper bootCompletedHelper = new BootCompletedActivityHelper(this);
    private final LaunchOnBootCompletedHelper launchOnBootCompletedHelper = new LaunchOnBootCompletedHelper(this);
    private final OemAppLauncher oemAppLauncher = new OemAppLauncher();

    public MainActivity() {
        super("Home", TvLauncherConstants.HOME_PAGE);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        AppTrace.beginSection("onCreate");
        try {
            super.onCreate(savedInstanceState);
            TvHomeDrawnManager.getInstance().setFullyDrawnListener(this);
            AppTrace.endSection();
            new Handler().postDelayed(new MainActivity$$Lambda$0(this), 2000);
        } catch (Throwable th) {
            AppTrace.endSection();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onCreate$0$MainActivity() {
        ContentRatingsManager.getInstance(this).preload();
    }

    public void onCreateAddContent(Bundle savedInstanceState) {
        if (this.launchOnBootCompletedHelper.isFirstLaunchAfterBoot()) {
            LaunchOnBootCompletedHelper launchOnBootCompletedHelper2 = this.launchOnBootCompletedHelper;
            ConditionVariable conditionVariable = lock;
            conditionVariable.getClass();
            launchOnBootCompletedHelper2.loadLaunchOnBootFlagsAsync(MainActivity$$Lambda$1.get$Lambda(conditionVariable));
            lock.block(BLOCK_TIMEOUT);
            if (this.bootCompletedHelper.isBootCompletedActivityDone()) {
                startLaunchOnBootCompletedHelperIfNeeded();
            }
        }
        setContentView(C1167R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(C1167R.C1170id.home_view_container, new HomeFragment()).commit();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        AppTrace.beginSection("onStart");
        try {
            super.onStart();
        } finally {
            AppTrace.endSection();
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        AppTrace.beginSection("onStop");
        try {
            super.onStop();
            TvHomeDrawnManager.getInstance().removeFullyDrawnListener();
        } finally {
            AppTrace.endSection();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        AppTrace.beginSection("onResume");
        try {
            super.onResume();
            startBootCompletedActivityIfNeeded();
            NotificationsUtils.showUnshownNotifications(this);
        } finally {
            AppTrace.endSection();
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        AppTrace.beginSection("onPause");
        try {
            super.onPause();
        } finally {
            AppTrace.endSection();
        }
    }

    public void onNewIntent(Intent intent) {
        AppTrace.beginSection("onNewIntent");
        try {
            super.onNewIntent(intent);
            Set<String> categories = intent.getCategories();
            if (categories != null && categories.contains("android.intent.category.HOME")) {
                MainBackHomeController.getInstance().onHomePressed(this);
            }
        } finally {
            AppTrace.endSection();
        }
    }

    public void onBackPressed() {
        MainBackHomeController.getInstance().onBackPressed(this);
    }

    public void onUserInteraction() {
        HomeFragment fragment = (HomeFragment) getFragmentManager().findFragmentById(C1167R.C1170id.home_view_container);
        if (fragment != null) {
            fragment.onUserInteraction();
        }
    }

    private void startBootCompletedActivityIfNeeded() {
        if (!this.bootCompletedHelper.isBootCompletedActivityDone()) {
            Intent intent = this.bootCompletedHelper.getBootCompletedIntent();
            if (intent != null) {
                startActivityForResult(intent, 0);
                return;
            }
            this.bootCompletedHelper.onBootCompletedActivityDone();
            startLaunchOnBootCompletedHelperIfNeeded();
            return;
        }
        startLaunchOnBootCompletedHelperIfNeeded();
    }

    private void startLaunchOnBootCompletedHelperIfNeeded() {
        if (Flags.getLaunchOemOnWakeFeatureFlag()) {
            if (this.launchOnBootCompletedHelper.isLoaded() && this.oemAppLauncher.onBoot(this, this.launchOnBootCompletedHelper.getLastForegroundPackageBeforeShutdown(), OemConfiguration.get(this).getPackageNameLaunchAfterBoot(), OemConfiguration.get(this).shouldForceLaunchPackageAfterBoot(), OemConfiguration.get(this).shouldLaunchOemUseMainIntent())) {
                finish();
            }
        } else if (this.launchOnBootCompletedHelper.isLoaded() && this.launchOnBootCompletedHelper.isFirstLaunchAfterBoot() && this.launchOnBootCompletedHelper.tryLaunchingOemPackage()) {
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == -1) {
                this.bootCompletedHelper.onBootCompletedActivityDone();
            }
            startLaunchOnBootCompletedHelperIfNeeded();
        }
    }

    public void onFullyDrawn() {
        Log.i(TAG, "reportFullyDrawn() is called");
        reportFullyDrawn();
    }
}
