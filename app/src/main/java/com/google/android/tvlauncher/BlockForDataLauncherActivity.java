package com.google.android.tvlauncher;

import android.app.Fragment;
import android.os.Bundle;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.analytics.LoggingActivity;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.util.OemConfiguration;

public abstract class BlockForDataLauncherActivity extends LoggingActivity {
    private static final String BLOCKING_FRAGMENT_TAG = "blocking_fragment_tag";
    private boolean contentFragmentAdded;
    protected boolean isBlockedForData;
    private OemConfiguration.OnDataLoadedListener onDataLoadedListener;

    public abstract void onCreateAddContent(Bundle bundle);

    public BlockForDataLauncherActivity(String name, VisualElementTag visualElementTag) {
        super(name, visualElementTag);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OemConfiguration oemConfiguration = OemConfiguration.get(this);
        if (!oemConfiguration.isDataLoaded()) {
            this.isBlockedForData = true;
            this.onDataLoadedListener = new BlockForDataLauncherActivity$$Lambda$0(this, savedInstanceState);
            oemConfiguration.registerOnDataLoadedListener(this.onDataLoadedListener);
            if (getFragmentManager().findFragmentByTag(BLOCKING_FRAGMENT_TAG) == null) {
                getFragmentManager().beginTransaction().add(16908290, new BlockForDataFragment(), BLOCKING_FRAGMENT_TAG).commit();
                return;
            }
            return;
        }
        lambda$onCreate$0$BlockForDataLauncherActivity(savedInstanceState);
    }

    private void removeBlockingFragment() {
        Fragment fragment = getFragmentManager().findFragmentByTag(BLOCKING_FRAGMENT_TAG);
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if (!this.isBlockedForData) {
            removeBlockingFragment();
            if (!this.contentFragmentAdded) {
                lambda$onCreate$0$BlockForDataLauncherActivity(null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: onCreateContinue */
    public void lambda$onCreate$0$BlockForDataLauncherActivity(Bundle savedInstanceState) {
        this.isBlockedForData = false;
        OemConfiguration.get(this).unregisterOnDataLoadedListener(this.onDataLoadedListener);
        LaunchItemsManagerProvider.getInstance(this).initIfNeeded();
        if (!getFragmentManager().isStateSaved()) {
            removeBlockingFragment();
            onCreateAddContent(savedInstanceState);
            this.contentFragmentAdded = true;
        }
    }
}
