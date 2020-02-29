package com.google.android.tvlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.google.android.tvlauncher.util.ChannelConfigurationInfo;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.OemUtils;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class OemConfigurationData {
    private static final String ALL_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG = "all_apps_out_of_box_ordering";
    private static final String APPLY_STANDARD_STYLE_TO_INPUT_STATE_ICONS_PREFS_TAG = "apply_standard_style_to_input_state_icons";
    private static final String APPNAME_PACKAGE_MAP_TAG = "appname_package_map";
    private static final String APPS_VIEW_LAYOUT_OPTION_TAG = "apps_view_layout_option_tag";
    private static final String APP_CHANNEL_QUOTA_PREFS_TAG = "app_channel_quota";
    private static final String BUNDLED_TUNER_BANNER_PREFS_TAG = "bundled_tuner_banner";
    private static final String BUNDLED_TUNER_TITLE_PREFS_TAG = "bundled_tuner_title";
    private static final String CHANNELS_OUT_OF_BOX_ORDERING_PREFS_TAG = "channels_out_of_box_ordering";
    private static final String CONFIGURE_CHANNELS_APP_ORDERING_TAG = "configure_channels_app_ordering";
    private static final String CONTENT_PROVIDER_QUERY_COUNT_KEY = "content_provider_query_count";
    private static final int CONTENT_PROVIDER_QUERY_COUNT_LIMIT = 3;
    private static final String DISABLE_DISCONNECTED_INPUTS_PREFS_TAG = "disable_disconnected_inputs";
    private static final String ENABLE_INPUT_STATE_ICON_PREFS_TAG = "enable_input_state_icon";
    private static final String FAVORITE_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG = "favorite_apps_out_of_box_ordering";
    private static final String FORCE_LAUNCH_PACKAGE_AFTER_BOOT_TAG = "force_launch_package_after_boot";
    private static final String GAMES_OUT_OF_BOX_ORDERING_PREFS_TAG = "games_out_of_box_ordering";
    private static final String HAS_SEEN_SPONSORED_CHANNELS_TAG = "has_seen_sponsored_channels";
    private static final String HEADSUP_NOTIFICATIONS_BACKGROUND_COLOR_PREFS_TAG = "headsup_notifications_background_color";
    private static final String HEADSUP_NOTIFICATIONS_FONT_PREFS_TAG = "headsup_notifications_font";
    private static final String HEADSUP_NOTIFICATIONS_LOCATION_PREFS_TAG = "headsup_notifications_location";
    private static final String HEADSUP_NOTIFICATIONS_TEXT_COLOR_PREFS_TAG = "headsup_notifications_text_color";
    private static final String HIDDEN_UNINSTALL_PACKAGE_LIST_PREFS_TAG = "hidden_uninstall_package_list";
    private static final String HOME_BACKGROUND_URI_PREFS_TAG = "home_background_uri";
    private static final String HOME_SCREEN_INPUTS_ORDERING_PREFS_TAG = "home_screen_inputs_ordering";
    private static final String INPUTS_LABEL_PREFS_TAG = "inputs_panel_label_option";
    private static final String LAUNCH_OEM_USE_MAIN_INTENT_TAG = "use_main_intent";
    private static final String LIVE_TV_CHANNEL_OUT_OF_BOX_PKG_NAME_PREFS_TAG = "live_tv_channel_out_of_box_package_name";
    private static final String LIVE_TV_CHANNEL_OUT_OF_BOX_POSITION_PREFS_TAG = "live_tv_channel_out_of_box_position";
    private static final String LIVE_TV_CHANNEL_OUT_OF_BOX_SYSTEM_CHANNEL_KEY_PREFS_TAG = "live_tv_channel_out_of_box_system_channel_key";
    private static final int LIVE_TV_OOB_PACKAGE_NO_POSITION = -1;
    private static final long LOAD_TASK_TIMEOUT = TimeUnit.SECONDS.toMillis(20);
    private static final String OEM_CONFIGURATION_PACKAGE_VERSION_TAG = "oem_config_package_ver";
    protected static final String OEM_CONFIG_SHARED_PREFS = "oem_config";
    private static final String OEM_INPUTS_ICON_PREFS_TAG = "inputs_icon";
    private static final String PACKAGE_NAME_LAUNCH_AFTER_BOOT_TAG = "package_name_launch_after_boot";
    private static final String PACKAGE_NOTIFICATION_WHITELIST_PREFS_TAG = "package_notification_whitelist";
    private static final int PINNED_FAVORITE_APPS_LIMIT = 2;
    private static final String PINNED_FAVORITE_APPS_TAG = "pinned_favorite_apps";
    private static final String SEARCH_ICON_PREFS_TAG = "search_icon";
    private static final String SEARCH_ORB_FOCUSED_COLOR_TAG = "search_orb_focused_color";
    private static final String SEPARATOR = ",|,";
    private static final String SHOW_ADD_TO_WATCH_NEXT_FROM_PROGRAMS_MENU_PREFS_TAG = "show_add_to_watch_next_from_program_menu";
    private static final String SHOW_INPUTS_PREFS_TAG = "show_inputs";
    private static final String SHOW_PHYSICAL_INPUTS_SEPARATELY_PREFS_TAG = "show_physical_inputs_separately";
    private static final String SHOW_REMOVE_PROGRAM_FROM_PROGRAMS_MENU_PREFS_TAG = "show_remove_program_from_program_menu";
    private static final String SPLIT_DELIMITER = ",\\|,";
    private static final String TAG = "OemConfigurationData";
    private static final String TIME_STAMP_PREFS_TAG = "time_stamp";
    private static final String USE_CUSTOM_INPUT_LIST_PREFS_TAG = "use_custom_input_list";
    private static final String WATCH_NEXT_CHANNEL_AUTO_HIDE_ENABLED = "watch_next_channel_auto_hide_enabled_2";
    private static final String WATCH_NEXT_CHANNEL_ENABLED_BY_DEFAULT = "watch_next_channel_enabled_by_default";
    private List<String> allAppsOutOfBoxOrdering;
    private HashMap<String, Integer> appChannelQuota;
    private HashMap<String, String> appNames;
    private boolean applyStandardStyleToInputStateIcons;
    private OemConfiguration.LayoutOrderOptions appsViewLayoutOption;
    private Uri bundledTunerBannerUri;
    private String bundledTunerTitle;
    private Map<String, ChannelConfigurationInfo> channelInfoMap;
    private List<ChannelConfigurationInfo> channelsOutOfBoxOrdering;
    private final int configurationPackageVersion;
    private List<String> configureChannelsAppOrdering;
    protected Context context;
    private boolean disableDisconnectedInputs;
    private String disconnectedInputText;
    private boolean enableInputStateIcon;
    private List<String> favoriteAppsOutOfBoxOrdering;
    private boolean forceLaunchPackageAfterBoot;
    private List<String> gamesOutOfBoxOrdering;
    private boolean hasSeenSponsoredChannelsForCurrentConfig;
    private boolean hasSeenSponsoredChannelsSoFar;
    private int headsupNotificationsBackgroundColor;
    private boolean headsupNotificationsBackgroundColorWasSet;
    private String headsupNotificationsFont;
    private String headsupNotificationsLocation;
    private int headsupNotificationsTextColor;
    private boolean headsupNotificationsTextColorWasSet;
    private List<String> hiddenUninstallPackageList;
    private String homeBackgroundUri;
    private List<String> homeScreenInputsOrdering;
    private String inputsPanelLabelOption;
    private boolean isDataCachedInPrefs;
    private boolean isDataLoadingInProgress;
    private int latestOutOfBoxChannelPosition;
    private boolean launchOemUseMainIntent;
    private int liveTvChannelOobPosition;
    private ChannelConfigurationInfo liveTvOobPackageInfo;
    private Uri oemInputsIconUri;
    private final ArraySet<OemConfiguration.OnDataLoadedListener> onDataLoadedListeners = new ArraySet<>(1);
    private List<OemOutOfBoxApp> outOfBoxApps;
    private String packageNameLaunchAfterBoot;
    private List<String> packageNotificationWhitelist;
    private List<String> pinnedFavoriteApps;
    private SharedPreferences prefs;
    private Uri searchIconUri;
    private int searchOrbFocusedColor;
    private boolean searchOrbFocusedColorWasSet;
    private boolean showAddToWatchNextFromProgramMenu;
    private boolean showInputs;
    private boolean showPhysicalInputsSeparately;
    private boolean showRemoveProgramFromProgramMenu;
    private List<ChannelConfigurationInfo> sponsoredChannelsReadFromContentProvider;
    private boolean useCustomInputList;
    private boolean watchNextChannelAutoHideEnabled;
    private boolean watchNextChannelEnabledByDefault;

    OemConfigurationData(Context context2, int configurationPackageVersion2) {
        boolean forceReadFromContentProvider = true;
        init();
        this.context = context2;
        this.configurationPackageVersion = configurationPackageVersion2;
        this.isDataCachedInPrefs = false;
        Context context3 = this.context;
        if (context3 != null) {
            this.prefs = context3.getSharedPreferences(OEM_CONFIG_SHARED_PREFS, 0);
            this.isDataCachedInPrefs = this.prefs.getLong(TIME_STAMP_PREFS_TAG, -1) != -1;
            this.hasSeenSponsoredChannelsSoFar = this.prefs.getBoolean(HAS_SEEN_SPONSORED_CHANNELS_TAG, false);
            forceReadFromContentProvider = configurationPackageVersion2 > this.prefs.getInt(OEM_CONFIGURATION_PACKAGE_VERSION_TAG, -1) && forceReadFromContentProvider;
            if (this.prefs.getBoolean(NotifyRefreshOemConfigurationDataJobService.REFRESH_OEM_CONFIGURATION_DATA, false)) {
                this.prefs.edit().putBoolean(NotifyRefreshOemConfigurationDataJobService.REFRESH_OEM_CONFIGURATION_DATA, false).apply();
                forceReadFromContentProvider = true;
            }
            loadData(forceReadFromContentProvider);
        }
    }

    /* access modifiers changed from: package-private */
    public void refresh() {
        init();
        boolean z = false;
        this.isDataCachedInPrefs = false;
        if (this.context != null) {
            if (this.prefs.getLong(TIME_STAMP_PREFS_TAG, -1) != -1) {
                z = true;
            }
            this.isDataCachedInPrefs = z;
            loadData(true);
        }
    }

    private void init() {
        this.hasSeenSponsoredChannelsForCurrentConfig = false;
        this.sponsoredChannelsReadFromContentProvider = new ArrayList();
        this.latestOutOfBoxChannelPosition = -1;
        this.homeScreenInputsOrdering = new ArrayList(8);
        this.packageNotificationWhitelist = new ArrayList(5);
        this.channelsOutOfBoxOrdering = new ArrayList(10);
        this.favoriteAppsOutOfBoxOrdering = new ArrayList(8);
        this.allAppsOutOfBoxOrdering = new ArrayList(20);
        this.gamesOutOfBoxOrdering = new ArrayList(10);
        this.configureChannelsAppOrdering = new ArrayList(1);
        this.pinnedFavoriteApps = new ArrayList(2);
        this.hiddenUninstallPackageList = new ArrayList(5);
        this.appChannelQuota = new HashMap<>(20);
        this.appNames = new HashMap<>();
        this.searchIconUri = null;
        this.searchOrbFocusedColorWasSet = false;
        this.searchOrbFocusedColor = -1;
        this.bundledTunerBannerUri = null;
        this.bundledTunerTitle = null;
        this.disableDisconnectedInputs = true;
        this.disconnectedInputText = null;
        this.enableInputStateIcon = false;
        this.applyStandardStyleToInputStateIcons = true;
        this.showInputs = false;
        this.inputsPanelLabelOption = OemConfiguration.INPUTS_PANEL_LABEL_INPUTS;
        this.oemInputsIconUri = null;
        this.showPhysicalInputsSeparately = false;
        this.useCustomInputList = false;
        this.headsupNotificationsFont = null;
        this.headsupNotificationsTextColorWasSet = false;
        this.headsupNotificationsBackgroundColorWasSet = false;
        this.headsupNotificationsTextColor = -1;
        this.headsupNotificationsBackgroundColor = -1;
        this.headsupNotificationsLocation = null;
        this.appsViewLayoutOption = null;
        this.forceLaunchPackageAfterBoot = false;
        this.packageNameLaunchAfterBoot = null;
        this.liveTvOobPackageInfo = null;
        this.liveTvChannelOobPosition = -1;
        this.outOfBoxApps = new ArrayList();
        this.channelInfoMap = new HashMap();
        this.homeBackgroundUri = null;
        this.showAddToWatchNextFromProgramMenu = true;
        this.showRemoveProgramFromProgramMenu = true;
        this.watchNextChannelEnabledByDefault = true;
        this.watchNextChannelAutoHideEnabled = true;
    }

    private void loadData(boolean forceReadFromContentProvider) {
        if (!this.isDataCachedInPrefs || forceReadFromContentProvider) {
            if (this.isDataCachedInPrefs) {
                readFromSharedPrefs(true);
            }
            OemConfigurationDataLoadingTask task = new OemConfigurationDataLoadingTask(this, this.context);
            task.executeOnExecutor(Executors.getThreadPoolExecutor());
            this.isDataLoadingInProgress = true;
            new Handler().postDelayed(new OemConfigurationData$$Lambda$0(this, task), LOAD_TASK_TIMEOUT);
            return;
        }
        readFromSharedPrefs(false);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$loadData$0$OemConfigurationData(OemConfigurationDataLoadingTask task) {
        if (task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            saveToSharedPrefs();
            onDataLoaded();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDataLoaded() {
        return this.isDataCachedInPrefs && !this.isDataLoadingInProgress;
    }

    /* access modifiers changed from: package-private */
    public void setDataLoaded(boolean loaded) {
        this.isDataCachedInPrefs = loaded;
    }

    /* access modifiers changed from: package-private */
    public void registerOnDataLoadedListener(OemConfiguration.OnDataLoadedListener listener) {
        this.onDataLoadedListeners.add(listener);
    }

    /* access modifiers changed from: package-private */
    public void unregisterOnDataLoadedListener(OemConfiguration.OnDataLoadedListener listener) {
        this.onDataLoadedListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    public void onDataLoaded() {
        this.isDataCachedInPrefs = true;
        this.isDataLoadingInProgress = false;
        resetContentProviderQueryCount();
        for (OemConfiguration.OnDataLoadedListener listener : new ArraySet<>((ArraySet) this.onDataLoadedListeners)) {
            listener.onDataLoaded();
        }
    }

    private void resetContentProviderQueryCount() {
        this.prefs.edit().putInt(CONTENT_PROVIDER_QUERY_COUNT_KEY, 0).apply();
    }

    /* access modifiers changed from: package-private */
    public Uri getSearchIconUri() {
        return this.searchIconUri;
    }

    /* access modifiers changed from: package-private */
    public void setSearchIconUri(Uri searchIconUri2) {
        this.searchIconUri = searchIconUri2;
    }

    /* access modifiers changed from: package-private */
    public int getSearchOrbFocusedColor(int defaultColor) {
        return this.searchOrbFocusedColorWasSet ? this.searchOrbFocusedColor : defaultColor;
    }

    /* access modifiers changed from: package-private */
    public void setSearchOrbFocusedColor(int focusedColor) {
        this.searchOrbFocusedColor = focusedColor;
        this.searchOrbFocusedColorWasSet = true;
    }

    /* access modifiers changed from: package-private */
    public Uri getBundledTunerBannerUri() {
        return this.bundledTunerBannerUri;
    }

    /* access modifiers changed from: package-private */
    public void setBundledTunerBannerUri(Uri bundledTunnerBannerUri) {
        this.bundledTunerBannerUri = bundledTunnerBannerUri;
    }

    /* access modifiers changed from: package-private */
    public String getBundledTunerTitle() {
        return this.bundledTunerTitle;
    }

    /* access modifiers changed from: package-private */
    public void setBundledTunerTitle(String bundledTunnerTitle) {
        this.bundledTunerTitle = bundledTunnerTitle;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisableDisconnectedInputs() {
        return this.disableDisconnectedInputs;
    }

    /* access modifiers changed from: package-private */
    public void setDisableDisconnectedInputs(boolean disableDisconnectedInputs2) {
        this.disableDisconnectedInputs = disableDisconnectedInputs2;
    }

    /* access modifiers changed from: package-private */
    public String getDisconnectedInputText() {
        return this.disconnectedInputText;
    }

    /* access modifiers changed from: package-private */
    public void setDisconnectedInputText(String disconnectedInputText2) {
        this.disconnectedInputText = disconnectedInputText2;
    }

    /* access modifiers changed from: package-private */
    public boolean isEnableInputStateIcon() {
        return this.enableInputStateIcon;
    }

    /* access modifiers changed from: package-private */
    public void setEnableInputStateIcon(boolean enableInputStateIcon2) {
        this.enableInputStateIcon = enableInputStateIcon2;
    }

    /* access modifiers changed from: package-private */
    public Uri getOemInputsIconUri() {
        return this.oemInputsIconUri;
    }

    /* access modifiers changed from: package-private */
    public void setOemInputsIconUri(Uri oemInputsIconUri2) {
        this.oemInputsIconUri = oemInputsIconUri2;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldApplyStandardStyleToInputStateIcons() {
        return this.applyStandardStyleToInputStateIcons;
    }

    /* access modifiers changed from: package-private */
    public void setApplyStandardStyleToInputStateIcons(boolean applyStandardStyleToInputStateIcons2) {
        this.applyStandardStyleToInputStateIcons = applyStandardStyleToInputStateIcons2;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowInputs() {
        return this.showInputs;
    }

    /* access modifiers changed from: package-private */
    public void setShowInputs(boolean showInputs2) {
        this.showInputs = showInputs2;
    }

    /* access modifiers changed from: package-private */
    public String getInputsPanelLabelOption() {
        return this.inputsPanelLabelOption;
    }

    /* access modifiers changed from: package-private */
    public void setInputsPanelLabelOption(String inputsPanelLabelOption2) {
        this.inputsPanelLabelOption = inputsPanelLabelOption2;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowPhysicalInputsSeparately() {
        return this.showPhysicalInputsSeparately;
    }

    /* access modifiers changed from: package-private */
    public void setShowPhysicalInputsSeparately(boolean showPhysicalInputsSeparately2) {
        this.showPhysicalInputsSeparately = showPhysicalInputsSeparately2;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUseCustomInputList() {
        return this.useCustomInputList;
    }

    /* access modifiers changed from: package-private */
    public void setUseCustomInputList(boolean useCustomInputList2) {
        this.useCustomInputList = useCustomInputList2;
    }

    /* access modifiers changed from: package-private */
    public String getHomeBackgroundUri() {
        return this.homeBackgroundUri;
    }

    /* access modifiers changed from: package-private */
    public void setHomeBackgroundUri(String homeBackgroundUri2) {
        this.homeBackgroundUri = homeBackgroundUri2;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowAddToWatchNextFromProgramMenu() {
        return this.showAddToWatchNextFromProgramMenu;
    }

    /* access modifiers changed from: package-private */
    public void setShowAddToWatchNextFromProgramMenu(boolean showAddToWatchNextFromProgramMenu2) {
        this.showAddToWatchNextFromProgramMenu = showAddToWatchNextFromProgramMenu2;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowRemoveProgramFromProgramMenu() {
        return this.showRemoveProgramFromProgramMenu;
    }

    /* access modifiers changed from: package-private */
    public void setShowRemoveProgramFromProgramMenu(boolean showRemoveProgramFromProgramMenu2) {
        this.showRemoveProgramFromProgramMenu = showRemoveProgramFromProgramMenu2;
    }

    /* access modifiers changed from: package-private */
    public boolean isWatchNextChannelEnabledByDefault() {
        return this.watchNextChannelEnabledByDefault;
    }

    /* access modifiers changed from: package-private */
    public void setWatchNextChannelEnabledByDefault(boolean watchNextChannelEnabledByDefault2) {
        this.watchNextChannelEnabledByDefault = watchNextChannelEnabledByDefault2;
    }

    /* access modifiers changed from: package-private */
    public boolean isWatchNextChannelAutoHideEnabled() {
        return this.watchNextChannelAutoHideEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setWatchNextChannelAutoHideEnabled(boolean watchNextChannelAutoHideEnabled2) {
        this.watchNextChannelAutoHideEnabled = watchNextChannelAutoHideEnabled2;
    }

    /* access modifiers changed from: package-private */
    public void addInputToHomeScreenInputsOrdering(String input) {
        if (!this.homeScreenInputsOrdering.contains(input)) {
            this.homeScreenInputsOrdering.add(input);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getHomeScreenInputsOrdering() {
        return this.homeScreenInputsOrdering;
    }

    /* access modifiers changed from: package-private */
    public void addAppToVirtualAppsOutOfBoxOrdering(OemOutOfBoxApp app) {
        addAppToVirtualAppsOutOfBoxOrdering(app, true);
    }

    private void addAppToVirtualAppsOutOfBoxOrdering(OemOutOfBoxApp app, boolean readFromConfigFile) {
        if ((!readFromConfigFile || !this.isDataCachedInPrefs) && app.isVirtualApp() && !TextUtils.isEmpty(app.getAppName()) && !TextUtils.isEmpty(app.getPackageName()) && !TextUtils.isEmpty(app.getBannerUri()) && !TextUtils.isEmpty(app.getDataUri())) {
            this.outOfBoxApps.add(app);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean addAppToFavoriteAppsOutOfBoxOrdering(String appName, String packageName) {
        return addAppToFavoriteAppsOutOfBoxOrdering(appName, packageName, true);
    }

    private boolean addAppToFavoriteAppsOutOfBoxOrdering(String appName, String packageName, boolean readFromConfigFile) {
        if ((readFromConfigFile && this.isDataCachedInPrefs) || this.favoriteAppsOutOfBoxOrdering.contains(packageName) || this.pinnedFavoriteApps.contains(packageName)) {
            return false;
        }
        this.favoriteAppsOutOfBoxOrdering.add(packageName);
        this.appNames.put(packageName, appName);
        return true;
    }

    /* access modifiers changed from: package-private */
    public List<String> getFavoriteAppsOutOfBoxOrdering() {
        return this.favoriteAppsOutOfBoxOrdering;
    }

    /* access modifiers changed from: package-private */
    public void addAppToAllAppsOutOfBoxOrdering(String appName, String packageName) {
        addAppToAllAppsOutOfBoxOrdering(appName, packageName, true);
    }

    private void addAppToAllAppsOutOfBoxOrdering(String appName, String packageName, boolean readFromConfigFile) {
        if ((!readFromConfigFile || !this.isDataCachedInPrefs) && !this.allAppsOutOfBoxOrdering.contains(packageName)) {
            this.allAppsOutOfBoxOrdering.add(packageName);
            this.appNames.put(packageName, appName);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getAllAppsOutOfBoxOrdering() {
        return this.allAppsOutOfBoxOrdering;
    }

    /* access modifiers changed from: package-private */
    public void addAppToGamesOutOfBoxOrdering(String appName, String packageName) {
        addAppToGamesOutOfBoxOrdering(appName, packageName, true);
    }

    private void addAppToGamesOutOfBoxOrdering(String appName, String packageName, boolean readFromConfigFile) {
        if ((!readFromConfigFile || !this.isDataCachedInPrefs) && !this.gamesOutOfBoxOrdering.contains(packageName)) {
            this.gamesOutOfBoxOrdering.add(packageName);
            this.appNames.put(packageName, appName);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getGamesOutOfBoxOrdering() {
        return this.gamesOutOfBoxOrdering;
    }

    /* access modifiers changed from: package-private */
    public void addAppToAppChannelQuota(String packageName, int quota) {
        this.appChannelQuota.put(packageName, Integer.valueOf(quota));
    }

    /* access modifiers changed from: package-private */
    public int getAppChannelQuota(String packageName) {
        if (this.appChannelQuota.containsKey(packageName)) {
            return this.appChannelQuota.get(packageName).intValue();
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void addAppToConfigureChannelsAppOrdering(String packageName) {
        addAppToConfigureChannelsAppOrdering(packageName, true);
    }

    private void addAppToConfigureChannelsAppOrdering(String packageName, boolean readFromConfigFile) {
        if ((!readFromConfigFile || !this.isDataCachedInPrefs) && !this.configureChannelsAppOrdering.contains(packageName)) {
            this.configureChannelsAppOrdering.add(packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getConfigureChannelsAppOrdering() {
        return this.configureChannelsAppOrdering;
    }

    /* access modifiers changed from: package-private */
    public void addChannelToOutOfBoxOrdering(ChannelConfigurationInfo.Builder channelConfigurationInfoBuilder) {
        addChannelToOutOfBoxOrdering(channelConfigurationInfoBuilder, true);
    }

    private void addChannelToOutOfBoxOrdering(ChannelConfigurationInfo.Builder channelConfigurationInfoBuilder, boolean readFromConfigFile) {
        boolean isRereadFromConfigFile = readFromConfigFile && this.isDataCachedInPrefs;
        if (!isRereadFromConfigFile || !this.hasSeenSponsoredChannelsSoFar) {
            if (channelConfigurationInfoBuilder.isSponsored()) {
                this.hasSeenSponsoredChannelsForCurrentConfig = true;
            }
            if (isRereadFromConfigFile) {
                reReadOutOfBoxChannelFromConfigFile(channelConfigurationInfoBuilder);
            } else if (TextUtils.isEmpty(channelConfigurationInfoBuilder.getSystemChannelKey()) || !this.channelInfoMap.containsKey(ChannelConfigurationInfo.getUniqueKey(channelConfigurationInfoBuilder.getPackageName(), channelConfigurationInfoBuilder.getSystemChannelKey()))) {
                ChannelConfigurationInfo channelConfigurationInfo = this.liveTvOobPackageInfo;
                if (channelConfigurationInfo == null || !TextUtils.equals(channelConfigurationInfo.getPackageName(), channelConfigurationInfoBuilder.getPackageName())) {
                    int i = this.latestOutOfBoxChannelPosition + 1;
                    this.latestOutOfBoxChannelPosition = i;
                    channelConfigurationInfoBuilder.setChannelPosition(i);
                    ChannelConfigurationInfo channelConfigurationInfo2 = channelConfigurationInfoBuilder.build();
                    this.channelsOutOfBoxOrdering.add(channelConfigurationInfo2);
                    if (channelConfigurationInfo2.isSponsored()) {
                        this.sponsoredChannelsReadFromContentProvider.add(channelConfigurationInfo2);
                    }
                    addChannelToChannelInfoMap(channelConfigurationInfo2);
                    return;
                }
                Log.e(TAG, "Live TV OOB channel order has already been defined.");
            } else {
                String packageName = channelConfigurationInfoBuilder.getPackageName();
                String systemChannelKey = channelConfigurationInfoBuilder.getSystemChannelKey();
                StringBuilder sb = new StringBuilder(String.valueOf(packageName).length() + 92 + String.valueOf(systemChannelKey).length());
                sb.append("channel with package = ");
                sb.append(packageName);
                sb.append(" with system_channel_key = ");
                sb.append(systemChannelKey);
                sb.append(" has already appeared in out-of-box order.");
                Log.e(TAG, sb.toString());
            }
        }
    }

    private void reReadOutOfBoxChannelFromConfigFile(ChannelConfigurationInfo.Builder channelConfigurationInfoBuilder) {
        int channelPos;
        if (channelConfigurationInfoBuilder.isSponsored()) {
            if (this.channelInfoMap.containsKey(ChannelConfigurationInfo.getUniqueKey(channelConfigurationInfoBuilder.getPackageName(), channelConfigurationInfoBuilder.getSystemChannelKey()))) {
                channelPos = this.channelsOutOfBoxOrdering.indexOf(channelConfigurationInfoBuilder.build());
            } else if (this.channelInfoMap.containsKey(ChannelConfigurationInfo.getUniqueKey(channelConfigurationInfoBuilder.getPackageName(), null))) {
                channelPos = this.channelsOutOfBoxOrdering.indexOf(new ChannelConfigurationInfo.Builder().setPackageName(channelConfigurationInfoBuilder.getPackageName()).setSystemChannelKey(null).build());
            } else {
                String packageName = channelConfigurationInfoBuilder.getPackageName();
                String systemChannelKey = channelConfigurationInfoBuilder.getSystemChannelKey();
                StringBuilder sb = new StringBuilder(String.valueOf(packageName).length() + ClientAnalytics.LogRequest.LogSource.FLYDROID_COUNTERS_VALUE + String.valueOf(systemChannelKey).length());
                sb.append("The sponsored channel is skipped because it did not exist before in the previous configuration with the same package name: ");
                sb.append(packageName);
                sb.append(" and system_channel_key: ");
                sb.append(systemChannelKey);
                Log.e(TAG, sb.toString());
                return;
            }
            if (channelPos != -1) {
                ChannelConfigurationInfo channelConfigurationInfo = channelConfigurationInfoBuilder.setChannelPosition(channelPos).build();
                this.channelsOutOfBoxOrdering.set(channelPos, channelConfigurationInfo);
                addChannelToChannelInfoMap(channelConfigurationInfo);
                this.sponsoredChannelsReadFromContentProvider.add(channelConfigurationInfo);
                return;
            }
            String packageName2 = channelConfigurationInfoBuilder.getPackageName();
            String systemChannelKey2 = channelConfigurationInfoBuilder.getSystemChannelKey();
            StringBuilder sb2 = new StringBuilder(String.valueOf(packageName2).length() + 105 + String.valueOf(systemChannelKey2).length());
            sb2.append("The sponsored channel with package name: ");
            sb2.append(packageName2);
            sb2.append(" and system_channel_key: ");
            sb2.append(systemChannelKey2);
            sb2.append(" does not exist in out of box ordering.");
            throw new IllegalStateException(sb2.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public List<ChannelConfigurationInfo> getSponsoredChannelsReadFromContentProvider() {
        return this.sponsoredChannelsReadFromContentProvider;
    }

    /* access modifiers changed from: package-private */
    public List<ChannelConfigurationInfo> getChannelsOutOfBoxOrdering() {
        return this.channelsOutOfBoxOrdering;
    }

    private void addChannelToChannelInfoMap(ChannelConfigurationInfo channelConfigurationInfo) {
        this.channelInfoMap.put(ChannelConfigurationInfo.getUniqueKey(channelConfigurationInfo.getPackageName(), channelConfigurationInfo.getSystemChannelKey()), channelConfigurationInfo);
    }

    /* access modifiers changed from: package-private */
    public ChannelConfigurationInfo getChannelInfo(String packageName, String systemChannelKey) {
        return this.channelInfoMap.get(ChannelConfigurationInfo.getUniqueKey(packageName, systemChannelKey));
    }

    /* access modifiers changed from: package-private */
    public void addPackageToPackageNotificationWhitelist(String packageName) {
        if (!this.packageNotificationWhitelist.contains(packageName)) {
            this.packageNotificationWhitelist.add(packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getPackageToPackageNotificationWhitelist() {
        return this.packageNotificationWhitelist;
    }

    /* access modifiers changed from: package-private */
    public String getHeadsupNotificationsLocation() {
        return this.headsupNotificationsLocation;
    }

    /* access modifiers changed from: package-private */
    public void setHeadsupNotificationsLocation(String headsupNotificationsLocation2) {
        this.headsupNotificationsLocation = headsupNotificationsLocation2;
    }

    /* access modifiers changed from: package-private */
    public String getHeadsupNotificationsFont() {
        return this.headsupNotificationsFont;
    }

    /* access modifiers changed from: package-private */
    public void setHeadsupNotificationsFont(String headsupNotificationsFont2) {
        this.headsupNotificationsFont = headsupNotificationsFont2;
    }

    /* access modifiers changed from: package-private */
    public int getHeadsupNotificationsTextColor(int defaultColor) {
        return this.headsupNotificationsTextColorWasSet ? this.headsupNotificationsTextColor : defaultColor;
    }

    /* access modifiers changed from: package-private */
    public void setHeadsupNotificationsTextColor(int headsupNotificationsTextColor2) {
        this.headsupNotificationsTextColor = headsupNotificationsTextColor2;
        this.headsupNotificationsTextColorWasSet = true;
    }

    /* access modifiers changed from: package-private */
    public int getHeadsupNotificationsBackgroundColor(int defaultColor) {
        if (this.headsupNotificationsBackgroundColorWasSet) {
            return this.headsupNotificationsBackgroundColor;
        }
        return defaultColor;
    }

    /* access modifiers changed from: package-private */
    public void setHeadsupNotificationsBackgroundColor(int headsupNotificationsBackgroundColor2) {
        this.headsupNotificationsBackgroundColor = headsupNotificationsBackgroundColor2;
        this.headsupNotificationsBackgroundColorWasSet = true;
    }

    /* access modifiers changed from: package-private */
    public String getAppNameByPackageName(String packageName) {
        return this.appNames.get(packageName);
    }

    /* access modifiers changed from: package-private */
    public OemConfiguration.LayoutOrderOptions getAppsViewLayoutOption() {
        return this.appsViewLayoutOption;
    }

    /* access modifiers changed from: package-private */
    public void setAppsViewLayoutOption(OemConfiguration.LayoutOrderOptions option) {
        setAppsViewLayoutOption(option, true);
    }

    private void setAppsViewLayoutOption(OemConfiguration.LayoutOrderOptions option, boolean readFromConfigFile) {
        if (!readFromConfigFile || !this.isDataCachedInPrefs) {
            this.appsViewLayoutOption = option;
        }
    }

    /* access modifiers changed from: package-private */
    public String getPackageNameLaunchAfterBoot() {
        return this.packageNameLaunchAfterBoot;
    }

    /* access modifiers changed from: package-private */
    public void setPackageNameLaunchAfterBoot(String packageName) {
        this.packageNameLaunchAfterBoot = packageName;
    }

    /* access modifiers changed from: package-private */
    public boolean getLaunchOemUseMainIntent() {
        return this.launchOemUseMainIntent;
    }

    /* access modifiers changed from: package-private */
    public void setLaunchOemUseMainIntent(boolean launchOemUseMainIntent2) {
        this.launchOemUseMainIntent = launchOemUseMainIntent2;
    }

    /* access modifiers changed from: package-private */
    public void setLiveTvPackageInfo(String packageName, String systemChannelKey) {
        setLiveTvPackageInfo(packageName, systemChannelKey, true);
    }

    private void setLiveTvPackageInfo(String packageName, String systemChannelKey, boolean readFromConfigFile) {
        if (!readFromConfigFile || !this.isDataCachedInPrefs) {
            for (ChannelConfigurationInfo oobPackageInfo : this.channelsOutOfBoxOrdering) {
                if (oobPackageInfo.getPackageName().equals(packageName)) {
                    Log.e(TAG, "Live TV Package cannot be declared in both live channel out-of-box ordering and channels out-of-box ordering.");
                    return;
                }
            }
            this.liveTvOobPackageInfo = new ChannelConfigurationInfo.Builder().setPackageName(packageName).setSystemChannelKey(systemChannelKey).build();
        }
    }

    /* access modifiers changed from: package-private */
    public ChannelConfigurationInfo getLiveTvOobPackageInfo() {
        return this.liveTvOobPackageInfo;
    }

    /* access modifiers changed from: package-private */
    public void setLiveTvChannelOobPosition(int position) {
        setLiveTvChannelOobPosition(position, true);
    }

    private void setLiveTvChannelOobPosition(int position, boolean readFromConfigFile) {
        if (!readFromConfigFile || !this.isDataCachedInPrefs) {
            this.liveTvChannelOobPosition = position;
        }
    }

    /* access modifiers changed from: package-private */
    public int getLiveTvChannelOobPosition() {
        return this.liveTvChannelOobPosition;
    }

    /* access modifiers changed from: package-private */
    public boolean getForceLaunchPackageAfterBoot() {
        return this.forceLaunchPackageAfterBoot;
    }

    /* access modifiers changed from: package-private */
    public void setForceLaunchPackageAfterBoot(boolean force) {
        this.forceLaunchPackageAfterBoot = force;
    }

    public List<OemOutOfBoxApp> getVirtualOutOfBoxApps() {
        return this.outOfBoxApps;
    }

    /* access modifiers changed from: package-private */
    public boolean addAppToPinnedFavoriteApps(String appName, String packageName) {
        if (this.pinnedFavoriteApps.contains(packageName) || this.favoriteAppsOutOfBoxOrdering.contains(packageName) || this.pinnedFavoriteApps.size() >= 2) {
            return false;
        }
        this.pinnedFavoriteApps.add(packageName);
        this.appNames.put(packageName, appName);
        return true;
    }

    /* access modifiers changed from: package-private */
    public List<String> getPinnedFavoriteApps() {
        return this.pinnedFavoriteApps;
    }

    /* access modifiers changed from: package-private */
    public void addPackageToHiddenUninstallList(String packageName) {
        if (!this.hiddenUninstallPackageList.contains(packageName)) {
            this.hiddenUninstallPackageList.add(packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getHiddenUninstallPackageList() {
        return this.hiddenUninstallPackageList;
    }

    /* JADX INFO: Multiple debug info for r0v58 java.lang.String: [D('packageName' java.lang.String), D('pinnedFavoriteApps' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r2v54 java.lang.String: [D('packageName' java.lang.String), D('whitelist' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r3v21 java.lang.String: [D('input' java.lang.String), D('liveTvChannelPackageName' java.lang.String)] */
    private void readFromSharedPrefs(boolean readOnlyImmutableFields) {
        String quotas;
        String liveTvChannelPackageName = this.prefs.getString(LIVE_TV_CHANNEL_OUT_OF_BOX_PKG_NAME_PREFS_TAG, null);
        if (liveTvChannelPackageName != null) {
            setLiveTvPackageInfo(liveTvChannelPackageName, this.prefs.getString(LIVE_TV_CHANNEL_OUT_OF_BOX_SYSTEM_CHANNEL_KEY_PREFS_TAG, null), false);
            setLiveTvChannelOobPosition(this.prefs.getInt(LIVE_TV_CHANNEL_OUT_OF_BOX_POSITION_PREFS_TAG, -1), false);
        }
        String channelOrdering = this.prefs.getString(CHANNELS_OUT_OF_BOX_ORDERING_PREFS_TAG, null);
        if (!TextUtils.isEmpty(channelOrdering)) {
            List<ChannelConfigurationInfo.Builder> channelConfigurationInfoBuilderList = ChannelConfigurationInfo.fromJsonArrayString(channelOrdering);
            if (!channelConfigurationInfoBuilderList.isEmpty()) {
                for (ChannelConfigurationInfo.Builder channelConfigurationInfoBuilder : channelConfigurationInfoBuilderList) {
                    addChannelToOutOfBoxOrdering(channelConfigurationInfoBuilder, false);
                }
            } else {
                upgradeChannelOrderingFromCathode(channelOrdering);
            }
        }
        Map<String, String> packageToAppName = new HashMap<>();
        String namesMap = this.prefs.getString(APPNAME_PACKAGE_MAP_TAG, null);
        if (namesMap != null) {
            String[] namesMapArray = namesMap.split(SPLIT_DELIMITER);
            if (namesMapArray.length > 1) {
                for (int i = 0; i < namesMapArray.length; i += 2) {
                    packageToAppName.put(namesMapArray[i], namesMapArray[i + 1]);
                }
            }
        }
        String favoritesOrdering = this.prefs.getString(FAVORITE_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG, null);
        if (favoritesOrdering != null) {
            for (String packageName : favoritesOrdering.split(SPLIT_DELIMITER)) {
                String appName = (String) packageToAppName.get(packageName);
                if (appName != null) {
                    addAppToFavoriteAppsOutOfBoxOrdering(appName, packageName, false);
                }
            }
        }
        String allAppsOrdering = this.prefs.getString(ALL_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG, null);
        if (allAppsOrdering != null) {
            for (String packageName2 : allAppsOrdering.split(SPLIT_DELIMITER)) {
                String appName2 = (String) packageToAppName.get(packageName2);
                if (appName2 != null) {
                    addAppToAllAppsOutOfBoxOrdering(appName2, packageName2, false);
                }
            }
        }
        String gamesOrdering = this.prefs.getString(GAMES_OUT_OF_BOX_ORDERING_PREFS_TAG, null);
        if (gamesOrdering != null) {
            for (String packageName3 : gamesOrdering.split(SPLIT_DELIMITER)) {
                String appName3 = (String) packageToAppName.get(packageName3);
                if (appName3 != null) {
                    addAppToGamesOutOfBoxOrdering(appName3, packageName3, false);
                }
            }
        }
        String configureChannelsAppOrdering2 = this.prefs.getString(CONFIGURE_CHANNELS_APP_ORDERING_TAG, null);
        if (configureChannelsAppOrdering2 != null) {
            for (String packageName4 : configureChannelsAppOrdering2.split(SPLIT_DELIMITER)) {
                addAppToConfigureChannelsAppOrdering(packageName4, false);
            }
        }
        setAppsViewLayoutOption(OemConfiguration.LayoutOrderOptions.getLayoutOptionForRowOrder(this.prefs.getString(APPS_VIEW_LAYOUT_OPTION_TAG, null)), false);
        if (!readOnlyImmutableFields) {
            String searchIconUri2 = this.prefs.getString(SEARCH_ICON_PREFS_TAG, null);
            setSearchIconUri(searchIconUri2 != null ? Uri.parse(searchIconUri2) : null);
            if (this.prefs.contains(SEARCH_ORB_FOCUSED_COLOR_TAG)) {
                setSearchOrbFocusedColor(this.prefs.getInt(SEARCH_ORB_FOCUSED_COLOR_TAG, 0));
            }
            String inputsIconUri = this.prefs.getString(OEM_INPUTS_ICON_PREFS_TAG, null);
            setOemInputsIconUri(inputsIconUri != null ? Uri.parse(inputsIconUri) : null);
            String tunerBannerUri = this.prefs.getString(BUNDLED_TUNER_BANNER_PREFS_TAG, null);
            setBundledTunerBannerUri(tunerBannerUri != null ? Uri.parse(tunerBannerUri) : null);
            setBundledTunerTitle(this.prefs.getString(BUNDLED_TUNER_TITLE_PREFS_TAG, null));
            if (this.prefs.contains(DISABLE_DISCONNECTED_INPUTS_PREFS_TAG)) {
                setDisableDisconnectedInputs(this.prefs.getBoolean(DISABLE_DISCONNECTED_INPUTS_PREFS_TAG, true));
            }
            if (this.prefs.contains(ENABLE_INPUT_STATE_ICON_PREFS_TAG)) {
                setEnableInputStateIcon(this.prefs.getBoolean(ENABLE_INPUT_STATE_ICON_PREFS_TAG, false));
            }
            if (this.prefs.contains(APPLY_STANDARD_STYLE_TO_INPUT_STATE_ICONS_PREFS_TAG)) {
                setApplyStandardStyleToInputStateIcons(this.prefs.getBoolean(APPLY_STANDARD_STYLE_TO_INPUT_STATE_ICONS_PREFS_TAG, true));
            }
            if (this.prefs.contains(SHOW_INPUTS_PREFS_TAG)) {
                setShowInputs(this.prefs.getBoolean(SHOW_INPUTS_PREFS_TAG, false));
            }
            if (this.prefs.contains(INPUTS_LABEL_PREFS_TAG)) {
                setInputsPanelLabelOption(this.prefs.getString(INPUTS_LABEL_PREFS_TAG, null));
            }
            if (this.prefs.contains(SHOW_PHYSICAL_INPUTS_SEPARATELY_PREFS_TAG)) {
                setShowPhysicalInputsSeparately(this.prefs.getBoolean(SHOW_PHYSICAL_INPUTS_SEPARATELY_PREFS_TAG, false));
            }
            if (this.prefs.contains(USE_CUSTOM_INPUT_LIST_PREFS_TAG)) {
                setUseCustomInputList(this.prefs.getBoolean(USE_CUSTOM_INPUT_LIST_PREFS_TAG, false));
            }
            String inputOrdering = this.prefs.getString(HOME_SCREEN_INPUTS_ORDERING_PREFS_TAG, null);
            if (inputOrdering != null) {
                String[] inputOrderingArray = inputOrdering.split(SPLIT_DELIMITER);
                int length = inputOrderingArray.length;
                int i2 = 0;
                while (i2 < length) {
                    addInputToHomeScreenInputsOrdering(inputOrderingArray[i2]);
                    i2++;
                    liveTvChannelPackageName = liveTvChannelPackageName;
                }
            }
            String whitelist = this.prefs.getString(PACKAGE_NOTIFICATION_WHITELIST_PREFS_TAG, null);
            if (whitelist != null) {
                String[] whitelistArray = whitelist.split(SPLIT_DELIMITER);
                int length2 = whitelistArray.length;
                int i3 = 0;
                while (i3 < length2) {
                    addPackageToPackageNotificationWhitelist(whitelistArray[i3]);
                    i3++;
                    whitelist = whitelist;
                }
            }
            String quotas2 = this.prefs.getString(APP_CHANNEL_QUOTA_PREFS_TAG, null);
            if (quotas2 != null) {
                String[] quotasArray = quotas2.split(SPLIT_DELIMITER);
                if (quotasArray.length > 1) {
                    int i4 = 0;
                    while (i4 < quotasArray.length) {
                        try {
                            quotas = quotas2;
                            try {
                                addAppToAppChannelQuota(quotasArray[i4], Integer.decode(quotasArray[i4 + 1]).intValue());
                            } catch (NumberFormatException e) {
                                e = e;
                            }
                        } catch (NumberFormatException e2) {
                            e = e2;
                            quotas = quotas2;
                            String valueOf = String.valueOf(quotasArray[i4 + 1]);
                            Log.e(TAG, valueOf.length() != 0 ? "Bad quota number: ".concat(valueOf) : "Bad quota number: ");
                            i4 += 2;
                            quotas2 = quotas;
                        }
                        i4 += 2;
                        quotas2 = quotas;
                    }
                }
            }
            setHeadsupNotificationsFont(this.prefs.getString(HEADSUP_NOTIFICATIONS_FONT_PREFS_TAG, null));
            if (this.prefs.contains(HEADSUP_NOTIFICATIONS_TEXT_COLOR_PREFS_TAG)) {
                setHeadsupNotificationsTextColor(this.prefs.getInt(HEADSUP_NOTIFICATIONS_TEXT_COLOR_PREFS_TAG, 0));
            }
            if (this.prefs.contains(HEADSUP_NOTIFICATIONS_BACKGROUND_COLOR_PREFS_TAG)) {
                setHeadsupNotificationsBackgroundColor(this.prefs.getInt(HEADSUP_NOTIFICATIONS_BACKGROUND_COLOR_PREFS_TAG, 0));
            }
            setHeadsupNotificationsLocation(this.prefs.getString(HEADSUP_NOTIFICATIONS_LOCATION_PREFS_TAG, null));
            if (this.prefs.contains(FORCE_LAUNCH_PACKAGE_AFTER_BOOT_TAG)) {
                setForceLaunchPackageAfterBoot(this.prefs.getBoolean(FORCE_LAUNCH_PACKAGE_AFTER_BOOT_TAG, false));
            }
            setPackageNameLaunchAfterBoot(this.prefs.getString(PACKAGE_NAME_LAUNCH_AFTER_BOOT_TAG, null));
            int i5 = 0;
            setLaunchOemUseMainIntent(this.prefs.getBoolean(LAUNCH_OEM_USE_MAIN_INTENT_TAG, false));
            String pinnedFavoriteApps2 = this.prefs.getString(PINNED_FAVORITE_APPS_TAG, null);
            if (pinnedFavoriteApps2 != null) {
                String[] pinnedFavoriteAppsArray = pinnedFavoriteApps2.split(SPLIT_DELIMITER);
                int length3 = pinnedFavoriteAppsArray.length;
                while (i5 < length3) {
                    String pinnedFavoriteApps3 = pinnedFavoriteApps2;
                    String pinnedFavoriteApps4 = pinnedFavoriteAppsArray[i5];
                    String[] pinnedFavoriteAppsArray2 = pinnedFavoriteAppsArray;
                    String appName4 = (String) packageToAppName.get(pinnedFavoriteApps4);
                    if (appName4 != null) {
                        addAppToPinnedFavoriteApps(appName4, pinnedFavoriteApps4);
                    }
                    i5++;
                    pinnedFavoriteApps2 = pinnedFavoriteApps3;
                    pinnedFavoriteAppsArray = pinnedFavoriteAppsArray2;
                }
            }
            String hiddenUninstallList = this.prefs.getString(HIDDEN_UNINSTALL_PACKAGE_LIST_PREFS_TAG, null);
            if (hiddenUninstallList != null) {
                for (String packageName5 : hiddenUninstallList.split(SPLIT_DELIMITER)) {
                    addPackageToHiddenUninstallList(packageName5);
                }
            }
            if (this.prefs.contains(HOME_BACKGROUND_URI_PREFS_TAG)) {
                this.homeBackgroundUri = this.prefs.getString(HOME_BACKGROUND_URI_PREFS_TAG, null);
            }
            if (this.prefs.contains(SHOW_ADD_TO_WATCH_NEXT_FROM_PROGRAMS_MENU_PREFS_TAG)) {
                this.showAddToWatchNextFromProgramMenu = this.prefs.getBoolean(SHOW_ADD_TO_WATCH_NEXT_FROM_PROGRAMS_MENU_PREFS_TAG, true);
            }
            if (this.prefs.contains(SHOW_REMOVE_PROGRAM_FROM_PROGRAMS_MENU_PREFS_TAG)) {
                this.showRemoveProgramFromProgramMenu = this.prefs.getBoolean(SHOW_REMOVE_PROGRAM_FROM_PROGRAMS_MENU_PREFS_TAG, true);
            }
            if (this.prefs.contains(WATCH_NEXT_CHANNEL_ENABLED_BY_DEFAULT)) {
                this.watchNextChannelEnabledByDefault = this.prefs.getBoolean(WATCH_NEXT_CHANNEL_ENABLED_BY_DEFAULT, true);
            }
            if (this.prefs.contains(WATCH_NEXT_CHANNEL_AUTO_HIDE_ENABLED)) {
                this.watchNextChannelAutoHideEnabled = this.prefs.getBoolean(WATCH_NEXT_CHANNEL_AUTO_HIDE_ENABLED, true);
            }
        }
    }

    private void upgradeChannelOrderingFromCathode(String channelOrdering) {
        String systemChannelKey;
        String packageName;
        for (String channelInfo : channelOrdering.split(SPLIT_DELIMITER)) {
            int colonIndex = channelInfo.indexOf(":");
            if (colonIndex != -1) {
                packageName = channelInfo.substring(0, colonIndex);
                systemChannelKey = channelInfo.substring(colonIndex + 1);
            } else {
                packageName = channelInfo;
                systemChannelKey = null;
            }
            addChannelToOutOfBoxOrdering(new ChannelConfigurationInfo.Builder().setPackageName(packageName).setSystemChannelKey(systemChannelKey), false);
        }
        this.prefs.edit().putString(CHANNELS_OUT_OF_BOX_ORDERING_PREFS_TAG, ChannelConfigurationInfo.toJsonArray(this.channelsOutOfBoxOrdering).toString()).apply();
    }

    /* access modifiers changed from: private */
    public void saveToSharedPrefs() {
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.clear();
        editor.putLong(TIME_STAMP_PREFS_TAG, System.currentTimeMillis());
        Uri uri = this.searchIconUri;
        if (uri != null) {
            editor.putString(SEARCH_ICON_PREFS_TAG, uri.toString());
        }
        if (this.searchOrbFocusedColorWasSet) {
            editor.putInt(SEARCH_ORB_FOCUSED_COLOR_TAG, this.searchOrbFocusedColor);
        }
        Uri uri2 = this.oemInputsIconUri;
        if (uri2 != null) {
            editor.putString(OEM_INPUTS_ICON_PREFS_TAG, uri2.toString());
        }
        Uri uri3 = this.bundledTunerBannerUri;
        if (uri3 != null) {
            editor.putString(BUNDLED_TUNER_BANNER_PREFS_TAG, uri3.toString());
        }
        editor.putString(BUNDLED_TUNER_TITLE_PREFS_TAG, this.bundledTunerTitle);
        editor.putBoolean(DISABLE_DISCONNECTED_INPUTS_PREFS_TAG, this.disableDisconnectedInputs);
        editor.putBoolean(ENABLE_INPUT_STATE_ICON_PREFS_TAG, this.enableInputStateIcon);
        editor.putBoolean(SHOW_INPUTS_PREFS_TAG, this.showInputs);
        editor.putString(INPUTS_LABEL_PREFS_TAG, this.inputsPanelLabelOption);
        editor.putBoolean(SHOW_PHYSICAL_INPUTS_SEPARATELY_PREFS_TAG, this.showPhysicalInputsSeparately);
        editor.putBoolean(USE_CUSTOM_INPUT_LIST_PREFS_TAG, this.useCustomInputList);
        storeListInSharedPrefs(this.homeScreenInputsOrdering, HOME_SCREEN_INPUTS_ORDERING_PREFS_TAG, editor);
        storeListInSharedPrefs(this.packageNotificationWhitelist, PACKAGE_NOTIFICATION_WHITELIST_PREFS_TAG, editor);
        ChannelConfigurationInfo channelConfigurationInfo = this.liveTvOobPackageInfo;
        if (channelConfigurationInfo != null) {
            editor.putString(LIVE_TV_CHANNEL_OUT_OF_BOX_PKG_NAME_PREFS_TAG, channelConfigurationInfo.getPackageName());
            editor.putString(LIVE_TV_CHANNEL_OUT_OF_BOX_SYSTEM_CHANNEL_KEY_PREFS_TAG, this.liveTvOobPackageInfo.getSystemChannelKey());
            editor.putInt(LIVE_TV_CHANNEL_OUT_OF_BOX_POSITION_PREFS_TAG, this.liveTvChannelOobPosition);
        }
        editor.putString(CHANNELS_OUT_OF_BOX_ORDERING_PREFS_TAG, ChannelConfigurationInfo.toJsonArray(this.channelsOutOfBoxOrdering).toString());
        this.hasSeenSponsoredChannelsSoFar = this.hasSeenSponsoredChannelsSoFar || this.hasSeenSponsoredChannelsForCurrentConfig;
        editor.putBoolean(HAS_SEEN_SPONSORED_CHANNELS_TAG, this.hasSeenSponsoredChannelsSoFar);
        if (!this.appNames.isEmpty()) {
            StringBuilder appsString = new StringBuilder(128);
            for (Map.Entry<String, String> entry : this.appNames.entrySet()) {
                appsString.append((String) entry.getKey());
                appsString.append(SEPARATOR);
                appsString.append((String) entry.getValue());
                appsString.append(SEPARATOR);
            }
            editor.putString(APPNAME_PACKAGE_MAP_TAG, appsString.toString());
        }
        storeListInSharedPrefs(this.favoriteAppsOutOfBoxOrdering, FAVORITE_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG, editor);
        storeListInSharedPrefs(this.allAppsOutOfBoxOrdering, ALL_APPS_OUT_OF_BOX_ORDERING_PREFS_TAG, editor);
        storeListInSharedPrefs(this.gamesOutOfBoxOrdering, GAMES_OUT_OF_BOX_ORDERING_PREFS_TAG, editor);
        storeListInSharedPrefs(this.configureChannelsAppOrdering, CONFIGURE_CHANNELS_APP_ORDERING_TAG, editor);
        storeListInSharedPrefs(this.pinnedFavoriteApps, PINNED_FAVORITE_APPS_TAG, editor);
        storeListInSharedPrefs(this.hiddenUninstallPackageList, HIDDEN_UNINSTALL_PACKAGE_LIST_PREFS_TAG, editor);
        if (!this.appChannelQuota.isEmpty()) {
            StringBuilder quotasString = new StringBuilder(128);
            for (Map.Entry<String, Integer> entry2 : this.appChannelQuota.entrySet()) {
                quotasString.append((String) entry2.getKey());
                quotasString.append(SEPARATOR);
                quotasString.append(entry2.getValue());
                quotasString.append(SEPARATOR);
            }
            editor.putString(APP_CHANNEL_QUOTA_PREFS_TAG, quotasString.toString());
        }
        editor.putString(HEADSUP_NOTIFICATIONS_FONT_PREFS_TAG, this.headsupNotificationsFont);
        if (this.headsupNotificationsTextColorWasSet) {
            editor.putInt(HEADSUP_NOTIFICATIONS_TEXT_COLOR_PREFS_TAG, this.headsupNotificationsTextColor);
        }
        if (this.headsupNotificationsBackgroundColorWasSet) {
            editor.putInt(HEADSUP_NOTIFICATIONS_BACKGROUND_COLOR_PREFS_TAG, this.headsupNotificationsBackgroundColor);
        }
        editor.putString(HEADSUP_NOTIFICATIONS_LOCATION_PREFS_TAG, this.headsupNotificationsLocation);
        OemConfiguration.LayoutOrderOptions layoutOrderOptions = this.appsViewLayoutOption;
        if (layoutOrderOptions != null) {
            editor.putString(APPS_VIEW_LAYOUT_OPTION_TAG, layoutOrderOptions.getRowOrder());
        }
        if (this.forceLaunchPackageAfterBoot) {
            editor.putBoolean(FORCE_LAUNCH_PACKAGE_AFTER_BOOT_TAG, true);
        }
        String str = this.packageNameLaunchAfterBoot;
        if (str != null) {
            editor.putString(PACKAGE_NAME_LAUNCH_AFTER_BOOT_TAG, str);
        }
        editor.putBoolean(LAUNCH_OEM_USE_MAIN_INTENT_TAG, this.launchOemUseMainIntent);
        editor.putString(HOME_BACKGROUND_URI_PREFS_TAG, this.homeBackgroundUri);
        editor.putBoolean(SHOW_ADD_TO_WATCH_NEXT_FROM_PROGRAMS_MENU_PREFS_TAG, this.showAddToWatchNextFromProgramMenu);
        editor.putBoolean(SHOW_REMOVE_PROGRAM_FROM_PROGRAMS_MENU_PREFS_TAG, this.showRemoveProgramFromProgramMenu);
        editor.putBoolean(WATCH_NEXT_CHANNEL_ENABLED_BY_DEFAULT, this.watchNextChannelEnabledByDefault);
        editor.putBoolean(WATCH_NEXT_CHANNEL_AUTO_HIDE_ENABLED, this.watchNextChannelAutoHideEnabled);
        editor.putInt(OEM_CONFIGURATION_PACKAGE_VERSION_TAG, this.configurationPackageVersion);
        editor.apply();
    }

    private static void storeListInSharedPrefs(Collection<String> data, String tag, SharedPreferences.Editor editor) {
        if (!data.isEmpty()) {
            StringBuilder dataString = new StringBuilder(128);
            for (String item : data) {
                dataString.append(item);
                dataString.append(SEPARATOR);
            }
            editor.putString(tag, dataString.toString());
        }
    }

    private static class OemConfigurationDataLoadingTask extends AsyncTask<Void, Void, InputStream> {
        private final Context context;
        private final OemConfigurationData oemConfigurationData;

        OemConfigurationDataLoadingTask(OemConfigurationData oemConfigurationData2, Context context2) {
            this.oemConfigurationData = oemConfigurationData2;
            this.context = context2.getApplicationContext();
        }

        /* access modifiers changed from: protected */
        public InputStream doInBackground(Void... params) {
            SharedPreferences prefs = this.context.getSharedPreferences(OemConfigurationData.OEM_CONFIG_SHARED_PREFS, 0);
            int queryCount = prefs.getInt(OemConfigurationData.CONTENT_PROVIDER_QUERY_COUNT_KEY, 0);
            if (queryCount < 3) {
                if (TestUtils.isRunningInTest()) {
                    prefs.edit().putInt(OemConfigurationData.CONTENT_PROVIDER_QUERY_COUNT_KEY, queryCount + 1).apply();
                } else {
                    prefs.edit().putInt(OemConfigurationData.CONTENT_PROVIDER_QUERY_COUNT_KEY, queryCount + 1).commit();
                }
                try {
                    return this.context.getContentResolver().openInputStream(PartnerCustomizationContract.OEM_CONFIGURATION_URI);
                } catch (Exception e) {
                    String valueOf = String.valueOf(PartnerCustomizationContract.OEM_CONFIGURATION_URI);
                    StringBuilder sb = new StringBuilder(valueOf.length() + 14);
                    sb.append("Error reading ");
                    sb.append(valueOf);
                    Log.e(OemConfigurationData.TAG, sb.toString(), e);
                    return null;
                }
            } else {
                String valueOf2 = String.valueOf(PartnerCustomizationContract.OEM_CONFIGURATION_URI);
                StringBuilder sb2 = new StringBuilder(valueOf2.length() + 68);
                sb2.append("Error reading ");
                sb2.append(valueOf2);
                sb2.append(". Queried the content provider unsuccessfully ");
                sb2.append(3);
                sb2.append(" times.");
                Log.e(OemConfigurationData.TAG, sb2.toString());
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(InputStream configurationFile) {
            super.onPostExecute((Object) configurationFile);
            if (configurationFile != null) {
                if (new OemConfigurationXmlParser(configurationFile, this.oemConfigurationData, OemUtils.isOperatorTierDevice(this.context), OemUtils.isDynamicConfigDevice(this.context)).parse()) {
                    this.oemConfigurationData.saveToSharedPrefs();
                }
                try {
                    configurationFile.close();
                } catch (IOException e) {
                    String valueOf = String.valueOf(PartnerCustomizationContract.OEM_CONFIGURATION_URI);
                    StringBuilder sb = new StringBuilder(valueOf.length() + 14);
                    sb.append("Error closing ");
                    sb.append(valueOf);
                    Log.e(OemConfigurationData.TAG, sb.toString(), e);
                }
            } else {
                String valueOf2 = String.valueOf(PartnerCustomizationContract.OEM_CONFIGURATION_URI);
                StringBuilder sb2 = new StringBuilder(valueOf2.length() + 14);
                sb2.append("Error reading ");
                sb2.append(valueOf2);
                Log.e(OemConfigurationData.TAG, sb2.toString());
                this.oemConfigurationData.saveToSharedPrefs();
            }
            this.oemConfigurationData.onDataLoaded();
        }
    }
}
