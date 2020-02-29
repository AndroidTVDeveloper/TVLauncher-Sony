package com.google.android.tvlauncher.home;

import android.os.Handler;
import android.support.p004v7.widget.RecyclerView;
import android.view.KeyEvent;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.VerticalGridView;
import com.google.android.tvlauncher.util.Util;

class RecyclerViewFastScrollingManager {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_EXIT_FAST_SCROLLING_DELAY_MS = 450;
    private static final int DPAD_EVENTS_COUNT_FOR_FAST_SCROLLING = 1;
    private static final String TAG = "RVFastScrollingHelper";
    private boolean animatorEnabled;
    private boolean animatorInitialized;
    /* access modifiers changed from: private */
    public int exitFastScrollingDelayMs = 450;
    /* access modifiers changed from: private */
    public final Runnable exitFastScrollingRunnable = new RecyclerViewFastScrollingManager$$Lambda$0(this);
    /* access modifiers changed from: private */
    public boolean fastScrollingEnabled;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    private RecyclerView.ItemAnimator itemAnimator;
    /* access modifiers changed from: private */
    public int keyCodeNext;
    /* access modifiers changed from: private */
    public int keyCodePrevious;
    /* access modifiers changed from: private */
    public BaseGridView list;
    private OnFastScrollingChangedListener listener;
    private final BaseGridView.OnUnhandledKeyListener onUnhandledKeyListener = new BaseGridView.OnUnhandledKeyListener() {
        public boolean onUnhandledKey(KeyEvent event) {
            if (event.getKeyCode() != RecyclerViewFastScrollingManager.this.keyCodePrevious && event.getKeyCode() != RecyclerViewFastScrollingManager.this.keyCodeNext) {
                return false;
            }
            if (!RecyclerViewFastScrollingManager.this.fastScrollingEnabled && event.getAction() == 0 && event.getRepeatCount() >= 1 && !Util.isAccessibilityEnabled(RecyclerViewFastScrollingManager.this.list.getContext())) {
                RecyclerViewFastScrollingManager.this.handler.removeCallbacks(RecyclerViewFastScrollingManager.this.exitFastScrollingRunnable);
                RecyclerViewFastScrollingManager.this.setFastScrollingEnabled(true);
                return false;
            } else if (!RecyclerViewFastScrollingManager.this.fastScrollingEnabled || event.getAction() != 1) {
                return false;
            } else {
                RecyclerViewFastScrollingManager.this.handler.removeCallbacks(RecyclerViewFastScrollingManager.this.exitFastScrollingRunnable);
                RecyclerViewFastScrollingManager.this.handler.postDelayed(RecyclerViewFastScrollingManager.this.exitFastScrollingRunnable, (long) RecyclerViewFastScrollingManager.this.exitFastScrollingDelayMs);
                return false;
            }
        }
    };
    private boolean scrollAllowedDuringFastScrolling = true;
    private boolean scrollEnabled;
    private boolean scrollInitialized;

    public interface OnFastScrollingChangedListener {
        void onFastScrollingChanged(boolean z);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$RecyclerViewFastScrollingManager() {
        setFastScrollingEnabled(false);
    }

    RecyclerViewFastScrollingManager(BaseGridView list2, RecyclerView.ItemAnimator itemAnimator2) {
        this.itemAnimator = itemAnimator2;
        this.list = list2;
        if (list2 instanceof HorizontalGridView) {
            this.keyCodePrevious = 21;
            this.keyCodeNext = 22;
        } else if (list2 instanceof VerticalGridView) {
            this.keyCodePrevious = 20;
            this.keyCodeNext = 19;
        } else {
            throw new IllegalArgumentException("Provided list must be a HorizontalGridView or a VerticalGridView");
        }
        this.list.setOnUnhandledKeyListener(this.onUnhandledKeyListener);
    }

    /* access modifiers changed from: package-private */
    public void setOnFastScrollingChangedListener(OnFastScrollingChangedListener listener2) {
        this.listener = listener2;
    }

    /* access modifiers changed from: package-private */
    public void setExitFastScrollingDelayMs(int delayMs) {
        this.exitFastScrollingDelayMs = delayMs;
    }

    /* access modifiers changed from: package-private */
    public boolean isFastScrollingEnabled() {
        return this.fastScrollingEnabled;
    }

    /* access modifiers changed from: private */
    public void setFastScrollingEnabled(boolean enabled) {
        this.fastScrollingEnabled = enabled;
        boolean z = true;
        setAnimatorEnabled(!this.fastScrollingEnabled);
        if (!this.scrollAllowedDuringFastScrolling || !this.fastScrollingEnabled) {
            z = false;
        }
        setScrollEnabled(z);
        OnFastScrollingChangedListener onFastScrollingChangedListener = this.listener;
        if (onFastScrollingChangedListener != null) {
            onFastScrollingChangedListener.onFastScrollingChanged(this.fastScrollingEnabled);
        }
    }

    public boolean isScrollAllowedDuringFastScrolling() {
        return this.scrollAllowedDuringFastScrolling;
    }

    /* access modifiers changed from: package-private */
    public void setScrollAllowedDuringFastScrolling(boolean allowed) {
        this.scrollAllowedDuringFastScrolling = allowed;
    }

    /* access modifiers changed from: package-private */
    public void setAnimatorEnabled(boolean enabled) {
        if (!Util.areHomeScreenAnimationsEnabled(this.list.getContext())) {
            enabled = false;
        }
        if (this.animatorEnabled != enabled || !this.animatorInitialized) {
            this.animatorEnabled = enabled;
            this.animatorInitialized = true;
            if (this.animatorEnabled) {
                this.list.setItemAnimator(this.itemAnimator);
            } else {
                this.list.setItemAnimator(null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setScrollEnabled(boolean enabled) {
        if (!Util.areHomeScreenAnimationsEnabled(this.list.getContext())) {
            enabled = true;
        }
        if (this.scrollEnabled != enabled || !this.scrollInitialized) {
            this.scrollEnabled = enabled;
            this.scrollInitialized = true;
            this.list.setScrollEnabled(this.scrollEnabled);
        }
    }
}
