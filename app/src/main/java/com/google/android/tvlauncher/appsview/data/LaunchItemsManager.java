package com.google.android.tvlauncher.appsview.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.PackageChangedReceiver;
import com.google.android.tvlauncher.settings.ProfilesManager;
import com.google.android.tvlauncher.util.IntentUtil;
import com.google.android.tvlauncher.util.OemAppBase;
import com.google.android.tvlauncher.util.OemAppPromotions;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class LaunchItemsManager implements PackageChangedReceiver.Listener, InstallingLaunchItemListener {
    private static final String APP_STORE_PACKAGE_NAME = "com.android.vending";
    private static final boolean DEBUG = false;
    static final long FLEX_TIME_OOB_ORDERING = 8640000;
    private static final String GAME_STORE_PACKAGE_NAME = "com.google.android.play.games";
    private static final String KEY_SAVE_LAUNCH_ITEM_ORDER = "key_save_launch_item_order";
    static final long MAX_TIME_OOB_ORDERING = 172800000;
    private static final String TAG = "LaunchItemsManager";
    /* access modifiers changed from: private */
    public final List<LaunchItem> allLaunchItems = new LinkedList();
    private LaunchItem appStore;
    /* access modifiers changed from: private */
    public final List<AppsViewChangeListener> appsViewListeners = new LinkedList();
    protected Context context;
    private LocaleList currentLocales;
    private final BroadcastReceiver externalAppsUpdateReceiver;
    private final FavoriteLaunchItemsManager favoriteLaunchItemsManager;
    private LaunchItem gameStore;
    private boolean hasPendingLoadRequest;
    private final List<LaunchItem> installingLaunchItems = new LinkedList();
    private InstallingLaunchItemsDataHelper installingLaunchItemsDataHelper;
    private boolean isInitialized;
    /* access modifiers changed from: private */
    public boolean itemsLoaded;
    /* access modifiers changed from: private */
    public LaunchItemsOrderManager launchItemsOrderManager;
    private final PackageChangedReceiver packageChangedReceiver;
    private int receiversRegisteredRefCount;
    /* access modifiers changed from: private */
    public AsyncTask<Void, Void, Set<LaunchItem>> refreshTask;
    private List<SearchPackageChangeListener> searchChangeListeners = new LinkedList();

    public interface AppsViewChangeListener {
        void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean z, Pair<Integer, Integer> pair);

        void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> arrayList);

        void onLaunchItemsLoaded();

        void onLaunchItemsRemoved(ArrayList<LaunchItem> arrayList);
    }

    public interface HomeScreenItemsChangeListener {
        void onHomeScreenItemsChanged(List<LaunchItem> list);

        void onHomeScreenItemsLoaded();

        void onHomeScreenItemsSwapped(int i, int i2);
    }

    public interface SearchPackageChangeListener {
        void onSearchPackageChanged();
    }

    /* access modifiers changed from: package-private */
    public abstract Intent createNativeAppIntent(ResolveInfo resolveInfo);

    /* access modifiers changed from: package-private */
    public abstract List<ResolveInfo> getRawLaunchItems();

    /* access modifiers changed from: package-private */
    public abstract List<ResolveInfo> getRawLaunchItemsForPackage(String str);

    /* access modifiers changed from: package-private */
    public abstract boolean shouldShowAppStoreLaunchItem();

    /* access modifiers changed from: package-private */
    public abstract boolean shouldShowGameStoreLaunchItem();

    public static boolean checkIfAppStore(String pkgName) {
        return "com.android.vending".equalsIgnoreCase(pkgName);
    }

    public static boolean checkIfGameStore(String pkgName) {
        return GAME_STORE_PACKAGE_NAME.equalsIgnoreCase(pkgName);
    }

    LaunchItemsManager(Context initContext) {
        this.context = initContext.getApplicationContext();
        this.currentLocales = this.context.getResources().getConfiguration().getLocales();
        this.packageChangedReceiver = new PackageChangedReceiver(this);
        this.externalAppsUpdateReceiver = new ExternalAppsUpdateReceiver();
        this.favoriteLaunchItemsManager = new FavoriteLaunchItemsManager(this.context);
        registerAppsViewChangeListener(this.favoriteLaunchItemsManager);
        this.launchItemsOrderManager = LaunchItemsOrderManager.getInstance(this.context);
        this.context.registerComponentCallbacks(new ComponentCallbacks() {
            public void onConfigurationChanged(Configuration newConfig) {
                LaunchItemsManager.this.configurationChanged(newConfig);
            }

            public void onLowMemory() {
            }
        });
        this.installingLaunchItemsDataHelper = InstallingLaunchItemsDataHelper.getInstance(this.context);
    }

    public void initIfNeeded() {
        if (!this.isInitialized) {
            OemConfiguration oemConfiguration = OemConfiguration.get(this.context);
            this.favoriteLaunchItemsManager.init(oemConfiguration);
            this.launchItemsOrderManager.init(oemConfiguration);
            this.isInitialized = true;
        }
    }

    public void onAppOrderChange(ArrayList<LaunchItem> orderChangedItems, boolean isGameItems, Pair<Integer, Integer> focusedIndex) {
        boolean previouslyDefaultOrdering = this.launchItemsOrderManager.userChangedOrder(orderChangedItems);
        this.launchItemsOrderManager.orderGivenItems(this.allLaunchItems);
        if (previouslyDefaultOrdering) {
            this.launchItemsOrderManager.switchToUserCustomization(getAllLaunchItemsWithSorting());
        }
        for (AppsViewChangeListener cl : this.appsViewListeners) {
            cl.onEditModeItemOrderChange(orderChangedItems, isGameItems, focusedIndex);
        }
    }

    public void onPackageAdded(String packageName) {
        addOrUpdatePackage(packageName);
        resetOemDataIfNecessary(this.context, packageName, false);
    }

    public void onPackageChanged(String packageName) {
        addOrUpdatePackage(packageName);
        resetOemDataIfNecessary(this.context, packageName, false);
        checkForSearchChanges(packageName);
    }

    public void onPackageFullyRemoved(String packageName) {
        removePackage(packageName);
        resetOemDataIfNecessary(this.context, packageName, true);
    }

    public void onPackageRemoved(String packageName) {
        removePackage(packageName);
        resetOemDataIfNecessary(this.context, packageName, true);
    }

    public void onPackageReplaced(String packageName) {
        addOrUpdatePackage(packageName);
        resetOemDataIfNecessary(this.context, packageName, false);
        checkForSearchChanges(packageName);
    }

    public void onInstallingLaunchItemAdded(LaunchItem launchItem) {
        addOrUpdateInstallingLaunchItem(launchItem);
        this.installingLaunchItemsDataHelper.storeDataForPackage(launchItem.getPackageName(), launchItem.isGame());
    }

    public void onInstallingLaunchItemChanged(LaunchItem launchItem) {
        addOrUpdateInstallingLaunchItem(launchItem);
    }

    public void onInstallingLaunchItemRemoved(LaunchItem launchItem, boolean success) {
        removeInstallingLaunchItem(launchItem, success);
    }

    public void onAppLinkAdded(String appLinkId) {
        addOrUpdatePackage(appLinkId);
    }

    public void onAppLinkRemoved(String appLinkId) {
        removePackage(appLinkId);
    }

    public ArrayList<LaunchItem> getAllNonFavoriteInstalledLaunchItems() {
        ArrayList<LaunchItem> launchItems = new ArrayList<>();
        Iterator<LaunchItem> it = getAppLaunchItems().iterator();
        while (it.hasNext()) {
            LaunchItem item = it.next();
            if (!isFavorite(item) && !item.isInstalling()) {
                launchItems.add(item);
            }
        }
        Iterator<LaunchItem> it2 = getGameLaunchItems().iterator();
        while (it2.hasNext()) {
            LaunchItem item2 = it2.next();
            if (!isFavorite(item2) && !item2.isInstalling()) {
                launchItems.add(item2);
            }
        }
        return launchItems;
    }

    public ArrayList<LaunchItem> getGameLaunchItems() {
        return getLaunchItems(true);
    }

    public ArrayList<LaunchItem> getAppLaunchItems() {
        return getLaunchItems(false);
    }

    public ArrayList<LaunchItem> getAllLaunchItemsWithSorting() {
        ArrayList<LaunchItem> launchItems = new ArrayList<>();
        launchItems.addAll(this.installingLaunchItems);
        launchItems.addAll(this.allLaunchItems);
        this.launchItemsOrderManager.orderGivenItems(launchItems);
        return launchItems;
    }

    public LaunchItem getAppStoreLaunchItem() {
        if (shouldShowAppStoreLaunchItem()) {
            return this.appStore;
        }
        return null;
    }

    public LaunchItem getGameStoreLaunchItem() {
        if (shouldShowGameStoreLaunchItem()) {
            return this.gameStore;
        }
        return null;
    }

    public void refreshLaunchItems() {
        this.hasPendingLoadRequest = false;
        AsyncTask<Void, Void, Set<LaunchItem>> asyncTask = this.refreshTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        this.refreshTask = new CreateLaunchItemsListTask().execute(new Void[0]);
    }

    public void addSearchPackageChangeListener(SearchPackageChangeListener listener) {
        if (listener != null) {
            this.searchChangeListeners.add(listener);
        }
    }

    public void removeSearchPackageChangeListener(SearchPackageChangeListener listener) {
        this.searchChangeListeners.remove(listener);
    }

    public void registerAppsViewChangeListener(AppsViewChangeListener listener) {
        if (!this.appsViewListeners.contains(listener)) {
            this.appsViewListeners.add(listener);
        }
    }

    public void unregisterAppsViewChangeListener(AppsViewChangeListener listener) {
        this.appsViewListeners.remove(listener);
    }

    public void setHomeScreenItemsChangeListener(HomeScreenItemsChangeListener listener) {
        this.favoriteLaunchItemsManager.setHomeScreenItemsChangeListener(listener);
    }

    public void registerUpdateListeners() {
        int i = this.receiversRegisteredRefCount;
        this.receiversRegisteredRefCount = i + 1;
        if (i == 0) {
            this.context.registerReceiver(this.packageChangedReceiver, PackageChangedReceiver.getIntentFilter());
            this.context.registerReceiver(this.externalAppsUpdateReceiver, ExternalAppsUpdateReceiver.getIntentFilter());
        }
    }

    public void unregisterUpdateListeners() {
        int i = this.receiversRegisteredRefCount - 1;
        this.receiversRegisteredRefCount = i;
        if (i == 0) {
            this.context.unregisterReceiver(this.packageChangedReceiver);
            this.context.unregisterReceiver(this.externalAppsUpdateReceiver);
        }
    }

    public void addToFavorites(LaunchItem item) {
        this.favoriteLaunchItemsManager.userAddToFavorites(item);
    }

    public void addToFavorites(String pkgName) {
        this.favoriteLaunchItemsManager.userAddToFavorites(getLaunchItem(pkgName));
    }

    public void removeFromFavorites(LaunchItem item) {
        this.favoriteLaunchItemsManager.userRemoveFromFavorites(item);
    }

    public void swapFavoriteAppOrder(LaunchItem from, LaunchItem to) {
        this.favoriteLaunchItemsManager.swapAppOrder(from, to);
    }

    public int getOrderedFavoritePosition(LaunchItem item) {
        return this.favoriteLaunchItemsManager.getOrderedFavoritePosition(item);
    }

    public List<LaunchItem> getHomeScreenItems() {
        return this.favoriteLaunchItemsManager.getFavoriteItems();
    }

    public boolean areItemsLoaded() {
        return this.itemsLoaded;
    }

    public boolean hasPendingLoadRequest() {
        return this.hasPendingLoadRequest;
    }

    public void saveOrderSnapshot(List<LaunchItem> items) {
        this.launchItemsOrderManager.saveOrderSnapshot(items);
    }

    public boolean isFavorite(LaunchItem item) {
        return this.favoriteLaunchItemsManager.isFavorite(item);
    }

    public boolean isPinnedFavorite(LaunchItem item) {
        return this.favoriteLaunchItemsManager.isPinnedFavorite(item);
    }

    public boolean isOnlyFavorite(LaunchItem item) {
        return this.favoriteLaunchItemsManager.isOnlyFavorite(item);
    }

    public boolean isOnlyGame(LaunchItem item) {
        ArrayList<LaunchItem> games = getGameLaunchItems();
        return games.size() == 1 && games.contains(item);
    }

    public boolean isOnlyApp(LaunchItem item) {
        ArrayList<LaunchItem> apps = getAppLaunchItems();
        return apps.size() == 1 && apps.contains(item);
    }

    public boolean isFavoritesFull() {
        return this.favoriteLaunchItemsManager.isFull();
    }

    public LaunchItem getLaunchItem(String packageName) {
        for (LaunchItem item : this.installingLaunchItems) {
            if (item.getPackageName().equalsIgnoreCase(packageName)) {
                return item;
            }
        }
        for (LaunchItem item2 : this.allLaunchItems) {
            if (item2.getPackageName().equalsIgnoreCase(packageName)) {
                return item2;
            }
        }
        return null;
    }

    public Set<String> getAllLaunchItemsPackageName() {
        Set<String> packageNames = new HashSet<>();
        for (LaunchItem item : this.installingLaunchItems) {
            packageNames.add(item.getPackageName());
        }
        for (LaunchItem item2 : this.allLaunchItems) {
            packageNames.add(item2.getPackageName());
        }
        return packageNames;
    }

    public Locale getCurrentLocale() {
        return this.currentLocales.get(0);
    }

    /* access modifiers changed from: package-private */
    public void stopOutOfBoxOrdering() {
        OemConfiguration oemConfiguration = OemConfiguration.get(this.context);
        if (!areItemsLoaded() || !oemConfiguration.isDataLoaded()) {
            new StopOutOfBoxOrderingTask().execute(this.context);
            return;
        }
        switchToUserCustomization();
    }

    /* access modifiers changed from: package-private */
    public ArrayList<LaunchItem> getAllLaunchItemsWithoutSorting() {
        ArrayList<LaunchItem> launchItems = new ArrayList<>();
        launchItems.addAll(this.installingLaunchItems);
        launchItems.addAll(this.allLaunchItems);
        return launchItems;
    }

    /* access modifiers changed from: package-private */
    public LaunchItem getInstallingLaunchItem(String packageName) {
        for (LaunchItem item : this.installingLaunchItems) {
            if (item.getPackageName().equalsIgnoreCase(packageName)) {
                return item;
            }
        }
        return null;
    }

    private void addOrUpdatePackage(String pkgName) {
        ArrayList<LaunchItem> removedItems = new ArrayList<>();
        findAndRemoveExistingLaunchItems(pkgName, removedItems);
        ArrayList<LaunchItem> items = createLaunchItems(pkgName, removedItems);
        if (!items.isEmpty()) {
            Iterator<LaunchItem> it = items.iterator();
            while (it.hasNext()) {
                addItemToAppropriateArea(it.next());
            }
            for (AppsViewChangeListener cl : this.appsViewListeners) {
                cl.onLaunchItemsAddedOrUpdated(items);
            }
        }
        notifyItemsRemoved(removedItems);
    }

    /* access modifiers changed from: package-private */
    public void configurationChanged(Configuration newConfig) {
        LocaleList newLocales = newConfig.getLocales();
        LocaleList localeList = this.currentLocales;
        if (localeList == null || !localeList.equals(newLocales)) {
            this.hasPendingLoadRequest = true;
            this.currentLocales = newLocales;
        }
    }

    /* access modifiers changed from: private */
    public void switchToUserCustomization() {
        this.favoriteLaunchItemsManager.onCustomizedByUser();
        this.launchItemsOrderManager.switchToUserCustomization(getAllLaunchItemsWithSorting());
        ((JobScheduler) this.context.getSystemService(JobScheduler.class)).cancel(1);
    }

    private void removePackage(String pkgName) {
        ArrayList<LaunchItem> removedItems = new ArrayList<>();
        findAndRemoveExistingLaunchItems(pkgName, removedItems);
        notifyItemsRemoved(removedItems);
        checkForSearchChanges(pkgName);
        this.installingLaunchItemsDataHelper.removeGameFlagForPackage(pkgName);
    }

    private void addOrUpdateInstallingLaunchItem(LaunchItem launchItem) {
        if (launchItem != null) {
            String pkgName = launchItem.getPackageName();
            ArrayList<LaunchItem> items = new ArrayList<>();
            getLaunchItems(this.installingLaunchItems, items, pkgName, true);
            getLaunchItems(this.allLaunchItems, items, pkgName, true);
            Iterator it = items.iterator();
            while (it.hasNext()) {
                LaunchItem item = (LaunchItem) it.next();
                item.setInstallProgressPercent(launchItem.getInstallProgressPercent());
                item.setInstallState(launchItem.getInstallState());
                item.setBannerUri(launchItem.getBannerUri());
                item.setIconUri(launchItem.getIconUri());
            }
            if (items.isEmpty()) {
                items.add(launchItem);
            }
            this.installingLaunchItems.addAll(items);
            for (AppsViewChangeListener cl : this.appsViewListeners) {
                cl.onLaunchItemsAddedOrUpdated(items);
            }
        }
    }

    private void removeInstallingLaunchItem(LaunchItem launchItem, boolean success) {
        if (launchItem != null && !success) {
            addOrUpdatePackage(launchItem.getPackageName());
        }
    }

    private void notifyItemsRemoved(ArrayList<LaunchItem> removedItems) {
        if (!removedItems.isEmpty()) {
            for (AppsViewChangeListener cl : this.appsViewListeners) {
                cl.onLaunchItemsRemoved(removedItems);
            }
            this.launchItemsOrderManager.removeItems(removedItems);
        }
    }

    private void findAndRemoveExistingLaunchItems(String pkgName, ArrayList<LaunchItem> removedItems) {
        getLaunchItems(this.installingLaunchItems, removedItems, pkgName, true);
        getLaunchItems(this.allLaunchItems, removedItems, pkgName, true);
    }

    private ArrayList<LaunchItem> getLaunchItems(boolean isGame) {
        ArrayList<LaunchItem> launchitems = new ArrayList<>();
        getLaunchItems(this.installingLaunchItems, launchitems, isGame);
        getLaunchItems(this.allLaunchItems, launchitems, isGame);
        this.launchItemsOrderManager.orderGivenItems(launchitems);
        return launchitems;
    }

    private void getLaunchItems(List<LaunchItem> parentList, List<LaunchItem> childList, boolean isGame) {
        for (LaunchItem item : parentList) {
            if (isGame == item.isGame()) {
                childList.add(item);
            }
        }
    }

    private ArrayList<LaunchItem> getLaunchItems(List<LaunchItem> parentList, ArrayList<LaunchItem> found, String pkgName, boolean remove) {
        if (found == null) {
            found = new ArrayList<>();
        }
        Iterator<LaunchItem> itt = parentList.iterator();
        while (itt.hasNext()) {
            LaunchItem item = itt.next();
            if (TextUtils.equals(pkgName, item.getPackageName())) {
                found.add(item);
                if (remove) {
                    itt.remove();
                }
            }
        }
        return found;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0051 A[EDGE_INSN: B:51:0x0051->B:15:0x0051 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0090 A[EDGE_INSN: B:54:0x0090->B:27:0x0090 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.ArrayList<com.google.android.tvlauncher.appsview.LaunchItem> createLaunchItems(java.lang.String r9, java.util.ArrayList<com.google.android.tvlauncher.appsview.LaunchItem> r10) {
        /*
            r8 = this;
            if (r10 != 0) goto L_0x0008
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r10 = r0
        L_0x0008:
            java.util.List r0 = r8.getRawLaunchItemsForPackage(r9)
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            java.util.Iterator r2 = r0.iterator()
        L_0x0015:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0057
            java.lang.Object r3 = r2.next()
            android.content.pm.ResolveInfo r3 = (android.content.pm.ResolveInfo) r3
            android.content.pm.ActivityInfo r4 = r3.activityInfo
            if (r4 == 0) goto L_0x0056
            java.util.Iterator r4 = r10.iterator()
            r5 = 0
        L_0x002a:
            boolean r6 = r4.hasNext()
            if (r6 == 0) goto L_0x0051
            java.lang.Object r6 = r4.next()
            com.google.android.tvlauncher.appsview.LaunchItem r6 = (com.google.android.tvlauncher.appsview.LaunchItem) r6
            boolean r7 = r6.isInitialInstall()
            if (r7 != 0) goto L_0x0044
            boolean r7 = r6.hasSamePackageName(r3)
            if (r7 == 0) goto L_0x0043
            goto L_0x0044
        L_0x0043:
            goto L_0x002a
        L_0x0044:
            android.content.Context r7 = r8.context
            com.google.android.tvlauncher.appsview.LaunchItem r7 = r8.changeToNativeLaunchItem(r6, r7, r3)
            r1.add(r7)
            r4.remove()
            r5 = 1
        L_0x0051:
            if (r5 == 0) goto L_0x0056
            r2.remove()
        L_0x0056:
            goto L_0x0015
        L_0x0057:
            android.content.Context r3 = r8.context
            com.google.android.tvlauncher.appsview.data.AppLinksDataManager r3 = com.google.android.tvlauncher.appsview.data.AppLinksDataManager.getInstance(r3)
            com.google.android.tvlauncher.util.OemAppBase r3 = r3.getAppLink(r9)
            if (r3 == 0) goto L_0x0090
            java.util.Iterator r4 = r10.iterator()
        L_0x0067:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x0090
            java.lang.Object r5 = r4.next()
            com.google.android.tvlauncher.appsview.LaunchItem r5 = (com.google.android.tvlauncher.appsview.LaunchItem) r5
            boolean r6 = r5.isInitialInstall()
            if (r6 != 0) goto L_0x0085
            java.lang.String r6 = r3.getId()
            boolean r6 = r5.hasSamePackageName(r6)
            if (r6 == 0) goto L_0x0084
            goto L_0x0085
        L_0x0084:
            goto L_0x0067
        L_0x0085:
            com.google.android.tvlauncher.appsview.LaunchItem r6 = changeToVirtualLaunchItem(r5, r3)
            r1.add(r6)
            r4.remove()
            r3 = 0
        L_0x0090:
            java.util.Iterator r2 = r0.iterator()
            java.util.Iterator r4 = r10.iterator()
        L_0x0098:
            boolean r5 = r2.hasNext()
            if (r5 == 0) goto L_0x00bd
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x00bd
            java.lang.Object r5 = r4.next()
            com.google.android.tvlauncher.appsview.LaunchItem r5 = (com.google.android.tvlauncher.appsview.LaunchItem) r5
            android.content.Context r6 = r8.context
            java.lang.Object r7 = r2.next()
            android.content.pm.ResolveInfo r7 = (android.content.pm.ResolveInfo) r7
            com.google.android.tvlauncher.appsview.LaunchItem r5 = r8.changeToNativeLaunchItem(r5, r6, r7)
            r1.add(r5)
            r4.remove()
            goto L_0x0098
        L_0x00bd:
            java.util.Iterator r4 = r10.iterator()
            if (r3 == 0) goto L_0x00da
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x00da
            java.lang.Object r5 = r4.next()
            com.google.android.tvlauncher.appsview.LaunchItem r5 = (com.google.android.tvlauncher.appsview.LaunchItem) r5
            com.google.android.tvlauncher.appsview.LaunchItem r5 = changeToVirtualLaunchItem(r5, r3)
            r1.add(r5)
            r4.remove()
            r3 = 0
        L_0x00da:
            boolean r5 = r2.hasNext()
            if (r5 == 0) goto L_0x00f0
            android.content.Context r5 = r8.context
            java.lang.Object r6 = r2.next()
            android.content.pm.ResolveInfo r6 = (android.content.pm.ResolveInfo) r6
            com.google.android.tvlauncher.appsview.LaunchItem r5 = r8.createNativeAppLaunchItem(r5, r6)
            r1.add(r5)
            goto L_0x00da
        L_0x00f0:
            if (r3 == 0) goto L_0x00f9
            com.google.android.tvlauncher.appsview.LaunchItem r5 = createAppLinkLaunchItem(r3)
            r1.add(r5)
        L_0x00f9:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.appsview.data.LaunchItemsManager.createLaunchItems(java.lang.String, java.util.ArrayList):java.util.ArrayList");
    }

    private void checkForSearchChanges(String packageName) {
        if (this.searchChangeListeners.size() > 0 && packageName != null) {
            OemConfiguration oemConfiguration = OemConfiguration.get(this.context);
            if (packageName.equalsIgnoreCase(Constants.SEARCH_APP_PACKAGE_NAME) || (oemConfiguration != null && packageName.equalsIgnoreCase(oemConfiguration.getPackageName()))) {
                for (SearchPackageChangeListener listener : this.searchChangeListeners) {
                    listener.onSearchPackageChanged();
                }
            }
        }
    }

    public void addItemToAppropriateArea(LaunchItem item) {
        if (!this.installingLaunchItems.contains(item)) {
            this.allLaunchItems.add(item);
            if (checkIfAppStore(item.getPackageName())) {
                this.appStore = item;
            } else if (checkIfGameStore(item.getPackageName())) {
                this.gameStore = item;
            }
        }
    }

    private void resetOemDataIfNecessary(Context context2, String packageName, boolean packageRemoved) {
        OemConfiguration.resetIfNecessary(packageName, packageRemoved);
        OemAppPromotions.resetIfNecessary(context2, packageName);
    }

    /* access modifiers changed from: package-private */
    public LaunchItem createNativeAppLaunchItem(Context context2, ResolveInfo info) {
        LaunchItem item = new LaunchItem();
        changeToNativeLaunchItem(item, context2, info);
        item.setInitialInstall(true);
        return item;
    }

    static LaunchItem createAppLinkLaunchItem(OemAppBase appLink) {
        LaunchItem item = new LaunchItem();
        changeToVirtualLaunchItem(item, appLink);
        item.setInitialInstall(true);
        return item;
    }

    private LaunchItem changeToNativeLaunchItem(LaunchItem item, Context context2, ResolveInfo info) {
        item.recycle();
        item.setLabel(info.loadLabel(context2.getPackageManager()));
        item.setPackageName(info.activityInfo.packageName);
        try {
            item.setLastUpdateTime(context2.getPackageManager().getPackageInfo(item.getPackageName(), 0).lastUpdateTime);
        } catch (PackageManager.NameNotFoundException e) {
            String valueOf = String.valueOf(item.getPackageName());
            Log.e(TAG, valueOf.length() != 0 ? "Package not found when converting to native launch item : ".concat(valueOf) : new String("Package not found when converting to native launch item : "));
            item.setLastUpdateTime(0);
        }
        boolean z = true;
        boolean flagIsGame = (info.activityInfo.applicationInfo.flags & 33554432) != 0;
        boolean categoryIsGame = false;
        if (Build.VERSION.SDK_INT >= 26) {
            categoryIsGame = info.activityInfo.applicationInfo.category == 0;
        }
        boolean storedGameFlag = this.installingLaunchItemsDataHelper.getGameFlagForPackage(item.getPackageName());
        if (!flagIsGame && !categoryIsGame && !storedGameFlag) {
            z = false;
        }
        item.setIsGame(z);
        item.setIntent(createNativeAppIntent(info));
        item.setResolveInfo(info);
        item.setIsAppLink(false);
        item.setBannerUri(null);
        return item;
    }

    private static LaunchItem changeToVirtualLaunchItem(LaunchItem item, OemAppBase appLink) {
        item.recycle();
        item.setLastUpdateTime(0);
        item.setLabel(appLink.getAppName());
        item.setPackageName(appLink.getId());
        item.setIsGame(appLink.isGame());
        item.setIntent(IntentUtil.createVirtualAppIntent(appLink.getPackageName(), appLink.getDataUri()));
        item.setBannerUri(appLink.getBannerUri());
        item.setDataUri(appLink.getDataUri());
        item.setIsAppLink(true);
        return item;
    }

    class CreateLaunchItemsListTask extends AsyncTask<Void, Void, Set<LaunchItem>> {
        /* access modifiers changed from: protected */
        public /* bridge */ /* synthetic */ void onPostExecute(Object obj) {
            onPostExecute((Set<LaunchItem>) ((Set) obj));
        }

        CreateLaunchItemsListTask() {
        }

        /* access modifiers changed from: protected */
        public Set<LaunchItem> doInBackground(Void... voids) {
            if (isCancelled()) {
                return null;
            }
            List<ResolveInfo> rawLaunchItems = LaunchItemsManager.this.getRawLaunchItems();
            Set<LaunchItem> launchItems = new HashSet<>();
            boolean isRestrictedMode = ProfilesManager.getInstance(LaunchItemsManager.this.context).isRestrictedProfile();
            if (rawLaunchItems != null && !isRestrictedMode) {
                for (int i = 0; i < rawLaunchItems.size() && !isCancelled(); i++) {
                    ResolveInfo info = rawLaunchItems.get(i);
                    if (info.activityInfo != null) {
                        LaunchItemsManager launchItemsManager = LaunchItemsManager.this;
                        launchItems.add(launchItemsManager.createNativeAppLaunchItem(launchItemsManager.context, info));
                    }
                }
            } else if (isRestrictedMode) {
                for (int i2 = 0; i2 < rawLaunchItems.size() && !isCancelled(); i2++) {
                    ResolveInfo info2 = rawLaunchItems.get(i2);
                    ActivityInfo activityInfo = info2.activityInfo;
                    if (activityInfo != null && !"com.android.vending".equals(activityInfo.packageName)) {
                        LaunchItemsManager launchItemsManager2 = LaunchItemsManager.this;
                        launchItems.add(launchItemsManager2.createNativeAppLaunchItem(launchItemsManager2.context, info2));
                    }
                }
            }
            return launchItems;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Set<LaunchItem> launchItems) {
            for (OemAppBase applink : AppLinksDataManager.getInstance(LaunchItemsManager.this.context).getAppLinks()) {
                if (isCancelled()) {
                    break;
                }
                launchItems.add(LaunchItemsManager.createAppLinkLaunchItem(applink));
            }
            LaunchItemsManager.this.allLaunchItems.clear();
            for (LaunchItem item : launchItems) {
                LaunchItemsManager.this.addItemToAppropriateArea(item);
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LaunchItemsManager.this.context);
            boolean saveOrderAndStartJob = prefs.getBoolean(LaunchItemsManager.KEY_SAVE_LAUNCH_ITEM_ORDER, true);
            if (saveOrderAndStartJob) {
                prefs.edit().putBoolean(LaunchItemsManager.KEY_SAVE_LAUNCH_ITEM_ORDER, false).apply();
            }
            LaunchItemsManager.this.launchItemsOrderManager.orderGivenItems(LaunchItemsManager.this.allLaunchItems);
            if (saveOrderAndStartJob) {
                LaunchItemsManager.this.launchItemsOrderManager.saveOrderSnapshot(LaunchItemsManager.this.getAllLaunchItemsWithSorting());
                ((JobScheduler) LaunchItemsManager.this.context.getSystemService(JobScheduler.class)).schedule(new JobInfo.Builder(1, new ComponentName(LaunchItemsManager.this.context, StopOutOfBoxOrderingJobService.class)).setPeriodic(LaunchItemsManager.MAX_TIME_OOB_ORDERING, LaunchItemsManager.FLEX_TIME_OOB_ORDERING).setPersisted(true).build());
            }
            boolean unused = LaunchItemsManager.this.itemsLoaded = true;
            for (AppsViewChangeListener cl : LaunchItemsManager.this.appsViewListeners) {
                cl.onLaunchItemsLoaded();
            }
            AsyncTask unused2 = LaunchItemsManager.this.refreshTask = null;
        }
    }

    private static class StopOutOfBoxOrderingTask extends AsyncTask<Context, Void, Void> {
        private StopOutOfBoxOrderingTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Context... contexts) {
            final Context context = contexts[0];
            final OemConfiguration oemConfiguration = OemConfiguration.get(context);
            if (!oemConfiguration.isDataLoaded()) {
                oemConfiguration.registerOnDataLoadedListener(new OemConfiguration.OnDataLoadedListener() {
                    public void onDataLoaded() {
                        oemConfiguration.unregisterOnDataLoadedListener(this);
                        StopOutOfBoxOrderingTask.this.checkItemsLoadedAndSwitchToUserCustomization(context);
                    }
                });
                return null;
            }
            checkItemsLoadedAndSwitchToUserCustomization(context);
            return null;
        }

        /* access modifiers changed from: private */
        public void checkItemsLoadedAndSwitchToUserCustomization(Context context) {
            final LaunchItemsManager launchItemsManager = LaunchItemsManagerProvider.getInstance(context);
            launchItemsManager.initIfNeeded();
            if (launchItemsManager.areItemsLoaded()) {
                launchItemsManager.switchToUserCustomization();
                return;
            }
            launchItemsManager.registerAppsViewChangeListener(new AppsViewChangeListener(this) {
                public void onLaunchItemsLoaded() {
                    launchItemsManager.unregisterAppsViewChangeListener(this);
                    launchItemsManager.switchToUserCustomization();
                }

                public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> arrayList) {
                }

                public void onLaunchItemsRemoved(ArrayList<LaunchItem> arrayList) {
                }

                public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
                }
            });
            launchItemsManager.refreshLaunchItems();
        }
    }
}
