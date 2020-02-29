package com.google.android.tvlauncher.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.support.p001v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.leanback.C0346R;
import com.google.android.tvlauncher.C1167R;

public class SearchOrb extends FrameLayout {
    private View focusIndicator;
    private float focusedZoom;
    private ImageView icon;
    private int indicatorFocusedColor;
    private int indicatorUnfocusedColor;

    public SearchOrb(Context context) {
        this(context, null);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, com.google.android.tvlauncher.view.SearchOrb, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public SearchOrb(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getContext().getResources();
        View rootView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C1167R.layout.search_orb_view, (ViewGroup) this, true);
        this.focusIndicator = rootView.findViewById(C1167R.C1170id.button_background);
        this.icon = (ImageView) rootView.findViewById(C1167R.C1170id.search_icon);
        this.focusedZoom = resources.getFraction(C0346R.fraction.lb_search_orb_focused_zoom, 1, 1);
        this.indicatorUnfocusedColor = ContextCompat.getColor(context, C1167R.color.search_orb_bg_dim_color);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.focusIndicator.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        });
        this.focusIndicator.setClipToOutline(true);
    }

    public void setOrbIcon(Drawable icon2) {
        this.icon.setImageDrawable(icon2);
    }

    public void setFocusedOrbColor(int color) {
        this.indicatorFocusedColor = color;
    }

    public View getFocusIndicator() {
        return this.focusIndicator;
    }

    /* access modifiers changed from: package-private */
    public void bind() {
        if (hasFocus()) {
            this.focusIndicator.setBackgroundColor(this.indicatorFocusedColor);
            setScaleX(this.focusedZoom);
            setScaleY(this.focusedZoom);
            return;
        }
        this.focusIndicator.setBackgroundColor(this.indicatorUnfocusedColor);
        setScaleX(1.0f);
        setScaleY(1.0f);
    }

    public Drawable getOrbIcon() {
        return this.icon.getDrawable();
    }
}
