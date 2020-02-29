package com.google.android.tvlauncher.appsview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.support.p001v4.content.ContextCompat;
import android.support.p001v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.home.view.FavoriteLaunchItemView;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.ColorUtils;

public class BannerView extends LinearLayout implements FavoriteLaunchItemView {
    private ObjectAnimator animBlink;
    private FrameLayout bannerContainer;
    private ImageView bannerImage;
    private float bannerImageCurrentDimmingFactor;
    private float bannerImageDimmedFactorValue;
    /* access modifiers changed from: private */
    public int bannerImageHeight;
    /* access modifiers changed from: private */
    public final int cornerRadius;
    private boolean defaultScaleAnimationsEnabled;
    private float focusedScale;
    private float focusedZDelta;
    private View frame;
    private boolean isBeingEdited;
    private LaunchItem item;
    private OnEditModeFocusSearchCallback onEditModeFocusSearchCallback;
    private OnWindowVisibilityChangedListener onWindowVisibilityChangedListener;
    private InstallStateOverlayHelper overlayHelper;
    private int scaleAnimDuration;
    /* access modifiers changed from: private */
    public TextView titleView;
    private int titleVisibility;
    private int unselectedTint;

    public interface OnWindowVisibilityChangedListener {
        void onWindowVisibilityChanged(int i);
    }

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.defaultScaleAnimationsEnabled = true;
        Resources res = getResources();
        this.cornerRadius = res.getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius);
        this.unselectedTint = ContextCompat.getColor(getContext(), C1167R.color.app_banner_image_unselected_tint);
        this.focusedZDelta = res.getDimension(C1167R.dimen.app_banner_selected_item_z_delta);
        this.focusedScale = res.getFraction(C1167R.fraction.app_banner_focused_scale, 1, 1);
        this.scaleAnimDuration = res.getInteger(C1167R.integer.banner_scale_anim_duration);
        this.bannerImageDimmedFactorValue = Util.getFloat(getResources(), C1167R.dimen.unfocused_channel_dimming_factor);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.bannerImage = (ImageView) findViewById(C1167R.C1170id.banner_image);
        this.bannerContainer = (FrameLayout) findViewById(C1167R.C1170id.banner_container);
        this.titleView = (TextView) findViewById(C1167R.C1170id.app_title);
        this.titleVisibility = this.titleView.getVisibility();
        this.bannerImageHeight = this.bannerContainer.getLayoutParams().height;
        this.bannerContainer.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) BannerView.this.cornerRadius);
            }
        });
        this.bannerContainer.setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), BannerView.this.bannerImageHeight, (float) BannerView.this.cornerRadius);
            }
        });
        this.frame = findViewById(C1167R.C1170id.edit_focused_frame);
        this.animBlink = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.edit_focused_frame_blink);
        this.animBlink.setTarget(this.frame);
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        OnWindowVisibilityChangedListener onWindowVisibilityChangedListener2 = this.onWindowVisibilityChangedListener;
        if (onWindowVisibilityChangedListener2 != null) {
            onWindowVisibilityChangedListener2.onWindowVisibilityChanged(visibility);
        }
    }

    public View focusSearch(int direction) {
        OnEditModeFocusSearchCallback onEditModeFocusSearchCallback2 = this.onEditModeFocusSearchCallback;
        if (onEditModeFocusSearchCallback2 == null) {
            return super.focusSearch(direction);
        }
        return onEditModeFocusSearchCallback2.onEditModeFocusSearch(direction, super.focusSearch(direction));
    }

    public void setLaunchItem(LaunchItem item2) {
        this.item = item2;
        CharSequence title = item2.getLabel();
        if (!TextUtils.equals(title, this.titleView.getText())) {
            this.titleView.setText(title);
        }
        this.bannerImage.setContentDescription(item2.getLabel());
        if (item2.isInstalling()) {
            if (this.overlayHelper == null) {
                this.overlayHelper = new InstallStateOverlayHelper(this);
            }
            this.overlayHelper.updateOverlay(item2);
            this.bannerImage.setVisibility(4);
            this.frame.bringToFront();
            return;
        }
        InstallStateOverlayHelper installStateOverlayHelper = this.overlayHelper;
        if (installStateOverlayHelper != null) {
            installStateOverlayHelper.removeOverlay();
            this.bannerImage.setVisibility(0);
        }
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.equals(title, this.titleView.getText())) {
            this.titleView.setText(title);
        }
    }

    public void setDimmingEnabled(boolean dimmingEnabled) {
        if (dimmingEnabled) {
            this.bannerImage.setColorFilter(this.unselectedTint);
        } else {
            this.bannerImage.clearColorFilter();
        }
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (this.defaultScaleAnimationsEnabled) {
            float scale = selected ? this.focusedScale : 1.0f;
            float elevation = selected ? this.focusedZDelta : 0.0f;
            setDimmingEnabled(!selected);
            animate().z(elevation).scaleX(scale).scaleY(scale).setDuration((long) this.scaleAnimDuration);
        }
    }

    public void setFocusedState(boolean isFocused) {
        if (this.defaultScaleAnimationsEnabled) {
            float destinationAlpha = 1.0f;
            float scale = isFocused ? this.focusedScale : 1.0f;
            float elevation = isFocused ? this.focusedZDelta : 0.0f;
            if (!isFocused) {
                destinationAlpha = 0.0f;
            }
            this.titleView.setSelected(isFocused);
            ObjectAnimator bannerX = ObjectAnimator.ofFloat(this, View.SCALE_X, scale);
            ObjectAnimator bannerY = ObjectAnimator.ofFloat(this, View.SCALE_Y, scale);
            ObjectAnimator bannerZ = ObjectAnimator.ofFloat(this, View.TRANSLATION_Z, elevation);
            ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(this.titleView, View.ALPHA, destinationAlpha);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(bannerX, bannerY, bannerZ, titleAlpha);
            animSet.setDuration((long) this.scaleAnimDuration);
            animSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    BannerView.this.titleView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animation) {
                    if (BannerView.this.titleView.getAlpha() == 0.0f) {
                        BannerView.this.titleView.setVisibility(4);
                    }
                }
            });
            animSet.start();
        }
    }

    public void setOnEditModeFocusSearchCallback(OnEditModeFocusSearchCallback listener) {
        this.onEditModeFocusSearchCallback = listener;
    }

    public void setOnWindowVisibilityChangedListener(OnWindowVisibilityChangedListener listener) {
        this.onWindowVisibilityChangedListener = listener;
    }

    public void setIsBeingEdited(boolean isBeingEdited2) {
        if (isBeingEdited2) {
            this.animBlink.start();
            this.frame.setVisibility(0);
        } else {
            this.animBlink.cancel();
            this.frame.setVisibility(8);
        }
        this.isBeingEdited = isBeingEdited2;
    }

    public void setDefaultScaleAnimationsEnabled(boolean enable) {
        this.defaultScaleAnimationsEnabled = enable;
    }

    public boolean isBeingEdited() {
        return this.isBeingEdited;
    }

    public LaunchItem getItem() {
        return this.item;
    }

    public ImageView getBannerImage() {
        return this.bannerImage;
    }

    public FrameLayout getBannerContainer() {
        return this.bannerContainer;
    }

    public void setBannerImageDimmed(boolean dimmed) {
        if (this.overlayHelper != null && this.item.isInstalling()) {
            this.overlayHelper.setDimmed(dimmed);
        }
        if (dimmed) {
            float f = this.bannerImageDimmedFactorValue;
            this.bannerImageCurrentDimmingFactor = f;
            this.bannerImage.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, f));
            return;
        }
        this.bannerImageCurrentDimmingFactor = 0.0f;
        this.bannerImage.setColorFilter((ColorFilter) null);
    }

    public float getBannerImageDimmingFactor() {
        return this.bannerImageCurrentDimmingFactor;
    }

    public ImageView getOverlayBannerView() {
        if (this.overlayHelper == null || !this.item.isInstalling()) {
            return null;
        }
        return this.overlayHelper.getBannerView();
    }

    public float getOverlayBannerViewDimmingFactor() {
        return this.overlayHelper.getDimmingFactor();
    }

    public int getCornerRadius() {
        return this.cornerRadius;
    }

    public void resetAnimations(boolean isFocused) {
        float destinationAlpha = 1.0f;
        float scale = isFocused ? this.focusedScale : 1.0f;
        float elevation = isFocused ? this.focusedZDelta : 0.0f;
        if (!isFocused) {
            destinationAlpha = 0.0f;
        }
        setTranslationZ(elevation);
        setScaleX(scale);
        setScaleY(scale);
        this.titleView.setSelected(isFocused);
        this.titleView.setAlpha(destinationAlpha);
    }

    public TextView getTitleView() {
        return this.titleView;
    }

    public int getTitleVisibility() {
        return this.titleVisibility;
    }

    public void setTitleVisibility(int titleVisibility2) {
        this.titleVisibility = titleVisibility2;
        this.titleView.setVisibility(titleVisibility2);
    }

    public void recycle() {
        InstallStateOverlayHelper installStateOverlayHelper = this.overlayHelper;
        if (installStateOverlayHelper != null) {
            installStateOverlayHelper.recycle();
            this.overlayHelper.removeOverlay();
        }
        if (this.bannerImage.getAnimation() != null) {
            this.bannerImage.getAnimation().cancel();
        }
    }
}
