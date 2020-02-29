package com.google.android.tvlauncher.instantvideo.preload.impl;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.media.impl.YoutubePlayerImpl;
import com.google.android.tvlauncher.instantvideo.preload.Preloader;
import com.google.android.tvlauncher.instantvideo.preload.PreloaderManager;
import com.google.android.tvlauncher.instantvideo.util.YouTubeUriUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class YoutubePreloaderManager extends PreloaderManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "YoutubePreloaderManager";
    private static final int YOUTUBE_PLAYER_CACHE_SIZE = 2;
    /* access modifiers changed from: private */
    public Context context;
    private final YoutubePlayerLruCache youtubePlayerCache = new YoutubePlayerLruCache(2);

    private class YoutubePlayerLruCache {
        private final ArrayList<Pair<Uri, YoutubePlayerImpl>> cache;
        private final int maxCapacity;
        private YoutubePlayerImpl releasedPlayer;

        private YoutubePlayerLruCache(int maxCapacity2) {
            this.cache = new ArrayList<>();
            this.maxCapacity = maxCapacity2;
        }

        /* access modifiers changed from: private */
        public YoutubePlayerImpl get(Uri key) {
            int index = 0;
            Iterator<Pair<Uri, YoutubePlayerImpl>> it = this.cache.iterator();
            while (it.hasNext() && !((Uri) it.next().first).equals(key)) {
                index++;
            }
            if (index < this.cache.size()) {
                YoutubePlayerImpl ret = (YoutubePlayerImpl) this.cache.get(index).second;
                this.cache.remove(index);
                return ret;
            }
            YoutubePlayerImpl ret2 = this.releasedPlayer;
            if (ret2 != null) {
                this.releasedPlayer = null;
            } else {
                ret2 = new YoutubePlayerImpl(YoutubePreloaderManager.this.context);
            }
            ret2.setVideoUri(key);
            return ret2;
        }

        /* access modifiers changed from: private */
        public void put(Uri key, YoutubePlayerImpl value) {
            if (this.maxCapacity <= 0) {
                this.releasedPlayer = value;
                this.releasedPlayer.release();
                return;
            }
            if (this.cache.size() >= this.maxCapacity) {
                this.releasedPlayer = (YoutubePlayerImpl) this.cache.get(0).second;
                this.releasedPlayer.release();
                this.cache.remove(0);
            }
            if (this.cache.size() < this.maxCapacity) {
                this.cache.add(new Pair(key, value));
            }
        }
    }

    public YoutubePreloaderManager(Context context2) {
        this.context = context2.getApplicationContext();
    }

    public boolean isPreloaded(Uri videoUri) {
        return false;
    }

    public Preloader createPreloader(Uri videoUri) {
        return null;
    }

    public void clearPreloadedData(Uri videoUri) {
    }

    public void bringPreloadedVideoToTopPriority(Uri videoUri) {
    }

    public MediaPlayer getOrCreatePlayer(Uri videoUri) {
        return this.youtubePlayerCache.get(videoUri);
    }

    public void recycleMediaPlayer(MediaPlayer mediaPlayer) {
        YoutubePlayerImpl youtubePlayer = (YoutubePlayerImpl) mediaPlayer;
        if (youtubePlayer.getPlaybackState() != 1) {
            youtubePlayer.stop();
        }
        this.youtubePlayerCache.put(mediaPlayer.getVideoUri(), youtubePlayer);
    }

    public int canPlayVideo(Uri videoUri) {
        return YouTubeUriUtils.isYouTubeWatchUri(videoUri) ? 100 : 0;
    }
}
