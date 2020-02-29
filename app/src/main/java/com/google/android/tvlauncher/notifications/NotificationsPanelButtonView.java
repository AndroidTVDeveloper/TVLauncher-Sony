package com.google.android.tvlauncher.notifications;

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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;

public class NotificationsPanelButtonView extends LinearLayout {
    private final int allNotifsSeenUnfocusedColor;
    /* access modifiers changed from: private */
    public AnimatorSet animateIn;
    /* access modifiers changed from: private */
    public AnimatorSet animateOut;
    private final int animationDuration;
    private ImageView circle;
    private float defaultTextSize;
    /* access modifiers changed from: private */
    public View focusIndicator;
    private final String greaterThanNineText;
    private float greaterThanNineTextSize;
    /* access modifiers changed from: private */
    public final int indicatorFocusedColor;
    /* access modifiers changed from: private */
    public TextView notifCountTextView;
    /* access modifiers changed from: private */
    public TextView notificationButtonTitle;
    private final int notifsFocusedBackgroundColor;
    private final String numberFormat;
    /* access modifiers changed from: private */
    public boolean seen;
    /* access modifiers changed from: private */
    public Drawable seenBackground;
    /* access modifiers changed from: private */
    public ValueAnimator seenIconColorFocusedAnimator;
    /* access modifiers changed from: private */
    public ValueAnimator seenIconColorUnfocusedAnimator;
    /* access modifiers changed from: private */
    public ObjectAnimator textFadeInAnimator;
    private Drawable unseenBackground;
    /* access modifiers changed from: private */
    public ObjectAnimator unseenIconColorFocusedAnimator;
    /* access modifiers changed from: private */
    public ObjectAnimator unseenIconColorUnfocusedAnimator;
    private int unseenNotifsUnfocusedBackgroundColor;
    /* access modifiers changed from: private */
    public ObjectAnimator unseenTextColorFocusedAnimator;
    /* access modifiers changed from: private */
    public ObjectAnimator unseenTextColorUnfocusedAnimator;
    private int unseenTextUnfocusedColor;

    public NotificationsPanelButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = context.getResources();
        this.allNotifsSeenUnfocusedColor = resources.getColor(C1167R.color.white_60, null);
        this.notifsFocusedBackgroundColor = resources.getColor(C1167R.color.notification_panel_icon_focused_color, null);
        this.unseenNotifsUnfocusedBackgroundColor = resources.getColor(C1167R.color.notification_panel_icon_unseen_color, null);
        this.indicatorFocusedColor = resources.getColor(C1167R.color.reference_white_100, null);
        this.unseenTextUnfocusedColor = resources.getColor(C1167R.color.notification_panel_icon_text_unfocused_color, null);
        this.animationDuration = resources.getInteger(C1167R.integer.top_row_button_animation_duration_ms);
        this.numberFormat = resources.getString(C1167R.string.number_format);
        this.greaterThanNineText = resources.getString(C1167R.string.greater_than_nine_notifs_text);
        this.defaultTextSize = resources.getDimension(C1167R.dimen.text_size_h4);
        this.greaterThanNineTextSize = resources.getDimension(C1167R.dimen.text_size_h5);
        this.seenBackground = resources.getDrawable(C1167R.C1168drawable.hollow_circle_background, null);
        this.seenBackground.setTint(this.allNotifsSeenUnfocusedColor);
        this.unseenBackground = resources.getDrawable(C1167R.C1168drawable.full_circle_background, null);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.focusIndicator = findViewById(C1167R.C1170id.button_background);
        this.circle = (ImageView) findViewById(C1167R.C1170id.notification_panel_background_circle);
        this.circle.setImageDrawable(this.seenBackground);
        this.notifCountTextView = (TextView) findViewById(C1167R.C1170id.notification_panel_count);
        this.notificationButtonTitle = (TextView) findViewById(C1167R.C1170id.button_title);
        this.notificationButtonTitle.setText(C1167R.string.notifications_panel_icon_title);
        this.focusIndicator.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        });
        this.focusIndicator.setClipToOutline(true);
        setUpAnimations();
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    NotificationsPanelButtonView.this.animateOut.cancel();
                    if (NotificationsPanelButtonView.this.seen) {
                        NotificationsPanelButtonView.this.seenIconColorFocusedAnimator.start();
                    } else {
                        NotificationsPanelButtonView.this.unseenIconColorFocusedAnimator.start();
                        NotificationsPanelButtonView.this.unseenTextColorFocusedAnimator.start();
                    }
                    NotificationsPanelButtonView.this.textFadeInAnimator.start();
                    NotificationsPanelButtonView.this.animateIn.start();
                } else {
                    NotificationsPanelButtonView.this.animateIn.cancel();
                    if (NotificationsPanelButtonView.this.seen) {
                        NotificationsPanelButtonView.this.seenIconColorUnfocusedAnimator.start();
                    } else {
                        NotificationsPanelButtonView.this.unseenIconColorUnfocusedAnimator.start();
                        NotificationsPanelButtonView.this.unseenTextColorUnfocusedAnimator.start();
                    }
                    NotificationsPanelButtonView.this.animateOut.start();
                    NotificationsPanelButtonView.this.notificationButtonTitle.setVisibility(8);
                }
                NotificationsPanelButtonView.this.notificationButtonTitle.setSelected(focused);
            }
        });
    }

    private void setUpAnimations() {
        this.textFadeInAnimator = ObjectAnimator.ofFloat(this.notificationButtonTitle, "alpha", 0.0f, 1.0f);
        this.textFadeInAnimator.setDuration((long) this.animationDuration);
        this.textFadeInAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                NotificationsPanelButtonView.this.notificationButtonTitle.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        this.seenIconColorFocusedAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.allNotifsSeenUnfocusedColor), Integer.valueOf(this.notifsFocusedBackgroundColor));
        this.seenIconColorFocusedAnimator.setDuration((long) this.animationDuration);
        this.seenIconColorFocusedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                NotificationsPanelButtonView.this.notifCountTextView.setTextColor(value);
                if (NotificationsPanelButtonView.this.seenBackground != null) {
                    NotificationsPanelButtonView.this.seenBackground.setTint(value);
                }
            }
        });
        this.seenIconColorUnfocusedAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.notifsFocusedBackgroundColor), Integer.valueOf(this.allNotifsSeenUnfocusedColor));
        this.seenIconColorUnfocusedAnimator.setDuration((long) this.animationDuration);
        this.seenIconColorUnfocusedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                NotificationsPanelButtonView.this.notifCountTextView.setTextColor(value);
                if (NotificationsPanelButtonView.this.seenBackground != null) {
                    NotificationsPanelButtonView.this.seenBackground.setTint(value);
                }
            }
        });
        this.unseenIconColorFocusedAnimator = ObjectAnimator.ofArgb(this.unseenBackground, "tint", this.unseenNotifsUnfocusedBackgroundColor, this.notifsFocusedBackgroundColor);
        this.unseenIconColorFocusedAnimator.setDuration((long) this.animationDuration);
        this.unseenIconColorUnfocusedAnimator = ObjectAnimator.ofArgb(this.unseenBackground, "tint", this.notifsFocusedBackgroundColor, this.unseenNotifsUnfocusedBackgroundColor);
        this.unseenIconColorUnfocusedAnimator.setDuration((long) this.animationDuration);
        this.unseenTextColorFocusedAnimator = ObjectAnimator.ofArgb(this.notifCountTextView, "textColor", this.unseenTextUnfocusedColor, this.indicatorFocusedColor);
        this.unseenTextColorFocusedAnimator.setDuration((long) this.animationDuration);
        this.unseenTextColorUnfocusedAnimator = ObjectAnimator.ofArgb(this.notifCountTextView, "textColor", this.indicatorFocusedColor, this.unseenTextUnfocusedColor);
        this.unseenTextColorUnfocusedAnimator.setDuration((long) this.animationDuration);
        this.animateIn = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.expand_fade_in);
        this.animateIn.setTarget(this.focusIndicator);
        this.animateIn.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                NotificationsPanelButtonView.this.focusIndicator.setBackgroundColor(NotificationsPanelButtonView.this.indicatorFocusedColor);
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        this.animateOut = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.shrink_fade_out);
        this.animateOut.setTarget(this.focusIndicator);
        this.animateOut.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                NotificationsPanelButtonView.this.focusIndicator.setBackgroundResource(0);
            }

            public void onAnimationCancel(Animator animation) {
                NotificationsPanelButtonView.this.focusIndicator.setBackgroundResource(0);
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    public void setCount(int count) {
        if (count > 9) {
            this.notifCountTextView.setTextSize(0, this.greaterThanNineTextSize);
            this.notifCountTextView.setText(this.greaterThanNineText);
        } else {
            this.notifCountTextView.setTextSize(0, this.defaultTextSize);
            this.notifCountTextView.setText(String.format(this.numberFormat, Integer.valueOf(count)));
        }
        setContentDescription(getResources().getQuantityString(C1167R.plurals.notification_panel_icon_accessibility_description, count, Integer.valueOf(count)));
    }

    public void setSeenState(boolean seen2) {
        this.seen = seen2;
        if (this.seen) {
            this.seenBackground.setTint(hasFocus() ? this.notifsFocusedBackgroundColor : this.allNotifsSeenUnfocusedColor);
            this.circle.setImageDrawable(this.seenBackground);
            this.notifCountTextView.setTextColor(hasFocus() ? this.notifsFocusedBackgroundColor : this.allNotifsSeenUnfocusedColor);
            return;
        }
        this.unseenBackground.setTint(hasFocus() ? this.notifsFocusedBackgroundColor : this.unseenNotifsUnfocusedBackgroundColor);
        this.circle.setImageDrawable(this.unseenBackground);
        this.notifCountTextView.setTextColor(hasFocus() ? this.indicatorFocusedColor : this.unseenTextUnfocusedColor);
    }

    public Drawable getUnseenBackground() {
        return this.unseenBackground;
    }

    public Drawable getSeenBackground() {
        return this.seenBackground;
    }
}
