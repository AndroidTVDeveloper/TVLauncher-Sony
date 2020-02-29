package com.google.android.tvlauncher.appsview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.p001v4.content.ContextCompat;
import android.support.p001v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.palette.InstallingItemPaletteBitmapContainer;
import com.google.android.tvlauncher.appsview.transformation.AppBannerInstallingIconTransformation;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.ColorUtils;

class InstallStateOverlayHelper {
    private static final int FALLBACK_PROGRESS_COLOR = -1;
    /* access modifiers changed from: private */
    public final int animationDuration;
    /* access modifiers changed from: private */
    public final FrameLayout bannerContainer;
    private final int bannerMaxHeight;
    private final int bannerMaxWidth;
    /* access modifiers changed from: private */
    public final ImageView bannerView = ((ImageView) this.overlay.findViewById(C1167R.C1170id.app_install_banner));
    private Animator circleRevealAnimator;
    private final ImageView containerImageView;
    private float currentDimmingFactor;
    private final int determinateProgressHeight;
    private final float dimmedFactorValue;
    private final String downloadingString;
    private final float grayScaleSaturation;
    private final int iconBannerBackgroundColor;
    private final float iconCornerMaxRadius;
    private final float iconDarkenFactor;
    private final int iconInstallingMaxSize;
    private final int indeterminateProgressBottomMargin;
    private final int indeterminateProgressHeight;
    private final String installPendingString;
    private final String installingString;
    /* access modifiers changed from: private */
    public boolean isShowingBanner;
    /* access modifiers changed from: private */
    public final View overlay;
    private final Drawable placeholderBanner;
    private Drawable placeholderIcon;
    /* access modifiers changed from: private */
    public final ProgressBar progressBar = ((ProgressBar) this.overlay.findViewById(C1167R.C1170id.progress_bar));
    private final String restorePendingString;
    private final TextView titleView;
    private final String updatePendingString;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, com.google.android.tvlauncher.appsview.BannerView, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    InstallStateOverlayHelper(BannerView v) {
        Context context = v.getContext();
        this.overlay = LayoutInflater.from(context).inflate(C1167R.layout.install_state_overlay, (ViewGroup) v, false);
        Resources res = context.getResources();
        this.bannerMaxWidth = res.getDimensionPixelSize(C1167R.dimen.app_banner_image_max_width);
        this.bannerMaxHeight = res.getDimensionPixelSize(C1167R.dimen.app_banner_image_max_height);
        this.dimmedFactorValue = Util.getFloat(res, C1167R.dimen.unfocused_channel_dimming_factor);
        this.animationDuration = res.getInteger(C1167R.integer.banner_install_anim_duration);
        this.iconBannerBackgroundColor = context.getColor(C1167R.color.app_banner_background_color_gray_scale);
        float bannerFocusedScale = res.getFraction(C1167R.fraction.home_app_banner_focused_scale, 1, 1);
        this.iconInstallingMaxSize = res.getDimensionPixelSize(C1167R.dimen.app_banner_installing_icon_max_size);
        this.iconCornerMaxRadius = ((float) res.getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius)) * bannerFocusedScale;
        this.grayScaleSaturation = Util.getFloat(res, C1167R.dimen.gray_scale_saturation);
        this.iconDarkenFactor = Util.getFloat(res, C1167R.dimen.install_icon_darken_factor);
        this.bannerContainer = v.getBannerContainer();
        this.titleView = v.getTitleView();
        this.containerImageView = (ImageView) this.bannerContainer.findViewById(C1167R.C1170id.banner_image);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.placeholderIcon = context.getDrawable(C1167R.C1168drawable.system_default_app_icon_banner);
        this.downloadingString = res.getString(C1167R.string.downloading);
        this.installingString = res.getString(C1167R.string.installing);
        this.installPendingString = res.getString(C1167R.string.install_pending);
        this.updatePendingString = res.getString(C1167R.string.update_pending);
        this.restorePendingString = res.getString(C1167R.string.restore_pending);
        this.determinateProgressHeight = res.getDimensionPixelSize(C1167R.dimen.install_progress_bar_height);
        this.indeterminateProgressHeight = res.getDimensionPixelSize(C1167R.dimen.install_progress_bar_indeterminate_height);
        this.indeterminateProgressBottomMargin = res.getDimensionPixelSize(C1167R.dimen.install_progress_bar_indeterminate_bottom_margin);
    }

    /* access modifiers changed from: package-private */
    public void updateOverlay(LaunchItem item) {
        Context context = this.bannerContainer.getContext();
        int progressPercent = item.getInstallProgressPercent();
        switch (item.getInstallState()) {
            case UNKNOWN:
                this.progressBar.setIndeterminate(true);
                this.progressBar.setProgress(0);
                this.progressBar.setRotation(0.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                break;
            case INSTALL_PENDING:
                this.progressBar.setIndeterminate(true);
                this.progressBar.setProgress(0);
                this.progressBar.setRotation(0.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                this.titleView.setText(this.installPendingString);
                break;
            case UPDATE_PENDING:
                this.progressBar.setIndeterminate(true);
                this.progressBar.setProgress(0);
                this.progressBar.setRotation(0.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                this.titleView.setText(this.updatePendingString);
                break;
            case RESTORE_PENDING:
                this.progressBar.setIndeterminate(true);
                this.progressBar.setProgress(0);
                this.progressBar.setRotation(0.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                this.titleView.setText(this.restorePendingString);
                break;
            case INSTALLING:
                this.progressBar.setIndeterminate(true);
                this.progressBar.setProgress(0);
                this.progressBar.setRotation(180.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                this.titleView.setText(this.installingString);
                break;
            case DOWNLOADING:
                this.progressBar.setIndeterminate(false);
                this.progressBar.setProgress(progressPercent);
                this.progressBar.setRotation(0.0f);
                this.progressBar.setLayoutParams(getProgressLayoutParams());
                this.titleView.setText(this.downloadingString);
                break;
            default:
                throw new IllegalStateException("Invalid install state.");
        }
        if (this.overlay.getParent() == null) {
            addOverlay();
            setInstallingGraphic(item, context);
        }
    }

    private FrameLayout.LayoutParams getProgressLayoutParams() {
        if (this.progressBar.isIndeterminate()) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, this.indeterminateProgressHeight);
            layoutParams.gravity = 80;
            layoutParams.setMargins(0, 0, 0, this.indeterminateProgressBottomMargin);
            return layoutParams;
        }
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, this.determinateProgressHeight);
        layoutParams2.gravity = 80;
        return layoutParams2;
    }

    private void setInstallingGraphic(LaunchItem item, Context context) {
        if (item.getBannerUri() != null) {
            this.isShowingBanner = true;
            Glide.with(context).mo11801as(InstallingItemPaletteBitmapContainer.class).error(createIconBannerBuilder(item, context)).load(item.getBannerUri()).apply((BaseRequestOptions<?>) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).placeholder(this.placeholderBanner)).transform(new AddBackgroundColorTransformation(context.getColor(C1167R.color.app_banner_background_color_gray_scale), true)))).into(new ImageViewTarget<InstallingItemPaletteBitmapContainer>(this.bannerView) {
                /* access modifiers changed from: protected */
                public void setResource(InstallingItemPaletteBitmapContainer resource) {
                    if (resource != null) {
                        InstallStateOverlayHelper.this.bannerView.setImageBitmap(resource.getBitmap());
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                        alphaAnimation.setDuration((long) InstallStateOverlayHelper.this.animationDuration);
                        alphaAnimation.setFillAfter(true);
                        int vibrantColor = resource.getPalette().getVibrantColor(-1);
                        InstallStateOverlayHelper.this.progressBar.getProgressDrawable().setTint(vibrantColor);
                        InstallStateOverlayHelper.this.progressBar.getIndeterminateDrawable().setTint(vibrantColor);
                        InstallStateOverlayHelper installStateOverlayHelper = InstallStateOverlayHelper.this;
                        installStateOverlayHelper.cancelCurrentAnimationAndStartAnimation(installStateOverlayHelper.bannerView, alphaAnimation);
                    }
                }

                public void onLoadFailed(Drawable resource) {
                    super.onLoadFailed(resource);
                    boolean unused = InstallStateOverlayHelper.this.isShowingBanner = false;
                }
            });
            return;
        }
        this.isShowingBanner = false;
        createIconBannerBuilder(item, context).into(new ImageViewTarget<InstallingItemPaletteBitmapContainer>(this.bannerView) {
            /* access modifiers changed from: protected */
            public void setResource(InstallingItemPaletteBitmapContainer resource) {
                if (resource != null) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                    alphaAnimation.setDuration((long) InstallStateOverlayHelper.this.animationDuration);
                    alphaAnimation.setFillAfter(true);
                    InstallStateOverlayHelper.this.bannerView.setImageBitmap(resource.getBitmap());
                    InstallStateOverlayHelper installStateOverlayHelper = InstallStateOverlayHelper.this;
                    installStateOverlayHelper.cancelCurrentAnimationAndStartAnimation(installStateOverlayHelper.bannerView, alphaAnimation);
                }
            }
        });
    }

    private RequestBuilder<InstallingItemPaletteBitmapContainer> createIconBannerBuilder(LaunchItem item, Context context) {
        RequestOptions requestOptions = new RequestOptions();
        int i = this.iconInstallingMaxSize;
        return Glide.with(context).mo11801as(InstallingItemPaletteBitmapContainer.class).load(item.getIconUri()).apply((BaseRequestOptions<?>) ((RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) requestOptions.override(i, i)).fallback(this.placeholderIcon)).diskCacheStrategy(DiskCacheStrategy.NONE)).transform(new AppBannerInstallingIconTransformation(this.iconBannerBackgroundColor, this.iconDarkenFactor, this.grayScaleSaturation, this.bannerMaxWidth, this.bannerMaxHeight, this.iconCornerMaxRadius))));
    }

    /* access modifiers changed from: package-private */
    public void removeOverlay() {
        if (this.overlay.getParent() == null) {
            return;
        }
        if (this.isShowingBanner && this.containerImageView.isAttachedToWindow()) {
            Animator animator = this.circleRevealAnimator;
            if (animator != null && animator.isStarted()) {
                this.circleRevealAnimator.end();
            }
            int cx = (this.containerImageView.getWidth() * 3) / 4;
            int cy = (this.containerImageView.getHeight() * 3) / 4;
            this.circleRevealAnimator = ViewAnimationUtils.createCircularReveal(this.containerImageView, cx, cy, 0.0f, (float) Math.hypot((double) cx, (double) cy));
            this.circleRevealAnimator.setDuration((long) this.animationDuration);
            this.circleRevealAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    InstallStateOverlayHelper.this.bannerContainer.removeView(InstallStateOverlayHelper.this.overlay);
                }
            });
            this.containerImageView.setVisibility(0);
            if (this.containerImageView.getAnimation() != null) {
                this.containerImageView.getAnimation().cancel();
            }
            this.circleRevealAnimator.start();
        } else if (this.isShowingBanner || !this.containerImageView.isAttachedToWindow()) {
            this.bannerContainer.removeView(this.overlay);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration((long) this.animationDuration);
            alphaAnimation.setFillAfter(true);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    InstallStateOverlayHelper.this.bannerContainer.removeView(InstallStateOverlayHelper.this.overlay);
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            cancelCurrentAnimationAndStartAnimation(this.overlay, alphaAnimation);
        }
    }

    public void setDimmed(boolean dimmed) {
        if (dimmed) {
            float f = this.dimmedFactorValue;
            this.currentDimmingFactor = f;
            this.bannerView.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, f));
            return;
        }
        this.currentDimmingFactor = 0.0f;
        this.bannerView.setColorFilter((ColorFilter) null);
    }

    /* access modifiers changed from: package-private */
    public ImageView getBannerView() {
        return this.bannerView;
    }

    /* access modifiers changed from: package-private */
    public float getDimmingFactor() {
        return this.currentDimmingFactor;
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        if (this.overlay.getAnimation() != null) {
            this.overlay.getAnimation().cancel();
        }
        if (this.bannerView.getAnimation() != null) {
            this.bannerView.getAnimation().cancel();
        }
        if (this.containerImageView.getAnimation() != null) {
            this.containerImageView.getAnimation().cancel();
        }
        Animator animator = this.circleRevealAnimator;
        if (animator != null) {
            animator.end();
        }
    }

    /* access modifiers changed from: private */
    public void cancelCurrentAnimationAndStartAnimation(View v, Animation animation) {
        if (v.getAnimation() != null) {
            v.getAnimation().cancel();
        }
        v.startAnimation(animation);
    }

    private void addOverlay() {
        this.bannerContainer.addView(this.overlay, 0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration((long) this.animationDuration);
        alphaAnimation.setFillAfter(true);
        cancelCurrentAnimationAndStartAnimation(this.overlay, alphaAnimation);
    }

    /* access modifiers changed from: package-private */
    public boolean isOverlayOnBannerContainer() {
        FrameLayout frameLayout = this.bannerContainer;
        return frameLayout != null && frameLayout.indexOfChild(this.overlay) >= 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isProgressBarIndeterminate() {
        return this.progressBar.isIndeterminate();
    }

    /* access modifiers changed from: package-private */
    public int getPercentage() {
        return this.progressBar.getProgress();
    }

    /* access modifiers changed from: package-private */
    public CharSequence getTitle() {
        return this.titleView.getText();
    }
}
