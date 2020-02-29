package com.google.android.tvlauncher.doubleclick;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.android.tvlauncher.instantvideo.widget.InstantVideoView;
import java.util.ArrayList;
import java.util.List;

public class VideoProgressPoller implements Handler.Callback {
    private static final int STOP_POLLER = 3;
    private static final String TAG = "VideoProgressPoller";
    private static final int VIDEO_ENDED_MSG = 2;
    static final int VIDEO_UPDATE_MSG = 1;
    final Handler handler;
    private final InstantVideoView instantVideoView;
    private boolean isTracking = false;
    private List<OnVideoProgressUpdateListener> onVideoProgressUpdateListeners = new ArrayList(2);
    private long refreshIntervalMillis;
    private long videoDurationMillis;

    public interface OnVideoProgressUpdateListener {
        void onVideoProgressUpdate(long j);
    }

    public VideoProgressPoller(InstantVideoView videoView) {
        this.instantVideoView = videoView;
        this.handler = new Handler(this);
    }

    public void addVideoProgressUpdateListener(OnVideoProgressUpdateListener listener) {
        if (!this.onVideoProgressUpdateListeners.contains(listener)) {
            this.onVideoProgressUpdateListeners.add(listener);
        }
    }

    public void removeVideoProgressUpdateListener(OnVideoProgressUpdateListener listener) {
        this.onVideoProgressUpdateListeners.remove(listener);
    }

    public void startTracking(long videoDurationMillis2) {
        if (this.isTracking) {
            long j = this.videoDurationMillis;
            StringBuilder sb = new StringBuilder(130);
            sb.append("startTracking was called while the tracking is in progress, last duration: ");
            sb.append(j);
            sb.append(" new duration: ");
            sb.append(videoDurationMillis2);
            Log.e(TAG, sb.toString());
            stopTracking();
        }
        this.isTracking = true;
        this.videoDurationMillis = videoDurationMillis2;
        this.refreshIntervalMillis = computeRefreshInterval(videoDurationMillis2);
        if (this.refreshIntervalMillis >= 0) {
            this.handler.sendEmptyMessage(1);
            return;
        }
        StringBuilder sb2 = new StringBuilder(81);
        sb2.append("A negative refresh interval obtained from video duration of: ");
        sb2.append(videoDurationMillis2);
        throw new IllegalArgumentException(sb2.toString());
    }

    public void onVideoStopped() {
        stopTracking();
    }

    public void onVideoEnded() {
        Handler handler2 = this.handler;
        handler2.sendMessageAtFrontOfQueue(Message.obtain(handler2, 2));
    }

    private void stopTracking() {
        if (this.isTracking) {
            this.isTracking = false;
            Handler handler2 = this.handler;
            handler2.sendMessageAtFrontOfQueue(Message.obtain(handler2, 3));
        }
    }

    /* access modifiers changed from: package-private */
    public long computeRefreshInterval(long videoDurationMillis2) {
        return videoDurationMillis2 / 4;
    }

    public boolean handleMessage(Message msg) {
        int i = msg.what;
        if (i != 1) {
            if (i == 2) {
                notifyProgressUpdateListeners(this.videoDurationMillis);
            } else if (i != 3) {
                return false;
            }
            this.handler.removeMessages(1);
            return false;
        }
        int currentPosition = this.instantVideoView.getCurrentPosition();
        notifyProgressUpdateListeners((long) currentPosition);
        if (((long) currentPosition) >= this.videoDurationMillis) {
            return false;
        }
        this.handler.sendEmptyMessageDelayed(1, this.refreshIntervalMillis);
        return false;
    }

    private void notifyProgressUpdateListeners(long currentProgressMillis) {
        for (OnVideoProgressUpdateListener listener : this.onVideoProgressUpdateListeners) {
            listener.onVideoProgressUpdate(currentProgressMillis);
        }
    }
}
