package com.google.android.tvrecommendations.shared.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Rect;
import android.util.FloatProperty;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class AnimUtil {
    private static final BoundsProperty BOUNDS_PROPERTY = new BoundsProperty();
    private static final Interpolator DELAYED_APPEARANCE_INTERPOLATOR = new AccelerateInterpolator(2.0f);
    private static final float EPS = 0.001f;
    private static final Interpolator FAST_DISAPPEARANCE_INTERPOLATOR = new DecelerateInterpolator(3.0f);
    private static final PivotXProperty PIVOT_X_PROPERTY = new PivotXProperty();
    public static final ScaleProperty SCALE_PROPERTY = new ScaleProperty();

    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    private static void setInterpolatorIfNotNull(Animator animator, Interpolator interpolator) {
        if (animator != null && interpolator != null) {
            animator.setInterpolator(interpolator);
        }
    }

    public static Animator createBoundsAnimator(View view, Rect preBounds, Rect postBounds, Interpolator interpolator) {
        if (preBounds.equals(postBounds)) {
            return null;
        }
        Animator animator = ObjectAnimator.ofObject(view, BOUNDS_PROPERTY, new DeceleratingAnimationRectEvaluator(), preBounds, postBounds);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createScaleAnimator(View view, Rect from, Rect to, float current, Interpolator interpolator) {
        return createScaleAnimator(view, from, to, current, 0.0f, 0.0f, interpolator);
    }

    public static Animator createScaleAnimator(View view, Rect from, Rect to, float current, float pivotX, float pivotY, Interpolator interpolator) {
        float fromWidth = ((float) from.width()) * current;
        if (from.width() <= 0 || to.width() <= 0 || Math.abs(fromWidth - ((float) to.width())) <= EPS) {
            return null;
        }
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
        Animator animator = ObjectAnimator.ofFloat(view, SCALE_PROPERTY, fromWidth / ((float) to.width()), 1.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createScaleAnimator(View view, float startScale, float endScale, Interpolator interpolator) {
        if (startScale == endScale) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, SCALE_PROPERTY, startScale, endScale);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createScaleXAnimator(View view, int from, int to, float current, Interpolator interpolator) {
        float fromWidth = ((float) from) * current;
        if (from <= 0 || to <= 0 || Math.abs(fromWidth - ((float) to)) <= EPS) {
            return null;
        }
        view.setPivotX(0.0f);
        Animator animator = ObjectAnimator.ofFloat(view, View.SCALE_X, fromWidth / ((float) to), 1.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createScaleYAnimator(View view, int from, int to, float current, Interpolator interpolator) {
        float fromHeight = ((float) from) * current;
        if (from <= 0 || to <= 0 || Math.abs(fromHeight - ((float) to)) <= EPS) {
            return null;
        }
        view.setPivotY(0.0f);
        Animator animator = ObjectAnimator.ofFloat(view, View.SCALE_Y, fromHeight / ((float) to), 1.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createPivotXAnimator(View view, float from, float to, Interpolator interpolator) {
        if (Math.abs(from - to) <= EPS) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, PIVOT_X_PROPERTY, from, to);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createTranslationXAnimator(View view, Rect from, Rect to, float translation, Interpolator interpolator) {
        return createTranslationPropertyAnimator(view, (float) from.left, (float) to.left, translation, View.TRANSLATION_X, interpolator);
    }

    public static Animator createTranslationXAnimator(View view, int from, int to, float translation, Interpolator interpolator) {
        return createTranslationPropertyAnimator(view, (float) from, (float) to, translation, View.TRANSLATION_X, interpolator);
    }

    public static Animator createTranslationYAnimator(View view, Rect from, Rect to, float translation, Interpolator interpolator) {
        return createTranslationPropertyAnimator(view, (float) from.top, (float) to.top, translation, View.TRANSLATION_Y, interpolator);
    }

    public static Animator createTranslationYAnimator(View view, int from, int to, float translation, Interpolator interpolator) {
        return createTranslationPropertyAnimator(view, (float) from, (float) to, translation, View.TRANSLATION_Y, interpolator);
    }

    public static Animator createTranslationXYAnimator(View view, int fromX, int fromY, int toX, int toY, float translationX, float translationY, Interpolator interpolator) {
        Animator animatorX = createTranslationPropertyAnimator(view, (float) fromX, (float) toX, translationX, View.TRANSLATION_X, interpolator);
        Animator animatorY = createTranslationPropertyAnimator(view, (float) fromY, (float) toY, translationY, View.TRANSLATION_Y, interpolator);
        if (animatorX == null) {
            return animatorY;
        }
        if (animatorY == null) {
            return animatorX;
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        return set;
    }

    public static Animator createTranslationZAnimator(View view, float from, float to, float translation, Interpolator interpolator) {
        return createTranslationPropertyAnimator(view, from, to, translation, View.TRANSLATION_Z, interpolator);
    }

    public static Animator createTranslationPropertyAnimator(View view, float from, float to, float translation, Property<View, Float> property, Interpolator interpolator) {
        float value = (from - to) + translation;
        if (value == 0.0f) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, property, value, 0.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createVisibilityAnimator(final View view, int preVisibility, int postVisibility, float startAlpha, Interpolator interpolator) {
        if (postVisibility != 0 && (preVisibility == 0 || startAlpha != 1.0f)) {
            Animator anim = ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, 0.0f);
            setInterpolatorIfNotNull(anim, interpolator);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(0);
                    animation.removeListener(this);
                }
            });
            return anim;
        } else if (postVisibility != 0) {
            return null;
        } else {
            if (preVisibility == 0 && startAlpha == 1.0f) {
                return null;
            }
            if (startAlpha == 1.0f) {
                startAlpha = 0.0f;
            }
            Animator anim2 = ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, 1.0f);
            setInterpolatorIfNotNull(anim2, interpolator);
            anim2.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(0);
                    animation.removeListener(this);
                }
            });
            return anim2;
        }
    }

    public static Animator createFastVisibilityAnimator(View view, int preVisibility, int postVisibility, float startAlpha) {
        Interpolator interpolator;
        Animator anim = createVisibilityAnimator(view, preVisibility, postVisibility, startAlpha, null);
        if (anim != null) {
            if (postVisibility == 0) {
                interpolator = DELAYED_APPEARANCE_INTERPOLATOR;
            } else {
                interpolator = FAST_DISAPPEARANCE_INTERPOLATOR;
            }
            anim.setInterpolator(interpolator);
        }
        return anim;
    }

    public static Animator createAlphaAnimator(View view, float fromAlpha, float toAlpha, Interpolator interpolator) {
        Animator anim = ObjectAnimator.ofFloat(view, View.ALPHA, fromAlpha, toAlpha);
        setInterpolatorIfNotNull(anim, interpolator);
        return anim;
    }

    public static Animator createTextColorAnimator(TextView view, int from, int to, Interpolator interpolator) {
        if (from == to) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, new TextColorProperty(from, to), 0.0f, 1.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createColorFilterAnimator(ImageView view, int color, float fromAlpha, float toAlpha, Interpolator interpolator) {
        if (Math.abs(fromAlpha - toAlpha) <= EPS) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, new ColorFilterProperty(color), fromAlpha, toAlpha);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Animator createBackgroundColorAnimator(View view, int from, int to, Interpolator interpolator) {
        if (from == to) {
            return null;
        }
        Animator animator = ObjectAnimator.ofFloat(view, new BackgroundColorProperty(from, to), 0.0f, 1.0f);
        setInterpolatorIfNotNull(animator, interpolator);
        return animator;
    }

    public static Rect getBounds(View view) {
        return new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    public static void setBounds(View view, Rect bounds) {
        view.offsetLeftAndRight(bounds.left - view.getLeft());
        view.offsetTopAndBottom(bounds.top - view.getTop());
        view.setRight(bounds.right);
        view.setBottom(bounds.bottom);
    }

    public static void addIfNotNull(List<Animator> animators, Animator animator) {
        if (animator != null) {
            animators.add(animator);
        }
    }

    private static class ScaleProperty extends FloatProperty<View> {
        private ScaleProperty() {
            super("scale");
        }

        public void setValue(View view, float value) {
            view.setScaleX(value);
            view.setScaleY(value);
        }

        public Float get(View view) {
            return Float.valueOf(view.getScaleX());
        }
    }

    private static class PivotXProperty extends FloatProperty<View> {
        private PivotXProperty() {
            super("pivotX");
        }

        public Float get(View view) {
            return Float.valueOf(view.getPivotX());
        }

        public void setValue(View view, float value) {
            view.setPivotX(value);
        }
    }

    private static class BoundsProperty extends Property<View, Rect> {
        private BoundsProperty() {
            super(Rect.class, "bounds");
        }

        public Rect get(View view) {
            return AnimUtil.getBounds(view);
        }

        public void set(View view, Rect value) {
            AnimUtil.setBounds(view, value);
        }
    }

    private static class DeceleratingAnimationRectEvaluator implements TypeEvaluator<Rect> {
        private Rect result;

        private DeceleratingAnimationRectEvaluator() {
            this.result = new Rect();
        }

        public Rect evaluate(float fraction, Rect start, Rect end) {
            float left = ((float) start.left) + (((float) (end.left - start.left)) * fraction);
            float right = ((float) start.right) + (((float) (end.right - start.right)) * fraction);
            float top = ((float) start.top) + (((float) (end.top - start.top)) * fraction);
            float bottom = ((float) start.bottom) + (((float) (end.bottom - start.bottom)) * fraction);
            if (fraction > 0.9f) {
                this.result.left = Math.round(left);
                this.result.right = Math.round(right);
                this.result.top = Math.round(top);
                this.result.bottom = Math.round(bottom);
            } else {
                Rect rect = this.result;
                rect.left = (int) left;
                rect.right = (int) right;
                rect.top = (int) top;
                rect.bottom = (int) bottom;
            }
            return this.result;
        }
    }

    private static class ColorFilterProperty extends FloatProperty<ImageView> {
        private int color;
        private float currentAlphaLevel;

        public ColorFilterProperty(int color2) {
            super("Color Filter");
            this.color = color2;
        }

        public void setValue(ImageView view, float value) {
            this.currentAlphaLevel = value;
            view.setColorFilter(ColorUtils.getColorFilter(this.color, value));
        }

        public Float get(ImageView object) {
            return Float.valueOf(this.currentAlphaLevel);
        }
    }

    private static class TextColorProperty extends ColorProperty<TextView> {
        public TextColorProperty(int startColor, int endColor) {
            super(startColor, endColor);
        }

        public void setValue(TextView view, float value) {
            this.currentFraction = value;
            view.setTextColor(calculateColor(value));
        }
    }

    private static class BackgroundColorProperty extends ColorProperty<View> {
        public BackgroundColorProperty(int startColor, int endColor) {
            super(startColor, endColor);
        }

        public void setValue(View view, float value) {
            this.currentFraction = value;
            view.setBackgroundColor(calculateColor(value));
        }
    }

    private static abstract class ColorProperty<T extends View> extends FloatProperty<T> {
        float currentFraction;
        float deltaA;
        float deltaB;
        float deltaG;
        float deltaR;
        float startA;
        float startB = ((float) Math.pow((double) this.startB, 2.2d));
        float startG = ((float) Math.pow((double) this.startG, 2.2d));
        float startR = ((float) Math.pow((double) this.startR, 2.2d));

        public ColorProperty(int startColor, int endColor) {
            super("Color");
            this.startA = ((float) ((startColor >> 24) & 255)) / 255.0f;
            this.startR = ((float) ((startColor >> 16) & 255)) / 255.0f;
            this.startG = ((float) ((startColor >> 8) & 255)) / 255.0f;
            this.startB = ((float) (startColor & 255)) / 255.0f;
            float endR = (float) Math.pow((double) (((float) ((endColor >> 16) & 255)) / 255.0f), 2.2d);
            float endG = (float) Math.pow((double) (((float) ((endColor >> 8) & 255)) / 255.0f), 2.2d);
            this.deltaA = (((float) ((endColor >> 24) & 255)) / 255.0f) - this.startA;
            this.deltaR = endR - this.startR;
            this.deltaG = endG - this.startG;
            this.deltaB = ((float) Math.pow((double) (((float) (endColor & 255)) / 255.0f), 2.2d)) - this.startB;
        }

        /* access modifiers changed from: protected */
        public int calculateColor(float fraction) {
            return (Math.round((this.startA + (this.deltaA * fraction)) * 255.0f) << 24) | (Math.round(((float) Math.pow((double) (this.startR + (this.deltaR * fraction)), 0.45454545454545453d)) * 255.0f) << 16) | (Math.round(((float) Math.pow((double) (this.startG + (this.deltaG * fraction)), 0.45454545454545453d)) * 255.0f) << 8) | Math.round(((float) Math.pow((double) (this.startB + (this.deltaB * fraction)), 0.45454545454545453d)) * 255.0f);
        }

        public Float get(T t) {
            return Float.valueOf(this.currentFraction);
        }
    }
}
