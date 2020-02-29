package com.google.android.tvlauncher.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.p004v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import androidx.leanback.widget.HorizontalGridView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.data.ChannelOrderManager;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.RecyclerViewFastScrollingManager;
import com.google.android.tvlauncher.home.util.ChannelUtil;
import com.google.android.tvlauncher.home.util.ProgramStateUtil;
import com.google.android.tvlauncher.home.view.ChannelItemsAnimator;
import com.google.android.tvlauncher.home.view.ChannelView;
import com.google.android.tvlauncher.model.HomeChannel;
import com.google.android.tvlauncher.util.AccessibilityContextMenu;
import com.google.android.tvlauncher.util.ContextMenuItem;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;
import com.google.android.tvlauncher.util.Util;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;

class ChannelRowController implements HomeRow, ChannelView.OnPerformMainActionListener, ChannelView.OnMoveChannelUpListener, ChannelView.OnMoveChannelDownListener, ChannelView.OnRemoveListener, ChannelView.OnStateChangeGesturePerformedListener, ChannelView.OnChannelLogoFocusedListener, EventLogger, BackHomeControllerListeners.OnBackPressedListener, BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnHomeNotHandledListener, RecyclerViewStateProvider, RecyclerViewFastScrollingManager.OnFastScrollingChangedListener {
    private static final int ACCESSIBILITY_MENU_DONE = 4;
    static final int ACCESSIBILITY_MENU_DOWN = 2;
    private static final int ACCESSIBILITY_MENU_OPEN = 0;
    private static final int ACCESSIBILITY_MENU_REMOVE = 3;
    static final int ACCESSIBILITY_MENU_UP = 1;
    private static final boolean DEBUG = false;
    private static final String TAG = "ChannelRowController";
    private AccessibilityContextMenu accessibilityContextMenu;
    private String actionUri;
    private long channelId;
    private final String channelLogoContentDescriptionFormat;
    private final RequestManager channelLogoRequestManager;
    private Uri channelLogoThumbnailUri;
    private final ChannelOrderManager channelOrderManager;
    private final ChannelView channelView;
    private final EventLogger eventLogger;
    private RecyclerViewFastScrollingManager fastScrollingManager;
    private boolean homeIsFastScrolling;
    private RecyclerViewStateProvider homeListStateProvider;
    private final IntentLaunchDispatcher intentLauncher;
    private final boolean isBranded;
    private boolean isLegacy;
    private final boolean isSponsored;
    private ChannelItemsAdapter itemsAdapter;
    private final HorizontalGridView itemsListView;
    private final ImageViewTarget<Bitmap> logoGlideTarget;
    private final ChannelItemMetadataController metadataController;
    private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    private OnHomeRowRemovedListener onHomeRowRemovedListener;
    private OnHomeRowSelectedListener onHomeRowSelectedListener;
    private OnHomeStateChangeListener onHomeStateChangeListener;
    private OnProgramSelectedListener onProgramSelectedListener;
    private String packageName;
    private String title;

    ChannelRowController(ChannelView channelView2, RequestManager channelLogoRequestManager2, EventLogger eventLogger2, ChannelOrderManager channelOrderManager2, ChannelItemMetadataController metadataController2, IntentLaunchDispatcher intentLaunchDispatcher, final boolean isSponsored2, boolean isBranded2) {
        String str;
        this.channelView = channelView2;
        this.channelLogoRequestManager = channelLogoRequestManager2;
        this.eventLogger = eventLogger2;
        this.channelOrderManager = channelOrderManager2;
        this.metadataController = metadataController2;
        if (!isSponsored2) {
            this.channelView.setOnPerformMainActionListener(this);
        }
        this.channelView.setOnMoveUpListener(this);
        this.channelView.setOnMoveDownListener(this);
        this.channelView.setOnRemoveListener(this);
        this.channelView.setOnStateChangeGesturePerformedListener(this);
        this.channelView.setOnChannelLogoFocusedListener(this);
        this.intentLauncher = intentLaunchDispatcher;
        this.isSponsored = isSponsored2;
        this.isBranded = isBranded2;
        if (isSponsored2) {
            this.channelView.setShowItemMeta(false);
            ChannelView channelView3 = this.channelView;
            if (this.isBranded) {
                str = channelView3.getContext().getString(C1167R.string.sponsored_channel_logo_title);
            } else {
                str = channelView3.getContext().getString(C1167R.string.sponsored_channel_unbranded_logo_title);
            }
            channelView3.setLogoTitle(str);
            if (!isBranded2) {
                String title2 = this.channelView.getContext().getString(C1167R.string.sponsored_channel_unbranded_logo_title);
                this.channelView.setZoomedOutLogoTitle(title2);
                this.channelView.setLogoContentDescription(title2);
            }
        }
        this.channelView.setIsSponsored(this.isSponsored, this.isBranded);
        if (this.isSponsored) {
            this.channelView.setStateSettings(ChannelUtil.getSponsoredChannelStateSettings(channelView2.getContext()));
        } else {
            this.channelView.setStateSettings(ChannelUtil.getDefaultChannelStateSettings(channelView2.getContext()));
        }
        this.itemsListView = this.channelView.getItemsListView();
        ChannelUtil.configureItemsListAlignment(this.itemsListView);
        final int programLogoDefaultBackground = channelView2.getContext().getColor(C1167R.color.channel_logo_default_background);
        if (!this.isSponsored || this.isBranded) {
            this.channelView.getChannelLogoImageView().setBackgroundColor(programLogoDefaultBackground);
        } else {
            this.channelView.getChannelLogoImageView().setBackground(null);
            this.channelView.getChannelLogoImageView().setImageDrawable(null);
        }
        this.logoGlideTarget = new ImageViewTarget<Bitmap>(this, this.channelView.getChannelLogoImageView()) {
            /* access modifiers changed from: protected */
            public void setResource(Bitmap bitmap) {
                if (bitmap != null) {
                    ((ImageView) this.view).setImageBitmap(bitmap);
                    if (isSponsored2 || !bitmap.hasAlpha()) {
                        ((ImageView) this.view).setBackground(null);
                    } else {
                        ((ImageView) this.view).setBackgroundColor(programLogoDefaultBackground);
                    }
                } else {
                    ((ImageView) this.view).setImageDrawable(null);
                    ((ImageView) this.view).setBackgroundColor(programLogoDefaultBackground);
                }
            }
        };
        this.channelLogoContentDescriptionFormat = channelView2.getContext().getString(C1167R.string.sponsored_channel_branding);
    }

    public void setOnHomeStateChangeListener(OnHomeStateChangeListener listener) {
        this.onHomeStateChangeListener = listener;
    }

    public void setOnHomeRowSelectedListener(OnHomeRowSelectedListener listener) {
        this.onHomeRowSelectedListener = listener;
    }

    public void setOnHomeRowRemovedListener(OnHomeRowRemovedListener listener) {
        this.onHomeRowRemovedListener = listener;
    }

    public void setHomeIsFastScrolling(boolean homeIsFastScrolling2) {
        if (this.homeIsFastScrolling != homeIsFastScrolling2) {
            this.homeIsFastScrolling = homeIsFastScrolling2;
            updateStateForHomeFastScrolling();
        }
    }

    public void onFastScrollingChanged(boolean fastScrolling) {
        if (fastScrolling) {
            this.itemsAdapter.setProgramState(4, false);
        } else {
            this.itemsAdapter.setProgramState(getProgramState(this.channelView.getState()), true);
        }
    }

    public View getView() {
        return this.channelView;
    }

    /* access modifiers changed from: package-private */
    public void setOnProgramSelectedListener(OnProgramSelectedListener listener) {
        this.onProgramSelectedListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnBackNotHandledListener(BackHomeControllerListeners.OnBackNotHandledListener listener) {
        this.onBackNotHandledListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener listener) {
        this.onHomeNotHandledListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setHomeListStateProvider(RecyclerViewStateProvider homeListStateProvider2) {
        this.homeListStateProvider = homeListStateProvider2;
    }

    private void ensureItemListIsSetUp() {
        if (this.itemsAdapter == null) {
            this.itemsAdapter = new ChannelItemsAdapter(this.channelView.getContext(), this);
            this.itemsAdapter.setIsSponsored(this.isSponsored, this.isBranded);
            this.itemsAdapter.setOnProgramSelectedListener(this.onProgramSelectedListener);
            this.itemsAdapter.setOnHomeNotHandledListener(this);
            this.itemsAdapter.setListStateProvider(this);
            this.itemsAdapter.setHomeListStateProvider(this.homeListStateProvider);
            this.itemsListView.setAdapter(this.itemsAdapter);
            this.fastScrollingManager = new RecyclerViewFastScrollingManager(this.itemsListView, new ChannelItemsAnimator());
            this.fastScrollingManager.setOnFastScrollingChangedListener(this);
            updateStateForHomeFastScrolling();
        }
    }

    private void updateStateForHomeFastScrolling() {
        this.fastScrollingManager.setAnimatorEnabled(!this.homeIsFastScrolling);
        this.fastScrollingManager.setScrollEnabled(false);
    }

    /* access modifiers changed from: package-private */
    public void onStart() {
        this.itemsAdapter.onStart();
    }

    /* access modifiers changed from: package-private */
    public void onStop() {
        this.itemsAdapter.onStop();
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        this.itemsAdapter.recycle();
        this.itemsListView.setSelectedPosition(0);
        this.channelView.recycle();
        this.metadataController.clear();
        this.channelLogoThumbnailUri = null;
        AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
        if (accessibilityContextMenu2 != null) {
            accessibilityContextMenu2.dismiss();
        }
    }

    /* access modifiers changed from: package-private */
    public void bind(HomeChannel channel, int channelViewState, boolean canAddToWatchNext, boolean canRemoveProgram) {
        AccessibilityContextMenu accessibilityContextMenu2;
        ensureItemListIsSetUp();
        if (!(this.accessibilityContextMenu == null || this.channelId == channel.getId())) {
            this.accessibilityContextMenu.dismiss();
        }
        this.channelId = channel.getId();
        this.actionUri = channel.getLaunchUri();
        this.title = channel.getDisplayName();
        this.packageName = channel.getPackageName();
        this.isLegacy = channel.isLegacy();
        this.metadataController.setLegacy(this.isLegacy);
        this.channelView.setAllowMoving(channel.canMove());
        this.channelView.setAllowRemoving(channel.canRemove());
        if (this.isLegacy || this.isSponsored) {
            canAddToWatchNext = false;
            canRemoveProgram = false;
        }
        if (!this.isSponsored) {
            bindChannelLogoTitle();
        } else if (this.isBranded) {
            bindSponsoredChannelLogoContentDescription(channel.getLogoContentDescription());
        }
        if (!this.isSponsored || this.isBranded) {
            this.channelView.setZoomedOutLogoTitle(this.title);
            this.channelView.setItemsTitle(this.title);
        }
        bindChannelMoveAction();
        if (!ChannelUtil.isEmptyState(channelViewState) || (accessibilityContextMenu2 = this.accessibilityContextMenu) == null || !accessibilityContextMenu2.isShowing()) {
            updateAccessibilityContextMenuIfNeeded();
        } else {
            this.accessibilityContextMenu.dismiss();
        }
        if (!this.isSponsored || this.isBranded) {
            Uri logoUri = TvDataManager.getInstance(this.channelView.getContext()).getChannelLogoUri(Long.valueOf(channel.getId()));
            this.channelLogoRequestManager.asBitmap().load(logoUri).thumbnail(this.channelLogoRequestManager.asBitmap().load(this.channelLogoThumbnailUri)).into(this.logoGlideTarget);
            this.channelLogoThumbnailUri = logoUri;
        }
        this.channelView.setState(channelViewState);
        int oldProgramState = this.itemsAdapter.getProgramState();
        int newProgramState = getProgramState(channelViewState);
        this.itemsAdapter.bind(channel.getId(), this.packageName, newProgramState, canAddToWatchNext, canRemoveProgram, this.isLegacy);
        updateItemsListPosition(newProgramState, oldProgramState);
    }

    /* access modifiers changed from: package-private */
    public void bindChannelMoveAction() {
        this.channelView.updateChannelMoveAction(this.channelOrderManager.canMoveChannelUp(this.channelId), this.channelOrderManager.canMoveChannelDown(this.channelId));
    }

    /* access modifiers changed from: package-private */
    public void bindChannelLogoTitle() {
        LaunchItem launchItem = LaunchItemsManagerProvider.getInstance(this.channelView.getContext()).getLaunchItem(this.packageName);
        String title2 = launchItem != null ? launchItem.getLabel().toString() : "";
        this.channelView.setLogoTitle(title2);
        this.channelView.setLogoContentDescription(title2);
    }

    private void bindSponsoredChannelLogoContentDescription(String contentDescription) {
        this.channelView.setLogoContentDescription(String.format(this.channelLogoContentDescriptionFormat, contentDescription));
    }

    public void setState(int channelViewState) {
        AccessibilityContextMenu accessibilityContextMenu2;
        if (this.channelView.getState() != channelViewState && this.fastScrollingManager.isFastScrollingEnabled() && this.itemsListView.getSelectedPosition() == 0) {
            this.fastScrollingManager.setAnimatorEnabled(!this.homeIsFastScrolling);
        }
        this.channelView.setState(channelViewState);
        int oldProgramState = this.itemsAdapter.getProgramState();
        int newProgramState = getProgramState(channelViewState);
        this.itemsAdapter.setProgramState(newProgramState);
        updateItemsListPosition(newProgramState, oldProgramState);
        if (ChannelUtil.isEmptyState(channelViewState) && (accessibilityContextMenu2 = this.accessibilityContextMenu) != null && accessibilityContextMenu2.isShowing()) {
            this.accessibilityContextMenu.dismiss();
        }
    }

    private void updateItemsListPosition(int newState, int oldState) {
        if (newState != oldState && ProgramStateUtil.isZoomedOutState(newState) && this.itemsAdapter.getItemCount() > 1 && this.itemsListView.getSelectedPosition() != 0) {
            this.itemsListView.scrollToPosition(0);
        }
    }

    /* access modifiers changed from: package-private */
    public AccessibilityContextMenu getAccessibilityContextMenu() {
        return this.accessibilityContextMenu;
    }

    private int getProgramState(int channelViewState) {
        switch (channelViewState) {
            case 0:
                return 3;
            case 1:
            case 2:
            case 3:
                return 0;
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
            case 7:
                return 4;
            case 8:
                return 6;
            case 9:
                return 5;
            case 10:
                return 7;
            case 11:
                return 9;
            case 12:
                return 8;
            case 13:
                return 11;
            case 14:
                return 10;
            case 15:
                return 12;
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return 0;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void bindItemMetadata() {
        if (!this.isSponsored) {
            int position = this.itemsListView.getSelectedPosition();
            TvDataManager dataManager = TvDataManager.getInstance(this.itemsListView.getContext());
            if (position == -1 || this.itemsAdapter.getItemCount() == 0) {
                this.metadataController.clear();
            } else if (position >= 0 && position < dataManager.getProgramCount(this.channelId)) {
                this.metadataController.bindView(dataManager.getProgram(this.channelId, position));
            }
        }
    }

    public void onPerformMainAction(ChannelView v) {
        if (ChannelUtil.isEmptyState(v.getState()) || !Util.isAccessibilityEnabled(v.getContext())) {
            performMainChannelAction(v.getChannelLogoImageView());
        } else {
            showAccessibilityMenu();
        }
    }

    private void performMainChannelAction(View view) {
        if (this.actionUri != null) {
            logChannelAction(this.channelView.getContext(), TvlauncherLogEnum.TvLauncherEventCode.START_APP, TvLauncherConstants.CHANNEL_TITLE_BUTTON);
            this.intentLauncher.launchChannelIntentFromUriWithAnimation(this.packageName, this.actionUri, false, view);
        }
    }

    private void showAccessibilityMenu() {
        if (this.accessibilityContextMenu == null) {
            Context context = this.channelView.getContext();
            this.accessibilityContextMenu = new AccessibilityContextMenu((Activity) context);
            this.accessibilityContextMenu.addItem(new ContextMenuItem(0, context.getString(C1167R.string.context_menu_primary_action_text), context.getDrawable(C1167R.C1168drawable.ic_context_menu_open_black)));
            this.accessibilityContextMenu.addItem(new ContextMenuItem(1, context.getString(C1167R.string.accessibility_menu_item_move_up), context.getDrawable(C1167R.C1168drawable.ic_arrow_up_black_24dp)));
            this.accessibilityContextMenu.addItem(new ContextMenuItem(2, context.getString(C1167R.string.accessibility_menu_item_move_down), context.getDrawable(C1167R.C1168drawable.ic_arrow_down_black_24dp)));
            this.accessibilityContextMenu.addItem(new ContextMenuItem(3, context.getString(C1167R.string.channel_action_remove), context.getDrawable(C1167R.C1168drawable.ic_remove_circle_black)));
            this.accessibilityContextMenu.addItem(new ContextMenuItem(4, context.getString(C1167R.string.accessibility_menu_item_done), context.getDrawable(C1167R.C1168drawable.ic_done_black_24dp)));
            this.accessibilityContextMenu.setOnMenuItemClickListener(new ChannelRowController$$Lambda$0(this));
        }
        updateAccessibilityContextMenu();
        this.accessibilityContextMenu.show();
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$showAccessibilityMenu$0$ChannelRowController(ContextMenuItem item) {
        int id = item.getId();
        if (id == 0) {
            performMainChannelAction(this.channelView.getChannelLogoImageView());
        } else if (id == 1) {
            onMoveChannelUp(this.channelView);
        } else if (id == 2) {
            onMoveChannelDown(this.channelView);
        } else if (id == 3) {
            this.accessibilityContextMenu.dismiss();
            onRemove(this.channelView);
        } else if (id == 4) {
            this.accessibilityContextMenu.dismiss();
        }
    }

    public void onMoveChannelUp(ChannelView v) {
        if (this.channelOrderManager.canMoveChannelUp(this.channelId)) {
            this.channelOrderManager.moveChannelUp(this.channelId);
            logChannelAction(v.getContext(), TvlauncherLogEnum.TvLauncherEventCode.MOVE_CHANNEL_UP, TvLauncherConstants.MOVE_CHANNEL_BUTTON);
            this.channelView.updateChannelMoveAction(this.channelOrderManager.canMoveChannelUp(this.channelId), this.channelOrderManager.canMoveChannelDown(this.channelId));
            updateAccessibilityContextMenuIfNeeded();
        }
    }

    public void onMoveChannelDown(ChannelView v) {
        if (this.channelOrderManager.canMoveChannelDown(this.channelId)) {
            this.channelOrderManager.moveChannelDown(this.channelId);
            logChannelAction(v.getContext(), TvlauncherLogEnum.TvLauncherEventCode.MOVE_CHANNEL_DOWN, TvLauncherConstants.MOVE_CHANNEL_BUTTON);
            this.channelView.updateChannelMoveAction(this.channelOrderManager.canMoveChannelUp(this.channelId), this.channelOrderManager.canMoveChannelDown(this.channelId));
            updateAccessibilityContextMenuIfNeeded();
        }
    }

    public void onRemove(ChannelView v) {
        logChannelAction(v.getContext(), TvlauncherLogEnum.TvLauncherEventCode.REMOVE_CHANNEL, TvLauncherConstants.REMOVE_CHANNEL_BUTTON);
        TvDataManager.getInstance(v.getContext()).removeHomeChannel(this.channelId);
        OnHomeRowRemovedListener onHomeRowRemovedListener2 = this.onHomeRowRemovedListener;
        if (onHomeRowRemovedListener2 != null) {
            onHomeRowRemovedListener2.onHomeRowRemoved(this);
        }
    }

    private void logChannelAction(Context context, TvlauncherLogEnum.TvLauncherEventCode eventCode, VisualElementTag buttonVeTag) {
        LogEvent event = new UserActionEvent(eventCode).setVisualElementTag(buttonVeTag);
        TvlauncherClientLog.Channel.Builder channel = event.getChannel();
        channel.setPackageName(this.packageName);
        if (!TextUtils.isEmpty(this.title)) {
            channel.setTitle(this.title);
        }
        event.getChannelCollection().setBrowsableCount(TvDataManager.getInstance(context).getHomeChannelCount());
        ChannelItemsAdapter channelItemsAdapter = this.itemsAdapter;
        if (channelItemsAdapter != null) {
            channel.setProgramCount(channelItemsAdapter.getItemCount());
        }
        channel.setIsLegacy(this.isLegacy);
        this.eventLogger.log(event);
    }

    public void onStateChangeGesturePerformed(ChannelView v, int newState) {
        switch (newState) {
            case 0:
            case 6:
            case 16:
            case 22:
                OnHomeRowSelectedListener onHomeRowSelectedListener2 = this.onHomeRowSelectedListener;
                if (onHomeRowSelectedListener2 != null) {
                    onHomeRowSelectedListener2.onHomeRowSelected(this);
                }
                OnHomeStateChangeListener onHomeStateChangeListener2 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener2 != null) {
                    onHomeStateChangeListener2.onHomeStateChange(0);
                    return;
                }
                return;
            case 1:
            case 7:
            case 17:
            case 23:
                OnHomeStateChangeListener onHomeStateChangeListener3 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener3 != null) {
                    onHomeStateChangeListener3.onHomeStateChange(0);
                    return;
                }
                return;
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 12:
            case 14:
            case 15:
            case 18:
            case 19:
            case 20:
            case 21:
            case 26:
            case 27:
            case 28:
            case 29:
                String valueOf = String.valueOf(ChannelView.stateToString(newState));
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported ChannelView state change gesture: ".concat(valueOf) : new String("Unsupported ChannelView state change gesture: "));
            case 8:
            case 24:
                OnHomeRowSelectedListener onHomeRowSelectedListener3 = this.onHomeRowSelectedListener;
                if (onHomeRowSelectedListener3 != null) {
                    onHomeRowSelectedListener3.onHomeRowSelected(this);
                }
                OnHomeStateChangeListener onHomeStateChangeListener4 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener4 != null) {
                    onHomeStateChangeListener4.onHomeStateChange(1);
                    return;
                }
                return;
            case 9:
            case 25:
                OnHomeStateChangeListener onHomeStateChangeListener5 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener5 != null) {
                    onHomeStateChangeListener5.onHomeStateChange(1);
                    return;
                }
                return;
            case 11:
                OnHomeRowSelectedListener onHomeRowSelectedListener4 = this.onHomeRowSelectedListener;
                if (onHomeRowSelectedListener4 != null) {
                    onHomeRowSelectedListener4.onHomeRowSelected(this);
                }
                OnHomeStateChangeListener onHomeStateChangeListener6 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener6 != null) {
                    onHomeStateChangeListener6.onHomeStateChange(2);
                    return;
                }
                return;
            case 13:
                OnHomeRowSelectedListener onHomeRowSelectedListener5 = this.onHomeRowSelectedListener;
                if (onHomeRowSelectedListener5 != null) {
                    onHomeRowSelectedListener5.onHomeRowSelected(this);
                }
                OnHomeStateChangeListener onHomeStateChangeListener7 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener7 != null) {
                    onHomeStateChangeListener7.onHomeStateChange(3);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onChannelLogoFocused() {
        this.itemsAdapter.updateProgramFocusState(this.itemsListView.getSelectedPosition());
    }

    public void log(LogEvent event) {
        if (event.getEventCode() == TvlauncherLogEnum.TvLauncherEventCode.START_PROGRAM) {
            this.metadataController.populateLogEvent(event);
        }
        TvlauncherClientLog.Channel.Builder channel = event.getChannel();
        channel.setPackageName(this.packageName);
        ChannelItemsAdapter channelItemsAdapter = this.itemsAdapter;
        if (channelItemsAdapter != null) {
            channel.setProgramCount(channelItemsAdapter.getItemCount());
        }
        if (!TextUtils.isEmpty(this.title)) {
            channel.setTitle(this.title);
        }
        channel.setIsLegacy(this.isLegacy);
        this.eventLogger.log(event);
    }

    private void setSelectedItemPosition(int position) {
        if (Util.areHomeScreenAnimationsEnabled(this.channelView.getContext())) {
            this.itemsListView.setSelectedPosition(position);
        } else {
            this.itemsListView.setSelectedPositionSmooth(position);
        }
    }

    public void onBackPressed(Context c) {
        if (this.itemsListView.getAdapter() != null) {
            if (this.channelView.getState() == 0 && this.itemsListView.getSelectedPosition() != 0 && this.itemsListView.getAdapter().getItemCount() > 0) {
                setSelectedItemPosition(0);
            } else if (this.channelView.getState() == 13 || this.channelView.getState() == 11) {
                onStateChangeGesturePerformed(this.channelView, 8);
            } else {
                BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2 = this.onBackNotHandledListener;
                if (onBackNotHandledListener2 != null) {
                    onBackNotHandledListener2.onBackNotHandled(c);
                }
            }
        }
    }

    public void onHomePressed(Context c) {
        AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
        if (accessibilityContextMenu2 != null && accessibilityContextMenu2.isShowing()) {
            this.accessibilityContextMenu.dismiss();
        } else if (this.channelView.getState() == 13 || this.channelView.getState() == 11) {
            onStateChangeGesturePerformed(this.channelView, 8);
        } else {
            if (this.channelView.getState() == 0) {
                HorizontalGridView horizontalGridView = this.itemsListView;
                RecyclerView.ViewHolder selectedViewHolder = horizontalGridView.findViewHolderForAdapterPosition(horizontalGridView.getSelectedPosition());
                if (selectedViewHolder instanceof BackHomeControllerListeners.OnHomePressedListener) {
                    ((BackHomeControllerListeners.OnHomePressedListener) selectedViewHolder).onHomePressed(c);
                    return;
                }
            }
            onHomeNotHandled(c);
        }
    }

    public void onHomeNotHandled(Context c) {
        BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2 = this.onHomeNotHandledListener;
        if (onHomeNotHandledListener2 != null) {
            onHomeNotHandledListener2.onHomeNotHandled(c);
        }
    }

    public boolean isAnimating() {
        return this.itemsListView.getItemAnimator() != null && this.itemsListView.getItemAnimator().isRunning();
    }

    public boolean isAnimating(RecyclerView.ItemAnimator.ItemAnimatorFinishedListener listener) {
        HorizontalGridView horizontalGridView = this.itemsListView;
        if (horizontalGridView != null && horizontalGridView.getItemAnimator() != null) {
            return this.itemsListView.getItemAnimator().isRunning(listener);
        }
        if (listener == null) {
            return false;
        }
        listener.onAnimationsFinished();
        return false;
    }

    private void updateAccessibilityContextMenu() {
        this.accessibilityContextMenu.findItem(1).setEnabled(this.channelOrderManager.canMoveChannelUp(this.channelId));
        this.accessibilityContextMenu.findItem(2).setEnabled(this.channelOrderManager.canMoveChannelDown(this.channelId));
    }

    private void updateAccessibilityContextMenuIfNeeded() {
        AccessibilityContextMenu accessibilityContextMenu2 = this.accessibilityContextMenu;
        if (accessibilityContextMenu2 != null && accessibilityContextMenu2.isShowing()) {
            updateAccessibilityContextMenu();
        }
    }

    public String toString() {
        String obj = super.toString();
        long j = this.channelId;
        String str = this.title;
        StringBuilder sb = new StringBuilder(String.valueOf(obj).length() + 46 + String.valueOf(str).length());
        sb.append('{');
        sb.append(obj);
        sb.append(", channelId='");
        sb.append(j);
        sb.append('\'');
        sb.append(", title='");
        sb.append(str);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }
}
