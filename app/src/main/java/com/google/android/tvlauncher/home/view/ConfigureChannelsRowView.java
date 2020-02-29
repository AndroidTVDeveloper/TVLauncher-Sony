package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;

public class ConfigureChannelsRowView extends LinearLayout {
    private TextView button;
    private TextView descriptionView;
    private int descriptionVisibility;

    public ConfigureChannelsRowView(Context context) {
        super(context);
    }

    public ConfigureChannelsRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConfigureChannelsRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.button = (TextView) findViewById(C1167R.C1170id.button);
        this.descriptionView = (TextView) findViewById(C1167R.C1170id.description_text);
        this.descriptionVisibility = this.descriptionView.getVisibility();
        final int cornerRadius = getResources().getDimensionPixelSize(C1167R.dimen.home_configure_channels_button_corner_radius);
        this.button.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) cornerRadius);
            }
        });
        this.button.setClipToOutline(true);
        TvHomeDrawnManager.getInstance().monitorViewLayoutDrawn(this);
    }

    public TextView getButton() {
        return this.button;
    }

    /* access modifiers changed from: package-private */
    public View getDescriptionView() {
        return this.descriptionView;
    }

    /* access modifiers changed from: package-private */
    public int getDescriptionVisibility() {
        return this.descriptionVisibility;
    }

    public void setDescriptionVisibility(int descriptionVisibility2) {
        this.descriptionVisibility = descriptionVisibility2;
        this.descriptionView.setVisibility(descriptionVisibility2);
    }
}
