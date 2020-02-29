package com.google.android.tvlauncher.util;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;

public class LauncherAudioPlayer {
    private static final String TAG = "LauncherAudioPlayer";
    /* access modifiers changed from: private */
    public CallBacks callBacks;
    /* access modifiers changed from: private */
    public String dataSource;
    /* access modifiers changed from: private */
    public MediaPlayer mediaPlayer;

    public interface CallBacks {
        void onCompleted();

        void onError();

        void onPrepared();

        void onStarted();
    }

    private void initializeAudio() {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                LauncherAudioPlayer.this.mediaPlayer.setOnCompletionListener(null);
                LauncherAudioPlayer.this.stopAndRelease();
                if (LauncherAudioPlayer.this.callBacks != null) {
                    LauncherAudioPlayer.this.callBacks.onCompleted();
                }
            }
        });
        this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (LauncherAudioPlayer.this.callBacks != null) {
                    LauncherAudioPlayer.this.callBacks.onPrepared();
                }
            }
        });
        this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String access$200 = LauncherAudioPlayer.this.dataSource;
                StringBuilder sb = new StringBuilder(String.valueOf(access$200).length() + 65);
                sb.append("Uri ");
                sb.append(access$200);
                sb.append(" cannot be played with what=");
                sb.append(what);
                sb.append(" and extra=");
                sb.append(extra);
                Log.e(LauncherAudioPlayer.TAG, sb.toString());
                LauncherAudioPlayer.this.stopAndRelease();
                if (LauncherAudioPlayer.this.callBacks == null) {
                    return true;
                }
                LauncherAudioPlayer.this.callBacks.onError();
                return true;
            }
        });
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String dataSource2) {
        this.dataSource = dataSource2;
    }

    public void setCallBacks(CallBacks callBacks2) {
        this.callBacks = callBacks2;
    }

    public void prepare() {
        initializeAudio();
        try {
            this.mediaPlayer.setDataSource(this.dataSource);
            this.mediaPlayer.prepareAsync();
        } catch (IOException e) {
            String valueOf = String.valueOf(e.getMessage());
            Log.e(TAG, valueOf.length() != 0 ? "[ERROR] ".concat(valueOf) : new String("[ERROR] "));
            stopAndRelease();
            CallBacks callBacks2 = this.callBacks;
            if (callBacks2 != null) {
                callBacks2.onError();
            }
        }
    }

    public void start() {
        this.mediaPlayer.start();
        CallBacks callBacks2 = this.callBacks;
        if (callBacks2 != null) {
            callBacks2.onStarted();
        }
    }

    public void stopAndRelease() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            if (mediaPlayer2.isPlaying()) {
                this.mediaPlayer.stop();
            }
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }
}
