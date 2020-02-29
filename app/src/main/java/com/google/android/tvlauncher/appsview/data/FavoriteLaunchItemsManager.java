package com.google.android.tvlauncher.appsview.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Pair;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.settings.FavoriteLaunchItemsActivity;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.OemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FavoriteLaunchItemsManager implements LaunchItemsManager.AppsViewChangeListener {
    private static final int MAX_HOME_SCREEN_APP_ITEMS = 100;
    private static final int MAX_HOME_SCREEN_OOB_ORDER_APP_ITEMS = 10;
    private static final String MORE_FAVORITES_PKGNAME = "com.google.android.tvlauncher.appsview.FavoriteItemsManager.MORE_FAVORITES_PKGNAME";
    private static final String PREF_FILE_NAME = "com.google.android.tvlauncher.appsview.FavoriteItemsManager.PREFERENCE_KEY";
    private static final String USER_CUSTOMIZATION_KEY = "com.google.android.tvlauncher.appsview.FavoriteItemsManager.USER_CUSTOMIZATION_KEY";
    private final Context context;
    private boolean customizedByUser;
    private final Map<String, Integer> defaultItemsOrder = new HashMap();
    /* access modifiers changed from: private */
    public final Map<LaunchItem, Integer> favoriteItemsToDesiredPosition = new HashMap();
    private LaunchItemsManager.HomeScreenItemsChangeListener homeScreenItemsChangeListener;
    private final LaunchItem moreFavoritesItem;
    private final SharedPreferences packageNamePrefs;
    private Map<String, Integer> pinnedAppPackagesToPosition;
    private LaunchItem[] pinnedItems;
    private final SharedPreferences userCustomizationPref;

    private class LaunchItemUserOrderComparator implements Comparator<LaunchItem> {
        private LaunchItemUserOrderComparator() {
        }

        public int compare(LaunchItem o1, LaunchItem o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            Integer o1Index = (Integer) FavoriteLaunchItemsManager.this.favoriteItemsToDesiredPosition.get(o1);
            Integer o2Index = (Integer) FavoriteLaunchItemsManager.this.favoriteItemsToDesiredPosition.get(o2);
            Integer o1Index2 = Integer.valueOf(o1Index == null ? FavoriteLaunchItemsManager.this.favoriteItemsToDesiredPosition.keySet().size() : o1Index.intValue());
            Integer o2Index2 = Integer.valueOf(o2Index == null ? FavoriteLaunchItemsManager.this.favoriteItemsToDesiredPosition.keySet().size() : o2Index.intValue());
            if (o1Index2.intValue() < o2Index2.intValue()) {
                return -1;
            }
            if (o1Index2.intValue() > o2Index2.intValue()) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

    FavoriteLaunchItemsManager(Context context2) {
        this.packageNamePrefs = context2.getSharedPreferences(PREF_FILE_NAME, 0);
        this.userCustomizationPref = context2.getSharedPreferences(USER_CUSTOMIZATION_KEY, 0);
        this.context = context2;
        this.moreFavoritesItem = createMoreFavoritesLaunchItem(this.context.getString(C1167R.string.favorite_more_apps), new Intent(this.context, FavoriteLaunchItemsActivity.class));
        this.customizedByUser = this.userCustomizationPref.getBoolean(USER_CUSTOMIZATION_KEY, false);
    }

    /* access modifiers changed from: package-private */
    public void init(OemConfiguration oemConfiguration) {
        List<String> oemOrder;
        if (oemConfiguration.isDataLoaded()) {
            if (!this.customizedByUser && (oemOrder = oemConfiguration.getOutOfBoxFavoriteAppsList()) != null && !oemOrder.isEmpty()) {
                int i = 0;
                while (i < oemOrder.size() && i < 10) {
                    this.defaultItemsOrder.put(oemOrder.get(i), Integer.valueOf(i));
                    i++;
                }
                if (this.packageNamePrefs.getAll().isEmpty()) {
                    initializeDefaultOrderFavorites();
                }
            }
            if (OemUtils.isOperatorTierDevice(this.context)) {
                this.pinnedAppPackagesToPosition = new HashMap();
                List<String> pinnedApps = oemConfiguration.getPinnedFavoriteApps();
                for (int i2 = 0; i2 < pinnedApps.size(); i2++) {
                    this.pinnedAppPackagesToPosition.put(pinnedApps.get(i2), Integer.valueOf(i2));
                }
                this.pinnedItems = new LaunchItem[this.pinnedAppPackagesToPosition.size()];
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void userAddToFavorites(LaunchItem item) {
        if (!this.favoriteItemsToDesiredPosition.containsKey(item) && this.favoriteItemsToDesiredPosition.size() < 100) {
            onCustomizedByUser();
            this.favoriteItemsToDesiredPosition.put(item, Integer.valueOf(this.favoriteItemsToDesiredPosition.keySet().size()));
            List<LaunchItem> items = getSortedFavorites();
            saveOrderSnapshot(items);
            notifyOnHomeScreenItemsChanged(items);
        }
    }

    /* access modifiers changed from: package-private */
    public void userRemoveFromFavorites(LaunchItem item) {
        onCustomizedByUser();
        removeFavorite(item);
    }

    private int removeFavorite(LaunchItem item) {
        Integer found = this.favoriteItemsToDesiredPosition.remove(item);
        if (found != null) {
            List<LaunchItem> items = getSortedFavorites();
            saveOrderSnapshotIfCustomizedByUser(items);
            notifyOnHomeScreenItemsChanged(items);
        }
        if (found == null) {
            return -1;
        }
        return found.intValue();
    }

    /* access modifiers changed from: package-private */
    public List<LaunchItem> getFavoriteItems() {
        List<LaunchItem> items = getSortedFavorites();
        if (items.size() < 100 && !items.contains(this.moreFavoritesItem)) {
            items.add(this.moreFavoritesItem);
        }
        LaunchItem[] launchItemArr = this.pinnedItems;
        if (launchItemArr != null) {
            for (int i = launchItemArr.length - 1; i >= 0; i--) {
                LaunchItem[] launchItemArr2 = this.pinnedItems;
                if (launchItemArr2[i] != null) {
                    items.add(0, launchItemArr2[i]);
                }
            }
        }
        return items;
    }

    /* access modifiers changed from: package-private */
    public boolean isFavorite(LaunchItem item) {
        return this.favoriteItemsToDesiredPosition.containsKey(item) || isPinnedFavorite(item);
    }

    /* access modifiers changed from: package-private */
    public boolean isPinnedFavorite(LaunchItem item) {
        Map<String, Integer> map = this.pinnedAppPackagesToPosition;
        if (map != null) {
            return map.containsKey(item.getPackageName());
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isOnlyFavorite(LaunchItem item) {
        return this.favoriteItemsToDesiredPosition.size() == 1 && this.favoriteItemsToDesiredPosition.containsKey(item);
    }

    /* access modifiers changed from: package-private */
    public void setHomeScreenItemsChangeListener(LaunchItemsManager.HomeScreenItemsChangeListener listener) {
        this.homeScreenItemsChangeListener = listener;
    }

    public void onLaunchItemsLoaded() {
        addAppsToFavoritesFromSharedPreferences();
        notifyOnHomeScreenItemsLoaded();
    }

    public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> addedOrUpdatedItems) {
        boolean changed = false;
        Iterator<LaunchItem> it = addedOrUpdatedItems.iterator();
        while (it.hasNext()) {
            LaunchItem updatedItem = it.next();
            if (this.favoriteItemsToDesiredPosition.containsKey(updatedItem)) {
                Map<LaunchItem, Integer> map = this.favoriteItemsToDesiredPosition;
                map.put(updatedItem, map.get(updatedItem));
                changed = true;
            } else if (!this.customizedByUser && this.defaultItemsOrder.containsKey(updatedItem.getPackageName())) {
                this.favoriteItemsToDesiredPosition.put(updatedItem, this.defaultItemsOrder.get(updatedItem.getPackageName()));
                changed = true;
            }
            if (this.pinnedAppPackagesToPosition != null) {
                changed |= addToPinnedApps(updatedItem);
            }
        }
        if (changed) {
            saveOrderSnapshotIfCustomizedByUser(getSortedFavorites());
            notifyOnHomeScreenItemsChanged(getFavoriteItems());
        }
    }

    public void onLaunchItemsRemoved(ArrayList<LaunchItem> removedItems) {
        Integer index;
        boolean changed = false;
        Iterator<LaunchItem> it = removedItems.iterator();
        while (it.hasNext()) {
            LaunchItem removedItem = it.next();
            changed |= removeFavorite(removedItem) == -1;
            Map<String, Integer> map = this.pinnedAppPackagesToPosition;
            if (!(map == null || (index = map.get(removedItem.getPackageName())) == null)) {
                this.pinnedItems[index.intValue()] = null;
                changed = true;
            }
        }
        if (changed) {
            notifyOnHomeScreenItemsChanged(getFavoriteItems());
        }
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
    }

    /* access modifiers changed from: package-private */
    public void swapAppOrder(LaunchItem from, LaunchItem to) {
        onCustomizedByUser();
        List<LaunchItem> displayedFavoritesList = getFavoriteItems();
        int fromIndex = this.favoriteItemsToDesiredPosition.get(from).intValue();
        int toIndex = this.favoriteItemsToDesiredPosition.get(to).intValue();
        this.favoriteItemsToDesiredPosition.put(from, Integer.valueOf(toIndex));
        this.favoriteItemsToDesiredPosition.put(to, Integer.valueOf(fromIndex));
        SharedPreferences.Editor editor = this.packageNamePrefs.edit();
        editor.putInt(from.getPackageName(), toIndex);
        editor.putInt(to.getPackageName(), fromIndex);
        editor.apply();
        notifyItemsSwapped(displayedFavoritesList.indexOf(from), displayedFavoritesList.indexOf(to));
    }

    /* access modifiers changed from: package-private */
    public int getOrderedFavoritePosition(LaunchItem item) {
        if (this.favoriteItemsToDesiredPosition.containsKey(item)) {
            return this.favoriteItemsToDesiredPosition.get(item).intValue();
        }
        return -1;
    }

    private List<LaunchItem> getSortedFavorites() {
        ArrayList<LaunchItem> items = new ArrayList<>(this.favoriteItemsToDesiredPosition.keySet());
        Collections.sort(items, new LaunchItemUserOrderComparator());
        return items;
    }

    private void addAppsToFavoritesFromSharedPreferences() {
        this.favoriteItemsToDesiredPosition.clear();
        Map<String, ?> keyValMap = this.packageNamePrefs.getAll();
        for (LaunchItem item : LaunchItemsManagerProvider.getInstance(this.context).getAllLaunchItemsWithoutSorting()) {
            addToPinnedApps(item);
            if (keyValMap.containsKey(item.getPackageName()) && !isPinnedFavorite(item)) {
                this.favoriteItemsToDesiredPosition.put(item, (Integer) keyValMap.get(item.getPackageName()));
            }
        }
    }

    private boolean addToPinnedApps(LaunchItem item) {
        Integer index;
        Map<String, Integer> map = this.pinnedAppPackagesToPosition;
        if (map == null || (index = map.get(item.getPackageName())) == null) {
            return false;
        }
        this.pinnedItems[index.intValue()] = item;
        return true;
    }

    private void initializeDefaultOrderFavorites() {
        SharedPreferences.Editor editor = this.packageNamePrefs.edit();
        editor.clear();
        for (String pkgName : this.defaultItemsOrder.keySet()) {
            editor.putInt(pkgName, this.defaultItemsOrder.get(pkgName).intValue());
        }
        editor.apply();
    }

    private void notifyOnHomeScreenItemsChanged(List<LaunchItem> changedItems) {
        LaunchItemsManager.HomeScreenItemsChangeListener homeScreenItemsChangeListener2 = this.homeScreenItemsChangeListener;
        if (homeScreenItemsChangeListener2 != null) {
            homeScreenItemsChangeListener2.onHomeScreenItemsChanged(changedItems);
        }
    }

    private void notifyOnHomeScreenItemsLoaded() {
        LaunchItemsManager.HomeScreenItemsChangeListener homeScreenItemsChangeListener2 = this.homeScreenItemsChangeListener;
        if (homeScreenItemsChangeListener2 != null) {
            homeScreenItemsChangeListener2.onHomeScreenItemsLoaded();
        }
    }

    private void notifyItemsSwapped(int fromDisplayedPosition, int toDisplayedPosition) {
        LaunchItemsManager.HomeScreenItemsChangeListener homeScreenItemsChangeListener2 = this.homeScreenItemsChangeListener;
        if (homeScreenItemsChangeListener2 != null) {
            homeScreenItemsChangeListener2.onHomeScreenItemsSwapped(fromDisplayedPosition, toDisplayedPosition);
        }
    }

    private void saveOrderSnapshotIfCustomizedByUser(List<LaunchItem> items) {
        if (this.customizedByUser) {
            saveOrderSnapshot(items);
        }
    }

    private void saveOrderSnapshot(List<LaunchItem> items) {
        SharedPreferences.Editor editor = this.packageNamePrefs.edit();
        editor.clear();
        for (int i = 0; i < items.size(); i++) {
            LaunchItem item = items.get(i);
            this.favoriteItemsToDesiredPosition.put(item, Integer.valueOf(i));
            editor.putInt(item.getPackageName(), i);
        }
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public boolean isFull() {
        return this.favoriteItemsToDesiredPosition.size() == 100;
    }

    /* access modifiers changed from: package-private */
    public void onCustomizedByUser() {
        if (!this.customizedByUser) {
            this.userCustomizationPref.edit().putBoolean(USER_CUSTOMIZATION_KEY, true).apply();
            this.customizedByUser = true;
            saveOrderSnapshot(getSortedFavorites());
        }
    }

    private static LaunchItem createMoreFavoritesLaunchItem(CharSequence appLabel, Intent intent) {
        LaunchItem item = new LaunchItem();
        item.setLabel(appLabel);
        item.setPackageName(MORE_FAVORITES_PKGNAME);
        item.setIntent(intent);
        return item;
    }
}
