package com.google.android.tvlauncher.appsview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.p001v4.content.ContextCompat;
import android.support.p004v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.leanback.widget.FacetProvider;
import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import com.google.android.tvlauncher.util.KeylineUtil;
import com.google.logs.tvlauncher.config.TvLauncherConstants;
import java.util.ArrayList;
import java.util.Iterator;

class EditModeGridAdapter extends RecyclerView.Adapter<LaunchItemViewHolder> implements LaunchItemsManager.AppsViewChangeListener {
    /* access modifiers changed from: private */
    public final int bannerHeight;
    private final int bannerMarginBottom;
    private final int bannerMarginEnd;
    /* access modifiers changed from: private */
    public final int bannerWidth;
    /* access modifiers changed from: private */
    public int bottomKeyline;
    private final EventLogger eventLogger;
    private final int keylineAppsRowTwo;
    /* access modifiers changed from: private */
    public final int keylineAppsRowTwoTitleAbove;
    /* access modifiers changed from: private */
    public final int keylineLastRow;
    private final ArrayList<LaunchItem> launchItems = new ArrayList<>();
    private OnEditItemRemovedListener onEditItemRemovedListener;
    /* access modifiers changed from: private */
    public OnShowAccessibilityMenuListener onShowAccessibilityMenuListener;
    /* access modifiers changed from: private */
    public final Drawable placeholderBanner;
    /* access modifiers changed from: private */
    public int topKeyline;

    interface OnEditItemRemovedListener {
        void onEditItemRemoved(int i);
    }

    EditModeGridAdapter(Context context, EventLogger eventLogger2) {
        Resources res = context.getResources();
        this.keylineAppsRowTwo = res.getDimensionPixelSize(C1167R.dimen.app_view_grid_keyline_app_row_two);
        this.keylineAppsRowTwoTitleAbove = res.getDimensionPixelSize(C1167R.dimen.app_view_grid_keyline_app_row_two_title_above);
        this.keylineLastRow = res.getDimensionPixelSize(C1167R.dimen.app_view_grid_keyline_last_row);
        this.bannerMarginEnd = res.getDimensionPixelSize(C1167R.dimen.app_banner_margin_end);
        this.bannerMarginBottom = res.getDimensionPixelSize(C1167R.dimen.app_row_view_margin_bottom);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.bannerWidth = res.getDimensionPixelOffset(C1167R.dimen.app_banner_image_max_width);
        this.bannerHeight = res.getDimensionPixelOffset(C1167R.dimen.app_banner_image_max_height);
        this.eventLogger = eventLogger2;
    }

    public void onLaunchItemsLoaded() {
    }

    public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> addedOrUpdatedItems) {
        Iterator<LaunchItem> it = addedOrUpdatedItems.iterator();
        while (it.hasNext()) {
            LaunchItem item = it.next();
            int index = this.launchItems.indexOf(item);
            if (index != -1) {
                notifyItemChanged(index);
            } else if (this.launchItems.size() <= 0 || this.launchItems.get(0).isGame() == item.isGame()) {
                this.launchItems.add(item);
                notifyItemInserted(this.launchItems.size() - 1);
            }
        }
    }

    public void onLaunchItemsRemoved(ArrayList<LaunchItem> removedItems) {
        Iterator<LaunchItem> it = removedItems.iterator();
        while (it.hasNext()) {
            int index = this.launchItems.indexOf(it.next());
            if (index != -1) {
                this.launchItems.remove(index);
                this.onEditItemRemovedListener.onEditItemRemoved(index);
                notifyItemRemoved(index);
            }
        }
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
    }

    final class LaunchItemViewHolder extends RecyclerView.ViewHolder implements FacetProvider, View.OnClickListener, View.OnFocusChangeListener {
        private BannerView bannerView;

        LaunchItemViewHolder(View itemView) {
            super(itemView);
            this.bannerView = (BannerView) itemView;
            this.bannerView.setOnClickListener(this);
            this.bannerView.setOnFocusChangeListener(this);
            this.bannerView.setSelected(false);
        }

        public void set(LaunchItem launchItem) {
            this.bannerView.setLaunchItem(launchItem);
            new LaunchItemImageLoader(this.itemView.getContext()).setLaunchItemImageDataSource(new LaunchItemImageDataSource(launchItem, PackageImageDataSource.ImageType.BANNER, LaunchItemsManagerProvider.getInstance(this.itemView.getContext()).getCurrentLocale())).setTargetImageView(this.bannerView.getBannerImage()).setPlaceholder(EditModeGridAdapter.this.placeholderBanner).setWidth(EditModeGridAdapter.this.bannerWidth).setHeight(EditModeGridAdapter.this.bannerHeight).loadLaunchItemImage();
        }

        public Object getFacet(Class<?> cls) {
            if (getAdapterPosition() == -1) {
                return null;
            }
            return KeylineUtil.createItemAlignmentFacet(-calculateOffset());
        }

        private int calculateOffset() {
            Pair<Integer, Integer> rowColIndex = LaunchItemsHolder.getRowColIndexFromListIndex(getAdapterPosition());
            if (rowColIndex == null) {
                return EditModeGridAdapter.this.topKeyline;
            }
            int rowPosition = ((Integer) rowColIndex.first).intValue();
            int totalRows = LaunchItemsHolder.getRowCount(EditModeGridAdapter.this.getItemCount());
            if (EditModeGridAdapter.this.keylineLastRow == EditModeGridAdapter.this.bottomKeyline && rowPosition == totalRows - 2 && totalRows >= 3) {
                return EditModeGridAdapter.this.keylineAppsRowTwoTitleAbove;
            }
            if (rowPosition == totalRows - 1) {
                return EditModeGridAdapter.this.bottomKeyline;
            }
            return EditModeGridAdapter.this.topKeyline;
        }

        public void onClick(View v) {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                EditModeGridAdapter.this.onShowAccessibilityMenuListener.onShowAccessibilityMenu(true);
            }
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (v instanceof BannerView) {
                ((BannerView) v).setIsBeingEdited(hasFocus);
            }
        }

        /* access modifiers changed from: package-private */
        public BannerView getBannerView() {
            return this.bannerView;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public LaunchItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BannerView bannerView = (BannerView) LayoutInflater.from(parent.getContext()).inflate(C1167R.layout.view_app_banner, parent, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(bannerView.getLayoutParams());
        params.bottomMargin = this.bannerMarginBottom;
        params.setMarginEnd(this.bannerMarginEnd);
        bannerView.setLayoutParams(params);
        return new LaunchItemViewHolder(bannerView);
    }

    public void onBindViewHolder(LaunchItemViewHolder holder, int position) {
        holder.set(this.launchItems.get(position));
    }

    public int getItemCount() {
        return this.launchItems.size();
    }

    /* access modifiers changed from: package-private */
    public void setTopKeyline(int topKeyline2) {
        this.topKeyline = topKeyline2;
    }

    /* access modifiers changed from: package-private */
    public void setBottomKeyline(int bottomKeyline2) {
        this.bottomKeyline = bottomKeyline2;
    }

    /* access modifiers changed from: package-private */
    public void setLaunchItems(ArrayList<LaunchItem> items) {
        this.launchItems.clear();
        this.launchItems.addAll(items);
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public void moveLaunchItems(int from, int to, int direction) {
        TvlauncherLogEnum.TvLauncherEventCode eventCode;
        VisualElementTag visualElementTag;
        if (from >= 0) {
            int offset = 1;
            if (from <= this.launchItems.size() - 1 && to >= 0 && to <= this.launchItems.size() - 1) {
                LaunchItem fromItem = this.launchItems.get(from);
                this.launchItems.set(from, this.launchItems.get(to));
                this.launchItems.set(to, fromItem);
                notifyItemMoved(from, to);
                int positionDifference = to - from;
                if (Math.abs(positionDifference) > 1) {
                    if (positionDifference > 0) {
                        offset = -1;
                    }
                    notifyItemMoved(to + offset, from);
                }
                if (direction == 17) {
                    eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_LEFT;
                } else if (direction == 33) {
                    eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_UP;
                } else if (direction == 66) {
                    eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_RIGHT;
                } else if (direction == 130) {
                    eventCode = TvlauncherLogEnum.TvLauncherEventCode.MOVE_LAUNCH_ITEM_DOWN;
                } else {
                    StringBuilder sb = new StringBuilder(30);
                    sb.append("Invalid direction: ");
                    sb.append(direction);
                    throw new IllegalArgumentException(sb.toString());
                }
                LogEvent logEvent = new LogEvent(eventCode).setVisualElementTag(TvLauncherConstants.LAUNCH_ITEM).setVisualElementRowIndex(to / 4).setVisualElementIndex(to % 4);
                logEvent.getApplication().setPackageName(fromItem.getPackageName());
                if (fromItem.isGame()) {
                    visualElementTag = TvLauncherConstants.GAMES_CONTAINER;
                } else {
                    visualElementTag = TvLauncherConstants.APPS_CONTAINER;
                }
                logEvent.pushParentVisualElementTag(visualElementTag);
                this.eventLogger.log(logEvent);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<LaunchItem> getLaunchItems() {
        return this.launchItems;
    }

    /* access modifiers changed from: package-private */
    public void setOnShowAccessibilityMenuListener(OnShowAccessibilityMenuListener listener) {
        this.onShowAccessibilityMenuListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setOnEditItemRemovedListener(OnEditItemRemovedListener listener) {
        this.onEditItemRemovedListener = listener;
    }
}
