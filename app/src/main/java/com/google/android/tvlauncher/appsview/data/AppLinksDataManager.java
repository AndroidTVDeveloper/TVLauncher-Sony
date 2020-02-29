package com.google.android.tvlauncher.appsview.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.tvlauncher.util.OemAppBase;
import com.google.android.tvlauncher.util.OemPromotionApp;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppLinksDataManager {
    private static final String PREF_FILE_NAME = "com.google.android.tvlauncher.appsview.AppLinksDataManager";
    private static final String TAG = "AppLinksDataManager";
    private static volatile AppLinksDataManager instance;
    private final SharedPreferences appLinkIdPrefs;
    private final Map<String, OemAppBase> appLinks = new HashMap();
    private final LaunchItemsManager launchItemsManager;

    public static AppLinksDataManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppLinksDataManager.class) {
                if (instance == null) {
                    instance = new AppLinksDataManager(context);
                }
            }
        }
        return instance;
    }

    AppLinksDataManager(Context context) {
        this.launchItemsManager = LaunchItemsManagerProvider.getInstance(context);
        this.appLinkIdPrefs = context.getSharedPreferences(PREF_FILE_NAME, 0);
        readFromPreferences();
    }

    public void createAppLink(OemAppBase appPromotion) {
        if (!this.appLinks.containsKey(appPromotion.getId())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.EXTRA_APP_NAME, appPromotion.getAppName());
                jsonObject.put(Constants.EXTRA_PACKAGE_NAME, appPromotion.getPackageName());
                jsonObject.put(Constants.EXTRA_BANNER_URI, appPromotion.getBannerUri());
                jsonObject.put(Constants.EXTRA_DATA_URI, appPromotion.getDataUri());
                jsonObject.put(Constants.EXTRA_DEVELOPER, appPromotion.getDeveloper());
                jsonObject.put(Constants.EXTRA_CATEGORY, appPromotion.getCategory());
                jsonObject.put(Constants.EXTRA_DESCRIPTION, appPromotion.getDescription());
                jsonObject.put(Constants.EXTRA_IS_GAME, appPromotion.isGame());
                jsonObject.put(Constants.EXTRA_SCREENSHOTS, new JSONArray((Collection) appPromotion.getScreenshotUris()));
                this.appLinkIdPrefs.edit().putString(appPromotion.getId(), jsonObject.toString()).apply();
                this.appLinks.put(appPromotion.getId(), appPromotion);
                this.launchItemsManager.onAppLinkAdded(appPromotion.getId());
            } catch (JSONException e) {
                Log.e(TAG, "Fail to convert the app promotion into JSON", e);
            }
        }
    }

    public OemAppBase getAppLink(String appLinkId) {
        return this.appLinks.get(appLinkId);
    }

    public void removeAppLink(String appLinkId) {
        if (this.appLinks.remove(appLinkId) != null) {
            this.appLinkIdPrefs.edit().remove(appLinkId).apply();
            this.launchItemsManager.onAppLinkRemoved(appLinkId);
        }
    }

    /* access modifiers changed from: package-private */
    public Collection<OemAppBase> getAppLinks() {
        return this.appLinks.values();
    }

    /* access modifiers changed from: package-private */
    public void readFromPreferences() {
        this.appLinks.clear();
        for (Map.Entry<String, String> entry : this.appLinkIdPrefs.getAll().entrySet()) {
            updateAppLinksFromJsonString((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private void updateAppLinksFromJsonString(String appLinkId, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            OemPromotionApp appLink = ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) new OemPromotionApp.Builder().setAppName(jsonObject.getString(Constants.EXTRA_APP_NAME))).setPackageName(jsonObject.getString(Constants.EXTRA_PACKAGE_NAME))).setBannerUri(jsonObject.getString(Constants.EXTRA_BANNER_URI))).setDataUri(jsonObject.getString(Constants.EXTRA_DATA_URI))).setDeveloper(jsonObject.getString(Constants.EXTRA_DEVELOPER))).setCategory(jsonObject.getString(Constants.EXTRA_CATEGORY))).setDescription(jsonObject.getString(Constants.EXTRA_DESCRIPTION))).setGame(jsonObject.getBoolean(Constants.EXTRA_IS_GAME))).setVirtualApp(true)).build();
            JSONArray screenshotUris = jsonObject.getJSONArray(Constants.EXTRA_SCREENSHOTS);
            for (int i = 0; i < screenshotUris.length(); i++) {
                appLink.addScreenshotUri(screenshotUris.getString(i));
            }
            if (TextUtils.equals(appLinkId, appLink.getId())) {
                this.appLinks.put(appLink.getId(), appLink);
                return;
            }
            StringBuilder sb = new StringBuilder(String.valueOf(appLinkId).length() + 47);
            sb.append("Promotion id '");
            sb.append(appLinkId);
            sb.append("' does not match with Json String");
            Log.e(TAG, sb.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Fail to convert encoded string into JSON", e);
        }
    }
}
