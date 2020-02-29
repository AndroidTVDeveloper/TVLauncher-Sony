package com.google.android.tvlauncher.doubleclick.vast;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.tvlauncher.doubleclick.TrackingUrl;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import com.google.android.tvlauncher.doubleclick.proto.nano.VideoCreative;
import com.google.android.tvlauncher.doubleclick.vast.VastVideoAd;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class VastVideoAdFactory {
    private static final String ADURL_URL_KEY = "adurl";
    private static final double ASPECT_RATIO_EPSILON = 0.01d;
    private static final boolean DEBUG = true;
    private static final String DEEP_LINK_URL_AD_PARAMETERS_KEY = "deeplinkURL";
    private static final double DESIRED_VIDEO_ASPECT_RATIO = 1.7777777777777777d;
    private static final String EXPAND_EVENT_NAME = "expand";
    private static final String FULLSCREEN_EVENT_NAME = "fullscreen";
    private static final String LANDING_PAGE_URL_FOR_INVALID_DEEPLINK = "http://www.google.com/placeholder";
    private static final String MARKET_URL_AD_PARAMETERS_KEY = "marketURL";
    private static final String MIME_MP4 = "video/mp4";
    private static final String PACKAGE_NAME_AD_PARAMETERS_KEY = "packageName";
    private static final String TAG = "VastVideoAdFactory";
    private static final TrackingUrlsComparator TRACKING_URLS_COMPARATOR = new TrackingUrlsComparator();
    private static final int VIDEO_VIEWABLE_IMPRESSION_DELAY_MS = 2000;

    public VastVideoAd createVastVideoAdFromAdAsset(AdConfig.AdAsset adAsset) {
        if (!adAsset.hasDoubleclickAdConfig() || !adAsset.getDoubleclickAdConfig().hasVast()) {
            String valueOf = String.valueOf(adAsset);
            StringBuilder sb = new StringBuilder(valueOf.length() + 41);
            sb.append("createVastVideoAd: a non-vast ad passed: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
            return null;
        }
        VideoCreative.VastXml vastXml = adAsset.getDoubleclickAdConfig().getVast();
        Map<String, String> customParamsMap = getCustomParamsMap(vastXml);
        String videoDestinationUrl = getVideoDestinationUrl(vastXml);
        return ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) ((VastVideoAd.Builder) new VastVideoAd.Builder().setAdAsset(adAsset)).setImageUri(extractImageUrl(vastXml.companion))).setVideoUri(extractPreferredVideo(vastXml.media))).setDisplayBannerImpressionTrackingUrl(extractBannerImpressionTrackingUrl(vastXml))).setVideoImpressionTrackingUrls(buildVideoImpressionTrackingUrls(vastXml))).setDisplayBannerFocusImpressionTrackingUrl(getDisplayBannerFocusTrackingUrl(vastXml))).setDisplayBannerClickTrackingUrl(getDisplayBannerDestinationUrl(vastXml))).setVideoClickTrackingUrl(videoDestinationUrl)).setPackageName(extractPackageName(customParamsMap))).setMarketUrl(extractMarketUrl(customParamsMap))).setDeeplinkUrl(extractDeeplinkUrl(customParamsMap, videoDestinationUrl))).setVideoDurationMillis((long) vastXml.duration)).build();
    }

    public VastVideoAd createVastVideoAdFromAdResponse(String adUnitId, InputStream inputStream) {
        AdConfig.AdAsset adAsset = new VastParser().parse(adUnitId, inputStream);
        if (adAsset == null) {
            return null;
        }
        return createVastVideoAdFromAdAsset(adAsset);
    }

    private static boolean isDisplayOnly(VideoCreative.VastXml vastXml) {
        return vastXml.duration == 0;
    }

    private static String extractBannerImpressionTrackingUrl(VideoCreative.VastXml vastXml) {
        if (isDisplayOnly(vastXml)) {
            if (vastXml.impression.length > 0 && !TextUtils.isEmpty(vastXml.impression[0].url)) {
                return vastXml.impression[0].url;
            }
            Log.e(TAG, "No banner impression tracking URL could be extracted from impression tag");
            return "";
        } else if (vastXml.companion.length > 0 && vastXml.companion[0].eventTracking.length > 0 && !TextUtils.isEmpty(vastXml.companion[0].eventTracking[0].eventUrl)) {
            return vastXml.companion[0].eventTracking[0].eventUrl;
        } else {
            Log.e(TAG, "No banner impression tracking URL could be extracted from companion");
            return "";
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static List<TrackingUrl> buildVideoImpressionTrackingUrls(VideoCreative.VastXml vastXml) {
        char c;
        VideoCreative.VastXml vastXml2 = vastXml;
        List<TrackingUrl> eventTrackingUrls = new ArrayList<>(vastXml2.eventTracking.length);
        if (vastXml2.impression.length > 0 && !TextUtils.isEmpty(vastXml2.impression[0].url)) {
            eventTrackingUrls.add(new TrackingUrl(vastXml2.impression[0].url, 0));
        }
        if (vastXml2.videoViewableImpression != null && !TextUtils.isEmpty(vastXml2.videoViewableImpression.eventUrl)) {
            eventTrackingUrls.add(new TrackingUrl(vastXml2.videoViewableImpression.eventUrl, (long) Math.min(vastXml2.duration, 2000)));
        }
        long durationMillis = (long) vastXml2.duration;
        for (VideoCreative.VastTracking eventTracking : vastXml2.eventTracking) {
            long offsetMillis = 0;
            String str = eventTracking.eventName;
            switch (str.hashCode()) {
                case -1638835128:
                    if (str.equals("midpoint")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1337830390:
                    if (str.equals("thirdQuartile")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -599445191:
                    if (str.equals("complete")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 109757538:
                    if (str.equals(TtmlNode.START)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 560220243:
                    if (str.equals("firstQuartile")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1778167540:
                    if (str.equals("creativeView")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (!(c == 0 || c == 1)) {
                if (c == 2) {
                    double d = (double) durationMillis;
                    Double.isNaN(d);
                    offsetMillis = (long) (d * 0.25d);
                } else if (c == 3) {
                    double d2 = (double) durationMillis;
                    Double.isNaN(d2);
                    offsetMillis = (long) (d2 * 0.5d);
                } else if (c == 4) {
                    double d3 = (double) durationMillis;
                    Double.isNaN(d3);
                    offsetMillis = (long) (d3 * 0.75d);
                } else if (c != 5) {
                } else {
                    offsetMillis = durationMillis;
                }
            }
            eventTrackingUrls.add(new TrackingUrl(eventTracking.eventUrl, offsetMillis));
        }
        eventTrackingUrls.sort(TRACKING_URLS_COMPARATOR);
        return eventTrackingUrls;
    }

    private static String extractImageUrl(@Nonnull VideoCreative.VastCompanion[] vastCompanion) {
        if (vastCompanion.length == 0) {
            Log.e(TAG, "No Image URL could be extracted: Empty vast companion array.");
            return "";
        }
        if (TextUtils.isEmpty(vastCompanion[0].staticResource)) {
            Log.e(TAG, "Empty Image URL found in the vast companion.");
        }
        return vastCompanion[0].staticResource;
    }

    private static String extractPreferredVideo(@Nonnull VideoCreative.VastMedia[] vastMedias) {
        VideoCreative.VastMedia[] vastMediaArr = vastMedias;
        double preferredVideoAspectRatio = -1.0d;
        VideoCreative.VastMedia preferredVastMedia = null;
        for (VideoCreative.VastMedia vastMedia : vastMediaArr) {
            if ("video/mp4".equals(vastMedia.type)) {
                if (preferredVastMedia == null) {
                    preferredVastMedia = vastMedia;
                    double d = (double) vastMediaArr[0].width;
                    double d2 = (double) vastMediaArr[0].height;
                    Double.isNaN(d);
                    Double.isNaN(d2);
                    preferredVideoAspectRatio = d / d2;
                } else {
                    double d3 = (double) vastMedia.width;
                    double d4 = (double) vastMedia.height;
                    Double.isNaN(d3);
                    Double.isNaN(d4);
                    double videoAspectRatio = d3 / d4;
                    if (Math.abs(videoAspectRatio - 1.7777777777777777d) <= ASPECT_RATIO_EPSILON) {
                        if (vastMedia.bitrate > preferredVastMedia.bitrate) {
                            preferredVastMedia = vastMedia;
                        }
                    } else if (Math.abs(videoAspectRatio - 1.7777777777777777d) < Math.abs(preferredVideoAspectRatio - 1.7777777777777777d)) {
                        preferredVastMedia = vastMedia;
                    }
                }
            }
        }
        if (preferredVastMedia != null) {
            return preferredVastMedia.url;
        }
        String valueOf = Arrays.toString(vastMedias);
        Log.e(TAG, valueOf.length() != 0 ? "No MP4 video found in VAST response medias: ".concat(valueOf) : "No MP4 video found in VAST response medias: ");
        return "";
    }

    private static String extractDeeplinkUrl(Map<String, String> customParamsMap, String destinationUrl) {
        String deeplinkUrl = "";
        if (!TextUtils.isEmpty(destinationUrl)) {
            deeplinkUrl = Uri.parse(destinationUrl).getQueryParameter(ADURL_URL_KEY);
            if (TextUtils.equals(deeplinkUrl, LANDING_PAGE_URL_FOR_INVALID_DEEPLINK)) {
                deeplinkUrl = "";
            }
        }
        if (TextUtils.isEmpty(deeplinkUrl) && customParamsMap != null && customParamsMap.containsKey(DEEP_LINK_URL_AD_PARAMETERS_KEY)) {
            deeplinkUrl = customParamsMap.get(DEEP_LINK_URL_AD_PARAMETERS_KEY);
        }
        if (TextUtils.isEmpty(deeplinkUrl)) {
            String valueOf = String.valueOf(destinationUrl);
            Log.d(TAG, valueOf.length() != 0 ? "No deeplinkUrl found in destinationUrl: ".concat(valueOf) : "No deeplinkUrl found in destinationUrl: ");
        }
        return deeplinkUrl;
    }

    private static String extractPackageName(Map<String, String> customParamsMap) {
        if (customParamsMap != null && customParamsMap.containsKey(PACKAGE_NAME_AD_PARAMETERS_KEY)) {
            return customParamsMap.get(PACKAGE_NAME_AD_PARAMETERS_KEY);
        }
        String valueOf = String.valueOf(customParamsMap);
        StringBuilder sb = new StringBuilder(valueOf.length() + 40);
        sb.append("no package name found in ad parameters: ");
        sb.append(valueOf);
        Log.e(TAG, sb.toString());
        return null;
    }

    private static String extractMarketUrl(Map<String, String> customParamsMap) {
        if (customParamsMap != null && customParamsMap.containsKey(MARKET_URL_AD_PARAMETERS_KEY)) {
            return customParamsMap.get(MARKET_URL_AD_PARAMETERS_KEY);
        }
        String valueOf = String.valueOf(customParamsMap);
        StringBuilder sb = new StringBuilder(valueOf.length() + 38);
        sb.append("no market URL found in ad parameters: ");
        sb.append(valueOf);
        Log.e(TAG, sb.toString());
        return null;
    }

    private static Map<String, String> getCustomParamsMap(VideoCreative.VastXml vastXml) {
        String customParams = vastXml.customParameters;
        if (TextUtils.isEmpty(customParams) && vastXml.nonLinearAsset.length > 0) {
            customParams = vastXml.nonLinearAsset[0].customParameters;
        }
        if (TextUtils.isEmpty(customParams)) {
            return null;
        }
        Map<String, String> customParamsMap = new HashMap<>();
        for (String keyValue : customParams.split(",")) {
            String[] pair = keyValue.split("=");
            if (pair.length != 2) {
                String valueOf = customParams;
                Log.e(TAG, valueOf.length() != 0 ? "Syntax error in ad parameters, must be comma-separated pairs key=value: ".concat(valueOf) : "Syntax error in ad parameters, must be comma-separated pairs key=value: ");
                return null;
            }
            customParamsMap.put(pair[0], pair[1]);
        }
        return customParamsMap;
    }

    private static String getDisplayBannerDestinationUrl(VideoCreative.VastXml vastXml) {
        if (isDisplayOnly(vastXml)) {
            if (vastXml.nonLinearAsset.length > 0 && !TextUtils.isEmpty(vastXml.nonLinearAsset[0].destinationUrl)) {
                return vastXml.nonLinearAsset[0].destinationUrl;
            }
            Log.e(TAG, "No non-linear destination URL found");
            return "";
        } else if (vastXml.companion.length > 0 && !TextUtils.isEmpty(vastXml.companion[0].destinationUrl)) {
            return vastXml.companion[0].destinationUrl;
        } else {
            Log.e(TAG, "No companion destination URL found");
            return "";
        }
    }

    private static String getVideoDestinationUrl(VideoCreative.VastXml vastXml) {
        String destinationUrl = vastXml.destinationUrl;
        if (!TextUtils.isEmpty(destinationUrl) || vastXml.nonLinearAsset.length <= 0) {
            return destinationUrl;
        }
        return vastXml.nonLinearAsset[0].destinationUrl;
    }

    private static String getDisplayBannerFocusTrackingUrl(VideoCreative.VastXml vastXml) {
        int i = 0;
        if (isDisplayOnly(vastXml)) {
            VideoCreative.VastTracking[] nonLinearEventTrackings = vastXml.nonLinearEventTracking;
            int length = nonLinearEventTrackings.length;
            while (i < length) {
                VideoCreative.VastTracking vastTracking = nonLinearEventTrackings[i];
                if (EXPAND_EVENT_NAME.equals(vastTracking.eventName)) {
                    return vastTracking.eventUrl;
                }
                i++;
            }
        } else {
            VideoCreative.VastTracking[] linearEventTrackings = vastXml.eventTracking;
            int length2 = linearEventTrackings.length;
            while (i < length2) {
                VideoCreative.VastTracking eventTracking = linearEventTrackings[i];
                if (FULLSCREEN_EVENT_NAME.equals(eventTracking.eventName)) {
                    return eventTracking.eventUrl;
                }
                i++;
            }
        }
        return "";
    }

    private static class TrackingUrlsComparator implements Comparator<TrackingUrl> {
        private TrackingUrlsComparator() {
        }

        public int compare(TrackingUrl lhs, TrackingUrl rhs) {
            return (lhs.getOffsetMillis() > rhs.getOffsetMillis() ? 1 : (lhs.getOffsetMillis() == rhs.getOffsetMillis() ? 0 : -1));
        }
    }
}
