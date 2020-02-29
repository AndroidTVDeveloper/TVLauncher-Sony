package com.google.android.tvlauncher.appsview;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.p001v4.content.ContextCompat;
import android.support.p004v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.data.AppLinksDataManager;
import com.google.android.tvlauncher.util.IntentUtil;
import com.google.android.tvlauncher.util.OemPromotionApp;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class PromotionRowAdapter extends RecyclerView.Adapter<PromotionViewHolder> {
    /* access modifiers changed from: private */
    public RequestOptions bannerRequestOptions;
    /* access modifiers changed from: private */
    public OnAppsViewActionListener onAppsViewActionListener;
    private List<OemPromotionApp> promotions = new ArrayList();

    PromotionRowAdapter(Context context) {
        Drawable placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.bannerRequestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().placeholder(placeholderBanner)).error(placeholderBanner)).diskCacheStrategy(DiskCacheStrategy.NONE);
    }

    class PromotionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener {
        private final BannerView bannerView;
        private OemPromotionApp promotion;

        PromotionViewHolder(View itemView) {
            super(itemView);
            this.bannerView = (BannerView) itemView;
            this.bannerView.setOnClickListener(this);
            this.bannerView.setOnFocusChangeListener(this);
        }

        /* access modifiers changed from: package-private */
        public void setPromotion(OemPromotionApp promotion2) {
            this.promotion = promotion2;
            BannerView bannerView2 = this.bannerView;
            bannerView2.resetAnimations(bannerView2.isFocused());
            this.bannerView.setTitle(promotion2.getAppName());
            Glide.with(this.bannerView.getContext()).setDefaultRequestOptions(PromotionRowAdapter.this.bannerRequestOptions).load(promotion2.getBannerUri()).into(new ImageViewTarget<Drawable>(this, this.bannerView.getBannerImage()) {
                public /* bridge */ /* synthetic */ void onResourceReady(Object obj, Transition transition) {
                    onResourceReady((Drawable) obj, (Transition<? super Drawable>) transition);
                }

                /* access modifiers changed from: protected */
                public void setResource(Drawable resource) {
                    setDrawable(resource);
                }

                /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
                 method: com.bumptech.glide.request.target.ImageViewTarget.onResourceReady(java.lang.Object, com.bumptech.glide.request.transition.Transition):void
                 arg types: [android.graphics.drawable.Drawable, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable>]
                 candidates:
                  com.google.android.tvlauncher.appsview.PromotionRowAdapter.PromotionViewHolder.1.onResourceReady(android.graphics.drawable.Drawable, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable>):void
                  com.bumptech.glide.request.target.ImageViewTarget.onResourceReady(java.lang.Object, com.bumptech.glide.request.transition.Transition):void */
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (!(resource instanceof BitmapDrawable) || !((BitmapDrawable) resource).getBitmap().hasAlpha()) {
                        ((ImageView) this.view).setBackground(null);
                    } else {
                        ((ImageView) this.view).setBackgroundColor(ContextCompat.getColor(((ImageView) this.view).getContext(), C1167R.color.app_banner_background_color));
                    }
                    super.onResourceReady((Object) resource, (Transition) transition);
                }
            });
        }

        public void onClick(View v) {
            Intent intent;
            Context context = this.itemView.getContext();
            if (this.promotion.isVirtualApp()) {
                OemPromotionApp appLink = (OemPromotionApp) AppLinksDataManager.getInstance(context).getAppLink(this.promotion.getId());
                if (appLink == null) {
                    intent = new Intent(Constants.ACTION_ADD_APP_LINK);
                    intent.putExtra(Constants.EXTRA_APP_NAME, this.promotion.getAppName());
                    intent.putExtra(Constants.EXTRA_PACKAGE_NAME, this.promotion.getPackageName());
                    intent.putExtra(Constants.EXTRA_BANNER_URI, this.promotion.getBannerUri());
                    intent.putExtra(Constants.EXTRA_DATA_URI, this.promotion.getDataUri());
                    intent.putExtra(Constants.EXTRA_DEVELOPER, this.promotion.getDeveloper());
                    intent.putExtra(Constants.EXTRA_CATEGORY, this.promotion.getCategory());
                    intent.putExtra(Constants.EXTRA_DESCRIPTION, this.promotion.getDescription());
                    intent.putExtra(Constants.EXTRA_IS_GAME, this.promotion.isGame());
                    List<String> screenshots = this.promotion.getScreenshotUris();
                    if (screenshots.size() > 0) {
                        intent.putExtra(Constants.EXTRA_SCREENSHOTS, (String[]) screenshots.toArray(new String[screenshots.size()]));
                    }
                } else {
                    intent = IntentUtil.createVirtualAppIntent(appLink.getPackageName(), appLink.getDataUri());
                }
            } else {
                intent = this.itemView.getContext().getPackageManager().getLeanbackLaunchIntentForPackage(this.promotion.getPackageName());
                if (intent != null) {
                    intent.addFlags(270532608);
                } else {
                    intent = new Intent("android.intent.action.VIEW");
                    String valueOf = String.valueOf(this.promotion.getPackageName());
                    intent.setData(Uri.parse(valueOf.length() != 0 ? "market://details?id=".concat(valueOf) : new String("market://details?id=")));
                }
            }
            PromotionRowAdapter.this.onAppsViewActionListener.onLaunchApp(intent, this.itemView);
        }

        public void onFocusChange(View v, boolean hasFocus) {
            this.bannerView.setFocusedState(hasFocus);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PromotionViewHolder(LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_app_banner, parent, false));
    }

    public void onBindViewHolder(PromotionViewHolder holder, int position) {
        holder.setPromotion(this.promotions.get(position));
    }

    public int getItemCount() {
        return this.promotions.size();
    }

    public long getItemId(int position) {
        return (long) this.promotions.get(position).getId().hashCode();
    }

    /* access modifiers changed from: package-private */
    public void setOnAppsViewActionListener(OnAppsViewActionListener listener) {
        this.onAppsViewActionListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setPromotions(List<OemPromotionApp> promotions2) {
        this.promotions = promotions2;
        notifyDataSetChanged();
    }
}
