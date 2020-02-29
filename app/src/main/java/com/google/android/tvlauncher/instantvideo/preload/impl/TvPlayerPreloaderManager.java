package com.google.android.tvlauncher.instantvideo.preload.impl;

import android.content.Context;
import android.media.tv.TvContract;
import android.net.Uri;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.media.impl.TvPlayerImpl;
import com.google.android.tvlauncher.instantvideo.preload.Preloader;
import com.google.android.tvlauncher.instantvideo.preload.PreloaderManager;
import com.google.android.tvlauncher.instantvideo.widget.CustomTvView;
import java.util.Deque;
import java.util.LinkedList;

public class TvPlayerPreloaderManager extends PreloaderManager {
    private static final int TV_VIEW_POOL_SIZE = 2;
    private final Context context;
    private final Deque<CustomTvView> tvViewPool = new LinkedList();

    public TvPlayerPreloaderManager(Context context2) {
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
        CustomTvView tvView = this.tvViewPool.pollFirst();
        if (tvView == null) {
            tvView = new CustomTvView(this.context);
        }
        TvPlayerImpl tvPlayer = new TvPlayerImpl(this.context, tvView);
        tvPlayer.setVideoUri(videoUri);
        return tvPlayer;
    }

    public void recycleMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer.getPlaybackState() != 1) {
            mediaPlayer.stop();
        }
        if (this.tvViewPool.size() < 2) {
            this.tvViewPool.addFirst((CustomTvView) mediaPlayer.getPlayerView());
        }
    }

    public int canPlayVideo(Uri videoUri) {
        if (TvContract.isChannelUri(videoUri) || TvPlayerImpl.isRecordedProgramUri(videoUri) || TvPlayerImpl.isPreviewProgramUri(videoUri)) {
            return 100;
        }
        return 0;
    }
}
