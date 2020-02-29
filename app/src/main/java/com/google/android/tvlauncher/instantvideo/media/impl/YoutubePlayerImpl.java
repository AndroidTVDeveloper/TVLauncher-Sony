package com.google.android.tvlauncher.instantvideo.media.impl;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.util.YouTubeUriUtils;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class YoutubePlayerImpl implements MediaPlayer {
    private static final byte[] BUFFER = new byte[1024];
    private static final boolean DEBUG = false;
    private static final int DEFAULT_DISPLAY_HEIGHT = 524;
    private static final int DEFAULT_DISPLAY_WIDTH = 932;
    private static final String JAVA_SCRIPT_BRIDGE_TAG = "Android";
    private static final String TAG = "YoutubePlayerImpl";
    private static final String TAG_DISPLAY_HEIGHT = "<display_height>";
    private static final String TAG_DISPLAY_WIDTH = "<display_width>";
    private static final String TAG_VIDEO_ID = "<video_id>";
    private static final int WEBVIEW_STATE_LOADED = 3;
    private static final int WEBVIEW_STATE_LOADING = 1;
    private static final int WEBVIEW_STATE_LOADING_BACKGROUND = 2;
    private static final int WEBVIEW_STATE_NONE = 0;
    private static final int YOUTUBE_STATE_BUFFERING = 3;
    private static final int YOUTUBE_STATE_ENDED = 0;
    private static final int YOUTUBE_STATE_PAUSED = 2;
    private static final int YOUTUBE_STATE_PLAYING = 1;
    private static final int YOUTUBE_STATE_UNSTARTED = -1;
    private static String youtubeHtmlTemplate;
    private Context context;
    /* access modifiers changed from: private */
    public int displayHeight;
    /* access modifiers changed from: private */
    public boolean displaySizeSet;
    /* access modifiers changed from: private */
    public int displayWidth;
    /* access modifiers changed from: private */
    public boolean firstFrameDrawn;
    private Handler handler = new Handler();
    /* access modifiers changed from: private */
    public boolean playWhenReady;
    /* access modifiers changed from: private */
    public int state = 1;
    /* access modifiers changed from: private */
    public MediaPlayer.VideoCallback videoCallback;
    private String videoId;
    /* access modifiers changed from: private */
    public final WebView webView;
    /* access modifiers changed from: private */
    public int webViewState;
    private Uri youtubeUri;

    public YoutubePlayerImpl(Context context2) {
        this.context = context2;
        if (youtubeHtmlTemplate == null) {
            youtubeHtmlTemplate = readYoutubeHtmlTemplate();
        }
        if (youtubeHtmlTemplate != null) {
            this.webViewState = 0;
            this.webView = new WebView(this.context);
            WebSettings settings = this.webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setAllowFileAccess(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setCacheMode(1);
            settings.setAppCacheEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            this.webView.setVerticalScrollBarEnabled(false);
            this.webView.setHorizontalScrollBarEnabled(false);
            this.webView.setOnTouchListener(new View.OnTouchListener(this) {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            return;
        }
        throw new IllegalStateException("Failed to read youtube html template");
    }

    public int getPlaybackState() {
        return this.state;
    }

    public void setVideoUri(Uri uri) {
        if (Uri.EMPTY.equals(uri) || !YouTubeUriUtils.isYouTubeWatchUri(uri)) {
            String valueOf = String.valueOf(uri);
            StringBuilder sb = new StringBuilder(valueOf.length() + 22);
            sb.append("Malformed youtube uri:");
            sb.append(valueOf);
            throw new IllegalArgumentException(sb.toString());
        }
        this.youtubeUri = uri;
        this.videoId = YouTubeUriUtils.getYouTubeVideoId(uri);
        this.displayWidth = 932;
        this.displayHeight = 524;
    }

    public Uri getVideoUri() {
        return this.youtubeUri;
    }

    public void prepare() {
        this.state = 2;
        this.displaySizeSet = true;
        this.firstFrameDrawn = false;
        if (this.webViewState == 0) {
            this.webView.addJavascriptInterface(this, JAVA_SCRIPT_BRIDGE_TAG);
            this.webView.loadDataWithBaseURL("", youtubeHtmlTemplate.replace(TAG_VIDEO_ID, this.videoId).replace(TAG_DISPLAY_WIDTH, Integer.toString(this.displayWidth)).replace(TAG_DISPLAY_HEIGHT, Integer.toString(this.displayHeight)), "text/html", "UTF-8", null);
            this.webView.setInitialScale(100);
            this.webViewState = 1;
            this.webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    int previousState = YoutubePlayerImpl.this.webViewState;
                    int unused = YoutubePlayerImpl.this.webViewState = 3;
                    super.onPageFinished(view, url);
                    YoutubePlayerImpl.this.webView.zoomBy(1.0f);
                    if (previousState == 1) {
                        YoutubePlayerImpl youtubePlayerImpl = YoutubePlayerImpl.this;
                        youtubePlayerImpl.setPlayWhenReady(youtubePlayerImpl.playWhenReady);
                    }
                }
            });
            return;
        }
        this.webView.setInitialScale(100);
    }

    public void setPlayWhenReady(boolean playWhenReady2) {
        this.playWhenReady = playWhenReady2;
        if (this.webViewState == 3) {
            if (playWhenReady2) {
                callJavaScript("player.playVideo();");
            } else {
                callJavaScript("player.pauseVideo();");
            }
        }
    }

    public void stop() {
        if (this.webViewState == 1) {
            this.webViewState = 2;
        }
        if (this.webViewState == 3) {
            callJavaScript("player.seekTo(0, false);player.pauseVideo();");
            this.state = 1;
        }
    }

    public void seekTo(int positionMs) {
    }

    public void setDisplaySize(int width, int height) {
        this.displayWidth = width;
        this.displayHeight = height;
        if (this.state == 3) {
            WebView webView2 = this.webView;
            StringBuilder sb = new StringBuilder(40);
            sb.append("player.setSize(");
            sb.append(width);
            sb.append(",");
            sb.append(height);
            sb.append(");");
            webView2.evaluateJavascript(sb.toString(), null);
            this.displaySizeSet = true;
            return;
        }
        this.displaySizeSet = false;
    }

    public int getCurrentPosition() {
        return 0;
    }

    public void setVolume(float volume) {
        if (volume < 0.0f) {
            volume = 0.0f;
        } else if (volume > 1.0f) {
            volume = 1.0f;
        }
        StringBuilder sb = new StringBuilder(30);
        sb.append("player.setVolume(");
        sb.append((int) ((volume / 1.0f) * 100.0f));
        sb.append(");");
        callJavaScript(sb.toString());
    }

    public View getPlayerView() {
        return this.webView;
    }

    public void setVideoCallback(MediaPlayer.VideoCallback callback) {
        this.videoCallback = callback;
    }

    @JavascriptInterface
    public void onJsPlayerChangeState(final int playerState) {
        this.handler.post(new Runnable() {
            public void run() {
                if (!YoutubePlayerImpl.this.displaySizeSet) {
                    if (!(YoutubePlayerImpl.this.displayWidth == 0 || YoutubePlayerImpl.this.displayHeight == 0)) {
                        YoutubePlayerImpl youtubePlayerImpl = YoutubePlayerImpl.this;
                        int access$400 = youtubePlayerImpl.displayWidth;
                        int access$500 = YoutubePlayerImpl.this.displayHeight;
                        StringBuilder sb = new StringBuilder(40);
                        sb.append("player.setSize(");
                        sb.append(access$400);
                        sb.append(",");
                        sb.append(access$500);
                        sb.append(");");
                        youtubePlayerImpl.callJavaScript(sb.toString());
                    }
                    boolean unused = YoutubePlayerImpl.this.displaySizeSet = true;
                }
                int i = playerState;
                if (i == 1 || i == 2) {
                    if (!YoutubePlayerImpl.this.firstFrameDrawn) {
                        if (YoutubePlayerImpl.this.videoCallback != null) {
                            YoutubePlayerImpl.this.videoCallback.onVideoAvailable();
                        }
                        boolean unused2 = YoutubePlayerImpl.this.firstFrameDrawn = true;
                    }
                    int unused3 = YoutubePlayerImpl.this.state = 3;
                } else if (i == 0) {
                    int unused4 = YoutubePlayerImpl.this.state = 4;
                    if (YoutubePlayerImpl.this.videoCallback != null) {
                        YoutubePlayerImpl.this.videoCallback.onVideoEnded();
                    }
                }
            }
        });
    }

    @JavascriptInterface
    public void onJsPlayerError(final int errorCode) {
        this.handler.post(new Runnable() {
            public void run() {
                YoutubePlayerImpl.this.stop();
                if (YoutubePlayerImpl.this.videoCallback != null) {
                    YoutubePlayerImpl.this.videoCallback.onVideoError();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void callJavaScript(String command) {
        this.webView.evaluateJavascript(command, null);
    }

    private String readYoutubeHtmlTemplate() {
        try {
            InputStream inputStream = this.context.getAssets().open("youtube_template.html");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                for (int readSize = inputStream.read(BUFFER); readSize >= 0; readSize = inputStream.read(BUFFER)) {
                    byteArrayOutputStream.write(BUFFER, 0, readSize);
                }
                inputStream.close();
                return byteArrayOutputStream.toString();
            } catch (IOException e) {
                return null;
            }
        } catch (IOException e2) {
            ThrowableExtension.printStackTrace(e2);
            return null;
        }
    }

    public void release() {
        if (this.webViewState != 0) {
            this.webView.removeJavascriptInterface(JAVA_SCRIPT_BRIDGE_TAG);
            this.webView.loadUrl("about:blank");
        }
        this.youtubeUri = null;
        this.videoId = null;
        this.displayWidth = 0;
        this.displayHeight = 0;
        this.displaySizeSet = false;
        this.firstFrameDrawn = false;
        this.webViewState = 0;
        this.videoCallback = null;
        this.state = 1;
    }
}
