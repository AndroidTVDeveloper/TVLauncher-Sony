package com.google.android.tvlauncher.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;

public class WatchNextInfoView extends FrameLayout {
    private String buttonActionDescription;
    private View container;
    private ImageView icon;
    private TextView message;
    private TextView title;

    public WatchNextInfoView(Context context) {
        super(context);
    }

    public WatchNextInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchNextInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WatchNextInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.container = findViewById(C1167R.C1170id.watch_next_info_container);
        this.icon = (ImageView) findViewById(C1167R.C1170id.watch_next_info_icon);
        this.title = (TextView) findViewById(C1167R.C1170id.watch_next_info_title);
        this.message = (TextView) findViewById(C1167R.C1170id.watch_next_info_message);
        this.buttonActionDescription = getResources().getString(C1167R.string.watch_next_row_acknowledgment_action_description);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, this.buttonActionDescription));
    }

    public View getContainer() {
        return this.container;
    }

    public ImageView getIcon() {
        return this.icon;
    }

    public TextView getTitle() {
        return this.title;
    }

    public TextView getMessage() {
        return this.message;
    }

    public View getIconTitleContainer() {
        return (View) this.icon.getParent();
    }
}
