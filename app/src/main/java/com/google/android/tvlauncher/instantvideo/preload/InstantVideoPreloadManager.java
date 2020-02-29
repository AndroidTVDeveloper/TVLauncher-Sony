package com.google.android.tvlauncher.instantvideo.preload;

import android.content.Context;
import android.net.Uri;
import android.util.LruCache;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.preload.Preloader;

public class InstantVideoPreloadManager {
    private static final boolean DEBUG = false;
    private static final int MAX_CONCURRENT_VIDEO_PRELOAD_COUNT = 10;
    private static final String TAG = "InstantVideoPreloadMgr";
    private static InstantVideoPreloadManager instance;
    private final Context appContext;
    private PreloaderManagerImpl preloaderManager;
    /* access modifiers changed from: private */
    public final LruCache<Uri, Preloader> videoPreloaderCache = new LruCache<Uri, Preloader>(10) {
        /* access modifiers changed from: protected */
        public void entryRemoved(boolean evicted, Uri key, Preloader oldValue, Preloader newValue) {
            if (newValue != null) {
                InstantVideoPreloadManager.this.onEntryRemovedFromCache(key, oldValue);
            }
        }
    };

    public static synchronized InstantVideoPreloadManager getInstance(Context context) {
        InstantVideoPreloadManager instantVideoPreloadManager;
        synchronized (InstantVideoPreloadManager.class) {
            if (instance == null) {
                instance = new InstantVideoPreloadManager(context);
            }
            instantVideoPreloadManager = instance;
        }
        return instantVideoPreloadManager;
    }

    InstantVideoPreloadManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.preloaderManager = new PreloaderManagerImpl();
    }

    public void preload(Uri videoUri) {
        if (videoUri == null) {
            throw new IllegalArgumentException("The video URI shouldn't be null.");
        } else if (this.preloaderManager.isPreloaded(videoUri)) {
            this.preloaderManager.bringPreloadedVideoToTopPriority(videoUri);
        } else {
            Preloader preloader = this.videoPreloaderCache.get(videoUri);
            if (preloader == null) {
                Preloader preloader2 = startPreloading(videoUri);
                if (preloader2 != null) {
                    this.videoPreloaderCache.put(videoUri, preloader2);
                    return;
                }
                return;
            }
            this.videoPreloaderCache.put(videoUri, preloader);
            this.preloaderManager.bringPreloadedVideoToTopPriority(videoUri);
        }
    }

    /* access modifiers changed from: private */
    public void onEntryRemovedFromCache(Uri videoUri, Preloader preloader) {
        preloader.stopPreload();
    }

    public void clearCache() {
    }

    public void registerPreloaderManager(PreloaderManager preloaderManager2) {
        this.preloaderManager.registerPreloaderManager(preloaderManager2);
    }

    public void unregisterPreloaderManager(PreloaderManager preloaderManager2) {
        this.preloaderManager.unregisterPreloaderManager(preloaderManager2);
    }

    public MediaPlayer getOrCreatePlayer(Uri videoUri) {
        return this.preloaderManager.getOrCreatePlayer(videoUri);
    }

    public void recyclePlayer(MediaPlayer player, Uri videoUri) {
        this.preloaderManager.recycleMediaPlayer(player);
    }

    private Preloader startPreloading(final Uri videoUri) {
        Preloader preloader = this.preloaderManager.createPreloader(videoUri);
        if (preloader == null) {
            return null;
        }
        preloader.startPreload(new Preloader.OnPreloadFinishedListener() {
            public void onPreloadFinishedListener() {
                InstantVideoPreloadManager.this.videoPreloaderCache.remove(videoUri);
            }
        });
        return preloader;
    }
}
