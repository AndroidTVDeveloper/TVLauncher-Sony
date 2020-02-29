package com.google.android.tvlauncher.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.settings.ProfilesManager;
import com.google.android.tvlauncher.util.OemAppPromotionsXmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OemAppPromotions implements LaunchItemsManager.AppsViewChangeListener {
    private static final long PROMOTIONS_LOAD_THROTTLE_TIME = 900000;
    private static final String TAG = "OemAppPromotions";
    private static final Object lock = new Object();
    private static OemAppPromotions oemAppPromotions = null;
    /* access modifiers changed from: private */
    public final Set<String> allPromotionIds = new HashSet();
    /* access modifiers changed from: private */
    public final List<OemPromotionApp> allPromotions = new ArrayList();
    /* access modifiers changed from: private */
    public List<OnAppPromotionsLoadedListener> appPromotionsLoadedListeners;
    private ContentObserver appPromotionsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            if (PartnerCustomizationContract.OEM_APP_RECS_URI.equals(uri)) {
                OemAppPromotions.this.readAppPromotions(false);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context context;
    private final String packageName;
    private long promotionsLastLoadedTime = -1;
    /* access modifiers changed from: private */
    public String promotionsRowTitle;
    /* access modifiers changed from: private */
    public final List<OemPromotionApp> visiblePromotions = new ArrayList();

    public interface OnAppPromotionsLoadedListener {
        void onAppPromotionsLoaded(List<OemPromotionApp> list);
    }

    private OemAppPromotions(Context context2, String packageName2) {
        this.packageName = packageName2;
        this.context = context2 != null ? context2.getApplicationContext() : null;
        this.appPromotionsLoadedListeners = new ArrayList(2);
    }

    public static OemAppPromotions get(Context context2) {
        synchronized (lock) {
            if (oemAppPromotions == null) {
                String packageName2 = getPromotionsApp(context2.getPackageManager());
                if (packageName2 != null) {
                    oemAppPromotions = new OemAppPromotions(context2, packageName2);
                }
                if (oemAppPromotions == null) {
                    oemAppPromotions = new OemAppPromotions(null, null);
                }
            }
        }
        return oemAppPromotions;
    }

    public static void resetIfNecessary(Context context2, String packageName2) {
        synchronized (lock) {
            if (oemAppPromotions != null && !TextUtils.isEmpty(packageName2) && packageName2.equals(oemAppPromotions.getPackageName())) {
                get(context2).readAppPromotions(true);
            }
        }
    }

    private static String getPromotionsApp(PackageManager pm) {
        ProviderInfo info = pm.resolveContentProvider("tvlauncher.apprecs", 0);
        if (info == null || (info.applicationInfo.flags & 1) == 0) {
            return null;
        }
        return info.packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getAppsPromotionRowTitle() {
        return this.promotionsRowTitle;
    }

    public void registerOnAppPromotionsLoadedListener(OnAppPromotionsLoadedListener listener) {
        if (!this.appPromotionsLoadedListeners.contains(listener)) {
            this.appPromotionsLoadedListeners.add(listener);
        }
        try {
            if (this.context != null && this.appPromotionsLoadedListeners.size() == 1) {
                this.context.getContentResolver().registerContentObserver(PartnerCustomizationContract.OEM_APP_RECS_URI, true, this.appPromotionsObserver);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "failed to register content observer for app promotions", e);
        }
    }

    public void unregisterOnAppPromotionsLoadedListener(OnAppPromotionsLoadedListener listener) {
        if (this.context != null && this.appPromotionsLoadedListeners.size() == 1 && this.appPromotionsLoadedListeners.contains(listener)) {
            this.context.getContentResolver().unregisterContentObserver(this.appPromotionsObserver);
        }
        this.appPromotionsLoadedListeners.remove(listener);
    }

    public void readAppPromotions(boolean forceRefresh) {
        Context context2 = this.context;
        if (context2 != null && !ProfilesManager.getInstance(context2).isRestrictedProfile() && this.appPromotionsLoadedListeners.size() > 0) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long j = this.promotionsLastLoadedTime;
            if (elapsedRealtime - j > PROMOTIONS_LOAD_THROTTLE_TIME || j < 0 || forceRefresh) {
                this.promotionsLastLoadedTime = SystemClock.elapsedRealtime();
                new AppPromotionsLoadingTask(this.context).execute(new Void[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPromotionApp(String id) {
        return this.allPromotionIds.contains(id);
    }

    /* access modifiers changed from: package-private */
    public boolean isVisiblePromotionApp(String id) {
        for (OemPromotionApp promotion : this.visiblePromotions) {
            if (promotion.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean removePackageNamesFromPromotions(List<OemPromotionApp> promotions, Set<String> packageNames) {
        boolean update = false;
        Iterator<OemPromotionApp> iterator = promotions.iterator();
        while (iterator.hasNext()) {
            if (packageNames.contains(iterator.next().getId())) {
                update = true;
                iterator.remove();
            }
        }
        return update;
    }

    private void updateIfNeeded(boolean update) {
        if (update) {
            for (OnAppPromotionsLoadedListener listener : this.appPromotionsLoadedListeners) {
                listener.onAppPromotionsLoaded(this.visiblePromotions);
            }
        }
    }

    public void onLaunchItemsLoaded() {
        updateIfNeeded(removePackageNamesFromPromotions(this.visiblePromotions, LaunchItemsManagerProvider.getInstance(this.context).getAllLaunchItemsPackageName()));
    }

    public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> addedOrUpdatedItems) {
        Set<String> addedOrUpdatedPackageName = new HashSet<>();
        Iterator<LaunchItem> it = addedOrUpdatedItems.iterator();
        while (it.hasNext()) {
            addedOrUpdatedPackageName.add(it.next().getPackageName());
        }
        updateIfNeeded(removePackageNamesFromPromotions(this.visiblePromotions, addedOrUpdatedPackageName));
    }

    public void onLaunchItemsRemoved(ArrayList<LaunchItem> removedItems) {
        boolean update = false;
        Iterator<LaunchItem> it = removedItems.iterator();
        while (true) {
            if (it.hasNext()) {
                if (this.allPromotionIds.contains(it.next().getPackageName())) {
                    update = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (update) {
            this.visiblePromotions.clear();
            this.visiblePromotions.addAll(this.allPromotions);
            removePackageNamesFromPromotions(this.visiblePromotions, LaunchItemsManagerProvider.getInstance(this.context).getAllLaunchItemsPackageName());
            updateIfNeeded(true);
        }
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
    }

    private class AppPromotionsLoadingTask extends AsyncTask<Void, Void, Boolean> {
        private OemAppPromotionsXmlParser.PromotionsData promotionsData;
        private ContentResolver resolver;

        AppPromotionsLoadingTask(Context context) {
            this.resolver = context.getContentResolver();
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(Void... params) {
            boolean z = false;
            try {
                InputStream promotionsFile = this.resolver.openInputStream(PartnerCustomizationContract.OEM_APP_RECS_URI);
                if (promotionsFile != null) {
                    this.promotionsData = OemAppPromotionsXmlParser.getInstance(OemAppPromotions.this.context).parse(promotionsFile);
                    try {
                        promotionsFile.close();
                    } catch (IOException e) {
                        String valueOf = String.valueOf(PartnerCustomizationContract.OEM_APP_RECS_URI);
                        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 14);
                        sb.append("Error closing ");
                        sb.append(valueOf);
                        Log.e(OemAppPromotions.TAG, sb.toString(), e);
                    }
                } else {
                    String valueOf2 = String.valueOf(PartnerCustomizationContract.OEM_APP_RECS_URI);
                    StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 14);
                    sb2.append("Error opening ");
                    sb2.append(valueOf2);
                    Log.e(OemAppPromotions.TAG, sb2.toString());
                }
                if (this.promotionsData != null) {
                    z = true;
                }
                return Boolean.valueOf(z);
            } catch (Exception e2) {
                String valueOf3 = String.valueOf(PartnerCustomizationContract.OEM_APP_RECS_URI);
                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length() + 14);
                sb3.append("Error opening ");
                sb3.append(valueOf3);
                Log.e(OemAppPromotions.TAG, sb3.toString(), e2);
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean result) {
            super.onPostExecute((Object) result);
            if (result.booleanValue()) {
                if (!TextUtils.isEmpty(this.promotionsData.getRowTitle())) {
                    String unused = OemAppPromotions.this.promotionsRowTitle = this.promotionsData.getRowTitle();
                }
                OemAppPromotions.this.allPromotions.clear();
                OemAppPromotions.this.allPromotionIds.clear();
                OemAppPromotions.this.visiblePromotions.clear();
                OemAppPromotions.this.allPromotions.addAll(this.promotionsData.getPromotions());
                OemAppPromotions.this.visiblePromotions.addAll(this.promotionsData.getPromotions());
                for (OemPromotionApp promotion : OemAppPromotions.this.allPromotions) {
                    OemAppPromotions.this.allPromotionIds.add(promotion.getId());
                }
                OemAppPromotions oemAppPromotions = OemAppPromotions.this;
                boolean unused2 = oemAppPromotions.removePackageNamesFromPromotions(oemAppPromotions.visiblePromotions, LaunchItemsManagerProvider.getInstance(OemAppPromotions.this.context).getAllLaunchItemsPackageName());
                for (OnAppPromotionsLoadedListener listener : OemAppPromotions.this.appPromotionsLoadedListeners) {
                    listener.onAppPromotionsLoaded(OemAppPromotions.this.visiblePromotions);
                }
            }
        }
    }
}
