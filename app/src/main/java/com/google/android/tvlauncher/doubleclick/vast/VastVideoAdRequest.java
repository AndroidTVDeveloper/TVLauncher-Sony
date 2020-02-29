package com.google.android.tvlauncher.doubleclick.vast;

import android.net.Uri;
import android.util.Size;
import com.google.android.exoplayer2.metadata.icy.IcyHeaders;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.tvlauncher.doubleclick.DoubleClickAdRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VastVideoAdRequest implements DoubleClickAdRequest {
    private static final String DFP_DOMAIN = "pubads.g.doubleclick.net";
    private static final String DFP_PATH = "/gampad/ads";
    private static final String SCHEME_HTTPS = "https";
    private final Uri dfpRequestUri;

    public VastVideoAdRequest(VastVideoAdTagModel vastVideoAdTagModel) {
        this.dfpRequestUri = buildAdRequest(vastVideoAdTagModel);
    }

    public Uri getDfpRequestUri() {
        return this.dfpRequestUri;
    }

    private static Uri buildAdRequest(VastVideoAdTagModel vastVideoAdTagModel) {
        Uri.Builder adRequestUriBuilder = new Uri.Builder();
        Uri.Builder appendQueryParameter = adRequestUriBuilder.scheme(SCHEME_HTTPS).authority(DFP_DOMAIN).path(DFP_PATH).appendQueryParameter("sz", buildSizes(vastVideoAdTagModel.getAdUnitSizes())).appendQueryParameter("ciu_szs", buildCompanionSizes(vastVideoAdTagModel.getCompanionAdSizes())).appendQueryParameter("iu", vastVideoAdTagModel.getAdUnitId()).appendQueryParameter("impl", "s");
        String str = IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE;
        appendQueryParameter.appendQueryParameter("gdfp_req", str).appendQueryParameter("env", "vp").appendQueryParameter("output", "vast").appendQueryParameter("unviewed_position_start", str).appendQueryParameter("url", vastVideoAdTagModel.getUrl()).appendQueryParameter("description_url", vastVideoAdTagModel.getDescriptionUrl()).appendQueryParameter("correlator", vastVideoAdTagModel.getCorrelator()).appendQueryParameter("cust_params", buildTargetingParameters(vastVideoAdTagModel.getTargeting()));
        AdvertisingIdClient.Info advertisingIdInfo = vastVideoAdTagModel.getAdvertisingIdInfo();
        if (advertisingIdInfo != null) {
            Uri.Builder appendQueryParameter2 = adRequestUriBuilder.appendQueryParameter("idtype", "adid").appendQueryParameter("rdid", advertisingIdInfo.getId());
            if (!advertisingIdInfo.isLimitAdTrackingEnabled()) {
                str = "0";
            }
            appendQueryParameter2.appendQueryParameter("is_lat", str);
        }
        return adRequestUriBuilder.build();
    }

    private static String buildSizes(Set<Size> adUnitSizes) {
        String sizes = joinSizes(adUnitSizes, '|');
        if (sizes != null) {
            return sizes;
        }
        throw new IllegalArgumentException("Ad Unit sizes cannot be empty");
    }

    private static String buildCompanionSizes(Set<Size> companionSizes) {
        String sizes = joinSizes(companionSizes, ',');
        if (sizes != null) {
            return sizes;
        }
        throw new IllegalArgumentException("Ad companion sizes cannot be empty");
    }

    private static String buildTargetingParameters(Map<String, String> targeting) {
        StringBuilder targetingBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = targeting.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, String> nextEntry = iterator.next();
            targetingBuilder.append((String) nextEntry.getKey());
            targetingBuilder.append("=");
            targetingBuilder.append((String) nextEntry.getValue());
            while (iterator.hasNext()) {
                Map.Entry<String, String> nextEntry2 = iterator.next();
                targetingBuilder.append("&");
                targetingBuilder.append((String) nextEntry2.getKey());
                targetingBuilder.append("=");
                targetingBuilder.append((String) nextEntry2.getValue());
            }
        }
        return targetingBuilder.toString();
    }

    private static String joinSizes(Set<Size> sizes, char delimtier) {
        StringBuilder sizesBuilder = new StringBuilder();
        Iterator<Size> iterator = sizes.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        Size nextSize = iterator.next();
        sizesBuilder.append(nextSize.getWidth());
        sizesBuilder.append('x');
        sizesBuilder.append(nextSize.getHeight());
        while (iterator.hasNext()) {
            Size nextSize2 = iterator.next();
            sizesBuilder.append(delimtier);
            sizesBuilder.append(nextSize2.getWidth());
            sizesBuilder.append('x');
            sizesBuilder.append(nextSize2.getHeight());
        }
        return sizesBuilder.toString();
    }
}
