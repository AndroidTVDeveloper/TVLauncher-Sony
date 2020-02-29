package com.google.android.tvlauncher.instantvideo.media.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.media.impl.IRemoteYoutubePlayerClient;
import com.google.android.tvlauncher.instantvideo.media.impl.IRemoteYoutubePlayerService;

public class RemoteYoutubePlayerImpl implements MediaPlayer {
    private static final String TAG = "RemoteYoutubePlayerImpl";
    private IRemoteYoutubePlayerClient.Stub client = new IRemoteYoutubePlayerClient.Stub() {
        public void onSessionCreated(String sessionToken) throws RemoteException {
            String unused = RemoteYoutubePlayerImpl.this.sessionToken = sessionToken;
            RemoteYoutubePlayerImpl.this.view.post(new Runnable() {
                public void run() {
                    RemoteYoutubePlayerImpl.this.start();
                }
            });
        }

        public void onVideoAvailable() throws RemoteException {
            RemoteYoutubePlayerImpl.this.view.post(new Runnable() {
                public void run() {
                    int unused = RemoteYoutubePlayerImpl.this.playbackState = 3;
                    if (RemoteYoutubePlayerImpl.this.listener != null) {
                        RemoteYoutubePlayerImpl.this.listener.onVideoAvailable();
                    }
                }
            });
        }

        public void onVideoError() throws RemoteException {
            RemoteYoutubePlayerImpl.this.view.post(new Runnable() {
                public void run() {
                    int unused = RemoteYoutubePlayerImpl.this.playbackState = 4;
                    if (RemoteYoutubePlayerImpl.this.listener != null) {
                        RemoteYoutubePlayerImpl.this.listener.onVideoError();
                    }
                }
            });
        }

        public void onVideoEnded() throws RemoteException {
            RemoteYoutubePlayerImpl.this.view.post(new Runnable() {
                public void run() {
                    int unused = RemoteYoutubePlayerImpl.this.playbackState = 4;
                    if (RemoteYoutubePlayerImpl.this.listener != null) {
                        RemoteYoutubePlayerImpl.this.listener.onVideoEnded();
                    }
                }
            });
        }
    };
    private final Context context;
    /* access modifiers changed from: private */
    public MediaPlayer.VideoCallback listener;
    /* access modifiers changed from: private */
    public int playbackState = 2;
    /* access modifiers changed from: private */
    public IRemoteYoutubePlayerService service;
    private ServiceConnection serviceConnection;
    /* access modifiers changed from: private */
    public String sessionToken;
    /* access modifiers changed from: private */
    public Surface surface;
    /* access modifiers changed from: private */
    public final RemoteYoutubeView view;
    private float volume;
    private Uri youtubeUri;

    public RemoteYoutubePlayerImpl(Context context2) {
        this.view = new RemoteYoutubeView(context2);
        this.context = context2;
        this.view.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                Surface unused = RemoteYoutubePlayerImpl.this.surface = holder.getSurface();
                RemoteYoutubePlayerImpl.this.connectIfConfigured();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                Surface unused = RemoteYoutubePlayerImpl.this.surface = null;
                RemoteYoutubePlayerImpl.this.stop();
            }
        });
    }

    public int getPlaybackState() {
        return this.playbackState;
    }

    public void setVideoUri(Uri uri) {
        this.youtubeUri = uri;
        connectIfConfigured();
    }

    public Uri getVideoUri() {
        return this.youtubeUri;
    }

    /* access modifiers changed from: private */
    public void start() {
        String str;
        Uri uri;
        IRemoteYoutubePlayerService iRemoteYoutubePlayerService = this.service;
        if (iRemoteYoutubePlayerService != null && (str = this.sessionToken) != null && (uri = this.youtubeUri) != null) {
            try {
                iRemoteYoutubePlayerService.start(str, uri, this.volume);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot start session", e);
            }
        }
    }

    public void stop() {
        String str;
        this.view.setVisibility(8);
        if (this.serviceConnection != null) {
            IRemoteYoutubePlayerService iRemoteYoutubePlayerService = this.service;
            if (!(iRemoteYoutubePlayerService == null || (str = this.sessionToken) == null)) {
                try {
                    iRemoteYoutubePlayerService.destroySession(str);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot destroy session", e);
                }
                this.service = null;
            }
            this.context.unbindService(this.serviceConnection);
            this.serviceConnection = null;
            this.sessionToken = null;
        }
    }

    public void setDisplaySize(int width, int height) {
        String str;
        IRemoteYoutubePlayerService iRemoteYoutubePlayerService = this.service;
        if (iRemoteYoutubePlayerService != null && (str = this.sessionToken) != null) {
            try {
                iRemoteYoutubePlayerService.setSize(str, width, height);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot set size", e);
            }
        }
    }

    public void setVolume(float volume2) {
        String str;
        this.volume = volume2;
        IRemoteYoutubePlayerService iRemoteYoutubePlayerService = this.service;
        if (iRemoteYoutubePlayerService != null && (str = this.sessionToken) != null) {
            try {
                iRemoteYoutubePlayerService.setVolume(str, volume2);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot set volume", e);
            }
        }
    }

    public View getPlayerView() {
        return this.view;
    }

    public void setVideoCallback(MediaPlayer.VideoCallback listener2) {
        this.listener = listener2;
        if (listener2 != null) {
            connectIfConfigured();
        }
    }

    public void prepare() {
    }

    public void setPlayWhenReady(boolean playWhenReady) {
    }

    public int getCurrentPosition() {
        return 0;
    }

    public void seekTo(int positionMs) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: private */
    public void connectIfConfigured() {
        if (this.surface != null && this.listener != null && this.youtubeUri != null) {
            this.serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    IRemoteYoutubePlayerService unused = RemoteYoutubePlayerImpl.this.service = IRemoteYoutubePlayerService.Stub.asInterface(binder);
                    RemoteYoutubePlayerImpl.this.onConnected();
                }

                public void onServiceDisconnected(ComponentName name) {
                    IRemoteYoutubePlayerService unused = RemoteYoutubePlayerImpl.this.service = null;
                    if (RemoteYoutubePlayerImpl.this.playbackState != 4 && RemoteYoutubePlayerImpl.this.listener != null) {
                        RemoteYoutubePlayerImpl.this.listener.onVideoEnded();
                    }
                }
            };
            this.context.bindService(new Intent(this.context, RemoteYoutubePlayerService.class), this.serviceConnection, 1);
        }
    }

    /* access modifiers changed from: private */
    public void onConnected() {
        try {
            if (this.surface != null && this.service != null) {
                this.service.createSession(this.surface, this.view.getWidth(), this.view.getHeight(), this.client);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot create session", e);
        }
    }

    public static class RemoteYoutubeView extends SurfaceView {
        public RemoteYoutubeView(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            getHolder().setFixedSize(right - left, top - bottom);
        }
    }
}
