package com.google.android.tvlauncher.widget;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class PartnerWidgetInfo {
    public static final String[] PROJECTION = {"icon", "title", "action"};
    private Drawable icon;
    private String intentUri;
    private String title;

    public PartnerWidgetInfo(Drawable icon2, String title2, String intentUri2) {
        this.title = title2;
        this.intentUri = intentUri2;
        this.icon = icon2;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIntent() {
        return this.intentUri;
    }

    public boolean isComplete() {
        return (this.icon == null || TextUtils.isEmpty(this.title) || this.intentUri == null) ? false : true;
    }
}
