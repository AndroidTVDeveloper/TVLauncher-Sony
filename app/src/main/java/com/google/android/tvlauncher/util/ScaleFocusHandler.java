package com.google.android.tvlauncher.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScaleFocusHandler implements View.OnFocusChangeListener {
    public static final int FOCUS_DELAY_MILLIS = 60;
    public static final int PIVOT_CENTER = 0;
    public static final int PIVOT_START = 1;
    private final int animationDuration;
    private AnimatorSet animator;
    private Animator.AnimatorListener animatorListener;
    private Runnable delayedFocusRunnable;
    private Runnable delayedUnfocusRunnable;
    private final float focusedElevation;
    private float focusedScale;
    private View.OnFocusChangeListener onFocusChangeListener;
    private int pivot;
    private PivotProvider pivotProvider;
    private int pivotVerticalShift;
    /* access modifiers changed from: private */
    public View view;

    @Retention(RetentionPolicy.SOURCE)
    @interface Pivot {
    }

    public interface PivotProvider {
        int getPivot();

        boolean shouldAnimate();
    }

    public ScaleFocusHandler(int animationDuration2, float scale, float elevation) {
        this(animationDuration2, scale, elevation, 0);
    }

    public ScaleFocusHandler(int animationDuration2, float scale, float elevation, int pivot2) {
        this.pivot = 0;
        this.delayedFocusRunnable = new Runnable() {
            public void run() {
                if (ScaleFocusHandler.this.view.isFocused()) {
                    ScaleFocusHandler.this.animateFocusedState(true);
                }
            }
        };
        this.delayedUnfocusRunnable = new Runnable() {
            public void run() {
                if (!ScaleFocusHandler.this.view.isFocused()) {
                    ScaleFocusHandler.this.animateFocusedState(false);
                }
            }
        };
        this.animationDuration = animationDuration2;
        this.focusedScale = scale;
        this.focusedElevation = elevation;
        this.pivot = pivot2;
    }

    public ScaleFocusHandler(ScaleFocusHandler handler) {
        this(handler.animationDuration, handler.focusedScale, handler.focusedElevation, handler.pivot);
    }

    public void setView(View view2) {
        this.view = view2;
        this.view.setOnFocusChangeListener(this);
    }

    public void setFocusedScale(float focusedScale2) {
        this.focusedScale = focusedScale2;
    }

    public void setPivot(int pivot2) {
        this.pivot = pivot2;
    }

    public void setPivotVerticalShift(int pivotVerticalShift2) {
        this.pivotVerticalShift = pivotVerticalShift2;
    }

    public void setOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener2) {
        this.onFocusChangeListener = onFocusChangeListener2;
    }

    public void setPivotProvider(PivotProvider pivotProvider2) {
        this.pivotProvider = pivotProvider2;
    }

    public void resetFocusedState() {
        releaseAnimator();
        float scale = this.view.isFocused() ? this.focusedScale : 1.0f;
        float elevation = this.view.isFocused() ? this.focusedElevation : 0.0f;
        applyPivot();
        this.view.setScaleX(scale);
        this.view.setScaleY(scale);
        this.view.setTranslationZ(elevation);
    }

    public int getAnimationDuration() {
        return this.animationDuration;
    }

    private void cancelAnimation() {
        AnimatorSet animatorSet = this.animator;
        if (animatorSet != null) {
            animatorSet.cancel();
            releaseAnimator();
        }
    }

    /* access modifiers changed from: private */
    public void releaseAnimator() {
        AnimatorSet animatorSet = this.animator;
        if (animatorSet != null) {
            Animator.AnimatorListener animatorListener2 = this.animatorListener;
            if (animatorListener2 != null) {
                animatorSet.removeListener(animatorListener2);
            }
            this.animator = null;
        }
        this.animatorListener = null;
    }

    public void onFocusChange(View v, boolean hasFocus) {
        v.removeCallbacks(this.delayedFocusRunnable);
        v.removeCallbacks(this.delayedUnfocusRunnable);
        v.postDelayed(hasFocus ? this.delayedFocusRunnable : this.delayedUnfocusRunnable, 60);
        View.OnFocusChangeListener onFocusChangeListener2 = this.onFocusChangeListener;
        if (onFocusChangeListener2 != null) {
            onFocusChangeListener2.onFocusChange(v, hasFocus);
        }
    }

    public void animateFocusedState(boolean hasFocus) {
        cancelAnimation();
        float beforePivotX = this.view.getPivotX();
        applyPivot();
        boolean animatePivot = false;
        PivotProvider pivotProvider2 = this.pivotProvider;
        if (pivotProvider2 != null) {
            animatePivot = pivotProvider2.shouldAnimate();
        }
        ObjectAnimator pivotAnimator = null;
        if (animatePivot) {
            View view2 = this.view;
            pivotAnimator = ObjectAnimator.ofFloat(view2, "pivotX", beforePivotX, view2.getPivotX());
        }
        float scale = hasFocus ? this.focusedScale : 1.0f;
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(this.view, View.TRANSLATION_Z, hasFocus ? this.focusedElevation : 0.0f);
        elevationAnimator.setDuration((long) this.animationDuration);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(this.view, View.SCALE_X, scale);
        scaleXAnimator.setDuration((long) this.animationDuration);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(this.view, View.SCALE_Y, scale);
        scaleYAnimator.setDuration((long) this.animationDuration);
        this.animator = new AnimatorSet();
        if (pivotAnimator != null) {
            this.animator.playTogether(elevationAnimator, scaleXAnimator, scaleYAnimator, pivotAnimator);
        } else {
            this.animator.playTogether(elevationAnimator, scaleXAnimator, scaleYAnimator);
        }
        this.animatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ScaleFocusHandler.this.releaseAnimator();
            }
        };
        this.animator.addListener(this.animatorListener);
        this.animator.start();
    }

    private void applyPivot() {
        int width = this.view.getLayoutParams().width;
        int height = this.view.getLayoutParams().height;
        int pivotX = 0;
        if (width <= 0 || height <= 0) {
            width = this.view.getWidth();
            height = this.view.getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
        }
        PivotProvider pivotProvider2 = this.pivotProvider;
        if (pivotProvider2 != null) {
            this.pivot = pivotProvider2.getPivot();
        }
        int i = this.pivot;
        if (i == 0) {
            pivotX = width / 2;
        } else if (i == 1) {
            if (this.view.getLayoutDirection() == 1) {
                pivotX = width;
            } else {
                pivotX = 0;
            }
        }
        this.view.setPivotX((float) pivotX);
        this.view.setPivotY((float) ((height / 2) + this.pivotVerticalShift));
    }
}
