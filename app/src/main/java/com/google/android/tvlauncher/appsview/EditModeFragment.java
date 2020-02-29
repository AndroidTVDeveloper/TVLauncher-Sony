package com.google.android.tvlauncher.appsview;

import android.app.Fragment;
import android.os.Bundle;
import android.support.p004v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.FragmentEventLogger;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvlauncher.util.Util;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;
import java.util.ArrayList;

public class EditModeFragment extends Fragment {
    public static final int EDIT_TYPE_APPS = 0;
    public static final int EDIT_TYPE_GAMES = 1;
    private static final String KEY_BOTTOM_KEYLINE = "key_bottom_keyline";
    public static final String KEY_EDIT_MODE_FOCUSED_POSITION = "key_edit_mode_focused_position";
    public static final String KEY_EDIT_MODE_TYPE = "key_edit_mode_type";
    private static final String KEY_TOP_KEYLINE = "key_top_keyline";
    private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            EditModeFragment.this.gridView.updateAccessibilityContextMenuIfNeeded();
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            EditModeFragment.this.gridView.updateAccessibilityContextMenuIfNeeded();
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            EditModeFragment.this.gridView.updateAccessibilityContextMenuIfNeeded();
        }
    };
    private EditModeGridAdapter editAdapter;
    private View editModeView;
    private int editType;
    private final FragmentEventLogger eventLogger = new FragmentEventLogger(this);
    private int focusPosition;
    /* access modifiers changed from: private */
    public EditModeGridView gridView;
    private final LaunchItemsManager launchItemsManager = LaunchItemsManagerProvider.getInstance(getContext());
    private OemConfiguration.LayoutOrderOptions layoutOrderOptions = OemConfiguration.get(getContext()).getAppsViewLayoutOption();
    /* access modifiers changed from: private */
    public OnShowAccessibilityMenuListener onShowAccessibilityMenuListener;

    public static EditModeFragment newInstance(int editModeType, int focusedAppPosition, int topKeyline, int bottomKeyline) {
        Bundle args = new Bundle();
        args.putInt(KEY_EDIT_MODE_TYPE, editModeType);
        args.putInt(KEY_EDIT_MODE_FOCUSED_POSITION, focusedAppPosition);
        args.putInt(KEY_TOP_KEYLINE, topKeyline);
        args.putInt(KEY_BOTTOM_KEYLINE, bottomKeyline);
        EditModeFragment fragment = new EditModeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public EditModeFragment() {
        if (this.layoutOrderOptions == null) {
            this.layoutOrderOptions = OemConfiguration.LayoutOrderOptions.APPS_OEM_GAMES;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.editType = args.getInt(KEY_EDIT_MODE_TYPE);
        this.focusPosition = args.getInt(KEY_EDIT_MODE_FOCUSED_POSITION);
        this.editAdapter = new EditModeGridAdapter(getContext(), this.eventLogger);
        this.editAdapter.registerAdapterDataObserver(this.adapterDataObserver);
        this.launchItemsManager.refreshLaunchItems();
        this.launchItemsManager.registerAppsViewChangeListener(this.editAdapter);
        this.editAdapter.setTopKeyline(args.getInt(KEY_TOP_KEYLINE));
        this.editAdapter.setBottomKeyline(args.getInt(KEY_BOTTOM_KEYLINE));
        this.onShowAccessibilityMenuListener = new EditModeFragment$$Lambda$0(this);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onCreate$0$EditModeFragment(boolean show) {
        if (!Util.isAccessibilityEnabled(getContext()) || !show) {
            this.gridView.hideAccessibilityMenu();
        } else {
            this.gridView.showAccessibilityMenu();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.editModeView = inflater.inflate(C1167R.layout.edit_mode_view, container, false);
        return this.editModeView;
    }

    /* renamed from: com.google.android.tvlauncher.appsview.EditModeFragment$3 */
    static /* synthetic */ class C11953 {

        /* renamed from: $SwitchMap$com$google$android$tvlauncher$util$OemConfiguration$LayoutOrderOptions */
        static final /* synthetic */ int[] f144x70c2e182 = new int[OemConfiguration.LayoutOrderOptions.values().length];

        static {
            try {
                f144x70c2e182[OemConfiguration.LayoutOrderOptions.APPS_OEM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f144x70c2e182[OemConfiguration.LayoutOrderOptions.APPS_OEM_GAMES.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f144x70c2e182[OemConfiguration.LayoutOrderOptions.APPS_GAMES_OEM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f144x70c2e182[OemConfiguration.LayoutOrderOptions.GAMES_APPS_OEM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.gridView = (EditModeGridView) this.editModeView.findViewById(C1167R.C1170id.edit_mode_grid);
        int i = C11953.f144x70c2e182[this.layoutOrderOptions.ordinal()];
        if (i == 1) {
            this.editAdapter.setLaunchItems(this.launchItemsManager.getAllLaunchItemsWithSorting());
        } else if (i == 2 || i == 3 || i == 4) {
            int i2 = this.editType;
            if (i2 == 0) {
                this.editAdapter.setLaunchItems(this.launchItemsManager.getAppLaunchItems());
            } else if (i2 == 1) {
                this.editAdapter.setLaunchItems(this.launchItemsManager.getGameLaunchItems());
            }
        }
        this.editAdapter.setOnShowAccessibilityMenuListener(this.onShowAccessibilityMenuListener);
        this.editAdapter.setOnEditItemRemovedListener(new EditModeFragment$$Lambda$1(this));
        if (this.editAdapter.getItemCount() <= 0) {
            getFragmentManager().popBackStack();
        }
        this.gridView.setNumColumns(4);
        this.gridView.setAdapter(this.editAdapter);
        this.gridView.setSelectedPosition(this.focusPosition);
        this.gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                View focusedChild = EditModeFragment.this.gridView.getFocusedChild();
                if (focusedChild instanceof BannerView) {
                    focusedChild.setSelected(true);
                    EditModeFragment.this.onShowAccessibilityMenuListener.onShowAccessibilityMenu(true);
                    EditModeFragment.this.gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        this.gridView.setWindowAlignment(0);
        this.gridView.setWindowAlignmentOffsetPercent(-1.0f);
        this.gridView.requestFocus();
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onViewCreated$1$EditModeFragment(int position) {
        EditModeGridView editModeGridView = this.gridView;
        if (editModeGridView != null && editModeGridView.getSelectedPosition() == position && !getFragmentManager().isStateSaved()) {
            getFragmentManager().popBackStack();
        }
    }

    public void onResume() {
        super.onResume();
        this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.ENTER_EDIT_APPS_MODE));
    }

    public void onPause() {
        int selectedPosition = this.gridView.getSelectedPosition();
        ArrayList<LaunchItem> launchItems = this.editAdapter.getLaunchItems();
        LaunchItemsManager launchItemsManager2 = this.launchItemsManager;
        boolean z = true;
        if (this.editType != 1) {
            z = false;
        }
        launchItemsManager2.onAppOrderChange(launchItems, z, LaunchItemsHolder.getRowColIndexFromListIndex(selectedPosition));
        this.onShowAccessibilityMenuListener.onShowAccessibilityMenu(false);
        super.onPause();
        UserActionEvent event = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.EXIT_EDIT_APPS_MODE);
        TvlauncherClientLog.LaunchItemCollection.Builder launchItemCollection = event.getLaunchItemCollection();
        if (this.editType == 0) {
            launchItemCollection.setCount(launchItems.size());
        } else {
            launchItemCollection.setGameCount(launchItems.size());
        }
        int size = launchItems.size();
        for (int i = 0; i < size; i++) {
            LaunchItem launchItem = launchItems.get(i);
            TvlauncherClientLog.LaunchItem.Builder item = TvlauncherClientLog.LaunchItem.newBuilder();
            if (launchItem.isAppLink()) {
                TvlauncherClientLog.AppLink.Builder appLink = TvlauncherClientLog.AppLink.newBuilder();
                appLink.setPackageName(launchItem.getPackageName());
                if (launchItem.getDataUri() != null) {
                    appLink.setUri(launchItem.getDataUri());
                }
                item.setAppLink(appLink);
            } else {
                TvlauncherClientLog.Application.Builder app = TvlauncherClientLog.Application.newBuilder();
                app.setPackageName(launchItem.getPackageName());
                app.setIsGame(launchItem.isGame());
                item.setApp(app);
            }
            launchItemCollection.addItems(item);
        }
        this.eventLogger.log(event);
    }

    public void onDestroy() {
        super.onDestroy();
        this.editAdapter.unregisterAdapterDataObserver(this.adapterDataObserver);
        this.launchItemsManager.unregisterAppsViewChangeListener(this.editAdapter);
    }
}
