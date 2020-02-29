package com.google.android.tvlauncher.instantvideo.media;

import android.net.Uri;
import android.view.View;

public interface MediaPlayer {
    int STATE_BUFFERING = 2;
    int STATE_ENDED = 4;
    int STATE_IDLE = 1;
    int STATE_READY = 3;

    interface VideoCallback {
        void onVideoAvailable();

        void onVideoEnded();

        void onVideoError();
    }

    int getCurrentPosition();

    int getPlaybackState();

    View getPlayerView();

    Uri getVideoUri();

    void prepare();

    void seekTo(int i);

    void setDisplaySize(int i, int i2);

    void setPlayWhenReady(boolean z);

    void setVideoCallback(VideoCallback videoCallback);

    void setVideoUri(Uri uri);

    void setVolume(float f);

    void stop();
}
