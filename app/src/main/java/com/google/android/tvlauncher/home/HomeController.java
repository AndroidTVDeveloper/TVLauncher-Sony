package com.google.android.tvlauncher.home;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import androidx.leanback.widget.FacetProvider;
import androidx.leanback.widget.ItemAlignmentFacet;
import androidx.leanback.widget.VerticalGridView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.BackHomeControllerListeners;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogEventParameters;
import com.google.android.tvlauncher.analytics.TvHomeDrawnManager;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.application.TvLauncherApplicationBase;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.data.ChannelOrderManager;
import com.google.android.tvlauncher.data.HomeChannelsObserver;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.data.WatchNextProgramsObserver;
import com.google.android.tvlauncher.home.view.ChannelView;
import com.google.android.tvlauncher.home.view.ConfigureChannelsRowView;
import com.google.android.tvlauncher.home.view.HomeRowAnimator;
import com.google.android.tvlauncher.inputs.InputsManagerUtil;
import com.google.android.tvlauncher.model.HomeChannel;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.notifications.NotificationsPanelController;
import com.google.android.tvlauncher.notifications.NotificationsTrayAdapter;
import com.google.android.tvlauncher.notifications.NotificationsUtils;
import com.google.android.tvlauncher.util.GservicesUtils;
import com.google.android.tvlauncher.util.IntentLaunchDispatcher;
import com.google.android.tvlauncher.util.KeylineUtil;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvlauncher.util.palette.PaletteUtil;
import com.google.android.tvlauncher.view.HomeTopRowView;
import com.google.android.tvlauncher.view.SearchView;
import com.google.android.tvlauncher.widget.PartnerWidgetInfo;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class HomeController extends RecyclerView.Adapter<HomeRowViewHolder> implements HomeTopRowView.OnActionListener, BackHomeControllerListeners.OnBackPressedListener, BackHomeControllerListeners.OnBackNotHandledListener, BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnHomeNotHandledListener, AccessibilityManager.AccessibilityStateChangeListener, LaunchItemsManager.AppsViewChangeListener, HomeTopRowView.OnHomeTopRowFocusChangedListener {
    private static final int ACCESSIBILITY_APP_SELECT_DELAY_MILLIS = 50;
    private static final int BACKGROUND_UPDATE_DELAY_MILLIS = 2000;
    private static final boolean DEBUG = false;
    private static final int EXIT_FAST_SCROLLING_DELAY_MILLIS = 600;
    private static final int MAX_SMOOTH_SCROLL_DISTANCE = 6;
    private static final int NOTIFY_IDLE_STATE_CHANGED_DELAY_MILLIS = 500;
    private static final int NUMBER_OF_ROWS_ABOVE_CHANNELS_WITHOUT_WATCH_NEXT = 2;
    private static final int NUMBER_OF_ROWS_ABOVE_CHANNELS_WITH_WATCH_NEXT = 3;
    private static final int NUMBER_OF_ROWS_BELOW_CHANNELS = 1;
    private static final String PAYLOAD_ASSISTANT_ICON = "PAYLOAD_ASSISTANT_ICON";
    private static final String PAYLOAD_ASSISTANT_SUGGESTIONS = "PAYLOAD_ASSISTANT_SUGGESTIONS";
    private static final String PAYLOAD_CHANNEL_ITEM_METADATA = "PAYLOAD_CHANNEL_ITEM_METADATA";
    private static final String PAYLOAD_CHANNEL_LOGO_TITLE = "PAYLOAD_CHANNEL_LOGO_TITLE";
    private static final String PAYLOAD_CHANNEL_MOVE_ACTION = "PAYLOAD_CHANNEL_MOVE_ACTION";
    private static final String PAYLOAD_FAST_SCROLLING = "PAYLOAD_FAST_SCROLLING";
    private static final String PAYLOAD_HOTWORD_STATUS = "PAYLOAD_HOTWORD_STATUS";
    private static final String PAYLOAD_IDLE_STATE = "PAYLOAD_IDLE_STATE";
    private static final String PAYLOAD_INPUT_ICON_VISIBILITY = "PAYLOAD_INPUT_ICON_VISIBILITY";
    private static final String PAYLOAD_MIC_STATUS = "PAYLOAD_MIC_STATUS";
    private static final String PAYLOAD_NOTIF_COUNT_CURSOR = "PAYLOAD_NOTIF_COUNT_CURSOR";
    private static final String PAYLOAD_NOTIF_TRAY_CURSOR = "PAYLOAD_NOTIF_TRAY_CURSOR";
    private static final String PAYLOAD_PARTNER_WIDGET_INFO = "PAYLOAD_PARTNER_WIDGET_INFO";
    private static final String PAYLOAD_STATE = "PAYLOAD_STATE";
    private static final String PAYLOAD_WATCH_NEXT_CARD_SELECTION_CHANGED = "PAYLOAD_WATCH_NEXT_CARD_SELECTION_CHANGED";
    private static final String PAYLOAD_WATCH_NEXT_DATA_CHANGED = "PAYLOAD_WATCH_NEXT_DATA_CHANGED";
    private static final long REFRESH_WATCH_NEXT_OFFSET_INTERVAL_MILLIS = 600000;
    private static final int ROW_TYPE_APPS = 1;
    private static final int ROW_TYPE_APPS_POSITION = 1;
    private static final int ROW_TYPE_BRANDED_SPONSORED_CHANNEL = 4;
    private static final int ROW_TYPE_CHANNEL = 3;
    private static final int ROW_TYPE_CONFIGURE_CHANNELS = 6;
    private static final int ROW_TYPE_TOP = 0;
    private static final int ROW_TYPE_TOP_POSITION = 0;
    private static final int ROW_TYPE_UNBRANDED_SPONSORED_CHANNEL = 5;
    private static final int ROW_TYPE_WATCH_NEXT = 2;
    private static final int ROW_TYPE_WATCH_NEXT_POSITION = 2;
    static final int STATE_CHANNEL_ACTIONS = 2;
    static final int STATE_DEFAULT = 0;
    static final int STATE_MOVE_CHANNEL = 3;
    static final int STATE_ZOOMED_OUT = 1;
    private static final String TAG = "HomeController";
    private static final int THRESHOLD_HOME_PRESS_PAUSE_INTERVAL_MILLIS = 200;
    private Set<ChannelRowController> activeChannelRowControllers = new HashSet();
    private Set<WatchNextRowController> activeWatchNextRowControllers = new HashSet();
    private Set<String> addToWatchNextPackagesBlacklist;
    /* access modifiers changed from: private */
    public ItemAlignmentFacet appsRowDefaultWithTrayFacet;
    /* access modifiers changed from: private */
    public ItemAlignmentFacet appsRowZoomedOutWithTrayFacet;
    private Drawable assistantIcon;
    private String[] assistantSuggestions;
    /* access modifiers changed from: private */
    public HomeBackgroundController backgroundController;
    private int channelKeyline;
    private RequestManager channelLogoRequestManager;
    private final HomeChannelsObserver channelsObserver = new HomeChannelsObserver() {
        public void onChannelsChange() {
            HomeController.this.emptyChannelsHelper.onChannelsChange();
            int count = HomeController.this.dataManager.getHomeChannelCount();
            if (count >= 0) {
                LogEvent event = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.SHOWN_CHANNEL_COUNT);
                event.getChannelCollection().setBrowsableCount(count);
                HomeController.this.eventLogger.log(event);
            }
            long oldSelectedId = HomeController.this.selectedId;
            if (HomeController.this.selectedPosition >= HomeController.this.getItemCount()) {
                HomeController homeController = HomeController.this;
                homeController.setSelectedPosition(homeController.getItemCount() - 1, false);
            }
            HomeController.this.updateSelectedId();
            if (HomeController.this.state == 3 || HomeController.this.state == 2) {
                if (HomeController.this.dataManager.isChannelEmpty(HomeController.this.selectedId)) {
                    int unused = HomeController.this.state = 1;
                } else if (oldSelectedId != HomeController.this.selectedId) {
                    int unused2 = HomeController.this.state = 1;
                }
            }
            if (HomeController.this.state == 1 && Util.isAccessibilityEnabled(HomeController.this.context)) {
                int unused3 = HomeController.this.state = 0;
            }
            HomeController.this.notifyDataSetChanged();
        }

        public void onChannelMove(int fromIndex, int toIndex) {
            int fromPosition = HomeController.this.getAdapterPositionForChannelIndex(fromIndex);
            int toPosition = HomeController.this.getAdapterPositionForChannelIndex(toIndex);
            if (HomeController.this.selectedPosition == fromPosition) {
                int unused = HomeController.this.selectedPosition = toPosition;
                HomeController.this.updateSelectedId();
            }
            int numChannels = HomeController.this.getChannelCount();
            if (fromIndex == 0 || toIndex == 0 || fromIndex == numChannels - 1 || toIndex == numChannels - 1) {
                HomeController.this.notifyItemChanged(fromPosition, HomeController.PAYLOAD_CHANNEL_MOVE_ACTION);
                HomeController.this.notifyItemChanged(toPosition, HomeController.PAYLOAD_CHANNEL_MOVE_ACTION);
            }
            HomeController.this.notifyItemMoved(fromPosition, toPosition);
        }

        public void onEmptyChannelRemove(int channelIndex) {
            HomeController homeController = HomeController.this;
            homeController.onItemRemoved(homeController.getAdapterPositionForChannelIndex(channelIndex));
        }

        public void onChannelEmptyStatusChange(int channelIndex) {
            int adapterPosition = HomeController.this.getAdapterPositionForChannelIndex(channelIndex);
            long channelId = HomeController.this.dataManager.getHomeChannel(channelIndex).getId();
            if (HomeController.this.selectedPosition != adapterPosition || (!(HomeController.this.state == 2 || HomeController.this.state == 3) || !HomeController.this.dataManager.isChannelEmpty(channelId))) {
                HomeController.this.notifyItemChanged(adapterPosition, HomeController.PAYLOAD_STATE);
            } else {
                if (Util.isAccessibilityEnabled(HomeController.this.context)) {
                    int unused = HomeController.this.state = 0;
                } else {
                    int unused2 = HomeController.this.state = 1;
                }
                HomeController homeController = HomeController.this;
                homeController.notifyItemRangeChanged(0, homeController.getItemCount(), HomeController.PAYLOAD_STATE);
            }
            HomeController.this.emptyChannelsHelper.onChannelEmptyStatusChange(channelId);
        }
    };
    /* access modifiers changed from: private */
    public ItemAlignmentFacet configureChannelsFacet;
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public final TvDataManager dataManager;
    /* access modifiers changed from: private */
    public EmptyChannelsHelper emptyChannelsHelper;
    /* access modifiers changed from: private */
    public final EventLogger eventLogger;
    /* access modifiers changed from: private */
    public RecyclerViewFastScrollingManager fastScrollingManager;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    private final RecyclerViewStateProvider homeListStateProvider = new RecyclerViewStateProvider() {
        public boolean isAnimating() {
            return HomeController.this.list != null && HomeController.this.list.isAnimating();
        }

        public boolean isAnimating(RecyclerView.ItemAnimator.ItemAnimatorFinishedListener listener) {
            if (HomeController.this.list != null && HomeController.this.list.getItemAnimator() != null) {
                return HomeController.this.list.getItemAnimator().isRunning(listener);
            }
            if (listener == null) {
                return false;
            }
            listener.onAnimationsFinished();
            return false;
        }
    };
    private boolean hotwordEnabled;
    private boolean inputIconVisible;
    private IntentLaunchDispatcher intentLauncher;
    private boolean isIdle = false;
    private long lastPausedTime;
    /* access modifiers changed from: private */
    public ProgramController lastSelectedProgramController;
    /* access modifiers changed from: private */
    public VerticalGridView list;
    private int micStatus;
    private Cursor notifCountCursor = null;
    /* access modifiers changed from: private */
    public Cursor notifTrayCursor = null;
    /* access modifiers changed from: private */
    public final Runnable notifyChannelItemMetadataChangedRunnable = new HomeController$$Lambda$3(this);
    private final Runnable notifyIdleStateChangedRunnable = new HomeController$$Lambda$5(this);
    private final Runnable notifySelectionChangedRunnable = new HomeController$$Lambda$1(this);
    private final Runnable notifyStateChangedRunnable = new HomeController$$Lambda$2(this);
    private final Runnable notifyTopRowStateChangedRunnable = new HomeController$$Lambda$0(this);
    /* access modifiers changed from: private */
    public final Runnable notifyWatchNextCardSelectionChangedRunnable = new HomeController$$Lambda$4(this);
    private BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener;
    private final OnProgramSelectedListener onProgramSelectedListener = new OnProgramSelectedListener() {
        public void onProgramSelected(Program program, ProgramController programController) {
            ProgramController unused = HomeController.this.lastSelectedProgramController = programController;
            if (HomeController.this.state == 0 && HomeController.this.fastScrollingManager.isFastScrollingEnabled()) {
                HomeController homeController = HomeController.this;
                if (homeController.hasFastScrollingMode(homeController.selectedPosition)) {
                    return;
                }
            }
            if (HomeController.this.backgroundController != null) {
                HomeController.this.handler.removeCallbacks(HomeController.this.updateBackgroundForProgramAfterDelayRunnable);
                HomeController.this.handler.postDelayed(HomeController.this.updateBackgroundForProgramAfterDelayRunnable, AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS);
            }
            HomeController homeController2 = HomeController.this;
            int rowType = homeController2.getItemViewType(homeController2.selectedPosition);
            if (rowType != 4 && rowType != 5) {
                HomeController.this.handler.removeCallbacks(HomeController.this.notifyChannelItemMetadataChangedRunnable);
                if (HomeController.this.list == null || HomeController.this.list.isComputingLayout()) {
                    Log.w(HomeController.TAG, "list is still computing layout => schedule item metadata change");
                    HomeController.this.handler.post(HomeController.this.notifyChannelItemMetadataChangedRunnable);
                } else {
                    HomeController.this.notifyChannelItemMetadataChangedRunnable.run();
                }
                HomeController homeController3 = HomeController.this;
                if (homeController3.getItemViewType(homeController3.selectedPosition) == 2) {
                    HomeController.this.handler.removeCallbacks(HomeController.this.notifyWatchNextCardSelectionChangedRunnable);
                    if (HomeController.this.list == null || HomeController.this.list.isComputingLayout()) {
                        Log.w(HomeController.TAG, "list is still computing layout => schedule watch next card selection changed");
                        HomeController.this.handler.post(HomeController.this.notifyWatchNextCardSelectionChangedRunnable);
                        return;
                    }
                    HomeController.this.notifyWatchNextCardSelectionChangedRunnable.run();
                }
            }
        }
    };
    private PartnerWidgetInfo partnerWidgetInfo;
    private HashSet<Integer> prevSelectedPositions = new HashSet<>();
    /* access modifiers changed from: private */
    public Runnable refreshWatchNextOffset = new Runnable() {
        public void run() {
            if (HomeController.this.watchNextEnabled) {
                HomeController.this.dataManager.refreshWatchNextOffset();
                HomeController.this.updateWatchNextVisibility();
            }
            HomeController.this.watchNextHandler.postDelayed(HomeController.this.refreshWatchNextOffset, 600000);
        }
    };
    private Set<String> removeProgramPackagesBlacklist;
    /* access modifiers changed from: private */
    public Runnable requeryWatchNext = new Runnable() {
        public void run() {
            if (HomeController.this.watchNextEnabled) {
                HomeController.this.dataManager.loadWatchNextProgramData();
            }
            HomeController.this.watchNextHandler.postDelayed(HomeController.this.requeryWatchNext, 600000);
        }
    };
    /* access modifiers changed from: private */
    public ItemAlignmentFacet secondToLastRowZoomedOutFacet;
    private Runnable selectFirstAppRunnable = new Runnable() {
        public void run() {
            if (HomeController.this.selectFirstAppWhenRowSelected && HomeController.this.selectedPosition == 1) {
                HomeRow appsRow = HomeController.this.getHomeRow(1);
                if (appsRow instanceof FavoriteLaunchItemsRowController) {
                    ((FavoriteLaunchItemsRowController) appsRow).setSelectedItemPosition(0);
                    boolean unused = HomeController.this.selectFirstAppWhenRowSelected = false;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean selectFirstAppWhenRowSelected;
    /* access modifiers changed from: private */
    public long selectedId;
    /* access modifiers changed from: private */
    public int selectedPosition = 1;
    /* access modifiers changed from: private */
    public ItemAlignmentFacet sponsoredChannelFacet;
    private boolean started;
    /* access modifiers changed from: private */
    public int state = 0;
    /* access modifiers changed from: private */
    public ItemAlignmentFacet thirdToLastRowZoomedOutFacet;
    private IntentFilter timeFilter = new IntentFilter();
    private final BroadcastReceiver timeSetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HomeController.this.watchNextHandler.removeCallbacks(HomeController.this.refreshWatchNextOffset);
            HomeController.this.watchNextHandler.removeCallbacks(HomeController.this.requeryWatchNext);
            HomeController.this.watchNextHandler.post(HomeController.this.refreshWatchNextOffset);
            HomeController.this.watchNextHandler.post(HomeController.this.requeryWatchNext);
        }
    };
    /* access modifiers changed from: private */
    public final Runnable updateBackgroundForProgramAfterDelayRunnable = new Runnable() {
        public void run() {
            if (!HomeController.this.fastScrollingManager.isFastScrollingEnabled() && HomeController.this.backgroundController != null && HomeController.this.lastSelectedProgramController != null && HomeController.this.lastSelectedProgramController.isViewFocused() && HomeController.this.lastSelectedProgramController.getPreviewImagePalette() != null) {
                HomeController.this.backgroundController.updateBackground(HomeController.this.lastSelectedProgramController.getPreviewImagePalette());
            }
        }
    };
    private final Runnable updateBackgroundForTopRowsAfterDelayRunnable = new Runnable() {
        public void run() {
            if (HomeController.this.backgroundController != null && HomeController.this.selectedPosition <= 1) {
                HomeController.this.backgroundController.enterDarkMode();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean watchNextEnabled;
    /* access modifiers changed from: private */
    public Handler watchNextHandler = new Handler();
    private SharedPreferences.OnSharedPreferenceChangeListener watchNextPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            boolean newWatchNextEnabled;
            if (WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY.equals(key) && HomeController.this.watchNextEnabled != (newWatchNextEnabled = sharedPreferences.getBoolean(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY, OemConfiguration.get(HomeController.this.context).isWatchNextChannelEnabledByDefault()))) {
                boolean unused = HomeController.this.watchNextEnabled = newWatchNextEnabled;
                if (HomeController.this.watchNextEnabled) {
                    HomeController.this.registerObserverAndUpdateWatchNextDataIfNeeded();
                    return;
                }
                HomeController.this.setWatchNextVisibility(false);
                HomeController.this.dataManager.unregisterWatchNextProgramsObserver(HomeController.this.watchNextProgramsObserver);
            }
        }
    };
    /* access modifiers changed from: private */
    public final WatchNextProgramsObserver watchNextProgramsObserver = new WatchNextProgramsObserver() {
        public void onProgramsChange() {
            if (HomeController.this.watchNextEnabled) {
                if (HomeController.this.watchNextVisible) {
                    HomeController homeController = HomeController.this;
                    if (homeController.getItemViewType(homeController.selectedPosition) == 2) {
                        HomeController homeController2 = HomeController.this;
                        homeController2.notifyItemChanged(homeController2.selectedPosition, HomeController.PAYLOAD_WATCH_NEXT_DATA_CHANGED);
                    }
                }
                HomeController.this.updateWatchNextVisibility();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean watchNextVisible;

    @Retention(RetentionPolicy.SOURCE)
    @interface RowType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public /* bridge */ /* synthetic */ void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i, List list2) {
        onBindViewHolder((HomeRowViewHolder) viewHolder, i, (List<Object>) list2);
    }

    /* access modifiers changed from: package-private */
    public HomeChannelsObserver getChannelsObserver() {
        return this.channelsObserver;
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$HomeController() {
        notifyItemChanged(0, PAYLOAD_STATE);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$1$HomeController() {
        int i;
        int i2;
        Set<Integer> positionsToUpdate = new HashSet<>(this.prevSelectedPositions.size() + 5);
        int i3 = this.state;
        if (i3 == 0 || i3 == 1) {
            positionsToUpdate.addAll(this.prevSelectedPositions);
            this.prevSelectedPositions.clear();
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition));
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition - 1));
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 1));
        }
        if (this.state == 0 && ((i2 = this.selectedPosition) == 0 || i2 == 1)) {
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 2));
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 3));
        }
        if (this.state == 1 && ((i = this.selectedPosition) == 0 || i == 1)) {
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 2));
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 3));
            positionsToUpdate.add(Integer.valueOf(this.selectedPosition + 4));
        }
        int itemCount = getItemCount();
        for (Integer position : positionsToUpdate) {
            if (position.intValue() >= 0 && position.intValue() < itemCount) {
                notifyItemChanged(position.intValue(), PAYLOAD_STATE);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$2$HomeController() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$3$HomeController() {
        notifyItemChanged(this.selectedPosition, PAYLOAD_CHANNEL_ITEM_METADATA);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$4$HomeController() {
        notifyItemChanged(this.selectedPosition, PAYLOAD_WATCH_NEXT_CARD_SELECTION_CHANGED);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$5$HomeController() {
        notifyItemChanged(0, PAYLOAD_IDLE_STATE);
    }

    private static String stateToString(int state2) {
        String stateString;
        if (state2 == 0) {
            stateString = "STATE_DEFAULT";
        } else if (state2 == 1) {
            stateString = "STATE_ZOOMED_OUT";
        } else if (state2 == 2) {
            stateString = "STATE_CHANNEL_ACTIONS";
        } else if (state2 != 3) {
            stateString = "STATE_UNKNOWN";
        } else {
            stateString = "STATE_MOVE_CHANNEL";
        }
        StringBuilder sb = new StringBuilder(stateString.length() + 14);
        sb.append(stateString);
        sb.append(" (");
        sb.append(state2);
        sb.append(")");
        return sb.toString();
    }

    HomeController(Context context2, EventLogger eventLogger2, TvDataManager dataManager2, EmptyChannelsHelper emptyChannelsHelper2) {
        this.context = context2;
        this.eventLogger = eventLogger2;
        this.dataManager = dataManager2;
        this.emptyChannelsHelper = emptyChannelsHelper2;
        this.intentLauncher = ((TvLauncherApplicationBase) context2.getApplicationContext()).getIntentLauncher();
        this.timeFilter.addAction("android.intent.action.TIME_SET");
        this.timeFilter.addAction("android.intent.action.DATE_CHANGED");
        this.timeFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.channelKeyline = context2.getResources().getDimensionPixelSize(C1167R.dimen.channel_items_list_keyline);
        this.appsRowZoomedOutWithTrayFacet = createChannelItemAlignmentFacet(C1167R.dimen.channel_items_list_apps_row_zoomed_out_notif_tray_keyline_offset);
        this.appsRowDefaultWithTrayFacet = createChannelItemAlignmentFacet(C1167R.dimen.channel_items_list_apps_row_notif_tray_keyline_offset);
        this.sponsoredChannelFacet = createChannelItemAlignmentFacet(C1167R.dimen.channel_items_list_sponsored_channel_keyline_offset);
        this.configureChannelsFacet = KeylineUtil.createItemAlignmentFacet(context2.getResources().getDimensionPixelSize(C1167R.dimen.home_configure_channels_keyline_offset), Integer.valueOf(C1167R.C1170id.button));
        this.secondToLastRowZoomedOutFacet = createChannelItemAlignmentFacet(C1167R.dimen.channel_items_list_second_to_last_row_zoomed_out_keyline_offset);
        this.thirdToLastRowZoomedOutFacet = createChannelItemAlignmentFacet(C1167R.dimen.channel_items_list_third_to_last_row_zoomed_out_keyline_offset);
        setHasStableIds(true);
        SharedPreferences watchNextPreferences = context2.getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0);
        this.watchNextEnabled = watchNextPreferences.getBoolean(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY, OemConfiguration.get(context2).isWatchNextChannelEnabledByDefault());
        watchNextPreferences.registerOnSharedPreferenceChangeListener(this.watchNextPrefListener);
        PaletteUtil.registerGlidePaletteTranscoder(context2);
        this.addToWatchNextPackagesBlacklist = GservicesUtils.retrievePackagesBlacklistedForWatchNext(context2.getContentResolver());
        this.removeProgramPackagesBlacklist = GservicesUtils.retrievePackagesBlacklistedForProgramRemoval(context2.getContentResolver());
        registerChannelsObserverAndLoadDataIfNeeded();
        if (this.watchNextEnabled) {
            registerObserverAndUpdateWatchNextDataIfNeeded();
        }
        this.started = true;
        updateSelectedId();
    }

    public void setList(VerticalGridView list2) {
        this.list = list2;
        if (list2.getLayoutManager() != null) {
            list2.getLayoutManager().setItemPrefetchEnabled(false);
        }
        list2.setItemAlignmentViewId(C1167R.C1170id.items_list);
        list2.setWindowAlignment(1);
        list2.setWindowAlignmentOffsetPercent(0.0f);
        list2.setWindowAlignmentOffset(this.channelKeyline);
        list2.setItemViewCacheSize(5);
        list2.setSelectedPosition(this.selectedPosition);
        TvHomeDrawnManager.getInstance().monitorHomeListViewDrawn(list2);
        this.fastScrollingManager = new RecyclerViewFastScrollingManager(list2, new HomeRowAnimator());
        this.fastScrollingManager.setOnFastScrollingChangedListener(new HomeController$$Lambda$6(this));
        this.fastScrollingManager.setAnimatorEnabled(true);
        this.fastScrollingManager.setScrollEnabled(false);
        this.fastScrollingManager.setExitFastScrollingDelayMs(600);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$setList$6$HomeController(boolean fastScrolling) {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_FAST_SCROLLING);
        if (!fastScrolling) {
            notifyItemChanged(this.selectedPosition, PAYLOAD_CHANNEL_ITEM_METADATA);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isWatchNextVisible() {
        return this.watchNextVisible;
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundController(HomeBackgroundController backgroundController2) {
        this.backgroundController = backgroundController2;
    }

    /* access modifiers changed from: package-private */
    public void setOnBackNotHandledListener(BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2) {
        this.onBackNotHandledListener = onBackNotHandledListener2;
    }

    /* access modifiers changed from: package-private */
    public void updateWatchNextVisibility() {
        setWatchNextVisibility(shouldShowWatchNextChannel());
    }

    /* access modifiers changed from: private */
    public void setWatchNextVisibility(boolean watchNextVisible2) {
        if (this.watchNextVisible != watchNextVisible2) {
            this.watchNextVisible = watchNextVisible2;
            if (this.watchNextVisible) {
                int i = this.selectedPosition;
                if (i >= 2) {
                    this.selectedPosition = i + 1;
                }
                notifyItemInserted(2);
                notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
            } else {
                int i2 = this.selectedPosition;
                if (i2 > 2) {
                    this.selectedPosition = i2 - 1;
                }
                notifyItemRemoved(2);
                notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
            }
            updateSelectedId();
        }
    }

    /* access modifiers changed from: private */
    public void registerObserverAndUpdateWatchNextDataIfNeeded() {
        this.dataManager.registerWatchNextProgramsObserver(this.watchNextProgramsObserver);
        if (!this.dataManager.isWatchNextProgramsDataLoaded() || this.dataManager.isWatchNextProgramsDataStale()) {
            this.dataManager.loadWatchNextProgramData();
            this.dataManager.loadAllWatchNextProgramDataIntoCache();
            return;
        }
        updateWatchNextVisibility();
    }

    private void registerChannelsObserverAndLoadDataIfNeeded() {
        this.dataManager.registerHomeChannelsObserver(this.channelsObserver);
        if (!this.dataManager.isHomeChannelDataLoaded() || this.dataManager.isHomeChannelDataStale()) {
            this.dataManager.loadHomeChannelData();
        }
    }

    private boolean shouldShowWatchNextChannel() {
        return this.dataManager.getWatchNextProgramsCount() > 0;
    }

    /* access modifiers changed from: private */
    public int getChannelCount() {
        if (this.dataManager.isHomeChannelDataLoaded()) {
            return this.dataManager.getHomeChannelCount();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void onStart() {
        ActiveMediaSessionManager.getInstance(this.context).start();
        this.context.registerReceiver(this.timeSetReceiver, this.timeFilter);
        LaunchItemsManagerProvider.getInstance(this.context).registerAppsViewChangeListener(this);
        for (WatchNextRowController controller : this.activeWatchNextRowControllers) {
            controller.onStart();
        }
        for (ChannelRowController controller2 : this.activeChannelRowControllers) {
            controller2.onStart();
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.context.getSystemService("accessibility");
        if (accessibilityManager != null) {
            accessibilityManager.addAccessibilityStateChangeListener(this);
        }
        LaunchItemsManager itemsManager = LaunchItemsManagerProvider.getInstance(this.context);
        if (!itemsManager.areItemsLoaded() || itemsManager.hasPendingLoadRequest()) {
            itemsManager.refreshLaunchItems();
        }
        if (!this.started) {
            this.emptyChannelsHelper.onStart();
            registerChannelsObserverAndLoadDataIfNeeded();
            if (this.watchNextEnabled) {
                registerObserverAndUpdateWatchNextDataIfNeeded();
            }
            this.started = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onStop() {
        ActiveMediaSessionManager.getInstance(this.context).stop();
        if (NotificationsUtils.hasNowPlaying(this.notifTrayCursor)) {
            updateNotifications(null);
            HomeTopRowView topRowView = getHomeTopRowView();
            if (topRowView != null) {
                topRowView.hideNotificationsTray();
            }
        }
        this.context.unregisterReceiver(this.timeSetReceiver);
        this.emptyChannelsHelper.onStop();
        this.dataManager.unregisterHomeChannelsObserver(this.channelsObserver);
        this.dataManager.unregisterWatchNextProgramsObserver(this.watchNextProgramsObserver);
        this.dataManager.pruneChannelProgramsCache();
        LaunchItemsManagerProvider.getInstance(this.context).unregisterAppsViewChangeListener(this);
        for (WatchNextRowController controller : this.activeWatchNextRowControllers) {
            controller.onStop();
        }
        for (ChannelRowController controller2 : this.activeChannelRowControllers) {
            controller2.onStop();
        }
        AccessibilityManager manager = (AccessibilityManager) this.context.getSystemService("accessibility");
        if (manager != null) {
            manager.removeAccessibilityStateChangeListener(this);
        }
        this.started = false;
    }

    /* access modifiers changed from: package-private */
    public void onResume() {
        this.watchNextHandler.postDelayed(this.refreshWatchNextOffset, 600000);
        this.watchNextHandler.postDelayed(this.requeryWatchNext, 600000);
        if (this.dataManager.isHomeChannelDataLoaded()) {
            LogEventParameters eventParams = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.SHOWN_CHANNEL_COUNT);
            eventParams.getChannelCollection().setBrowsableCount(this.dataManager.getHomeChannelCount());
            this.eventLogger.log(eventParams);
        }
        if (isWatchNextVisible() && this.dataManager.isWatchNextProgramsDataLoaded()) {
            LogEventParameters eventParams2 = new LogEventParameters(TvlauncherLogEnum.TvLauncherEventCode.OPEN_HOME, LogEventParameters.WATCH_NEXT);
            eventParams2.getWatchNextChannel().setProgramCount(this.dataManager.getWatchNextProgramsCount());
            this.eventLogger.log(eventParams2);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPause() {
        ProgramController programController = this.lastSelectedProgramController;
        if (programController != null) {
            programController.stopPreviewMedia();
        }
        this.watchNextHandler.removeCallbacks(this.refreshWatchNextOffset);
        this.watchNextHandler.removeCallbacks(this.requeryWatchNext);
        this.lastPausedTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public HomeRow getHomeRow(int position) {
        HomeRowViewHolder holder = (HomeRowViewHolder) this.list.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            return holder.homeRow;
        }
        return null;
    }

    private HomeRow getSelectedHomeRow() {
        return getHomeRow(this.selectedPosition);
    }

    private HomeTopRowView getHomeTopRowView() {
        HomeRow row = getHomeRow(0);
        if (row != null) {
            return (HomeTopRowView) row.getView();
        }
        return null;
    }

    private void scrollToAppsRow() {
        this.selectFirstAppWhenRowSelected = true;
        if (this.list.getSelectedPosition() > 6 || Util.areHomeScreenAnimationsEnabled(this.context)) {
            this.list.setLayoutFrozen(true);
            this.list.setSelectedPosition(1);
            setSelectedPosition(1, true);
            this.list.setLayoutFrozen(false);
            return;
        }
        this.list.setSelectedPositionSmooth(1);
    }

    public void onAccessibilityStateChanged(boolean enabled) {
        notifyItemChanged(0, PAYLOAD_NOTIF_TRAY_CURSOR);
        if (enabled && this.state != 0) {
            this.state = 0;
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
        }
    }

    public void onBackPressed(Context c) {
        HomeRow homeRow = getSelectedHomeRow();
        if (homeRow instanceof BackHomeControllerListeners.OnBackPressedListener) {
            ((BackHomeControllerListeners.OnBackPressedListener) homeRow).onBackPressed(c);
        } else {
            onBackNotHandled(c);
        }
    }

    public void onBackNotHandled(Context c) {
        int i;
        if (this.list.getSelectedPosition() == 1 || !((i = this.state) == 0 || i == 1)) {
            BackHomeControllerListeners.OnBackNotHandledListener onBackNotHandledListener2 = this.onBackNotHandledListener;
            if (onBackNotHandledListener2 != null) {
                onBackNotHandledListener2.onBackNotHandled(c);
                return;
            }
            return;
        }
        scrollToAppsRow();
    }

    public void onHomePressed(Context c) {
        if (SystemClock.elapsedRealtime() - this.lastPausedTime <= 200) {
            HomeRow homeRow = getSelectedHomeRow();
            if (homeRow instanceof BackHomeControllerListeners.OnHomePressedListener) {
                ((BackHomeControllerListeners.OnHomePressedListener) homeRow).onHomePressed(c);
            } else {
                onHomeNotHandled(c);
            }
        }
    }

    public void onHomeNotHandled(Context c) {
        if (this.list.getSelectedPosition() != 1) {
            int i = this.state;
            if (i == 0 || i == 1) {
                scrollToAppsRow();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public WatchNextProgramsObserver getWatchNextProgramsObserver() {
        return this.watchNextProgramsObserver;
    }

    /* access modifiers changed from: package-private */
    public void updateInputIconVisibility(boolean visible) {
        if (this.inputIconVisible != visible) {
            this.inputIconVisible = visible;
            notifyItemChanged(0, PAYLOAD_INPUT_ICON_VISIBILITY);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInputIconVisible() {
        return this.inputIconVisible;
    }

    /* access modifiers changed from: package-private */
    public void updateNotifications(Cursor cursor) {
        Cursor oldCursor = this.notifTrayCursor;
        this.notifTrayCursor = cursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        notifyItemChanged(0, PAYLOAD_NOTIF_TRAY_CURSOR);
    }

    /* access modifiers changed from: package-private */
    public void updatePanelNotificationsCount(Cursor cursor) {
        this.notifCountCursor = cursor;
        notifyItemChanged(0, PAYLOAD_NOTIF_COUNT_CURSOR);
    }

    /* access modifiers changed from: package-private */
    public void onIdleStateChange(boolean isIdle2) {
        this.isIdle = isIdle2;
        this.handler.removeCallbacks(this.notifyIdleStateChangedRunnable);
        this.handler.postDelayed(this.notifyIdleStateChangedRunnable, 500);
    }

    /* access modifiers changed from: package-private */
    public void onSearchIconUpdate(Drawable assistantIcon2) {
        this.assistantIcon = assistantIcon2;
        notifyItemChanged(0, PAYLOAD_ASSISTANT_ICON);
    }

    /* access modifiers changed from: package-private */
    public void onSearchSuggestionsUpdate(String[] assistantSuggestions2) {
        this.assistantSuggestions = assistantSuggestions2;
        notifyItemChanged(0, PAYLOAD_ASSISTANT_SUGGESTIONS);
    }

    /* access modifiers changed from: package-private */
    public void onPartnerWidgetUpdate(PartnerWidgetInfo info) {
        this.partnerWidgetInfo = info;
        notifyItemChanged(0, PAYLOAD_PARTNER_WIDGET_INFO);
    }

    /* access modifiers changed from: package-private */
    public void onMicStatusUpdated(Integer status) {
        this.micStatus = status == null ? 0 : status.intValue();
        notifyItemChanged(0, PAYLOAD_MIC_STATUS);
    }

    /* access modifiers changed from: package-private */
    public void onHotwordEnabledUpdated(Boolean enabled) {
        this.hotwordEnabled = enabled != null && enabled.booleanValue();
        notifyItemChanged(0, PAYLOAD_HOTWORD_STATUS);
    }

    /* access modifiers changed from: package-private */
    public void setChannelLogoRequestManager(RequestManager requestManager) {
        this.channelLogoRequestManager = requestManager;
        this.channelLogoRequestManager.setDefaultRequestOptions((RequestOptions) new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE));
    }

    private ItemAlignmentFacet createChannelItemAlignmentFacet(int dimenId) {
        return KeylineUtil.createItemAlignmentFacet(this.context.getResources().getDimensionPixelSize(dimenId), Integer.valueOf(C1167R.C1170id.items_list));
    }

    private int getNumberOfRowsAboveChannels() {
        if (this.watchNextVisible) {
            return 3;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public boolean hasFastScrollingMode(int position) {
        return position > 3 && position < getItemCount() - 3;
    }

    /* access modifiers changed from: package-private */
    public void setState(int newState) {
        boolean z = true;
        if (Util.isAccessibilityEnabled(this.context) && (newState == 1 || newState == 2 || newState == 3)) {
            newState = 0;
        }
        int i = this.state;
        if (i != newState) {
            if (i != 0) {
                if (i == 1) {
                    this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.EXIT_ZOOMED_OUT_MODE).setVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                } else if (i == 2) {
                    this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.EXIT_CHANNEL_ACTIONS_MODE).setVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                } else if (i == 3) {
                    this.eventLogger.log(new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.EXIT_MOVE_CHANNEL_MODE).setVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                }
            }
            this.state = newState;
            int i2 = this.state;
            if (i2 != 0) {
                if (i2 == 1) {
                    this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.ENTER_ZOOMED_OUT_MODE).setVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                } else if (i2 == 2) {
                    this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.ENTER_CHANNEL_ACTIONS_MODE).setVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                } else if (i2 == 3) {
                    this.eventLogger.log(new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.ENTER_MOVE_CHANNEL_MODE).setVisualElementTag(TvLauncherConstants.MOVE_CHANNEL_BUTTON).pushParentVisualElementTag(TvLauncherConstants.CHANNEL_ROW));
                }
            }
            RecyclerViewFastScrollingManager recyclerViewFastScrollingManager = this.fastScrollingManager;
            if (this.state == 3) {
                z = false;
            }
            recyclerViewFastScrollingManager.setScrollAllowedDuringFastScrolling(z);
            if (this.list.isComputingLayout()) {
                this.handler.post(this.notifyStateChangedRunnable);
            } else {
                this.notifyStateChangedRunnable.run();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getState() {
        return this.state;
    }

    /* access modifiers changed from: package-private */
    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    /* access modifiers changed from: package-private */
    public void setSelectedPosition(int newSelectedPosition) {
        setSelectedPosition(newSelectedPosition, true);
    }

    /* access modifiers changed from: private */
    public void setSelectedPosition(int newSelectedPosition, boolean notifyChange) {
        if (this.selectedPosition != newSelectedPosition) {
            if (this.state != 0 || !this.fastScrollingManager.isFastScrollingEnabled() || !hasFastScrollingMode(newSelectedPosition)) {
                if (this.selectedPosition >= getNumberOfRowsAboveChannels() && this.selectedPosition < getItemCount() - 1 && this.dataManager.isChannelEmpty(this.selectedId)) {
                    this.handler.post(new HomeController$$Lambda$7(this, this.selectedId));
                }
                int i = this.state;
                if ((i == 0 || i == 1) && notifyChange) {
                    this.prevSelectedPositions.add(Integer.valueOf(this.selectedPosition));
                    this.selectedPosition = newSelectedPosition;
                    updateSelectedId();
                    this.handler.removeCallbacks(this.notifySelectionChangedRunnable);
                    if (this.list.isComputingLayout()) {
                        Log.w(TAG, "setSelectedPosition: still computing layout => scheduling");
                        this.handler.post(this.notifySelectionChangedRunnable);
                    } else {
                        this.notifySelectionChangedRunnable.run();
                    }
                } else {
                    this.selectedPosition = newSelectedPosition;
                    updateSelectedId();
                }
                if (this.selectedPosition > 1 || this.backgroundController == null) {
                    this.handler.removeCallbacks(this.updateBackgroundForTopRowsAfterDelayRunnable);
                } else {
                    this.handler.postDelayed(this.updateBackgroundForTopRowsAfterDelayRunnable, AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS);
                }
            } else {
                this.selectedPosition = newSelectedPosition;
                updateSelectedId();
            }
            if (this.selectFirstAppWhenRowSelected && newSelectedPosition == 1) {
                this.handler.removeCallbacks(this.selectFirstAppRunnable);
                if (Util.isAccessibilityEnabled(this.context)) {
                    this.handler.postDelayed(this.selectFirstAppRunnable, 50);
                } else {
                    this.selectFirstAppRunnable.run();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$setSelectedPosition$7$HomeController(long copiedSelectedId) {
        this.dataManager.hideEmptyChannel(copiedSelectedId);
    }

    /* access modifiers changed from: package-private */
    public long getSelectedId() {
        return this.selectedId;
    }

    /* access modifiers changed from: package-private */
    public void updateSelectedId() {
        this.selectedId = getItemId(Math.min(this.selectedPosition, getItemCount() - 1));
    }

    /* access modifiers changed from: package-private */
    public void onItemRemoved(int position) {
        int i;
        this.emptyChannelsHelper.onChannelsChange();
        boolean isFirstChannel = true;
        boolean isLastChannel = position == getItemCount() - 1;
        int i2 = this.selectedPosition;
        if (i2 > position) {
            this.selectedPosition = i2 - 1;
        }
        long oldSelectedId = this.selectedId;
        updateSelectedId();
        if (oldSelectedId != this.selectedId && ((i = this.state) == 3 || i == 2)) {
            this.state = 1;
        }
        if (this.state == 1 && Util.isAccessibilityEnabled(this.context)) {
            this.state = 0;
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE);
        if (position != getNumberOfRowsAboveChannels()) {
            isFirstChannel = false;
        }
        if (isFirstChannel && !isLastChannel) {
            notifyItemChanged(position, PAYLOAD_CHANNEL_MOVE_ACTION);
        }
        if (isLastChannel && !isFirstChannel) {
            notifyItemChanged(position - 1, PAYLOAD_CHANNEL_MOVE_ACTION);
        }
    }

    /* access modifiers changed from: private */
    public int getAdapterPositionForChannelIndex(int channelIndex) {
        return getNumberOfRowsAboveChannels() + channelIndex;
    }

    /* access modifiers changed from: private */
    public int getChannelIndexForAdapterPosition(int position) {
        return position - getNumberOfRowsAboveChannels();
    }

    public int getItemCount() {
        return getChannelCount() + getNumberOfRowsAboveChannels() + 1;
    }

    public long getItemId(int position) {
        switch (getItemViewType(position)) {
            case 0:
                return -2;
            case 1:
                return -3;
            case 2:
                return -4;
            case 3:
            case 4:
            case 5:
                return this.dataManager.getHomeChannel(getChannelIndexForAdapterPosition(position)).getId();
            case 6:
                return -5;
            default:
                return super.getItemId(position);
        }
    }

    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return 6;
        }
        if (position == 0) {
            return 0;
        }
        if (position == 1) {
            return 1;
        }
        if (position == 2 && this.watchNextVisible) {
            return 2;
        }
        HomeChannel homeChannel = this.dataManager.getHomeChannel(getChannelIndexForAdapterPosition(position));
        if (!homeChannel.isSponsored()) {
            return 3;
        }
        if (homeChannel.getSubtype() == 2) {
            return 5;
        }
        return 4;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public HomeRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = parent;
        int i = viewType;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        boolean z = false;
        if (i == 0) {
            HomeTopRowView v = (HomeTopRowView) inflater.inflate(C1167R.layout.home_top_row, viewGroup, false);
            v.setOnActionListener(this);
            v.setEventLogger(this.eventLogger);
            v.setFocusChangeListener(this);
            NotificationsPanelController controller = new NotificationsPanelController(parent.getContext(), this.eventLogger);
            v.setNotificationsTrayAdapter(new NotificationsTrayAdapter(parent.getContext(), this.eventLogger, this.notifTrayCursor));
            v.setNotificationsPanelController(controller);
            return new HomeRowViewHolder(v);
        } else if (i == 1) {
            ChannelView channelView = (ChannelView) inflater.inflate(C1167R.layout.home_channel_row, viewGroup, false);
            channelView.setId(C1167R.C1170id.apps_row);
            FavoriteLaunchItemsRowController controller2 = new FavoriteLaunchItemsRowController(channelView, this.eventLogger);
            controller2.setOnBackNotHandledListener(this);
            controller2.setOnHomeNotHandledListener(this);
            controller2.setHomeListStateProvider(this.homeListStateProvider);
            return new HomeRowViewHolder(controller2);
        } else if (i == 2) {
            WatchNextRowController controller3 = new WatchNextRowController((ChannelView) inflater.inflate(C1167R.layout.home_channel_row, viewGroup, false), this.eventLogger);
            controller3.setOnProgramSelectedListener(this.onProgramSelectedListener);
            controller3.setOnBackNotHandledListener(this);
            controller3.setOnHomeNotHandledListener(this);
            controller3.setHomeListStateProvider(this.homeListStateProvider);
            return new HomeRowViewHolder(controller3);
        } else if (i == 6) {
            return new HomeRowViewHolder(new ConfigureChannelsRowController((ConfigureChannelsRowView) inflater.inflate(C1167R.layout.home_configure_channels_row, viewGroup, false)));
        } else {
            ChannelView channelView2 = (ChannelView) inflater.inflate(C1167R.layout.home_channel_row, viewGroup, false);
            ChannelEventLogger logger = new ChannelEventLogger();
            RequestManager requestManager = this.channelLogoRequestManager;
            ChannelOrderManager channelOrderManager = TvDataManager.getInstance(this.context).getChannelOrderManager();
            ChannelItemMetadataController channelItemMetadataController = new ChannelItemMetadataController(channelView2.getItemMetadataView());
            IntentLaunchDispatcher intentLaunchDispatcher = this.intentLauncher;
            boolean z2 = i == 4 || i == 5;
            if (i == 4) {
                z = true;
            }
            ChannelRowController controller4 = new ChannelRowController(channelView2, requestManager, logger, channelOrderManager, channelItemMetadataController, intentLaunchDispatcher, z2, z);
            controller4.setOnProgramSelectedListener(this.onProgramSelectedListener);
            controller4.setOnBackNotHandledListener(this);
            controller4.setOnHomeNotHandledListener(this);
            controller4.setHomeListStateProvider(this.homeListStateProvider);
            HomeRowViewHolder viewHolder = new HomeRowViewHolder(controller4);
            HomeRowViewHolder unused = logger.viewHolder = viewHolder;
            return viewHolder;
        }
    }

    public void onBindViewHolder(HomeRowViewHolder holder, int position, List<Object> payloads) {
        HomeRowViewHolder homeRowViewHolder = holder;
        int i = position;
        List<Object> list2 = payloads;
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        int rowType = getItemViewType(i);
        if (list2.contains(PAYLOAD_CHANNEL_ITEM_METADATA)) {
            if (rowType == 3) {
                ((ChannelRowController) homeRowViewHolder.homeRow).bindItemMetadata();
            } else if (rowType == 2) {
                ((WatchNextRowController) homeRowViewHolder.homeRow).bindItemMetadata();
            }
        }
        if (rowType == 2 && (list2.contains(PAYLOAD_WATCH_NEXT_CARD_SELECTION_CHANGED) || list2.contains(PAYLOAD_WATCH_NEXT_DATA_CHANGED))) {
            ((WatchNextRowController) homeRowViewHolder.homeRow).bindInfoAcknowledgedButton();
        }
        if (list2.contains(PAYLOAD_STATE)) {
            if (rowType == 2) {
                WatchNextRowController controller = (WatchNextRowController) homeRowViewHolder.homeRow;
                controller.setState(getNonEmptyChannelState(i));
                this.activeWatchNextRowControllers.add(controller);
            } else if (rowType == 3 || rowType == 4 || rowType == 5) {
                ChannelRowController controller2 = (ChannelRowController) homeRowViewHolder.homeRow;
                long channelId = this.dataManager.getHomeChannel(getChannelIndexForAdapterPosition(i)).getId();
                boolean isEmpty = this.dataManager.isChannelEmpty(channelId);
                controller2.setState(getChannelState(i, isEmpty));
                if (isEmpty) {
                    this.emptyChannelsHelper.setChannelSelected(channelId, this.selectedPosition == i);
                }
                this.activeChannelRowControllers.add(controller2);
            } else if (rowType == 0) {
                ((HomeTopRowView) homeRowViewHolder.homeRow.getView()).setState(this.selectedPosition == i);
            } else {
                onBindViewHolder(holder, position);
            }
        }
        if (list2.contains(PAYLOAD_FAST_SCROLLING)) {
            if (rowType == 1 || rowType == 2 || rowType == 3 || rowType == 4 || rowType == 5) {
                homeRowViewHolder.homeRow.setHomeIsFastScrolling(this.fastScrollingManager.isFastScrollingEnabled());
            }
            if (rowType == 3 || rowType == 4 || rowType == 5) {
                ((ChannelRowController) homeRowViewHolder.homeRow).setState(getChannelState(i, this.dataManager.isChannelEmpty(this.dataManager.getHomeChannel(getChannelIndexForAdapterPosition(i)).getId())));
            }
        }
        if (list2.contains(PAYLOAD_CHANNEL_MOVE_ACTION) && (rowType == 3 || rowType == 4 || rowType == 5)) {
            ((ChannelRowController) homeRowViewHolder.homeRow).bindChannelMoveAction();
        }
        if (list2.contains(PAYLOAD_CHANNEL_LOGO_TITLE) && rowType == 3) {
            ((ChannelRowController) homeRowViewHolder.homeRow).bindChannelLogoTitle();
        }
        if (rowType == 0) {
            HomeTopRowView v = (HomeTopRowView) homeRowViewHolder.homeRow.getView();
            SearchView searchView = v.getSearchWidget();
            if (searchView != null) {
                if (list2.contains(PAYLOAD_IDLE_STATE)) {
                    searchView.setIdleState(this.isIdle);
                }
                if (list2.contains(PAYLOAD_ASSISTANT_ICON)) {
                    searchView.updateAssistantIcon(this.assistantIcon);
                }
                if (list2.contains(PAYLOAD_ASSISTANT_SUGGESTIONS)) {
                    searchView.updateSearchSuggestions(this.assistantSuggestions);
                }
                if (list2.contains(PAYLOAD_MIC_STATUS)) {
                    searchView.updateMicStatus(this.micStatus);
                }
                if (list2.contains(PAYLOAD_HOTWORD_STATUS)) {
                    searchView.updateHotwordEnabled(this.hotwordEnabled);
                }
            }
            if (list2.contains(PAYLOAD_PARTNER_WIDGET_INFO)) {
                v.onPartnerWidgetUpdate(this.partnerWidgetInfo);
            }
            if (list2.contains(PAYLOAD_NOTIF_COUNT_CURSOR)) {
                v.getNotificationsPanelController().updateNotificationsCount(this.notifCountCursor);
            }
            if (list2.contains(PAYLOAD_NOTIF_TRAY_CURSOR)) {
                v.getNotificationsTrayAdapter().changeCursor(this.notifTrayCursor);
                v.updateNotificationsTrayVisibility();
            }
            if (list2.contains(PAYLOAD_INPUT_ICON_VISIBILITY)) {
                v.updateInputIconVisibility(this.inputIconVisible);
            }
        }
    }

    public void onBindViewHolder(HomeRowViewHolder viewHolder, int position) {
        int rowType = getItemViewType(position);
        boolean z = false;
        boolean rowSelected = this.selectedPosition == position;
        if (rowType == 2) {
            WatchNextRowController controller = (WatchNextRowController) viewHolder.homeRow;
            int channelState = getNonEmptyChannelState(position);
            controller.bind(channelState);
            if (channelState == 0) {
                controller.bindItemMetadata();
            }
            controller.setHomeIsFastScrolling(this.fastScrollingManager.isFastScrollingEnabled());
            this.activeWatchNextRowControllers.add(controller);
        } else if (rowType == 1) {
            FavoriteLaunchItemsRowController controller2 = (FavoriteLaunchItemsRowController) viewHolder.homeRow;
            controller2.bind(getNonEmptyChannelState(position));
            controller2.setHomeIsFastScrolling(this.fastScrollingManager.isFastScrollingEnabled());
            if (this.selectFirstAppWhenRowSelected && position == this.selectedPosition) {
                controller2.setSelectedItemPosition(0);
                this.selectFirstAppWhenRowSelected = false;
            }
        } else if (rowType == 6) {
            ConfigureChannelsRowController controller3 = (ConfigureChannelsRowController) viewHolder.homeRow;
            int i = this.state;
            if (getItemViewType(position - 1) == 1) {
                z = true;
            }
            controller3.bind(i, rowSelected, z);
        } else if (rowType == 3 || rowType == 4 || rowType == 5) {
            onBindChannel((ChannelRowController) viewHolder.homeRow, getChannelIndexForAdapterPosition(position), position);
        } else if (rowType == 0) {
            HomeTopRowView v = (HomeTopRowView) viewHolder.homeRow.getView();
            v.getNotificationsTrayAdapter().changeCursor(this.notifTrayCursor);
            v.getNotificationsPanelController().updateNotificationsCount(this.notifCountCursor);
            v.updateNotificationsTrayVisibility();
            v.setState(rowSelected);
            SearchView searchView = v.getSearchWidget();
            searchView.setIdleState(this.isIdle);
            searchView.updateAssistantIcon(this.assistantIcon);
            searchView.updateSearchSuggestions(this.assistantSuggestions);
            searchView.updateMicStatus(this.micStatus);
            searchView.updateHotwordEnabled(this.hotwordEnabled);
            v.onPartnerWidgetUpdate(this.partnerWidgetInfo);
            v.updateInputIconVisibility(this.inputIconVisible);
        }
    }

    private void onBindChannel(ChannelRowController controller, int channelIndex, int position) {
        HomeChannel channel = this.dataManager.getHomeChannel(channelIndex);
        long channelId = channel.getId();
        boolean isEmpty = this.dataManager.isChannelEmpty(channelId);
        int channelState = getChannelState(position, isEmpty);
        boolean z = true;
        controller.bind(channel, channelState, !this.addToWatchNextPackagesBlacklist.contains(channel.getPackageName()), !this.removeProgramPackagesBlacklist.contains(channel.getPackageName()));
        if (channelState == 0) {
            controller.bindItemMetadata();
        }
        if (isEmpty) {
            EmptyChannelsHelper emptyChannelsHelper2 = this.emptyChannelsHelper;
            if (this.selectedPosition != position) {
                z = false;
            }
            emptyChannelsHelper2.setChannelSelected(channelId, z);
        }
        controller.setHomeIsFastScrolling(this.fastScrollingManager.isFastScrollingEnabled());
        this.activeChannelRowControllers.add(controller);
    }

    private int getChannelState(int position, boolean isEmpty) {
        int nonEmptyState = getNonEmptyChannelState(position);
        if (isEmpty) {
            return this.emptyChannelsHelper.getEmptyChannelState(nonEmptyState);
        }
        return nonEmptyState;
    }

    private int getNonEmptyChannelState(int position) {
        int channelViewState;
        boolean channelSelected = this.selectedPosition == position;
        int i = this.state;
        if (i == 0) {
            int channelViewState2 = 6;
            if (this.fastScrollingManager.isFastScrollingEnabled() && hasFastScrollingMode(position)) {
                if (!channelSelected) {
                    channelViewState2 = 7;
                }
                return channelViewState2;
            } else if (channelSelected) {
                return 0;
            } else {
                if (this.selectedPosition == 0 && ((position == 2 || position == 3) && getItemViewType(position) != 6)) {
                    return 4;
                }
                if (this.selectedPosition == 1 && ((position == 2 || position == 3) && getItemViewType(position) != 6)) {
                    return 5;
                }
                if (this.selectedPosition == getItemCount() - 1 && position == getItemCount() - 2) {
                    return 15;
                }
                int i2 = this.selectedPosition;
                if (position == i2 - 1 || (i2 == getItemCount() - 1 && position == getItemCount() - 3 && getItemViewType(position) != 0)) {
                    return 2;
                }
                if (position != this.selectedPosition + 1 || position <= 2) {
                    return 1;
                }
                return 3;
            }
        } else if (i != 1) {
            if (i == 2) {
                if (channelSelected) {
                    channelViewState = 11;
                } else {
                    channelViewState = 12;
                }
                return channelViewState;
            } else if (i != 3) {
                return 1;
            } else {
                return channelSelected ? 13 : 14;
            }
        } else if (channelSelected) {
            return 8;
        } else {
            if (this.selectedPosition != 0 || position <= 0 || position > 4) {
                return 9;
            }
            return 10;
        }
    }

    public void onViewRecycled(HomeRowViewHolder holder) {
        super.onViewRecycled((RecyclerView.ViewHolder) holder);
        if (holder.homeRow instanceof ChannelRowController) {
            ChannelRowController channelRowController = (ChannelRowController) holder.homeRow;
            channelRowController.recycle();
            this.activeChannelRowControllers.remove(channelRowController);
        } else if (holder.homeRow instanceof WatchNextRowController) {
            WatchNextRowController watchNextRowController = (WatchNextRowController) holder.homeRow;
            watchNextRowController.recycle();
            this.activeWatchNextRowControllers.remove(watchNextRowController);
        } else if (holder.homeRow instanceof HomeTopRowView) {
            HomeTopRowView homeTopRowView = (HomeTopRowView) holder.homeRow;
            NotificationsPanelController notificationsPanelController = homeTopRowView.getNotificationsPanelController();
            homeTopRowView.getNotificationsTrayAdapter().changeCursor(null);
        }
    }

    public boolean onFailedToRecycleView(HomeRowViewHolder holder) {
        String valueOf = String.valueOf(holder);
        String valueOf2 = String.valueOf(holder.homeRow);
        StringBuilder sb = new StringBuilder(valueOf.length() + 29 + valueOf2.length());
        sb.append("onFailedToRecycleView: h=");
        sb.append(valueOf);
        sb.append(", r=");
        sb.append(valueOf2);
        Log.w(TAG, sb.toString());
        return super.onFailedToRecycleView((RecyclerView.ViewHolder) holder);
    }

    public void onShowInputs() {
        logClick(TvLauncherConstants.INPUTS_LAUNCH_BUTTON);
        InputsManagerUtil.launchInputsActivity(this.context);
    }

    public void onStartSettings() {
        logClick(TvLauncherConstants.SETTINGS_LAUNCH_BUTTON);
        try {
            this.context.startActivity(new Intent("android.settings.SETTINGS"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Exception starting settings", e);
            Context context2 = this.context;
            Toast.makeText(context2, context2.getString(C1167R.string.app_unavailable), 0).show();
        }
    }

    private void logClick(VisualElementTag visualElementTag) {
        this.eventLogger.log(new ClickEvent(null).setVisualElementTag(visualElementTag));
    }

    public void onLaunchItemsLoaded() {
        notifyItemRangeChanged(getNumberOfRowsAboveChannels(), getItemCount(), PAYLOAD_CHANNEL_LOGO_TITLE);
    }

    public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> arrayList) {
        notifyItemRangeChanged(getNumberOfRowsAboveChannels(), getItemCount(), PAYLOAD_CHANNEL_LOGO_TITLE);
    }

    public void onLaunchItemsRemoved(ArrayList<LaunchItem> arrayList) {
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
    }

    class HomeRowViewHolder extends RecyclerView.ViewHolder implements OnHomeStateChangeListener, OnHomeRowSelectedListener, OnHomeRowRemovedListener, FacetProvider {
        HomeRow homeRow;

        HomeRowViewHolder(HomeRow homeRow2) {
            super(homeRow2.getView());
            this.homeRow = homeRow2;
            homeRow2.setOnHomeStateChangeListener(this);
            homeRow2.setOnHomeRowSelectedListener(this);
            homeRow2.setOnHomeRowRemovedListener(this);
        }

        public void onHomeStateChange(int state) {
            HomeController.this.setState(state);
        }

        public void onHomeRowSelected(HomeRow homeRow2) {
            int newSelection = getAdapterPosition();
            if (newSelection != -1) {
                HomeController.this.setSelectedPosition(newSelection);
            }
        }

        public void onHomeRowRemoved(HomeRow homeRow2) {
            HomeController.this.onItemRemoved(getAdapterPosition());
        }

        public Object getFacet(Class<?> cls) {
            int itemCount = HomeController.this.getItemCount();
            int position = getAdapterPosition();
            if (position == -1) {
                return null;
            }
            int type = getItemViewType();
            if (type == 1) {
                if (HomeController.this.notifTrayCursor == null || HomeController.this.notifTrayCursor.getCount() <= 0) {
                    return null;
                }
                if (HomeController.this.state == 0) {
                    return HomeController.this.appsRowDefaultWithTrayFacet;
                }
                return HomeController.this.appsRowZoomedOutWithTrayFacet;
            } else if (position == itemCount - 1 && type == 6) {
                return HomeController.this.configureChannelsFacet;
            } else {
                if (position == itemCount - 2 && HomeController.this.state != 0 && (type == 3 || type == 2 || type == 4 || type == 5)) {
                    return HomeController.this.secondToLastRowZoomedOutFacet;
                }
                if (position == itemCount - 3 && HomeController.this.state != 0) {
                    return HomeController.this.thirdToLastRowZoomedOutFacet;
                }
                if (type == 4 || type == 5) {
                    return HomeController.this.sponsoredChannelFacet;
                }
                return null;
            }
        }
    }

    public void onHomeTopRowFocusChanged() {
        this.handler.removeCallbacks(this.notifyTopRowStateChangedRunnable);
        if (this.list.isComputingLayout()) {
            Log.w(TAG, "onHomeTopRowFocusChanged: still computing layout => scheduling");
            this.handler.post(this.notifyTopRowStateChangedRunnable);
            return;
        }
        this.notifyTopRowStateChangedRunnable.run();
    }

    private class ChannelEventLogger implements EventLogger {
        /* access modifiers changed from: private */
        public HomeRowViewHolder viewHolder;

        private ChannelEventLogger() {
        }

        public void log(LogEvent event) {
            event.pushParentVisualElementTag(TvLauncherConstants.CHANNEL_ROW);
            event.setVisualElementRowIndex(HomeController.this.getChannelIndexForAdapterPosition(this.viewHolder.getAdapterPosition()));
            HomeController.this.eventLogger.log(event);
        }
    }
}
