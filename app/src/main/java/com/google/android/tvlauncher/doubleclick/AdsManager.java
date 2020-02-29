package com.google.android.tvlauncher.doubleclick;

import android.content.ContentValues;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.tvlauncher.application.TvLauncherApplication;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AdsManager {
    private static final int CORE_POOL_SIZE = 3;
    private static final boolean DEBUG = false;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int MAXIMUM_POOL_SIZE = 3;
    private static final String TAG = "AdsManager";
    private static final int WAITING_QUEUE_CAPACITY = 128;
    private final AdvertisingIdClientWrapper advertisingIdClientWrapper;
    private final Context appContext;
    private final DoubleClickAdConfigSerializer doubleClickAdConfigSerializer;
    private final DoubleClickAdServer doubleClickAdServer;
    private final DoubleClickThreadPoolExecutor executor;
    private final OutstreamVideoAdFactory outstreamVideoAdFactory;

    public AdsManager(Context context) {
        this(context, ((TvLauncherApplication) context.getApplicationContext()).getDoubleClickAdServer(), ((TvLauncherApplication) context.getApplicationContext()).getDoubleClickAdConfigSerializer(), new AdvertisingIdClientWrapper(), ((TvLauncherApplication) context.getApplicationContext()).getOutstreamVideoAdFactory(), new DoubleClickThreadPoolExecutor(3, 3, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(128)));
    }

    AdsManager(Context context, DoubleClickAdServer doubleClickAdServer2, DoubleClickAdConfigSerializer doubleClickAdConfigSerializer2, AdvertisingIdClientWrapper advertisingIdClientWrapper2, OutstreamVideoAdFactory outstreamVideoAdFactory2, DoubleClickThreadPoolExecutor executor2) {
        this.appContext = context.getApplicationContext();
        this.doubleClickAdServer = doubleClickAdServer2;
        this.doubleClickAdConfigSerializer = doubleClickAdConfigSerializer2;
        this.advertisingIdClientWrapper = advertisingIdClientWrapper2;
        this.outstreamVideoAdFactory = outstreamVideoAdFactory2;
        this.executor = executor2;
    }

    public void processAdRequest(long programId, String adId) {
        this.executor.addTaskAndExecuteIfNeeded(new AdLoaderTask(this.appContext, programId, adId, this.doubleClickAdServer, this.doubleClickAdConfigSerializer, this.advertisingIdClientWrapper, this.outstreamVideoAdFactory));
    }

    public void recordImpression(String oldImpressionTrackingUrl, String newImpressionTrackingUrl) {
        this.executor.addTaskAndExecuteIfNeeded(new ImpressionTrackerTask(this.doubleClickAdServer, oldImpressionTrackingUrl, newImpressionTrackingUrl));
    }

    public void recordFocusImpression(String focusTrackingUrl) {
        this.executor.addTaskAndExecuteIfNeeded(new FocusImpressionTrackerTask(this.doubleClickAdServer, focusTrackingUrl));
    }

    public void recordImpressionsInBatch(List<String> newImpressionTrackingUrls) {
        this.executor.addTaskAndExecuteIfNeeded(new ImpressionBatchTrackerTask(this.doubleClickAdServer, newImpressionTrackingUrls));
    }

    public void recordClick(String clickTrackingUrl) {
        this.executor.addTaskAndExecuteIfNeeded(new ClickTrackerTask(this.doubleClickAdServer, clickTrackingUrl));
    }

    public void removeRequestedTrackingUrls(Set<String> requestedTrackingUrls) {
        this.doubleClickAdServer.removeRequestedTrackingUrls(requestedTrackingUrls);
    }

    private static class AdLoaderTask implements Runnable {
        private final String adId;
        private final AdvertisingIdClientWrapper advertisingIdClientWrapper;
        private final Context appContext;
        private final DoubleClickAdConfigSerializer doubleClickAdConfigSerializer;
        private final DoubleClickAdServer doubleClickAdServer;
        private final OutstreamVideoAdFactory outstreamVideoAdFactory;
        private final long programId;

        AdLoaderTask(Context context, long programId2, String adId2, DoubleClickAdServer doubleClickAdServer2, DoubleClickAdConfigSerializer doubleClickAdConfigSerializer2, AdvertisingIdClientWrapper advertisingIdClientWrapper2, OutstreamVideoAdFactory outstreamVideoAdFactory2) {
            this.appContext = context;
            this.programId = programId2;
            this.adId = adId2;
            this.doubleClickAdServer = doubleClickAdServer2;
            this.doubleClickAdConfigSerializer = doubleClickAdConfigSerializer2;
            this.advertisingIdClientWrapper = advertisingIdClientWrapper2;
            this.outstreamVideoAdFactory = outstreamVideoAdFactory2;
        }

        public int hashCode() {
            return Objects.hash(this.adId, Long.valueOf(this.programId));
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof AdLoaderTask)) {
                return false;
            }
            AdLoaderTask adLoaderTask = (AdLoaderTask) obj;
            if (!TextUtils.equals(this.adId, adLoaderTask.adId) || this.programId != adLoaderTask.programId) {
                return false;
            }
            return true;
        }

        public void run() {
            Process.setThreadPriority(10);
            AdvertisingIdClient.Info advertisingIdInfo = null;
            try {
                advertisingIdInfo = this.advertisingIdClientWrapper.getAdvertisingIdInfo(this.appContext);
            } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException | IOException e) {
                Log.e(AdsManager.TAG, "AdLoaderTask: could not get advertisingIdInfo", e);
            }
            InputStream inputStream = this.doubleClickAdServer.getDoubleClickAdFromServer(this.adId, advertisingIdInfo);
            if (inputStream != null) {
                OutstreamVideoAd outstreamVideoAd = this.outstreamVideoAdFactory.createOutstreamVideoAdFromAdResponse(this.adId, inputStream);
                if (outstreamVideoAd == null) {
                    String valueOf = String.valueOf(this.adId);
                    Log.e(AdsManager.TAG, valueOf.length() != 0 ? "AdLoaderTask: failed to create outstream video ad for ad id: ".concat(valueOf) : new String("AdLoaderTask: failed to create outstream video ad for ad id: "));
                    return;
                }
                byte[] adConfigBlob = this.doubleClickAdConfigSerializer.serialize(outstreamVideoAd.getAdAsset());
                ContentValues contentValues = new ContentValues();
                contentValues.put(TvContractCompat.ProgramColumns.COLUMN_POSTER_ART_URI, outstreamVideoAd.getImageUri());
                if (!TextUtils.isEmpty(outstreamVideoAd.getVideoUri())) {
                    contentValues.put(TvContractCompat.PreviewProgramColumns.COLUMN_PREVIEW_VIDEO_URI, outstreamVideoAd.getVideoUri());
                } else {
                    contentValues.putNull(TvContractCompat.PreviewProgramColumns.COLUMN_PREVIEW_VIDEO_URI);
                }
                contentValues.put("internal_provider_data", adConfigBlob);
                this.appContext.getContentResolver().update(TvContractCompat.buildPreviewProgramUri(this.programId), contentValues, null, null);
            }
        }
    }

    private static class ImpressionTrackerTask implements Runnable {
        private final DoubleClickAdServer doubleClickAdServer;
        private final String newImpressionTrackingUrl;
        private final String oldImpressionTrackingUrl;

        ImpressionTrackerTask(DoubleClickAdServer doubleClickAdServer2, String oldImpressionTrackingUrl2, String newImpressionTrackingUrl2) {
            this.doubleClickAdServer = doubleClickAdServer2;
            this.oldImpressionTrackingUrl = oldImpressionTrackingUrl2;
            this.newImpressionTrackingUrl = newImpressionTrackingUrl2;
        }

        public int hashCode() {
            return this.newImpressionTrackingUrl.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ImpressionTrackerTask)) {
                return false;
            }
            return TextUtils.equals(this.newImpressionTrackingUrl, ((ImpressionTrackerTask) obj).newImpressionTrackingUrl);
        }

        public void run() {
            Process.setThreadPriority(10);
            this.doubleClickAdServer.pingImpressionTrackingUrl(this.oldImpressionTrackingUrl, this.newImpressionTrackingUrl);
        }
    }

    private static class ImpressionBatchTrackerTask implements Runnable {
        private final DoubleClickAdServer doubleClickAdServer;
        private final List<String> newImpressionTrackingUrls;

        ImpressionBatchTrackerTask(DoubleClickAdServer doubleClickAdServer2, List<String> newImpressionTrackingUrls2) {
            this.doubleClickAdServer = doubleClickAdServer2;
            this.newImpressionTrackingUrls = newImpressionTrackingUrls2;
        }

        public int hashCode() {
            return this.newImpressionTrackingUrls.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ImpressionBatchTrackerTask)) {
                return false;
            }
            return this.newImpressionTrackingUrls.equals(((ImpressionBatchTrackerTask) obj).newImpressionTrackingUrls);
        }

        public void run() {
            Process.setThreadPriority(10);
            this.doubleClickAdServer.pingImpressionTrackingUrlsInBatch(this.newImpressionTrackingUrls);
        }
    }

    private static class ClickTrackerTask implements Runnable {
        private final String clickTrackingUrl;
        private final DoubleClickAdServer doubleClickAdServer;

        ClickTrackerTask(DoubleClickAdServer doubleClickAdServer2, String clickTrackingUrl2) {
            this.doubleClickAdServer = doubleClickAdServer2;
            this.clickTrackingUrl = clickTrackingUrl2;
        }

        public int hashCode() {
            return this.clickTrackingUrl.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ClickTrackerTask)) {
                return false;
            }
            return TextUtils.equals(this.clickTrackingUrl, ((ClickTrackerTask) obj).clickTrackingUrl);
        }

        public void run() {
            Process.setThreadPriority(10);
            this.doubleClickAdServer.pingTrackingUrl(this.clickTrackingUrl);
        }
    }

    private static class FocusImpressionTrackerTask implements Runnable {
        private final DoubleClickAdServer doubleClickAdServer;
        private final String focusTrackingUrl;

        FocusImpressionTrackerTask(DoubleClickAdServer doubleClickAdServer2, String focusTrackingUrl2) {
            this.doubleClickAdServer = doubleClickAdServer2;
            this.focusTrackingUrl = focusTrackingUrl2;
        }

        public int hashCode() {
            return this.focusTrackingUrl.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof FocusImpressionTrackerTask)) {
                return false;
            }
            return TextUtils.equals(this.focusTrackingUrl, ((FocusImpressionTrackerTask) obj).focusTrackingUrl);
        }

        public void run() {
            Process.setThreadPriority(10);
            this.doubleClickAdServer.pingTrackingUrl(this.focusTrackingUrl);
        }
    }
}
