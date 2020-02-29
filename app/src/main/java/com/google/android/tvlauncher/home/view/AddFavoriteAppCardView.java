package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.graphics.ColorFilter;
import android.support.p001v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.ColorUtils;

public class AddFavoriteAppCardView extends LinearLayout implements FavoriteLaunchItemView {
    private ImageView bannerImage;
    private float bannerImageCurrentDimmingFactor;
    private float bannerImageDimmedFactorValue;
    private TextView titleView;
    private int titleVisibility;

    public AddFavoriteAppCardView(Context context) {
        this(context, null);
    }

    public AddFavoriteAppCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddFavoriteAppCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AddFavoriteAppCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.titleView = (TextView) findViewById(C1167R.C1170id.app_title);
        this.titleVisibility = this.titleView.getVisibility();
        this.bannerImage = (ImageView) findViewById(C1167R.C1170id.banner_image);
        this.bannerImageDimmedFactorValue = Util.getFloat(getResources(), C1167R.dimen.unfocused_channel_dimming_factor);
    }

    public TextView getTitleView() {
        return this.titleView;
    }

    public int getTitleVisibility() {
        return this.titleVisibility;
    }

    public void setTitleVisibility(int titleVisibility2) {
        this.titleVisibility = titleVisibility2;
        this.titleView.setVisibility(titleVisibility2);
    }

    public ImageView getBannerImage() {
        return this.bannerImage;
    }

    public void setBannerImageDimmed(boolean dimmed) {
        if (dimmed) {
            this.bannerImageCurrentDimmingFactor = this.bannerImageDimmedFactorValue;
            this.bannerImage.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, this.bannerImageCurrentDimmingFactor));
            return;
        }
        this.bannerImageCurrentDimmingFactor = 0.0f;
        this.bannerImage.setColorFilter((ColorFilter) null);
    }

    public float getBannerImageDimmingFactor() {
        return this.bannerImageCurrentDimmingFactor;
    }
}
