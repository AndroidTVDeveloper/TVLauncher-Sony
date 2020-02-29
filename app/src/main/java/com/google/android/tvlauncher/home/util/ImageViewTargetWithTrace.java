package com.google.android.tvlauncher.home.util;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.trace.AppTrace;

public abstract class ImageViewTargetWithTrace<T> extends ImageViewTarget<T> {
    private final String traceSection;
    private AppTrace.TraceTag traceTag;

    public ImageViewTargetWithTrace(ImageView view, String traceSection2) {
        super(view);
        this.traceSection = traceSection2;
    }

    public void setRequest(Request request) {
        this.traceTag = AppTrace.beginAsyncSection(this.traceSection);
        super.setRequest(request);
    }

    public void onResourceReady(T resource, Transition<? super T> transition) {
        super.onResourceReady(resource, transition);
        AppTrace.TraceTag traceTag2 = this.traceTag;
        if (traceTag2 != null) {
            AppTrace.endAsyncSection(traceTag2);
            this.traceTag = null;
        }
    }

    public void onLoadFailed(Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        AppTrace.TraceTag traceTag2 = this.traceTag;
        if (traceTag2 != null) {
            AppTrace.endAsyncSection(traceTag2);
            this.traceTag = null;
        }
    }
}
