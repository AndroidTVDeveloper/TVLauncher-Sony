package com.google.android.tvlauncher.instantvideo.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.google.android.tvlauncher.instantvideo.media.MediaPlayer;
import com.google.android.tvlauncher.instantvideo.preload.InstantVideoPreloadManager;

public class InstantVideoView extends FrameLayout {
    private static final boolean DEBUG = false;
    private static final long FADE_OUT_DURATION_MS = 2000;
    private static final String TAG = "InstantVideoView";
    private static final int VIDEO_IDLE = 0;
    private static final int VIDEO_PREPARING = 1;
    private static final int VIDEO_SHOWN = 2;
    /* access modifiers changed from: private */
    public ImageView imageView;
    /* access modifiers changed from: private */
    public ViewPropertyAnimator imageViewFadeOut;
    private MediaPlayer player;
    private Runnable stopVideoRunnable;
    /* access modifiers changed from: private */
    public boolean videoStarted;
    private Uri videoUri;
    private View videoView;
    private float volume;

    public InstantVideoView(Context context) {
        this(context, null, 0);
    }

    public InstantVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstantVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.volume = 1.0f;
        this.stopVideoRunnable = new Runnable() {
            public void run() {
                InstantVideoView.this.stopVideoInternal();
            }
        };
        setImageViewEnabled(true);
        setDisplayedChild(0);
    }

    public void setVideoUri(Uri uri) {
        this.videoUri = uri;
    }

    public Uri getVideoUri() {
        return this.videoUri;
    }

    public void setImageDrawable(Drawable drawable) {
        this.imageView.setImageDrawable(drawable);
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public void setVolume(float volume2) {
        this.volume = volume2;
        MediaPlayer mediaPlayer = this.player;
        if (mediaPlayer != null && this.videoStarted) {
            mediaPlayer.setVolume(volume2);
        }
    }

    public void setImageViewEnabled(boolean enable) {
        if (enable && this.imageView == null) {
            this.imageView = new ImageView(getContext());
            addView(this.imageView, new FrameLayout.LayoutParams(-1, -1, 17));
        } else if (!enable && this.imageView != null) {
            ViewPropertyAnimator viewPropertyAnimator = this.imageViewFadeOut;
            if (viewPropertyAnimator != null) {
                viewPropertyAnimator.cancel();
                this.imageViewFadeOut = null;
            }
            removeView(this.imageView);
            this.imageView = null;
        }
    }

    public void start(final VideoCallback callback) {
        if (!this.videoStarted) {
            if (this.videoView != null) {
                stopVideoInternal();
            }
            this.videoStarted = true;
            this.player = InstantVideoPreloadManager.getInstance(getContext()).getOrCreatePlayer(this.videoUri);
            MediaPlayer mediaPlayer = this.player;
            if (mediaPlayer == null) {
                this.videoStarted = false;
                if (callback != null) {
                    callback.onVideoError(this);
                    return;
                }
                return;
            }
            this.videoView = mediaPlayer.getPlayerView();
            addView(this.videoView, new FrameLayout.LayoutParams(-1, -1, 17));
            ImageView imageView2 = this.imageView;
            if (imageView2 != null) {
                bringChildToFront(imageView2);
            }
            if (!(getWidth() == 0 || getHeight() == 0)) {
                this.player.setDisplaySize(getWidth(), getHeight());
            }
            setDisplayedChild(1);
            this.player.prepare();
            this.player.setPlayWhenReady(true);
            this.player.setVolume(this.volume);
            this.player.setVideoCallback(new MediaPlayer.VideoCallback() {
                public void onVideoAvailable() {
                    if (InstantVideoView.this.videoStarted) {
                        InstantVideoView.this.setDisplayedChild(2);
                        VideoCallback videoCallback = callback;
                        if (videoCallback != null) {
                            videoCallback.onVideoStarted(InstantVideoView.this);
                        }
                    }
                }

                public void onVideoError() {
                    VideoCallback videoCallback;
                    if (InstantVideoView.this.videoStarted && (videoCallback = callback) != null) {
                        videoCallback.onVideoError(InstantVideoView.this);
                    }
                }

                public void onVideoEnded() {
                    VideoCallback videoCallback;
                    if (InstantVideoView.this.videoStarted && (videoCallback = callback) != null) {
                        videoCallback.onVideoEnded(InstantVideoView.this);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        MediaPlayer mediaPlayer;
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width != 0 && height != 0 && (mediaPlayer = this.player) != null) {
            mediaPlayer.setDisplaySize(width, height);
        }
    }

    /* access modifiers changed from: private */
    public void setDisplayedChild(int videoState) {
        if (videoState == 0) {
            ViewPropertyAnimator viewPropertyAnimator = this.imageViewFadeOut;
            if (viewPropertyAnimator != null) {
                viewPropertyAnimator.cancel();
                this.imageViewFadeOut = null;
            }
            ImageView imageView2 = this.imageView;
            if (imageView2 != null) {
                imageView2.setVisibility(0);
                this.imageView.setAlpha(1.0f);
            }
            View view = this.videoView;
            if (view != null) {
                view.setVisibility(8);
            }
        } else if (videoState == 1) {
            View view2 = this.videoView;
            if (view2 != null) {
                view2.setVisibility(0);
                this.videoView.setAlpha(0.0f);
            }
        } else {
            ImageView imageView3 = this.imageView;
            if (imageView3 != null) {
                this.imageViewFadeOut = imageView3.animate();
                this.imageViewFadeOut.alpha(0.0f).setDuration(2000).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ViewPropertyAnimator unused = InstantVideoView.this.imageViewFadeOut = null;
                        InstantVideoView.this.imageView.setVisibility(8);
                    }
                }).start();
            }
            View view3 = this.videoView;
            if (view3 != null) {
                view3.setVisibility(0);
                this.videoView.setAlpha(1.0f);
            }
        }
    }

    public void stop() {
        if (this.videoStarted) {
            this.videoStarted = false;
            this.player.setVideoCallback(null);
            setDisplayedChild(0);
            post(this.stopVideoRunnable);
        }
    }

    public void seekTo(int positionMs) {
        this.player.seekTo(positionMs);
    }

    public int getCurrentPosition() {
        return this.player.getCurrentPosition();
    }

    public boolean isPlaying() {
        return this.videoStarted;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    /* access modifiers changed from: private */
    public void stopVideoInternal() {
        removeCallbacks(this.stopVideoRunnable);
        if (this.videoView != null) {
            this.player.setVideoCallback(null);
            this.player.stop();
            InstantVideoPreloadManager.getInstance(getContext()).recyclePlayer(this.player, this.videoUri);
            this.player = null;
            removeView(this.videoView);
            this.videoView = null;
        }
    }

    public static abstract class VideoCallback {
        public void onVideoError(InstantVideoView view) {
        }

        public void onVideoEnded(InstantVideoView view) {
        }

        public void onVideoStarted(InstantVideoView view) {
        }
    }
}
