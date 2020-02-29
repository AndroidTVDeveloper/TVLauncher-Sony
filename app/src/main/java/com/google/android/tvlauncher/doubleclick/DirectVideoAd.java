package com.google.android.tvlauncher.doubleclick;

import android.util.Log;
import com.google.android.tvlauncher.doubleclick.OutstreamVideoAd;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;

public class DirectVideoAd extends OutstreamVideoAd {
    private static final String TAG = "DirectVideoAd";

    private DirectVideoAd(Builder builder) {
        super(builder);
    }

    public boolean supportsVideoTracking() {
        return false;
    }

    public static DirectVideoAd fromAdAsset(AdConfig.AdAsset adAsset) {
        if (!adAsset.hasDirectAdConfig()) {
            String valueOf = String.valueOf(adAsset);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 37);
            sb.append("fromAdAsset: a non-Direct ad passed: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
            return null;
        }
        AdConfig.DirectAdConfig directAdConfig = adAsset.getDirectAdConfig();
        return ((Builder) ((Builder) ((Builder) ((Builder) new Builder().setPackageName(directAdConfig.packageName)).setMarketUrl(directAdConfig.dataUrl)).setDeeplinkUrl(directAdConfig.dataUrl)).setAdAsset(adAsset)).build();
    }

    static final class Builder extends OutstreamVideoAd.Builder<Builder> {
        Builder() {
        }

        public DirectVideoAd build() {
            return new DirectVideoAd(this);
        }
    }
}
