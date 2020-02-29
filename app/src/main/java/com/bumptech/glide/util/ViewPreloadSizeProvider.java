package com.bumptech.glide.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import java.util.Arrays;

public class ViewPreloadSizeProvider<T> implements ListPreloader.PreloadSizeProvider<T>, SizeReadyCallback {
    private int[] size;
    private SizeViewTarget viewTarget;

    public ViewPreloadSizeProvider() {
    }

    public ViewPreloadSizeProvider(View view) {
        this.viewTarget = new SizeViewTarget(view);
        this.viewTarget.getSize(this);
    }

    public int[] getPreloadSize(T t, int adapterPosition, int itemPosition) {
        int[] iArr = this.size;
        if (iArr == null) {
            return null;
        }
        return Arrays.copyOf(iArr, iArr.length);
    }

    public void onSizeReady(int width, int height) {
        this.size = new int[]{width, height};
        this.viewTarget = null;
    }

    public void setView(View view) {
        if (this.size == null && this.viewTarget == null) {
            this.viewTarget = new SizeViewTarget(view);
            this.viewTarget.getSize(this);
        }
    }

    static final class SizeViewTarget extends CustomViewTarget<View, Object> {
        SizeViewTarget(View view) {
            super(view);
        }

        /* access modifiers changed from: protected */
        public void onResourceCleared(Drawable placeholder) {
        }

        public void onLoadFailed(Drawable errorDrawable) {
        }

        public void onResourceReady(Object resource, Transition<? super Object> transition) {
        }
    }
}
