package com.google.android.tvlauncher.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Outline;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.ContextMenu;
import com.google.android.tvlauncher.util.ContextMenuItem;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccessibilityContextMenu {
    private final Activity activity;
    /* access modifiers changed from: private */
    public final SparseArray<View> contextItemViews = new SparseArray<>();
    /* access modifiers changed from: private */
    public final LinkedHashMap<Integer, ContextMenuItem> contextItems = new LinkedHashMap<>();
    /* access modifiers changed from: private */
    public boolean isShowing;
    private final LinearLayout menuContainer;
    /* access modifiers changed from: private */
    public final int menuItemCornerRadius;
    /* access modifiers changed from: private */
    public final int menuItemDisabledColor;
    /* access modifiers changed from: private */
    public final int menuItemEnabledColor;
    private final int menuItemHeight;
    private final int menuItemMarginEnd;
    private final int menuItemWidth;
    /* access modifiers changed from: private */
    public ContextMenu.OnDismissListener onDismissListener;
    /* access modifiers changed from: private */
    public ContextMenu.OnItemClickListener onItemClickListener;
    private View.OnFocusChangeListener onItemViewFocusChangeListener = new View.OnFocusChangeListener(this) {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setAccessibilityLiveRegion(1);
            } else {
                v.setAccessibilityLiveRegion(0);
            }
        }
    };
    private ContextMenuItem.OnMenuItemChangedListener onMenuItemChangedListener;
    private final PopupWindow popupWindow;
    private final ViewOutlineProvider viewOutlineProvider;

    public AccessibilityContextMenu(Activity activity2) {
        this.activity = activity2;
        Resources res = this.activity.getResources();
        this.menuContainer = (LinearLayout) ((LayoutInflater) this.activity.getSystemService("layout_inflater")).inflate(C1167R.layout.accessibility_context_menu_container, (ViewGroup) null);
        this.popupWindow = new PopupWindow(this.menuContainer, -1, -2);
        this.popupWindow.setFocusable(true);
        this.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                boolean unused = AccessibilityContextMenu.this.isShowing = false;
                if (AccessibilityContextMenu.this.onDismissListener != null) {
                    AccessibilityContextMenu.this.onDismissListener.onDismiss();
                }
            }
        });
        this.menuItemHeight = res.getDimensionPixelSize(C1167R.dimen.accessibility_context_menu_item_height);
        this.menuItemWidth = res.getDimensionPixelSize(C1167R.dimen.accessibility_context_menu_item_width);
        this.menuItemMarginEnd = res.getDimensionPixelSize(C1167R.dimen.accessibility_context_menu_item_margin_end);
        this.menuItemCornerRadius = res.getDimensionPixelSize(C1167R.dimen.accessibility_context_menu_item_corner_radius);
        this.menuItemEnabledColor = this.activity.getColor(C1167R.color.accessibility_context_menu_background_enabled_color);
        this.menuItemDisabledColor = this.activity.getColor(C1167R.color.accessibility_context_menu_background_disabled_color);
        this.viewOutlineProvider = new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) AccessibilityContextMenu.this.menuItemCornerRadius);
            }
        };
        this.onMenuItemChangedListener = new ContextMenuItem.OnMenuItemChangedListener() {
            public void onMenuItemChanged(ContextMenuItem contextMenuItem) {
                View itemView = (View) AccessibilityContextMenu.this.contextItemViews.get(contextMenuItem.getId());
                if (itemView != null) {
                    itemView.setEnabled(contextMenuItem.isEnabled());
                    itemView.setBackgroundColor(contextMenuItem.isEnabled() ? AccessibilityContextMenu.this.menuItemEnabledColor : AccessibilityContextMenu.this.menuItemDisabledColor);
                }
            }
        };
    }

    public void show() {
        LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService("layout_inflater");
        this.menuContainer.removeAllViews();
        for (Map.Entry<Integer, ContextMenuItem> entry : this.contextItems.entrySet()) {
            ContextMenuItem item = (ContextMenuItem) entry.getValue();
            item.setOnMenuItemChangedListener(this.onMenuItemChangedListener);
            LinearLayout itemView = (LinearLayout) layoutInflater.inflate(C1167R.layout.accessibility_context_menu_item, (ViewGroup) null);
            setUpMenuItemView(item, itemView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.menuItemWidth, this.menuItemHeight);
            params.setMarginEnd(this.menuItemMarginEnd);
            this.contextItemViews.put(item.getId(), itemView);
            this.menuContainer.addView(itemView, params);
        }
        this.menuContainer.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(AccessibilityContextMenu.this.contextItems.size(), 0, false));
            }
        });
        this.popupWindow.showAtLocation(this.activity.getWindow().getDecorView().getRootView(), 80, 0, 0);
        this.isShowing = true;
    }

    public void addItem(ContextMenuItem item) {
        this.contextItems.put(Integer.valueOf(item.getId()), item);
    }

    public void setOnMenuItemClickListener(ContextMenu.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ContextMenu.OnItemClickListener getOnItemClickListener() {
        return this.onItemClickListener;
    }

    public void setOnDismissListener(ContextMenu.OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public void dismiss() {
        this.popupWindow.dismiss();
    }

    public boolean isShowing() {
        return this.isShowing;
    }

    public ContextMenuItem findItem(int menuId) {
        return this.contextItems.get(Integer.valueOf(menuId));
    }

    private void setUpMenuItemView(final ContextMenuItem item, View view) {
        view.setOutlineProvider(this.viewOutlineProvider);
        view.setClipToOutline(true);
        ((TextView) view.findViewById(C1167R.C1170id.title)).setText(item.getTitle());
        ImageView iconView = (ImageView) view.findViewById(C1167R.C1170id.icon);
        iconView.setImageTintList(view.getContext().getResources().getColorStateList(C1167R.color.context_menu_icon_enabled_color, null));
        iconView.setImageDrawable(item.getIcon());
        view.setEnabled(item.isEnabled());
        view.setBackgroundColor(item.isEnabled() ? this.menuItemEnabledColor : this.menuItemDisabledColor);
        view.setOnFocusChangeListener(this.onItemViewFocusChangeListener);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (item.isEnabled() && AccessibilityContextMenu.this.onItemClickListener != null) {
                    AccessibilityContextMenu.this.onItemClickListener.onItemClick(item);
                }
            }
        });
    }
}
