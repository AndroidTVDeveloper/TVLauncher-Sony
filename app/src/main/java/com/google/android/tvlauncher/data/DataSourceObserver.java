package com.google.android.tvlauncher.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.tvlauncher.data.DataLoadingBackgroundTask;
import com.google.android.tvlauncher.util.ExtendableTimer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DataSourceObserver {
    private static final int ALL_CHANNELS_INVALIDATION_DELAY_MS = 3000;
    private static final int ALL_CHANNELS_INVALIDATION_MAXIMUM_DELAY_MS = 10000;
    private static final int ALL_CHANNELS_INVALIDATION_TIMER_ID = -1000;
    private static final int ALL_PROGRAMS_INVALIDATION_DELAY_MS = 3000;
    private static final int ALL_PROGRAMS_INVALIDATION_MAXIMUM_DELAY_MS = 15000;
    private static final int ALL_PROGRAMS_INVALIDATION_TIMER_ID = -2000;
    private static final int CHANNEL_ID_COLUMN_INDEX = 1;
    private static final boolean DEBUG = false;
    private static final int HOME_CHANNEL_PROGRAMS_INVALIDATION_DELAY_MS = 3000;
    private static final int HOME_CHANNEL_PROGRAMS_INVALIDATION_MAXIMUM_DELAY_MS = 10000;
    private static final int ID_COLUMN_INDEX = 0;
    private static final int MATCH_CHANNEL = 1;
    private static final int MATCH_CHANNEL_ID = 2;
    private static final int MATCH_CHANNEL_ID_LOGO = 3;
    private static final int MATCH_PROGRAM = 4;
    private static final int MATCH_PROGRAM_ID = 5;
    private static final int MATCH_WATCH_NEXT_PROGRAM = 6;
    private static final int MATCH_WATCH_NEXT_PROGRAM_ID = 7;
    private static final int PACKAGE_CHANNELS_INVALIDATION_DELAY_MS = 3000;
    private static final int PACKAGE_CHANNELS_INVALIDATION_MAXIMUM_DELAY_MS = 10000;
    private static final int PACKAGE_CHANNELS_INVALIDATION_TIMER_ID = -5000;
    private static final int PROGRAMS_COUNT_DELAY_MS = 3000;
    private static final int PROGRAMS_COUNT_MAXIMUM_DELAY_MS = 10000;
    private static final int PROGRAMS_COUNT_TIMER_ID = -6000;
    private static final int PROGRAMS_DATA_LOAD_BATCH = 100;
    private static final int PROGRAMS_DATA_LOAD_DELAY_MS = 1000;
    private static final int PROGRAMS_DATA_LOAD_MAXIMUM_DELAY_MS = 5000;
    private static final int PROGRAMS_DATA_LOAD_TIMER_ID = -4000;
    private static final String[] PROJECTION = {"_id", "channel_id"};
    private static final String TAG = "DataSourceObserver";
    private static final int WATCH_NEXT_INVALIDATION_TIMER_ID = -3000;
    private static final int WATCH_NEXT_PROGRAMS_INVALIDATION_DELAY_MS = 3000;
    private static final int WATCH_NEXT_PROGRAMS_INVALIDATION_MAXIMUM_DELAY_MS = 10000;
    /* access modifiers changed from: private */
    public static UriMatcher uriMatcher = new UriMatcher(-1);
    private ExtendableTimer allChannelsInvalidationTimer;
    /* access modifiers changed from: private */
    public ExtendableTimer allProgramsInvalidationTimer;
    private BackgroundTaskCallbacks backgroundTaskCallbacks;
    /* access modifiers changed from: private */
    public Callbacks callbacks;
    /* access modifiers changed from: private */
    public Set<Long> channelsToCountPrograms = new HashSet(100);
    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        /* JADX INFO: Multiple debug info for r0v3 java.util.List<java.lang.String>: [D('channelIdParam' java.lang.String), D('pathSegments' java.util.List<java.lang.String>)] */
        public void onChange(boolean selfChange, Uri uri) {
            switch (DataSourceObserver.uriMatcher.match(uri)) {
                case 1:
                case 2:
                    DataSourceObserver.this.invalidateAllChannels();
                    return;
                case 3:
                    List<String> pathSegments = uri.getPathSegments();
                    if (pathSegments.size() >= 2) {
                        try {
                            DataSourceObserver.this.invalidateChannelLogo(Long.parseLong(pathSegments.get(1)));
                            DataSourceObserver.this.invalidateAllChannels();
                            return;
                        } catch (NumberFormatException e) {
                            String valueOf = String.valueOf(uri);
                            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 27);
                            sb.append("Invalid channel ID in URI: ");
                            sb.append(valueOf);
                            Log.e(DataSourceObserver.TAG, sb.toString());
                            return;
                        }
                    } else {
                        String valueOf2 = String.valueOf(uri);
                        StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 26);
                        sb2.append("Invalid channel logo URI: ");
                        sb2.append(valueOf2);
                        Log.e(DataSourceObserver.TAG, sb2.toString());
                        return;
                    }
                case 4:
                    String channelIdParam = uri.getQueryParameter("channel");
                    if (channelIdParam != null) {
                        try {
                            long channelId = Long.parseLong(channelIdParam);
                            DataSourceObserver.this.invalidateHomeChannelProgramsIfCached(channelId);
                            if (DataSourceObserver.this.callbacks.isChannelBrowsable(channelId)) {
                                DataSourceObserver.this.scheduleProgramsCount(channelId);
                            }
                            DataSourceObserver.this.invalidatePackageChannels();
                            return;
                        } catch (NumberFormatException e2) {
                            String valueOf3 = String.valueOf(uri);
                            StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length() + 27);
                            sb3.append("Invalid channel ID in URI: ");
                            sb3.append(valueOf3);
                            Log.e(DataSourceObserver.TAG, sb3.toString());
                            return;
                        }
                    } else {
                        DataSourceObserver.this.invalidateAllPrograms();
                        DataSourceObserver.this.invalidatePackageChannels();
                        return;
                    }
                case 5:
                    try {
                        DataSourceObserver.this.invalidateProgram(Long.parseLong(uri.getLastPathSegment()));
                        DataSourceObserver.this.invalidatePackageChannels();
                        return;
                    } catch (NumberFormatException e3) {
                        String valueOf4 = String.valueOf(uri);
                        StringBuilder sb4 = new StringBuilder(String.valueOf(valueOf4).length() + 27);
                        sb4.append("Invalid program ID in URI: ");
                        sb4.append(valueOf4);
                        Log.e(DataSourceObserver.TAG, sb4.toString());
                        return;
                    }
                case 6:
                case 7:
                    DataSourceObserver.this.invalidateWatchNextPrograms();
                    return;
                default:
                    return;
            }
        }
    };
    private final Context context;
    private ExtendableTimerListener extendableTimerListener;
    /* access modifiers changed from: private */
    public Map<Long, ExtendableTimer> homeChannelProgramsInvalidationTimers = new HashMap(10);
    private ExtendableTimer packageChannelsInvalidationTimer;
    /* access modifiers changed from: private */
    public ExtendableTimer programsCountTimer;
    private ExtendableTimer programsDataLoadTimer;
    private Set<Long> programsToLoadData = new HashSet(100);
    private boolean registered;
    private ExtendableTimer watchNextInvalidationTimer;

    interface Callbacks {
        boolean areProgramsCached(long j);

        Long findCachedChannelId(long j);

        Set<Long> getAllHomeChannelIds();

        void invalidateAllChannels();

        void invalidateAllPrograms();

        void invalidateChannelLogo(long j);

        void invalidateHomeChannelPrograms(long j);

        void invalidatePackageChannels();

        void invalidateProgramsCountForBrowsableChannels(Set<Long> set);

        void invalidateWatchNextPrograms();

        boolean isChannelBrowsable(long j);
    }

    static {
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "channel", 1);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "channel/#", 2);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "channel/#/logo", 3);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "preview_program", 4);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "preview_program/#", 5);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "watch_next_program", 6);
        uriMatcher.addURI(TvContractCompat.AUTHORITY, "watch_next_program/#", 7);
    }

    DataSourceObserver(Context context2, Callbacks callbacks2) {
        this.context = context2.getApplicationContext();
        this.callbacks = callbacks2;
    }

    /* access modifiers changed from: package-private */
    public ContentObserver getContentObserver() {
        return this.contentObserver;
    }

    /* access modifiers changed from: package-private */
    public Callbacks getCallbacks() {
        return this.callbacks;
    }

    /* access modifiers changed from: package-private */
    public void register() {
        if (!this.registered) {
            ContentResolver resolver = this.context.getContentResolver();
            resolver.registerContentObserver(TvContract.Channels.CONTENT_URI, true, this.contentObserver);
            resolver.registerContentObserver(TvContract.PreviewPrograms.CONTENT_URI, true, this.contentObserver);
            resolver.registerContentObserver(TvContract.WatchNextPrograms.CONTENT_URI, true, this.contentObserver);
            this.registered = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void unregister() {
        if (this.registered) {
            this.context.getContentResolver().unregisterContentObserver(this.contentObserver);
            this.registered = false;
        }
    }

    private ExtendableTimerListener getTimerListener() {
        if (this.extendableTimerListener == null) {
            this.extendableTimerListener = new ExtendableTimerListener();
        }
        return this.extendableTimerListener;
    }

    /* access modifiers changed from: private */
    public void invalidateAllChannels() {
        if (this.allChannelsInvalidationTimer == null) {
            this.allChannelsInvalidationTimer = new ExtendableTimer();
            this.allChannelsInvalidationTimer.setTimeout(3000);
            this.allChannelsInvalidationTimer.setMaximumTimeout(10000);
            this.allChannelsInvalidationTimer.setId(-1000);
            this.allChannelsInvalidationTimer.setListener(getTimerListener());
        }
        this.allChannelsInvalidationTimer.start();
    }

    /* access modifiers changed from: private */
    public void invalidatePackageChannels() {
        if (this.packageChannelsInvalidationTimer == null) {
            this.packageChannelsInvalidationTimer = new ExtendableTimer();
            this.packageChannelsInvalidationTimer.setTimeout(3000);
            this.packageChannelsInvalidationTimer.setMaximumTimeout(10000);
            this.packageChannelsInvalidationTimer.setId(-5000);
            this.packageChannelsInvalidationTimer.setListener(getTimerListener());
        }
        this.packageChannelsInvalidationTimer.start();
    }

    /* access modifiers changed from: private */
    public void invalidateChannelLogo(long channelId) {
        this.callbacks.invalidateChannelLogo(channelId);
    }

    /* access modifiers changed from: private */
    public void invalidateWatchNextPrograms() {
        if (this.watchNextInvalidationTimer == null) {
            this.watchNextInvalidationTimer = ExtendableTimer.obtain();
            this.watchNextInvalidationTimer.setTimeout(3000);
            this.watchNextInvalidationTimer.setMaximumTimeout(10000);
            this.watchNextInvalidationTimer.setId(-3000);
            this.watchNextInvalidationTimer.setListener(getTimerListener());
        }
        this.watchNextInvalidationTimer.start();
    }

    /* access modifiers changed from: private */
    public void invalidateAllPrograms() {
        if (this.allProgramsInvalidationTimer == null) {
            this.allProgramsInvalidationTimer = new ExtendableTimer();
            this.allProgramsInvalidationTimer.setTimeout(3000);
            this.allProgramsInvalidationTimer.setMaximumTimeout(15000);
            this.allProgramsInvalidationTimer.setId(-2000);
            this.allProgramsInvalidationTimer.setListener(getTimerListener());
        }
        ExtendableTimer extendableTimer = this.programsDataLoadTimer;
        if (extendableTimer != null && extendableTimer.isStarted()) {
            this.programsDataLoadTimer.cancel();
        }
        this.programsToLoadData.clear();
        ExtendableTimer extendableTimer2 = this.programsCountTimer;
        if (extendableTimer2 != null && extendableTimer2.isStarted()) {
            this.programsCountTimer.cancel();
        }
        this.channelsToCountPrograms.clear();
        for (ExtendableTimer timer : this.homeChannelProgramsInvalidationTimers.values()) {
            timer.cancel();
            timer.recycle();
        }
        this.homeChannelProgramsInvalidationTimers.clear();
        this.allProgramsInvalidationTimer.start();
    }

    /* access modifiers changed from: private */
    public void invalidateHomeChannelProgramsIfCached(long channelId) {
        ExtendableTimer extendableTimer = this.allProgramsInvalidationTimer;
        if ((extendableTimer == null || !extendableTimer.isStarted()) && this.callbacks.areProgramsCached(channelId)) {
            invalidateHomeChannelPrograms(channelId);
        }
    }

    private void invalidateHomeChannelPrograms(long channelId) {
        ExtendableTimer extendableTimer = this.allProgramsInvalidationTimer;
        if (extendableTimer == null || !extendableTimer.isStarted()) {
            ExtendableTimer timer = this.homeChannelProgramsInvalidationTimers.get(Long.valueOf(channelId));
            if (timer == null) {
                timer = ExtendableTimer.obtain();
                timer.setTimeout(3000);
                timer.setMaximumTimeout(10000);
                timer.setId(channelId);
                timer.setListener(getTimerListener());
                this.homeChannelProgramsInvalidationTimers.put(Long.valueOf(channelId), timer);
            }
            timer.start();
        }
    }

    /* access modifiers changed from: private */
    public void invalidateProgram(long programId) {
        ExtendableTimer extendableTimer = this.allProgramsInvalidationTimer;
        if (extendableTimer == null || !extendableTimer.isStarted()) {
            Long channelId = this.callbacks.findCachedChannelId(programId);
            if (channelId != null) {
                invalidateHomeChannelPrograms(channelId.longValue());
            }
            scheduleProgramsDataLoad(programId);
        }
    }

    private void scheduleProgramsDataLoad(long programId) {
        this.programsToLoadData.add(Long.valueOf(programId));
        if (this.programsToLoadData.size() >= 100) {
            loadProgramData();
            return;
        }
        if (this.programsDataLoadTimer == null) {
            this.programsDataLoadTimer = new ExtendableTimer();
            this.programsDataLoadTimer.setTimeout(1000);
            this.programsDataLoadTimer.setMaximumTimeout(DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            this.programsDataLoadTimer.setId(-4000);
            this.programsDataLoadTimer.setListener(getTimerListener());
        }
        this.programsDataLoadTimer.start();
    }

    /* access modifiers changed from: private */
    public void loadProgramData() {
        ExtendableTimer extendableTimer = this.programsDataLoadTimer;
        if (extendableTimer != null) {
            extendableTimer.cancel();
        }
        if (!this.programsToLoadData.isEmpty()) {
            String selection = buildProgramSelection(this.programsToLoadData);
            if (this.backgroundTaskCallbacks == null) {
                this.backgroundTaskCallbacks = new BackgroundTaskCallbacks();
            }
            DataLoadingBackgroundTask.obtain(this.context).setUri(TvContract.PreviewPrograms.CONTENT_URI).setProjection(PROJECTION).setSelection(selection).setCallbacks(this.backgroundTaskCallbacks).setExtraParam(this.programsToLoadData).execute();
            this.programsToLoadData = new HashSet(100);
        }
    }

    private void startProgramsCountTimer() {
        if (this.programsCountTimer == null) {
            this.programsCountTimer = new ExtendableTimer();
            this.programsCountTimer.setTimeout(3000);
            this.programsCountTimer.setMaximumTimeout(10000);
            this.programsCountTimer.setId(-6000);
            this.programsCountTimer.setListener(getTimerListener());
        }
        this.programsCountTimer.start();
    }

    /* access modifiers changed from: private */
    public void scheduleProgramsCount(long channelId) {
        ExtendableTimer extendableTimer = this.allProgramsInvalidationTimer;
        if (extendableTimer == null || !extendableTimer.isStarted()) {
            this.channelsToCountPrograms.add(Long.valueOf(channelId));
            startProgramsCountTimer();
        }
    }

    /* access modifiers changed from: private */
    public void scheduleProgramsCount(Set<Long> channelIds) {
        ExtendableTimer extendableTimer = this.allProgramsInvalidationTimer;
        if (extendableTimer == null || !extendableTimer.isStarted()) {
            this.channelsToCountPrograms.addAll(channelIds);
            startProgramsCountTimer();
        }
    }

    private String buildProgramSelection(Set<Long> programsToLoad) {
        StringBuilder sb = new StringBuilder("_id IN (");
        for (Long programId : programsToLoad) {
            sb.append(programId.toString());
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    private class BackgroundTaskCallbacks implements DataLoadingBackgroundTask.Callbacks {
        private BackgroundTaskCallbacks() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f1, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f2, code lost:
            if (r0 != null) goto L_0x00f4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f8, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f9, code lost:
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x00fd, code lost:
            throw r2;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onTaskCompleted(com.google.android.tvlauncher.data.DataLoadingBackgroundTask r12) {
            /*
                r11 = this;
                android.database.Cursor r0 = r12.getResult()
                if (r0 != 0) goto L_0x0030
                java.lang.String r1 = "DataSourceObserver"
                java.lang.String r2 = java.lang.String.valueOf(r12)     // Catch:{ all -> 0x00ef }
                java.lang.String r3 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00ef }
                int r3 = r3.length()     // Catch:{ all -> 0x00ef }
                int r3 = r3 + 38
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ef }
                r4.<init>(r3)     // Catch:{ all -> 0x00ef }
                java.lang.String r3 = "Error loading program data with task: "
                r4.append(r3)     // Catch:{ all -> 0x00ef }
                r4.append(r2)     // Catch:{ all -> 0x00ef }
                java.lang.String r2 = r4.toString()     // Catch:{ all -> 0x00ef }
                android.util.Log.e(r1, r2)     // Catch:{ all -> 0x00ef }
                if (r0 == 0) goto L_0x002f
                r0.close()
            L_0x002f:
                return
            L_0x0030:
                com.google.android.tvlauncher.data.DataSourceObserver r1 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.util.ExtendableTimer r1 = r1.allProgramsInvalidationTimer     // Catch:{ all -> 0x00ef }
                if (r1 == 0) goto L_0x004a
                com.google.android.tvlauncher.data.DataSourceObserver r1 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.util.ExtendableTimer r1 = r1.allProgramsInvalidationTimer     // Catch:{ all -> 0x00ef }
                boolean r1 = r1.isStarted()     // Catch:{ all -> 0x00ef }
                if (r1 == 0) goto L_0x004a
                if (r0 == 0) goto L_0x0049
                r0.close()
            L_0x0049:
                return
            L_0x004a:
                java.util.HashSet r1 = new java.util.HashSet     // Catch:{ all -> 0x00ef }
                r1.<init>()     // Catch:{ all -> 0x00ef }
                java.lang.Object r2 = r12.getExtraParam()     // Catch:{ all -> 0x00ef }
                java.util.Set r2 = (java.util.Set) r2     // Catch:{ all -> 0x00ef }
            L_0x0055:
                boolean r3 = r0.moveToNext()     // Catch:{ all -> 0x00ef }
                if (r3 == 0) goto L_0x0099
                r3 = 0
                long r3 = r0.getLong(r3)     // Catch:{ all -> 0x00ef }
                java.lang.Long r5 = java.lang.Long.valueOf(r3)     // Catch:{ all -> 0x00ef }
                r2.remove(r5)     // Catch:{ all -> 0x00ef }
                r5 = 1
                long r5 = r0.getLong(r5)     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver r7 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver$Callbacks r7 = r7.callbacks     // Catch:{ all -> 0x00ef }
                java.lang.Long r7 = r7.findCachedChannelId(r3)     // Catch:{ all -> 0x00ef }
                if (r7 == 0) goto L_0x0080
                long r8 = r7.longValue()     // Catch:{ all -> 0x00ef }
                int r10 = (r8 > r5 ? 1 : (r8 == r5 ? 0 : -1))
                if (r10 == 0) goto L_0x0098
            L_0x0080:
                com.google.android.tvlauncher.data.DataSourceObserver r8 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                r8.invalidateHomeChannelProgramsIfCached(r5)     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver r8 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver$Callbacks r8 = r8.callbacks     // Catch:{ all -> 0x00ef }
                boolean r8 = r8.isChannelBrowsable(r5)     // Catch:{ all -> 0x00ef }
                if (r8 == 0) goto L_0x0098
                java.lang.Long r8 = java.lang.Long.valueOf(r5)     // Catch:{ all -> 0x00ef }
                r1.add(r8)     // Catch:{ all -> 0x00ef }
            L_0x0098:
                goto L_0x0055
            L_0x0099:
                java.util.Iterator r3 = r2.iterator()     // Catch:{ all -> 0x00ef }
            L_0x009d:
                boolean r4 = r3.hasNext()     // Catch:{ all -> 0x00ef }
                if (r4 == 0) goto L_0x00de
                java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x00ef }
                java.lang.Long r4 = (java.lang.Long) r4     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver r5 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver$Callbacks r5 = r5.callbacks     // Catch:{ all -> 0x00ef }
                long r6 = r4.longValue()     // Catch:{ all -> 0x00ef }
                java.lang.Long r5 = r5.findCachedChannelId(r6)     // Catch:{ all -> 0x00ef }
                if (r5 == 0) goto L_0x00cd
                com.google.android.tvlauncher.data.DataSourceObserver r6 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver$Callbacks r6 = r6.callbacks     // Catch:{ all -> 0x00ef }
                java.util.Set r6 = r6.getAllHomeChannelIds()     // Catch:{ all -> 0x00ef }
                boolean r6 = r6.contains(r5)     // Catch:{ all -> 0x00ef }
                if (r6 == 0) goto L_0x00cd
                r1.add(r5)     // Catch:{ all -> 0x00ef }
                goto L_0x00dd
            L_0x00cd:
                if (r5 != 0) goto L_0x00dd
                com.google.android.tvlauncher.data.DataSourceObserver r3 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                com.google.android.tvlauncher.data.DataSourceObserver$Callbacks r3 = r3.callbacks     // Catch:{ all -> 0x00ef }
                java.util.Set r3 = r3.getAllHomeChannelIds()     // Catch:{ all -> 0x00ef }
                r1.addAll(r3)     // Catch:{ all -> 0x00ef }
                goto L_0x00de
            L_0x00dd:
                goto L_0x009d
            L_0x00de:
                boolean r3 = r1.isEmpty()     // Catch:{ all -> 0x00ef }
                if (r3 != 0) goto L_0x00e9
                com.google.android.tvlauncher.data.DataSourceObserver r3 = com.google.android.tvlauncher.data.DataSourceObserver.this     // Catch:{ all -> 0x00ef }
                r3.scheduleProgramsCount(r1)     // Catch:{ all -> 0x00ef }
            L_0x00e9:
                if (r0 == 0) goto L_0x00ee
                r0.close()
            L_0x00ee:
                return
            L_0x00ef:
                r1 = move-exception
                throw r1     // Catch:{ all -> 0x00f1 }
            L_0x00f1:
                r2 = move-exception
                if (r0 == 0) goto L_0x00fc
                r0.close()     // Catch:{ all -> 0x00f8 }
                goto L_0x00fc
            L_0x00f8:
                r3 = move-exception
                com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3)
            L_0x00fc:
                goto L_0x00fe
            L_0x00fd:
                throw r2
            L_0x00fe:
                goto L_0x00fd
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.data.DataSourceObserver.BackgroundTaskCallbacks.onTaskCompleted(com.google.android.tvlauncher.data.DataLoadingBackgroundTask):void");
        }

        public void onTaskPostProcess(DataLoadingBackgroundTask task) {
        }

        public void onTaskCanceled(DataLoadingBackgroundTask task) {
        }

        public void onTaskFailed(DataLoadingBackgroundTask task, Throwable throwable) {
            String valueOf = String.valueOf(task);
            String valueOf2 = String.valueOf(throwable);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 20 + String.valueOf(valueOf2).length());
            sb.append("onTaskFailed: ");
            sb.append(valueOf);
            sb.append(", ex: ");
            sb.append(valueOf2);
            Log.e(DataSourceObserver.TAG, sb.toString());
        }
    }

    private class ExtendableTimerListener implements ExtendableTimer.Listener {
        private ExtendableTimerListener() {
        }

        public void onTimerFired(ExtendableTimer timer) {
            long timerId = timer.getId();
            if (timerId == -1000) {
                DataSourceObserver.this.callbacks.invalidateAllChannels();
            } else if (timerId == -5000) {
                DataSourceObserver.this.callbacks.invalidatePackageChannels();
            } else if (timerId == -2000) {
                DataSourceObserver.this.callbacks.invalidateAllPrograms();
            } else if (timerId == -3000) {
                DataSourceObserver.this.callbacks.invalidateWatchNextPrograms();
            } else if (timerId == -4000) {
                DataSourceObserver.this.loadProgramData();
            } else if (timerId == -6000) {
                if (DataSourceObserver.this.programsCountTimer != null) {
                    DataSourceObserver.this.programsCountTimer.cancel();
                }
                DataSourceObserver.this.callbacks.invalidateProgramsCountForBrowsableChannels(DataSourceObserver.this.channelsToCountPrograms);
                DataSourceObserver.this.channelsToCountPrograms.clear();
            } else if (DataSourceObserver.this.homeChannelProgramsInvalidationTimers.containsKey(Long.valueOf(timerId))) {
                DataSourceObserver.this.homeChannelProgramsInvalidationTimers.remove(Long.valueOf(timerId));
                timer.recycle();
                DataSourceObserver.this.callbacks.invalidateHomeChannelPrograms(timerId);
            } else {
                StringBuilder sb = new StringBuilder(38);
                sb.append("Unknown timer ID: ");
                sb.append(timerId);
                Log.w(DataSourceObserver.TAG, sb.toString());
            }
        }
    }
}
