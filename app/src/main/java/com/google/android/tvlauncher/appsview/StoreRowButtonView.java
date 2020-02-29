package com.google.android.tvlauncher.appsview;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.support.p001v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.C1167R;

public class StoreRowButtonView extends LinearLayout implements View.OnClickListener, View.OnFocusChangeListener {
    private final int animDuration;
    /* access modifiers changed from: private */
    public final int cornerRadius;
    private final int elevation;
    private final int focusedFillColor;
    private final float focusedScale;
    private OnAppsViewActionListener onAppsViewActionListener;
    private ImageView storeIconView;
    private LaunchItem storeItem;
    private TextView storeTitleView;
    private final int unfocusedFillColor;
    private VisualElementTag visualElementTag;

    public StoreRowButtonView(Context context) {
        this(context, null);
    }

    public StoreRowButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoreRowButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StoreRowButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.focusedFillColor = ContextCompat.getColor(getContext(), C1167R.color.store_button_focused_fill);
        this.unfocusedFillColor = ContextCompat.getColor(getContext(), C1167R.color.store_button_unfocused_fill);
        Resources res = getResources();
        this.focusedScale = res.getFraction(C1167R.fraction.store_button_focused_scale, 1, 1);
        this.animDuration = res.getInteger(C1167R.integer.banner_scale_anim_duration);
        this.cornerRadius = res.getDimensionPixelSize(C1167R.dimen.store_button_rounded_corner_radius);
        this.elevation = res.getDimensionPixelSize(C1167R.dimen.store_button_z);
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, StoreRowButtonView.this.getWidth(), StoreRowButtonView.this.getHeight(), (float) StoreRowButtonView.this.cornerRadius);
            }
        });
        setOnClickListener(this);
        setClipToOutline(true);
        setOnFocusChangeListener(this);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.storeIconView = (ImageView) findViewById(C1167R.C1170id.store_icon);
        this.storeTitleView = (TextView) findViewById(C1167R.C1170id.store_title);
    }

    public ImageView getStoreIconView() {
        return this.storeIconView;
    }

    public void setStoreItem(LaunchItem item, OnAppsViewActionListener listener) {
        this.storeItem = item;
        this.onAppsViewActionListener = listener;
    }

    public void setStoreTitle(String title) {
        if (!TextUtils.equals(title, this.storeTitleView.getText())) {
            this.storeTitleView.setText(title);
        }
    }

    public void onClick(View view) {
        this.onAppsViewActionListener.onStoreLaunch(this.storeItem.getIntent(), this.visualElementTag, view);
    }

    public void onFocusChange(View v, boolean hasFocus) {
        float scale = hasFocus ? this.focusedScale : 1.0f;
        int colorFrom = hasFocus ? this.unfocusedFillColor : this.focusedFillColor;
        int colorTo = hasFocus ? this.focusedFillColor : this.unfocusedFillColor;
        int elevationTo = hasFocus ? this.elevation : 0;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(colorFrom), Integer.valueOf(colorTo));
        colorAnimation.setDuration((long) this.animDuration);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animator) {
                StoreRowButtonView.this.setBackgroundColor(((Integer) animator.getAnimatedValue()).intValue());
            }
        });
        colorAnimation.start();
        v.animate().scaleX(scale).scaleY(scale).translationZ((float) elevationTo).setDuration((long) this.animDuration);
        this.storeTitleView.setSelected(hasFocus);
    }

    public void setVisualElementTag(VisualElementTag visualElementTag2) {
        this.visualElementTag = visualElementTag2;
    }
}
