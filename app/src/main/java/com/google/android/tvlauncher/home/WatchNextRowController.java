package com.google.android.tvlauncher.home;

import android.content.Context;
import android.support.p004v7.widget.RecyclerView;
import android.view.View;
import androidx.leanback.widget.HorizontalGridView;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.RecyclerViewFastScrollingManager;
import com.google.android.tvlauncher.home.util.ChannelUtil;
import com.google.android.tvlauncher.home.util.ProgramStateUtil;
import com.google.android.tvlauncher.home.view.ChannelItemsAnimator;
import com.google.android.tvlauncher.home.view.ChannelView;
import com.google.android.tvlauncher.util.Util;

class WatchNextRowController implements HomeRow, ChannelView.OnStateChangeGesturePerformedListener, BackHomeControllerListeners.OnBackPressedListener, BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnHomeNotHandledListener, RecyclerViewStateProvider, RecyclerViewFastScrollingManager.OnFastScrollingChangedListener {
    private static final boolean DEBUG = false;
    private static final int EXIT_FAST_SCROLLING_DELAY_MS = 550;
    private static final String TAG = "WatchNextRowController";
    private final ChannelView channelView;
    private final EventLogger eventLogger;
    private RecyclerViewFastScrollingManager fastScrollingManager;
    private boolean homeIsFastScrolling;
    private RecyclerViewStateProvider homeListStateProvider;
    private WatchNextItemsAdapter itemsAdapter;
    private final HorizontalGridView itemsListView = this.channelView.getItemsListView();
    private WatchNextItemMetadataController metadataController;
    private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    private OnHomeRowSelectedListener onHomeRowSelectedListener;
    private OnHomeStateChangeListener onHomeStateChangeListener;
    private OnProgramSelectedListener onProgramSelectedListener;

    WatchNextRowController(ChannelView channelView2, EventLogger eventLogger2) {
        this.channelView = channelView2;
        this.eventLogger = eventLogger2;
        this.channelView.setOnStateChangeGesturePerformedListener(this);
        this.channelView.setAllowMoving(false);
        this.channelView.setAllowRemoving(false);
        this.channelView.setStateSettings(ChannelUtil.getDefaultChannelStateSettings(channelView2.getContext()));
        String title = this.channelView.getContext().getString(C1167R.string.play_next_channel_title);
        this.channelView.setLogoTitle(title);
        this.channelView.setLogoContentDescription(title);
        this.channelView.setZoomedOutLogoTitle(title);
        ChannelUtil.setWatchNextLogo(this.channelView.getChannelLogoImageView());
        ChannelUtil.configureItemsListAlignment(this.itemsListView);
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

    public void onFastScrollingChanged(boolean fastScrolling) {
        if (fastScrolling) {
            this.itemsAdapter.setProgramState(4, false);
        } else {
            this.itemsAdapter.setProgramState(getProgramState(this.channelView.getState()), true);
        }
        this.channelView.setIsFastScrolling(fastScrolling);
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
            this.itemsAdapter = new WatchNextItemsAdapter(this.channelView.getContext(), this.eventLogger);
            this.itemsAdapter.setOnProgramSelectedListener(this.onProgramSelectedListener);
            this.itemsAdapter.setOnHomeNotHandledListener(this);
            this.itemsAdapter.setListStateProvider(this);
            this.itemsAdapter.setHomeListStateProvider(this.homeListStateProvider);
            this.itemsListView.setAdapter(this.itemsAdapter);
            this.fastScrollingManager = new RecyclerViewFastScrollingManager(this.itemsListView, new ChannelItemsAnimator());
            this.fastScrollingManager.setExitFastScrollingDelayMs(550);
            this.fastScrollingManager.setOnFastScrollingChangedListener(this);
            updateStateForHomeFastScrolling();
            this.metadataController = new WatchNextItemMetadataController(this.channelView.getItemMetadataView());
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
        this.metadataController.clear();
    }

    /* access modifiers changed from: package-private */
    public void bind(int channelViewState) {
        ensureItemListIsSetUp();
        this.channelView.setState(channelViewState);
        int oldProgramState = this.itemsAdapter.getProgramState();
        int newProgramState = getProgramState(channelViewState);
        this.itemsAdapter.bind(newProgramState);
        updateItemsListPosition(newProgramState, oldProgramState);
    }

    public void setState(int channelViewState) {
        if (this.channelView.getState() != channelViewState && this.fastScrollingManager.isFastScrollingEnabled() && this.itemsListView.getSelectedPosition() == 0) {
            this.fastScrollingManager.setAnimatorEnabled(!this.homeIsFastScrolling);
        }
        this.channelView.setState(channelViewState);
        int oldProgramState = this.itemsAdapter.getProgramState();
        int newProgramState = getProgramState(channelViewState);
        this.itemsAdapter.setProgramState(newProgramState);
        updateItemsListPosition(newProgramState, oldProgramState);
    }

    private void updateItemsListPosition(int newState, int oldState) {
        if (newState != oldState && ProgramStateUtil.isZoomedOutState(newState) && this.itemsAdapter.getItemCount() > 1 && this.itemsListView.getSelectedPosition() != 0) {
            this.itemsListView.scrollToPosition(0);
        }
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
                String valueOf = ChannelView.stateToString(channelViewState);
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported Watch Next row state ".concat(valueOf) : "Unsupported Watch Next row state ");
            case 8:
                return 6;
            case 9:
                return 5;
            case 10:
                return 7;
            case 12:
                return 8;
            case 14:
                return 10;
            case 15:
                return 12;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void bindItemMetadata() {
        int position = this.itemsListView.getSelectedPosition();
        TvDataManager dataManager = TvDataManager.getInstance(this.itemsListView.getContext());
        if (position == -1 || this.itemsAdapter.getItemCount() == 0) {
            this.metadataController.clear();
        } else if (this.itemsAdapter.getItemCount() <= 0 || this.itemsAdapter.getItemViewType(position) != 1) {
            int programIndex = this.itemsAdapter.getProgramIndexFromAdapterPosition(position);
            if (programIndex >= 0 && programIndex < dataManager.getWatchNextProgramsCount()) {
                this.metadataController.bindView(dataManager.getWatchNextProgram(programIndex));
            }
        } else {
            this.metadataController.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public void bindInfoAcknowledgedButton() {
        int position = this.itemsListView.getSelectedPosition();
        boolean isInfoCardSelected = true;
        if (this.itemsAdapter.getItemCount() <= 0 || this.itemsAdapter.getItemViewType(position) != 1) {
            isInfoCardSelected = false;
        }
        this.channelView.bindWatchNextInfoAcknowledgedButton(isInfoCardSelected);
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
                String valueOf = ChannelView.stateToString(newState);
                throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported ChannelView state change gesture: ".concat(valueOf) : "Unsupported ChannelView state change gesture: ");
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

    private void setSelectedItemPosition(int position) {
        if (Util.areHomeScreenAnimationsEnabled(this.channelView.getContext())) {
            this.itemsListView.setSelectedPosition(position);
        } else {
            this.itemsListView.setSelectedPositionSmooth(position);
        }
    }

    public void onBackPressed(Context c) {
        if (this.itemsListView.getAdapter() != null) {
            if (this.channelView.getState() != 0 || this.itemsListView.getSelectedPosition() == 0 || this.itemsListView.getAdapter().getItemCount() <= 0) {
                BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2 = this.onBackNotHandledListener;
                if (onBackNotHandledListener2 != null) {
                    onBackNotHandledListener2.onBackNotHandled(c);
                    return;
                }
                return;
            }
            setSelectedItemPosition(0);
        }
    }

    public void onHomePressed(Context c) {
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
}
