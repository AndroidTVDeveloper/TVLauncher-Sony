package com.google.android.tvlauncher.doubleclick.vast;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.tvlauncher.application.TvLauncherApplication;
import com.google.android.tvlauncher.doubleclick.AdVideoTracker;
import com.google.android.tvlauncher.doubleclick.AdsManager;
import com.google.android.tvlauncher.doubleclick.TrackingUrl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class VastVideoTracker extends AdVideoTracker {
    private AdsManager adsManager;
    private boolean canSafelyRemovePreviouslyRecordedVideoImpression;
    private String currentAdId;
    private Set<String> currentlyRecordedVideoImpressions;
    private long lastTimeMillis;
    private List<TrackingUrl> vastTrackingUrls;

    public VastVideoTracker(Context context) {
        this(((TvLauncherApplication) context.getApplicationContext()).getAdsManager());
    }

    VastVideoTracker(AdsManager adsManager2) {
        this.currentlyRecordedVideoImpressions = new HashSet();
        this.lastTimeMillis = -1;
        this.adsManager = adsManager2;
    }

    public void resetTracking(String adId, @Nonnull List<TrackingUrl> vastTrackingUrls2) {
        this.canSafelyRemovePreviouslyRecordedVideoImpression = TextUtils.equals(this.currentAdId, adId) && !vastTrackingUrls2.equals(this.vastTrackingUrls);
        if (this.canSafelyRemovePreviouslyRecordedVideoImpression) {
            this.lastTimeMillis = -1;
        }
        this.vastTrackingUrls = vastTrackingUrls2;
        this.currentAdId = adId;
    }

    public void onVideoProgressUpdate(long currentProgressMillis) {
        List<String> intersectingCues = findCrossedCuesNotPreviouslyImpressioned(this.lastTimeMillis, currentProgressMillis);
        if (!intersectingCues.isEmpty()) {
            if (this.canSafelyRemovePreviouslyRecordedVideoImpression && !this.currentlyRecordedVideoImpressions.isEmpty()) {
                this.adsManager.removeRequestedTrackingUrls(this.currentlyRecordedVideoImpressions);
                this.canSafelyRemovePreviouslyRecordedVideoImpression = false;
                this.currentlyRecordedVideoImpressions.clear();
            }
            this.adsManager.recordImpressionsInBatch(intersectingCues);
            this.lastTimeMillis = currentProgressMillis;
            this.currentlyRecordedVideoImpressions.addAll(intersectingCues);
        }
    }

    private List<String> findCrossedCuesNotPreviouslyImpressioned(long lastTimeMillis2, long currentTimeMillis) {
        List<String> intersectingCues = new ArrayList<>();
        for (TrackingUrl trackingUrl : this.vastTrackingUrls) {
            if (hasCuePointCrossed(trackingUrl, lastTimeMillis2, currentTimeMillis)) {
                intersectingCues.add(trackingUrl.getUrl());
            }
        }
        return intersectingCues;
    }

    private boolean hasCuePointCrossed(TrackingUrl trackingUrl, long lastTimeMillis2, long currentTimeMillis) {
        long cueTimeMillis = trackingUrl.getOffsetMillis();
        return cueTimeMillis > lastTimeMillis2 && cueTimeMillis <= currentTimeMillis;
    }
}
