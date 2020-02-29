package com.google.android.tvlauncher.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.FacetProvider;
import androidx.leanback.widget.ItemAlignmentFacet;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogEventParameters;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.data.WatchNextProgramsObserver;
import com.google.android.tvlauncher.home.util.ProgramSettings;
import com.google.android.tvlauncher.home.util.ProgramStateUtil;
import com.google.android.tvlauncher.home.util.ProgramUtil;
import com.google.android.tvlauncher.home.view.ProgramView;
import com.google.android.tvlauncher.home.view.WatchNextInfoView;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.Util;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.util.List;

class WatchNextItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final boolean DEBUG = false;
    private static final int INFO_CARD_ID = -2;
    private static final String TAG = "WatchNextItemsAdapter";
    static final int TYPE_INFO = 1;
    private static final int TYPE_INFO_ADAPTER_POSITION = 1;
    private static final int TYPE_PROGRAM = 0;
    /* access modifiers changed from: private */
    public final TvDataManager dataManager;
    private final EventLogger eventLogger;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider homeListStateProvider;
    /* access modifiers changed from: private */
    public int lastUnfocusedAdapterPosition = -1;
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider listStateProvider;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    /* access modifiers changed from: private */
    public OnProgramSelectedListener onProgramSelectedListener;
    /* access modifiers changed from: private */
    public SharedPreferences preferences;
    /* access modifiers changed from: private */
    public int programState = 0;
    private final WatchNextProgramsObserver programsObserver = new WatchNextProgramsObserver() {
        public void onProgramsChange() {
            WatchNextItemsAdapter.this.logDataLoaded();
            int unused = WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            WatchNextItemsAdapter.this.notifyDataSetChanged();
        }
    };
    /* access modifiers changed from: private */
    public RecyclerView recyclerView;
    /* access modifiers changed from: private */
    public boolean showInfo;
    private boolean started;

    WatchNextItemsAdapter(Context context, EventLogger eventLogger2) {
        this.dataManager = TvDataManager.getInstance(context);
        this.eventLogger = eventLogger2;
        setHasStableIds(true);
        this.preferences = context.getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0);
        this.showInfo = !this.preferences.getBoolean("watch_next_info_acknowledged", false);
    }

    /* access modifiers changed from: package-private */
    public void setOnProgramSelectedListener(OnProgramSelectedListener listener) {
        this.onProgramSelectedListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2) {
        this.onHomeNotHandledListener = onHomeNotHandledListener2;
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
    public void bind(int programState2) {
        this.programState = programState2;
        if (!registerObserverAndUpdateDataIfNeeded()) {
            this.lastUnfocusedAdapterPosition = -1;
            notifyDataSetChanged();
        }
        this.started = true;
    }

    /* access modifiers changed from: package-private */
    public int getProgramState() {
        return this.programState;
    }

    /* access modifiers changed from: package-private */
    public void setProgramState(int state) {
        setProgramState(state, true);
    }

    /* access modifiers changed from: package-private */
    public void setProgramState(int state, boolean updateVisibleItems) {
        if (this.programState != state) {
            this.programState = state;
            if (updateVisibleItems) {
                this.lastUnfocusedAdapterPosition = -1;
                notifyItemRangeChanged(0, getItemCount(), "PAYLOAD_STATE");
            }
        }
    }

    private boolean registerObserverAndUpdateDataIfNeeded() {
        this.dataManager.registerWatchNextProgramsObserver(this.programsObserver);
        if (this.dataManager.isWatchNextProgramsDataLoaded() && !this.dataManager.isWatchNextProgramsDataStale()) {
            return false;
        }
        this.dataManager.loadWatchNextProgramData();
        this.dataManager.loadAllWatchNextProgramDataIntoCache();
        return true;
    }

    public void onStart() {
        if (!this.started) {
            registerObserverAndUpdateDataIfNeeded();
        }
        this.started = true;
    }

    public void onStop() {
        if (this.started) {
            this.dataManager.unregisterWatchNextProgramsObserver(this.programsObserver);
        }
        this.started = false;
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        this.lastUnfocusedAdapterPosition = -1;
        onStop();
    }

    public int getItemCount() {
        if (!this.dataManager.isWatchNextProgramsDataLoaded()) {
            return 0;
        }
        int programCount = this.dataManager.getWatchNextProgramsCount();
        if (this.showInfo) {
            return programCount + 1;
        }
        return programCount;
    }

    public int getItemViewType(int position) {
        if (this.dataManager.getWatchNextProgramsCount() <= 0) {
            return 1;
        }
        if (!this.showInfo || position != 1) {
            return 0;
        }
        return 1;
    }

    public long getItemId(int position) {
        if (getItemViewType(position) == 1) {
            return -2;
        }
        return this.dataManager.getWatchNextProgram(getProgramIndexFromAdapterPosition(position)).getId();
    }

    /* access modifiers changed from: package-private */
    public int getProgramIndexFromAdapterPosition(int adapterPosition) {
        if (!this.showInfo || adapterPosition < 1) {
            return adapterPosition;
        }
        return adapterPosition - 1;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0) {
            return new InfoCardViewHolder((WatchNextInfoView) LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_watch_next_info, parent, false));
        }
        ProgramViewHolder programViewHolder = new ProgramViewHolder((ProgramView) LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_program, parent, false), this.eventLogger);
        programViewHolder.getProgramController().setOnHomeNotHandledListener(this.onHomeNotHandledListener);
        return programViewHolder;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProgramViewHolder) {
            ((ProgramViewHolder) holder).bind(this.dataManager.getWatchNextProgram(getProgramIndexFromAdapterPosition(position)), this.programState);
        } else if (holder instanceof InfoCardViewHolder) {
            ((InfoCardViewHolder) holder).bind();
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else if (holder instanceof ProgramViewHolder) {
            ProgramViewHolder vh = (ProgramViewHolder) holder;
            if (payloads.contains("PAYLOAD_STATE")) {
                vh.getProgramController().bindState(this.programState);
            }
            if (payloads.contains("PAYLOAD_STATE") || payloads.contains(ProgramBindPayloads.FOCUS_CHANGED)) {
                vh.getProgramController().updateFocusedState();
            }
        } else if (holder instanceof InfoCardViewHolder) {
            InfoCardViewHolder vh2 = (InfoCardViewHolder) holder;
            if (payloads.contains("PAYLOAD_STATE")) {
                vh2.bindState();
            }
            if (payloads.contains("PAYLOAD_STATE") || payloads.contains(ProgramBindPayloads.FOCUS_CHANGED)) {
                vh2.updateFocusedState();
            }
        }
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = recyclerView2;
        TvHomeDrawnManager.getInstance().monitorChannelItemListViewDrawn(this.recyclerView);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = null;
    }

    /* access modifiers changed from: private */
    public void logDataLoaded() {
        LogEventParameters parameters = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.WATCH_NEXT);
        parameters.getWatchNextChannel().setProgramCount(this.showInfo ? getItemCount() - 1 : getItemCount());
        this.eventLogger.log(parameters);
    }

    /* access modifiers changed from: private */
    public double getCardBeforeInfoCardAspectRatio() {
        return ProgramUtil.getAspectRatio(this.dataManager.getWatchNextProgram(0).getPreviewImageAspectRatio());
    }

    class ProgramViewHolder extends RecyclerView.ViewHolder implements OnProgramViewFocusChangedListener, BackHomeControllerListeners.OnHomePressedListener, EventLogger {
        private final EventLogger eventLogger;
        private Runnable notifyFocusChangedRunnable = new WatchNextItemsAdapter$ProgramViewHolder$$Lambda$0(this);
        private final ProgramController programController;

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$0$WatchNextItemsAdapter$ProgramViewHolder() {
            WatchNextItemsAdapter.this.notifyItemChanged(getAdapterPosition(), ProgramBindPayloads.FOCUS_CHANGED);
            if (WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition != -1) {
                WatchNextItemsAdapter watchNextItemsAdapter = WatchNextItemsAdapter.this;
                watchNextItemsAdapter.notifyItemChanged(watchNextItemsAdapter.lastUnfocusedAdapterPosition, ProgramBindPayloads.FOCUS_CHANGED);
                int unused = WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            }
        }

        ProgramViewHolder(ProgramView v, EventLogger eventLogger2) {
            super(v);
            this.programController = new ProgramController(v, this, false, false);
            this.eventLogger = eventLogger2;
            this.programController.setOnProgramViewFocusChangedListener(this);
            this.programController.setIsWatchNextProgram(true);
            this.programController.setListStateProvider(WatchNextItemsAdapter.this.listStateProvider);
            this.programController.setHomeListStateProvider(WatchNextItemsAdapter.this.homeListStateProvider);
        }

        public void onProgramViewFocusChanged(boolean hasFocus) {
            int position = getAdapterPosition();
            if (position != -1) {
                if (WatchNextItemsAdapter.this.onProgramSelectedListener != null && hasFocus) {
                    WatchNextItemsAdapter.this.onProgramSelectedListener.onProgramSelected(WatchNextItemsAdapter.this.dataManager.getWatchNextProgram(WatchNextItemsAdapter.this.getProgramIndexFromAdapterPosition(position)), this.programController);
                }
                WatchNextItemsAdapter.this.handler.removeCallbacks(this.notifyFocusChangedRunnable);
                if (WatchNextItemsAdapter.this.programState == 4) {
                    if (this.programController.isProgramSelected() && !hasFocus) {
                        notifyFocusChanged();
                    }
                } else if (!hasFocus) {
                    int unused = WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition = getAdapterPosition();
                } else {
                    notifyFocusChanged();
                }
            }
        }

        private void notifyFocusChanged() {
            if (WatchNextItemsAdapter.this.recyclerView == null || WatchNextItemsAdapter.this.recyclerView.isComputingLayout()) {
                Log.w(WatchNextItemsAdapter.TAG, "list is still computing layout => schedule program selection change");
                WatchNextItemsAdapter.this.handler.post(this.notifyFocusChangedRunnable);
                return;
            }
            this.notifyFocusChangedRunnable.run();
        }

        /* access modifiers changed from: package-private */
        public ProgramController getProgramController() {
            return this.programController;
        }

        /* access modifiers changed from: package-private */
        public void bind(Program program, int programState) {
            this.programController.bind(program, null, programState, false, false, false);
            if (WatchNextItemsAdapter.this.onProgramSelectedListener != null && this.programController.isViewFocused()) {
                WatchNextItemsAdapter.this.onProgramSelectedListener.onProgramSelected(program, this.programController);
            }
        }

        public void log(LogEvent event) {
            event.setVisualElementIndex(WatchNextItemsAdapter.this.getProgramIndexFromAdapterPosition(getAdapterPosition())).pushParentVisualElementTag(TvLauncherConstants.WATCH_NEXT_CONTAINER).getWatchNextChannel().setProgramCount(WatchNextItemsAdapter.this.showInfo ? WatchNextItemsAdapter.this.getItemCount() - 1 : WatchNextItemsAdapter.this.getItemCount());
            this.eventLogger.log(event);
        }

        public void onHomePressed(Context c) {
            this.programController.onHomePressed(c);
        }
    }

    private class InfoCardViewHolder extends RecyclerView.ViewHolder implements FacetProvider {
        private ItemAlignmentFacet facet;
        private WatchNextInfoController infoController;
        private ItemAlignmentFacet.ItemAlignmentDef itemAlignmentDef;
        private Runnable notifyFocusChangedRunnable = new WatchNextItemsAdapter$InfoCardViewHolder$$Lambda$0(this);
        private View.OnFocusChangeListener onFocusChangeListener = new WatchNextItemsAdapter$InfoCardViewHolder$$Lambda$1(this);
        private ProgramSettings programSettings;

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$0$WatchNextItemsAdapter$InfoCardViewHolder() {
            WatchNextItemsAdapter.this.notifyItemChanged(getAdapterPosition(), ProgramBindPayloads.FOCUS_CHANGED);
            if (WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition != -1) {
                WatchNextItemsAdapter watchNextItemsAdapter = WatchNextItemsAdapter.this;
                watchNextItemsAdapter.notifyItemChanged(watchNextItemsAdapter.lastUnfocusedAdapterPosition, ProgramBindPayloads.FOCUS_CHANGED);
                int unused = WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            }
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$1$WatchNextItemsAdapter$InfoCardViewHolder(View view, boolean hasFocus) {
            if (getAdapterPosition() != -1) {
                if (WatchNextItemsAdapter.this.onProgramSelectedListener != null && hasFocus) {
                    WatchNextItemsAdapter.this.onProgramSelectedListener.onProgramSelected(null, null);
                }
                WatchNextItemsAdapter.this.handler.removeCallbacks(this.notifyFocusChangedRunnable);
                if (WatchNextItemsAdapter.this.programState == 4) {
                    if (this.infoController.isSelected() && !hasFocus) {
                        notifyFocusChanged();
                    }
                } else if (!hasFocus) {
                    int unused = WatchNextItemsAdapter.this.lastUnfocusedAdapterPosition = getAdapterPosition();
                } else {
                    notifyFocusChanged();
                }
            }
        }

        private void notifyFocusChanged() {
            if (WatchNextItemsAdapter.this.recyclerView == null || WatchNextItemsAdapter.this.recyclerView.isComputingLayout()) {
                Log.w(WatchNextItemsAdapter.TAG, "list is still computing layout => schedule card selection change");
                WatchNextItemsAdapter.this.handler.post(this.notifyFocusChangedRunnable);
                return;
            }
            this.notifyFocusChangedRunnable.run();
        }

        InfoCardViewHolder(WatchNextInfoView v) {
            super(v);
            this.programSettings = ProgramUtil.getProgramSettings(v.getContext());
            this.infoController = new WatchNextInfoController(v, this.programSettings);
            if (Util.areHomeScreenAnimationsEnabled(v.getContext())) {
                v.setOnFocusChangeListener(this.onFocusChangeListener);
            }
            v.setOnClickListener(new WatchNextItemsAdapter$InfoCardViewHolder$$Lambda$2(this));
            this.itemAlignmentDef = new ItemAlignmentFacet.ItemAlignmentDef();
            this.itemAlignmentDef.setItemAlignmentOffsetPercent(-1.0f);
            this.facet = new ItemAlignmentFacet();
            this.facet.setAlignmentDefs(new ItemAlignmentFacet.ItemAlignmentDef[]{this.itemAlignmentDef});
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$2$WatchNextItemsAdapter$InfoCardViewHolder(View view) {
            if (WatchNextItemsAdapter.this.showInfo) {
                boolean unused = WatchNextItemsAdapter.this.showInfo = false;
                WatchNextItemsAdapter.this.notifyItemRemoved(1);
                WatchNextItemsAdapter.this.preferences.edit().putBoolean("watch_next_info_acknowledged", true).apply();
            }
        }

        /* access modifiers changed from: package-private */
        public void bind() {
            bindState();
            updateFocusedState();
        }

        /* access modifiers changed from: package-private */
        public void bindState() {
            this.infoController.bindState(WatchNextItemsAdapter.this.programState);
        }

        /* access modifiers changed from: package-private */
        public void updateFocusedState() {
            if (WatchNextItemsAdapter.this.dataManager.getWatchNextProgramsCount() > 0) {
                double d = (double) this.programSettings.selectedHeight;
                double access$1000 = WatchNextItemsAdapter.this.getCardBeforeInfoCardAspectRatio();
                Double.isNaN(d);
                double previousCardWidth = d * access$1000;
                double d2 = (double) (this.programSettings.focusedScale - 1.0f);
                Double.isNaN(d2);
                double d3 = (double) this.programSettings.defaultMarginHorizontal;
                Double.isNaN(d3);
                this.infoController.updateFocusedState((float) ((d2 * previousCardWidth) - d3));
                return;
            }
            this.infoController.updateFocusedState(0.0f);
        }

        public Object getFacet(Class<?> cls) {
            if (getAdapterPosition() == -1) {
                return null;
            }
            if (WatchNextItemsAdapter.this.dataManager.getWatchNextProgramsCount() > 0) {
                double previousCardAspectRatio = WatchNextItemsAdapter.this.getCardBeforeInfoCardAspectRatio();
                int prevCardHeight = 0;
                int prevCardMarginEnd = 0;
                switch (WatchNextItemsAdapter.this.programState) {
                    case 0:
                        prevCardHeight = this.programSettings.defaultHeight;
                        prevCardMarginEnd = this.programSettings.defaultMarginHorizontal;
                        break;
                    case 1:
                    case 2:
                        prevCardHeight = this.programSettings.defaultTopHeight;
                        prevCardMarginEnd = this.programSettings.defaultMarginHorizontal;
                        break;
                    case 3:
                    case 4:
                    case 12:
                        prevCardHeight = this.programSettings.selectedHeight;
                        prevCardMarginEnd = this.programSettings.defaultMarginHorizontal;
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                        prevCardHeight = this.programSettings.zoomedOutHeight;
                        prevCardMarginEnd = this.programSettings.zoomedOutMarginHorizontal;
                        break;
                    case 9:
                    case 11:
                        String valueOf = String.valueOf(ProgramStateUtil.stateToString(WatchNextItemsAdapter.this.programState));
                        throw new IllegalStateException(valueOf.length() != 0 ? "Unsupported Watch Next program state: ".concat(valueOf) : new String("Unsupported Watch Next program state: "));
                }
                double d = (double) prevCardHeight;
                Double.isNaN(d);
                this.itemAlignmentDef.setItemAlignmentOffset((-((int) (d * previousCardAspectRatio))) - prevCardMarginEnd);
            } else {
                this.itemAlignmentDef.setItemAlignmentOffset(0);
            }
            return this.facet;
        }
    }
}
