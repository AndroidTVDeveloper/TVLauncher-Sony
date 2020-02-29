package com.google.android.tvlauncher.home.operatorbackground;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;

public class BitmapHomeBackgroundController {
    private static final int TRANSITION_DURATION_MILLIS = 600;
    private final String backgroundUri;
    private final Context context;
    private final int optimalHeight;
    private final int optimalWidth;
    /* access modifiers changed from: private */
    public final BitmapBackgroundTransitionDrawable transitionDrawable = new BitmapBackgroundTransitionDrawable();

    public BitmapHomeBackgroundController(String backgroundUri2, Context context2) {
        this.context = context2;
        this.backgroundUri = backgroundUri2;
        this.optimalWidth = context2.getResources().getDimensionPixelSize(C1167R.dimen.home_background_image_optimal_width);
        this.optimalHeight = context2.getResources().getDimensionPixelSize(C1167R.dimen.home_background_image_optimal_height);
    }

    public void loadInto(View backgroundView) {
        backgroundView.setBackground(this.transitionDrawable);
        loadBitmap();
    }

    private void loadBitmap() {
        Transformation[] transformationArr = {new CenterCrop(), new AddScrimTransformation(this.context), new CropBackgroundBitmapTransformation(this.context)};
        Glide.with(this.context).asBitmap().load(this.backgroundUri).apply((BaseRequestOptions<?>) ((RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().override(this.optimalWidth, this.optimalHeight)).downsample(DownsampleStrategy.AT_LEAST)).transform(new MultiTransformation(transformationArr))).skipMemoryCache(true)).diskCacheStrategy(DiskCacheStrategy.RESOURCE))).into(new SimpleTarget<Bitmap>() {
            public /* bridge */ /* synthetic */ void onResourceReady(Object obj, Transition transition) {
                onResourceReady((Bitmap) obj, (Transition<? super Bitmap>) transition);
            }

            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                BitmapHomeBackgroundController.this.transitionDrawable.animateFadeIn(resource, 600);
            }
        });
    }
}
