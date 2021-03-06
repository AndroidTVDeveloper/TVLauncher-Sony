package com.google.android.tvlauncher.appsview.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.util.OemConfiguration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LaunchItemsOrderManager {
    private static final String KEY_USE_DEFAULT_ORDER = "key_use_default_order";
    private static final int MAX_OOB_APPS = 20;
    private static final int MAX_OOB_GAMES = 10;
    private static final String PREF_FILE_NAME = "com.google.android.tvlauncher.appsview.PREFERENCE_FILE_KEY";
    private static LaunchItemsOrderManager launchItemsOrderManager;
    private LaunchItemComparator comparator;
    /* access modifiers changed from: private */
    public final HashMap<String, Integer> itemOrderMap = new HashMap<>();
    /* access modifiers changed from: private */
    public final HashMap<String, Integer> oemOrderMap = new HashMap<>();
    private SharedPreferences prefs;
    /* access modifiers changed from: private */
    public boolean useDefaultOrdering;

    private class LaunchItemComparator implements Comparator<LaunchItem> {
        private LaunchItemComparator() {
        }

        public int compare(LaunchItem o1, LaunchItem o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (LaunchItemsOrderManager.this.useDefaultOrdering) {
                Integer o1Index = (Integer) LaunchItemsOrderManager.this.oemOrderMap.get(o1.getPackageName());
                Integer o2Index = (Integer) LaunchItemsOrderManager.this.oemOrderMap.get(o2.getPackageName());
                int returnVal = compareIndices(o1Index, o2Index);
                if (returnVal != 0) {
                    return returnVal;
                }
                if (!(o1Index == null || o2Index == null)) {
                    return o1.compareTo(o2);
                }
            }
            Integer o1Index2 = (Integer) LaunchItemsOrderManager.this.itemOrderMap.get(o1.getPackageName());
            Integer o2Index2 = (Integer) LaunchItemsOrderManager.this.itemOrderMap.get(o2.getPackageName());
            int returnVal2 = compareIndices(o1Index2, o2Index2);
            if (returnVal2 != 0) {
                return returnVal2;
            }
            if (o1Index2 == null || o2Index2 == null) {
                return o1.compareTo(o2);
            }
            return o1.compareTo(o2);
        }

        private int compareIndices(Integer o1Index, Integer o2Index) {
            if (o1Index == null && o2Index == null) {
                return 0;
            }
            if (o1Index == null) {
                return 1;
            }
            if (o2Index == null || o2Index.intValue() > o1Index.intValue()) {
                return -1;
            }
            if (o2Index.intValue() < o1Index.intValue()) {
                return 1;
            }
            return 0;
        }
    }

    public static LaunchItemsOrderManager getInstance(Context context) {
        if (launchItemsOrderManager == null) {
            launchItemsOrderManager = new LaunchItemsOrderManager(context);
        }
        return launchItemsOrderManager;
    }

    public static void resetForTesting(Context context) {
        context.getSharedPreferences(PREF_FILE_NAME, 0).edit().clear().apply();
        launchItemsOrderManager = null;
    }

    /* access modifiers changed from: package-private */
    public void orderGivenItems(List<LaunchItem> items) {
        readIntoList();
        Collections.sort(items, this.comparator);
    }

    /* access modifiers changed from: package-private */
    public boolean userChangedOrder(List<LaunchItem> items) {
        SharedPreferences.Editor editor = this.prefs.edit();
        boolean useDefaultOrdering2 = this.prefs.getBoolean(KEY_USE_DEFAULT_ORDER, true);
        if (useDefaultOrdering2) {
            editor.putBoolean(KEY_USE_DEFAULT_ORDER, false);
            this.useDefaultOrdering = false;
        }
        addItemsToPreferences(items, editor);
        editor.apply();
        return useDefaultOrdering2;
    }

    /* access modifiers changed from: package-private */
    public void saveOrderSnapshot(List<LaunchItem> items) {
        SharedPreferences.Editor editor = this.prefs.edit();
        addItemsToPreferences(items, editor);
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public void switchToUserCustomization(List<LaunchItem> items) {
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.clear();
        editor.putBoolean(KEY_USE_DEFAULT_ORDER, false);
        this.useDefaultOrdering = false;
        this.itemOrderMap.clear();
        addItemsToPreferences(items, editor);
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public void removeItems(List<LaunchItem> items) {
        for (LaunchItem item : items) {
            removeItem(item);
        }
    }

    private void addOemOrderIfNeeded(SharedPreferences.Editor editor, List<String> oemOrder, int maxOobLimit) {
        if (oemOrder != null && !oemOrder.isEmpty() && this.useDefaultOrdering) {
            int i = 0;
            while (i < oemOrder.size() && i < maxOobLimit) {
                this.oemOrderMap.put(oemOrder.get(i), Integer.valueOf(i));
                i++;
            }
            addPkgNamesToPreferences(oemOrder, editor);
        }
    }

    /* access modifiers changed from: package-private */
    public void init(OemConfiguration oemConfiguration) {
        if (oemConfiguration.isDataLoaded()) {
            this.oemOrderMap.clear();
            SharedPreferences.Editor editor = this.prefs.edit();
            if (this.useDefaultOrdering) {
                readIntoList();
            }
            addOemOrderIfNeeded(editor, oemConfiguration.getOutOfBoxAllAppsList(), 20);
            addOemOrderIfNeeded(editor, oemConfiguration.getOutOfBoxGamesList(), 10);
            editor.apply();
        }
    }

    LaunchItemsOrderManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_FILE_NAME, 0);
        this.comparator = new LaunchItemComparator();
        this.useDefaultOrdering = this.prefs.getBoolean(KEY_USE_DEFAULT_ORDER, true);
    }

    private void addItemsToPreferences(List<LaunchItem> items, SharedPreferences.Editor editor) {
        for (int i = 0; i < items.size(); i++) {
            String pkgName = items.get(i).getPackageName();
            this.itemOrderMap.put(pkgName, Integer.valueOf(i));
            editor.putInt(pkgName, i);
        }
    }

    private void addPkgNamesToPreferences(List<String> pkgNames, SharedPreferences.Editor editor) {
        for (int i = 0; i < pkgNames.size(); i++) {
            editor.putInt(pkgNames.get(i), i);
        }
    }

    private void readIntoList() {
        if (this.itemOrderMap.isEmpty()) {
            Map<String, ?> keyValMap = this.prefs.getAll();
            for (String key : keyValMap.keySet()) {
                if (!TextUtils.equals(key, KEY_USE_DEFAULT_ORDER) && (keyValMap.get(key) instanceof Integer)) {
                    this.itemOrderMap.put(key, (Integer) keyValMap.get(key));
                }
            }
        }
    }

    private void removeItem(LaunchItem item) {
        this.itemOrderMap.remove(item.getPackageName());
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.remove(item.getPackageName());
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public HashMap<String, Integer> getItemOrderMap() {
        return this.itemOrderMap;
    }
}
