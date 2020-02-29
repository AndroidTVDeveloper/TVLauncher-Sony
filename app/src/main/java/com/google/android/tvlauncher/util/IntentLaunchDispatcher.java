package com.google.android.tvlauncher.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.p001v4.content.IntentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.exoplayer2.C0847C;
import com.google.android.tvlauncher.C1167R;
import java.net.URISyntaxException;
import java.util.List;

public class IntentLaunchDispatcher {
    private static final boolean DEBUG = false;
    private static final String TAG = "IntentLaunchDispatcher";
    private Context context;
    private Intent intent;
    private LeanbackCategoryIntentLauncher leanbackCategoryIntentLauncher;
    private PlayStoreIntentLauncher playStoreIntentLauncher;
    private TvRecommendationsIntentLauncher tvRecommendationsIntentLauncher;
    private View viewToStartAnimation;

    public IntentLaunchDispatcher(Context context2) {
        this(context2, new TvRecommendationsIntentLauncher(), new LeanbackCategoryIntentLauncher(), new PlayStoreIntentLauncher());
    }

    IntentLaunchDispatcher(Context context2, TvRecommendationsIntentLauncher tvRecommendationsFallbackIntentHandler, LeanbackCategoryIntentLauncher leanbackLauncherFallbackIntentHandler, PlayStoreIntentLauncher playStoreFallbackIntentHandler) {
        this.context = context2;
        this.tvRecommendationsIntentLauncher = tvRecommendationsFallbackIntentHandler;
        this.leanbackCategoryIntentLauncher = leanbackLauncherFallbackIntentHandler;
        this.playStoreIntentLauncher = playStoreFallbackIntentHandler;
    }

    public String launchMediaIntentForDoubleClickAd(String packageName, String deeplinkUrl, String marketUrl) {
        if (PackageUtils.isPackageInstalled(this.context, packageName)) {
            if (TextUtils.isEmpty(deeplinkUrl)) {
                this.leanbackCategoryIntentLauncher.launchIntent(this.context, packageName, null);
                return null;
            } else if (launchIntentFromUriByPackage(packageName, deeplinkUrl, true)) {
                return deeplinkUrl;
            } else {
                this.leanbackCategoryIntentLauncher.launchIntent(this.context, packageName, null);
                return null;
            }
        } else if (TextUtils.isEmpty(marketUrl)) {
            this.playStoreIntentLauncher.launchIntent(this.context, packageName, null);
            return null;
        } else {
            if (!launchIntentFromUri(marketUrl, false)) {
                this.playStoreIntentLauncher.launchIntent(this.context, packageName, null);
            }
            return null;
        }
    }

    public String launchMediaIntentForDirectAd(String packageName, String dataUrl) {
        if (TextUtils.isEmpty(packageName)) {
            try {
                packageName = Intent.parseUri(dataUrl, 1).getPackage();
            } catch (URISyntaxException e) {
                String valueOf = String.valueOf(dataUrl);
                Log.e(TAG, valueOf.length() != 0 ? "Bad URI syntax: ".concat(valueOf) : "Bad URI syntax: ");
                return null;
            }
        }
        if (TextUtils.isEmpty(packageName)) {
            if (TextUtils.isEmpty(dataUrl)) {
                Log.e(TAG, "Failed to launch. Both packageName and dataUrl are empty.");
                return null;
            } else if (launchIntentFromUri(dataUrl, true)) {
                return dataUrl;
            } else {
                return null;
            }
        } else if (!PackageUtils.isPackageInstalled(this.context, packageName)) {
            this.playStoreIntentLauncher.launchIntent(this.context, packageName, null);
            return null;
        } else if (TextUtils.isEmpty(dataUrl)) {
            this.leanbackCategoryIntentLauncher.launchIntent(this.context, packageName, null);
            return null;
        } else if (launchIntentFromUriByPackage(packageName, dataUrl, true)) {
            return dataUrl;
        } else {
            this.leanbackCategoryIntentLauncher.launchIntent(this.context, packageName, null);
            return null;
        }
    }

    public boolean launchChannelIntentFromUri(String packageName, String uri, boolean launchMedia) {
        boolean success = launchIntentFromUriByPackage(packageName, uri, launchMedia) || this.tvRecommendationsIntentLauncher.launchIntent(this.context, packageName, this.intent);
        if (!success) {
            Toast.makeText(this.context, C1167R.string.failed_launch, 0).show();
        }
        return success;
    }

    public boolean launchChannelIntentFromUriWithAnimation(String packageName, String uri, boolean launchMedia, View view) {
        boolean success = launchIntentFromUriByPackageWithAnimation(packageName, uri, launchMedia, view) || this.tvRecommendationsIntentLauncher.launchIntent(this.context, packageName, this.intent);
        if (!success) {
            Toast.makeText(this.context, C1167R.string.failed_launch, 0).show();
        }
        return success;
    }

    public boolean launchIntentFromUri(String uri, boolean launchMedia) {
        this.intent = parseUri(uri, launchMedia);
        this.viewToStartAnimation = null;
        return launchIntent();
    }

    private boolean launchIntentFromUriByPackage(String packageName, String uri, boolean launchMedia) {
        this.intent = parseUri(uri, launchMedia);
        if (packageName != null) {
            this.intent.setPackage(packageName);
        }
        this.viewToStartAnimation = null;
        return launchIntent();
    }

    private boolean launchIntentFromUriByPackageWithAnimation(String packageName, String uri, boolean launchMedia, View view) {
        this.intent = parseUri(uri, launchMedia);
        if (packageName != null) {
            this.intent.setPackage(packageName);
        }
        this.viewToStartAnimation = view;
        return launchIntent();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    private Intent parseUri(String uri, boolean launchMedia) {
        if (uri == null) {
            Log.e(TAG, "No URI provided");
            return null;
        }
        try {
            Intent intent2 = Intent.parseUri(uri, 1);
            if (launchMedia) {
                intent2.putExtra(IntentCompat.EXTRA_START_PLAYBACK, true);
            }
            return intent2;
        } catch (URISyntaxException e) {
            String valueOf = uri;
            Log.e(TAG, valueOf.length() != 0 ? "Bad URI syntax: ".concat(valueOf) : "Bad URI syntax: ");
            return null;
        }
    }

    private boolean launchIntent() {
        if (this.intent == null) {
            return false;
        }
        List<ResolveInfo> activities = this.context.getPackageManager().queryIntentActivities(this.intent, 0);
        this.intent.addFlags(C0847C.ENCODING_PCM_MU_LAW);
        if (activities == null || activities.size() <= 0) {
            return false;
        }
        try {
            if (this.viewToStartAnimation != null) {
                LaunchUtil.startActivityWithAnimation(this.intent, this.viewToStartAnimation);
                return true;
            }
            this.context.startActivity(this.intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            String valueOf = String.valueOf(this.intent);
            StringBuilder sb = new StringBuilder(valueOf.length() + 17);
            sb.append("Failed to launch ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString(), e);
            return false;
        }
    }
}
