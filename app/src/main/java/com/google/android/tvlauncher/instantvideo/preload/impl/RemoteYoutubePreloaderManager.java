package com.google.android.tvlauncher.instantvideo.preload.impl;

import android.content.Context;
import android.net.Uri;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.media.impl.RemoteYoutubePlayerImpl;
import com.google.android.tvlauncher.instantvideo.preload.Preloader;
import com.google.android.tvlauncher.instantvideo.preload.PreloaderManager;
import com.google.android.tvlauncher.instantvideo.util.YouTubeUriUtils;

public class RemoteYoutubePreloaderManager extends PreloaderManager {
    private final Context context;

    public RemoteYoutubePreloaderManager(Context context2) {
        this.context = context2;
    }

    public MediaPlayer getOrCreatePlayer(Uri videoUri) {
        RemoteYoutubePlayerImpl player = new RemoteYoutubePlayerImpl(this.context);
        player.setVideoUri(videoUri);
        return player;
    }

    public int canPlayVideo(Uri videoUri) {
        return YouTubeUriUtils.isYouTubeWatchUri(videoUri) ? 100 : -1;
    }

    public void recycleMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer instanceof RemoteYoutubePlayerImpl) {
            mediaPlayer.stop();
        }
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
}
