package com.google.android.tvlauncher.analytics;

import android.support.p004v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.leanback.widget.VerticalGridView;

public class TvHomeDrawnManager {
    private static final String TAG = "TvHomeDrawnManager";
    private static TvHomeDrawnManager manager;
    private int firstVisibleHomeItemsCount;
    private OnFullyDrawnListener fullyDrawnListener;
    private int homeRowDrawnCount;
    /* access modifiers changed from: private */
    public boolean isMonitoringEnabled = true;

    public interface OnFullyDrawnListener {
        void onFullyDrawn();
    }

    /* access modifiers changed from: private */
    public static boolean areRecyclerViewItemsBound(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() != 0) {
            return isViewVisible(recyclerView);
        }
        return true;
    }

    private static boolean isViewVisible(RecyclerView view) {
        int childCount = view.getChildCount();
        for (int pos = 0; pos < childCount; pos++) {
            if (view.getChildAt(pos).getVisibility() == 0) {
                return true;
            }
        }
        return false;
    }

    public static TvHomeDrawnManager getInstance() {
        if (manager == null) {
            manager = new TvHomeDrawnManager();
        }
        return manager;
    }

    private TvHomeDrawnManager() {
        if (manager != null) {
            throw new RuntimeException("Use getInstance() method to get single instance.");
        }
    }

    public void setFullyDrawnListener(OnFullyDrawnListener listener) {
        this.fullyDrawnListener = listener;
    }

    public void removeFullyDrawnListener() {
        this.fullyDrawnListener = null;
    }

    private void fireFullyDrawnListener() {
        OnFullyDrawnListener onFullyDrawnListener = this.fullyDrawnListener;
        if (onFullyDrawnListener != null) {
            onFullyDrawnListener.onFullyDrawn();
            removeFullyDrawnListener();
        }
    }

    /* access modifiers changed from: private */
    public void notifyHomeRowsFrameDrawn(int visibleItems) {
        this.firstVisibleHomeItemsCount = visibleItems;
    }

    /* access modifiers changed from: private */
    public void notifyHomeRowDrawn() {
        this.homeRowDrawnCount++;
        if (this.homeRowDrawnCount >= this.firstVisibleHomeItemsCount && this.fullyDrawnListener != null) {
            this.isMonitoringEnabled = false;
            fireFullyDrawnListener();
        }
    }

    public void monitorHomeListViewDrawn(final VerticalGridView homeListView) {
        if (this.isMonitoringEnabled) {
            homeListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (TvHomeDrawnManager.areRecyclerViewItemsBound(homeListView)) {
                        TvHomeDrawnManager.this.notifyHomeRowsFrameDrawn(homeListView.getChildCount());
                        homeListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    public void monitorChannelItemListViewDrawn(final RecyclerView recyclerView) {
        if (this.isMonitoringEnabled) {
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (!TvHomeDrawnManager.this.isMonitoringEnabled) {
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (TvHomeDrawnManager.areRecyclerViewItemsBound(recyclerView)) {
                        TvHomeDrawnManager.this.notifyHomeRowDrawn();
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    public void monitorViewLayoutDrawn(final View view) {
        if (this.isMonitoringEnabled) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (!TvHomeDrawnManager.this.isMonitoringEnabled) {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (view.getVisibility() == 0) {
                        TvHomeDrawnManager.this.notifyHomeRowDrawn();
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }
}
