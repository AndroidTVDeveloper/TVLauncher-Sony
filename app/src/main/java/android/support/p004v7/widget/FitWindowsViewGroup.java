package android.support.p004v7.widget;

import android.graphics.Rect;

/* renamed from: android.support.v7.widget.FitWindowsViewGroup */
public interface FitWindowsViewGroup {

    /* renamed from: android.support.v7.widget.FitWindowsViewGroup$OnFitSystemWindowsListener */
    interface OnFitSystemWindowsListener {
        void onFitSystemWindows(Rect rect);
    }

    void setOnFitSystemWindowsListener(OnFitSystemWindowsListener onFitSystemWindowsListener);
}
