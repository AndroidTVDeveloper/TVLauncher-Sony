package android.support.p004v7.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.p004v7.appcompat.C0207R;
import android.view.ViewConfiguration;

/* renamed from: android.support.v7.view.ActionBarPolicy */
public class ActionBarPolicy {
    private Context mContext;

    public static ActionBarPolicy get(Context context) {
        return new ActionBarPolicy(context);
    }

    private ActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public int getMaxActionButtons() {
        Configuration configuration = this.mContext.getResources().getConfiguration();
        int widthDp = configuration.screenWidthDp;
        int heightDp = configuration.screenHeightDp;
        if (configuration.smallestScreenWidthDp > 600 || widthDp > 600) {
            return 5;
        }
        if (widthDp > 960 && heightDp > 720) {
            return 5;
        }
        if (widthDp > 720 && heightDp > 960) {
            return 5;
        }
        if (widthDp >= 500) {
            return 4;
        }
        if (widthDp > 640 && heightDp > 480) {
            return 4;
        }
        if (widthDp > 480 && heightDp > 640) {
            return 4;
        }
        if (widthDp >= 360) {
            return 3;
        }
        return 2;
    }

    public boolean showsOverflowMenuButton() {
        if (Build.VERSION.SDK_INT >= 19) {
            return true;
        }
        return !ViewConfiguration.get(this.mContext).hasPermanentMenuKey();
    }

    public int getEmbeddedMenuWidthLimit() {
        return this.mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }

    public boolean hasEmbeddedTabs() {
        return this.mContext.getResources().getBoolean(C0207R.bool.abc_action_bar_embed_tabs);
    }

    public int getTabContainerHeight() {
        TypedArray a = this.mContext.obtainStyledAttributes(null, C0207R.styleable.ActionBar, C0207R.attr.actionBarStyle, 0);
        int height = a.getLayoutDimension(C0207R.styleable.ActionBar_height, 0);
        Resources r = this.mContext.getResources();
        if (!hasEmbeddedTabs()) {
            height = Math.min(height, r.getDimensionPixelSize(C0207R.dimen.abc_action_bar_stacked_max_height));
        }
        a.recycle();
        return height;
    }

    public boolean enableHomeButtonByDefault() {
        return this.mContext.getApplicationInfo().targetSdkVersion < 14;
    }

    public int getStackedTabMaxWidth() {
        return this.mContext.getResources().getDimensionPixelSize(C0207R.dimen.abc_action_bar_stacked_tab_max_width);
    }
}
