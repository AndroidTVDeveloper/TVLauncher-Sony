package com.google.android.tvlauncher.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvrecommendations.shared.util.OemUtils;
import java.util.ArrayList;
import java.util.List;

public abstract class OemConfiguration {
    private static final String APPS_VIEW_LAYOUT_APPS_GAMES_OEM = "apps_games_oem";
    private static final String APPS_VIEW_LAYOUT_APPS_OEM = "apps_oem";
    private static final String APPS_VIEW_LAYOUT_APPS_OEM_GAMES = "apps_oem_games";
    private static final String APPS_VIEW_LAYOUT_GAMES_APPS_OEM = "games_apps_oem";
    private static final int DEFAULT_QUOTA = 1;
    public static final String INPUTS_PANEL_LABEL_INPUTS = "inputs";
    public static final String INPUTS_PANEL_LABEL_SOURCES = "sources";
    private static final Object lock = new Object();
    private static OemConfiguration oemConfiguration;
    protected final OemConfigurationData configuration;
    private List<OemConfigurationPackageChangeListener> configurationPackageChangeListeners = new ArrayList(1);
    protected Context context;
    private String packageName;

    public interface OemConfigurationPackageChangeListener {
        void onOemConfigurationPackageChanged();

        void onOemConfigurationPackageRemoved();
    }

    public interface OnDataLoadedListener {
        void onDataLoaded();
    }

    public abstract List<OemOutOfBoxApp> getVirtualOutOfBoxApps();

    public abstract boolean isWatchNextChannelEnabledByDefault();

    /* access modifiers changed from: package-private */
    public abstract void onOemConfigurationFetched();

    public abstract boolean shouldShowAddToWatchNextFromProgramMenu();

    public abstract boolean shouldShowRemoveProgramFromProgramMenu();

    public enum LayoutOrderOptions {
        APPS_OEM_GAMES(OemConfiguration.APPS_VIEW_LAYOUT_APPS_OEM_GAMES),
        APPS_GAMES_OEM(OemConfiguration.APPS_VIEW_LAYOUT_APPS_GAMES_OEM),
        GAMES_APPS_OEM(OemConfiguration.APPS_VIEW_LAYOUT_GAMES_APPS_OEM),
        APPS_OEM(OemConfiguration.APPS_VIEW_LAYOUT_APPS_OEM);
        
        private final String rowOrder;

        LayoutOrderOptions(String rowOrder2) {
            this.rowOrder = rowOrder2;
        }

        public String getRowOrder() {
            return this.rowOrder;
        }

        public static LayoutOrderOptions getLayoutOptionForRowOrder(String rowOrder2) {
            if (rowOrder2 == null) {
                return null;
            }
            char c = 65535;
            switch (rowOrder2.hashCode()) {
                case 315532620:
                    if (rowOrder2.equals(OemConfiguration.APPS_VIEW_LAYOUT_APPS_GAMES_OEM)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1185685002:
                    if (rowOrder2.equals(OemConfiguration.APPS_VIEW_LAYOUT_APPS_OEM)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1234273356:
                    if (rowOrder2.equals(OemConfiguration.APPS_VIEW_LAYOUT_APPS_OEM_GAMES)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1305388488:
                    if (rowOrder2.equals(OemConfiguration.APPS_VIEW_LAYOUT_GAMES_APPS_OEM)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return APPS_OEM_GAMES;
            }
            if (c == 1) {
                return APPS_GAMES_OEM;
            }
            if (c == 2) {
                return GAMES_APPS_OEM;
            }
            if (c != 3) {
                return null;
            }
            return APPS_OEM;
        }
    }

    public static OemConfiguration get(Context context2) {
        synchronized (lock) {
            if (oemConfiguration == null) {
                String packageName2 = OemUtils.getCustomizationApp(context2.getPackageManager());
                if (packageName2 != null) {
                    oemConfiguration = new FlavorSpecificOemConfiguration(context2, packageName2);
                }
                if (oemConfiguration == null) {
                    oemConfiguration = new FlavorSpecificOemConfiguration(null, null);
                    oemConfiguration.configuration.setDataLoaded(true);
                }
            }
        }
        return oemConfiguration;
    }

    private void notifyConfigurationPackageRemoved() {
        this.packageName = null;
        this.context = null;
        for (OemConfigurationPackageChangeListener listener : this.configurationPackageChangeListeners) {
            listener.onOemConfigurationPackageRemoved();
        }
    }

    public static void resetIfNecessary(String packageName2, boolean packageRemoved) {
        synchronized (lock) {
            if (oemConfiguration != null && !TextUtils.isEmpty(packageName2) && packageName2.equals(oemConfiguration.getPackageName()) && packageRemoved) {
                oemConfiguration.notifyConfigurationPackageRemoved();
            }
        }
    }

    protected OemConfiguration(Context initContext, String initPackageName) {
        this.context = initContext != null ? initContext.getApplicationContext() : null;
        this.packageName = initPackageName;
        Context context2 = this.context;
        this.configuration = new OemConfigurationData(this.context, context2 != null ? PackageUtils.getApplicationVersionCode(context2, this.packageName) : -1);
        if (this.context != null) {
            registerOnDataLoadedListener(OemConfiguration$$Lambda$0.$instance);
            if (OemUtils.isOperatorTierDevice(this.context)) {
                NotifyRefreshOemConfigurationDataJobService.schedule(this.context);
            }
        }
    }

    public void registerOnDataLoadedListener(OnDataLoadedListener listener) {
        this.configuration.registerOnDataLoadedListener(listener);
    }

    public void unregisterOnDataLoadedListener(OnDataLoadedListener listener) {
        this.configuration.unregisterOnDataLoadedListener(listener);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public boolean isDataLoaded() {
        return this.configuration.isDataLoaded();
    }

    public Uri getCustomSearchIconUri() {
        return this.configuration.getSearchIconUri();
    }

    public int getSearchOrbFocusedColor(int defaultColor) {
        return this.configuration.getSearchOrbFocusedColor(defaultColor);
    }

    public boolean shouldShowInputs() {
        return this.configuration.isShowInputs();
    }

    public String getInputsPanelLabelText(Context context2) {
        if (INPUTS_PANEL_LABEL_SOURCES.equals(this.configuration.getInputsPanelLabelOption())) {
            return context2.getString(C1167R.string.inputs_panel_label_sources);
        }
        return context2.getString(C1167R.string.inputs_panel_label_inputs);
    }

    public Uri getOemInputsIconUri() {
        return this.configuration.getOemInputsIconUri();
    }

    public boolean shouldShowPhysicalTunersSeparately() {
        return this.configuration.isShowPhysicalInputsSeparately();
    }

    public boolean shouldUseCustomInputList() {
        return this.configuration.shouldUseCustomInputList();
    }

    public boolean shouldDisableDisconnectedInputs() {
        return this.configuration.isDisableDisconnectedInputs();
    }

    public boolean getStateIconFromTVInput() {
        return this.configuration.isEnableInputStateIcon();
    }

    public boolean shouldApplyStandardStyleToInputStateIcons() {
        return this.configuration.shouldApplyStandardStyleToInputStateIcons();
    }

    public String getBundledTunerTitle() {
        return this.configuration.getBundledTunerTitle();
    }

    public String getDisconnectedInputToastText() {
        return this.configuration.getDisconnectedInputText();
    }

    public Uri getBundledTunerBannerUri() {
        return this.configuration.getBundledTunerBannerUri();
    }

    public List<String> getHomeScreenInputsOrdering() {
        return this.configuration.getHomeScreenInputsOrdering();
    }

    public String getAppsPromotionRowPackage() {
        return null;
    }

    public List<String> getOutOfBoxFavoriteAppsList() {
        return this.configuration.getFavoriteAppsOutOfBoxOrdering();
    }

    public List<String> getOutOfBoxAllAppsList() {
        return this.configuration.getAllAppsOutOfBoxOrdering();
    }

    public List<String> getOutOfBoxGamesList() {
        return this.configuration.getGamesOutOfBoxOrdering();
    }

    public List<String> getConfigureChannelsAppOrdering() {
        return this.configuration.getConfigureChannelsAppOrdering();
    }

    public String getAppNameByPackageName(String packageName2) {
        return this.configuration.getAppNameByPackageName(packageName2);
    }

    public List<ChannelConfigurationInfo> getOutOfBoxChannelsList() {
        return this.configuration.getChannelsOutOfBoxOrdering();
    }

    public ChannelConfigurationInfo getLiveTvOobPackageInfo() {
        return this.configuration.getLiveTvOobPackageInfo();
    }

    public int getLiveTvChannelOobPosition() {
        return this.configuration.getLiveTvChannelOobPosition();
    }

    public LayoutOrderOptions getAppsViewLayoutOption() {
        return this.configuration.getAppsViewLayoutOption();
    }

    public boolean shouldForceLaunchPackageAfterBoot() {
        return this.configuration.getForceLaunchPackageAfterBoot();
    }

    public String getPackageNameLaunchAfterBoot() {
        return this.configuration.getPackageNameLaunchAfterBoot();
    }

    public boolean shouldLaunchOemUseMainIntent() {
        return this.configuration.getLaunchOemUseMainIntent();
    }

    public List<String> getPinnedFavoriteApps() {
        return this.configuration.getPinnedFavoriteApps();
    }

    public List<String> getHiddenUninstallPackageList() {
        return this.configuration.getHiddenUninstallPackageList();
    }

    public List<String> getNotificationWhitelist() {
        return this.configuration.getPackageToPackageNotificationWhitelist();
    }

    public int getChannelQuota(String packageName2) {
        int quota = this.configuration.getAppChannelQuota(packageName2);
        if (quota != -1) {
            return quota;
        }
        return 1;
    }

    public String getHeadsUpNotificationFont() {
        return this.configuration.getHeadsupNotificationsFont();
    }

    public int getHeadsUpNotificationTextColor(int defaultColor) {
        return this.configuration.getHeadsupNotificationsTextColor(defaultColor);
    }

    public int getHeadsUpNotificationBackgroundColor(int defaultColor) {
        return this.configuration.getHeadsupNotificationsBackgroundColor(defaultColor);
    }

    public int getHeadsUpNotificationLocation() {
        String gravity = this.configuration.getHeadsupNotificationsLocation();
        if (TextUtils.isEmpty(gravity)) {
            return 8388661;
        }
        return getGravity(gravity.replaceAll("\\s+", "").toLowerCase());
    }

    public ChannelConfigurationInfo getChannelInfo(String packageName2, String systemChannelKey) {
        return this.configuration.getChannelInfo(packageName2, systemChannelKey);
    }

    public boolean isWatchNextAutoHideEnabled() {
        return this.configuration.isWatchNextChannelAutoHideEnabled();
    }

    public String getHomeBackgroundUri() {
        return this.configuration.getHomeBackgroundUri();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int getGravity(String gravity) {
        char c;
        switch (gravity.hashCode()) {
            case -2128092485:
                if (gravity.equals("start|top")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1681362038:
                if (gravity.equals("bottom|end")) {
                    c = 10;
                    break;
                }
                c = 65535;
                break;
            case -1613025466:
                if (gravity.equals("bottom|center")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1606040908:
                if (gravity.equals("end|top")) {
                    c = 9;
                    break;
                }
                c = 65535;
                break;
            case -1146190628:
                if (gravity.equals("top|center")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1138690636:
                if (gravity.equals("top|end")) {
                    c = 8;
                    break;
                }
                c = 65535;
                break;
            case -868106415:
                if (gravity.equals("bottom|start")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -506054907:
                if (gravity.equals("start|bottom")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -144103316:
                if (gravity.equals("end|bottom")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -5904462:
                if (gravity.equals("center|bottom")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 948068091:
                if (gravity.equals("top|start")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1163180334:
                if (gravity.equals("center|top")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                return 8388659;
            case 2:
            case 3:
                return 8388691;
            case 4:
            case 5:
                return 49;
            case 6:
            case 7:
                return 81;
            case 8:
            case 9:
                return 8388661;
            case 10:
            case 11:
                return 8388693;
            default:
                return 8388661;
        }
    }

    public void addConfigurationPackageChangeListener(OemConfigurationPackageChangeListener listener) {
        if (listener != null) {
            this.configurationPackageChangeListeners.add(listener);
        }
    }

    public void removeConfigurationPackageChangeListener(OemConfigurationPackageChangeListener listener) {
        this.configurationPackageChangeListeners.remove(listener);
    }
}
