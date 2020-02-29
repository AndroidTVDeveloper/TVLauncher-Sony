package com.google.android.tvlauncher.util;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class ContextMenuItem {
    private boolean autoDismiss = true;
    private boolean enabled;
    private Drawable icon;

    /* renamed from: id */
    private int f162id;
    private boolean isLinkedWithTriangle;
    private OnMenuItemChangedListener onMenuItemChangedListener;
    private String title;
    private boolean visible;

    interface OnMenuItemChangedListener {
        void onMenuItemChanged(ContextMenuItem contextMenuItem);
    }

    public ContextMenuItem(int id, String title2, Drawable icon2) {
        this.f162id = id;
        this.title = title2;
        this.icon = icon2;
        this.enabled = true;
        this.visible = true;
        this.isLinkedWithTriangle = false;
    }

    public int getId() {
        return this.f162id;
    }

    public String getTitle() {
        return this.title;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setTitle(String title2) {
        if (!TextUtils.equals(this.title, title2)) {
            this.title = title2;
            notifyMenuItemChanged();
        }
    }

    public void setIcon(Drawable icon2) {
        if (this.icon != icon2) {
            this.icon = icon2;
            notifyMenuItemChanged();
        }
    }

    public void setEnabled(boolean isEnabled) {
        if (this.enabled != isEnabled) {
            this.enabled = isEnabled;
            notifyMenuItemChanged();
        }
    }

    public void setVisible(boolean isVisible) {
        if (this.visible != isVisible) {
            this.visible = isVisible;
            notifyMenuItemChanged();
        }
    }

    public void setAutoDismiss(boolean autoDismiss2) {
        this.autoDismiss = autoDismiss2;
    }

    /* access modifiers changed from: package-private */
    public boolean isLinkedWithTriangle() {
        return this.isLinkedWithTriangle;
    }

    /* access modifiers changed from: package-private */
    public boolean isAutoDismiss() {
        return this.autoDismiss;
    }

    /* access modifiers changed from: package-private */
    public void setLinkedWithTriangle(boolean linkedWithTriangle) {
        this.isLinkedWithTriangle = linkedWithTriangle;
    }

    /* access modifiers changed from: package-private */
    public void setOnMenuItemChangedListener(OnMenuItemChangedListener onMenuItemChangedListener2) {
        this.onMenuItemChangedListener = onMenuItemChangedListener2;
    }

    private void notifyMenuItemChanged() {
        OnMenuItemChangedListener onMenuItemChangedListener2 = this.onMenuItemChangedListener;
        if (onMenuItemChangedListener2 != null) {
            onMenuItemChangedListener2.onMenuItemChanged(this);
        }
    }
}
