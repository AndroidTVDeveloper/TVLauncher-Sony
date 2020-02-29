package com.google.android.tvlauncher.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.application.TvLauncherApplicationBase;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.util.ImageViewTargetWithTrace;
import com.google.android.tvlauncher.home.util.ProgramPreviewImageData;
import com.google.android.tvlauncher.home.util.ProgramPreviewImageTranscoder;
import com.google.android.tvlauncher.home.util.ProgramSettings;
import com.google.android.tvlauncher.home.util.ProgramUtil;
import com.google.android.tvlauncher.home.view.ProgramView;
import com.google.android.tvlauncher.instantvideo.widget.InstantVideoView;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.ContextMenu;
import com.google.android.tvlauncher.util.ContextMenuItem;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;
import com.google.android.tvlauncher.util.LauncherAudioPlayer;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvlauncher.util.ScaleFocusHandler;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvlauncher.util.palette.PaletteBitmapContainer;
import com.google.android.tvrecommendations.shared.util.Constants;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class ProgramController implements View.OnClickListener, ContextMenu.OnItemClickListener, View.OnLongClickListener, ProgramView.OnWindowVisibilityChangedListener, BackHomeControllerListeners.OnHomePressedListener {
    private static final boolean DEBUG = false;
    static final boolean ENABLE_DOUBLE_CLICK_ADS = true;
    private static final double EPS = 0.001d;
    private static final long LIVE_PLAYBACK_PROGRESS_UPDATE_INTERVAL_MS = 60000;
    static final int MENU_ADD_TO_WATCH_NEXT = 2;
    static final int MENU_PRIMARY_ACTION = 1;
    private static final int MENU_REMOVE_FROM_WATCH_NEXT = 4;
    static final int MENU_REMOVE_PREVIEW_PROGRAM = 3;
    private static final int PREVIEW_IMAGE_FADE_DURATION_MILLIS = 300;
    private static final int PREVIEW_MEDIA_START_DELAY_MILLIS = 1250;
    private static final int SCALING_ERROR_MARGIN = 20;
    private static final int STATE_PLAYBACK_ENDED = 1;
    private static final int STATE_PLAYBACK_ERROR = 2;
    private static final int STATE_PLAYBACK_STOPPED = 0;
    private static final String TAG = "ProgramController";
    private static boolean previewImageTranscoderRegistered = false;
    private final MeasureFormat a11yDurationFormat;
    private String actionUri;
    /* access modifiers changed from: private */
    public BitmapDrawable blurredPreviewImageDrawable;
    private boolean canAddToWatchNext;
    private boolean canRemoveProgram;
    private long channelId;
    private String contentId;
    private String debugTitle;
    private final EventLogger eventLogger;
    private ScaleFocusHandler focusHandler;
    private Double focusedAspectRatio;
    private RecyclerViewStateProvider homeListStateProvider;
    private final RequestOptions imageRequestOptions;
    private final IntentLaunchDispatcher intentLauncher;
    private boolean isLegacy;
    private final boolean isSponsored;
    private final boolean isSponsoredBranded;
    /* access modifiers changed from: private */
    public boolean isWatchNextProgram;
    /* access modifiers changed from: private */
    public LauncherAudioPlayer launcherAudioPlayer;
    private RecyclerViewStateProvider listStateProvider;
    private final ColorStateList liveProgressBarForegroundColor;
    private final Runnable liveProgressUpdateRunnable;
    private String logoContentDescription;
    private String logoUri;
    View.OnFocusChangeListener onFocusChangeListener;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    /* access modifiers changed from: private */
    public OnProgramViewFocusChangedListener onProgramViewFocusChangedListener;
    /* access modifiers changed from: private */
    public final View previewAudioContainer;
    /* access modifiers changed from: private */
    public String previewAudioUri;
    /* access modifiers changed from: private */
    public final View previewDelayOverlay;
    private ImageViewTargetWithTrace<ProgramPreviewImageData> previewImageBlurGlideTarget;
    private final int previewImageExpandedVerticalMargin;
    private ValueAnimator previewImageFadeInAnimator;
    private Animator.AnimatorListener previewImageFadeInAnimatorListener;
    private ValueAnimator previewImageFadeOutAnimator;
    private Animator.AnimatorListener previewImageFadeOutAnimatorListener;
    private ValueAnimator.AnimatorUpdateListener previewImageFadeUpdateListener;
    private boolean previewImageNeedsTreatment;
    /* access modifiers changed from: private */
    public Palette previewImagePalette;
    private ImageViewTargetWithTrace<PaletteBitmapContainer> previewImagePaletteGlideTarget;
    private float previewImageVisibilityValue;
    private final SharedPreferences previewMediaPref;
    /* access modifiers changed from: private */
    public final InstantVideoView previewVideo;
    /* access modifiers changed from: private */
    public String previewVideoUri;
    private final int programDefaultBackgroundColor;
    private final ColorDrawable programDefaultBackgroundDrawable;
    private long programDuration;
    private long programId;
    private boolean programIsLive;
    private long programLiveEndTime;
    private long programLiveStartTime;
    /* access modifiers changed from: private */
    public ContextMenu programMenu;
    private final String programMenuAddToWatchNextNotAvailableText;
    private final String programMenuAddToWatchNextText;
    private final String programMenuAlreadyInWatchNextText;
    private final String programMenuRemoveText;
    private String programPackageName;
    /* access modifiers changed from: private */
    public boolean programSelected;
    private final ProgramSettings programSettings;
    private int programState;
    /* access modifiers changed from: private */
    public int programType;
    /* access modifiers changed from: private */
    public ProgramViewLiveProgressUpdateCallback programViewLiveProgressUpdateCallback;
    private String sponsoredProgramContentDescription;
    /* access modifiers changed from: private */
    public final SponsoredProgramControllerHelper sponsoredProgramControllerHelper;
    private final Runnable startPreviewAudioRunnable;
    private final Runnable startPreviewVideoRunnable;
    /* access modifiers changed from: private */
    public long startedPreviewVideoMillis;
    /* access modifiers changed from: private */
    public final ImageView thumbnail;
    private ImageViewTargetWithTrace<Drawable> thumbnailImageGlideTarget;
    /* access modifiers changed from: private */
    public String thumbnailUri;
    private Double unfocusedAspectRatio;
    /* access modifiers changed from: private */
    public final InstantVideoView.VideoCallback videoCallback;
    /* access modifiers changed from: private */
    public final ProgramView view;
    private final ColorStateList watchNextProgressBarForegroundColor;
    private int watchedPreviewVideoSeconds;

    @Retention(RetentionPolicy.SOURCE)
    @interface PlaybackStoppedState {
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    ProgramController(ProgramView v, EventLogger eventLogger2, boolean isSponsored2, boolean isSponsoredBranded2) {
        this(v, eventLogger2, isSponsored2, isSponsoredBranded2, isSponsored2 ? new SponsoredProgramControllerHelper(v) : null);
    }

    ProgramController(ProgramView v, EventLogger eventLogger2, boolean isSponsored2, boolean isSponsoredBranded2, SponsoredProgramControllerHelper sponsoredProgramControllerHelper2) {
        ProgramSettings programSettings2;
        this.isWatchNextProgram = false;
        this.previewImageVisibilityValue = 1.0f;
        this.videoCallback = new InstantVideoView.VideoCallback() {
            public void onVideoStarted(InstantVideoView view) {
                long unused = ProgramController.this.startedPreviewVideoMillis = SystemClock.elapsedRealtime();
                ProgramController.this.fadePreviewImageOut();
                ProgramController.this.previewDelayOverlay.setVisibility(4);
                if (ProgramController.this.sponsoredProgramControllerHelper != null) {
                    ProgramController.this.sponsoredProgramControllerHelper.onVideoStarted();
                }
            }

            public void onVideoEnded(InstantVideoView view) {
                ProgramController.this.stopPreviewVideo(true, 1);
            }

            public void onVideoError(InstantVideoView view) {
                String valueOf = String.valueOf(view.getVideoUri());
                StringBuilder sb = new StringBuilder(valueOf.length() + 20);
                sb.append("onVideoError: uri=[");
                sb.append(valueOf);
                sb.append("]");
                Log.e(ProgramController.TAG, sb.toString());
                ProgramController.this.stopPreviewVideo(true, 2);
            }
        };
        this.startPreviewAudioRunnable = new Runnable() {
            public void run() {
                if (!ProgramController.this.isWatchNextProgram && ProgramController.this.previewAudioUri != null && ProgramController.this.programSelected) {
                    ProgramController.this.launcherAudioPlayer.setCallBacks(new LauncherAudioPlayer.CallBacks() {
                        public void onStarted() {
                            ProgramController.this.previewDelayOverlay.setVisibility(4);
                            ProgramController.this.previewAudioContainer.setVisibility(0);
                        }

                        public void onCompleted() {
                            ProgramController.this.stopPreviewAudio();
                        }

                        public void onError() {
                            ProgramController.this.previewDelayOverlay.setVisibility(4);
                            ProgramController.this.stopPreviewAudio();
                        }

                        public void onPrepared() {
                            if (ProgramController.this.programSelected) {
                                ProgramController.this.launcherAudioPlayer.start();
                            }
                        }
                    });
                    ProgramController.this.launcherAudioPlayer.prepare();
                }
            }
        };
        this.startPreviewVideoRunnable = new Runnable() {
            public void run() {
                if (!ProgramController.this.isWatchNextProgram && ProgramController.this.previewVideoUri != null) {
                    if (ProgramController.this.programSelected) {
                        ProgramController.this.previewVideo.setVisibility(0);
                        if (!ProgramController.this.allowPreviewAudioPlaying()) {
                            ProgramController.this.previewVideo.setVolume(0.0f);
                        }
                        ProgramController.this.previewVideo.start(ProgramController.this.videoCallback);
                        return;
                    }
                    ProgramController.this.startPreviewVideoDelayed();
                }
            }
        };
        this.liveProgressUpdateRunnable = new Runnable() {
            public void run() {
                if (ProgramController.this.programViewLiveProgressUpdateCallback != null) {
                    ProgramController.this.programViewLiveProgressUpdateCallback.updateProgramViewLiveProgress();
                }
            }
        };
        this.onFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (ProgramController.this.programMenu != null && ProgramController.this.programMenu.isShowing()) {
                    ProgramController.this.programMenu.forceDismiss();
                }
                if (!ProgramController.this.isWatchNextProgram) {
                    int i = 0;
                    if (ProgramController.this.previewVideoUri == null) {
                        if (ProgramController.this.thumbnailUri != null) {
                            ProgramController.this.thumbnail.setVisibility(hasFocus ? 0 : 8);
                            View previewImageContainer = ProgramController.this.view.getPreviewImageContainer();
                            if (hasFocus) {
                                i = 8;
                            }
                            previewImageContainer.setVisibility(i);
                        }
                        if (ProgramController.this.previewAudioUri != null) {
                            if (!hasFocus || !v.hasWindowFocus() || ActiveMediaSessionManager.getInstance(v.getContext()).hasActiveMediaSession() || !ProgramController.this.allowPreviewAudioPlaying()) {
                                ProgramController.this.stopPreviewAudio();
                            } else {
                                ProgramController.this.startPreviewAudioDelayed();
                            }
                        }
                    } else if (!hasFocus || !v.hasWindowFocus() || ActiveMediaSessionManager.getInstance(v.getContext()).hasActiveMediaSession() || !ProgramController.this.allowPreviewVideoPlaying()) {
                        ProgramController.this.stopPreviewVideo(true, 0);
                    } else {
                        ProgramController.this.startPreviewVideoDelayed();
                    }
                }
                if (ProgramController.this.onProgramViewFocusChangedListener != null) {
                    ProgramController.this.onProgramViewFocusChangedListener.onProgramViewFocusChanged(hasFocus);
                }
                if (ProgramController.this.sponsoredProgramControllerHelper != null && ProgramController.this.programType == 1002) {
                    ProgramController.this.sponsoredProgramControllerHelper.recordFocusIfDoubleClickAd(hasFocus);
                }
            }
        };
        this.view = v;
        this.eventLogger = eventLogger2;
        this.isSponsored = isSponsored2;
        this.isSponsoredBranded = isSponsoredBranded2;
        this.sponsoredProgramControllerHelper = sponsoredProgramControllerHelper2;
        Context context = this.view.getContext();
        this.intentLauncher = ((TvLauncherApplicationBase) context.getApplicationContext()).getIntentLauncher();
        v.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) view.getResources().getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius));
            }
        });
        v.setClipToOutline(true);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        this.view.setOnWindowVisibilityChangedListener(this);
        this.previewMediaPref = context.getSharedPreferences(TvDataManager.PREVIEW_MEDIA_PREF_FILE_NAME, 0);
        if (this.isSponsored) {
            programSettings2 = ProgramUtil.getSponsoredProgramSettings(v.getContext());
        } else {
            programSettings2 = ProgramUtil.getProgramSettings(v.getContext());
        }
        this.programSettings = programSettings2;
        if (!previewImageTranscoderRegistered) {
            Glide.get(context).getRegistry().register(Bitmap.class, ProgramPreviewImageData.class, new ProgramPreviewImageTranscoder(context));
            previewImageTranscoderRegistered = true;
        }
        this.previewVideo = (InstantVideoView) v.findViewById(C1167R.C1170id.preview_video_view);
        this.previewVideo.setImageViewEnabled(false);
        this.previewAudioContainer = v.findViewById(C1167R.C1170id.preview_audio_container);
        this.thumbnail = (ImageView) v.findViewById(C1167R.C1170id.thumbnail);
        this.a11yDurationFormat = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE);
        this.previewDelayOverlay = v.findViewById(C1167R.C1170id.preview_delay_overlay);
        if (Util.areHomeScreenAnimationsEnabled(context)) {
            this.view.setOnFocusChangeListener(this.onFocusChangeListener);
        } else {
            this.focusHandler = new ScaleFocusHandler(this.programSettings.focusedAnimationDuration, this.programSettings.focusedScale, this.programSettings.focusedElevation, 1);
            this.focusHandler.setView(v);
            this.focusHandler.setOnFocusChangeListener(this.onFocusChangeListener);
        }
        this.programDefaultBackgroundColor = context.getColor(C1167R.color.program_default_background);
        this.programDefaultBackgroundDrawable = new ColorDrawable(this.programDefaultBackgroundColor);
        this.previewImageExpandedVerticalMargin = this.view.getResources().getDimensionPixelOffset(C1167R.dimen.program_preview_image_expanded_vertical_margin);
        int maxHeight = this.programSettings.selectedHeight;
        double d = (double) this.programSettings.selectedHeight;
        Double.isNaN(d);
        this.imageRequestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().override(((int) (d * 1.7777777777777777d)) + 20, maxHeight)).centerInside()).transform(new AddBackgroundColorTransformation(this.programDefaultBackgroundColor, false));
        this.programMenuAddToWatchNextText = context.getString(C1167R.string.program_menu_add_to_play_next_text);
        this.programMenuRemoveText = context.getString(C1167R.string.program_menu_remove_text);
        this.programMenuAddToWatchNextNotAvailableText = context.getString(C1167R.string.program_menu_add_to_play_next_not_available_text);
        this.programMenuAlreadyInWatchNextText = context.getString(C1167R.string.program_menu_already_in_play_next_text);
        this.liveProgressBarForegroundColor = ColorStateList.valueOf(this.view.getContext().getColor(C1167R.color.program_playback_live_progress_bar_foreground_color));
        this.watchNextProgressBarForegroundColor = ColorStateList.valueOf(this.view.getContext().getColor(C1167R.color.program_playback_watch_next_progress_bar_foreground_color));
        if (!this.isSponsored) {
            return;
        }
        if (this.isSponsoredBranded) {
            this.sponsoredProgramContentDescription = context.getString(C1167R.string.sponsored_channel_branding, "Google Play");
            return;
        }
        this.sponsoredProgramContentDescription = context.getString(C1167R.string.sponsored_channel_unbranded_logo_title);
    }

    /* access modifiers changed from: private */
    public boolean allowPreviewVideoPlaying() {
        return !this.isLegacy && this.previewMediaPref.getBoolean(TvDataManager.ENABLE_PREVIEW_VIDEO_KEY, true);
    }

    /* access modifiers changed from: private */
    public boolean allowPreviewAudioPlaying() {
        return !this.isLegacy && this.previewMediaPref.getBoolean(TvDataManager.ENABLE_PREVIEW_AUDIO_KEY, true);
    }

    /* access modifiers changed from: package-private */
    public View getPreviewDelayOverlay() {
        return this.previewDelayOverlay;
    }

    /* access modifiers changed from: package-private */
    public ContextMenu getProgramMenu() {
        return this.programMenu;
    }

    /* access modifiers changed from: package-private */
    public void stopPreviewMedia() {
        if (this.previewVideoUri != null) {
            stopPreviewVideo(true, 0);
        }
        if (this.previewAudioUri != null) {
            stopPreviewAudio();
        }
    }

    /* access modifiers changed from: private */
    public void startPreviewAudioDelayed() {
        this.view.removeCallbacks(this.startPreviewAudioRunnable);
        this.view.postDelayed(this.startPreviewAudioRunnable, 1250);
        this.previewDelayOverlay.setVisibility(0);
    }

    /* access modifiers changed from: private */
    public void stopPreviewAudio() {
        this.view.removeCallbacks(this.startPreviewAudioRunnable);
        LauncherAudioPlayer launcherAudioPlayer2 = this.launcherAudioPlayer;
        if (launcherAudioPlayer2 != null) {
            launcherAudioPlayer2.stopAndRelease();
        }
        this.previewDelayOverlay.setVisibility(4);
        this.previewAudioContainer.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public void startPreviewVideoDelayed() {
        this.view.removeCallbacks(this.startPreviewVideoRunnable);
        this.view.postDelayed(this.startPreviewVideoRunnable, 1250);
        this.previewDelayOverlay.setVisibility(0);
    }

    /* access modifiers changed from: private */
    public void finishStoppingPreviewVideo(int videoPlaybackStoppedState) {
        SponsoredProgramControllerHelper sponsoredProgramControllerHelper2 = this.sponsoredProgramControllerHelper;
        if (sponsoredProgramControllerHelper2 != null) {
            if (videoPlaybackStoppedState == 0) {
                sponsoredProgramControllerHelper2.onVideoStopped();
            } else if (videoPlaybackStoppedState == 1) {
                sponsoredProgramControllerHelper2.onVideoEnded();
            } else if (videoPlaybackStoppedState == 2) {
                sponsoredProgramControllerHelper2.onVideoError();
            }
        }
        this.previewVideo.setVisibility(8);
        this.previewVideo.stop();
    }

    /* access modifiers changed from: private */
    public void stopPreviewVideo(boolean animated, int videoPlaybackStoppedState) {
        logStopVideo();
        this.view.removeCallbacks(this.startPreviewVideoRunnable);
        this.previewDelayOverlay.setVisibility(4);
        if (this.previewImageVisibilityValue == 1.0f) {
            finishStoppingPreviewVideo(videoPlaybackStoppedState);
        } else if (animated) {
            fadePreviewImageIn(videoPlaybackStoppedState);
        } else {
            this.view.getPreviewImageContainer().setVisibility(0);
            setPreviewImageVisibilityValue(1.0f);
            finishStoppingPreviewVideo(videoPlaybackStoppedState);
        }
    }

    private void logStopVideo() {
        if (this.startedPreviewVideoMillis != 0) {
            this.watchedPreviewVideoSeconds = (int) ((SystemClock.elapsedRealtime() - this.startedPreviewVideoMillis) / 1000);
            if (this.watchedPreviewVideoSeconds > 0) {
                LogEvent event = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.WATCH_PREVIEW);
                event.getProgram().setPreview(TvlauncherClientLog.Program.Preview.newBuilder().setPlayedTimestamp(this.startedPreviewVideoMillis).setPlayedDurationSeconds(this.watchedPreviewVideoSeconds));
                this.eventLogger.log(event);
            }
            this.startedPreviewVideoMillis = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setListStateProvider(RecyclerViewStateProvider listStateProvider2) {
        this.listStateProvider = listStateProvider2;
    }

    /* access modifiers changed from: package-private */
    public void setHomeListStateProvider(RecyclerViewStateProvider homeListStateProvider2) {
        this.homeListStateProvider = homeListStateProvider2;
    }

    /* access modifiers changed from: package-private */
    public void setOnProgramViewFocusChangedListener(OnProgramViewFocusChangedListener listener) {
        this.onProgramViewFocusChangedListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setProgramViewLiveProgressUpdateCallback(ProgramViewLiveProgressUpdateCallback callback) {
        this.programViewLiveProgressUpdateCallback = callback;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2) {
        this.onHomeNotHandledListener = onHomeNotHandledListener2;
    }

    /* access modifiers changed from: package-private */
    public void setIsWatchNextProgram(boolean isWatchNextProgram2) {
        this.isWatchNextProgram = isWatchNextProgram2;
    }

    /* access modifiers changed from: package-private */
    public void bind(Program program, String packageName, int programState2, boolean canAddToWatchNext2, boolean canRemoveProgram2, boolean isLegacy2) {
        Program program2 = program;
        ContextMenu contextMenu = this.programMenu;
        if (contextMenu != null && contextMenu.isShowing()) {
            this.programMenu.forceDismiss();
        }
        this.programPackageName = this.isWatchNextProgram ? program.getPackageName() : packageName;
        updateProgramStateAndSelection(programState2);
        this.canAddToWatchNext = canAddToWatchNext2;
        this.canRemoveProgram = canRemoveProgram2;
        this.isLegacy = isLegacy2;
        this.programId = program.getId();
        this.channelId = program.getChannelId();
        this.contentId = program.getContentId();
        this.programType = program.getType();
        int i = this.programType;
        if ((i == 1002 || i == 1001) && !this.isSponsored) {
            this.programType = 4;
        }
        boolean dataReadyForBinding = true;
        int i2 = this.programType;
        if (i2 == 1002) {
            dataReadyForBinding = this.sponsoredProgramControllerHelper.bindDoubleClickAdProgram(program2);
        } else if (i2 == 1001) {
            this.sponsoredProgramControllerHelper.bindDirectAdProgram(program2);
        }
        this.previewVideoUri = program.getPreviewVideoUri();
        String currentPreviewVideoUri = this.previewVideo.getVideoUri() != null ? this.previewVideo.getVideoUri().toString() : null;
        if (currentPreviewVideoUri != null && !currentPreviewVideoUri.equals(this.previewVideoUri)) {
            stopPreviewVideo(false, 0);
        }
        String str = this.previewVideoUri;
        if (str != null) {
            this.previewVideo.setVideoUri(Uri.parse(str));
        } else {
            this.previewVideo.setVideoUri(null);
        }
        int i3 = this.programType;
        if (i3 == 7 || i3 == 8 || i3 == 9 || i3 == 10 || i3 == 11) {
            this.previewAudioUri = program.getPreviewAudioUri();
        } else {
            this.previewAudioUri = null;
        }
        if (this.previewAudioUri != null) {
            if (this.launcherAudioPlayer == null) {
                this.launcherAudioPlayer = new LauncherAudioPlayer();
            }
            if (!this.previewAudioUri.equals(this.launcherAudioPlayer.getDataSource())) {
                if (this.launcherAudioPlayer.getDataSource() != null) {
                    stopPreviewAudio();
                }
                this.launcherAudioPlayer.setDataSource(this.previewAudioUri);
            }
        } else {
            LauncherAudioPlayer launcherAudioPlayer2 = this.launcherAudioPlayer;
            if (launcherAudioPlayer2 != null) {
                launcherAudioPlayer2.setDataSource(null);
            }
        }
        this.watchedPreviewVideoSeconds = 0;
        this.actionUri = program.getActionUri();
        this.programDuration = program.getDuration();
        this.programIsLive = program.isLive();
        long j = 0;
        this.programLiveStartTime = this.programIsLive ? program.getLiveStartTime() : 0;
        if (this.programIsLive) {
            j = program.getLiveEndTime();
        }
        this.programLiveEndTime = j;
        this.unfocusedAspectRatio = Double.valueOf(ProgramUtil.getAspectRatio(program.getPreviewImageAspectRatio()));
        this.thumbnailUri = null;
        this.focusedAspectRatio = null;
        if (!this.isWatchNextProgram) {
            if (this.previewVideoUri != null) {
                int videoWidth = program.getVideoWidth();
                int videoHeight = program.getVideoHeight();
                if (videoWidth > 0 && videoHeight > 0) {
                    double d = (double) videoWidth;
                    double d2 = (double) videoHeight;
                    Double.isNaN(d);
                    Double.isNaN(d2);
                    double ratio = d / d2;
                    if (ratio < this.unfocusedAspectRatio.doubleValue()) {
                        this.focusedAspectRatio = this.unfocusedAspectRatio;
                    } else if (this.unfocusedAspectRatio.doubleValue() <= ratio && ratio <= 4.0d) {
                        this.focusedAspectRatio = Double.valueOf(ratio);
                    }
                }
                if (this.focusedAspectRatio == null) {
                    this.focusedAspectRatio = Double.valueOf(1.7777777777777777d);
                }
            } else {
                this.thumbnailUri = program.getThumbnailUri();
                if (this.thumbnailUri != null) {
                    this.focusedAspectRatio = Double.valueOf(ProgramUtil.getAspectRatio(program.getThumbnailAspectRatio()));
                    if (this.focusedAspectRatio.doubleValue() < this.unfocusedAspectRatio.doubleValue()) {
                        this.focusedAspectRatio = this.unfocusedAspectRatio;
                    }
                }
            }
        }
        if (this.focusedAspectRatio == null) {
            this.focusedAspectRatio = this.unfocusedAspectRatio;
        }
        this.previewImageNeedsTreatment = this.previewVideoUri != null && Math.abs(this.unfocusedAspectRatio.doubleValue() - this.focusedAspectRatio.doubleValue()) > EPS;
        this.blurredPreviewImageDrawable = null;
        if (!Util.areHomeScreenAnimationsEnabled(this.view.getContext())) {
            this.focusHandler.resetFocusedState();
        }
        updateSize();
        updateFocusedState();
        if (dataReadyForBinding) {
            this.view.getPreviewImage().setContentDescription(program.getTitle());
            loadPreviewImage(program.getPreviewImageUri());
        } else {
            setPreviewImagePlaceholder();
        }
        if (this.isSponsored) {
            this.view.getPreviewImage().setContentDescription(this.sponsoredProgramContentDescription);
        }
        updateProgramDimmingFactor();
        this.thumbnail.setVisibility(8);
        if (this.thumbnailUri != null) {
            loadThumbnailImage();
            if (this.programSelected) {
                this.thumbnail.setVisibility(0);
                this.view.getPreviewImageContainer().setVisibility(8);
            }
        } else {
            this.thumbnail.setImageDrawable(null);
            this.view.getPreviewImageContainer().setVisibility(0);
        }
        this.logoUri = program.getLogoUri();
        this.logoContentDescription = program.getLogoContentDescription();
        bindLogoAndBadges();
        updateLogoAndBadgesVisibility(this.programSelected);
        updateProgressState(program);
    }

    private void updateProgramStateAndSelection(int programState2) {
        this.programState = programState2;
        this.programSelected = this.programState == 3 && this.view.isFocused();
    }

    /* access modifiers changed from: package-private */
    public void onStop() {
        stopUpdateProgress();
        SponsoredProgramControllerHelper sponsoredProgramControllerHelper2 = this.sponsoredProgramControllerHelper;
        if (sponsoredProgramControllerHelper2 != null) {
            sponsoredProgramControllerHelper2.onStop();
        }
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        stopUpdateProgress();
        if (Util.isValidContextForGlide(this.view.getContext())) {
            Glide.with(this.view.getContext()).clear(this.view.getPreviewImage());
        }
        this.view.getPreviewImage().setImageDrawable(null);
        SponsoredProgramControllerHelper sponsoredProgramControllerHelper2 = this.sponsoredProgramControllerHelper;
        if (sponsoredProgramControllerHelper2 != null) {
            sponsoredProgramControllerHelper2.onStop();
        }
    }

    private void stopUpdateProgress() {
        this.view.removeCallbacks(this.liveProgressUpdateRunnable);
    }

    private void bindLogoAndBadges() {
        if (needLogo()) {
            loadLogoImage();
            this.view.getLogo().setContentDescription(this.logoContentDescription);
        } else {
            this.view.getLogo().setContentDescription(null);
        }
        if (needDurationBadge()) {
            TextView durationBadge = this.view.getDurationBadge();
            durationBadge.setText(formatDurationInHoursMinutesAndSeconds(this.programDuration));
            durationBadge.setContentDescription(formatDurationForAccessibility(this.programDuration));
        }
    }

    private static String formatDurationInHoursMinutesAndSeconds(long milliseconds) {
        return DateUtils.formatElapsedTime(milliseconds / 1000);
    }

    /* access modifiers changed from: package-private */
    public CharSequence formatDurationForAccessibility(long milliseconds) {
        long milliseconds2;
        long hours = 0;
        long minutes = 0;
        if (milliseconds >= 3600000) {
            hours = milliseconds / 3600000;
            milliseconds2 = milliseconds - (3600000 * hours);
        } else {
            milliseconds2 = milliseconds;
        }
        if (milliseconds2 >= 60000) {
            minutes = milliseconds2 / 60000;
            milliseconds2 -= 60000 * minutes;
        }
        long seconds = milliseconds2 / 1000;
        if (hours > 0) {
            return this.a11yDurationFormat.formatMeasures(new Measure(Long.valueOf(hours), MeasureUnit.HOUR), new Measure(Long.valueOf(minutes), MeasureUnit.MINUTE), new Measure(Long.valueOf(seconds), MeasureUnit.SECOND));
        }
        return this.a11yDurationFormat.formatMeasures(new Measure(Long.valueOf(minutes), MeasureUnit.MINUTE), new Measure(Long.valueOf(seconds), MeasureUnit.SECOND));
    }

    private boolean needLogo() {
        if (this.isLegacy || this.logoUri == null) {
            return false;
        }
        int i = this.programType;
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 5 || i == 6;
    }

    private boolean needLiveBadge() {
        return !this.isLegacy && this.programIsLive;
    }

    private boolean needDurationBadge() {
        if (this.isLegacy) {
            return false;
        }
        long j = this.programDuration;
        long hours = j / 3600000;
        return j >= 1000 && hours <= 99 && this.programType == 4;
    }

    private void updateLogoAndBadgesVisibility(boolean programSelected2) {
        boolean isSelectedRow = this.programState == 3;
        if (!isSelectedRow || !needLogo()) {
            this.view.setLogoVisibility(4);
            this.view.setLogoDimmerVisibility(4);
        } else {
            this.view.setLogoVisibility(0);
            this.view.setLogoDimmerVisibility(0);
        }
        boolean hasLiveIcon = false;
        if (needLiveBadge()) {
            double aspectRatio = (programSelected2 ? this.focusedAspectRatio : this.unfocusedAspectRatio).doubleValue();
            if (!isSelectedRow) {
                this.view.setLiveBadgeVisibility(4);
                this.view.setLiveIconVisibility(4);
            } else if (Double.compare(aspectRatio, 0.6666666666666666d) > 0) {
                this.view.setLiveBadgeVisibility(0);
                this.view.setLiveIconVisibility(4);
            } else {
                hasLiveIcon = true;
                this.view.setLiveBadgeVisibility(4);
                this.view.setLiveIconVisibility(0);
            }
        } else {
            this.view.setLiveBadgeVisibility(8);
            this.view.setLiveIconVisibility(8);
        }
        if (!needDurationBadge()) {
            this.view.setDurationBadgeVisibility(8);
        } else if (!isSelectedRow || hasLiveIcon) {
            this.view.setDurationBadgeVisibility(4);
        } else {
            this.view.setDurationBadgeVisibility(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void bindState(int programState2) {
        updateProgramStateAndSelection(programState2);
        updateProgramDimmingFactor();
        updateLogoAndBadgesVisibility(this.programSelected);
        updateSize();
    }

    private void updateProgramDimmingFactor() {
        ProgramView programView = this.view;
        int i = this.programState;
        boolean z = true;
        if (!(i == 0 || i == 12 || i == 1 || i == 5 || i == 7 || i == 8 || i == 10)) {
            z = false;
        }
        programView.setPreviewImageDimmed(z);
    }

    /* access modifiers changed from: package-private */
    public void updateProgressState(Program program) {
        boolean enableProgressBar = updateLiveProgressIfNeeded();
        if (!enableProgressBar) {
            enableProgressBar = updateWatchNextProgressIfNeeded(program);
        }
        int i = 0;
        this.view.getPlaybackProgressDimmer().setVisibility((!enableProgressBar || needLogo()) ? 8 : 0);
        ProgressBar playbackProgress = this.view.getPlaybackProgress();
        if (!enableProgressBar) {
            i = 8;
        }
        playbackProgress.setVisibility(i);
    }

    private boolean updateLiveProgressIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (this.programIsLive) {
            long j = this.programLiveStartTime;
            if (j > 0) {
                long j2 = this.programLiveEndTime;
                if (j2 > 0 && j < currentTime && currentTime < j2) {
                    ProgressBar playbackProgress = this.view.getPlaybackProgress();
                    playbackProgress.setMin(0);
                    playbackProgress.setMax((int) (this.programLiveEndTime - this.programLiveStartTime));
                    playbackProgress.setProgress((int) (currentTime - this.programLiveStartTime));
                    playbackProgress.setProgressTintList(this.liveProgressBarForegroundColor);
                    this.view.removeCallbacks(this.liveProgressUpdateRunnable);
                    this.view.postDelayed(this.liveProgressUpdateRunnable, 60000);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean updateWatchNextProgressIfNeeded(Program program) {
        if (!this.isWatchNextProgram || program.getDuration() <= 0 || program.getPlaybackPosition() <= 0 || program.getWatchNextType() != 0) {
            return false;
        }
        ProgressBar playbackProgress = this.view.getPlaybackProgress();
        playbackProgress.setMin(0);
        playbackProgress.setMax((int) program.getDuration());
        playbackProgress.setProgress((int) program.getPlaybackPosition());
        playbackProgress.setProgressTintList(this.watchNextProgressBarForegroundColor);
        return true;
    }

    /* access modifiers changed from: private */
    public void setPreviewImageSpecialBackground() {
        this.view.getPreviewImageBackground().setVisibility(0);
        if (this.blurredPreviewImageDrawable == null || this.view.getPreviewImage().getDrawable() == null) {
            this.view.getPreviewImageBackground().setImageDrawable(this.programDefaultBackgroundDrawable);
        } else {
            this.view.getPreviewImageBackground().setImageDrawable(this.blurredPreviewImageDrawable);
        }
    }

    public void clearPreviewImageBackgroundIfPossible() {
        if (this.view.getPreviewImage().getDrawable() != null) {
            this.view.getPreviewImageBackground().setVisibility(4);
            this.view.getPreviewImageBackground().setImageDrawable(null);
        }
    }

    /* access modifiers changed from: package-private */
    public void loadPreviewImage(String previewImageUri) {
        setPreviewImagePlaceholder();
        this.previewImagePalette = null;
        this.blurredPreviewImageDrawable = null;
        if (this.previewImageNeedsTreatment) {
            loadPreviewImageWithBlur(previewImageUri);
        } else {
            loadPreviewImageWithoutBlur(previewImageUri);
        }
    }

    private void setPreviewImagePlaceholder() {
        this.view.getPreviewImageBackground().setVisibility(0);
        this.view.getPreviewImageBackground().setImageDrawable(this.programDefaultBackgroundDrawable);
    }

    private void loadPreviewImageWithBlur(String previewImageUri) {
        RequestBuilder<ProgramPreviewImageData> builder = Glide.with(this.view.getContext()).mo11801as(ProgramPreviewImageData.class).load(previewImageUri).apply((BaseRequestOptions<?>) this.imageRequestOptions);
        if (this.previewImageBlurGlideTarget == null) {
            this.previewImageBlurGlideTarget = new ImageViewTargetWithTrace<ProgramPreviewImageData>(this.view.getPreviewImage(), "LoadProgramImageWithBlur") {
                /* access modifiers changed from: protected */
                public void setResource(ProgramPreviewImageData resource) {
                    if (resource != null) {
                        ((ImageView) this.view).setImageBitmap(resource.getBitmap());
                        Palette unused = ProgramController.this.previewImagePalette = resource.getPalette();
                        ProgramController programController = ProgramController.this;
                        BitmapDrawable unused2 = programController.blurredPreviewImageDrawable = new BitmapDrawable(programController.view.getResources(), resource.getBlurredBitmap());
                    } else {
                        ((ImageView) this.view).setImageDrawable(null);
                    }
                    if (ProgramController.this.programSelected) {
                        ProgramController.this.setPreviewImageSpecialBackground();
                    } else {
                        ProgramController.this.clearPreviewImageBackgroundIfPossible();
                    }
                }
            };
        }
        builder.into(this.previewImageBlurGlideTarget);
    }

    private void loadPreviewImageWithoutBlur(String previewImageUri) {
        RequestBuilder<PaletteBitmapContainer> builder = Glide.with(this.view.getContext()).mo11801as(PaletteBitmapContainer.class).load(previewImageUri).apply((BaseRequestOptions<?>) this.imageRequestOptions);
        if (this.previewImagePaletteGlideTarget == null) {
            this.previewImagePaletteGlideTarget = new ImageViewTargetWithTrace<PaletteBitmapContainer>(this.view.getPreviewImage(), "LoadProgramImage") {
                /* access modifiers changed from: protected */
                public void setResource(PaletteBitmapContainer resource) {
                    if (resource != null) {
                        ((ImageView) this.view).setImageBitmap(resource.getBitmap());
                        Palette unused = ProgramController.this.previewImagePalette = resource.getPalette();
                    } else {
                        ((ImageView) this.view).setImageDrawable(null);
                    }
                    ProgramController.this.clearPreviewImageBackgroundIfPossible();
                }
            };
        }
        builder.into(this.previewImagePaletteGlideTarget);
    }

    private void loadThumbnailImage() {
        this.thumbnail.setImageDrawable(null);
        this.thumbnail.setBackground(this.programDefaultBackgroundDrawable);
        RequestBuilder<Drawable> builder = Glide.with(this.thumbnail.getContext()).load(this.thumbnailUri).apply((BaseRequestOptions<?>) this.imageRequestOptions);
        if (this.thumbnailImageGlideTarget == null) {
            this.thumbnailImageGlideTarget = new ImageViewTargetWithTrace<Drawable>(this, this.thumbnail, "LoadFocusedProgramImage") {
                /* access modifiers changed from: protected */
                public void setResource(Drawable resource) {
                    ((ImageView) this.view).setImageDrawable(resource);
                    if (resource != null) {
                        ((ImageView) this.view).setBackground(null);
                    }
                }
            };
        }
        builder.into(this.thumbnailImageGlideTarget);
    }

    private void loadLogoImage() {
        this.view.getLogo().setImageDrawable(null);
        Glide.with(this.view.getContext()).load(this.logoUri).into(this.view.getLogo());
    }

    private void updateSize() {
        ProgramUtil.updateSize(this.view, this.programState, this.unfocusedAspectRatio.doubleValue(), this.programSettings);
    }

    /* access modifiers changed from: package-private */
    public void updateFocusedState() {
        updateProgramStateAndSelection(this.programState);
        updateAspectRatio(this.programSelected);
        updatePreviewImageSize(this.programSelected);
        updatePreviewImageBackground(this.programSelected);
        updateZoom(this.programSelected);
        updateLogoAndBadgesVisibility(this.programSelected);
    }

    private void updatePreviewImageBackground(boolean programSelected2) {
        if (programSelected2 && this.previewImageNeedsTreatment) {
            setPreviewImageSpecialBackground();
        } else if (!Util.areHomeScreenAnimationsEnabled(this.view.getContext())) {
            clearPreviewImageBackgroundIfPossible();
        }
    }

    private void updateZoom(boolean programSelected2) {
        int pivotX;
        if (Util.areHomeScreenAnimationsEnabled(this.view.getContext())) {
            int width = this.view.getLayoutParams().width;
            int height = this.view.getLayoutParams().height;
            if (width <= 0 || height <= 0) {
                width = this.view.getWidth();
                height = this.view.getHeight();
            }
            if (width > 0 && height > 0) {
                if (this.view.getLayoutDirection() == 1) {
                    pivotX = width;
                } else {
                    pivotX = 0;
                }
                this.view.setPivotX((float) pivotX);
                this.view.setPivotY((float) (height / 2));
            }
            float scale = programSelected2 ? this.programSettings.focusedScale : 1.0f;
            float elevation = programSelected2 ? this.programSettings.focusedElevation : 0.0f;
            this.view.setScaleX(scale);
            this.view.setScaleY(scale);
            this.view.setElevation(elevation);
        }
    }

    private void updateAspectRatio(boolean programSelected2) {
        int newHeight;
        ViewGroup.MarginLayoutParams containerLayoutParams = (ViewGroup.MarginLayoutParams) this.view.getLayoutParams();
        double targetAspectRatio = (programSelected2 ? this.focusedAspectRatio : this.unfocusedAspectRatio).doubleValue();
        double currentAspectRatio = 0.0d;
        if (containerLayoutParams.height > 0) {
            double d = (double) containerLayoutParams.width;
            double d2 = (double) containerLayoutParams.height;
            Double.isNaN(d);
            Double.isNaN(d2);
            currentAspectRatio = d / d2;
        }
        if (Math.abs(targetAspectRatio - currentAspectRatio) > EPS) {
            double d3 = (double) containerLayoutParams.height;
            Double.isNaN(d3);
            containerLayoutParams.width = (int) Math.round(d3 * targetAspectRatio);
            this.view.setLayoutParams(containerLayoutParams);
        }
        if (containerLayoutParams.width > 0 && this.unfocusedAspectRatio.doubleValue() != 0.0d) {
            if (programSelected2) {
                double d4 = (double) containerLayoutParams.width;
                double doubleValue = this.unfocusedAspectRatio.doubleValue();
                Double.isNaN(d4);
                newHeight = (int) Math.round(d4 / doubleValue);
            } else {
                newHeight = -1;
            }
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.view.getPreviewImageBackground().getLayoutParams();
            if (newHeight != lp.height) {
                lp.height = newHeight;
                this.view.getPreviewImageBackground().setLayoutParams(lp);
            }
        }
    }

    private void updatePreviewImageSize(boolean programSelected2) {
        ViewGroup.MarginLayoutParams containerLayoutParams = (ViewGroup.MarginLayoutParams) this.view.getLayoutParams();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.view.getPreviewImage().getLayoutParams();
        int newHeight = lp.height;
        int newWidth = lp.width;
        if (!programSelected2 || !this.previewImageNeedsTreatment) {
            newHeight = -1;
            newWidth = -1;
        } else if (containerLayoutParams.height > 0) {
            newHeight = containerLayoutParams.height - (this.previewImageExpandedVerticalMargin * 2);
            double d = (double) newHeight;
            double doubleValue = this.unfocusedAspectRatio.doubleValue();
            Double.isNaN(d);
            newWidth = (int) Math.round(d * doubleValue);
        }
        if (lp.height != newHeight || lp.width != newWidth) {
            lp.height = newHeight;
            lp.width = newWidth;
            this.view.getPreviewImage().setLayoutParams(lp);
        }
    }

    /* access modifiers changed from: private */
    public void fadePreviewImageOut() {
        ValueAnimator valueAnimator = this.previewImageFadeOutAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            ValueAnimator valueAnimator2 = this.previewImageFadeInAnimator;
            if (valueAnimator2 != null && valueAnimator2.isRunning()) {
                this.previewImageFadeInAnimator.cancel();
                this.previewImageFadeInAnimator = null;
            }
            if (this.previewImageFadeOutAnimatorListener == null) {
                this.previewImageFadeOutAnimatorListener = new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        ProgramController.this.previewVideo.setVisibility(0);
                    }

                    public void onAnimationEnd(Animator animation) {
                        ProgramController.this.view.getPreviewImageContainer().setVisibility(8);
                    }
                };
            }
            this.previewImageFadeOutAnimator = ValueAnimator.ofFloat(this.previewImageVisibilityValue, 0.0f);
            this.previewImageFadeOutAnimator.addUpdateListener(getPreviewImageFadeUpdateListener());
            this.previewImageFadeOutAnimator.addListener(this.previewImageFadeOutAnimatorListener);
            this.previewImageFadeOutAnimator.setDuration(300L);
            this.previewImageFadeOutAnimator.start();
        }
    }

    private void fadePreviewImageIn(final int videoPlaybackStoppedState) {
        ValueAnimator valueAnimator = this.previewImageFadeInAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            ValueAnimator valueAnimator2 = this.previewImageFadeOutAnimator;
            if (valueAnimator2 != null && valueAnimator2.isRunning()) {
                this.previewImageFadeOutAnimator.cancel();
                this.previewImageFadeOutAnimator = null;
            }
            if (this.previewImageFadeInAnimatorListener == null) {
                this.previewImageFadeInAnimatorListener = new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        ProgramController.this.view.getPreviewImageContainer().setVisibility(0);
                    }

                    public void onAnimationEnd(Animator animation) {
                        ProgramController.this.finishStoppingPreviewVideo(videoPlaybackStoppedState);
                    }
                };
            }
            this.previewImageFadeInAnimator = ValueAnimator.ofFloat(this.previewImageVisibilityValue, 1.0f);
            this.previewImageFadeInAnimator.addUpdateListener(getPreviewImageFadeUpdateListener());
            this.previewImageFadeInAnimator.addListener(this.previewImageFadeInAnimatorListener);
            this.previewImageFadeInAnimator.setDuration(300L);
            this.previewImageFadeInAnimator.start();
        }
    }

    private ValueAnimator.AnimatorUpdateListener getPreviewImageFadeUpdateListener() {
        if (this.previewImageFadeUpdateListener == null) {
            this.previewImageFadeUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    ProgramController.this.setPreviewImageVisibilityValue(((Float) animation.getAnimatedValue()).floatValue());
                }
            };
        }
        return this.previewImageFadeUpdateListener;
    }

    /* access modifiers changed from: private */
    public void setPreviewImageVisibilityValue(float visibilityValue) {
        this.previewImageVisibilityValue = visibilityValue;
        this.view.getPreviewImageContainer().setAlpha(this.previewImageVisibilityValue);
        this.view.getLogoAndBadgesContainer().setAlpha(this.previewImageVisibilityValue);
        this.view.getPlaybackProgressDimmer().setAlpha(this.previewImageVisibilityValue);
        this.view.getPlaybackProgress().setAlpha(this.previewImageVisibilityValue);
        if (allowPreviewAudioPlaying()) {
            this.previewVideo.setVolume(1.0f - this.previewImageVisibilityValue);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isViewFocused() {
        return this.view.isFocused();
    }

    /* access modifiers changed from: package-private */
    public boolean isProgramSelected() {
        return this.programSelected;
    }

    /* access modifiers changed from: package-private */
    public Palette getPreviewImagePalette() {
        return this.previewImagePalette;
    }

    /* access modifiers changed from: package-private */
    public LauncherAudioPlayer getLauncherAudioPlayer() {
        return this.launcherAudioPlayer;
    }

    /* access modifiers changed from: package-private */
    public void setLauncherAudioPlayer(LauncherAudioPlayer launcherAudioPlayer2) {
        this.launcherAudioPlayer = launcherAudioPlayer2;
    }

    /* access modifiers changed from: package-private */
    public void setProgramMenu(ContextMenu contextMenu) {
        this.programMenu = contextMenu;
    }

    public void onClick(View v) {
        if (Util.isAccessibilityEnabled(v.getContext())) {
            onLongClick(v);
        } else {
            onPrimaryAction(v);
        }
    }

    private void onPrimaryAction(View view2) {
        int i;
        if (this.actionUri != null || (i = this.programType) == 1002 || i == 1001) {
            long timestamp = System.currentTimeMillis();
            boolean hasPreviewVideo = this.previewVideoUri != null && !this.isWatchNextProgram;
            boolean isPlayingVideo = this.startedPreviewVideoMillis != 0;
            LogEvent event = new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.START_PROGRAM).setVisualElementTag(TvLauncherConstants.PROGRAM_ITEM);
            event.getProgram().setPackageName(this.programPackageName);
            TvlauncherClientLog.Program.Type type = LogEvent.programType(this.programType);
            if (type != null) {
                event.getProgram().setType(type);
            }
            if (hasPreviewVideo) {
                stopPreviewVideo(true, 0);
                TvlauncherClientLog.Program.Preview.Builder preview = TvlauncherClientLog.Program.Preview.newBuilder();
                int i2 = this.watchedPreviewVideoSeconds;
                if (i2 != 0) {
                    preview.setPlayedDurationSeconds(i2);
                }
                event.getProgram().setPreview(preview);
            }
            if (!this.isWatchNextProgram && this.previewAudioUri != null) {
                stopPreviewAudio();
            }
            this.eventLogger.log(event);
            String launchedMediaActionUri = null;
            int i3 = this.programType;
            if (i3 == 1002 || i3 == 1001) {
                launchedMediaActionUri = this.sponsoredProgramControllerHelper.launchMediaIntent(this.actionUri);
                if (this.programType == 1002) {
                    this.sponsoredProgramControllerHelper.recordClickIfDoubleClickAd(isPlayingVideo);
                }
            } else if (this.intentLauncher.launchChannelIntentFromUriWithAnimation(this.programPackageName, this.actionUri, true, view2)) {
                launchedMediaActionUri = this.actionUri;
            }
            if (!TextUtils.isEmpty(launchedMediaActionUri)) {
                Intent intent = new Intent(Constants.ACTION_PROGRAM_LAUNCH_LOG_EVENT).putExtra("timestamp", timestamp).putExtra(Constants.EXTRA_URI, launchedMediaActionUri).putExtra(Constants.EXTRA_HAS_PREVIEW, hasPreviewVideo).putExtra(Constants.EXTRA_IS_PLAYING_PREVIEW, isPlayingVideo);
                intent.setPackage(Constants.TVRECOMMENDATIONS_PACKAGE_NAME);
                this.view.getContext().sendBroadcast(intent);
            }
        }
    }

    public boolean onLongClick(View v) {
        RecyclerViewStateProvider recyclerViewStateProvider;
        RecyclerViewStateProvider recyclerViewStateProvider2;
        String str;
        if (!v.hasFocus() || (((recyclerViewStateProvider = this.listStateProvider) != null && recyclerViewStateProvider.isAnimating()) || ((recyclerViewStateProvider2 = this.homeListStateProvider) != null && recyclerViewStateProvider2.isAnimating()))) {
            return true;
        }
        boolean shouldShowAddToWatchNext = shouldShowAddToWatchNextInProgramMenu();
        boolean shouldShowRemoveProgram = shouldShowRemoveProgramInProgramMenu();
        this.programMenu = new ContextMenu((Activity) v.getContext(), v, v.getResources().getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius));
        boolean z = false;
        if (Util.isAccessibilityEnabled(v.getContext())) {
            ContextMenuItem openItem = new ContextMenuItem(1, v.getContext().getString(C1167R.string.context_menu_primary_action_text), v.getContext().getDrawable(C1167R.C1168drawable.ic_context_menu_open_black));
            openItem.setAutoDismiss(false);
            this.programMenu.addItem(openItem);
        } else if (!this.isWatchNextProgram && !shouldShowAddToWatchNext && !shouldShowRemoveProgram) {
            onPrimaryAction(v);
            return true;
        }
        if (!this.isWatchNextProgram) {
            if (shouldShowAddToWatchNext) {
                boolean isInWatchNext = TvDataManager.getInstance(v.getContext()).isInWatchNext(this.contentId, this.programPackageName);
                ContextMenuItem addToWatchNextItem = new ContextMenuItem(2, null, v.getContext().getDrawable(C1167R.C1168drawable.ic_context_menu_add_to_watch_next_black));
                this.programMenu.addItem(addToWatchNextItem);
                if (this.canAddToWatchNext && !isInWatchNext) {
                    z = true;
                }
                addToWatchNextItem.setEnabled(z);
                if (isInWatchNext) {
                    addToWatchNextItem.setTitle(this.programMenuAlreadyInWatchNextText);
                } else {
                    if (this.canAddToWatchNext) {
                        str = this.programMenuAddToWatchNextText;
                    } else {
                        str = this.programMenuAddToWatchNextNotAvailableText;
                    }
                    addToWatchNextItem.setTitle(str);
                }
            }
            if (shouldShowRemoveProgram) {
                ContextMenuItem removeProgramItem = new ContextMenuItem(3, null, v.getContext().getDrawable(C1167R.C1168drawable.ic_context_menu_uninstall_black));
                this.programMenu.addItem(removeProgramItem);
                removeProgramItem.setTitle(this.programMenuRemoveText);
            }
        } else {
            this.programMenu.addItem(new ContextMenuItem(4, v.getContext().getString(C1167R.string.program_menu_remove_for_play_next_text), v.getContext().getDrawable(C1167R.C1168drawable.ic_context_menu_uninstall_black)));
        }
        this.programMenu.setOnMenuItemClickListener(this);
        this.programMenu.show();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowAddToWatchNextInProgramMenu() {
        return OemConfiguration.get(this.view.getContext()).shouldShowAddToWatchNextFromProgramMenu();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowRemoveProgramInProgramMenu() {
        return this.canRemoveProgram && OemConfiguration.get(this.view.getContext()).shouldShowRemoveProgramFromProgramMenu();
    }

    public void onItemClick(ContextMenuItem item) {
        TvDataManager tvDataManager = TvDataManager.getInstance(this.view.getContext());
        int id = item.getId();
        if (id == 1) {
            onPrimaryAction(this.view);
        } else if (id == 2) {
            tvDataManager.addProgramToWatchlist(this.programId, this.programPackageName);
            tvDataManager.addProgramToWatchNextCache(this.contentId, this.programPackageName);
            LogEvent userEvent = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.ADD_PROGRAM_TO_WATCH_NEXT);
            if (this.programPackageName != null) {
                userEvent.getProgram().setPackageName(this.programPackageName);
            }
            this.eventLogger.log(userEvent);
        } else if (id == 3) {
            tvDataManager.removePreviewProgram(this.programId, this.channelId, this.programPackageName);
            LogEvent userEvent2 = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.REMOVE_PROGRAM_FROM_CHANNEL);
            TvlauncherClientLog.Program.Type type = LogEvent.programType(this.programType);
            if (type != null) {
                userEvent2.getProgram().setType(type);
            }
            if (this.programPackageName != null) {
                userEvent2.getProgram().setPackageName(this.programPackageName);
            }
            this.eventLogger.log(userEvent2);
        } else if (id == 4) {
            tvDataManager.removeProgramFromWatchlist(this.programId, this.programPackageName);
            tvDataManager.removeProgramFromWatchNextCache(this.contentId, this.programPackageName);
            LogEvent userEvent3 = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.REMOVE_PROGRAM_FROM_WATCH_NEXT);
            if (!TextUtils.isEmpty(this.programPackageName)) {
                userEvent3.getProgram().setPackageName(this.programPackageName);
            }
            this.eventLogger.log(userEvent3);
        }
    }

    public void onWindowVisibilityChanged(int visibility) {
        ContextMenu contextMenu;
        if ((visibility == 4 || visibility == 8) && (contextMenu = this.programMenu) != null && contextMenu.isShowing()) {
            this.programMenu.forceDismiss();
        }
    }

    public void onHomePressed(Context c) {
        ContextMenu contextMenu = this.programMenu;
        if (contextMenu == null || !contextMenu.isShowing()) {
            BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2 = this.onHomeNotHandledListener;
            if (onHomeNotHandledListener2 != null) {
                onHomeNotHandledListener2.onHomeNotHandled(c);
                return;
            }
            return;
        }
        this.programMenu.forceDismiss();
    }

    public String toString() {
        String programView = this.view.toString();
        String str = this.debugTitle;
        StringBuilder sb = new StringBuilder(programView.length() + 12 + String.valueOf(str).length());
        sb.append('{');
        sb.append(programView);
        sb.append(", title='");
        sb.append(str);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }
}
