package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.support.p001v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.ColorUtils;

public class ProgramView extends FrameLayout {
    private TextView durationBadge;
    private int durationBadgeVisibility;
    private TextView liveBadge;
    private int liveBadgeVisibility;
    private ImageView liveIcon;
    private int liveIconVisibility;
    private ImageView logo;
    private View logoAndBadgesContainer;
    private View logoDimmer;
    private int logoDimmerVisibility;
    private int logoVisibility;
    private OnWindowVisibilityChangedListener onWindowVisibilityChangedListener;
    private ProgressBar playbackProgress;
    private View playbackProgressDimmer;
    private View previewDelayProgress;
    private ImageView previewImage;
    private ImageView previewImageBackground;
    private View previewImageContainer;
    private float previewImageCurrentDimmingFactor;
    private float previewImageDimmedFactorValue;
    private View previewVideo;

    public interface OnWindowVisibilityChangedListener {
        void onWindowVisibilityChanged(int i);
    }

    public ProgramView(Context context) {
        this(context, null);
    }

    public ProgramView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        OnWindowVisibilityChangedListener onWindowVisibilityChangedListener2 = this.onWindowVisibilityChangedListener;
        if (onWindowVisibilityChangedListener2 != null) {
            onWindowVisibilityChangedListener2.onWindowVisibilityChanged(visibility);
        }
    }

    public void setOnWindowVisibilityChangedListener(OnWindowVisibilityChangedListener listener) {
        this.onWindowVisibilityChangedListener = listener;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.previewImageContainer = findViewById(C1167R.C1170id.preview_image_container);
        this.previewImageBackground = (ImageView) findViewById(C1167R.C1170id.preview_image_background);
        this.previewImage = (ImageView) findViewById(C1167R.C1170id.preview_image);
        this.previewVideo = findViewById(C1167R.C1170id.preview_video_view);
        this.previewDelayProgress = findViewById(C1167R.C1170id.preview_delay_progress);
        this.playbackProgress = (ProgressBar) findViewById(C1167R.C1170id.playback_progress);
        this.playbackProgressDimmer = findViewById(C1167R.C1170id.program_playback_progress_dimmer);
        this.logoAndBadgesContainer = findViewById(C1167R.C1170id.program_logo_and_badges_container);
        this.logo = (ImageView) findViewById(C1167R.C1170id.program_card_logo);
        this.logoVisibility = this.logo.getVisibility();
        if (Util.isRtl(getContext())) {
            this.logo.setScaleType(ImageView.ScaleType.FIT_END);
        }
        this.logoDimmer = findViewById(C1167R.C1170id.program_logo_dimmer);
        this.logoDimmerVisibility = this.logoDimmer.getVisibility();
        this.liveBadge = (TextView) findViewById(C1167R.C1170id.program_live_badge);
        this.liveBadgeVisibility = this.liveBadge.getVisibility();
        this.liveIcon = (ImageView) findViewById(C1167R.C1170id.program_live_icon);
        this.liveIconVisibility = this.liveIcon.getVisibility();
        this.durationBadge = (TextView) findViewById(C1167R.C1170id.program_duration_badge);
        this.durationBadgeVisibility = this.durationBadge.getVisibility();
        View badgesContainer = (View) this.liveBadge.getParent();
        badgesContainer.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) view.getResources().getDimensionPixelSize(C1167R.dimen.program_badge_background_corner_radius));
            }
        });
        badgesContainer.setClipToOutline(true);
        this.previewImageDimmedFactorValue = Util.getFloat(getResources(), C1167R.dimen.unfocused_channel_dimming_factor);
    }

    public View getPreviewImageContainer() {
        return this.previewImageContainer;
    }

    public ImageView getPreviewImageBackground() {
        return this.previewImageBackground;
    }

    public ImageView getPreviewImage() {
        return this.previewImage;
    }

    public void setPreviewImageDimmed(boolean dimmed) {
        if (dimmed) {
            this.previewImageCurrentDimmingFactor = this.previewImageDimmedFactorValue;
            this.previewImage.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, this.previewImageCurrentDimmingFactor));
            return;
        }
        this.previewImageCurrentDimmingFactor = 0.0f;
        this.previewImage.setColorFilter((ColorFilter) null);
    }

    public float getPreviewImageDimmingFactor() {
        return this.previewImageCurrentDimmingFactor;
    }

    /* access modifiers changed from: package-private */
    public View getPreviewDelayProgress() {
        return this.previewDelayProgress;
    }

    public View getPreviewVideo() {
        return this.previewVideo;
    }

    public ProgressBar getPlaybackProgress() {
        return this.playbackProgress;
    }

    public View getPlaybackProgressDimmer() {
        return this.playbackProgressDimmer;
    }

    public View getLogoAndBadgesContainer() {
        return this.logoAndBadgesContainer;
    }

    public ImageView getLogo() {
        return this.logo;
    }

    /* access modifiers changed from: package-private */
    public int getLogoVisibility() {
        return this.logoVisibility;
    }

    public void setLogoVisibility(int logoVisibility2) {
        this.logoVisibility = logoVisibility2;
        this.logo.setVisibility(logoVisibility2);
    }

    /* access modifiers changed from: package-private */
    public View getLogoDimmer() {
        return this.logoDimmer;
    }

    /* access modifiers changed from: package-private */
    public int getLogoDimmerVisibility() {
        return this.logoDimmerVisibility;
    }

    public void setLogoDimmerVisibility(int visibility) {
        this.logoDimmerVisibility = visibility;
        this.logoDimmer.setVisibility(visibility);
    }

    public TextView getLiveBadge() {
        return this.liveBadge;
    }

    /* access modifiers changed from: package-private */
    public int getLiveBadgeVisibility() {
        return this.liveBadgeVisibility;
    }

    public void setLiveBadgeVisibility(int visibility) {
        this.liveBadgeVisibility = visibility;
        this.liveBadge.setVisibility(visibility);
    }

    public ImageView getLiveIcon() {
        return this.liveIcon;
    }

    /* access modifiers changed from: package-private */
    public int getLiveIconVisibility() {
        return this.liveIconVisibility;
    }

    public void setLiveIconVisibility(int visibility) {
        this.liveIconVisibility = visibility;
        this.liveIcon.setVisibility(visibility);
    }

    public TextView getDurationBadge() {
        return this.durationBadge;
    }

    /* access modifiers changed from: package-private */
    public int getDurationBadgeVisibility() {
        return this.durationBadgeVisibility;
    }

    public void setDurationBadgeVisibility(int visibility) {
        this.durationBadgeVisibility = visibility;
        this.durationBadge.setVisibility(visibility);
    }
}
