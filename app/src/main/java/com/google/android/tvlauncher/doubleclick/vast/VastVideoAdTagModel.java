package com.google.android.tvlauncher.doubleclick.vast;

import android.util.Size;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class VastVideoAdTagModel {
    private final String adUnitId;
    private final Set<Size> adUnitSizes;
    private final AdvertisingIdClient.Info advertisingIdInfo;
    private final Set<Size> companionAdSizes;
    private final String correlator;
    private final String descriptionUrl;
    private final Map<String, String> targeting;
    private final String url;

    private VastVideoAdTagModel(Builder builder) {
        this.adUnitId = builder.adUnitId;
        this.url = builder.url;
        this.descriptionUrl = builder.descriptionUrl;
        this.adUnitSizes = builder.adUnitSizes;
        this.companionAdSizes = builder.companionAdSizes;
        this.correlator = builder.correlator;
        this.targeting = builder.targeting;
        this.advertisingIdInfo = builder.advertisingIdInfo;
    }

    /* access modifiers changed from: package-private */
    public String getAdUnitId() {
        return this.adUnitId;
    }

    /* access modifiers changed from: package-private */
    public String getUrl() {
        return this.url;
    }

    /* access modifiers changed from: package-private */
    public String getDescriptionUrl() {
        return this.descriptionUrl;
    }

    /* access modifiers changed from: package-private */
    public Set<Size> getAdUnitSizes() {
        return this.adUnitSizes;
    }

    /* access modifiers changed from: package-private */
    public Set<Size> getCompanionAdSizes() {
        return this.companionAdSizes;
    }

    /* access modifiers changed from: package-private */
    public String getCorrelator() {
        return this.correlator;
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getTargeting() {
        return this.targeting;
    }

    @Nullable
    public AdvertisingIdClient.Info getAdvertisingIdInfo() {
        return this.advertisingIdInfo;
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public String adUnitId;
        /* access modifiers changed from: private */
        public Set<Size> adUnitSizes = new HashSet();
        /* access modifiers changed from: private */
        public AdvertisingIdClient.Info advertisingIdInfo;
        /* access modifiers changed from: private */
        public Set<Size> companionAdSizes = new HashSet();
        /* access modifiers changed from: private */
        public String correlator;
        /* access modifiers changed from: private */
        public String descriptionUrl;
        /* access modifiers changed from: private */
        public Map<String, String> targeting;
        /* access modifiers changed from: private */
        public String url;

        public Builder setAdUnitId(String adUnitId2) {
            this.adUnitId = adUnitId2;
            return this;
        }

        public Builder setUrl(String url2) {
            this.url = url2;
            return this;
        }

        public Builder setDescriptionUrl(String descriptionUrl2) {
            this.descriptionUrl = descriptionUrl2;
            return this;
        }

        public Builder setAdUnitSizes(Set<Size> adUnitSizes2) {
            this.adUnitSizes = adUnitSizes2;
            return this;
        }

        public Builder setCompanionAdSizes(Set<Size> companionAdSizes2) {
            this.companionAdSizes = companionAdSizes2;
            return this;
        }

        public Builder setCorrelator(String correlator2) {
            this.correlator = correlator2;
            return this;
        }

        public Builder setTargeting(Map<String, String> targeting2) {
            this.targeting = targeting2;
            return this;
        }

        public Builder setAdvertisingIdInfo(AdvertisingIdClient.Info advertisingIdInfo2) {
            this.advertisingIdInfo = advertisingIdInfo2;
            return this;
        }

        public VastVideoAdTagModel build() {
            return new VastVideoAdTagModel(this);
        }
    }
}
