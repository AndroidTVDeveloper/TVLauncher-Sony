package com.google.android.tvlauncher.home;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.support.p001v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;

public class HomeTopRowButton extends LinearLayout {
    private final AnimatorSet animateIn;
    private final AnimatorSet animateOut;
    private final int animationDuration;
    private View.OnFocusChangeListener focusChangeListener;
    /* access modifiers changed from: private */
    public ImageView icon;
    private final int iconFocusedColor;
    private final int iconUnfocusedColor;
    /* access modifiers changed from: private */
    public View indicator;
    /* access modifiers changed from: private */
    public final int indicatorFocusedColor;
    /* access modifiers changed from: private */
    public TextView title;

    public HomeTopRowButton(Context context) {
        this(context, null);
    }

    public HomeTopRowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.focusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    HomeTopRowButton.this.animateIn();
                } else {
                    HomeTopRowButton.this.animateOut();
                }
            }
        };
        Resources res = getResources();
        this.indicatorFocusedColor = res.getColor(C1167R.color.reference_white_100, null);
        this.iconUnfocusedColor = res.getColor(C1167R.color.reference_white_60, null);
        this.iconFocusedColor = ViewCompat.MEASURED_STATE_MASK;
        this.animationDuration = res.getInteger(C1167R.integer.top_row_button_animation_duration_ms);
        this.animateIn = new AnimatorSet();
        this.animateOut = new AnimatorSet();
    }

    private void setUpAnimations() {
        ValueAnimator iconUnfocused = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.iconFocusedColor), Integer.valueOf(this.iconUnfocusedColor));
        iconUnfocused.setDuration((long) this.animationDuration);
        iconUnfocused.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (HomeTopRowButton.this.icon != null && HomeTopRowButton.this.icon.getDrawable() != null) {
                    HomeTopRowButton.this.icon.getDrawable().setTint(((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            }
        });
        ValueAnimator iconFocused = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.iconUnfocusedColor), Integer.valueOf(this.iconFocusedColor));
        iconFocused.setDuration((long) this.animationDuration);
        iconFocused.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (HomeTopRowButton.this.icon != null && HomeTopRowButton.this.icon.getDrawable() != null) {
                    HomeTopRowButton.this.icon.getDrawable().setTint(((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            }
        });
        ObjectAnimator textFadeIn = ObjectAnimator.ofFloat(this.title, "alpha", 0.0f, 1.0f);
        textFadeIn.setDuration((long) this.animationDuration);
        textFadeIn.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                HomeTopRowButton.this.title.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        AnimatorSet expandFadeIn = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.expand_fade_in);
        expandFadeIn.setTarget(this.indicator);
        this.animateIn.playTogether(expandFadeIn, iconFocused, textFadeIn);
        this.animateIn.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                HomeTopRowButton.this.indicator.setBackgroundColor(HomeTopRowButton.this.indicatorFocusedColor);
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        AnimatorSet shrinkFadeOut = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.shrink_fade_out);
        shrinkFadeOut.setTarget(this.indicator);
        this.animateOut.playTogether(shrinkFadeOut, iconUnfocused);
        this.animateOut.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                HomeTopRowButton.this.indicator.setBackgroundResource(0);
            }

            public void onAnimationCancel(Animator animation) {
                HomeTopRowButton.this.indicator.setBackgroundResource(0);
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.icon = (ImageView) findViewById(C1167R.C1170id.button_icon);
        this.title = (TextView) findViewById(C1167R.C1170id.button_title);
        this.indicator = findViewById(C1167R.C1170id.button_background);
        this.indicator.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        });
        this.indicator.setClipToOutline(true);
        setUpAnimations();
        setOnFocusChangeListener(this.focusChangeListener);
    }

    public void setIcon(int id) {
        ImageView imageView = this.icon;
        if (imageView != null) {
            imageView.setImageResource(id);
            if (hasFocus()) {
                this.icon.getDrawable().setTint(this.iconFocusedColor);
            }
        }
    }

    public void setIcon(Drawable drawable) {
        ImageView imageView = this.icon;
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
            if (hasFocus()) {
                drawable.setTint(this.iconFocusedColor);
            }
        }
    }

    public void setText(int id) {
        TextView textView = this.title;
        if (textView != null) {
            textView.setText(id);
        }
    }

    public void setText(String text) {
        TextView textView = this.title;
        if (textView != null) {
            textView.setText(text);
        }
    }

    /* access modifiers changed from: private */
    public void animateIn() {
        this.animateOut.cancel();
        this.animateIn.start();
        this.title.setSelected(true);
    }

    /* access modifiers changed from: private */
    public void animateOut() {
        this.animateIn.cancel();
        this.animateOut.start();
        this.title.setVisibility(8);
        this.title.setSelected(false);
    }

    public Drawable getIcon() {
        ImageView imageView = this.icon;
        if (imageView != null) {
            return imageView.getDrawable();
        }
        return null;
    }

    public String getTitle() {
        TextView textView = this.title;
        if (textView != null) {
            return textView.getText().toString();
        }
        return null;
    }

    public ImageView getIconImageView() {
        return this.icon;
    }
}
