package com.google.android.tvlauncher.home;

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import java.util.List;

public class ActiveMediaSessionManager implements MediaSessionManager.OnActiveSessionsChangedListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "ActiveMediaSessionManager";
    private static ActiveMediaSessionManager mediaSessionListener;
    private MediaController activeMediaController;
    private Context context;

    public static ActiveMediaSessionManager getInstance(Context context2) {
        if (mediaSessionListener == null) {
            mediaSessionListener = new ActiveMediaSessionManager(context2);
        }
        return mediaSessionListener;
    }

    private ActiveMediaSessionManager(Context context2) {
        this.context = context2.getApplicationContext();
    }

    public void onActiveSessionsChanged(List<MediaController> controllers) {
        this.activeMediaController = controllers.size() == 0 ? null : controllers.get(0);
    }

    public void start() {
        MediaSessionManager manager = (MediaSessionManager) this.context.getSystemService("media_session");
        onActiveSessionsChanged(manager.getActiveSessions(null));
        manager.addOnActiveSessionsChangedListener(this, null);
    }

    public void stop() {
        ((MediaSessionManager) this.context.getSystemService("media_session")).removeOnActiveSessionsChangedListener(this);
    }

    /* access modifiers changed from: package-private */
    public boolean hasActiveMediaSession() {
        PlaybackState state;
        MediaController mediaController = this.activeMediaController;
        if (mediaController == null || (state = mediaController.getPlaybackState()) == null || state.getState() != 3) {
            return false;
        }
        return true;
    }
}
