package com.google.android.tvlauncher.instantvideo.media.impl;

import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.widget.CustomTvView;
import java.util.List;

public class TvPlayerImpl implements MediaPlayer {
    private static final boolean DEBUG = false;
    private static final String PARAM_INPUT = "input";
    private static final String PATH_PREVIEW_PROGRAM = "preview_program";
    private static final String PATH_RECORDED_PROGRAM = "recorded_program";
    private static final String TAG = "TvPlayerImpl";
    private static final int TYPE_LIVE_CONTENT = 1;
    private static final int TYPE_PREVIEW_PROGRAM = 3;
    private static final int TYPE_RECORDED_PROGRAM = 2;
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public long currentPosition = 0;
    /* access modifiers changed from: private */
    public boolean started = false;
    /* access modifiers changed from: private */
    public int state = 1;
    /* access modifiers changed from: private */
    public String tvInputId;
    /* access modifiers changed from: private */
    public final CustomTvView tvView;
    /* access modifiers changed from: private */
    public MediaPlayer.VideoCallback videoCallback;
    /* access modifiers changed from: private */
    public int videoType;
    /* access modifiers changed from: private */
    public Uri videoUri;
    private float volume = 1.0f;
    /* access modifiers changed from: private */
    public boolean volumeUpdated = false;

    public TvPlayerImpl(Context context2, CustomTvView tvView2) {
        this.context = context2;
        this.tvView = tvView2;
    }

    public Uri getVideoUri() {
        return this.videoUri;
    }

    public void setVideoUri(Uri uri) {
        this.videoUri = uri;
        this.tvInputId = this.videoUri.getQueryParameter("input");
        if (TvContract.isChannelUri(uri)) {
            this.videoType = 1;
        } else if (isRecordedProgramUri(uri)) {
            this.videoType = 2;
        } else if (isPreviewProgramUri(uri)) {
            this.videoType = 3;
        }
    }

    public void prepare() {
        this.started = true;
        this.volumeUpdated = false;
        this.state = 2;
        this.tvView.setCallback(new TvView.TvInputCallback() {
            public void onVideoAvailable(String inputId) {
                if (TvPlayerImpl.this.videoCallback != null) {
                    int unused = TvPlayerImpl.this.state = 3;
                    TvPlayerImpl.this.videoCallback.onVideoAvailable();
                }
            }

            public void onVideoUnavailable(String inputId, int reason) {
                if (reason == 0) {
                    TvPlayerImpl.this.stop();
                    if (TvPlayerImpl.this.videoCallback != null) {
                        TvPlayerImpl.this.videoCallback.onVideoError();
                    }
                }
            }

            public void onConnectionFailed(String inputId) {
                TvPlayerImpl.this.stop();
                if (TvPlayerImpl.this.videoCallback != null) {
                    TvPlayerImpl.this.videoCallback.onVideoError();
                }
            }

            public void onDisconnected(String inputId) {
                TvPlayerImpl.this.stop();
                if (TvPlayerImpl.this.videoCallback != null) {
                    TvPlayerImpl.this.videoCallback.onVideoError();
                }
            }
        });
        if (this.videoType == 2) {
            this.tvView.setTimeShiftPositionCallback(new TvView.TimeShiftPositionCallback() {
                public void onTimeShiftCurrentPositionChanged(String inputId, long timeMs) {
                    super.onTimeShiftCurrentPositionChanged(inputId, timeMs);
                    long unused = TvPlayerImpl.this.currentPosition = timeMs;
                }
            });
        }
        if (this.tvInputId != null) {
            prepareVideo();
            this.tvView.setStreamVolume(0.0f);
        } else if (this.videoType != 3) {
            new AsyncTask<Void, Void, String>() {
                /* access modifiers changed from: protected */
                public String doInBackground(Void... params) {
                    Cursor cursor = TvPlayerImpl.this.context.getContentResolver().query(TvPlayerImpl.this.videoUri, getProjection(), null, null, null);
                    String result = null;
                    if (cursor != null && cursor.moveToNext()) {
                        result = cursor.getString(0);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return result;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(String newTvInputId) {
                    super.onPostExecute((Object) newTvInputId);
                    if (newTvInputId != null && TvPlayerImpl.this.started) {
                        String unused = TvPlayerImpl.this.tvInputId = newTvInputId;
                        TvPlayerImpl.this.prepareVideo();
                        if (!TvPlayerImpl.this.volumeUpdated) {
                            TvPlayerImpl.this.tvView.setStreamVolume(0.0f);
                        }
                    }
                }

                private String[] getProjection() {
                    if (TvPlayerImpl.this.videoType == 1) {
                        return new String[]{"input_id"};
                    }
                    return new String[]{"input_id"};
                }
            }.execute(new Void[0]);
        } else {
            Log.e(TAG, "TV input id must be given via URI query parameter");
            stop();
            MediaPlayer.VideoCallback videoCallback2 = this.videoCallback;
            if (videoCallback2 != null) {
                videoCallback2.onVideoError();
            }
        }
    }

    public void setDisplaySize(int width, int height) {
    }

    public void seekTo(int positionMs) {
        if (this.videoType == 2) {
            this.tvView.timeShiftSeekTo((long) positionMs);
        }
    }

    public void stop() {
        this.started = false;
        this.state = 1;
        this.tvView.reset();
        this.tvView.setCallback(null);
        this.tvView.setTimeShiftPositionCallback(null);
        this.currentPosition = 0;
    }

    public int getCurrentPosition() {
        return (int) this.currentPosition;
    }

    public void setVolume(float volume2) {
        this.volume = volume2;
        if (this.volumeUpdated) {
            this.tvView.setStreamVolume(volume2);
        }
    }

    public int getPlaybackState() {
        return this.state;
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (playWhenReady) {
            this.volumeUpdated = true;
            this.tvView.setStreamVolume(this.volume);
        }
    }

    public void setVideoCallback(MediaPlayer.VideoCallback callback) {
        this.videoCallback = callback;
    }

    public View getPlayerView() {
        return this.tvView;
    }

    public static boolean isRecordedProgramUri(Uri uri) {
        return isTvUri(uri) && isTwoSegmentUriStartingWith(uri, PATH_RECORDED_PROGRAM);
    }

    public static boolean isPreviewProgramUri(Uri uri) {
        return isTvUri(uri) && isTwoSegmentUriStartingWith(uri, PATH_PREVIEW_PROGRAM);
    }

    private static boolean isTvUri(Uri uri) {
        return uri != null && "content".equals(uri.getScheme()) && TvContractCompat.AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isTwoSegmentUriStartingWith(Uri uri, String pathSegment) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments != null && pathSegments.size() == 2 && pathSegment.equals(pathSegments.get(0))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void prepareVideo() {
        int i = this.videoType;
        if (i == 1) {
            this.tvView.tune(this.tvInputId, this.videoUri);
        } else if (i == 2) {
            this.tvView.timeShiftPlay(this.tvInputId, this.videoUri);
        } else if (i == 3) {
            this.tvView.tune(this.tvInputId, this.videoUri);
        }
    }
}
