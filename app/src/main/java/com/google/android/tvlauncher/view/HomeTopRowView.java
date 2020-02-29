package com.google.android.tvlauncher.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.application.TvLauncherApplicationBase;
import com.google.android.tvlauncher.appsview.PackageChangedReceiver;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.home.HomeRow;
import com.google.android.tvlauncher.home.HomeTopRowButton;
import com.google.android.tvlauncher.home.OnHomeRowRemovedListener;
import com.google.android.tvlauncher.home.OnHomeRowSelectedListener;
import com.google.android.tvlauncher.home.OnHomeStateChangeListener;
import com.google.android.tvlauncher.notifications.NotificationsPanelButtonView;
import com.google.android.tvlauncher.notifications.NotificationsPanelController;
import com.google.android.tvlauncher.notifications.NotificationsTrayAdapter;
import com.google.android.tvlauncher.notifications.NotificationsTrayView;
import com.google.android.tvlauncher.settings.ProfilesManager;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvlauncher.view.SearchView;
import com.google.android.tvlauncher.widget.PartnerWidgetInfo;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class HomeTopRowView extends LinearLayout implements View.OnFocusChangeListener, HomeRow, PackageChangedReceiver.Listener {
    private static final String SETTINGS_PACKAGE_NAME = "com.android.tv.settings";
    public static final int STATE_DEFAULT = 0;
    public static final int STATE_TOP_BAR_SELECTED = 1;
    public static final int STATE_TRAY_SELECTED = 2;
    private static final String TAG = "HomeTopRowView";
    private Context context;
    private int defaultItemsContainerBottomMargin;
    private int defaultItemsContainerTopMargin;
    private int duration;
    /* access modifiers changed from: private */
    public EventLogger eventLogger;
    /* access modifiers changed from: private */
    public OnHomeTopRowFocusChangedListener focusChangeListener;
    private float focusedElevation;
    private float focusedZoom;
    private final ViewTreeObserver.OnGlobalFocusChangeListener globalFocusChangeListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            if (HomeTopRowView.this.findFocus() == newFocus) {
                HomeTopRowView.this.onHomeRowSelectedListener.onHomeRowSelected(HomeTopRowView.this);
            }
            if ((!HomeTopRowView.this.notificationsTray.hasFocus() || HomeTopRowView.this.state != 1) && (!HomeTopRowView.this.itemsContainer.hasFocus() || HomeTopRowView.this.state != 2)) {
                if (((oldFocus instanceof SearchOrb) || (newFocus instanceof SearchOrb)) && HomeTopRowView.this.focusChangeListener != null) {
                    HomeTopRowView.this.focusChangeListener.onHomeTopRowFocusChanged();
                }
            } else if (HomeTopRowView.this.focusChangeListener != null) {
                HomeTopRowView.this.focusChangeListener.onHomeTopRowFocusChanged();
            }
        }
    };
    private HomeTopRowButton inputs;
    private IntentLaunchDispatcher intentLauncher;
    /* access modifiers changed from: private */
    public ViewGroup itemsContainer;
    /* access modifiers changed from: private */
    public NotificationsTrayView notificationsTray;
    private OnActionListener onActionListener;
    /* access modifiers changed from: private */
    public OnHomeRowSelectedListener onHomeRowSelectedListener;
    private PackageChangedReceiver packageChangedReceiver;
    private NotificationsPanelController panelController = null;
    private HomeTopRowButton partnerWidget;
    private PartnerWidgetInfo partnerWidgetInfo = null;
    private HomeTopRowButton profiles;
    private SearchView search;
    private final SearchView.ActionCallbacks searchViewActionCallbacks = new SearchView.ActionCallbacks() {
        public void onStartedVoiceSearch() {
            HomeTopRowView.this.eventLogger.log(new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.START_VOICE_SEARCH).setVisualElementTag(TvLauncherConstants.VOICE_SEARCH_BUTTON));
        }

        public void onStartedKeyboardSearch() {
            HomeTopRowView.this.eventLogger.log(new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.START_KEYBOARD_SEARCH).setVisualElementTag(TvLauncherConstants.KEYBOARD_SEARCH_BUTTON));
        }
    };
    private int selectedItemsContainerBottomMargin;
    private int selectedItemsContainerTopMargin;
    /* access modifiers changed from: private */
    public int state = 0;
    private float unfocusedElevation;

    public interface OnActionListener {
        void onShowInputs();

        void onStartSettings();
    }

    public interface OnHomeTopRowFocusChangedListener {
        void onHomeTopRowFocusChanged();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    /* access modifiers changed from: package-private */
    public SearchView.ActionCallbacks getSearchViewActionCallbacks() {
        return this.searchViewActionCallbacks;
    }

    public HomeTopRowView(Context context2) {
        super(context2);
        init(context2);
    }

    public HomeTopRowView(Context context2, AttributeSet attrs) {
        super(context2, attrs);
        init(context2);
    }

    public HomeTopRowView(Context context2, AttributeSet attrs, int defStyleAttr) {
        super(context2, attrs, defStyleAttr);
        init(context2);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (this.notificationsTray.getVisibility() == 0) {
            views.add(this.notificationsTray);
        }
        super.addFocusables(views, direction, focusableMode);
    }

    private void init(Context context2) {
        this.context = context2;
        Resources resources = context2.getResources();
        this.intentLauncher = ((TvLauncherApplicationBase) this.context.getApplicationContext()).getIntentLauncher();
        this.focusedElevation = resources.getDimension(C1167R.dimen.top_row_item_focused_z);
        this.unfocusedElevation = resources.getDimension(C1167R.dimen.top_row_item_unfocused_z);
        this.focusedZoom = resources.getFraction(C1167R.fraction.top_row_item_focused_zoom, 1, 1);
        this.duration = resources.getInteger(C1167R.integer.top_row_scale_duration_ms);
        this.defaultItemsContainerTopMargin = resources.getDimensionPixelSize(C1167R.dimen.top_row_items_container_margin_top);
        this.defaultItemsContainerBottomMargin = resources.getDimensionPixelSize(C1167R.dimen.top_row_items_container_margin_bottom);
        this.selectedItemsContainerTopMargin = resources.getDimensionPixelSize(C1167R.dimen.top_row_selected_items_container_margin_top);
        this.selectedItemsContainerBottomMargin = resources.getDimensionPixelSize(C1167R.dimen.top_row_selected_items_container_margin_bottom);
        this.packageChangedReceiver = new PackageChangedReceiver(this);
    }

    public void setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
    }

    public void setEventLogger(EventLogger eventLogger2) {
        this.eventLogger = eventLogger2;
    }

    public void setFocusChangeListener(OnHomeTopRowFocusChangedListener focusChangeListener2) {
        this.focusChangeListener = focusChangeListener2;
    }

    public void setNotificationsTrayAdapter(NotificationsTrayAdapter adapter) {
        this.notificationsTray.setTrayAdapter(adapter);
    }

    public void updateNotificationsTrayVisibility() {
        this.notificationsTray.updateVisibility();
        updateMargins();
    }

    public void hideNotificationsTray() {
        NotificationsTrayView notificationsTrayView = this.notificationsTray;
        if (notificationsTrayView != null) {
            notificationsTrayView.setVisibility(8);
        }
    }

    public void setNotificationsPanelController(NotificationsPanelController controller) {
        this.panelController = controller;
        this.panelController.setView((NotificationsPanelButtonView) findViewById(C1167R.C1170id.notification_panel_button));
    }

    public NotificationsTrayAdapter getNotificationsTrayAdapter() {
        return this.notificationsTray.getTrayAdapter();
    }

    public NotificationsPanelController getNotificationsPanelController() {
        return this.panelController;
    }

    public SearchView getSearchWidget() {
        return this.search;
    }

    public ViewGroup getItemsContainer() {
        return this.itemsContainer;
    }

    public NotificationsTrayView getNotificationsTray() {
        return this.notificationsTray;
    }

    public void updateInputIconVisibility(boolean visible) {
        this.inputs.setVisibility(visible ? 0 : 8);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.itemsContainer = (ViewGroup) findViewById(C1167R.C1170id.items_container);
        this.search = (SearchView) findViewById(C1167R.C1170id.search_view);
        this.search.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        });
        this.search.registerActionsCallbacks(this.searchViewActionCallbacks);
        this.profiles = (HomeTopRowButton) findViewById(C1167R.C1170id.profiles);
        this.profiles.setIcon(C1167R.C1168drawable.ic_tv_options_parental_black);
        this.profiles.setText(C1167R.string.restricted_profile_icon_title);
        this.profiles.setContentDescription(getResources().getString(C1167R.string.profiles_accessibility_description));
        this.profiles.setOnClickListener(new HomeTopRowView$$Lambda$0(this));
        updateProfiles();
        this.inputs = (HomeTopRowButton) findViewById(C1167R.C1170id.inputs);
        Uri inputsUri = OemConfiguration.get(this.context).getOemInputsIconUri();
        if (inputsUri != null) {
            ((RequestBuilder) Glide.with(this.context).asDrawable().load(inputsUri).error(C1167R.C1168drawable.ic_action_inputs_black)).into(new ImageViewTarget<Drawable>(this, this.inputs.getIconImageView()) {
                /* access modifiers changed from: protected */
                public void setResource(Drawable resource) {
                    setDrawable(resource);
                }
            });
        } else {
            this.inputs.setIcon(C1167R.C1168drawable.ic_action_inputs_black);
        }
        String label = OemConfiguration.get(this.context).getInputsPanelLabelText(this.context);
        this.inputs.setText(label);
        this.inputs.setContentDescription(label);
        this.inputs.setOnClickListener(new HomeTopRowView$$Lambda$1(this));
        this.partnerWidget = (HomeTopRowButton) findViewById(C1167R.C1170id.partner_widget);
        this.partnerWidget.setOnClickListener(new HomeTopRowView$$Lambda$2(this));
        updatePartnerWidget();
        HomeTopRowButton settings = (HomeTopRowButton) findViewById(C1167R.C1170id.settings);
        settings.setIcon(C1167R.C1168drawable.ic_action_settings_black);
        settings.setText(C1167R.string.settings_icon_title);
        settings.setContentDescription(getResources().getString(C1167R.string.settings_accessibility_description));
        settings.setOnClickListener(new HomeTopRowView$$Lambda$3(this));
        this.notificationsTray = (NotificationsTrayView) findViewById(C1167R.C1170id.notifications_tray);
        updateNotificationsTrayVisibility();
        TvHomeDrawnManager.getInstance().monitorViewLayoutDrawn(this);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$0$HomeTopRowView(View v) {
        Intent launchIntent = ProfilesManager.getInstance(getContext()).getProfileIntent();
        if (launchIntent != null) {
            this.context.startActivity(launchIntent);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$1$HomeTopRowView(View v) {
        OnActionListener onActionListener2 = this.onActionListener;
        if (onActionListener2 != null) {
            onActionListener2.onShowInputs();
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$2$HomeTopRowView(View v) {
        PartnerWidgetInfo partnerWidgetInfo2 = this.partnerWidgetInfo;
        if (partnerWidgetInfo2 != null && partnerWidgetInfo2.isComplete()) {
            this.intentLauncher.launchIntentFromUri(this.partnerWidgetInfo.getIntent(), false);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$3$HomeTopRowView(View v) {
        OnActionListener onActionListener2 = this.onActionListener;
        if (onActionListener2 != null) {
            onActionListener2.onStartSettings();
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        float scale = hasFocus ? this.focusedZoom : 1.0f;
        v.animate().z(hasFocus ? this.focusedElevation : this.unfocusedElevation).scaleX(scale).scaleY(scale).setDuration((long) this.duration);
    }

    public void setState(boolean selected) {
        boolean z = false;
        if (!selected) {
            this.state = 0;
        } else if (this.notificationsTray.hasFocus()) {
            this.state = 2;
        } else {
            this.state = 1;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.search.getLayoutParams();
        lp.setMarginStart(getResources().getDimensionPixelOffset(C1167R.dimen.search_orb_margin_start));
        this.search.setLayoutParams(lp);
        SearchView searchView = this.search;
        if (this.state == 1) {
            z = true;
        }
        searchView.bind(z);
        updateMargins();
    }

    private void updateMargins() {
        int i;
        int i2;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.itemsContainer.getLayoutParams();
        int i3 = this.state;
        if (i3 != 0) {
            if (i3 == 1) {
                i = this.selectedItemsContainerTopMargin;
            } else {
                i = this.defaultItemsContainerTopMargin;
            }
            lp.topMargin = i;
            if (this.notificationsTray.getVisibility() == 0) {
                i2 = this.defaultItemsContainerBottomMargin;
            } else {
                i2 = this.selectedItemsContainerBottomMargin;
            }
            lp.bottomMargin = i2;
        } else {
            lp.topMargin = this.defaultItemsContainerTopMargin;
            lp.bottomMargin = this.defaultItemsContainerBottomMargin;
        }
        this.itemsContainer.setLayoutParams(lp);
    }

    private void updateProfiles() {
        if (ProfilesManager.getInstance(this.context).hasRestrictedProfile()) {
            this.profiles.setVisibility(0);
        } else {
            this.profiles.setVisibility(8);
        }
    }

    public void updateProfiles(String packageName) {
        if (TextUtils.equals(SETTINGS_PACKAGE_NAME, packageName) && this.profiles != null) {
            updateProfiles();
        }
    }

    public void onPartnerWidgetUpdate(PartnerWidgetInfo info) {
        this.partnerWidgetInfo = info;
        updatePartnerWidget();
    }

    private void updatePartnerWidget() {
        PartnerWidgetInfo partnerWidgetInfo2 = this.partnerWidgetInfo;
        if (partnerWidgetInfo2 == null || !partnerWidgetInfo2.isComplete()) {
            this.partnerWidget.setVisibility(8);
            return;
        }
        this.partnerWidget.setIcon(this.partnerWidgetInfo.getIcon());
        this.partnerWidget.setText(this.partnerWidgetInfo.getTitle());
        this.partnerWidget.setContentDescription(this.partnerWidgetInfo.getTitle());
        this.partnerWidget.setVisibility(0);
    }

    public void setOnHomeStateChangeListener(OnHomeStateChangeListener listener) {
    }

    public void setOnHomeRowSelectedListener(OnHomeRowSelectedListener listener) {
        this.onHomeRowSelectedListener = listener;
    }

    public void setOnHomeRowRemovedListener(OnHomeRowRemovedListener listener) {
    }

    public void setHomeIsFastScrolling(boolean homeIsFastScrolling) {
    }

    public View getView() {
        return this;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalFocusChangeListener(this.globalFocusChangeListener);
        this.context.unregisterReceiver(this.packageChangedReceiver);
        LaunchItemsManagerProvider.getInstance(getContext()).removeSearchPackageChangeListener(this.search);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(this.globalFocusChangeListener);
        this.context.registerReceiver(this.packageChangedReceiver, PackageChangedReceiver.getIntentFilter());
        LaunchItemsManagerProvider.getInstance(getContext()).addSearchPackageChangeListener(this.search);
    }

    public void onPackageAdded(String packageName) {
        updateProfiles(packageName);
    }

    public void onPackageChanged(String packageName) {
        updateProfiles(packageName);
    }

    public void onPackageFullyRemoved(String packageName) {
        updateProfiles(packageName);
    }

    public void onPackageRemoved(String packageName) {
        updateProfiles(packageName);
    }

    public void onPackageReplaced(String packageName) {
        updateProfiles(packageName);
    }
}
