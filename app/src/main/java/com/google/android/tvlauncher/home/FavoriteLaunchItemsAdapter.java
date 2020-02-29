package com.google.android.tvlauncher.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.p001v4.content.ContextCompat;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogUtils;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.BannerView;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.LaunchItemImageLoader;
import com.google.android.tvlauncher.appsview.OnEditModeFocusSearchCallback;
import com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import com.google.android.tvlauncher.home.view.AddFavoriteAppCardView;
import com.google.android.tvlauncher.home.view.FavoriteLaunchItemView;
import com.google.android.tvlauncher.util.AccessibilityContextMenu;
import com.google.android.tvlauncher.util.ContextMenu;
import com.google.android.tvlauncher.util.ContextMenuItem;
import com.google.android.tvlauncher.util.LaunchUtil;
import com.google.android.tvlauncher.util.ScaleFocusHandler;
import com.google.android.tvlauncher.util.Util;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.util.List;

class FavoriteLaunchItemsAdapter extends RecyclerView.Adapter<BaseViewHolder> implements EventLogger {
    private static final boolean DEBUG = false;
    private static final String PAYLOAD_FOCUS_CHANGED = "PAYLOAD_FOCUS_CHANGED";
    private static final String PAYLOAD_STATE = "PAYLOAD_STATE";
    private static final String PAYLOAD_UPDATE_PIVOT = "PAYLOAD_UPDATE_PIVOT";
    private static final String TAG = "FavLaunchItemsAdapter";
    private static final int TYPE_APP = 0;
    private static final int TYPE_MORE = 1;
    /* access modifiers changed from: private */
    public int appState;
    /* access modifiers changed from: private */
    public final float bannerFocusedElevation;
    /* access modifiers changed from: private */
    public final float bannerFocusedScale;
    /* access modifiers changed from: private */
    public final int bannerHeight;
    /* access modifiers changed from: private */
    public final int bannerWidth;
    /* access modifiers changed from: private */
    public final LaunchItemsManager dataManager;
    /* access modifiers changed from: private */
    public final int defaultAboveSelectedBottomMargin;
    /* access modifiers changed from: private */
    public final int defaultBottomMargin;
    /* access modifiers changed from: private */
    public final int defaultHorizontalMargin;
    /* access modifiers changed from: private */
    public final int defaultTopMargin;
    /* access modifiers changed from: private */
    public FavoriteLaunchItemsRowEditModeActionCallbacks editModeCallbacks;
    private final EventLogger eventLogger;
    /* access modifiers changed from: private */
    public final ScaleFocusHandler focusHandlerTemplate;
    /* access modifiers changed from: private */
    public Handler handler;
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider homeListStateProvider;
    /* access modifiers changed from: private */
    public int lastUnfocusedAdapterPosition;
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider listStateProvider;
    private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    /* access modifiers changed from: private */
    public final Drawable placeholderBanner;
    /* access modifiers changed from: private */
    public RecyclerView recyclerView;
    /* access modifiers changed from: private */
    public final int zoomedOutBottomMargin;
    /* access modifiers changed from: private */
    public final int zoomedOutHorizontalMargin;
    /* access modifiers changed from: private */
    public final int zoomedOutTopMargin;

    public /* bridge */ /* synthetic */ void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i, List list) {
        onBindViewHolder((BaseViewHolder) viewHolder, i, (List<Object>) list);
    }

    FavoriteLaunchItemsAdapter(Context context, EventLogger eventLogger2) {
        this(context, eventLogger2, LaunchItemsManagerProvider.getInstance(context));
    }

    FavoriteLaunchItemsAdapter(Context context, EventLogger eventLogger2, LaunchItemsManager launchItemsManager) {
        this.appState = 0;
        this.lastUnfocusedAdapterPosition = -1;
        this.handler = new Handler();
        this.dataManager = launchItemsManager;
        this.eventLogger = eventLogger2;
        this.dataManager.setHomeScreenItemsChangeListener(new LaunchItemsManager.HomeScreenItemsChangeListener() {
            public void onHomeScreenItemsLoaded() {
                int unused = FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
                FavoriteLaunchItemsAdapter.this.notifyDataSetChanged();
            }

            public void onHomeScreenItemsChanged(List<LaunchItem> list) {
                int unused = FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
                FavoriteLaunchItemsAdapter.this.notifyDataSetChanged();
            }

            public void onHomeScreenItemsSwapped(int fromDisplayedPosition, int toDisplayedPosition) {
                int unused = FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
                FavoriteLaunchItemsAdapter.this.notifyItemMoved(fromDisplayedPosition, toDisplayedPosition);
            }
        });
        Resources resources = context.getResources();
        this.bannerFocusedScale = resources.getFraction(C1167R.fraction.home_app_banner_focused_scale, 1, 1);
        this.bannerFocusedElevation = resources.getDimension(C1167R.dimen.home_app_banner_focused_elevation);
        this.focusHandlerTemplate = new ScaleFocusHandler(resources.getInteger(C1167R.integer.home_app_banner_focused_animation_duration_ms), this.bannerFocusedScale, this.bannerFocusedElevation);
        this.defaultTopMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_default_margin_top);
        this.defaultBottomMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_default_margin_bottom);
        this.defaultHorizontalMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_default_margin_horizontal);
        this.defaultAboveSelectedBottomMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_default_above_selected_margin_bottom);
        this.zoomedOutHorizontalMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_zoomed_out_margin_horizontal);
        this.zoomedOutTopMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_zoomed_out_margin_top);
        this.zoomedOutBottomMargin = resources.getDimensionPixelSize(C1167R.dimen.home_app_banner_zoomed_out_margin_bottom);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.bannerWidth = resources.getDimensionPixelSize(C1167R.dimen.app_banner_image_max_width);
        this.bannerHeight = resources.getDimensionPixelSize(C1167R.dimen.app_banner_image_max_height);
        setHasStableIds(true);
        if (!this.dataManager.areItemsLoaded() || this.dataManager.hasPendingLoadRequest()) {
            this.dataManager.refreshLaunchItems();
        }
    }

    /* access modifiers changed from: package-private */
    public int getAppState() {
        return this.appState;
    }

    /* access modifiers changed from: package-private */
    public void setAppState(int state) {
        if (this.appState != state) {
            this.appState = state;
            this.lastUnfocusedAdapterPosition = -1;
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppsRowEditModeActionCallbacks(FavoriteLaunchItemsRowEditModeActionCallbacks callbacks) {
        this.editModeCallbacks = callbacks;
    }

    /* access modifiers changed from: package-private */
    public void setListStateProvider(RecyclerViewStateProvider listStateProvider2) {
        this.listStateProvider = listStateProvider2;
    }

    /* access modifiers changed from: package-private */
    public void setHomeListStateProvider(RecyclerViewStateProvider homeListStateProvider2) {
        this.homeListStateProvider = homeListStateProvider2;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2) {
        this.onHomeNotHandledListener = onHomeNotHandledListener2;
    }

    /* access modifiers changed from: package-private */
    public void setOnBackNotHandledListener(BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2) {
        this.onBackNotHandledListener = onBackNotHandledListener2;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0) {
            return new AddMoreViewHolder(this, LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_home_add_more_apps_banner, parent, false));
        }
        AppViewHolder appViewHolder = new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_app_banner_home, parent, false));
        appViewHolder.setOnHomeNotHandledListener(this.onHomeNotHandledListener);
        appViewHolder.setOnBackNotHandledListener(this.onBackNotHandledListener);
        return appViewHolder;
    }

    public int getItemViewType(int position) {
        LaunchItem item = this.dataManager.getHomeScreenItems().get(position);
        if (this.dataManager.isFavorite(item) || this.dataManager.isPinnedFavorite(item)) {
            return 0;
        }
        return 1;
    }

    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.setItem(this.dataManager.getHomeScreenItems().get(position));
        holder.updateSize();
        holder.updateBannerDimmingFactor();
        if (Util.areHomeScreenAnimationsEnabled(holder.itemView.getContext())) {
            holder.updateFocusedState();
        } else {
            holder.focusHandler.resetFocusedState();
        }
        if (holder instanceof AppViewHolder) {
            ((AppViewHolder) holder).updateAccessibilityContextMenuIfNeeded();
        }
    }

    public void onBindViewHolder(BaseViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        if (payloads.contains(PAYLOAD_STATE)) {
            holder.updateSize();
            holder.updateBannerDimmingFactor();
        }
        if ((payloads.contains("PAYLOAD_FOCUS_CHANGED") || payloads.contains(PAYLOAD_STATE) || payloads.contains(PAYLOAD_UPDATE_PIVOT)) && Util.areHomeScreenAnimationsEnabled(holder.itemView.getContext())) {
            holder.updateFocusedState();
        }
    }

    public int getItemCount() {
        if (this.dataManager.areItemsLoaded()) {
            return this.dataManager.getHomeScreenItems().size();
        }
        return 0;
    }

    public long getItemId(int position) {
        return (long) this.dataManager.getHomeScreenItems().get(position).getPackageName().hashCode();
    }

    public void log(LogEvent event) {
        event.pushParentVisualElementTag(TvLauncherConstants.FAVORITE_APPS_CONTAINER);
        this.eventLogger.log(event);
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = recyclerView2;
        TvHomeDrawnManager.getInstance().monitorChannelItemListViewDrawn(this.recyclerView);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = null;
    }

    class AppViewHolder extends BaseViewHolder implements View.OnClickListener, View.OnLongClickListener, OnEditModeFocusSearchCallback, BannerView.OnWindowVisibilityChangedListener, BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnBackPressedListener {
        static final int ACCESSIBILITY_MENU_DONE = 2;
        static final int ACCESSIBILITY_MENU_LEFT = 0;
        static final int ACCESSIBILITY_MENU_RIGHT = 1;
        private static final int MENU_FAVORITE = 2;
        private static final int MENU_MOVE = 1;
        private static final int MENU_PRIMARY_ACTION = 0;
        private AccessibilityContextMenu accessibilityContextMenu;
        /* access modifiers changed from: private */
        public int adapterPositionAfterMovement;
        /* access modifiers changed from: private */
        public int adapterPositionBeforeMovement;
        /* access modifiers changed from: private */
        public final BannerView banner;
        private ContextMenu contextMenu;
        private Runnable notifyPivotChangedRunnable = new Runnable() {
            public void run() {
                FavoriteLaunchItemsAdapter.this.notifyItemChanged(AppViewHolder.this.adapterPositionBeforeMovement, FavoriteLaunchItemsAdapter.PAYLOAD_UPDATE_PIVOT);
                FavoriteLaunchItemsAdapter.this.notifyItemChanged(AppViewHolder.this.adapterPositionAfterMovement, FavoriteLaunchItemsAdapter.PAYLOAD_UPDATE_PIVOT);
            }
        };
        private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
        private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
        private boolean pinned;

        AppViewHolder(View v) {
            super(v);
            this.banner = (BannerView) v;
            if (!Util.areHomeScreenAnimationsEnabled(this.context)) {
                this.focusHandler.setPivotProvider(new ScaleFocusHandler.PivotProvider(FavoriteLaunchItemsAdapter.this) {
                    public int getPivot() {
                        if (AppViewHolder.this.getAdapterPosition() == 0) {
                            return 1;
                        }
                        return 0;
                    }

                    public boolean shouldAnimate() {
                        return AppViewHolder.this.banner.isBeingEdited() && AppViewHolder.this.getAdapterPosition() <= 1;
                    }
                });
            }
            this.banner.setOnClickListener(this);
            this.banner.setOnLongClickListener(this);
            this.banner.setOnEditModeFocusSearchCallback(this);
            this.banner.setOnWindowVisibilityChangedListener(this);
            this.banner.setDefaultScaleAnimationsEnabled(false);
        }

        public void setItem(LaunchItem item) {
            super.setItem(item);
            this.pinned = FavoriteLaunchItemsAdapter.this.dataManager.isPinnedFavorite(item);
            this.banner.setLaunchItem(item);
            new LaunchItemImageLoader(this.banner.getContext()).setLaunchItemImageDataSource(new LaunchItemImageDataSource(item, PackageImageDataSource.ImageType.BANNER, LaunchItemsManagerProvider.getInstance(this.context).getCurrentLocale())).setTargetImageView(this.banner.getBannerImage()).setPlaceholder(FavoriteLaunchItemsAdapter.this.placeholderBanner).setWidth(FavoriteLaunchItemsAdapter.this.bannerWidth).setHeight(FavoriteLaunchItemsAdapter.this.bannerHeight).loadLaunchItemImage();
        }

        /* access modifiers changed from: package-private */
        public BannerView getBanner() {
            return this.banner;
        }

        /* access modifiers changed from: package-private */
        public AccessibilityContextMenu getAccessibilityContextMenu() {
            return this.accessibilityContextMenu;
        }

        /* access modifiers changed from: package-private */
        public void onEnterEditModeView() {
            if (FavoriteLaunchItemsAdapter.this.editModeCallbacks != null) {
                FavoriteLaunchItemsAdapter.this.editModeCallbacks.onEnterEditMode();
            }
            this.banner.setIsBeingEdited(true);
            if (Util.isAccessibilityEnabled(this.context)) {
                showAccessibilityMenu();
            }
        }

        /* access modifiers changed from: package-private */
        public void onExitEditModeView() {
            if (FavoriteLaunchItemsAdapter.this.editModeCallbacks != null) {
                FavoriteLaunchItemsAdapter.this.editModeCallbacks.onExitEditMode();
            }
            this.banner.setIsBeingEdited(false);
            AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
            if (accessibilityContextMenu2 != null) {
                accessibilityContextMenu2.dismiss();
            }
        }

        /* access modifiers changed from: package-private */
        public void onPrimaryAction(LaunchItem item, View view) {
            try {
                String packageName = LogUtils.getPackage(item.getIntent());
                if (packageName == null) {
                    packageName = item.getPackageName();
                }
                LogEvent event = new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.START_APP).setVisualElementTag(TvLauncherConstants.LAUNCH_ITEM).setVisualElementIndex(getAdapterPosition());
                event.getApplication().setPackageName(packageName);
                FavoriteLaunchItemsAdapter.this.log(event);
                LaunchUtil.startActivityWithAnimation(item.getIntent(), view);
            } catch (ActivityNotFoundException | SecurityException e) {
                Toast.makeText(this.context, C1167R.string.failed_launch, 0).show();
                String valueOf = String.valueOf(e);
                StringBuilder sb = new StringBuilder(valueOf.length() + 24);
                sb.append("Cannot start activity : ");
                sb.append(valueOf);
                Log.e(FavoriteLaunchItemsAdapter.TAG, sb.toString());
            }
        }

        /* access modifiers changed from: package-private */
        public void onUnFavorite(LaunchItem item) {
            LogEvent event = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.UNFAVORITE_APP);
            if (item.isAppLink()) {
                event.getAppLink().setPackageName(item.getPackageName());
                if (item.getDataUri() != null) {
                    event.getAppLink().setUri(item.getDataUri());
                }
            }
            FavoriteLaunchItemsAdapter.this.log(event);
            FavoriteLaunchItemsAdapter.this.dataManager.removeFromFavorites(item);
        }

        public void onClick(View view) {
            if (Util.isAccessibilityEnabled(this.context)) {
                showContextMenu(this.banner);
            } else if (this.banner.isBeingEdited()) {
                onExitEditModeView();
            } else {
                onPrimaryAction(this.banner.getItem(), view);
            }
        }

        public boolean onLongClick(View v) {
            if (this.banner.isBeingEdited()) {
                return true;
            }
            showContextMenu(this.banner);
            return true;
        }

        public View onEditModeFocusSearch(int direction, View searchedView) {
            TvlauncherLogEnum.TvLauncherEventCode eventCode;
            boolean isBeingEdited = this.banner.isBeingEdited();
            if (isBeingEdited && (!(searchedView instanceof BannerView) || FavoriteLaunchItemsAdapter.this.dataManager.isPinnedFavorite(((BannerView) searchedView).getItem()))) {
                return this.banner;
            }
            if (!isBeingEdited) {
                return searchedView;
            }
            LaunchItem item = this.banner.getItem();
            LaunchItem searchedItem = ((BannerView) searchedView).getItem();
            this.adapterPositionBeforeMovement = getAdapterPosition();
            FavoriteLaunchItemsAdapter.this.dataManager.swapFavoriteAppOrder(item, searchedItem);
            this.adapterPositionAfterMovement = getAdapterPosition();
            if (!Util.areHomeScreenAnimationsEnabled(this.context)) {
                this.focusHandler.animateFocusedState(true);
            } else if (this.adapterPositionBeforeMovement <= 1 && this.adapterPositionAfterMovement <= 1) {
                FavoriteLaunchItemsAdapter.this.handler.removeCallbacks(this.notifyPivotChangedRunnable);
                if (FavoriteLaunchItemsAdapter.this.recyclerView == null || FavoriteLaunchItemsAdapter.this.recyclerView.isComputingLayout()) {
                    FavoriteLaunchItemsAdapter.this.handler.post(this.notifyPivotChangedRunnable);
                } else {
                    this.notifyPivotChangedRunnable.run();
                }
            }
            updateAccessibilityContextMenuIfNeeded();
            int moveDirection = direction;
            if (!(moveDirection == 66 || moveDirection == 17)) {
                moveDirection = this.adapterPositionAfterMovement > this.adapterPositionBeforeMovement ? 66 : 17;
            }
            if (moveDirection == 17) {
                eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_LEFT;
            } else if (moveDirection == 66) {
                eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_RIGHT;
            } else {
                StringBuilder sb = new StringBuilder(30);
                sb.append("Invalid direction: ");
                sb.append(direction);
                throw new IllegalArgumentException(sb.toString());
            }
            LogEvent logEvent = new LogEvent(eventCode).setVisualElementTag(TvLauncherConstants.LAUNCH_ITEM).setVisualElementIndex(FavoriteLaunchItemsAdapter.this.dataManager.getOrderedFavoritePosition(item));
            logEvent.getApplication().setPackageName(item.getPackageName());
            FavoriteLaunchItemsAdapter.this.log(logEvent);
            return this.banner;
        }

        /* access modifiers changed from: protected */
        public void handleFocusChange(boolean hasFocus) {
            super.handleFocusChange(hasFocus);
            if (this.banner.isBeingEdited() && !hasFocus) {
                onExitEditModeView();
            }
            ContextMenu contextMenu2 = this.contextMenu;
            if (contextMenu2 != null && contextMenu2.isShowing()) {
                this.contextMenu.forceDismiss();
            }
        }

        /* access modifiers changed from: package-private */
        public ContextMenu getContextMenu() {
            return this.contextMenu;
        }

        /* access modifiers changed from: package-private */
        public void showContextMenu(BannerView bannerView) {
            if (!bannerView.hasFocus()) {
                return;
            }
            if (FavoriteLaunchItemsAdapter.this.listStateProvider != null && FavoriteLaunchItemsAdapter.this.listStateProvider.isAnimating()) {
                return;
            }
            if (FavoriteLaunchItemsAdapter.this.homeListStateProvider == null || !FavoriteLaunchItemsAdapter.this.homeListStateProvider.isAnimating()) {
                LaunchItem item = bannerView.getItem();
                this.contextMenu = new ContextMenu((Activity) this.context, bannerView.getBannerContainer(), bannerView.getCornerRadius(), bannerView.getScaleX(), bannerView.getScaleY());
                ContextMenuItem primaryActionItem = new ContextMenuItem(0, this.context.getString(C1167R.string.context_menu_primary_action_text), this.context.getDrawable(C1167R.C1168drawable.ic_context_menu_open_black));
                primaryActionItem.setAutoDismiss(false);
                this.contextMenu.addItem(primaryActionItem);
                if (this.pinned) {
                    ContextMenuItem menuCantMoveItem = new ContextMenuItem(1, this.context.getString(C1167R.string.context_menu_can_not_move_text), this.context.getDrawable(C1167R.C1168drawable.ic_context_menu_move_left_right_black));
                    menuCantMoveItem.setEnabled(false);
                    ContextMenuItem menuCantRemove = new ContextMenuItem(2, this.context.getString(C1167R.string.context_menu_can_not_remove_text), this.context.getDrawable(C1167R.C1168drawable.ic_context_menu_unfavorite_black));
                    menuCantRemove.setEnabled(false);
                    this.contextMenu.addItem(menuCantMoveItem);
                    this.contextMenu.addItem(menuCantRemove);
                } else {
                    this.contextMenu.addItem(new ContextMenuItem(1, this.context.getString(C1167R.string.context_menu_move_text), this.context.getDrawable(C1167R.C1168drawable.ic_context_menu_move_left_right_black)));
                    this.contextMenu.addItem(new ContextMenuItem(2, this.context.getString(C1167R.string.context_menu_unfavorite_text), this.context.getDrawable(C1167R.C1168drawable.ic_context_menu_unfavorite_black)));
                    this.contextMenu.findItem(1).setEnabled(!FavoriteLaunchItemsAdapter.this.dataManager.isOnlyFavorite(item));
                }
                this.contextMenu.setOnMenuItemClickListener(createOnItemClickListener(item, bannerView));
                this.contextMenu.show();
            }
        }

        private ContextMenu.OnItemClickListener createOnItemClickListener(LaunchItem launchItem, View view) {
            return new FavoriteLaunchItemsAdapter$AppViewHolder$$Lambda$0(this, launchItem, view);
        }

        /* access modifiers changed from: package-private */
        /* renamed from: lambda$createOnItemClickListener$0$FavoriteLaunchItemsAdapter$AppViewHolder */
        public final /* synthetic */ void mo21543x708b30d3(LaunchItem launchItem, View view, ContextMenuItem item) {
            int id = item.getId();
            if (id == 0) {
                onPrimaryAction(launchItem, view);
            } else if (id == 1) {
                onEnterEditModeView();
            } else if (id == 2) {
                onUnFavorite(launchItem);
            }
        }

        private void showAccessibilityMenu() {
            if (this.accessibilityContextMenu == null) {
                this.accessibilityContextMenu = new AccessibilityContextMenu((Activity) this.context);
                ContextMenuItem menuLeft = new ContextMenuItem(0, this.context.getString(C1167R.string.accessibility_menu_item_move_left), this.context.getDrawable(C1167R.C1168drawable.ic_arrow_left_black_24dp));
                ContextMenuItem menuRight = new ContextMenuItem(1, this.context.getString(C1167R.string.accessibility_menu_item_move_right), this.context.getDrawable(C1167R.C1168drawable.ic_arrow_right_black_24dp));
                ContextMenuItem menuDone = new ContextMenuItem(2, this.context.getString(C1167R.string.accessibility_menu_item_done), this.context.getDrawable(C1167R.C1168drawable.ic_done_black_24dp));
                if (Util.isRtl(this.context)) {
                    this.accessibilityContextMenu.addItem(menuRight);
                    this.accessibilityContextMenu.addItem(menuLeft);
                    this.accessibilityContextMenu.addItem(menuDone);
                } else {
                    this.accessibilityContextMenu.addItem(menuLeft);
                    this.accessibilityContextMenu.addItem(menuRight);
                    this.accessibilityContextMenu.addItem(menuDone);
                }
                this.accessibilityContextMenu.setOnMenuItemClickListener(new FavoriteLaunchItemsAdapter$AppViewHolder$$Lambda$1(this));
                this.accessibilityContextMenu.setOnDismissListener(new FavoriteLaunchItemsAdapter$AppViewHolder$$Lambda$2(this));
            }
            updateAccessibilityContextMenu();
            this.accessibilityContextMenu.show();
        }

        /* access modifiers changed from: package-private */
        /* renamed from: lambda$showAccessibilityMenu$1$FavoriteLaunchItemsAdapter$AppViewHolder */
        public final /* synthetic */ void mo21544x32081830(ContextMenuItem item) {
            int id = item.getId();
            if (id == 0) {
                this.banner.focusSearch(17);
            } else if (id == 1) {
                this.banner.focusSearch(66);
            } else if (id == 2) {
                this.accessibilityContextMenu.dismiss();
            }
        }

        public void onWindowVisibilityChanged(int visibility) {
            ContextMenu contextMenu2;
            if ((visibility == 4 || visibility == 8) && (contextMenu2 = this.contextMenu) != null && contextMenu2.isShowing()) {
                this.contextMenu.forceDismiss();
            }
        }

        /* access modifiers changed from: package-private */
        public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2) {
            this.onHomeNotHandledListener = onHomeNotHandledListener2;
        }

        /* access modifiers changed from: package-private */
        public void setOnBackNotHandledListener(BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2) {
            this.onBackNotHandledListener = onBackNotHandledListener2;
        }

        public void onHomePressed(Context c) {
            BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2;
            if (!handleHomeBackPress() && (onHomeNotHandledListener2 = this.onHomeNotHandledListener) != null) {
                onHomeNotHandledListener2.onHomeNotHandled(c);
            }
        }

        public void onBackPressed(Context c) {
            BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2;
            if (!handleHomeBackPress() && (onBackNotHandledListener2 = this.onBackNotHandledListener) != null) {
                onBackNotHandledListener2.onBackNotHandled(c);
            }
        }

        private boolean handleHomeBackPress() {
            if (this.banner.isBeingEdited()) {
                onExitEditModeView();
                return true;
            }
            ContextMenu contextMenu2 = this.contextMenu;
            if (contextMenu2 == null || !contextMenu2.isShowing()) {
                return false;
            }
            this.contextMenu.forceDismiss();
            return true;
        }

        /* access modifiers changed from: package-private */
        public void updateFocusedState() {
            super.updateFocusedState();
            ((BannerView) this.itemView).setTitleVisibility(this.itemView.isFocused() ? 0 : 4);
        }

        private void updateAccessibilityContextMenu() {
            boolean z = true;
            if (Util.isRtl(this.context)) {
                this.accessibilityContextMenu.findItem(0).setEnabled(getAdapterPosition() < FavoriteLaunchItemsAdapter.this.getItemCount() + -2);
                ContextMenuItem findItem = this.accessibilityContextMenu.findItem(1);
                if (getAdapterPosition() <= 0) {
                    z = false;
                }
                findItem.setEnabled(z);
                return;
            }
            this.accessibilityContextMenu.findItem(0).setEnabled(getAdapterPosition() > 0);
            ContextMenuItem findItem2 = this.accessibilityContextMenu.findItem(1);
            if (getAdapterPosition() >= FavoriteLaunchItemsAdapter.this.getItemCount() - 2) {
                z = false;
            }
            findItem2.setEnabled(z);
        }

        /* access modifiers changed from: private */
        public void updateAccessibilityContextMenuIfNeeded() {
            AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
            if (accessibilityContextMenu2 != null && accessibilityContextMenu2.isShowing()) {
                updateAccessibilityContextMenu();
            }
        }
    }

    class AddMoreViewHolder extends BaseViewHolder implements View.OnClickListener {
        private Intent intent;

        AddMoreViewHolder(final FavoriteLaunchItemsAdapter this$0, View v) {
            super(v);
            this.itemView.setOnClickListener(this);
            final int cornerRadius = this.itemView.getResources().getDimensionPixelSize(C1167R.dimen.card_rounded_corner_radius);
            this.imageView.setOutlineProvider(new ViewOutlineProvider(this) {
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) cornerRadius);
                }
            });
            this.imageView.setClipToOutline(true);
            this.itemView.setOutlineProvider(new ViewOutlineProvider(this) {
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getResources().getDimensionPixelSize(C1167R.dimen.home_app_banner_width), view.getResources().getDimensionPixelSize(C1167R.dimen.home_app_banner_image_height), (float) cornerRadius);
                }
            });
            if (!Util.areHomeScreenAnimationsEnabled(this.context)) {
                this.focusHandler.setPivotProvider(new ScaleFocusHandler.PivotProvider() {
                    public int getPivot() {
                        if (AddMoreViewHolder.this.getAdapterPosition() == 0) {
                            return 1;
                        }
                        return 0;
                    }

                    public boolean shouldAnimate() {
                        return false;
                    }
                });
            }
        }

        public void setItem(LaunchItem item) {
            super.setItem(item);
            this.intent = item.getIntent();
        }

        public void onClick(View v) {
            try {
                this.context.startActivity(this.intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this.context, C1167R.string.failed_launch, 0).show();
                String valueOf = String.valueOf(e);
                StringBuilder sb = new StringBuilder(valueOf.length() + 24);
                sb.append("Cannot start activity : ");
                sb.append(valueOf);
                Log.e(FavoriteLaunchItemsAdapter.TAG, sb.toString());
            }
        }

        /* access modifiers changed from: package-private */
        public void updateFocusedState() {
            super.updateFocusedState();
            ((AddFavoriteAppCardView) this.itemView).setTitleVisibility(this.itemView.isFocused() ? 0 : 4);
        }
    }

    abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        final Context context;
        ScaleFocusHandler focusHandler;
        ImageView imageView;
        private Runnable notifyFocusChangedRunnable = new FavoriteLaunchItemsAdapter$BaseViewHolder$$Lambda$0(this);
        int pivotVerticalShift;
        TextView titleView;

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$0$FavoriteLaunchItemsAdapter$BaseViewHolder() {
            FavoriteLaunchItemsAdapter.this.notifyItemChanged(getAdapterPosition(), "PAYLOAD_FOCUS_CHANGED");
            if (FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition != -1) {
                FavoriteLaunchItemsAdapter favoriteLaunchItemsAdapter = FavoriteLaunchItemsAdapter.this;
                favoriteLaunchItemsAdapter.notifyItemChanged(favoriteLaunchItemsAdapter.lastUnfocusedAdapterPosition, "PAYLOAD_FOCUS_CHANGED");
                int unused = FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            }
        }

        BaseViewHolder(View v) {
            super(v);
            this.context = v.getContext();
            this.titleView = (TextView) v.findViewById(C1167R.C1170id.app_title);
            this.imageView = (ImageView) v.findViewById(C1167R.C1170id.banner_image);
            this.pivotVerticalShift = (-v.getResources().getDimensionPixelSize(C1167R.dimen.app_banner_title_height)) / 2;
            View.OnFocusChangeListener onFocusChangeListener = new FavoriteLaunchItemsAdapter$BaseViewHolder$$Lambda$1(this);
            if (!Util.areHomeScreenAnimationsEnabled(this.context)) {
                this.focusHandler = new ScaleFocusHandler(FavoriteLaunchItemsAdapter.this.focusHandlerTemplate);
                this.focusHandler.setView(v);
                this.focusHandler.setOnFocusChangeListener(onFocusChangeListener);
                this.focusHandler.setPivotVerticalShift(this.pivotVerticalShift);
                return;
            }
            v.setOnFocusChangeListener(onFocusChangeListener);
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$1$FavoriteLaunchItemsAdapter$BaseViewHolder(View view, boolean hasFocus) {
            handleFocusChange(hasFocus);
        }

        /* access modifiers changed from: protected */
        public void handleFocusChange(boolean hasFocus) {
            if (!Util.areHomeScreenAnimationsEnabled(this.context)) {
                handleFocusChangeWhenAnimationsDisabled(hasFocus);
                return;
            }
            FavoriteLaunchItemsAdapter.this.handler.removeCallbacks(this.notifyFocusChangedRunnable);
            if (!hasFocus) {
                int unused = FavoriteLaunchItemsAdapter.this.lastUnfocusedAdapterPosition = getAdapterPosition();
            } else if (FavoriteLaunchItemsAdapter.this.recyclerView == null || FavoriteLaunchItemsAdapter.this.recyclerView.isComputingLayout()) {
                FavoriteLaunchItemsAdapter.this.handler.post(this.notifyFocusChangedRunnable);
            } else {
                this.notifyFocusChangedRunnable.run();
            }
        }

        private void handleFocusChangeWhenAnimationsDisabled(boolean hasFocus) {
            TextView textView = this.titleView;
            if (textView != null) {
                textView.setSelected(this.itemView.hasFocus());
                this.itemView.postDelayed(new FavoriteLaunchItemsAdapter$BaseViewHolder$$Lambda$2(this, hasFocus), 60);
            }
        }

        /* access modifiers changed from: package-private */
        /* renamed from: lambda$handleFocusChangeWhenAnimationsDisabled$2$FavoriteLaunchItemsAdapter$BaseViewHolder */
        public final /* synthetic */ void mo21555xe3903b3a(boolean hasFocus) {
            this.titleView.animate().alpha(hasFocus ? 1.0f : 0.0f).setDuration((long) FavoriteLaunchItemsAdapter.this.focusHandlerTemplate.getAnimationDuration()).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    BaseViewHolder.this.titleView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animation) {
                    if (BaseViewHolder.this.titleView.getAlpha() == 0.0f) {
                        BaseViewHolder.this.titleView.setVisibility(4);
                    }
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void updateSize() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.itemView.getLayoutParams();
            switch (FavoriteLaunchItemsAdapter.this.appState) {
                case 0:
                case 2:
                    lp.setMargins(0, FavoriteLaunchItemsAdapter.this.defaultTopMargin, 0, FavoriteLaunchItemsAdapter.this.defaultBottomMargin);
                    lp.setMarginEnd(FavoriteLaunchItemsAdapter.this.defaultHorizontalMargin);
                    break;
                case 1:
                    lp.setMargins(0, FavoriteLaunchItemsAdapter.this.defaultTopMargin, 0, FavoriteLaunchItemsAdapter.this.defaultAboveSelectedBottomMargin);
                    lp.setMarginEnd(FavoriteLaunchItemsAdapter.this.defaultHorizontalMargin);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    lp.setMargins(0, FavoriteLaunchItemsAdapter.this.zoomedOutTopMargin, 0, FavoriteLaunchItemsAdapter.this.zoomedOutBottomMargin);
                    lp.setMarginEnd(FavoriteLaunchItemsAdapter.this.zoomedOutHorizontalMargin);
                    break;
            }
            this.itemView.setLayoutParams(lp);
        }

        public void setItem(LaunchItem item) {
        }

        /* access modifiers changed from: package-private */
        public void updateFocusedState() {
            int pivot;
            int pivotX;
            boolean selected = this.itemView.isFocused();
            float scale = selected ? FavoriteLaunchItemsAdapter.this.bannerFocusedScale : 1.0f;
            float elevation = selected ? FavoriteLaunchItemsAdapter.this.bannerFocusedElevation : 0.0f;
            this.itemView.setScaleX(scale);
            this.itemView.setScaleY(scale);
            this.itemView.setElevation(elevation);
            this.titleView.setSelected(selected);
            int width = this.itemView.getLayoutParams().width;
            int height = this.itemView.getLayoutParams().height;
            if (width <= 0 || height <= 0) {
                width = this.itemView.getWidth();
                height = this.itemView.getHeight();
            }
            if (width > 0 && height > 0) {
                if (getAdapterPosition() == 0) {
                    pivot = 1;
                } else {
                    pivot = 0;
                }
                if (pivot == 0) {
                    pivotX = width / 2;
                } else if (this.itemView.getLayoutDirection() == 1) {
                    pivotX = width;
                } else {
                    pivotX = 0;
                }
                this.itemView.setPivotX((float) pivotX);
                this.itemView.setPivotY((float) ((height / 2) + this.pivotVerticalShift));
            }
        }

        /* access modifiers changed from: package-private */
        public void updateBannerDimmingFactor() {
            FavoriteLaunchItemView favoriteLaunchItemView = (FavoriteLaunchItemView) this.itemView;
            boolean z = true;
            if (!(FavoriteLaunchItemsAdapter.this.appState == 0 || FavoriteLaunchItemsAdapter.this.appState == 1 || FavoriteLaunchItemsAdapter.this.appState == 3 || FavoriteLaunchItemsAdapter.this.appState == 5 || FavoriteLaunchItemsAdapter.this.appState == 6 || FavoriteLaunchItemsAdapter.this.appState == 7)) {
                z = false;
            }
            favoriteLaunchItemView.setBannerImageDimmed(z);
        }
    }
}
