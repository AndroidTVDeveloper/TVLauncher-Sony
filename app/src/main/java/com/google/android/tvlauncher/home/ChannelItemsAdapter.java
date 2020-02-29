package com.google.android.tvlauncher.home;

import android.content.Context;
import android.os.Handler;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.data.ChannelProgramsObserver;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.view.ProgramView;
import com.google.android.tvlauncher.model.Program;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final boolean DEBUG = false;
    private static final int NO_CHANNEL_ID = -1;
    private static final String PAYLOAD_LIVE_PROGRESS_UPDATE = "PAYLOAD_LIVE_PROGRESS_UPDATE";
    private static final String TAG = "ChannelItemsAdapter";
    private Set<ProgramController> activeProgramControllers = new HashSet();
    /* access modifiers changed from: private */
    public boolean canAddToWatchNext;
    /* access modifiers changed from: private */
    public boolean canRemoveProgram;
    /* access modifiers changed from: private */
    public long channelId = -1;
    /* access modifiers changed from: private */
    public final TvDataManager dataManager;
    private final EventLogger eventLogger;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider homeListStateProvider;
    /* access modifiers changed from: private */
    public boolean isLegacy;
    /* access modifiers changed from: private */
    public boolean isSponsored;
    /* access modifiers changed from: private */
    public boolean isSponsoredBranded;
    /* access modifiers changed from: private */
    public int lastUnfocusedAdapterPosition = -1;
    /* access modifiers changed from: private */
    public RecyclerViewStateProvider listStateProvider;
    private BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener;
    /* access modifiers changed from: private */
    public OnProgramSelectedListener onProgramSelectedListener;
    /* access modifiers changed from: private */
    public String packageName;
    /* access modifiers changed from: private */
    public int programState = 0;
    private final ChannelProgramsObserver programsObserver = new ChannelProgramsObserver() {
        public void onProgramsChange(long channelId) {
            int unused = ChannelItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            ChannelItemsAdapter.this.notifyDataSetChanged();
        }
    };
    /* access modifiers changed from: private */
    public RecyclerView recyclerView;
    private boolean started;

    ChannelItemsAdapter(Context context, EventLogger eventLogger2) {
        this.dataManager = TvDataManager.getInstance(context);
        this.eventLogger = eventLogger2;
        setHasStableIds(true);
    }

    /* access modifiers changed from: package-private */
    public void setIsSponsored(boolean isSponsored2, boolean isSponsoredBranded2) {
        this.isSponsored = isSponsored2;
        this.isSponsoredBranded = isSponsoredBranded2;
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
    public void setOnProgramSelectedListener(OnProgramSelectedListener listener) {
        this.onProgramSelectedListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnHomeNotHandledListener(BackHomeControllerListeners.OnHomeNotHandledListener onHomeNotHandledListener2) {
        this.onHomeNotHandledListener = onHomeNotHandledListener2;
    }

    /* access modifiers changed from: package-private */
    public void bind(long channelId2, String packageName2, int programState2, boolean canAddToWatchNext2, boolean canRemoveProgram2, boolean isLegacy2) {
        long j = channelId2;
        int i = programState2;
        this.packageName = packageName2;
        this.canAddToWatchNext = canAddToWatchNext2;
        this.canRemoveProgram = canRemoveProgram2;
        this.isLegacy = isLegacy2;
        int oldProgramState = this.programState;
        this.programState = i;
        long j2 = this.channelId;
        if (j != j2) {
            if (j2 != -1) {
                this.dataManager.unregisterChannelProgramsObserver(j2, this.programsObserver);
            }
            this.channelId = j;
            if (this.channelId != -1) {
                if (!registerObserverAndUpdateDataIfNeeded()) {
                    this.lastUnfocusedAdapterPosition = -1;
                    notifyDataSetChanged();
                }
                this.started = true;
                return;
            }
            this.started = false;
        } else if (oldProgramState != i) {
            this.lastUnfocusedAdapterPosition = -1;
            notifyItemRangeChanged(0, getItemCount(), "PAYLOAD_STATE");
        }
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

    /* access modifiers changed from: package-private */
    public void updateProgramFocusState(int position) {
        notifyItemChanged(position, ProgramBindPayloads.FOCUS_CHANGED);
    }

    private boolean registerObserverAndUpdateDataIfNeeded() {
        this.dataManager.registerChannelProgramsObserver(this.channelId, this.programsObserver);
        if (this.dataManager.isProgramDataLoaded(this.channelId) && !this.dataManager.isProgramDataStale(this.channelId)) {
            return false;
        }
        this.dataManager.loadProgramData(this.channelId);
        return true;
    }

    public void onStart() {
        if (!this.started && this.channelId != -1 && !registerObserverAndUpdateDataIfNeeded()) {
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_LIVE_PROGRESS_UPDATE);
        }
        this.started = true;
    }

    public void onStop() {
        if (this.started) {
            long j = this.channelId;
            if (j != -1) {
                this.dataManager.unregisterChannelProgramsObserver(j, this.programsObserver);
                for (ProgramController controller : this.activeProgramControllers) {
                    controller.onStop();
                }
            }
        }
        this.started = false;
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        bind(-1, null, this.programState, false, false, false);
        this.started = false;
        this.lastUnfocusedAdapterPosition = -1;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        if (this.dataManager.isProgramDataLoaded(this.channelId)) {
            return this.dataManager.getProgramCount(this.channelId);
        }
        return 0;
    }

    public long getItemId(int position) {
        return this.dataManager.getProgram(this.channelId, position).getId();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ProgramViewHolder programViewHolder = new ProgramViewHolder((ProgramView) LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_program, parent, false), this.eventLogger);
        programViewHolder.getProgramController().setOnHomeNotHandledListener(this.onHomeNotHandledListener);
        return programViewHolder;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProgramViewHolder vh = (ProgramViewHolder) holder;
        vh.bind(this.dataManager.getProgram(this.channelId, position));
        this.activeProgramControllers.add(vh.getProgramController());
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        ProgramViewHolder vh = (ProgramViewHolder) holder;
        if (payloads.contains("PAYLOAD_STATE")) {
            vh.getProgramController().bindState(this.programState);
        }
        if (payloads.contains("PAYLOAD_STATE") || payloads.contains(ProgramBindPayloads.FOCUS_CHANGED)) {
            vh.getProgramController().updateFocusedState();
        }
        if (payloads.contains(PAYLOAD_LIVE_PROGRESS_UPDATE)) {
            vh.getProgramController().updateProgressState(this.dataManager.getProgram(this.channelId, position));
        }
    }

    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ProgramViewHolder) {
            ProgramViewHolder vh = (ProgramViewHolder) holder;
            vh.recycle();
            this.activeProgramControllers.remove(vh.getProgramController());
        }
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = recyclerView2;
        TvHomeDrawnManager.getInstance().monitorChannelItemListViewDrawn(this.recyclerView);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView2) {
        this.recyclerView = null;
    }

    public class ProgramViewHolder extends RecyclerView.ViewHolder implements OnProgramViewFocusChangedListener, ProgramViewLiveProgressUpdateCallback, BackHomeControllerListeners.OnHomePressedListener, EventLogger {
        private final EventLogger eventLogger;
        private Runnable notifyFocusChangedRunnable = new ChannelItemsAdapter$ProgramViewHolder$$Lambda$0(this);
        private Runnable notifyLiveProgressUpdateRunnable = new ChannelItemsAdapter$ProgramViewHolder$$Lambda$1(this);
        final ProgramController programController;

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$0$ChannelItemsAdapter$ProgramViewHolder() {
            ChannelItemsAdapter.this.notifyItemChanged(getAdapterPosition(), ProgramBindPayloads.FOCUS_CHANGED);
            if (ChannelItemsAdapter.this.lastUnfocusedAdapterPosition != -1) {
                ChannelItemsAdapter channelItemsAdapter = ChannelItemsAdapter.this;
                channelItemsAdapter.notifyItemChanged(channelItemsAdapter.lastUnfocusedAdapterPosition, ProgramBindPayloads.FOCUS_CHANGED);
                int unused = ChannelItemsAdapter.this.lastUnfocusedAdapterPosition = -1;
            }
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$new$1$ChannelItemsAdapter$ProgramViewHolder() {
            ChannelItemsAdapter.this.notifyItemChanged(getAdapterPosition(), ChannelItemsAdapter.PAYLOAD_LIVE_PROGRESS_UPDATE);
        }

        ProgramViewHolder(ProgramView v, EventLogger eventLogger2) {
            super(v);
            this.eventLogger = eventLogger2;
            this.programController = new ProgramController(v, this, ChannelItemsAdapter.this.isSponsored, ChannelItemsAdapter.this.isSponsoredBranded);
            this.programController.setOnProgramViewFocusChangedListener(this);
            this.programController.setIsWatchNextProgram(false);
            this.programController.setProgramViewLiveProgressUpdateCallback(this);
        }

        public void onProgramViewFocusChanged(boolean hasFocus) {
            int position = getAdapterPosition();
            if (position != -1) {
                if (ChannelItemsAdapter.this.onProgramSelectedListener != null && hasFocus) {
                    ChannelItemsAdapter.this.onProgramSelectedListener.onProgramSelected(ChannelItemsAdapter.this.dataManager.getProgram(ChannelItemsAdapter.this.channelId, position), this.programController);
                }
                ChannelItemsAdapter.this.handler.removeCallbacks(this.notifyFocusChangedRunnable);
                if (ChannelItemsAdapter.this.programState == 4) {
                    if (this.programController.isProgramSelected() && !hasFocus) {
                        notifyFocusChanged();
                    }
                } else if (!hasFocus) {
                    int unused = ChannelItemsAdapter.this.lastUnfocusedAdapterPosition = getAdapterPosition();
                } else {
                    notifyFocusChanged();
                }
            }
        }

        private void notifyFocusChanged() {
            if (ChannelItemsAdapter.this.recyclerView == null || ChannelItemsAdapter.this.recyclerView.isComputingLayout()) {
                Log.w(ChannelItemsAdapter.TAG, "list is still computing layout => schedule program selection change");
                ChannelItemsAdapter.this.handler.post(this.notifyFocusChangedRunnable);
                return;
            }
            this.notifyFocusChangedRunnable.run();
        }

        public void updateProgramViewLiveProgress() {
            ChannelItemsAdapter.this.handler.removeCallbacks(this.notifyLiveProgressUpdateRunnable);
            if (ChannelItemsAdapter.this.recyclerView == null || ChannelItemsAdapter.this.recyclerView.isComputingLayout()) {
                Log.w(ChannelItemsAdapter.TAG, "list is still computing layout => schedule live progress change");
                ChannelItemsAdapter.this.handler.post(this.notifyLiveProgressUpdateRunnable);
                return;
            }
            this.notifyLiveProgressUpdateRunnable.run();
        }

        public void log(LogEvent event) {
            event.setVisualElementIndex(getAdapterPosition());
            TvlauncherClientLog.Channel.Builder channel = event.getChannel();
            channel.setPackageName(ChannelItemsAdapter.this.packageName);
            channel.setProgramCount(ChannelItemsAdapter.this.getItemCount());
            this.eventLogger.log(event);
        }

        public ProgramController getProgramController() {
            return this.programController;
        }

        /* access modifiers changed from: package-private */
        public void bind(Program program) {
            this.programController.bind(program, ChannelItemsAdapter.this.packageName, ChannelItemsAdapter.this.programState, ChannelItemsAdapter.this.canAddToWatchNext, ChannelItemsAdapter.this.canRemoveProgram, ChannelItemsAdapter.this.isLegacy);
            if (ChannelItemsAdapter.this.onProgramSelectedListener != null && this.programController.isViewFocused()) {
                ChannelItemsAdapter.this.onProgramSelectedListener.onProgramSelected(program, this.programController);
            }
            this.programController.setListStateProvider(ChannelItemsAdapter.this.listStateProvider);
            this.programController.setHomeListStateProvider(ChannelItemsAdapter.this.homeListStateProvider);
        }

        /* access modifiers changed from: package-private */
        public void recycle() {
            this.programController.setListStateProvider(null);
            this.programController.setHomeListStateProvider(null);
            this.programController.recycle();
            ChannelItemsAdapter.this.handler.removeCallbacks(this.notifyLiveProgressUpdateRunnable);
        }

        public void onHomePressed(Context c) {
            this.programController.onHomePressed(c);
        }
    }
}
