package com.google.android.tvlauncher.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import com.google.android.tvlauncher.application.TvLauncherApplicationBase;
import com.google.android.tvlauncher.data.DataLoadingBackgroundTask;
import com.google.android.tvlauncher.data.DataSourceObserver;
import com.google.android.tvlauncher.home.WatchNextPrefs;
import com.google.android.tvlauncher.model.Channel;
import com.google.android.tvlauncher.model.ChannelPackage;
import com.google.android.tvlauncher.model.HomeChannel;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.ChannelConfigurationInfo;
import com.google.android.tvlauncher.util.Executors;
import com.google.android.tvlauncher.util.GoogleConfiguration;
import com.google.android.tvlauncher.util.GoogleConfigurationManager;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.Constants;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TvDataManager implements DataLoadingBackgroundTask.Callbacks {
    private static final int CHANNEL_PROGRAM_COUNT_BATCH_SIZE = 1000;
    private static final int COLUMN_INDEX_CONTENT_ID = 0;
    private static final int COLUMN_INDEX_PACKAGE_NAME = 1;
    private static final boolean DEBUG = false;
    private static final LongSparseArray<ChannelConfigurationInfo> EMPTY_PINNED_CHANNEL_CONFIG = new LongSparseArray<>(0);
    public static final String ENABLE_PREVIEW_AUDIO_KEY = "enable_preview_audio_key";
    public static final String ENABLE_PREVIEW_VIDEO_KEY = "show_preview_video_key";
    private static final int FIRST_PROGRAMS_CHANNELS_ALWAYS_CACHED = 5;
    private static final String HOME_CHANNELS_SELECTION = "browsable=1 AND type='TYPE_PREVIEW'";
    private static final String KEY_BOOT_COUNT = "boot_count";
    private static final String KEY_LIVE_TV_CHANNEL_ID = "live_tv_channel_id";
    private static final String KEY_NON_EMPTY_OLD_HOME_CHANNEL_IDS = "non_empty_old_home_channel_ids";
    private static final long LIVE_TV_CHANNEL_NO_ID = -1;
    private static final String LIVE_TV_CHANNEL_PREF_FILE_NAME = "com.google.android.tvlauncher.data.TvDataManager.LIVE_TV_CHANNEL_PREF";
    private static final int MAX_PROGRAMS_CHANNELS_CACHED = 15;
    private static final int MAX_WATCH_NEXT_PROGRAMS = 1000;
    private static final String OLD_CHANNEL_IDS_SEPARATOR = ",";
    private static final String PACKAGE_CHANNELS_SELECTION = "type='TYPE_PREVIEW'";
    public static final String PREVIEW_MEDIA_PREF_FILE_NAME = "com.google.android.tvlauncher.data.TvDataManager.PREVIEW_VIDEO_PREF_FILE_NAME";
    private static final String PROGRAMS_BY_CHANNEL_ID_SELECTION = "channel_id=? AND browsable=1";
    private static final String PROMO_CHANNEL_SELECTION = "package_name=?";
    public static Provider PROVIDER = TvDataManager$$Lambda$2.$instance;
    private static final long SPONSORED_GOOGLE_CHANNEL_NO_ID = -1;
    private static final int SPONSORED_GOOGLE_CHANNEL_NO_OOB_POSITION = -1;
    private static final String TAG = "TvDataManager";
    private static final int TASK_HOME_CHANNELS = 0;
    private static final int TASK_HOME_CHANNEL_PROGRAMS = 1;
    private static final int TASK_PACKAGE_CHANNELS = 2;
    private static final int TASK_PROMO_CHANNEL = 3;
    private static final int TASK_WATCH_NEXT_CACHE = 5;
    private static final int TASK_WATCH_NEXT_PROGRAMS = 4;
    private static final String TV_DATA_MANAGER_PREF_FILE_NAME = "com.google.android.tvlauncher.data.TvDataManager.PREF";
    private static final String[] WATCH_NEXT_CACHE_PROJECTION = {"content_id", "package_name"};
    public static final long WATCH_NEXT_REQUERY_INTERVAL_MILLIS = 600000;
    private static final String WATCH_NEXT_SELECTION = "browsable=1 AND last_engagement_time_utc_millis<=?";
    private static int channelProgramCountBatchSize = 1000;
    private static TvDataManager instance;
    /* access modifiers changed from: private */
    public Map<Long, HomeChannel> browsableChannels = new HashMap();
    private Queue<Long> cachedProgramsChannelOrder = new ArrayDeque();
    /* access modifiers changed from: private */
    public Map<Long, Uri> channelLogoUris = new HashMap();
    /* access modifiers changed from: private */
    public ChannelOrderManager channelOrderManager;
    /* access modifiers changed from: private */
    public Map<Long, ProgramsDataBuffer> channelPrograms = new HashMap();
    /* access modifiers changed from: private */
    public Map<Long, List<ChannelProgramsObserver>> channelProgramsObservers = new HashMap();
    /* access modifiers changed from: private */
    public final Context context;
    private final DataSourceObserver dataSourceObserver;
    private final GoogleConfigurationManager googleConfigurationManager;
    private final Handler handler = new Handler();
    /* access modifiers changed from: private */
    public HashSet<Long> homeChannelIds = new HashSet<>();
    private Map<Long, DataLoadingBackgroundTask> homeChannelProgramsBackgroundTasks = new HashMap();
    /* access modifiers changed from: private */
    public List<HomeChannel> homeChannels;
    private DataLoadingBackgroundTask homeChannelsBackgroundTask;
    /* access modifiers changed from: private */
    public List<HomeChannelsObserver> homeChannelsObservers = new LinkedList();
    /* access modifiers changed from: private */
    public boolean homeChannelsStale = true;
    private boolean isFirstLaunchAfterBoot = true;
    private long liveTvChannelId;
    private SharedPreferences liveTvChannelPref;
    /* access modifiers changed from: private */
    public Set<Long> nonEmptyHomeChannelIds;
    private Map<String, List<Channel>> packageChannels;
    private DataLoadingBackgroundTask packageChannelsBackgroundTask;
    private List<PackageChannelsObserver> packageChannelsObservers = new LinkedList();
    private List<ChannelPackage> packagesWithChannels;
    private List<PackagesWithChannelsObserver> packagesWithChannelsObservers = new LinkedList();
    private PinnedChannelOrderManager pinnedChannelOrderManager = new PinnedChannelOrderManager();
    /* access modifiers changed from: private */
    public Map<Long, Long> programChannelIds = Collections.synchronizedMap(new HashMap());
    private Channel promoChannel;
    private DataLoadingBackgroundTask promoChannelBackgroundTask;
    private boolean promoChannelLoaded;
    /* access modifiers changed from: private */
    public List<PromoChannelObserver> promoChannelObservers = new LinkedList();
    /* access modifiers changed from: private */
    public Set<Long> staleProgramsChannels = new HashSet();
    private SharedPreferences tvDataManagerPref;
    private DataLoadingBackgroundTask watchNextCacheBackgroundTask;
    private Set<String> watchNextContentIdsCache = new HashSet();
    private final SharedPreferences.OnSharedPreferenceChangeListener watchNextPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.startsWith(WatchNextPrefs.WATCH_NEXT_PACKAGE_KEY_PREFIX)) {
                TvDataManager tvDataManager = TvDataManager.this;
                String unused = tvDataManager.watchNextSelection = tvDataManager.buildWatchNextSelection(sharedPreferences);
                TvDataManager.this.loadWatchNextProgramData();
            }
        }
    };
    private WatchNextProgramsDataBuffer watchNextPrograms;
    private DataLoadingBackgroundTask watchNextProgramsBackgroundTask;
    /* access modifiers changed from: private */
    public List<WatchNextProgramsObserver> watchNextProgramsObservers = new LinkedList();
    /* access modifiers changed from: private */
    public boolean watchNextProgramsStale = true;
    /* access modifiers changed from: private */
    public String watchNextSelection;

    public interface Provider {
        TvDataManager get(Context context);
    }

    static void setChannelProgramCountBatchSize(int channelProgramCountBatchSize2) {
        channelProgramCountBatchSize = channelProgramCountBatchSize2;
    }

    public static TvDataManager getInstance(Context context2) {
        if (instance == null) {
            instance = new TvDataManager(context2, ((TvLauncherApplicationBase) context2.getApplicationContext()).getGoogleConfigurationManager());
        }
        return instance;
    }

    TvDataManager(Context context2, GoogleConfigurationManager googleConfigurationManager2) {
        this.context = context2.getApplicationContext();
        this.googleConfigurationManager = googleConfigurationManager2;
        this.dataSourceObserver = new DataSourceObserver(this.context, new DataSourceObserverCallbacks());
        SharedPreferences watchNextPreferences = context2.getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0);
        watchNextPreferences.registerOnSharedPreferenceChangeListener(this.watchNextPrefListener);
        this.watchNextSelection = buildWatchNextSelection(watchNextPreferences);
        loadAllWatchNextProgramDataIntoCache();
        this.liveTvChannelPref = this.context.getSharedPreferences(LIVE_TV_CHANNEL_PREF_FILE_NAME, 0);
        this.liveTvChannelId = this.liveTvChannelPref.getLong(KEY_LIVE_TV_CHANNEL_ID, -1);
        this.tvDataManagerPref = this.context.getSharedPreferences(TV_DATA_MANAGER_PREF_FILE_NAME, 0);
        String allOldHomeChannelIds = this.tvDataManagerPref.getString(KEY_NON_EMPTY_OLD_HOME_CHANNEL_IDS, null);
        if (allOldHomeChannelIds != null) {
            for (String homeChannelIdStr : allOldHomeChannelIds.split(OLD_CHANNEL_IDS_SEPARATOR)) {
                try {
                    this.homeChannelIds.add(Long.valueOf(Long.parseLong(homeChannelIdStr)));
                } catch (NumberFormatException e) {
                    StringBuilder sb = new StringBuilder(String.valueOf(homeChannelIdStr).length() + 61 + allOldHomeChannelIds.length());
                    sb.append("Invalid channel ID: [");
                    sb.append(homeChannelIdStr);
                    sb.append("] in old home channel ids shared pref [");
                    sb.append(allOldHomeChannelIds);
                    sb.append("]");
                    Log.e(TAG, sb.toString());
                }
            }
        }
        this.nonEmptyHomeChannelIds = (Set) this.homeChannelIds.clone();
    }

    /* access modifiers changed from: private */
    public void saveNonEmptyHomeChannelIdsToSharedPref() {
        SharedPreferences.Editor editor = this.tvDataManagerPref.edit();
        if (this.nonEmptyHomeChannelIds.size() > 0) {
            StringBuilder idsString = new StringBuilder(128);
            for (Long homeChannelId : this.nonEmptyHomeChannelIds) {
                idsString.append(homeChannelId);
                idsString.append(OLD_CHANNEL_IDS_SEPARATOR);
            }
            editor.putString(KEY_NON_EMPTY_OLD_HOME_CHANNEL_IDS, idsString.toString());
        } else {
            editor.putString(KEY_NON_EMPTY_OLD_HOME_CHANNEL_IDS, "");
        }
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public void setChannelOrderManager(ChannelOrderManager channelOrderManager2) {
        this.channelOrderManager = channelOrderManager2;
    }

    /* access modifiers changed from: package-private */
    public void setPinnedChannelOrderManager(PinnedChannelOrderManager pinnedChannelOrderManager2) {
        this.pinnedChannelOrderManager = pinnedChannelOrderManager2;
    }

    private void logObserversState() {
        int channelsObserved = this.channelProgramsObservers.size();
        int channelObservers = 0;
        for (List<ChannelProgramsObserver> observers : this.channelProgramsObservers.values()) {
            channelObservers += observers.size();
        }
        int size = this.homeChannelsObservers.size();
        int size2 = this.packagesWithChannelsObservers.size();
        int size3 = this.packageChannelsObservers.size();
        int size4 = this.promoChannelObservers.size();
        int size5 = this.watchNextProgramsObservers.size();
        StringBuilder sb = new StringBuilder((int) ClientAnalytics.LogRequest.LogSource.SNAPSEED_IOS_PRIMES_VALUE);
        sb.append("Observers: \nhomeChannelsObservers.size()=");
        sb.append(size);
        sb.append("\nchannelProgramsObservers.size()=");
        sb.append(channelsObserved);
        sb.append(" (total: ");
        sb.append(channelObservers);
        sb.append(")\npackagesWithChannelsObservers.size()=");
        sb.append(size2);
        sb.append("\npackageChannelsObservers.size()=");
        sb.append(size3);
        sb.append("\npromoChannelObservers.size()=");
        sb.append(size4);
        sb.append("\nwatchNextProgramsObservers.size()=");
        sb.append(size5);
        Log.d(TAG, sb.toString());
    }

    private void logCachesState() {
        List<HomeChannel> list = this.homeChannels;
        Object obj = "null";
        String valueOf = String.valueOf(list != null ? Integer.valueOf(list.size()) : obj);
        Map<Long, ProgramsDataBuffer> map = this.channelPrograms;
        String valueOf2 = String.valueOf(map != null ? Integer.valueOf(map.size()) : obj);
        List<ChannelPackage> list2 = this.packagesWithChannels;
        String valueOf3 = String.valueOf(list2 != null ? Integer.valueOf(list2.size()) : obj);
        Map<String, List<Channel>> map2 = this.packageChannels;
        String valueOf4 = String.valueOf(map2 != null ? Integer.valueOf(map2.size()) : obj);
        String valueOf5 = String.valueOf(this.cachedProgramsChannelOrder);
        String valueOf6 = String.valueOf(this.staleProgramsChannels);
        WatchNextProgramsDataBuffer watchNextProgramsDataBuffer = this.watchNextPrograms;
        if (watchNextProgramsDataBuffer != null) {
            obj = Integer.valueOf(watchNextProgramsDataBuffer.getCount());
        }
        String valueOf7 = String.valueOf(obj);
        StringBuilder sb = new StringBuilder(valueOf.length() + 150 + valueOf2.length() + valueOf3.length() + valueOf4.length() + valueOf5.length() + valueOf6.length() + valueOf7.length());
        sb.append("Cache: \nhomeChannels=");
        sb.append(valueOf);
        sb.append("\nchannelPrograms=");
        sb.append(valueOf2);
        sb.append("\npackagesWithChannels=");
        sb.append(valueOf3);
        sb.append("\npackageChannels=");
        sb.append(valueOf4);
        sb.append("\ncachedProgramsChannelOrder: ");
        sb.append(valueOf5);
        sb.append("\nstaleProgramsChannels: ");
        sb.append(valueOf6);
        sb.append("\nwatchNextPrograms: ");
        sb.append(valueOf7);
        Log.d(TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public DataSourceObserver getDataSourceObserver() {
        return this.dataSourceObserver;
    }

    private void registerDataSourceObserver() {
        this.dataSourceObserver.register();
    }

    private void unregisterDataSourceObserverIfNoObservers() {
        if (this.homeChannelsObservers.size() == 0 && this.channelProgramsObservers.size() == 0 && this.packagesWithChannelsObservers.size() == 0 && this.packageChannelsObservers.size() == 0 && this.promoChannelObservers.size() == 0 && this.watchNextProgramsObservers.size() == 0) {
            this.homeChannelsStale = true;
            this.channelLogoUris.clear();
            this.staleProgramsChannels.addAll(this.channelPrograms.keySet());
            this.watchNextProgramsStale = true;
            this.dataSourceObserver.unregister();
        }
    }

    public void registerHomeChannelsObserver(HomeChannelsObserver observer) {
        if (!this.homeChannelsObservers.contains(observer)) {
            this.homeChannelsObservers.add(observer);
        }
        registerDataSourceObserver();
    }

    public void unregisterHomeChannelsObserver(HomeChannelsObserver observer) {
        DataLoadingBackgroundTask dataLoadingBackgroundTask = this.homeChannelsBackgroundTask;
        if (dataLoadingBackgroundTask != null) {
            dataLoadingBackgroundTask.cancel();
            this.homeChannelsBackgroundTask = null;
        }
        this.homeChannelsObservers.remove(observer);
        unregisterDataSourceObserverIfNoObservers();
    }

    public void registerChannelProgramsObserver(long channelId, ChannelProgramsObserver observer) {
        addToMultimap(this.channelProgramsObservers, Long.valueOf(channelId), observer);
        registerDataSourceObserver();
    }

    public void unregisterChannelProgramsObserver(long channelId, ChannelProgramsObserver observer) {
        removeFromMultimap(this.channelProgramsObservers, Long.valueOf(channelId), observer);
        unregisterDataSourceObserverIfNoObservers();
    }

    public void registerPackagesWithChannelsObserver(PackagesWithChannelsObserver observer) {
        if (!this.packagesWithChannelsObservers.contains(observer)) {
            this.packagesWithChannelsObservers.add(observer);
        }
        registerDataSourceObserver();
    }

    public void unregisterPackagesWithChannelsObserver(PackagesWithChannelsObserver observer) {
        this.packagesWithChannelsObservers.remove(observer);
        clearPackageChannelsCacheAndCancelTasksIfNoObservers();
        unregisterDataSourceObserverIfNoObservers();
    }

    public void registerPackageChannelsObserver(PackageChannelsObserver observer) {
        if (!this.packageChannelsObservers.contains(observer)) {
            this.packageChannelsObservers.add(observer);
        }
        registerDataSourceObserver();
    }

    public void unregisterPackageChannelsObserver(PackageChannelsObserver observer) {
        this.packageChannelsObservers.remove(observer);
        clearPackageChannelsCacheAndCancelTasksIfNoObservers();
        unregisterDataSourceObserverIfNoObservers();
    }

    private void clearPackageChannelsCacheAndCancelTasksIfNoObservers() {
        if (this.packagesWithChannelsObservers.size() == 0 && this.packageChannelsObservers.size() == 0) {
            DataLoadingBackgroundTask dataLoadingBackgroundTask = this.packageChannelsBackgroundTask;
            if (dataLoadingBackgroundTask != null) {
                dataLoadingBackgroundTask.cancel();
                this.packageChannelsBackgroundTask = null;
            }
            this.packagesWithChannels = null;
            this.packageChannels = null;
        }
    }

    public void registerPromoChannelObserver(PromoChannelObserver observer) {
        if (!this.promoChannelObservers.contains(observer)) {
            this.promoChannelObservers.add(observer);
        }
        registerDataSourceObserver();
    }

    public void unregisterPromoChannelObserver(PromoChannelObserver observer) {
        DataLoadingBackgroundTask dataLoadingBackgroundTask = this.promoChannelBackgroundTask;
        if (dataLoadingBackgroundTask != null) {
            dataLoadingBackgroundTask.cancel();
            this.promoChannelBackgroundTask = null;
        }
        this.promoChannelObservers.remove(observer);
        if (this.promoChannelObservers.size() == 0) {
            this.promoChannel = null;
            this.promoChannelLoaded = false;
        }
        unregisterDataSourceObserverIfNoObservers();
    }

    public void registerWatchNextProgramsObserver(WatchNextProgramsObserver observer) {
        if (!this.watchNextProgramsObservers.contains(observer)) {
            this.watchNextProgramsObservers.add(observer);
        }
        registerDataSourceObserver();
    }

    public void unregisterWatchNextProgramsObserver(WatchNextProgramsObserver observer) {
        DataLoadingBackgroundTask dataLoadingBackgroundTask = this.watchNextProgramsBackgroundTask;
        if (dataLoadingBackgroundTask != null) {
            dataLoadingBackgroundTask.cancel();
            this.watchNextProgramsBackgroundTask = null;
        }
        this.watchNextProgramsObservers.remove(observer);
        unregisterDataSourceObserverIfNoObservers();
    }

    public boolean isInWatchNext(String contentId, String packageName) {
        if (contentId == null || packageName == null) {
            return false;
        }
        return this.watchNextContentIdsCache.contains(createKeyFor(contentId, packageName));
    }

    private String createKeyFor(String contentId, String packageName) {
        StringBuilder sb = new StringBuilder(String.valueOf(contentId).length() + 1 + String.valueOf(packageName).length());
        sb.append(contentId);
        sb.append("-");
        sb.append(packageName);
        return sb.toString();
    }

    private HashSet<String> extractWatchNextCache(Cursor cursor) {
        HashSet<String> contentIds = new HashSet<>();
        if (cursor == null || cursor.getCount() <= 0) {
            return contentIds;
        }
        cursor.moveToFirst();
        do {
            contentIds.add(createKeyFor(cursor.getString(0), cursor.getString(1)));
        } while (cursor.moveToNext());
        return contentIds;
    }

    public void refreshWatchNextOffset() {
        WatchNextProgramsDataBuffer watchNextProgramsDataBuffer = this.watchNextPrograms;
        if (watchNextProgramsDataBuffer != null && watchNextProgramsDataBuffer.refresh()) {
            notifyWatchNextProgramsChange();
        }
    }

    public void removeProgramFromWatchNextCache(String contentId, String packageName) {
        if (contentId != null && packageName != null) {
            this.watchNextContentIdsCache.remove(createKeyFor(contentId, packageName));
        }
    }

    public void addProgramToWatchNextCache(String contentId, String packageName) {
        if (contentId != null && packageName != null) {
            this.watchNextContentIdsCache.add(createKeyFor(contentId, packageName));
        }
    }

    private static <K, V> void addToMultimap(Map<K, List<V>> multimap, K key, V item) {
        List<V> list = multimap.get(key);
        if (list == null) {
            List<V> list2 = new LinkedList<>();
            list2.add(item);
            multimap.put(key, list2);
        } else if (!list.contains(item)) {
            list.add(item);
        }
    }

    private static <K, V> void removeFromMultimap(Map<K, List<V>> multimap, K key, V item) {
        List<V> list = multimap.get(key);
        if (list != null) {
            list.remove(item);
            if (list.size() == 0) {
                multimap.remove(key);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyHomeChannelsChange() {
        for (HomeChannelsObserver observer : this.homeChannelsObservers) {
            observer.onChannelsChange();
        }
    }

    private void notifyEmptyHomeChannelRemoved(int channelIndex) {
        for (HomeChannelsObserver observer : this.homeChannelsObservers) {
            observer.onEmptyChannelRemove(channelIndex);
        }
    }

    /* access modifiers changed from: private */
    public void notifyHomeChannelEmptyStatusChanged(int channelIndex) {
        for (HomeChannelsObserver observer : this.homeChannelsObservers) {
            observer.onChannelEmptyStatusChange(channelIndex);
        }
    }

    private void notifyChannelProgramsChange(long channelId) {
        List<ChannelProgramsObserver> observers = this.channelProgramsObservers.get(Long.valueOf(channelId));
        if (observers != null) {
            for (ChannelProgramsObserver observer : observers) {
                observer.onProgramsChange(channelId);
            }
        }
    }

    private void notifyPackagesWithChannelsChange() {
        for (PackagesWithChannelsObserver observer : this.packagesWithChannelsObservers) {
            observer.onPackagesChange();
        }
    }

    private void notifyPackageChannelsChange() {
        for (PackageChannelsObserver observer : this.packageChannelsObservers) {
            observer.onChannelsChange();
        }
    }

    private void notifyPromoChannelChange() {
        for (PromoChannelObserver observer : this.promoChannelObservers) {
            observer.onChannelChange();
        }
    }

    private void notifyWatchNextProgramsChange() {
        for (WatchNextProgramsObserver observer : this.watchNextProgramsObservers) {
            observer.onProgramsChange();
        }
    }

    public boolean isHomeChannelDataLoaded() {
        return this.homeChannels != null;
    }

    public boolean isHomeChannelDataStale() {
        return this.homeChannelsStale;
    }

    public int getHomeChannelCount() {
        List<HomeChannel> list = this.homeChannels;
        if (list != null) {
            return list.size();
        }
        return -1;
    }

    public HomeChannel getHomeChannel(int position) {
        return this.homeChannels.get(position);
    }

    public boolean isChannelEmpty(long channelId) {
        return !this.nonEmptyHomeChannelIds.contains(Long.valueOf(channelId));
    }

    public boolean isHomeChannel(long channelId) {
        return this.homeChannelIds.contains(Long.valueOf(channelId));
    }

    public void loadHomeChannelData() {
        loadHomeChannelDataInternal();
    }

    public void removeHomeChannel(long channelId) {
        Integer position = this.channelOrderManager.getChannelPosition(channelId);
        if (position != null) {
            this.homeChannels.remove(position.intValue());
            this.homeChannelIds.remove(Long.valueOf(channelId));
            this.browsableChannels.remove(Long.valueOf(channelId));
        }
        setChannelBrowsable(channelId, false, false);
    }

    public void hideEmptyChannel(long channelId) {
        Integer position = this.channelOrderManager.getChannelPosition(channelId);
        if (position != null) {
            this.homeChannelIds.remove(Long.valueOf(channelId));
            this.channelOrderManager.onEmptyChannelHidden(this.homeChannels.remove(position.intValue()));
            notifyEmptyHomeChannelRemoved(position.intValue());
        }
    }

    public Uri getChannelLogoUri(Long channelId) {
        Uri uri = this.channelLogoUris.get(channelId);
        if (uri != null) {
            return uri;
        }
        Uri uri2 = TvContract.buildChannelLogoUri(channelId.longValue()).buildUpon().appendQueryParameter("t", String.valueOf(System.currentTimeMillis())).build();
        this.channelLogoUris.put(channelId, uri2);
        return uri2;
    }

    public boolean isProgramDataLoaded(long channelId) {
        return this.channelPrograms.containsKey(Long.valueOf(channelId));
    }

    public boolean isProgramDataStale(long channelId) {
        return this.staleProgramsChannels.contains(Long.valueOf(channelId));
    }

    public int getProgramCount(long channelId) {
        if (this.channelPrograms.containsKey(Long.valueOf(channelId))) {
            return this.channelPrograms.get(Long.valueOf(channelId)).getCount();
        }
        return -1;
    }

    public Program getProgram(long channelId, int position) {
        if (position >= 0) {
            ProgramsDataBuffer programs = this.channelPrograms.get(Long.valueOf(channelId));
            if (programs != null && position >= programs.getCount()) {
                StringBuilder sb = new StringBuilder(56);
                sb.append("Position [");
                sb.append(position);
                sb.append("] is out of bounds [0, ");
                sb.append(programs.getCount() - 1);
                sb.append("]");
                throw new IllegalArgumentException(sb.toString());
            } else if (programs != null) {
                return programs.get(position);
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("Position must be positive");
        }
    }

    public void loadProgramData(long channelId) {
        loadHomeChannelProgramData(channelId);
    }

    public void pruneChannelProgramsCache() {
        Iterator<Map.Entry<Long, ProgramsDataBuffer>> it = this.channelPrograms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ProgramsDataBuffer> entry = it.next();
            Long channelId = (Long) entry.getKey();
            if (!this.channelProgramsObservers.containsKey(channelId) && !areChannelProgramsAlwaysCached(channelId)) {
                it.remove();
                cleanupAfterChannelProgramsRemovalFromCache((ProgramsDataBuffer) entry.getValue(), channelId);
            }
        }
    }

    private void cleanupAfterChannelProgramsRemovalFromCache(ProgramsDataBuffer buffer, Long channelId) {
        if (buffer != null) {
            buffer.release();
        }
        this.staleProgramsChannels.remove(channelId);
        this.cachedProgramsChannelOrder.remove(channelId);
    }

    /* access modifiers changed from: private */
    public boolean areChannelProgramsAlwaysCached(Long channelId) {
        Integer position;
        ChannelOrderManager channelOrderManager2 = this.channelOrderManager;
        return channelOrderManager2 != null && (position = channelOrderManager2.getChannelPosition(channelId.longValue())) != null && position.intValue() < 5;
    }

    public boolean isPackagesWithChannelsDataLoaded() {
        return this.packagesWithChannels != null;
    }

    public List<ChannelPackage> getPackagesWithChannels() {
        return this.packagesWithChannels;
    }

    public void loadPackagesWithChannelsData() {
        loadPackageChannelsData();
    }

    public boolean isPackageChannelDataLoaded(String packageName) {
        Map<String, List<Channel>> map = this.packageChannels;
        return map != null && map.containsKey(packageName);
    }

    public List<Channel> getPackageChannels(String packageName) {
        return this.packageChannels.get(packageName);
    }

    public void loadPackageChannelsData(String packageName) {
        loadPackageChannelsData();
    }

    private boolean isPromoChannelLoaded() {
        return this.promoChannelLoaded;
    }

    public Channel getPromoChannel() {
        return this.promoChannel;
    }

    public void loadPromoChannel() {
        loadPromoChannelData();
    }

    public boolean isWatchNextProgramsDataLoaded() {
        return this.watchNextPrograms != null;
    }

    public boolean isWatchNextProgramsDataStale() {
        return this.watchNextProgramsStale;
    }

    public Program getWatchNextProgram(int position) {
        if (position >= 0) {
            WatchNextProgramsDataBuffer watchNextProgramsDataBuffer = this.watchNextPrograms;
            if (watchNextProgramsDataBuffer == null || position < watchNextProgramsDataBuffer.getCount()) {
                WatchNextProgramsDataBuffer watchNextProgramsDataBuffer2 = this.watchNextPrograms;
                if (watchNextProgramsDataBuffer2 != null) {
                    return watchNextProgramsDataBuffer2.get(position);
                }
                return null;
            }
            StringBuilder sb = new StringBuilder(56);
            sb.append("Position [");
            sb.append(position);
            sb.append("] is out of bounds [0, ");
            sb.append(this.watchNextPrograms.getCount() - 1);
            sb.append("]");
            throw new IllegalArgumentException(sb.toString());
        }
        throw new IllegalArgumentException("Position must be positive");
    }

    public int getWatchNextProgramsCount() {
        WatchNextProgramsDataBuffer watchNextProgramsDataBuffer = this.watchNextPrograms;
        if (watchNextProgramsDataBuffer != null) {
            return watchNextProgramsDataBuffer.getCount();
        }
        return 0;
    }

    public void loadWatchNextProgramData() {
        loadWatchNextProgramDataInternal();
    }

    /* access modifiers changed from: private */
    public void loadHomeChannelDataInternal() {
        if (this.homeChannelsBackgroundTask == null) {
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.Channels.CONTENT_URI).setProjection(HomeChannel.PROJECTION).setSelection(HOME_CHANNELS_SELECTION).setTag(0).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    private void removeOneChannelProgramsIfCacheTooBig() {
        if (this.channelPrograms.size() >= 15) {
            for (Long cachedChannelId : this.cachedProgramsChannelOrder) {
                if (!this.channelProgramsObservers.containsKey(cachedChannelId) && !areChannelProgramsAlwaysCached(cachedChannelId)) {
                    cleanupAfterChannelProgramsRemovalFromCache(this.channelPrograms.remove(cachedChannelId), cachedChannelId);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadHomeChannelProgramData(long channelId) {
        if (!this.homeChannelProgramsBackgroundTasks.containsKey(Long.valueOf(channelId))) {
            if (!this.channelPrograms.containsKey(Long.valueOf(channelId))) {
                removeOneChannelProgramsIfCacheTooBig();
            }
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.PreviewPrograms.CONTENT_URI).setProjection(Program.PROJECTION).setSelection(PROGRAMS_BY_CHANNEL_ID_SELECTION).setSelectionArg(String.valueOf(channelId)).setSortOrder("weight DESC").setExtraParam(Long.valueOf(channelId)).setTag(1).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    private void loadPackageChannelsData() {
        if (this.packageChannelsBackgroundTask == null) {
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.Channels.CONTENT_URI).setProjection(Channel.PROJECTION).setSelection(PACKAGE_CHANNELS_SELECTION).setTag(2).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    /* access modifiers changed from: private */
    public void loadPackageChannelsDataIfNeeded() {
        if (this.packagesWithChannelsObservers.size() > 0 || this.packageChannelsObservers.size() > 0) {
            loadPackageChannelsData();
        }
    }

    /* access modifiers changed from: private */
    public void loadPromoChannelData() {
        if (this.promoChannelBackgroundTask == null) {
            String promotionRowPackage = OemConfiguration.get(this.context).getAppsPromotionRowPackage();
            if (promotionRowPackage == null) {
                Log.e(TAG, "Promotion row package is not configured");
                this.handler.post(new TvDataManager$$Lambda$0(this));
                return;
            }
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.Channels.CONTENT_URI).setProjection(Channel.PROJECTION).setSelection(PROMO_CHANNEL_SELECTION).setSelectionArg(promotionRowPackage).setTag(3).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$loadPromoChannelData$0$TvDataManager() {
        setPromoChannelAndNotify(null);
    }

    /* access modifiers changed from: private */
    public void loadWatchNextProgramDataInternal() {
        if (this.watchNextProgramsBackgroundTask == null) {
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.WatchNextPrograms.CONTENT_URI).setProjection(Program.WATCH_NEXT_PROJECTION).setSelection(this.watchNextSelection).setSelectionArg(String.valueOf(System.currentTimeMillis() + WATCH_NEXT_REQUERY_INTERVAL_MILLIS)).setSortOrder("last_engagement_time_utc_millis DESC LIMIT 1000").setTag(4).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    public void loadAllWatchNextProgramDataIntoCache() {
        if (this.watchNextCacheBackgroundTask == null) {
            DataLoadingBackgroundTask task = DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.WatchNextPrograms.CONTENT_URI).setProjection(WATCH_NEXT_CACHE_PROJECTION).setSelection("browsable=1").setTag(5).setCallbacks(this);
            startTrackingTask(task);
            task.execute();
        }
    }

    /* access modifiers changed from: private */
    public String buildWatchNextSelection(SharedPreferences prefs) {
        StringBuilder sb = new StringBuilder(" NOT IN (");
        boolean hasPackages = false;
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(WatchNextPrefs.WATCH_NEXT_PACKAGE_KEY_PREFIX)) {
                sb.append("'");
                sb.append(key.substring(WatchNextPrefs.WATCH_NEXT_PACKAGE_KEY_PREFIX.length()));
                sb.append("',");
                hasPackages = true;
            }
        }
        if (!hasPackages) {
            return WATCH_NEXT_SELECTION;
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        String notInClause = sb.toString();
        String valueOf = "browsable=1 AND last_engagement_time_utc_millis<=? AND package_name";
        String valueOf2 = notInClause;
        return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf;
    }

    private ChannelConfigurationInfo getChannelConfigInfo(GoogleConfiguration googleConfiguration, OemConfiguration oemConfiguration, String packageName, String systemChannelKey) {
        ChannelConfigurationInfo channelConfig = null;
        if (googleConfiguration != null) {
            channelConfig = googleConfiguration.getChannelInfo(packageName, systemChannelKey);
        }
        if (channelConfig == null) {
            return oemConfiguration.getChannelInfo(packageName, systemChannelKey);
        }
        return channelConfig;
    }

    private boolean isChannelSponsored(String packageName, String systemChannelKey, GoogleConfiguration googleConfig, ChannelConfigurationInfo channelConfig) {
        return (googleConfig != null && googleConfig.isSponsored(packageName, systemChannelKey)) || (channelConfig != null && channelConfig.isSponsored());
    }

    private void startTrackingTask(DataLoadingBackgroundTask task) {
        if (task.getTag() == 0) {
            this.homeChannelsBackgroundTask = task;
        } else if (task.getTag() == 1) {
            this.homeChannelProgramsBackgroundTasks.put((Long) task.getExtraParam(), task);
        } else if (task.getTag() == 2) {
            this.packageChannelsBackgroundTask = task;
        } else if (task.getTag() == 3) {
            this.promoChannelBackgroundTask = task;
        } else if (task.getTag() == 4) {
            this.watchNextProgramsBackgroundTask = task;
        } else if (task.getTag() == 5) {
            this.watchNextCacheBackgroundTask = task;
        }
    }

    private void stopTrackingTask(DataLoadingBackgroundTask task) {
        if (task.getTag() == 0) {
            this.homeChannelsBackgroundTask = null;
        } else if (task.getTag() == 1) {
            this.homeChannelProgramsBackgroundTasks.remove((Long) task.getExtraParam());
        } else if (task.getTag() == 2) {
            this.packageChannelsBackgroundTask = null;
        } else if (task.getTag() == 3) {
            this.promoChannelBackgroundTask = null;
        } else if (task.getTag() == 4) {
            this.watchNextProgramsBackgroundTask = null;
        } else if (task.getTag() == 5) {
            this.watchNextCacheBackgroundTask = null;
        }
    }

    public void onTaskPostProcess(DataLoadingBackgroundTask task) {
        Throwable th;
        Throwable th2;
        Map<String, List<Channel>> packageChannels2;
        List<ChannelPackage> packagesWithChannels2;
        Throwable th3;
        List<HomeChannel> channels;
        long sponsoredGoogleChannelId;
        int sponsoredGoogleChannelOobPosition;
        List<HomeChannel> pinnedChannels;
        LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig;
        Throwable th4;
        DataLoadingBackgroundTask dataLoadingBackgroundTask = task;
        if (task.getTag() == 0) {
            long sponsoredGoogleChannelId2 = -1;
            int sponsoredGoogleChannelOobPosition2 = -1;
            List<HomeChannel> pinnedChannels2 = null;
            LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig2 = null;
            Cursor cursor = task.getResult();
            if (cursor != null) {
                try {
                    List<HomeChannel> channels2 = new ArrayList<>(cursor.getCount());
                    GoogleConfiguration googleConfiguration = this.googleConfigurationManager.getChannelConfigs();
                    OemConfiguration oemConfiguration = OemConfiguration.get(this.context);
                    while (cursor.moveToNext()) {
                        HomeChannel homeChannel = HomeChannel.fromCursor(cursor);
                        channels2.add(homeChannel);
                        ChannelConfigurationInfo channelConfig = getChannelConfigInfo(googleConfiguration, oemConfiguration, homeChannel.getPackageName(), homeChannel.getSystemChannelKey());
                        homeChannel.setSponsored(isChannelSponsored(homeChannel.getPackageName(), homeChannel.getSystemChannelKey(), googleConfiguration, channelConfig));
                        if (channelConfig != null) {
                            if (channelConfig.isSponsored() && channelConfig.isGoogleConfig()) {
                                sponsoredGoogleChannelId2 = homeChannel.getId();
                                sponsoredGoogleChannelOobPosition2 = channelConfig.getChannelPosition();
                            }
                            homeChannel.setCanMove(channelConfig.canMove());
                            homeChannel.setCanRemove(channelConfig.canHide());
                            if (!homeChannel.canMove() && !homeChannel.canRemove()) {
                                if (pinnedChannelsConfig2 == null) {
                                    pinnedChannelsConfig2 = new LongSparseArray<>();
                                    pinnedChannels2 = new ArrayList<>();
                                }
                                pinnedChannels2.add(homeChannel);
                                pinnedChannelsConfig2.put(homeChannel.getId(), channelConfig);
                            }
                        }
                    }
                    channels = channels2;
                    sponsoredGoogleChannelOobPosition = sponsoredGoogleChannelOobPosition2;
                    sponsoredGoogleChannelId = sponsoredGoogleChannelId2;
                } catch (Throwable th5) {
                    Throwable th6 = th5;
                    if (cursor != null) {
                        $closeResource(th4, cursor);
                    }
                    throw th6;
                }
            } else {
                List<HomeChannel> channels3 = Collections.emptyList();
                Log.e(TAG, "error loading home channels, cursor is null");
                channels = channels3;
                sponsoredGoogleChannelOobPosition = -1;
                sponsoredGoogleChannelId = -1;
            }
            if (cursor != null) {
                $closeResource(null, cursor);
            }
            if (pinnedChannels2 == null) {
                pinnedChannels = Collections.emptyList();
            } else {
                pinnedChannels = pinnedChannels2;
            }
            if (pinnedChannelsConfig2 == null) {
                pinnedChannelsConfig = EMPTY_PINNED_CHANNEL_CONFIG;
            } else {
                pinnedChannelsConfig = pinnedChannelsConfig2;
            }
            HomeChannelsBackgroundTaskResults homeChannelsBackgroundTaskResults = r1;
            HomeChannelsBackgroundTaskResults homeChannelsBackgroundTaskResults2 = new HomeChannelsBackgroundTaskResults(this, channels, sponsoredGoogleChannelId, sponsoredGoogleChannelOobPosition, pinnedChannels, pinnedChannelsConfig);
            dataLoadingBackgroundTask.setExtraResult(homeChannelsBackgroundTaskResults);
        } else if (task.getTag() == 1) {
            Cursor cursor2 = task.getResult();
            if (cursor2 != null) {
                while (cursor2.moveToNext()) {
                    this.programChannelIds.put(Long.valueOf(cursor2.getLong(0)), Long.valueOf(cursor2.getLong(1)));
                }
                return;
            }
            String valueOf = String.valueOf(task.getExtraParam());
            StringBuilder sb = new StringBuilder(valueOf.length() + 51);
            sb.append("error loading programs for channel ");
            sb.append(valueOf);
            sb.append(", cursor is null");
            Log.e(TAG, sb.toString());
        } else if (task.getTag() == 2) {
            Set<Long> packageChannelIds = new HashSet<>(100);
            Cursor cursor3 = task.getResult();
            if (cursor3 != null) {
                try {
                    packagesWithChannels2 = new ArrayList<>(cursor3.getCount());
                    packageChannels2 = new HashMap<>(cursor3.getCount());
                    GoogleConfiguration googleConfiguration2 = this.googleConfigurationManager.getChannelConfigs();
                    OemConfiguration oemConfiguration2 = OemConfiguration.get(this.context);
                    while (cursor3.moveToNext()) {
                        Channel channel = Channel.fromCursor(cursor3);
                        packageChannelIds.add(Long.valueOf(channel.getId()));
                        ChannelConfigurationInfo channelConfig2 = getChannelConfigInfo(googleConfiguration2, oemConfiguration2, channel.getPackageName(), channel.getSystemChannelKey());
                        channel.setSponsored(isChannelSponsored(channel.getPackageName(), channel.getSystemChannelKey(), googleConfiguration2, channelConfig2));
                        String packageName = channel.getPackageName();
                        if (channel.isSponsored()) {
                            packageName = Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME;
                        }
                        if (channelConfig2 != null) {
                            channel.setCanRemove(channelConfig2.canHide());
                        }
                        addToMultimap(packageChannels2, packageName, channel);
                    }
                    for (Map.Entry<String, List<Channel>> entry : packageChannels2.entrySet()) {
                        packagesWithChannels2.add(new ChannelPackage((String) entry.getKey(), ((List) entry.getValue()).size()));
                    }
                } catch (Throwable th7) {
                    Throwable th8 = th7;
                    if (cursor3 != null) {
                        $closeResource(th3, cursor3);
                    }
                    throw th8;
                }
            } else {
                packagesWithChannels2 = Collections.emptyList();
                Map<String, List<Channel>> packageChannels3 = Collections.emptyMap();
                Log.e(TAG, "error loading package channels, cursor is null");
                packageChannels2 = packageChannels3;
            }
            if (cursor3 != null) {
                $closeResource(null, cursor3);
            }
            Set<Long> nonEmptyChannelIds = getNonEmptyChannelIds(packageChannelIds);
            for (ChannelPackage channelPackage : packagesWithChannels2) {
                List<Channel> channels4 = packageChannels2.get(channelPackage.getPackageName());
                for (Channel channel2 : channels4) {
                    channel2.setIsEmpty(!nonEmptyChannelIds.contains(Long.valueOf(channel2.getId())));
                }
                if (channels4.size() == 1) {
                    channelPackage.setOnlyChannelAttributes((Channel) channels4.get(0));
                }
            }
            dataLoadingBackgroundTask.setExtraResult(new PackageChannelsBackgroundTaskResults(this, packagesWithChannels2, packageChannels2));
        } else if (task.getTag() == 3) {
            Channel channel3 = null;
            Cursor cursor4 = task.getResult();
            if (cursor4 != null) {
                try {
                    if (cursor4.moveToFirst()) {
                        channel3 = Channel.fromCursor(cursor4);
                    }
                } catch (Throwable th9) {
                    Throwable th10 = th9;
                    if (cursor4 != null) {
                        $closeResource(th2, cursor4);
                    }
                    throw th10;
                }
            } else {
                Log.e(TAG, "error loading promo channel, cursor is null");
            }
            if (cursor4 != null) {
                $closeResource(null, cursor4);
            }
            dataLoadingBackgroundTask.setExtraResult(channel3);
        } else if (task.getTag() == 5) {
            Cursor cursor5 = task.getResult();
            if (cursor5 != null) {
                try {
                    dataLoadingBackgroundTask.setExtraResult(extractWatchNextCache(cursor5));
                } catch (Throwable th11) {
                    Throwable th12 = th11;
                    if (cursor5 != null) {
                        $closeResource(th, cursor5);
                    }
                    throw th12;
                }
            } else {
                Log.e(TAG, "error loading watch next data into cache, cursor is null");
                dataLoadingBackgroundTask.setExtraResult(new HashSet());
            }
            if (cursor5 != null) {
                $closeResource(null, cursor5);
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, Cursor x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                ThrowableExtension.addSuppressed(x0, th);
            }
        } else {
            x1.close();
        }
    }

    private String buildCountProgramForChannelsSelection(List<Long> channelsToCount) {
        StringBuilder sb = new StringBuilder(channelsToCount.size() * 5);
        sb.append("channel_id IN (");
        for (Long programId : channelsToCount) {
            sb.append(programId.toString());
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public Set<Long> getNonEmptyChannelIds(Set<Long> channelIds) {
        int channelIdsCount = channelIds.size();
        Set<Long> nonEmptyChannelIds = new HashSet<>(channelIds.size());
        List<Long> channelIdsList = new ArrayList<>(channelIds);
        for (int i = 0; i < channelIdsCount; i += channelProgramCountBatchSize) {
            List<Long> subChannelIdList = channelIdsList.subList(i, Math.min(channelProgramCountBatchSize + i, channelIdsCount));
            String buildCountProgramForChannelsSelection = buildCountProgramForChannelsSelection(subChannelIdList);
            StringBuilder sb = new StringBuilder(buildCountProgramForChannelsSelection.length() + 38);
            sb.append("browsable=1 AND ");
            sb.append(buildCountProgramForChannelsSelection);
            sb.append(") GROUP BY (");
            sb.append("channel_id");
            Cursor cursor = this.context.getContentResolver().query(TvContract.PreviewPrograms.CONTENT_URI, new String[]{"channel_id"}, sb.toString(), null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        nonEmptyChannelIds.add(Long.valueOf(cursor.getLong(0)));
                    } catch (Throwable th) {
                        if (cursor != null) {
                            $closeResource(th, cursor);
                        }
                        throw th;
                    }
                }
            } else {
                int size = channelIds.size();
                StringBuilder sb2 = new StringBuilder(70);
                sb2.append("Program count failed for ");
                sb2.append(size);
                sb2.append(" channels. Returned cursor is null");
                Log.e(TAG, sb2.toString());
            }
            if (cursor != null) {
                $closeResource(null, cursor);
            }
        }
        return nonEmptyChannelIds;
    }

    /* access modifiers changed from: package-private */
    public void invalidateProgramsCountForBrowsableChannelsInternal(Set<Long> channelIds) {
        if (!channelIds.isEmpty()) {
            final Set<Long> channelIdsCopy = new HashSet<>(channelIds);
            new AsyncTask<Object, Void, Set<Long>>() {
                /* access modifiers changed from: protected */
                public /* bridge */ /* synthetic */ void onPostExecute(Object obj) {
                    onPostExecute((Set<Long>) ((Set) obj));
                }

                /* access modifiers changed from: protected */
                public Set<Long> doInBackground(Object... params) {
                    return TvDataManager.this.getNonEmptyChannelIds(channelIdsCopy);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Set<Long> nonEmptyChannelIds) {
                    boolean homeChannelsAdded = false;
                    Set<Long> emptyStatusChangedHomeChannelIds = new HashSet<>();
                    for (Long countedChannelId : channelIdsCopy) {
                        if (TvDataManager.this.browsableChannels.containsKey(countedChannelId)) {
                            if (TvDataManager.this.homeChannelIds.contains(countedChannelId)) {
                                boolean havePrograms = nonEmptyChannelIds.contains(countedChannelId);
                                if (havePrograms != TvDataManager.this.nonEmptyHomeChannelIds.contains(countedChannelId)) {
                                    if (havePrograms) {
                                        TvDataManager.this.nonEmptyHomeChannelIds.add(countedChannelId);
                                    } else {
                                        TvDataManager.this.nonEmptyHomeChannelIds.remove(countedChannelId);
                                    }
                                    emptyStatusChangedHomeChannelIds.add(countedChannelId);
                                }
                            } else if (nonEmptyChannelIds.contains(countedChannelId)) {
                                TvDataManager.this.homeChannels.add((HomeChannel) TvDataManager.this.browsableChannels.get(countedChannelId));
                                TvDataManager.this.homeChannelIds.add(countedChannelId);
                                TvDataManager.this.nonEmptyHomeChannelIds.add(countedChannelId);
                                homeChannelsAdded = true;
                                TvDataManager.this.channelOrderManager.onNewChannelAdded(countedChannelId.longValue());
                            }
                        }
                    }
                    if (homeChannelsAdded || emptyStatusChangedHomeChannelIds.size() > 0) {
                        TvDataManager.this.saveNonEmptyHomeChannelIdsToSharedPref();
                        if (homeChannelsAdded) {
                            TvDataManager.this.channelOrderManager.applyOrder(TvDataManager.this.homeChannels);
                        }
                        if (emptyStatusChangedHomeChannelIds.size() > 0) {
                            for (Long longValue : emptyStatusChangedHomeChannelIds) {
                                Integer position = TvDataManager.this.channelOrderManager.getChannelPosition(longValue.longValue());
                                if (position != null) {
                                    TvDataManager.this.notifyHomeChannelEmptyStatusChanged(position.intValue());
                                }
                            }
                        }
                        if (homeChannelsAdded) {
                            TvDataManager.this.notifyHomeChannelsChange();
                        }
                    }
                }
            }.executeOnExecutor(Executors.getThreadPoolExecutor());
        }
    }

    private boolean isFirstLaunchAfterBoot() {
        int bootCount;
        if (!this.isFirstLaunchAfterBoot || this.tvDataManagerPref.getInt(KEY_BOOT_COUNT, -1) >= (bootCount = getBootCount())) {
            return false;
        }
        this.tvDataManagerPref.edit().putInt(KEY_BOOT_COUNT, bootCount).apply();
        this.isFirstLaunchAfterBoot = false;
        return true;
    }

    private int getBootCount() {
        return Settings.Global.getInt(this.context.getContentResolver(), KEY_BOOT_COUNT, 0);
    }

    public void onTaskCompleted(DataLoadingBackgroundTask task) {
        boolean z = false;
        if (task.getTag() == 0) {
            boolean needToCountProgramsForAllBrowsableChannels = isHomeChannelDataStale();
            HomeChannelsBackgroundTaskResults results = (HomeChannelsBackgroundTaskResults) task.getExtraResult();
            List<HomeChannel> list = this.homeChannels;
            if (list == null) {
                this.homeChannels = new ArrayList();
            } else {
                list.clear();
            }
            HashSet<Long> newHomeChannelIds = new HashSet<>(this.homeChannelIds.size());
            boolean firstLaunchAfterBoot = isFirstLaunchAfterBoot();
            Map<Long, HomeChannel> newBrowsableChannels = new HashMap<>(results.channels.size());
            Set<Long> addedBrowsableChannelIds = new HashSet<>();
            for (HomeChannel channel : results.channels) {
                long channelId = channel.getId();
                if (this.liveTvChannelId == -1 && isLiveTvChannel(channel.getPackageName(), channel.getSystemChannelKey())) {
                    saveLiveTvChannelId(channelId);
                }
                if (!needToCountProgramsForAllBrowsableChannels && !this.browsableChannels.containsKey(Long.valueOf(channelId))) {
                    addedBrowsableChannelIds.add(Long.valueOf(channelId));
                }
                newBrowsableChannels.put(Long.valueOf(channelId), channel);
                if (this.homeChannelIds.contains(Long.valueOf(channelId)) && (!firstLaunchAfterBoot || !channel.isLegacy())) {
                    this.homeChannels.add(channel);
                    newHomeChannelIds.add(Long.valueOf(channelId));
                }
            }
            this.browsableChannels = newBrowsableChannels;
            this.homeChannelIds = newHomeChannelIds;
            this.nonEmptyHomeChannelIds.removeIf(new TvDataManager$$Lambda$1(this));
            saveNonEmptyHomeChannelIdsToSharedPref();
            this.homeChannelsStale = false;
            this.pinnedChannelOrderManager.setPinnedChannels(results.pinnedChannels, results.pinnedChannelsConfig);
            if (this.channelOrderManager == null) {
                Context context2 = this.context;
                this.channelOrderManager = new ChannelOrderManager(context2, OemConfiguration.get(context2).getOutOfBoxChannelsList(), OemConfiguration.get(this.context).getLiveTvChannelOobPosition());
                this.channelOrderManager.setChannelsObservers(this.homeChannelsObservers);
                this.channelOrderManager.setPinnedChannelOrderManager(this.pinnedChannelOrderManager);
            }
            this.channelOrderManager.setLiveTvChannelId(this.liveTvChannelId);
            this.channelOrderManager.setSponsoredGoogleChannelId(results.sponsoredGoogleChannelId);
            this.channelOrderManager.setSponsoredGoogleChannelOobPosition(results.sponsoredGoogleChannelOobPosition);
            this.channelOrderManager.applyOrder(this.homeChannels);
            notifyHomeChannelsChange();
            if (needToCountProgramsForAllBrowsableChannels) {
                invalidateProgramsCountForBrowsableChannelsInternal(this.browsableChannels.keySet());
            } else {
                invalidateProgramsCountForBrowsableChannelsInternal(addedBrowsableChannelIds);
            }
        } else if (task.getTag() == 1) {
            if (task.getResult() != null) {
                Long channelId2 = (Long) task.getExtraParam();
                cleanupAfterChannelProgramsRemovalFromCache(this.channelPrograms.remove(channelId2), channelId2);
                this.channelPrograms.put(channelId2, new ProgramsDataBuffer(task.getResult()));
                this.cachedProgramsChannelOrder.add(channelId2);
                notifyChannelProgramsChange(channelId2.longValue());
                if (task.getResult().getCount() != 0) {
                    z = true;
                }
                boolean havePrograms = z;
                if (this.nonEmptyHomeChannelIds.contains(channelId2) != havePrograms) {
                    if (havePrograms) {
                        this.nonEmptyHomeChannelIds.add(channelId2);
                    } else {
                        this.nonEmptyHomeChannelIds.remove(channelId2);
                    }
                    saveNonEmptyHomeChannelIdsToSharedPref();
                    Integer position = this.channelOrderManager.getChannelPosition(channelId2.longValue());
                    if (position != null) {
                        notifyHomeChannelEmptyStatusChanged(position.intValue());
                    }
                }
            }
        } else if (task.getTag() == 2) {
            PackageChannelsBackgroundTaskResults results2 = (PackageChannelsBackgroundTaskResults) task.getExtraResult();
            this.packagesWithChannels = results2.packagesWithChannels;
            this.packageChannels = results2.packageChannels;
            for (Map.Entry<String, List<Channel>> entry : this.packageChannels.entrySet()) {
                for (Channel channel2 : (List) entry.getValue()) {
                    if (this.liveTvChannelId == -1 && isLiveTvChannel(channel2.getPackageName(), channel2.getSystemChannelKey())) {
                        saveLiveTvChannelId(channel2.getId());
                    }
                }
            }
            notifyPackagesWithChannelsChange();
            notifyPackageChannelsChange();
        } else if (task.getTag() == 3) {
            setPromoChannelAndNotify((Channel) task.getExtraResult());
        } else if (task.getTag() == 4) {
            if (task.getResult() != null) {
                WatchNextProgramsDataBuffer watchNextProgramsDataBuffer = this.watchNextPrograms;
                if (watchNextProgramsDataBuffer != null) {
                    watchNextProgramsDataBuffer.release();
                }
                this.watchNextPrograms = new WatchNextProgramsDataBuffer(task.getResult());
                this.watchNextProgramsStale = false;
                notifyWatchNextProgramsChange();
            } else {
                Log.e(TAG, "error loading watch next programs, cursor is null");
            }
        } else if (task.getTag() == 5) {
            this.watchNextContentIdsCache = (HashSet) task.getExtraResult();
        }
        stopTrackingTask(task);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ boolean lambda$onTaskCompleted$1$TvDataManager(Long channelId) {
        return !this.homeChannelIds.contains(channelId);
    }

    private void setPromoChannelAndNotify(Channel promoChannel2) {
        this.promoChannel = promoChannel2;
        this.promoChannelLoaded = true;
        notifyPromoChannelChange();
    }

    public void onTaskCanceled(DataLoadingBackgroundTask task) {
        stopTrackingTask(task);
    }

    public void onTaskFailed(DataLoadingBackgroundTask task, Throwable throwable) {
        String valueOf = String.valueOf(task);
        String valueOf2 = String.valueOf(throwable);
        StringBuilder sb = new StringBuilder(valueOf.length() + 27 + valueOf2.length());
        sb.append("onTaskFailed: ");
        sb.append(valueOf);
        sb.append(", throwable: ");
        sb.append(valueOf2);
        Log.e(TAG, sb.toString());
        stopTrackingTask(task);
    }

    private boolean isLiveTvChannel(String packageName, String systemChannelKey) {
        ChannelConfigurationInfo liveChannelConfigurationInfo = OemConfiguration.get(this.context).getLiveTvOobPackageInfo();
        if (liveChannelConfigurationInfo == null || !liveChannelConfigurationInfo.getPackageName().equals(packageName)) {
            return false;
        }
        return TextUtils.isEmpty(liveChannelConfigurationInfo.getSystemChannelKey()) || TextUtils.equals(liveChannelConfigurationInfo.getSystemChannelKey(), systemChannelKey);
    }

    private void saveLiveTvChannelId(long liveChannelId) {
        this.liveTvChannelId = liveChannelId;
        this.liveTvChannelPref.edit().putLong(KEY_LIVE_TV_CHANNEL_ID, this.liveTvChannelId).apply();
    }

    public void setChannelBrowsable(long channelId, boolean browsable, boolean reloadImmediately) {
        ChannelOrderManager channelOrderManager2 = this.channelOrderManager;
        if (channelOrderManager2 != null) {
            if (!browsable) {
                channelOrderManager2.onChannelRemoved(channelId);
            }
            this.channelOrderManager.notifyUserHasManagedChannels();
        }
        final boolean z = browsable;
        final long j = channelId;
        final boolean z2 = reloadImmediately;
        new AsyncTask<Object, Void, Integer>() {
            /* access modifiers changed from: protected */
            public Integer doInBackground(Object... params) {
                ContentValues values = new ContentValues();
                values.put("browsable", Boolean.valueOf(z));
                return Integer.valueOf(TvDataManager.this.context.getContentResolver().update(TvContract.buildChannelUri(j), values, null, null));
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer numRow) {
                if (numRow.intValue() == 1 && z2) {
                    TvDataManager.this.loadHomeChannelDataInternal();
                }
            }
        }.executeOnExecutor(Executors.getThreadPoolExecutor(), Boolean.valueOf(browsable), Long.valueOf(channelId));
    }

    public void removePreviewProgram(long programId, long channelId, String packageName) {
        final long j = programId;
        final String str = packageName;
        final long j2 = channelId;
        new AsyncTask<Object, Void, Integer>() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Byte):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Float):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.String):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Long):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, byte[]):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Double):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Short):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void} */
            /* access modifiers changed from: protected */
            public Integer doInBackground(Object... params) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("browsable", (Boolean) false);
                int numRowAffected = TvDataManager.this.context.getContentResolver().update(TvContract.buildPreviewProgramUri(j), contentValues, null, null);
                if (numRowAffected == 1) {
                    ((TvInputManager) TvDataManager.this.context.getSystemService("tv_input")).notifyPreviewProgramBrowsableDisabled(str, j);
                }
                return Integer.valueOf(numRowAffected);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer numRowAffected) {
                if (numRowAffected.intValue() == 1) {
                    TvDataManager.this.loadHomeChannelProgramData(j2);
                }
            }
        }.executeOnExecutor(Executors.getThreadPoolExecutor());
    }

    public void removeProgramFromWatchlist(final long programId, final String packageName) {
        new AsyncTask<Object, Void, Integer>() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Byte):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Float):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.String):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Long):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, byte[]):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Double):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Short):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void} */
            /* access modifiers changed from: protected */
            public Integer doInBackground(Object... params) {
                Uri watchNextProgramUri = TvContract.buildWatchNextProgramUri(programId);
                ContentValues contentValues = new ContentValues();
                contentValues.put("browsable", (Boolean) false);
                int numRowAffected = TvDataManager.this.context.getContentResolver().update(watchNextProgramUri, contentValues, null, null);
                if (numRowAffected == 1) {
                    ((TvInputManager) TvDataManager.this.context.getSystemService("tv_input")).notifyWatchNextProgramBrowsableDisabled(packageName, programId);
                }
                return Integer.valueOf(numRowAffected);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer numRowAffected) {
                if (numRowAffected.intValue() == 1) {
                    TvDataManager.this.loadWatchNextProgramDataInternal();
                }
            }
        }.executeOnExecutor(Executors.getThreadPoolExecutor());
    }

    public void addProgramToWatchlist(final long programId, final String packageName) {
        new AsyncTask<Object, Void, Boolean>() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Byte):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Float):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.String):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Long):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, byte[]):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Double):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Short):void}
              ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void} */
            /* access modifiers changed from: protected */
            /* JADX WARNING: Removed duplicated region for block: B:10:0x0076 A[Catch:{ all -> 0x0431, all -> 0x006f }] */
            /* JADX WARNING: Removed duplicated region for block: B:40:0x0436 A[Catch:{ all -> 0x0431, all -> 0x006f }] */
            /* JADX WARNING: Removed duplicated region for block: B:43:0x043e  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public java.lang.Boolean doInBackground(java.lang.Object... r31) {
                /*
                    r30 = this;
                    r1 = r30
                    java.lang.String r0 = "searchable"
                    java.lang.String r2 = "thumbnail_uri"
                    java.lang.String r3 = "poster_art_uri"
                    java.lang.String r4 = "content_rating"
                    java.lang.String r5 = "audio_language"
                    java.lang.String r6 = "video_height"
                    java.lang.String r7 = "video_width"
                    java.lang.String r8 = "long_description"
                    java.lang.String r9 = "short_description"
                    java.lang.String r10 = "canonical_genre"
                    java.lang.String r11 = "episode_title"
                    java.lang.String r12 = "episode_display_number"
                    java.lang.String r13 = "season_title"
                    java.lang.String r14 = "season_display_number"
                    java.lang.String r15 = "title"
                    r16 = r0
                    java.lang.String r0 = "preview_audio_uri"
                    r17 = r0
                    java.lang.String r0 = "end_time_utc_millis"
                    r18 = r0
                    java.lang.String r0 = "start_time_utc_millis"
                    r19 = r0
                    java.lang.String r0 = "genre"
                    r20 = r0
                    java.lang.String r0 = "logo_content_description"
                    r21 = r0
                    java.lang.String r0 = "tv_series_item_type"
                    r22 = r2
                    com.google.android.tvlauncher.data.TvDataManager r2 = com.google.android.tvlauncher.data.TvDataManager.this
                    android.content.Context r2 = r2.context
                    android.content.ContentResolver r23 = r2.getContentResolver()
                    r29 = r3
                    long r2 = r4
                    android.net.Uri r24 = android.media.tv.TvContract.buildPreviewProgramUri(r2)
                    r25 = 0
                    r26 = 0
                    r27 = 0
                    r28 = 0
                    android.database.Cursor r2 = r23.query(r24, r25, r26, r27, r28)
                    if (r2 == 0) goto L_0x0073
                    boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x006f }
                    if (r3 == 0) goto L_0x0073
                    r3 = 1
                    goto L_0x0074
                L_0x006f:
                    r0 = move-exception
                L_0x0070:
                    r3 = r0
                    goto L_0x0442
                L_0x0073:
                    r3 = 0
                L_0x0074:
                    if (r3 == 0) goto L_0x0436
                    android.content.ContentValues r23 = new android.content.ContentValues     // Catch:{ all -> 0x006f }
                    r23.<init>()     // Catch:{ all -> 0x006f }
                    r24 = r23
                    r23 = r3
                    java.lang.String r3 = "package_name"
                    r25 = r4
                    java.lang.String r4 = r6     // Catch:{ all -> 0x006f }
                    r1 = r24
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "watch_next_type"
                    r4 = 3
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "last_engagement_time_utc_millis"
                    long r26 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0431 }
                    java.lang.Long r4 = java.lang.Long.valueOf(r26)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getColumnIndex(r15)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r15, r3)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getColumnIndex(r14)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r14, r3)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    r4 = -1
                    if (r3 == r4) goto L_0x00d1
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getInt(r3)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r0, r3)     // Catch:{ all -> 0x0431 }
                L_0x00d1:
                    int r0 = r2.getColumnIndex(r13)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r13, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r12)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r12, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r11)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r11, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r10)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r10, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r9)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r9, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r8)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r8, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r7)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getInt(r0)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r7, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r6)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getInt(r0)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r6, r0)     // Catch:{ all -> 0x0431 }
                    int r0 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r5, r0)     // Catch:{ all -> 0x0431 }
                    r0 = r25
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r0, r3)     // Catch:{ all -> 0x0431 }
                    r0 = r29
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r0, r3)     // Catch:{ all -> 0x0431 }
                    r0 = r22
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r0, r3)     // Catch:{ all -> 0x0431 }
                    r0 = r16
                    int r3 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getInt(r3)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0431 }
                    r1.put(r0, r3)     // Catch:{ all -> 0x0431 }
                    java.lang.String r0 = "internal_provider_data"
                    int r0 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0431 }
                    int r3 = r2.getType(r0)     // Catch:{ all -> 0x0431 }
                    r5 = 4
                    if (r3 != r5) goto L_0x0197
                    java.lang.String r3 = "internal_provider_data"
                    byte[] r5 = r2.getBlob(r0)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                L_0x0197:
                    java.lang.String r3 = "internal_provider_flag1"
                    java.lang.String r5 = "internal_provider_flag1"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "internal_provider_flag2"
                    java.lang.String r5 = "internal_provider_flag2"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "internal_provider_flag3"
                    java.lang.String r5 = "internal_provider_flag3"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "internal_provider_flag4"
                    java.lang.String r5 = "internal_provider_flag4"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "version_number"
                    java.lang.String r5 = "version_number"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "review_rating_style"
                    java.lang.String r5 = "review_rating_style"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "review_rating"
                    java.lang.String r5 = "review_rating"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "type"
                    java.lang.String r5 = "type"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "poster_art_aspect_ratio"
                    java.lang.String r5 = "poster_art_aspect_ratio"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "poster_thumbnail_aspect_ratio"
                    java.lang.String r5 = "poster_thumbnail_aspect_ratio"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "logo_uri"
                    java.lang.String r5 = "logo_uri"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    r3 = r21
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    if (r5 == r4) goto L_0x0280
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                L_0x0280:
                    r3 = r20
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    if (r5 == r4) goto L_0x0294
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                L_0x0294:
                    r3 = r19
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    if (r5 == r4) goto L_0x02ac
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    long r5 = r2.getLong(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Long r5 = java.lang.Long.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                L_0x02ac:
                    r3 = r18
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    if (r5 == r4) goto L_0x02c4
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    long r5 = r2.getLong(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Long r5 = java.lang.Long.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                L_0x02c4:
                    java.lang.String r3 = "availability"
                    java.lang.String r5 = "availability"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "starting_price"
                    java.lang.String r5 = "starting_price"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "offer_price"
                    java.lang.String r5 = "offer_price"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "release_date"
                    java.lang.String r5 = "release_date"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "item_count"
                    java.lang.String r5 = "item_count"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "live"
                    java.lang.String r5 = "live"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    int r5 = r2.getInt(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "internal_provider_id"
                    java.lang.String r5 = "internal_provider_id"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "preview_video_uri"
                    java.lang.String r5 = "preview_video_uri"
                    int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x0431 }
                    java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r5)     // Catch:{ all -> 0x0431 }
                    r3 = r17
                    int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    if (r5 == r4) goto L_0x0362
                    int r4 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0431 }
                    java.lang.String r4 = r2.getString(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                L_0x0362:
                    java.lang.String r3 = "last_playback_position_millis"
                    java.lang.String r4 = "last_playback_position_millis"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    int r4 = r2.getInt(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "duration_millis"
                    java.lang.String r4 = "duration_millis"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    int r4 = r2.getInt(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "intent_uri"
                    java.lang.String r4 = "intent_uri"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r4 = r2.getString(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "transient"
                    java.lang.String r4 = "transient"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    int r4 = r2.getInt(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "interaction_type"
                    java.lang.String r4 = "interaction_type"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    int r4 = r2.getInt(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "interaction_count"
                    java.lang.String r4 = "interaction_count"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    long r4 = r2.getLong(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Long r4 = java.lang.Long.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "author"
                    java.lang.String r4 = "author"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r4 = r2.getString(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "browsable"
                    java.lang.String r4 = "browsable"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    int r4 = r2.getInt(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r3 = "content_id"
                    java.lang.String r4 = "content_id"
                    int r4 = r2.getColumnIndex(r4)     // Catch:{ all -> 0x0431 }
                    java.lang.String r4 = r2.getString(r4)     // Catch:{ all -> 0x0431 }
                    r1.put(r3, r4)     // Catch:{ all -> 0x0431 }
                    r3 = r1
                    r1 = r30
                    com.google.android.tvlauncher.data.TvDataManager r4 = com.google.android.tvlauncher.data.TvDataManager.this     // Catch:{ all -> 0x006f }
                    android.content.Context r4 = r4.context     // Catch:{ all -> 0x006f }
                    android.content.ContentResolver r4 = r4.getContentResolver()     // Catch:{ all -> 0x006f }
                    android.net.Uri r5 = android.media.tv.TvContract.WatchNextPrograms.CONTENT_URI     // Catch:{ all -> 0x006f }
                    android.net.Uri r4 = r4.insert(r5, r3)     // Catch:{ all -> 0x006f }
                    long r9 = android.content.ContentUris.parseId(r4)     // Catch:{ all -> 0x006f }
                    com.google.android.tvlauncher.data.TvDataManager r5 = com.google.android.tvlauncher.data.TvDataManager.this     // Catch:{ all -> 0x006f }
                    android.content.Context r5 = r5.context     // Catch:{ all -> 0x006f }
                    java.lang.String r6 = "tv_input"
                    java.lang.Object r5 = r5.getSystemService(r6)     // Catch:{ all -> 0x006f }
                    android.media.tv.TvInputManager r5 = (android.media.tv.TvInputManager) r5     // Catch:{ all -> 0x006f }
                    java.lang.String r6 = r6     // Catch:{ all -> 0x006f }
                    long r7 = r4     // Catch:{ all -> 0x006f }
                    r5.notifyPreviewProgramAddedToWatchNext(r6, r7, r9)     // Catch:{ all -> 0x006f }
                    goto L_0x0438
                L_0x0431:
                    r0 = move-exception
                    r1 = r30
                    goto L_0x0070
                L_0x0436:
                    r23 = r3
                L_0x0438:
                    java.lang.Boolean r0 = java.lang.Boolean.valueOf(r23)     // Catch:{ all -> 0x006f }
                    if (r2 == 0) goto L_0x0441
                    r2.close()
                L_0x0441:
                    return r0
                L_0x0442:
                    throw r3     // Catch:{ all -> 0x0443 }
                L_0x0443:
                    r0 = move-exception
                    r4 = r0
                    if (r2 == 0) goto L_0x0450
                    r2.close()     // Catch:{ all -> 0x044b }
                    goto L_0x0450
                L_0x044b:
                    r0 = move-exception
                    r5 = r0
                    com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r3, r5)
                L_0x0450:
                    goto L_0x0452
                L_0x0451:
                    throw r4
                L_0x0452:
                    goto L_0x0451
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.data.TvDataManager.C12336.doInBackground(java.lang.Object[]):java.lang.Boolean");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean hasProgram) {
                if (hasProgram.booleanValue()) {
                    TvDataManager.this.loadWatchNextProgramDataInternal();
                }
            }
        }.executeOnExecutor(Executors.getThreadPoolExecutor());
    }

    public ChannelOrderManager getChannelOrderManager() {
        if (isHomeChannelDataLoaded()) {
            return this.channelOrderManager;
        }
        throw new IllegalStateException("Home channel data not loaded yet");
    }

    private class DataSourceObserverCallbacks implements DataSourceObserver.Callbacks {
        private DataSourceObserverCallbacks() {
        }

        public void invalidateAllChannels() {
            if (TvDataManager.this.homeChannelsObservers.size() > 0) {
                TvDataManager.this.loadHomeChannelDataInternal();
            } else {
                boolean unused = TvDataManager.this.homeChannelsStale = true;
            }
            TvDataManager.this.loadPackageChannelsDataIfNeeded();
            if (TvDataManager.this.promoChannelObservers.size() > 0 && OemConfiguration.get(TvDataManager.this.context).getAppsPromotionRowPackage() != null) {
                TvDataManager.this.loadPromoChannelData();
            }
        }

        public void invalidateChannelLogo(long channelId) {
            TvDataManager.this.channelLogoUris.remove(Long.valueOf(channelId));
        }

        public void invalidatePackageChannels() {
            TvDataManager.this.loadPackageChannelsDataIfNeeded();
        }

        private void reloadProgramsOrMarkStale(Long channelId) {
            if (TvDataManager.this.channelProgramsObservers.containsKey(channelId) || TvDataManager.this.areChannelProgramsAlwaysCached(channelId)) {
                TvDataManager.this.loadHomeChannelProgramData(channelId.longValue());
            } else {
                TvDataManager.this.staleProgramsChannels.add(channelId);
            }
        }

        public void invalidateAllPrograms() {
            for (Long channelId : TvDataManager.this.channelPrograms.keySet()) {
                reloadProgramsOrMarkStale(channelId);
            }
            Set<Long> nonObservedBrowsableChannelIds = new HashSet<>(TvDataManager.this.browsableChannels.keySet());
            nonObservedBrowsableChannelIds.removeAll(TvDataManager.this.channelProgramsObservers.keySet());
            TvDataManager.this.invalidateProgramsCountForBrowsableChannelsInternal(nonObservedBrowsableChannelIds);
        }

        public void invalidateProgramsCountForBrowsableChannels(Set<Long> channelIds) {
            TvDataManager.this.invalidateProgramsCountForBrowsableChannelsInternal(channelIds);
        }

        public void invalidateHomeChannelPrograms(long channelId) {
            if (TvDataManager.this.channelPrograms.containsKey(Long.valueOf(channelId))) {
                reloadProgramsOrMarkStale(Long.valueOf(channelId));
            }
        }

        public void invalidateWatchNextPrograms() {
            if (TvDataManager.this.watchNextProgramsObservers.size() > 0) {
                TvDataManager.this.loadWatchNextProgramDataInternal();
                TvDataManager.this.loadAllWatchNextProgramDataIntoCache();
                return;
            }
            boolean unused = TvDataManager.this.watchNextProgramsStale = true;
        }

        public boolean isChannelBrowsable(long channelId) {
            return TvDataManager.this.browsableChannels.containsKey(Long.valueOf(channelId));
        }

        public Long findCachedChannelId(long programId) {
            return (Long) TvDataManager.this.programChannelIds.get(Long.valueOf(programId));
        }

        public boolean areProgramsCached(long channelId) {
            return TvDataManager.this.channelPrograms.containsKey(Long.valueOf(channelId));
        }

        public Set<Long> getAllHomeChannelIds() {
            return TvDataManager.this.homeChannelIds;
        }
    }

    private class HomeChannelsBackgroundTaskResults {
        final List<HomeChannel> channels;
        final List<HomeChannel> pinnedChannels;
        final LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig;
        final long sponsoredGoogleChannelId;
        final int sponsoredGoogleChannelOobPosition;

        HomeChannelsBackgroundTaskResults(TvDataManager tvDataManager, List<HomeChannel> channels2, long sponsoredGoogleChannelId2, int sponsoredGoogleChannelOobPosition2, List<HomeChannel> pinnedChannels2, LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig2) {
            this.channels = channels2;
            this.sponsoredGoogleChannelId = sponsoredGoogleChannelId2;
            this.sponsoredGoogleChannelOobPosition = sponsoredGoogleChannelOobPosition2;
            this.pinnedChannels = pinnedChannels2;
            this.pinnedChannelsConfig = pinnedChannelsConfig2;
        }
    }

    private class PackageChannelsBackgroundTaskResults {
        final Map<String, List<Channel>> packageChannels;
        final List<ChannelPackage> packagesWithChannels;

        PackageChannelsBackgroundTaskResults(TvDataManager tvDataManager, List<ChannelPackage> packagesWithChannels2, Map<String, List<Channel>> packageChannels2) {
            this.packagesWithChannels = packagesWithChannels2;
            this.packageChannels = packageChannels2;
        }
    }
}
