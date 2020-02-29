package com.google.android.tvlauncher.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.p001v4.view.GravityCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import java.util.ArrayList;
import java.util.List;

public class ContextMenu {
    static final int BOTTOM_ALIGN = 1;
    private static final float FLOAT_COMPARISON_DELTA = 1.0E-4f;
    static final int SCROLL = 2;
    static final int TOP_ALIGN = 0;
    private Activity activity;
    /* access modifiers changed from: private */
    public View anchor;
    /* access modifiers changed from: private */
    public View.OnAttachStateChangeListener anchorOnAttachStateChangeListener;
    private float anchorRealHeight;
    private float anchorRealWidth;
    /* access modifiers changed from: private */
    public View.OnLayoutChangeListener anchorRootLayoutChangeListener;
    private float anchorX;
    private float anchorY;
    private List<ContextMenuItem> contextMenuItems;
    /* access modifiers changed from: private */
    public CutoutOverlayLayout cutoutOverlay;
    private float deltaX;
    private float deltaY;
    /* access modifiers changed from: private */
    public final int dimBackgroundColor;
    private final int disabledColor;
    private final int enabledColor;
    /* access modifiers changed from: private */
    public final int focusedColor;
    private int gravity;
    private int horizontalPosition;
    /* access modifiers changed from: private */
    public boolean isShowing;
    private FrameLayout menuContainer;
    private int menuHeight;
    private final int menuHeightPerRow;
    private LinearLayout menuLinearLayout;
    private final int menuVerticalMargin;
    private int menuWidth;
    /* access modifiers changed from: private */
    public OnDismissListener onDismissListener;
    /* access modifiers changed from: private */
    public OnItemClickListener onItemClickListener;
    /* access modifiers changed from: private */
    public ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
    private final float overlayAlpha;
    private ObjectAnimator overlayAnimator;
    private final int overlayDismissAnimationDuration;
    private final int overlayShowAnimationDuration;
    private final int overscanHorizontal;
    private final int overscanVertical;
    private PopupWindow popupWindow;
    /* access modifiers changed from: private */
    public ViewGroup rootParentWindow;
    /* access modifiers changed from: private */
    public ImageView triangle;
    private final int triangleEdgeOffset;
    private final int triangleHeight;
    private final int triangleVerticalMenuMargin;
    private final int triangleWidth;
    /* access modifiers changed from: private */
    public final int unfocusedColor;
    private int verticalPosition;
    /* access modifiers changed from: private */
    public List<ContextMenuItem> visibleItems;
    private List<View> visibleMenuItemViews;

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnItemClickListener {
        void onItemClick(ContextMenuItem contextMenuItem);
    }

    @interface VerticalPosition {
    }

    public boolean isShowing() {
        return this.isShowing;
    }

    /* access modifiers changed from: package-private */
    public List<View> getVisibleMenuItemViews() {
        return this.visibleMenuItemViews;
    }

    public List<ContextMenuItem> getContextMenuItems() {
        return this.contextMenuItems;
    }

    /* access modifiers changed from: package-private */
    public View.OnAttachStateChangeListener getAnchorOnAttachStateChangeListener() {
        return this.anchorOnAttachStateChangeListener;
    }

    /* access modifiers changed from: package-private */
    public ObjectAnimator getOverlayAnimator() {
        return this.overlayAnimator;
    }

    /* access modifiers changed from: package-private */
    public float getDeltaY() {
        return this.deltaY;
    }

    /* access modifiers changed from: package-private */
    public float getDeltaX() {
        return this.deltaX;
    }

    /* access modifiers changed from: package-private */
    public int getGravity() {
        return this.gravity;
    }

    /* access modifiers changed from: package-private */
    public CutoutOverlayLayout getCutoutOverlay() {
        return this.cutoutOverlay;
    }

    /* access modifiers changed from: package-private */
    public int getHorizontalPosition() {
        return this.horizontalPosition;
    }

    /* access modifiers changed from: package-private */
    public int getVerticalPosition() {
        return this.verticalPosition;
    }

    public ContextMenu(Activity activity2, View anchor2, int cornerRadius) {
        this(activity2, anchor2, cornerRadius, anchor2.getScaleX(), anchor2.getScaleY());
    }

    public ContextMenu(Activity activity2, View anchor2, int cornerRadius, float scaleX, float scaleY) {
        this.contextMenuItems = new ArrayList();
        this.visibleMenuItemViews = new ArrayList();
        this.anchorOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                ContextMenu.this.alignCutoutOverlayToAnchor();
            }

            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            public void onScrollChanged() {
                ContextMenu.this.alignCutoutOverlayToAnchor();
            }
        };
        this.anchorRootLayoutChangeListener = new View.OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ContextMenu.this.alignCutoutOverlayToAnchor();
            }
        };
        this.isShowing = false;
        this.anchor = anchor2;
        this.activity = activity2;
        this.menuContainer = new FrameLayout(this.activity);
        this.menuContainer.setContentDescription(this.activity.getString(C1167R.string.context_menu_description));
        this.popupWindow = new PopupWindow(this.menuContainer, -2, -2);
        this.popupWindow.setFocusable(true);
        this.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                ContextMenu.this.clearDimBackground();
                if (ContextMenu.this.onDismissListener != null) {
                    ContextMenu.this.onDismissListener.onDismiss();
                }
                ContextMenu.this.anchor.removeOnAttachStateChangeListener(ContextMenu.this.anchorOnAttachStateChangeListener);
                ContextMenu.this.anchor.getViewTreeObserver().removeOnScrollChangedListener(ContextMenu.this.onScrollChangedListener);
                ContextMenu.this.anchor.getRootView().removeOnLayoutChangeListener(ContextMenu.this.anchorRootLayoutChangeListener);
                boolean unused = ContextMenu.this.isShowing = false;
            }
        });
        float[] anchorCoordinates = new float[2];
        getLocationInWindow(this.anchor, anchorCoordinates);
        this.anchorX = anchorCoordinates[0];
        this.anchorY = anchorCoordinates[1];
        this.anchorRealWidth = ((float) this.anchor.getWidth()) * scaleX;
        this.anchorRealHeight = ((float) this.anchor.getHeight()) * scaleY;
        this.menuVerticalMargin = getDimenInPixels(C1167R.dimen.context_menu_vertical_margin);
        this.triangleVerticalMenuMargin = getDimenInPixels(C1167R.dimen.context_menu_triangle_vertical_margin);
        this.triangleEdgeOffset = getDimenInPixels(C1167R.dimen.context_menu_triangle_edge_offset);
        this.triangleHeight = getDimenInPixels(C1167R.dimen.context_menu_triangle_height);
        this.triangleWidth = getDimenInPixels(C1167R.dimen.context_menu_triangle_width);
        this.focusedColor = this.activity.getColor(C1167R.color.context_menu_background_focused_color);
        this.unfocusedColor = this.activity.getColor(C1167R.color.context_menu_background_unfocused_color);
        this.enabledColor = this.activity.getColor(C1167R.color.context_menu_icon_enabled_color);
        this.disabledColor = this.activity.getColor(C1167R.color.context_menu_icon_disabled_color);
        this.menuHeightPerRow = getDimenInPixels(C1167R.dimen.context_menu_height_per_row);
        this.overscanHorizontal = getDimenInPixels(C1167R.dimen.overscan_horizontal);
        this.overscanVertical = getDimenInPixels(C1167R.dimen.overscan_vertical);
        this.overlayAlpha = getFloat(C1167R.dimen.context_menu_overlay_alpha);
        this.overlayShowAnimationDuration = this.activity.getResources().getInteger(C1167R.integer.context_menu_overlay_show_animation_duration_ms);
        this.overlayDismissAnimationDuration = this.activity.getResources().getInteger(C1167R.integer.context_menu_overlay_dismiss_animation_duration_ms);
        this.dimBackgroundColor = this.activity.getColor(C1167R.color.context_menu_overlay_background_color);
        float f = this.anchorX;
        float f2 = this.anchorY;
        RectF anchorRect = new RectF(f, f2, this.anchorRealWidth + f, this.anchorRealHeight + f2);
        this.cutoutOverlay = new CutoutOverlayLayout(this.activity, (int) (((float) cornerRadius) * scaleX), (int) (((float) cornerRadius) * scaleY));
        this.cutoutOverlay.setAnchorRect(anchorRect);
        this.triangle = new ImageView(this.activity);
    }

    private void getLocationInWindow(View anchorView, float[] outLocation) {
        if (outLocation == null || outLocation.length < 2) {
            throw new IllegalArgumentException("outLocation must be an array of two floats");
        }
        float[] position = {0.0f, 0.0f};
        anchorView.getMatrix().mapPoints(position);
        position[0] = position[0] + ((float) anchorView.getLeft());
        position[1] = position[1] + ((float) anchorView.getTop());
        ViewParent viewParent = anchorView.getParent();
        while (viewParent instanceof View) {
            View view = (View) viewParent;
            position[0] = position[0] - ((float) view.getScrollX());
            position[1] = position[1] - ((float) view.getScrollY());
            view.getMatrix().mapPoints(position);
            position[0] = position[0] + ((float) view.getLeft());
            position[1] = position[1] + ((float) view.getTop());
            viewParent = view.getParent();
        }
        outLocation[0] = position[0];
        outLocation[1] = position[1];
    }

    public ContextMenuItem findItem(int menuId) {
        for (ContextMenuItem item : this.contextMenuItems) {
            if (item.getId() == menuId) {
                return item;
            }
        }
        return null;
    }

    private boolean testBit(int bitSet, int mask) {
        if (mask == 0) {
            if (bitSet == 0) {
                return true;
            }
            return false;
        } else if ((bitSet & mask) == mask) {
            return true;
        } else {
            return false;
        }
    }

    private void dimBackground() {
        ViewGroup viewGroup = this.rootParentWindow;
        viewGroup.addView(this.cutoutOverlay, viewGroup.getWidth(), this.rootParentWindow.getHeight());
        this.cutoutOverlay.setAlpha(0.0f);
        animateBackgroundOverlayAlpha(this.overlayAlpha, this.overlayShowAnimationDuration);
    }

    private void animateBackgroundOverlayAlpha(float destinationAlpha, int duration) {
        ObjectAnimator objectAnimator = this.overlayAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        this.overlayAnimator = ObjectAnimator.ofFloat(this.cutoutOverlay, View.ALPHA, destinationAlpha);
        this.overlayAnimator.setDuration((long) duration);
        this.overlayAnimator.start();
    }

    /* access modifiers changed from: private */
    public void clearDimBackground() {
        animateBackgroundOverlayAlpha(0.0f, this.overlayDismissAnimationDuration);
        this.overlayAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ContextMenu.this.rootParentWindow.removeView(ContextMenu.this.cutoutOverlay);
            }
        });
    }

    public void forceDismiss() {
        this.popupWindow.dismiss();
    }

    private int getDimenInPixels(int resourceId) {
        return this.anchor.getResources().getDimensionPixelSize(resourceId);
    }

    private List<ContextMenuItem> getVisibleItems() {
        List<ContextMenuItem> list = new ArrayList<>();
        for (ContextMenuItem item : this.contextMenuItems) {
            if (item.isVisible()) {
                list.add(item);
            }
        }
        return list;
    }

    private void determineGravity() {
        this.gravity = 0;
        this.horizontalPosition = 17;
        if (this.anchor.getLayoutDirection() == 1) {
            if ((this.anchorX + this.anchorRealWidth) - ((float) this.menuWidth) >= ((float) this.overscanHorizontal)) {
                this.gravity |= 5;
            } else {
                this.gravity |= 3;
            }
        } else if (this.anchorX + ((float) this.menuWidth) <= ((float) (this.rootParentWindow.getWidth() - this.overscanHorizontal))) {
            this.gravity |= 3;
        } else {
            this.gravity |= 5;
        }
        float f = this.anchorY + this.anchorRealHeight + ((float) this.menuHeight);
        int height = this.rootParentWindow.getHeight();
        int i = this.overscanVertical;
        if (f <= ((float) (height - i))) {
            this.gravity |= 80;
            return;
        }
        float f2 = this.anchorY;
        int i2 = this.menuHeight;
        if (f2 - ((float) i2) >= ((float) i)) {
            this.gravity |= 48;
            return;
        }
        this.menuHeight = i2 - this.menuVerticalMargin;
        if (this.anchor.getLayoutDirection() == 0) {
            if (this.anchorX + this.anchorRealWidth + ((float) this.menuWidth) + ((float) this.triangleHeight) <= ((float) (this.rootParentWindow.getWidth() - this.overscanHorizontal))) {
                this.horizontalPosition = 5;
            } else {
                this.horizontalPosition = 3;
            }
        } else if ((this.anchorX - ((float) this.menuWidth)) - ((float) this.triangleHeight) >= ((float) this.overscanHorizontal)) {
            this.horizontalPosition = 3;
        } else {
            this.horizontalPosition = 5;
        }
        float f3 = this.anchorY + ((float) this.menuHeight);
        int height2 = this.rootParentWindow.getHeight();
        int i3 = this.overscanVertical;
        if (f3 <= ((float) (height2 - i3))) {
            this.verticalPosition = 0;
        } else if ((this.anchorY + this.anchorRealHeight) - ((float) this.menuHeight) >= ((float) i3)) {
            this.verticalPosition = 1;
        } else {
            this.verticalPosition = 2;
        }
    }

    private void calculateMenuSize() {
        this.menuLinearLayout.measure(0, 0);
        this.menuWidth = this.menuLinearLayout.getMeasuredWidth();
        this.menuHeight = this.menuLinearLayout.getMeasuredHeight() + this.menuVerticalMargin;
    }

    public void addItem(ContextMenuItem item) {
        this.contextMenuItems.add(item);
    }

    private int getRelativeGravity(int absoluteGravity, int layoutDirection) {
        if (testBit(absoluteGravity, 5) && layoutDirection == 0) {
            return GravityCompat.END;
        }
        if (testBit(absoluteGravity, 3) && layoutDirection == 1) {
            return GravityCompat.END;
        }
        if (testBit(absoluteGravity, 3) && layoutDirection == 0) {
            return GravityCompat.START;
        }
        if (!testBit(absoluteGravity, 5) || layoutDirection != 1) {
            return 0;
        }
        return GravityCompat.START;
    }

    private void adjustTrianglePosition() {
        FrameLayout.LayoutParams triangleLayoutParams = (FrameLayout.LayoutParams) this.triangle.getLayoutParams();
        triangleLayoutParams.gravity = 0;
        if (this.horizontalPosition == 17) {
            if (getRelativeGravity(this.gravity, this.anchor.getLayoutDirection()) == 8388613) {
                triangleLayoutParams.gravity |= GravityCompat.END;
                triangleLayoutParams.setMarginEnd(this.triangleEdgeOffset);
            } else {
                triangleLayoutParams.gravity |= GravityCompat.START;
                triangleLayoutParams.setMarginStart(this.triangleEdgeOffset);
            }
            if (testBit(this.gravity, 48)) {
                triangleLayoutParams.gravity |= 80;
                triangleLayoutParams.bottomMargin = this.triangleVerticalMenuMargin;
                this.triangle.setScaleY(-1.0f);
                return;
            }
            triangleLayoutParams.gravity |= 48;
            triangleLayoutParams.topMargin = this.triangleVerticalMenuMargin;
            return;
        }
        triangleLayoutParams.gravity |= GravityCompat.START;
        float f = 90.0f;
        if (getRelativeGravity(this.horizontalPosition, this.anchor.getLayoutDirection()) == 8388611) {
            triangleLayoutParams.setMarginStart(this.menuWidth - 2);
            ImageView imageView = this.triangle;
            if (this.anchor.getLayoutDirection() == 1) {
                f = 270.0f;
            }
            imageView.setRotation(f);
        } else {
            triangleLayoutParams.setMarginStart(0);
            ImageView imageView2 = this.triangle;
            if (this.anchor.getLayoutDirection() != 1) {
                f = 270.0f;
            }
            imageView2.setRotation(f);
        }
        int i = this.verticalPosition;
        if (i == 0) {
            triangleLayoutParams.gravity |= 48;
            triangleLayoutParams.topMargin = (int) ((this.anchorRealHeight - ((float) this.triangleWidth)) / 2.0f);
        } else if (i == 1) {
            triangleLayoutParams.gravity |= 80;
            triangleLayoutParams.bottomMargin = (int) ((this.anchorRealHeight - ((float) this.triangleWidth)) / 2.0f);
        } else {
            int menuLocationY = this.rootParentWindow.getHeight() - this.menuHeight;
            triangleLayoutParams.gravity |= 48;
            triangleLayoutParams.topMargin = (int) ((this.anchorY - ((float) menuLocationY)) + ((this.anchorRealHeight - ((float) this.triangleWidth)) / 2.0f));
        }
        this.menuWidth += this.triangleHeight;
    }

    private void adjustLayoutMenu() {
        ViewGroup.MarginLayoutParams menuLayoutParams = (ViewGroup.MarginLayoutParams) this.menuLinearLayout.getLayoutParams();
        int i = this.horizontalPosition;
        if (i != 17) {
            menuLayoutParams.topMargin = 0;
            menuLayoutParams.bottomMargin = 0;
            if (i == 3) {
                menuLayoutParams.rightMargin = this.triangleHeight;
            } else {
                menuLayoutParams.leftMargin = this.triangleHeight;
            }
        } else if (testBit(this.gravity, 48)) {
            menuLayoutParams.bottomMargin = this.menuVerticalMargin;
            menuLayoutParams.topMargin = 0;
        } else {
            menuLayoutParams.bottomMargin = 0;
            menuLayoutParams.topMargin = this.menuVerticalMargin;
        }
        if ((this.horizontalPosition == 17 && testBit(this.gravity, 48)) || (this.horizontalPosition != 17 && this.verticalPosition == 1)) {
            this.menuLinearLayout.removeAllViews();
            for (int i2 = this.visibleItems.size() - 1; i2 >= 0; i2--) {
                this.menuLinearLayout.addView(this.visibleMenuItemViews.get(i2));
            }
        }
    }

    private void adjustMenuShowUpPosition() {
        float f = 0.0f;
        this.deltaX = 0.0f;
        this.deltaY = 0.0f;
        int i = this.horizontalPosition;
        if (i == 17) {
            float deltaX2 = this.anchorRealWidth - ((float) this.anchor.getWidth());
            this.deltaY = testBit(this.gravity, 80) ? this.anchorRealHeight - ((float) this.anchor.getHeight()) : 0.0f;
            if (testBit(this.gravity, 5)) {
                f = deltaX2;
            }
            this.deltaX = f;
            return;
        }
        if (i == 3) {
            this.deltaX = (float) (-this.menuWidth);
        } else {
            this.deltaX = this.anchorRealWidth;
        }
        this.popupWindow.setOverlapAnchor(true);
        if (this.verticalPosition == 1) {
            this.deltaY = -(((float) this.menuHeight) - this.anchorRealHeight);
        }
    }

    public void show() {
        this.visibleItems = getVisibleItems();
        this.visibleMenuItemViews.clear();
        if (this.rootParentWindow == null) {
            this.rootParentWindow = (ViewGroup) this.activity.getWindow().getDecorView().getRootView();
        }
        this.anchor.addOnAttachStateChangeListener(this.anchorOnAttachStateChangeListener);
        this.anchor.getViewTreeObserver().addOnScrollChangedListener(this.onScrollChangedListener);
        this.anchor.getRootView().addOnLayoutChangeListener(this.anchorRootLayoutChangeListener);
        dimBackground();
        this.menuLinearLayout = new LinearLayout(this.activity);
        this.menuLinearLayout.setOrientation(1);
        this.menuContainer.addView(this.menuLinearLayout, -2, -2);
        this.menuContainer.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(ContextMenu.this.visibleItems.size(), 0, false));
            }
        });
        this.menuLinearLayout.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) view.getResources().getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius));
            }
        });
        this.menuLinearLayout.setClipToOutline(true);
        this.triangle.setImageDrawable(this.activity.getDrawable(C1167R.C1168drawable.context_menu_triangle));
        this.triangle.setColorFilter(this.unfocusedColor, PorterDuff.Mode.SRC_ATOP);
        addMenuItemViews();
        calculateMenuSize();
        determineGravity();
        this.menuContainer.addView(this.triangle, -2, -2);
        adjustTrianglePosition();
        adjustLayoutMenu();
        int triangleTopLocation = ((ViewGroup.MarginLayoutParams) this.triangle.getLayoutParams()).topMargin;
        if (this.horizontalPosition != 17) {
            int i = 0;
            while (true) {
                if (i >= this.visibleItems.size()) {
                    break;
                }
                int i2 = this.menuHeightPerRow;
                if (triangleTopLocation >= i2 * i && triangleTopLocation <= i2 * (i + 1)) {
                    this.visibleItems.get(i).setLinkedWithTriangle(true);
                    break;
                }
                i++;
            }
        } else {
            this.visibleItems.get(0).setLinkedWithTriangle(true);
        }
        adjustMenuShowUpPosition();
        this.visibleMenuItemViews.get(0).requestFocus();
        this.popupWindow.setWidth(this.menuWidth);
        this.popupWindow.setHeight(this.menuHeight);
        if (this.horizontalPosition == 17) {
            this.popupWindow.showAsDropDown(this.anchor, (int) this.deltaX, (int) this.deltaY, this.gravity);
        } else {
            this.popupWindow.showAsDropDown(this.anchor, (int) this.deltaX, (int) this.deltaY, 3);
        }
        this.isShowing = true;
    }

    public void setOnMenuItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.res.Resources.getValue(int, android.util.TypedValue, boolean):void throws android.content.res.Resources$NotFoundException}
     arg types: [int, android.util.TypedValue, int]
     candidates:
      ClspMth{android.content.res.Resources.getValue(java.lang.String, android.util.TypedValue, boolean):void throws android.content.res.Resources$NotFoundException}
      ClspMth{android.content.res.Resources.getValue(int, android.util.TypedValue, boolean):void throws android.content.res.Resources$NotFoundException} */
    private float getFloat(int resourceId) {
        TypedValue resValue = new TypedValue();
        this.activity.getResources().getValue(resourceId, resValue, true);
        return resValue.getFloat();
    }

    private void bindMenuItemView(final ContextMenuItem menuItem, final View view) {
        int i;
        TextView actionTextView = (TextView) view.findViewById(C1167R.C1170id.title);
        actionTextView.setText(menuItem.getTitle());
        Context context = view.getContext();
        if (menuItem.isEnabled()) {
            i = C1167R.color.context_menu_text_enabled_color;
        } else {
            i = C1167R.color.context_menu_text_disabled_color;
        }
        actionTextView.setTextColor(context.getColor(i));
        ImageView menuIcon = (ImageView) view.findViewById(C1167R.C1170id.icon);
        menuIcon.setColorFilter(menuItem.isEnabled() ? this.enabledColor : this.disabledColor, PorterDuff.Mode.SRC_ATOP);
        if (menuItem.getIcon() != null) {
            menuIcon.setImageDrawable(menuItem.getIcon());
        }
        view.setBackgroundColor(this.unfocusedColor);
        if (Util.isAccessibilityEnabled(this.activity)) {
            view.setFocusable(true);
        } else {
            view.setFocusable(menuItem.isEnabled());
        }
        view.setEnabled(menuItem.isEnabled());
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (menuItem.isEnabled()) {
                    if (menuItem.isAutoDismiss()) {
                        ContextMenu.this.forceDismiss();
                    }
                    if (ContextMenu.this.onItemClickListener != null) {
                        ContextMenu.this.onItemClickListener.onItemClick(menuItem);
                    }
                }
            }
        });
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (menuItem.isLinkedWithTriangle()) {
                        ContextMenu.this.triangle.setColorFilter(ContextMenu.this.focusedColor, PorterDuff.Mode.SRC_ATOP);
                    }
                    view.setBackgroundColor(ContextMenu.this.focusedColor);
                    return;
                }
                if (menuItem.isLinkedWithTriangle()) {
                    ContextMenu.this.triangle.setColorFilter(ContextMenu.this.unfocusedColor, PorterDuff.Mode.SRC_ATOP);
                }
                view.setBackgroundColor(ContextMenu.this.unfocusedColor);
            }
        });
        this.visibleMenuItemViews.add(view);
    }

    private void addMenuItemViews() {
        this.menuLinearLayout.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService("layout_inflater");
        for (int i = 0; i < this.visibleItems.size(); i++) {
            View rowView = layoutInflater.inflate(C1167R.layout.context_menu_item, (ViewGroup) null);
            bindMenuItemView(this.visibleItems.get(i), rowView);
            this.menuLinearLayout.addView(rowView, -1, this.menuHeightPerRow);
        }
    }

    /* access modifiers changed from: private */
    public void alignCutoutOverlayToAnchor() {
        View view = this.anchor;
        if (view != null && this.cutoutOverlay != null) {
            float[] anchorCoordinates = new float[2];
            getLocationInWindow(view, anchorCoordinates);
            if (Math.abs(anchorCoordinates[0] - this.anchorX) > FLOAT_COMPARISON_DELTA || Math.abs(anchorCoordinates[1] - this.anchorY) > FLOAT_COMPARISON_DELTA) {
                this.anchorX = anchorCoordinates[0];
                this.anchorY = anchorCoordinates[1];
                float f = this.anchorX;
                float f2 = this.anchorY;
                this.cutoutOverlay.setAnchorRect(new RectF(f, f2, this.anchorRealWidth + f, this.anchorRealHeight + f2));
                this.cutoutOverlay.invalidate();
            }
        }
    }

    class CutoutOverlayLayout extends FrameLayout {
        private Paint paint = new Paint();
        private int radiusX;
        private int radiusY;
        private RectF rect;

        public CutoutOverlayLayout(Context context, int radiusX2, int radiusY2) {
            super(context);
            this.radiusX = radiusX2;
            this.radiusY = radiusY2;
            setWillNotDraw(false);
            setLayerType(2, null);
            this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            this.paint.setAntiAlias(true);
        }

        public void setAnchorRect(RectF rect2) {
            this.rect = rect2;
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            canvas.drawColor(ContextMenu.this.dimBackgroundColor);
            canvas.drawRoundRect(this.rect, (float) this.radiusX, (float) this.radiusY, this.paint);
        }

        /* access modifiers changed from: package-private */
        public RectF getRect() {
            return this.rect;
        }

        /* access modifiers changed from: package-private */
        public int getRadiusX() {
            return this.radiusX;
        }

        /* access modifiers changed from: package-private */
        public int getRadiusY() {
            return this.radiusY;
        }
    }
}
