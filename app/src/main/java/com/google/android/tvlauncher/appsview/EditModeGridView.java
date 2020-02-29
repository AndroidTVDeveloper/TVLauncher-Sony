package com.google.android.tvlauncher.appsview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.leanback.widget.VerticalGridView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.AccessibilityContextMenu;
import com.google.android.tvlauncher.util.ContextMenu;
import com.google.android.tvlauncher.util.ContextMenuItem;
import com.google.android.tvlauncher.util.Util;

public class EditModeGridView extends VerticalGridView {
    static final int ACCESSIBILITY_MENU_DONE = 4;
    static final int ACCESSIBILITY_MENU_DOWN = 1;
    static final int ACCESSIBILITY_MENU_LEFT = 2;
    static final int ACCESSIBILITY_MENU_RIGHT = 3;
    static final int ACCESSIBILITY_MENU_UP = 0;
    /* access modifiers changed from: private */
    public AccessibilityContextMenu accessibilityContextMenu;

    public EditModeGridView(Context context) {
        this(context, null);
    }

    public EditModeGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditModeGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setColumnWidth(getResources().getDimensionPixelSize(C1167R.dimen.app_banner_width) + getResources().getDimensionPixelSize(C1167R.dimen.app_banner_margin_end));
    }

    public View focusSearch(View focused, int direction) {
        if (focused.isSelected()) {
            return swapItemsIfNeeded(focused, direction);
        }
        return super.focusSearch(focused, direction);
    }

    /* access modifiers changed from: package-private */
    public AccessibilityContextMenu getAccessibilityContextMenu() {
        return this.accessibilityContextMenu;
    }

    /* access modifiers changed from: package-private */
    public void showAccessibilityMenu() {
        Context context = getContext();
        if (this.accessibilityContextMenu == null) {
            this.accessibilityContextMenu = new AccessibilityContextMenu((Activity) context);
            this.accessibilityContextMenu.addItem(new ContextMenuItem(0, context.getString(C1167R.string.accessibility_menu_item_move_up), context.getDrawable(C1167R.C1168drawable.ic_arrow_up_black_24dp)));
            this.accessibilityContextMenu.addItem(new ContextMenuItem(1, context.getString(C1167R.string.accessibility_menu_item_move_down), context.getDrawable(C1167R.C1168drawable.ic_arrow_down_black_24dp)));
            ContextMenuItem menuLeft = new ContextMenuItem(2, context.getString(C1167R.string.accessibility_menu_item_move_left), context.getDrawable(C1167R.C1168drawable.ic_arrow_left_black_24dp));
            ContextMenuItem menuRight = new ContextMenuItem(3, context.getString(C1167R.string.accessibility_menu_item_move_right), context.getDrawable(C1167R.C1168drawable.ic_arrow_right_black_24dp));
            if (Util.isRtl(getContext())) {
                this.accessibilityContextMenu.addItem(menuRight);
                this.accessibilityContextMenu.addItem(menuLeft);
            } else {
                this.accessibilityContextMenu.addItem(menuLeft);
                this.accessibilityContextMenu.addItem(menuRight);
            }
            this.accessibilityContextMenu.addItem(new ContextMenuItem(4, context.getString(C1167R.string.accessibility_menu_item_done), context.getDrawable(C1167R.C1168drawable.ic_done_black_24dp)));
            this.accessibilityContextMenu.setOnMenuItemClickListener(new ContextMenu.OnItemClickListener() {
                public void onItemClick(ContextMenuItem item) {
                    int id = item.getId();
                    if (id == 0) {
                        EditModeGridView editModeGridView = EditModeGridView.this;
                        View unused = editModeGridView.swapItemsIfNeeded(editModeGridView.getFocusedChild(), 33);
                    } else if (id == 1) {
                        EditModeGridView editModeGridView2 = EditModeGridView.this;
                        View unused2 = editModeGridView2.swapItemsIfNeeded(editModeGridView2.getFocusedChild(), 130);
                    } else if (id == 2) {
                        EditModeGridView editModeGridView3 = EditModeGridView.this;
                        View unused3 = editModeGridView3.swapItemsIfNeeded(editModeGridView3.getFocusedChild(), 17);
                    } else if (id == 3) {
                        EditModeGridView editModeGridView4 = EditModeGridView.this;
                        View unused4 = editModeGridView4.swapItemsIfNeeded(editModeGridView4.getFocusedChild(), 66);
                    } else if (id == 4) {
                        EditModeGridView.this.accessibilityContextMenu.dismiss();
                    }
                }
            });
            this.accessibilityContextMenu.setOnDismissListener(new ContextMenu.OnDismissListener() {
                public void onDismiss() {
                    View focusedChild = EditModeGridView.this.getFocusedChild();
                    if (focusedChild != null) {
                        focusedChild.setSelected(false);
                    }
                }
            });
        }
        updateAccessibilityContextMenu();
        this.accessibilityContextMenu.show();
    }

    /* access modifiers changed from: package-private */
    public void hideAccessibilityMenu() {
        AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
        if (accessibilityContextMenu2 != null) {
            accessibilityContextMenu2.dismiss();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAccessibilityContextMenuIfNeeded() {
        AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
        if (accessibilityContextMenu2 != null && accessibilityContextMenu2.isShowing()) {
            updateAccessibilityContextMenu();
        }
    }

    private void updateAccessibilityContextMenu() {
        int endDirection;
        int startDirection;
        int position = getChildAdapterPosition(getFocusedChild());
        if (position != -1) {
            this.accessibilityContextMenu.findItem(1).setEnabled(canMoveInDirection(position, 130));
            this.accessibilityContextMenu.findItem(0).setEnabled(canMoveInDirection(position, 33));
            if (getLayoutDirection() == 1) {
                startDirection = 66;
                endDirection = 17;
            } else {
                startDirection = 17;
                endDirection = 66;
            }
            this.accessibilityContextMenu.findItem(2).setEnabled(canMoveInDirection(position, startDirection));
            this.accessibilityContextMenu.findItem(3).setEnabled(canMoveInDirection(position, endDirection));
        }
    }

    /* access modifiers changed from: private */
    public View swapItemsIfNeeded(View focused, int direction) {
        int position = getChildAdapterPosition(focused);
        if (getItemAnimator().isRunning()) {
            return focused;
        }
        if (getLayoutDirection() == 1 && (direction == 17 || direction == 66)) {
            direction = direction == 17 ? 66 : 17;
        }
        if (canMoveInDirection(position, direction)) {
            if (direction == 17) {
                moveLaunchPoint(position, position - 1, direction);
            } else if (direction == 33) {
                moveLaunchPoint(position, position - 4, direction);
            } else if (direction == 66) {
                moveLaunchPoint(position, position + 1, direction);
            } else if (direction == 130) {
                moveLaunchPoint(position, position + 4, direction);
            }
            updateAccessibilityContextMenuIfNeeded();
        }
        return focused;
    }

    private boolean canMoveInDirection(int position, int relativeDirection) {
        if (relativeDirection != 17) {
            if (relativeDirection != 33) {
                if (relativeDirection != 66) {
                    if (relativeDirection == 130 && position + 4 <= getAdapter().getItemCount() - 1) {
                        return true;
                    }
                    return false;
                } else if (position % 4 >= 3 || position >= getAdapter().getItemCount() - 1) {
                    return false;
                } else {
                    return true;
                }
            } else if (position - 4 >= 0) {
                return true;
            } else {
                return false;
            }
        } else if (position % 4 > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void moveLaunchPoint(int fromPosition, int toPosition, int direction) {
        ((EditModeGridAdapter) getAdapter()).moveLaunchItems(fromPosition, toPosition, direction);
    }
}
