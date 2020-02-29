package com.google.android.tvlauncher.home;

import android.content.Context;
import android.content.res.Resources;
import android.support.p004v7.widget.DefaultItemAnimator;
import android.support.p004v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import androidx.leanback.widget.HorizontalGridView;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.appsview.AppsViewActivity;
import com.google.android.tvlauncher.home.util.ChannelUtil;
import com.google.android.tvlauncher.home.util.HomeAppStateUtil;
import com.google.android.tvlauncher.home.view.ChannelItemsAnimator;
import com.google.android.tvlauncher.home.view.ChannelView;
import com.google.android.tvlauncher.util.Util;

class FavoriteLaunchItemsRowController implements HomeRow, ChannelView.OnPerformMainActionListener, ChannelView.OnStateChangeGesturePerformedListener, BackHomeControllerListeners.OnBackPressedListener, BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnHomeNotHandledListener, BackHomeControllerListeners.OnBackNotHandledListener, RecyclerViewStateProvider {
    private static final boolean DEBUG = false;
    private static final String TAG = "FavLaunchItemController";
    /* access modifiers changed from: private */
    public final ChannelView channelView;
    private final EventLogger eventLogger;
    private RecyclerViewFastScrollingManager fastScrollingManager;
    private FavoriteLaunchItemsRowEditModeActionCallbacks favoriteLaunchItemsRowEditModeActionCallbacks;
    private boolean homeIsFastScrolling;
    private RecyclerViewStateProvider homeListStateProvider;
    private FavoriteLaunchItemsAdapter itemsAdapter;
    private final int itemsListDefaultPaddingEnd;
    private final int itemsListDefaultWindowAlignmentOffset;
    /* access modifiers changed from: private */
    public final HorizontalGridView itemsListView = this.channelView.getItemsListView();
    private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    private OnHomeRowSelectedListener onHomeRowSelectedListener;
    private OnHomeStateChangeListener onHomeStateChangeListener;

    FavoriteLaunchItemsRowController(ChannelView channelView2, EventLogger eventLogger2) {
        this.channelView = channelView2;
        this.eventLogger = eventLogger2;
        this.channelView.setOnPerformMainActionListener(this);
        this.channelView.setOnStateChangeGesturePerformedListener(this);
        this.channelView.setAllowMoving(false);
        this.channelView.setAllowRemoving(false);
        this.channelView.setShowItemMeta(false);
        this.channelView.setShowItemsTitle(false);
        this.channelView.setStateSettings(ChannelUtil.getAppsRowStateSettings(channelView2.getContext()));
        this.channelView.invalidateState();
        Context context = this.channelView.getContext();
        String title = context.getString(C1167R.string.action_apps_view);
        this.channelView.setLogoTitle(title);
        this.channelView.setLogoContentDescription(title);
        this.channelView.setZoomedOutLogoTitle(title);
        Resources resources = context.getResources();
        this.itemsListDefaultWindowAlignmentOffset = resources.getDimensionPixelOffset(C1167R.dimen.home_app_channel_items_list_default_window_alignment_offset);
        this.itemsListDefaultPaddingEnd = resources.getDimensionPixelOffset(C1167R.dimen.home_app_channel_items_list_default_padding_end);
        ChannelUtil.configureAppRowItemsListAlignment(this.itemsListView);
        this.channelView.setItemsListWindowAlignmentOffset(this.itemsListDefaultWindowAlignmentOffset);
        this.channelView.setItemsListEndPadding(this.itemsListDefaultPaddingEnd);
        ImageView logoView = this.channelView.getChannelLogoImageView();
        logoView.setImageDrawable(context.getDrawable(C1167R.C1168drawable.apps_view_logo));
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.favoriteLaunchItemsRowEditModeActionCallbacks = new FavoriteLaunchItemsRowEditModeActionCallbacks() {
            public void onEnterEditMode() {
                FavoriteLaunchItemsRowController.this.channelView.setAllowZoomOut(false);
                if (!Util.areHomeScreenAnimationsEnabled(FavoriteLaunchItemsRowController.this.channelView.getContext())) {
                    FavoriteLaunchItemsRowController.this.itemsListView.setItemAnimator(new DefaultItemAnimator());
                }
            }

            public void onExitEditMode() {
                FavoriteLaunchItemsRowController.this.channelView.setAllowZoomOut(true);
                if (!Util.areHomeScreenAnimationsEnabled(FavoriteLaunchItemsRowController.this.channelView.getContext())) {
                    FavoriteLaunchItemsRowController.this.itemsListView.setItemAnimator(null);
                }
            }
        };
    }

    public void setOnHomeStateChangeListener(OnHomeStateChangeListener listener) {
        this.onHomeStateChangeListener = listener;
    }

    public void setOnHomeRowSelectedListener(OnHomeRowSelectedListener listener) {
        this.onHomeRowSelectedListener = listener;
    }

    public void setOnHomeRowRemovedListener(OnHomeRowRemovedListener listener) {
    }

    public void setHomeIsFastScrolling(boolean homeIsFastScrolling2) {
        if (this.homeIsFastScrolling != homeIsFastScrolling2) {
            this.homeIsFastScrolling = homeIsFastScrolling2;
            updateStateForHomeFastScrolling();
        }
    }

    public View getView() {
        return this.channelView;
    }

    /* access modifiers changed from: package-private */
    public void setHomeListStateProvider(RecyclerViewStateProvider homeListStateProvider2) {
        this.homeListStateProvider = homeListStateProvider2;
    }

    /* access modifiers changed from: package-private */
    public void setSelectedItemPosition(int position) {
        if (this.itemsListView.getAdapter() == null || position < 0 || position >= this.itemsListView.getAdapter().getItemCount()) {
            return;
        }
        if (Util.areHomeScreenAnimationsEnabled(this.channelView.getContext())) {
            this.itemsListView.setSelectedPosition(position);
        } else {
            this.itemsListView.setSelectedPositionSmooth(position);
        }
    }

    /* access modifiers changed from: package-private */
    public void setOnBackNotHandledListener(BackHomeControllerListeners.OnBackNotHandledListener listener) {
        this.onBackNotHandledListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener listener) {
        this.onHomeNotHandledListener = listener;
    }

    private void ensureItemListIsSetUp() {
        if (this.itemsAdapter == null) {
            this.itemsAdapter = new FavoriteLaunchItemsAdapter(this.channelView.getContext(), this.eventLogger);
            this.itemsAdapter.setAppsRowEditModeActionCallbacks(this.favoriteLaunchItemsRowEditModeActionCallbacks);
            this.itemsAdapter.setOnHomeNotHandledListener(this);
            this.itemsAdapter.setOnBackNotHandledListener(this);
            this.itemsAdapter.setListStateProvider(this);
            this.itemsAdapter.setHomeListStateProvider(this.homeListStateProvider);
            this.itemsListView.setAdapter(this.itemsAdapter);
            this.fastScrollingManager = new RecyclerViewFastScrollingManager(this.itemsListView, new ChannelItemsAnimator());
            updateStateForHomeFastScrolling();
        }
    }

    private void updateStateForHomeFastScrolling() {
        this.fastScrollingManager.setAnimatorEnabled(!this.homeIsFastScrolling);
        this.fastScrollingManager.setScrollEnabled(false);
    }

    /* access modifiers changed from: package-private */
    public void bind(int channelViewState) {
        ensureItemListIsSetUp();
        this.channelView.setState(channelViewState);
        int oldAppState = this.itemsAdapter.getAppState();
        int newAppState = getAppState(channelViewState);
        this.itemsAdapter.setAppState(newAppState);
        updateItemsListPosition(newAppState, oldAppState);
    }

    private void updateItemsListPosition(int newState, int oldState) {
        if (newState != oldState && HomeAppStateUtil.isZoomedOutState(newState) && this.itemsAdapter.getItemCount() > 1 && this.itemsListView.getSelectedPosition() != 0) {
            this.itemsListView.scrollToPosition(0);
        }
    }

    private int getAppState(int channelViewState) {
        switch (channelViewState) {
            case 0:
                return 2;
            case 1:
                return 0;
            case 2:
            case 15:
                return 1;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 11:
            case 13:
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
                String valueOf = String.valueOf(ChannelView.stateToString(channelViewState));
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported Apps row state ".concat(valueOf) : new String("Unsupported Apps row state "));
            case 8:
                return 4;
            case 9:
                return 3;
            case 10:
                return 5;
            case 12:
                return 6;
            case 14:
                return 7;
            default:
                return 0;
        }
    }

    public void onPerformMainAction(ChannelView v) {
        AppsViewActivity.startAppsViewActivity(null, v.getContext());
    }

    public void onStateChangeGesturePerformed(ChannelView v, int newState) {
        switch (newState) {
            case 0:
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
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
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
                String valueOf = String.valueOf(ChannelView.stateToString(newState));
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported ChannelView state change gesture: ".concat(valueOf) : new String("Unsupported ChannelView state change gesture: "));
            case 8:
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
                OnHomeStateChangeListener onHomeStateChangeListener5 = this.onHomeStateChangeListener;
                if (onHomeStateChangeListener5 != null) {
                    onHomeStateChangeListener5.onHomeStateChange(1);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onBackPressed(Context c) {
        if (this.itemsListView.getAdapter() != null) {
            if (this.channelView.getState() == 0 && this.itemsListView.getAdapter().getItemCount() > 0) {
                HorizontalGridView horizontalGridView = this.itemsListView;
                RecyclerView.ViewHolder selectedViewHolder = horizontalGridView.findViewHolderForAdapterPosition(horizontalGridView.getSelectedPosition());
                if (selectedViewHolder instanceof BackHomeControllerListeners.OnBackPressedListener) {
                    ((BackHomeControllerListeners.OnBackPressedListener) selectedViewHolder).onBackPressed(c);
                    return;
                }
            }
            onBackNotHandled(c);
        }
    }

    public void onHomePressed(Context c) {
        if (this.itemsListView.getAdapter() != null) {
            if (this.channelView.getState() == 0 && this.itemsListView.getAdapter().getItemCount() > 0) {
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

    private boolean selectFirstItemIfNeeded() {
        if (this.itemsListView.getAdapter() == null || this.channelView.getState() != 0 || this.itemsListView.getAdapter().getItemCount() <= 0 || this.itemsListView.getSelectedPosition() == 0) {
            return false;
        }
        setSelectedItemPosition(0);
        return true;
    }

    public void onHomeNotHandled(Context c) {
        BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2;
        if (!selectFirstItemIfNeeded() && (onHomeNotHandledListener2 = this.onHomeNotHandledListener) != null) {
            onHomeNotHandledListener2.onHomeNotHandled(c);
        }
    }

    public void onBackNotHandled(Context c) {
        BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2;
        if (!selectFirstItemIfNeeded() && (onBackNotHandledListener2 = this.onBackNotHandledListener) != null) {
            onBackNotHandledListener2.onBackNotHandled(c);
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
}
