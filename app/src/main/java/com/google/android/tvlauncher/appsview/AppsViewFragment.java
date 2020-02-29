package com.google.android.tvlauncher.appsview;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import androidx.leanback.widget.VerticalGridView;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.FragmentEventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LogEventParameters;
import com.google.android.tvlauncher.analytics.LogUtils;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.util.LaunchUtil;
import com.google.android.tvlauncher.util.OemAppPromotions;
import com.google.android.tvrecommendations.shared.util.Constants;

public class AppsViewFragment extends Fragment {
    private static final String TAG = "AppsViewFragment";
    private View appsView;
    private boolean appsViewOnStopped = true;
    /* access modifiers changed from: private */
    public final FragmentEventLogger eventLogger = new FragmentEventLogger(this);
    /* access modifiers changed from: private */
    public OnForceFinishAnimationListener forceFinishListener;
    /* access modifiers changed from: private */
    public VerticalGridView gridView;
    /* access modifiers changed from: private */
    public LaunchItemsManager launchItemsManager;
    private OnAppsViewActionListener onAppsViewActionListener;
    private final OnEditModeOrderChangeCallback onEditModeOrderChangeCallback = new OnEditModeOrderChangeCallback();
    /* access modifiers changed from: private */
    public RowListAdapter rowListAdapter;
    private final RecyclerView.AdapterDataObserver rowListAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        public void onChanged() {
            super.onChanged();
            AppsViewFragment.this.updateVerticalGridViewLayout();
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            AppsViewFragment.this.updateVerticalGridViewLayout();
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            AppsViewFragment.this.updateVerticalGridViewLayout();
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            AppsViewFragment.this.updateVerticalGridViewLayout();
        }
    };
    /* access modifiers changed from: private */
    public boolean scrollToTopWhenResume = true;

    interface OnForceFinishAnimationListener {
        void onForceFinishAnimation(boolean z);
    }

    class OnEditModeOrderChangeCallback {
        OnEditModeOrderChangeCallback() {
        }

        /* access modifiers changed from: package-private */
        public void onEditModeExited(int rowIndex, final int colIndex) {
            AppsViewFragment.this.gridView.scrollToPosition(rowIndex);
            AppsViewFragment.this.gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    View row = AppsViewFragment.this.gridView.getFocusedChild();
                    if (row instanceof LaunchItemsRowView) {
                        ((LaunchItemsRowView) row).setOneTimeFocusPosition(colIndex);
                    }
                    AppsViewFragment.this.gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.launchItemsManager = LaunchItemsManagerProvider.getInstance(getContext());
        this.launchItemsManager.refreshLaunchItems();
        this.rowListAdapter = new RowListAdapter(getContext(), this.eventLogger, this.launchItemsManager);
        this.rowListAdapter.registerAdapterDataObserver(this.rowListAdapterDataObserver);
        this.launchItemsManager.registerAppsViewChangeListener(this.rowListAdapter);
        this.onAppsViewActionListener = createOnShowAppsViewListener();
    }

    public void onResume() {
        this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_APPS_VIEW).expectParameters(LogEventParameters.APP_COUNT));
        if (this.gridView != null && this.scrollToTopWhenResume && this.appsViewOnStopped) {
            int i = 0;
            while (true) {
                if (i >= this.rowListAdapter.getItemCount()) {
                    break;
                } else if (this.rowListAdapter.getItemViewType(i) != 5) {
                    this.gridView.scrollToPosition(i);
                    break;
                } else {
                    i++;
                }
            }
        }
        this.rowListAdapter.setResetViewHolderPositions(this.scrollToTopWhenResume);
        this.scrollToTopWhenResume = true;
        this.appsViewOnStopped = false;
        OemAppPromotions oemAppPromotions = OemAppPromotions.get(getContext());
        oemAppPromotions.registerOnAppPromotionsLoadedListener(this.rowListAdapter);
        oemAppPromotions.readAppPromotions(true);
        this.launchItemsManager.registerAppsViewChangeListener(oemAppPromotions);
        super.onResume();
    }

    public void onPause() {
        OemAppPromotions oemAppPromotions = OemAppPromotions.get(getContext());
        oemAppPromotions.unregisterOnAppPromotionsLoadedListener(this.rowListAdapter);
        this.launchItemsManager.unregisterAppsViewChangeListener(oemAppPromotions);
        super.onPause();
    }

    public void onStop() {
        this.appsViewOnStopped = true;
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        this.launchItemsManager.unregisterAppsViewChangeListener(this.rowListAdapter);
        this.rowListAdapter.unregisterAdapterDataObserver(this.rowListAdapterDataObserver);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.appsView = inflater.inflate(C1167R.layout.apps_view_fragment, container, false);
        return this.appsView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.gridView = (VerticalGridView) this.appsView.findViewById(C1167R.C1170id.row_list_view);
        this.rowListAdapter.setDataInRows();
        this.rowListAdapter.setOnAppsViewActionListener(this.onAppsViewActionListener);
        this.rowListAdapter.setOnEditModeOrderChangeCallback(this.onEditModeOrderChangeCallback);
        this.rowListAdapter.initRows();
        this.gridView.setAdapter(this.rowListAdapter);
        this.appsView.requestFocus();
    }

    /* access modifiers changed from: package-private */
    public void setOnForceFinishAnimationListener(OnForceFinishAnimationListener listener) {
        this.forceFinishListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void updateVerticalGridViewLayout() {
        if (this.gridView != null && this.rowListAdapter.getItemCount() != 0) {
            if (this.rowListAdapter.getItemViewType(0) == 3) {
                this.gridView.setWindowAlignment(0);
                this.gridView.setWindowAlignmentOffsetPercent(-1.0f);
                return;
            }
            int paddingTop = this.gridView.getContext().getResources().getDimensionPixelSize(C1167R.dimen.apps_view_padding_top);
            this.gridView.setWindowAlignmentOffset(paddingTop);
            this.gridView.setWindowAlignmentOffsetPercent(-1.0f);
            this.gridView.setWindowAlignment(1);
            VerticalGridView verticalGridView = this.gridView;
            verticalGridView.setPadding(verticalGridView.getPaddingLeft(), paddingTop, this.gridView.getPaddingRight(), this.gridView.getPaddingBottom());
        }
    }

    public void startEditMode(int editModeType) {
        OnAppsViewActionListener onAppsViewActionListener2 = this.onAppsViewActionListener;
        if (onAppsViewActionListener2 != null) {
            onAppsViewActionListener2.onShowEditModeView(editModeType, 0);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && data.getBooleanExtra("extra_launched_virtual_app", false)) {
            this.forceFinishListener.onForceFinishAnimation(true);
        }
    }

    private OnAppsViewActionListener createOnShowAppsViewListener() {
        return new OnAppsViewActionListener() {
            public void onShowEditModeView(int editModeType, int focusedAppPosition) {
                AppsViewFragment.this.getFragmentManager().beginTransaction().replace(16908290, EditModeFragment.newInstance(editModeType, focusedAppPosition, AppsViewFragment.this.rowListAdapter.getTopKeylineForEditMode(editModeType), AppsViewFragment.this.rowListAdapter.getBottomKeylineForEditMode(editModeType)), "edit_mode_fragment").addToBackStack(null).commit();
            }

            public void onShowAppInfo(String packageName) {
                boolean unused = AppsViewFragment.this.scrollToTopWhenResume = false;
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                String valueOf = String.valueOf(packageName);
                intent.setData(Uri.parse(valueOf.length() != 0 ? "package:".concat(valueOf) : "package:"));
                AppsViewFragment.this.startActivity(intent);
            }

            public void onUninstallApp(String packageName) {
                boolean unused = AppsViewFragment.this.scrollToTopWhenResume = false;
                if (AppsViewFragment.this.launchItemsManager.getLaunchItem(packageName).isAppLink()) {
                    Intent intent = new Intent(Constants.ACTION_REMOVE_APP_LINK);
                    intent.putExtra(Constants.EXTRA_APP_LINK_ID, packageName);
                    AppsViewFragment.this.startActivityForResult(intent, 0);
                } else {
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.UNINSTALL_PACKAGE");
                    String valueOf = String.valueOf(packageName);
                    intent2.setData(Uri.parse(valueOf.length() != 0 ? "package:".concat(valueOf) : "package:"));
                    AppsViewFragment.this.startActivity(intent2);
                }
                AppsViewFragment.this.forceFinishListener.onForceFinishAnimation(false);
            }

            public void onLaunchApp(Intent intent, View view) {
                try {
                    boolean unused = AppsViewFragment.this.scrollToTopWhenResume = false;
                    if (Constants.ACTION_ADD_APP_LINK.equals(intent.getAction())) {
                        AppsViewFragment.this.startActivityForResult(intent, 0);
                        return;
                    }
                    LaunchUtil.startActivityWithAnimation(intent, view);
                    AppsViewFragment.this.forceFinishListener.onForceFinishAnimation(false);
                } catch (ActivityNotFoundException | SecurityException e) {
                    Toast.makeText(AppsViewFragment.this.getContext(), C1167R.string.failed_launch, 0).show();
                    String valueOf = String.valueOf(e);
                    StringBuilder sb = new StringBuilder(valueOf.length() + 24);
                    sb.append("Cannot start activity : ");
                    sb.append(valueOf);
                    Log.e(AppsViewFragment.TAG, sb.toString());
                }
            }

            public void onToggleFavorite(LaunchItem item) {
                if (AppsViewFragment.this.launchItemsManager.isFavorite(item)) {
                    AppsViewFragment.this.launchItemsManager.removeFromFavorites(item);
                } else {
                    AppsViewFragment.this.launchItemsManager.addToFavorites(item);
                }
            }

            public void onStoreLaunch(Intent intent, VisualElementTag visualElementTag, View view) {
                try {
                    String packageName = LogUtils.getPackage(intent);
                    LogEvent event = new ClickEvent(TvlauncherLogEnum.TvLauncherEventCode.START_APP).setVisualElementTag(visualElementTag);
                    if (packageName != null) {
                        event.getApplication().setPackageName(packageName);
                    }
                    AppsViewFragment.this.eventLogger.log(event);
                    boolean unused = AppsViewFragment.this.scrollToTopWhenResume = false;
                    LaunchUtil.startActivityWithAnimation(intent, view);
                    AppsViewFragment.this.forceFinishListener.onForceFinishAnimation(false);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(AppsViewFragment.this.getContext(), C1167R.string.failed_launch, 0).show();
                    String str = LogUtils.getPackage(intent);
                    String valueOf = String.valueOf(e);
                    StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 44 + valueOf.length());
                    sb.append("Cannot start store with package: ");
                    sb.append(str);
                    sb.append(", due to : ");
                    sb.append(valueOf);
                    Log.e(AppsViewFragment.TAG, sb.toString());
                }
            }
        };
    }
}
