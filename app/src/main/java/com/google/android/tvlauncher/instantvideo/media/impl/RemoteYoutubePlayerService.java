package com.google.android.tvlauncher.instantvideo.media.impl;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.media.impl.IRemoteYoutubePlayerService;
import java.util.HashMap;
import java.util.Map;

public class RemoteYoutubePlayerService extends Service {
    private static final String TAG = "RemoteYTPlayerService";
    /* access modifiers changed from: private */
    public Handler handler = new Handler(Looper.getMainLooper());
    private int nextSessionToken;
    private final Map<String, Session> sessions = new HashMap();
    private IRemoteYoutubePlayerService.Stub stub = new IRemoteYoutubePlayerService.Stub() {
        public void createSession(Surface surface, int width, int height, IRemoteYoutubePlayerClient client) {
            final Surface surface2 = surface;
            final int i = width;
            final int i2 = height;
            final IRemoteYoutubePlayerClient iRemoteYoutubePlayerClient = client;
            RemoteYoutubePlayerService.this.handler.post(new Runnable() {
                public void run() {
                    String unused = RemoteYoutubePlayerService.this.doCreateSession(surface2, i, i2, iRemoteYoutubePlayerClient);
                }
            });
        }

        public void destroySession(final String sessionToken) throws RemoteException {
            RemoteYoutubePlayerService.this.handler.post(new Runnable() {
                public void run() {
                    RemoteYoutubePlayerService.this.doDestroySession(sessionToken);
                }
            });
        }

        public void start(final String sessionToken, final Uri youtubeUri, float volume) throws RemoteException {
            RemoteYoutubePlayerService.this.handler.post(new Runnable() {
                public void run() {
                    RemoteYoutubePlayerService.this.doStart(sessionToken, youtubeUri);
                }
            });
        }

        public void setVolume(final String sessionToken, final float volume) throws RemoteException {
            RemoteYoutubePlayerService.this.handler.post(new Runnable() {
                public void run() {
                    RemoteYoutubePlayerService.this.doSetVolume(sessionToken, volume);
                }
            });
        }

        public void setSize(final String sessionToken, final int width, final int height) throws RemoteException {
            RemoteYoutubePlayerService.this.handler.post(new Runnable() {
                public void run() {
                    RemoteYoutubePlayerService.this.doSetSize(sessionToken, width, height);
                }
            });
        }
    };

    public IBinder onBind(Intent intent) {
        return this.stub;
    }

    /* access modifiers changed from: private */
    public String doCreateSession(Surface surface, int width, int height, IRemoteYoutubePlayerClient client) {
        Session session = new Session(surface, width, height, client);
        int i = this.nextSessionToken;
        this.nextSessionToken = i + 1;
        String sessionToken = String.valueOf(i);
        this.sessions.put(sessionToken, session);
        try {
            client.onSessionCreated(sessionToken);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot start session", e);
            this.sessions.remove(sessionToken);
        }
        return sessionToken;
    }

    /* access modifiers changed from: private */
    public void doDestroySession(String sessionToken) {
        Session session = this.sessions.get(sessionToken);
        if (session != null) {
            session.stop();
            this.sessions.remove(sessionToken);
        }
    }

    /* access modifiers changed from: private */
    public void doStart(String sessionToken, Uri youtubeUri) {
        Session session = this.sessions.get(sessionToken);
        if (session != null) {
            session.start(youtubeUri);
        }
    }

    /* access modifiers changed from: private */
    public void doSetVolume(String sessionToken, float volume) {
        Session session = this.sessions.get(sessionToken);
        if (session != null) {
            session.setVolume(volume);
        }
    }

    /* access modifiers changed from: private */
    public void doSetSize(String sessionToken, int width, int height) {
        Session session = this.sessions.get(sessionToken);
        if (session != null) {
            session.setSize(width, height);
        }
    }

    private class Session {
        /* access modifiers changed from: private */
        public final IRemoteYoutubePlayerClient client;
        private final int height;
        private YoutubePlayerImpl player;
        private final Surface surface;
        private VirtualDisplay virtualDisplay;
        private final int width;
        private WindowManager windowManager;

        Session(Surface surface2, int width2, int height2, IRemoteYoutubePlayerClient client2) {
            this.surface = surface2;
            this.width = width2;
            this.height = height2;
            this.client = client2;
        }

        /* access modifiers changed from: package-private */
        public void start(Uri youtubeUri) {
            if (this.player == null) {
                createPlayer();
            }
            this.player.setVideoUri(youtubeUri);
            this.player.setDisplaySize(this.width, this.height);
            this.player.prepare();
            this.player.setPlayWhenReady(true);
            this.player.setVideoCallback(new MediaPlayer.VideoCallback() {
                public void onVideoAvailable() {
                    try {
                        Session.this.client.onVideoAvailable();
                    } catch (RemoteException e) {
                        Log.e(RemoteYoutubePlayerService.TAG, "Video callback failed", e);
                    }
                }

                public void onVideoError() {
                    try {
                        Session.this.client.onVideoError();
                    } catch (RemoteException e) {
                        Log.e(RemoteYoutubePlayerService.TAG, "Video callback failed", e);
                    }
                }

                public void onVideoEnded() {
                    try {
                        Session.this.client.onVideoEnded();
                    } catch (RemoteException e) {
                        Log.e(RemoteYoutubePlayerService.TAG, "Video callback failed", e);
                    }
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            YoutubePlayerImpl youtubePlayerImpl = this.player;
            if (youtubePlayerImpl != null) {
                youtubePlayerImpl.stop();
                this.windowManager.removeViewImmediate(this.player.getPlayerView());
                this.virtualDisplay.release();
            }
        }

        /* access modifiers changed from: package-private */
        public void setVolume(float volume) {
            YoutubePlayerImpl youtubePlayerImpl = this.player;
            if (youtubePlayerImpl != null) {
                youtubePlayerImpl.setVolume(volume);
            }
        }

        /* access modifiers changed from: package-private */
        public void setSize(int width2, int height2) {
            YoutubePlayerImpl youtubePlayerImpl = this.player;
            if (youtubePlayerImpl != null) {
                youtubePlayerImpl.setDisplaySize(width2, height2);
            }
        }

        private void createPlayer() {
            this.player = new YoutubePlayerImpl(RemoteYoutubePlayerService.this);
            this.virtualDisplay = ((DisplayManager) RemoteYoutubePlayerService.this.getSystemService(DisplayManager.class)).createVirtualDisplay("youtube", this.width, this.height, getDefaultDisplayDensity(), this.surface, 8);
            this.windowManager = (WindowManager) RemoteYoutubePlayerService.this.createDisplayContext(this.virtualDisplay.getDisplay()).getSystemService(WindowManager.class);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = this.width;
            params.height = this.height;
            params.type = 2030;
            params.flags = 8;
            this.windowManager.addView(this.player.getPlayerView(), params);
        }

        private int getDefaultDisplayDensity() {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) RemoteYoutubePlayerService.this.getSystemService(WindowManager.class)).getDefaultDisplay().getMetrics(metrics);
            return metrics.densityDpi;
        }
    }
}
