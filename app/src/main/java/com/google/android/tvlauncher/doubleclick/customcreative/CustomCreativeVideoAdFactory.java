package com.google.android.tvlauncher.doubleclick.customcreative;

import android.util.Log;
import com.google.android.tvlauncher.doubleclick.customcreative.CustomCreativeVideoAd;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import java.io.InputStream;

public class CustomCreativeVideoAdFactory {
    private static final String TAG = "CustomCreativeVideoAdFactory";
    private final CustomCreativeAdParser customCreativeAdParser;

    public CustomCreativeVideoAdFactory() {
        this(new CustomCreativeAdParser());
    }

    CustomCreativeVideoAdFactory(CustomCreativeAdParser customCreativeAdParser2) {
        this.customCreativeAdParser = customCreativeAdParser2;
    }

    public CustomCreativeVideoAd createCustomCreativeVideoAdFromAdAsset(AdConfig.AdAsset adAsset) {
        if (!adAsset.hasDoubleclickAdConfig() || !adAsset.getDoubleclickAdConfig().hasCustomCreative()) {
            String valueOf = String.valueOf(adAsset);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 52);
            sb.append("createVastVideoAd: a non-custom creative ad passed: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
            return null;
        }
        AdConfig.CustomCreative customCreative = adAsset.getDoubleclickAdConfig().getCustomCreative();
        if (!customCreative.hasAppInstallCreativeInfo()) {
            String valueOf2 = String.valueOf(adAsset);
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 48);
            sb2.append("createVastVideoAd: a non-app install ad passed: ");
            sb2.append(valueOf2);
            Log.e(TAG, sb2.toString());
            return null;
        }
        AdConfig.AppInstallCreativeInfo appInstallCreativeInfo = customCreative.getAppInstallCreativeInfo();
        return ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) ((CustomCreativeVideoAd.Builder) new CustomCreativeVideoAd.Builder().setAdAsset(adAsset)).setImageUri(customCreative.imageUri)).setVideoUri(customCreative.videoUri)).setDisplayBannerImpressionTrackingUrl(customCreative.displayBannerImpressionTrackingUrl)).setVideoImpressionTrackingUrls(null)).setDisplayBannerClickTrackingUrl(customCreative.clickTrackingUrl)).setPackageName(appInstallCreativeInfo.packageName)).setMarketUrl(appInstallCreativeInfo.marketUrl)).setDeeplinkUrl(appInstallCreativeInfo.deeplinkUrl)).build();
    }

    public CustomCreativeVideoAd createCustomCreativeVideoAdFromAdResponse(String adUnitId, InputStream inputStream) {
        AdConfig.AdAsset adAsset = this.customCreativeAdParser.parse(adUnitId, inputStream);
        if (adAsset == null) {
            return null;
        }
        return createCustomCreativeVideoAdFromAdAsset(adAsset);
    }
}
