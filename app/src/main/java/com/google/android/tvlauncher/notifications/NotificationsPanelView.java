package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.support.p004v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.leanback.widget.VerticalGridView;

public class NotificationsPanelView extends VerticalGridView {
    private ViewTreeObserver.OnGlobalFocusChangeListener focusChangeListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            if (NotificationsPanelView.this.listener != null) {
                NotificationsPanelView.this.listener.onFocusChanged();
            }
        }
    };
    /* access modifiers changed from: private */
    public OnFocusChangedListener listener;
    private final RecyclerView.ItemAnimator.ItemAnimatorFinishedListener onAnimationsFinishedListener = new RecyclerView.ItemAnimator.ItemAnimatorFinishedListener() {
        public void onAnimationsFinished() {
            if (!NotificationsPanelView.this.isComputingLayout() && NotificationsPanelView.this.isLayoutFrozen()) {
                NotificationsPanelView.this.setLayoutFrozen(false);
                NotificationsPanelView.this.requestLayout();
            }
        }
    };

    public interface OnFocusChangedListener {
        void onFocusChanged();
    }

    public NotificationsPanelView(Context context) {
        super(context);
        init();
    }

    public NotificationsPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationsPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setHasFixedSize(false);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        RecyclerView.ItemAnimator animator = getItemAnimator();
        if (animator != null && animator.isRunning()) {
            animator.isRunning(this.onAnimationsFinishedListener);
            setLayoutFrozen(true);
        } else if (isLayoutFrozen()) {
            setLayoutFrozen(false);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(this.focusChangeListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalFocusChangeListener(this.focusChangeListener);
    }

    public void setOnFocusChangedListener(OnFocusChangedListener listener2) {
        this.listener = listener2;
    }
}
