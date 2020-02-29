package com.google.android.tvlauncher.home;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.application.TvLauncherApplication;
import com.google.android.tvlauncher.doubleclick.AdVideoTracker;
import com.google.android.tvlauncher.doubleclick.AdVideoTrackerFactory;
import com.google.android.tvlauncher.doubleclick.AdsManager;
import com.google.android.tvlauncher.doubleclick.AdsUtil;
import com.google.android.tvlauncher.doubleclick.DirectAdConfigSerializer;
import com.google.android.tvlauncher.doubleclick.DirectVideoAd;
import com.google.android.tvlauncher.doubleclick.DoubleClickAdConfigSerializer;
import com.google.android.tvlauncher.doubleclick.OutstreamVideoAd;
import com.google.android.tvlauncher.doubleclick.OutstreamVideoAdFactory;
import com.google.android.tvlauncher.doubleclick.VideoProgressPoller;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import com.google.android.tvlauncher.home.view.ProgramView;
import com.google.android.tvlauncher.instantvideo.widget.InstantVideoView;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.Clock;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;

class SponsoredProgramControllerHelper {
    private static final boolean DEBUG = false;
    private static final long START_TIME_UNINITIALIZED = -1;
    private static final String TAG = "SponsoredProgramControllerHelper";
    private String adId;
    private final AdVideoTracker adVideoTracker;
    private final AdsManager adsManager;
    private final AdsUtil adsUtil;
    private final Clock clock;
    private final DirectAdConfigSerializer directAdConfigSerializer;
    private final DoubleClickAdConfigSerializer doubleClickAdConfigSerializer;
    Runnable doubleClickAdRefreshRunnable;
    Runnable doubleClickAdVisibilityCheckRunnable;
    private final Handler handler;
    private final IntentLaunchDispatcher intentLauncher;
    private boolean isAdFresh;
    private String lastRequestedImpressionUrl;
    private OutstreamVideoAd outstreamVideoAd;
    private final OutstreamVideoAdFactory outstreamVideoAdFactory;
    private final InstantVideoView previewVideo;
    private long programId;
    private int programType;
    private final VideoProgressPoller videoProgressPoller;
    private final ProgramView view;
    private long visibleCheckStartTime;

    SponsoredProgramControllerHelper(ProgramView v) {
        this(v, ((TvLauncherApplication) v.getContext().getApplicationContext()).getAdsManager(), ((TvLauncherApplication) v.getContext().getApplicationContext()).getOutstreamVideoAdFactory(), ((TvLauncherApplication) v.getContext().getApplicationContext()).getDoubleClickAdConfigSerializer(), ((TvLauncherApplication) v.getContext().getApplicationContext()).getDirectAdConfigSerializer(), new Clock(), new Handler(), ((TvLauncherApplication) v.getContext().getApplicationContext()).getIntentLauncher(), ((TvLauncherApplication) v.getContext().getApplicationContext()).getAdsUtil(), null, null);
    }

    SponsoredProgramControllerHelper(ProgramView v, AdsManager adsManager2, OutstreamVideoAdFactory outstreamVideoAdFactory2, DoubleClickAdConfigSerializer doubleClickAdConfigSerializer2, DirectAdConfigSerializer directAdConfigSerializer2, Clock clock2, Handler handler2, IntentLaunchDispatcher intentLauncher2, AdsUtil adsUtil2, VideoProgressPoller videoProgressPoller2, AdVideoTracker adVideoTracker2) {
        AdVideoTracker adVideoTracker3;
        this.visibleCheckStartTime = -1;
        this.doubleClickAdRefreshRunnable = new Runnable() {
            public void run() {
                SponsoredProgramControllerHelper.this.refreshDoubleClickAd();
            }
        };
        this.doubleClickAdVisibilityCheckRunnable = new Runnable() {
            public void run() {
                SponsoredProgramControllerHelper.this.recordImpressionOrRescheduleCheck();
            }
        };
        this.view = v;
        this.previewVideo = (InstantVideoView) v.findViewById(C1167R.C1170id.preview_video_view);
        this.clock = clock2;
        this.handler = handler2;
        this.adsManager = adsManager2;
        this.outstreamVideoAdFactory = outstreamVideoAdFactory2;
        this.doubleClickAdConfigSerializer = doubleClickAdConfigSerializer2;
        this.directAdConfigSerializer = directAdConfigSerializer2;
        this.intentLauncher = intentLauncher2;
        this.adsUtil = adsUtil2;
        this.videoProgressPoller = videoProgressPoller2 != null ? videoProgressPoller2 : new VideoProgressPoller(this.previewVideo);
        if (adVideoTracker2 != null) {
            adVideoTracker3 = adVideoTracker2;
        } else {
            adVideoTracker3 = AdVideoTrackerFactory.createAdVideoTracker(v.getContext());
        }
        this.adVideoTracker = adVideoTracker3;
    }

    /* access modifiers changed from: package-private */
    public boolean bindDoubleClickAdProgram(Program program) {
        this.programId = program.getId();
        this.programType = program.getType();
        this.adId = program.getAdId();
        cleanUpOldCallbacksAndTrackers();
        AdConfig.AdAsset adAsset = null;
        if (!TextUtils.isEmpty(this.adId)) {
            byte[] adConfigSerialized = program.getAdConfigSerialized();
            if (adConfigSerialized != null) {
                adAsset = this.doubleClickAdConfigSerializer.deserialize(adConfigSerialized);
            }
            this.outstreamVideoAd = this.outstreamVideoAdFactory.createOutstreamVideoAdFromAdAsset(adAsset);
            this.isAdFresh = refreshDoubleClickAd();
            if (this.isAdFresh) {
                this.adVideoTracker.resetTracking(this.adId, this.outstreamVideoAd.getVideoImpressionTrackingUrls());
                if (TextUtils.isEmpty(this.outstreamVideoAd.getDisplayBannerImpressionTrackingUrl())) {
                    String valueOf = String.valueOf(this.outstreamVideoAd);
                    StringBuilder sb = new StringBuilder(valueOf.length() + 34);
                    sb.append("The ad has empty impression URLs: ");
                    sb.append(valueOf);
                    Log.e(TAG, sb.toString());
                } else if (!TextUtils.equals(this.lastRequestedImpressionUrl, this.outstreamVideoAd.getDisplayBannerImpressionTrackingUrl())) {
                    this.handler.post(this.doubleClickAdVisibilityCheckRunnable);
                }
            }
        } else {
            this.outstreamVideoAd = null;
            this.isAdFresh = false;
        }
        return this.outstreamVideoAd != null;
    }

    /* access modifiers changed from: package-private */
    public boolean bindDirectAdProgram(Program program) {
        AdConfig.AdAsset adAsset;
        this.programId = program.getId();
        this.programType = program.getType();
        this.adId = null;
        byte[] adConfigSerialized = program.getAdConfigSerialized();
        if (adConfigSerialized != null) {
            adAsset = this.directAdConfigSerializer.deserialize(adConfigSerialized);
        } else {
            adAsset = null;
        }
        if (adAsset == null) {
            this.outstreamVideoAd = null;
        } else if (!adAsset.hasDirectAdConfig()) {
            Log.e(TAG, "AdAsset for a DirectAd does not contain any DirectAd configuration.");
            this.outstreamVideoAd = null;
        } else {
            this.outstreamVideoAd = DirectVideoAd.fromAdAsset(adAsset);
        }
        return this.outstreamVideoAd != null;
    }

    /* access modifiers changed from: package-private */
    public void recordImpressionOrRescheduleCheck() {
        View candidateViewForImpression;
        if (this.view.getPreviewVideo().getVisibility() == 8) {
            candidateViewForImpression = this.view.getPreviewImage();
        } else {
            candidateViewForImpression = this.view.getPreviewVideo();
        }
        boolean shouldReschedule = true;
        if (!this.adsUtil.isViewVisible(candidateViewForImpression)) {
            this.visibleCheckStartTime = -1;
        } else if (this.visibleCheckStartTime == -1) {
            this.visibleCheckStartTime = this.clock.currentTimeMillis();
        } else if (this.outstreamVideoAd == null) {
            Log.e(TAG, "Outstream video ad is invalid but the runnable is still running");
            shouldReschedule = false;
        } else if (this.clock.currentTimeMillis() - this.visibleCheckStartTime >= 1000) {
            String newImpressionTrackingUrl = this.outstreamVideoAd.getDisplayBannerImpressionTrackingUrl();
            this.adsManager.recordImpression(this.lastRequestedImpressionUrl, newImpressionTrackingUrl);
            this.lastRequestedImpressionUrl = newImpressionTrackingUrl;
            shouldReschedule = false;
        }
        if (shouldReschedule) {
            this.handler.postDelayed(this.doubleClickAdVisibilityCheckRunnable, 1000);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean refreshDoubleClickAd() {
        long currentTime = this.clock.currentTimeMillis();
        OutstreamVideoAd outstreamVideoAd2 = this.outstreamVideoAd;
        if (outstreamVideoAd2 == null || !TextUtils.equals(this.adId, outstreamVideoAd2.getAdUnitId()) || this.outstreamVideoAd.getExpirationMillis() <= currentTime) {
            this.adsManager.processAdRequest(this.programId, this.adId);
            return false;
        }
        this.handler.removeCallbacks(this.doubleClickAdRefreshRunnable);
        this.handler.postDelayed(this.doubleClickAdRefreshRunnable, this.outstreamVideoAd.getExpirationMillis() - currentTime);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onStop() {
        cleanUpOldCallbacksAndTrackers();
    }

    private void cleanUpOldCallbacksAndTrackers() {
        stopRunnables();
        stopVideoTracking();
    }

    private void stopRunnables() {
        this.handler.removeCallbacks(this.doubleClickAdRefreshRunnable);
        this.handler.removeCallbacks(this.doubleClickAdVisibilityCheckRunnable);
    }

    /* access modifiers changed from: package-private */
    public String launchMediaIntent(String actionUrl) {
        int i = this.programType;
        if (i == 1001) {
            return launchMediaIntentForDirectAd(actionUrl);
        }
        if (i != 1002) {
            return null;
        }
        return launchMediaIntentForDoubleClickAd();
    }

    /* access modifiers changed from: package-private */
    public void recordFocusIfDoubleClickAd(boolean hasFocus) {
        OutstreamVideoAd outstreamVideoAd2 = this.outstreamVideoAd;
        if (outstreamVideoAd2 != null && hasFocus && this.isAdFresh) {
            String focusTrackingUrl = outstreamVideoAd2.getDisplayBannerFocusImpressionTrackingUrl();
            if (!TextUtils.isEmpty(focusTrackingUrl)) {
                this.adsManager.recordFocusImpression(focusTrackingUrl);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void recordClickIfDoubleClickAd(boolean isPlayingVideo) {
        String clickTrackingUrl;
        OutstreamVideoAd outstreamVideoAd2 = this.outstreamVideoAd;
        if (outstreamVideoAd2 != null) {
            if (isPlayingVideo) {
                clickTrackingUrl = outstreamVideoAd2.getVideoClickTrackingUrl();
            } else {
                clickTrackingUrl = outstreamVideoAd2.getDisplayBannerClickTrackingUrl();
            }
            if (!TextUtils.isEmpty(clickTrackingUrl)) {
                this.adsManager.recordClick(clickTrackingUrl);
            }
        }
    }

    private String launchMediaIntentForDoubleClickAd() {
        String packageName = null;
        OutstreamVideoAd outstreamVideoAd2 = this.outstreamVideoAd;
        if (outstreamVideoAd2 != null) {
            packageName = outstreamVideoAd2.getPackageName();
        }
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        return this.intentLauncher.launchMediaIntentForDoubleClickAd(packageName, this.outstreamVideoAd.getDeepLinkUrl(), this.outstreamVideoAd.getMarketUrl());
    }

    private String launchMediaIntentForDirectAd(String actionUrl) {
        String dataUrl;
        String packageName = null;
        OutstreamVideoAd outstreamVideoAd2 = this.outstreamVideoAd;
        if (outstreamVideoAd2 != null) {
            packageName = outstreamVideoAd2.getPackageName();
            dataUrl = this.outstreamVideoAd.getDeepLinkUrl();
        } else {
            dataUrl = actionUrl;
        }
        if (!TextUtils.isEmpty(packageName) || !TextUtils.isEmpty(dataUrl)) {
            return this.intentLauncher.launchMediaIntentForDirectAd(packageName, dataUrl);
        }
        Log.e(TAG, "Error launching direct ad program - both package name and URLs are empty");
        return null;
    }

    /* access modifiers changed from: package-private */
    public void onVideoStarted() {
        if (this.outstreamVideoAd == null) {
            throw new IllegalStateException("OutstreamVideoAd is null for a video ad that is about to start playing.");
        } else if (canStartTrackingVideo()) {
            this.videoProgressPoller.addVideoProgressUpdateListener(this.adVideoTracker);
            this.videoProgressPoller.startTracking(this.outstreamVideoAd.getVideoDurationMillis());
        }
    }

    public void onVideoEnded() {
        if (canStartTrackingVideo()) {
            this.videoProgressPoller.onVideoEnded();
        }
    }

    public void onVideoStopped() {
        if (canStartTrackingVideo()) {
            stopVideoTracking();
        }
    }

    public void onVideoError() {
        if (canStartTrackingVideo()) {
            stopVideoTracking();
        }
    }

    private void stopVideoTracking() {
        if (canStartTrackingVideo()) {
            this.videoProgressPoller.onVideoStopped();
            this.videoProgressPoller.removeVideoProgressUpdateListener(this.adVideoTracker);
        }
    }

    private boolean canStartTrackingVideo() {
        OutstreamVideoAd outstreamVideoAd2;
        return this.isAdFresh && (outstreamVideoAd2 = this.outstreamVideoAd) != null && outstreamVideoAd2.supportsVideoTracking();
    }

    /* access modifiers changed from: package-private */
    public OutstreamVideoAd getOutstreamVideoAd() {
        return this.outstreamVideoAd;
    }
}
