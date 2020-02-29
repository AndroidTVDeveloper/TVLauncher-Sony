package com.google.android.tvlauncher.doubleclick;

import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import java.util.List;

public abstract class OutstreamVideoAd {
    private final AdConfig.AdAsset adAsset;
    private final String deeplinkUrl;
    private final String displayBannerClickTrackingUrl;
    private final String displayBannerFocusImpressionTrackingUrl;
    private final String displayBannerImpressionTrackingUrl;
    private final String imageUri;
    private final String marketUrl;
    private final String packageName;
    private final String videoClickTrackingUrl;
    private final long videoDurationMillis;
    private final List<TrackingUrl> videoImpressionTrackingUrls;
    private final String videoUri;

    public OutstreamVideoAd(Builder<?> builder) {
        this.imageUri = builder.imageUri;
        this.videoUri = builder.videoUri;
        this.displayBannerImpressionTrackingUrl = builder.displayBannerImpressionTrackingUrl;
        this.videoImpressionTrackingUrls = builder.videoImpressionTrackingUrls;
        this.displayBannerFocusImpressionTrackingUrl = builder.displayBannerFocusImpressionTrackingUrl;
        this.displayBannerClickTrackingUrl = builder.displayBannerClickTrackingUrl;
        this.videoClickTrackingUrl = builder.videoClickTrackingUrl;
        this.packageName = builder.packageName;
        this.marketUrl = builder.marketUrl;
        this.deeplinkUrl = builder.deeplinkUrl;
        this.videoDurationMillis = builder.videoDurationMillis;
        this.adAsset = builder.adAsset;
    }

    public String getImageUri() {
        return this.imageUri;
    }

    public String getVideoUri() {
        return this.videoUri;
    }

    public String getDisplayBannerImpressionTrackingUrl() {
        return this.displayBannerImpressionTrackingUrl;
    }

    public String getDisplayBannerFocusImpressionTrackingUrl() {
        return this.displayBannerFocusImpressionTrackingUrl;
    }

    public List<TrackingUrl> getVideoImpressionTrackingUrls() {
        return this.videoImpressionTrackingUrls;
    }

    public String getDisplayBannerClickTrackingUrl() {
        return this.displayBannerClickTrackingUrl;
    }

    public String getVideoClickTrackingUrl() {
        return this.videoClickTrackingUrl;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getMarketUrl() {
        return this.marketUrl;
    }

    public String getDeepLinkUrl() {
        return this.deeplinkUrl;
    }

    public long getVideoDurationMillis() {
        return this.videoDurationMillis;
    }

    public AdConfig.AdAsset getAdAsset() {
        return this.adAsset;
    }

    public String getAdUnitId() {
        AdConfig.AdAsset adAsset2 = this.adAsset;
        if (adAsset2 == null || !adAsset2.hasDoubleclickAdConfig()) {
            return null;
        }
        return this.adAsset.getDoubleclickAdConfig().adUnitId;
    }

    public long getExpirationMillis() {
        AdConfig.AdAsset adAsset2 = this.adAsset;
        if (adAsset2 == null) {
            return -1;
        }
        return adAsset2.expiration;
    }

    public boolean supportsVideoTracking() {
        return false;
    }

    public String toString() {
        return String.format("OutstreamVideoAd: [imageUri=%s, videoUri=%s, displayBannerImpressionTrackingUrl=%s, videoImpressionTrackingUrls=%s, displayBannerFocusImpressionTrackingUrl=%s, displayBannerClickTrackingUrl=%s, videoClickTrackingUrl=%s, packageName=%s, marketUrl=%s, deepLinkUrl=%s, adAsset=%s]", getImageUri(), getVideoUri(), getDisplayBannerImpressionTrackingUrl(), getVideoImpressionTrackingUrls(), getDisplayBannerFocusImpressionTrackingUrl(), getDisplayBannerClickTrackingUrl(), getVideoClickTrackingUrl(), getPackageName(), getMarketUrl(), getDeepLinkUrl(), getAdAsset());
    }

    public static abstract class Builder<T extends Builder<T>> {
        /* access modifiers changed from: private */
        public AdConfig.AdAsset adAsset;
        /* access modifiers changed from: private */
        public String deeplinkUrl;
        /* access modifiers changed from: private */
        public String displayBannerClickTrackingUrl;
        /* access modifiers changed from: private */
        public String displayBannerFocusImpressionTrackingUrl;
        /* access modifiers changed from: private */
        public String displayBannerImpressionTrackingUrl;
        /* access modifiers changed from: private */
        public String imageUri;
        /* access modifiers changed from: private */
        public String marketUrl;
        /* access modifiers changed from: private */
        public String packageName;
        /* access modifiers changed from: private */
        public String videoClickTrackingUrl;
        /* access modifiers changed from: private */
        public long videoDurationMillis;
        /* access modifiers changed from: private */
        public List<TrackingUrl> videoImpressionTrackingUrls;
        /* access modifiers changed from: private */
        public String videoUri;

        private T self() {
            return this;
        }

        public T setImageUri(String imageUri2) {
            this.imageUri = imageUri2;
            return self();
        }

        public T setVideoUri(String videoUri2) {
            this.videoUri = videoUri2;
            return self();
        }

        public T setDisplayBannerImpressionTrackingUrl(String displayBannerImpressionTrackingUrl2) {
            this.displayBannerImpressionTrackingUrl = displayBannerImpressionTrackingUrl2;
            return self();
        }

        public T setVideoImpressionTrackingUrls(List<TrackingUrl> videoImpressionTrackingUrls2) {
            this.videoImpressionTrackingUrls = videoImpressionTrackingUrls2;
            return self();
        }

        public T setDisplayBannerFocusImpressionTrackingUrl(String displayBannerFocusImpressionTrackingUrl2) {
            this.displayBannerFocusImpressionTrackingUrl = displayBannerFocusImpressionTrackingUrl2;
            return self();
        }

        public T setDisplayBannerClickTrackingUrl(String displayBannerClickTrackingUrl2) {
            this.displayBannerClickTrackingUrl = displayBannerClickTrackingUrl2;
            return self();
        }

        public T setVideoClickTrackingUrl(String videoClickTrackingUrl2) {
            this.videoClickTrackingUrl = videoClickTrackingUrl2;
            return self();
        }

        public T setPackageName(String packageName2) {
            this.packageName = packageName2;
            return self();
        }

        public T setMarketUrl(String marketUrl2) {
            this.marketUrl = marketUrl2;
            return self();
        }

        public T setDeeplinkUrl(String deeplinkUrl2) {
            this.deeplinkUrl = deeplinkUrl2;
            return self();
        }

        public T setVideoDurationMillis(long videoDurationMillis2) {
            this.videoDurationMillis = videoDurationMillis2;
            return self();
        }

        public T setAdAsset(AdConfig.AdAsset adAsset2) {
            this.adAsset = adAsset2;
            return self();
        }
    }
}
