package com.google.android.tvlauncher.settings;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.p001v4.content.ContextCompat;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.FragmentEventLogger;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.WatchNextPrefs;
import com.google.android.tvlauncher.settings.AppModel;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.Comparator;
import java.util.List;

public class ConfigureChannelsFragment extends LeanbackPreferenceFragment implements AppModel.LoadAppsCallback, Preference.OnPreferenceChangeListener {
    private SummaryPreferenceCategory appChannelsGroup;
    Comparator<AppModel.AppInfo> appInfoComparator = new ConfigureChannelsFragment$$Lambda$0(this);
    private AppModel appModel;
    private final FragmentEventLogger eventLogger = new FragmentEventLogger(this);
    private int maxIconHeight;
    private int maxIconWidth;
    private String oemFirstApp = null;
    private RequestOptions requestOptions;
    private TvDataManager tvDataManager;
    private TvDataManager.Provider tvDataManagerProvider = TvDataManager.PROVIDER;

    /* access modifiers changed from: package-private */
    public final /* synthetic */ int lambda$new$0$ConfigureChannelsFragment(AppModel.AppInfo o1, AppModel.AppInfo o2) {
        if (o1 == null) {
            if (o2 != null) {
                return 1;
            }
            return 0;
        } else if (o2 == null) {
            return -1;
        } else {
            if (Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME.equals(o1.packageName)) {
                return 1;
            }
            if (Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME.equals(o2.packageName)) {
                return -1;
            }
            String str = this.oemFirstApp;
            if (str != null) {
                if (str.equals(o1.packageName)) {
                    return -1;
                }
                if (this.oemFirstApp.equals(o2.packageName)) {
                    return 1;
                }
            }
            if (o1.title == null) {
                if (o2.title != null) {
                    return 1;
                }
                return 0;
            } else if (o2.title == null) {
                return -1;
            } else {
                return o1.title.toString().compareToIgnoreCase(o2.title.toString());
            }
        }
    }

    public static Fragment newInstance() {
        return new ConfigureChannelsFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        this.maxIconHeight = context.getResources().getDimensionPixelSize(C1167R.dimen.preference_item_banner_height);
        this.maxIconWidth = context.getResources().getDimensionPixelSize(C1167R.dimen.preference_item_banner_width);
        Drawable placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.requestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().placeholder(placeholderBanner)).error(placeholderBanner)).diskCacheStrategy(DiskCacheStrategy.NONE)).transform(new AddBackgroundColorTransformation(getContext().getColor(C1167R.color.app_banner_background_color), true));
        this.tvDataManager = this.tvDataManagerProvider.get(context);
    }

    public void onCreatePreferences(Bundle bundle, String s) {
        Context preferenceContext = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(preferenceContext);
        screen.setTitle(C1167R.string.add_channels_title);
        setPreferenceScreen(screen);
        createWatchNextPreference();
        this.appModel = new AppModel(getPreferenceManager().getContext());
        this.appChannelsGroup = new SummaryPreferenceCategory(preferenceContext);
        this.appChannelsGroup.setTitle(C1167R.string.home_screen_channels_group_title);
        this.appChannelsGroup.setSummary(C1167R.string.home_screen_channels_group_summary);
        this.appChannelsGroup.setEnabled(true);
        screen.addPreference(this.appChannelsGroup);
        List<String> appOrdering = OemConfiguration.get(getContext()).getConfigureChannelsAppOrdering();
        if (appOrdering != null && !appOrdering.isEmpty()) {
            this.oemFirstApp = appOrdering.get(0);
        }
    }

    public void onResume() {
        super.onResume();
        this.appModel.loadApps(this);
    }

    public void onPause() {
        super.onPause();
        this.appModel.onPause();
    }

    public void onAppsLoaded(List<AppModel.AppInfo> applicationInfos) {
        if (isAdded()) {
            Context context = getPreferenceManager().getContext();
            this.appChannelsGroup.removeAll();
            if (applicationInfos != null && applicationInfos.size() > 0) {
                applicationInfos.sort(this.appInfoComparator);
                for (AppModel.AppInfo applicationInfo : applicationInfos) {
                    addPreference(context, applicationInfo);
                }
            }
            this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_MANAGE_CHANNELS));
        }
    }

    private void createWatchNextPreference() {
        PreferenceScreen screen = getPreferenceScreen();
        Preference preference = new Preference(getPreferenceManager().getContext());
        preference.setLayoutResource(C1167R.layout.watch_next_preference);
        preference.setKey(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY);
        preference.setTitle(C1167R.string.play_next_setting_title);
        preference.setSummary(C1167R.string.play_next_setting_summary);
        preference.setFragment(AppChannelWatchNextFragment.class.getName());
        preference.setPersistent(false);
        screen.addPreference(preference);
    }

    public void onAppsChanged() {
        this.appModel.loadApps(this);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: com.google.android.tvlauncher.settings.AppBannerDrillDownPreference} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: com.google.android.tvlauncher.settings.AppBannerDrillDownPreference} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v7, resolved type: com.google.android.tvlauncher.settings.AppBannerSwitchPreference} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v8, resolved type: com.google.android.tvlauncher.settings.AppBannerDrillDownPreference} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addPreference(android.content.Context r12, com.google.android.tvlauncher.settings.AppModel.AppInfo r13) {
        /*
            r11 = this;
            com.google.android.tvlauncher.model.ChannelPackage r0 = r13.channelPackage
            java.lang.String r1 = r13.packageName
            java.lang.String r2 = "sponsored.legacy"
            boolean r1 = r2.equals(r1)
            r2 = 0
            r3 = 1
            if (r1 != 0) goto L_0x0017
            int r4 = r0.getChannelCount()
            if (r4 != r3) goto L_0x0017
            r4 = 1
            goto L_0x0018
        L_0x0017:
            r4 = 0
        L_0x0018:
            if (r4 == 0) goto L_0x0053
            com.google.android.tvlauncher.settings.AppBannerSwitchPreference r5 = new com.google.android.tvlauncher.settings.AppBannerSwitchPreference
            r5.<init>(r12)
            boolean r6 = r0.isOnlyChannelRemovable()
            r5.setShowToggle(r6)
            boolean r6 = r0.isOnlyChannelBrowsable()
            r5.setChecked(r6)
            r5.setShowIcon(r3)
            r3 = r5
            boolean r6 = r0.isOnlyChannelEmpty()
            if (r6 == 0) goto L_0x003d
            int r6 = com.google.android.tvlauncher.C1167R.string.empty_channel_message
            r3.setSummary(r6)
            goto L_0x0044
        L_0x003d:
            java.lang.String r6 = r0.getOnlyChannelDisplayName()
            r3.setSummary(r6)
        L_0x0044:
            long r6 = r0.getOnlyChannelId()
            java.lang.String r6 = java.lang.Long.toString(r6)
            r3.setKey(r6)
            r3.setOnPreferenceChangeListener(r11)
            goto L_0x00a1
        L_0x0053:
            com.google.android.tvlauncher.settings.AppBannerDrillDownPreference r5 = new com.google.android.tvlauncher.settings.AppBannerDrillDownPreference
            r5.<init>(r12)
            r6 = r1 ^ 1
            r5.setShowIcon(r6)
            r6 = r5
            java.lang.String r7 = r13.packageName
            r6.setKey(r7)
            android.content.res.Resources r7 = r12.getResources()
            int r8 = com.google.android.tvlauncher.C1167R.plurals.app_channel_count
            int r9 = r0.getChannelCount()
            java.lang.Object[] r3 = new java.lang.Object[r3]
            int r10 = r0.getChannelCount()
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            r3[r2] = r10
            java.lang.String r3 = r7.getQuantityString(r8, r9, r3)
            r6.setSummary(r3)
            java.lang.Class<com.google.android.tvlauncher.settings.ConfigureChannelsAppDetailsFragment> r3 = com.google.android.tvlauncher.settings.ConfigureChannelsAppDetailsFragment.class
            java.lang.String r3 = r3.getName()
            r6.setFragment(r3)
            android.os.Bundle r3 = r6.getExtras()
            java.lang.String r7 = r13.packageName
            java.lang.String r8 = "channel_app"
            r3.putString(r8, r7)
            java.lang.CharSequence r7 = r13.title
            java.lang.String r7 = r7.toString()
            java.lang.String r8 = "app_name"
            r3.putString(r8, r7)
            r3 = r6
        L_0x00a1:
            java.lang.CharSequence r5 = r13.title
            r3.setTitle(r5)
            r3.setPersistent(r2)
            if (r1 != 0) goto L_0x00e0
            com.google.android.tvlauncher.settings.ConfigureChannelsFragment$1 r2 = new com.google.android.tvlauncher.settings.ConfigureChannelsFragment$1
            int r5 = r11.maxIconWidth
            int r6 = r11.maxIconHeight
            r2.<init>(r11, r5, r6, r3)
            android.content.Context r5 = r11.getContext()
            com.bumptech.glide.RequestManager r5 = com.bumptech.glide.Glide.with(r5)
            com.google.android.tvlauncher.appsview.data.PackageImageDataSource r6 = new com.google.android.tvlauncher.appsview.data.PackageImageDataSource
            java.lang.String r7 = r13.packageName
            android.content.pm.ResolveInfo r8 = r13.resolveInfo
            com.google.android.tvlauncher.appsview.data.PackageImageDataSource$ImageType r9 = com.google.android.tvlauncher.appsview.data.PackageImageDataSource.ImageType.BANNER
            android.content.Context r10 = r11.getContext()
            com.google.android.tvlauncher.appsview.data.LaunchItemsManager r10 = com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider.getInstance(r10)
            java.util.Locale r10 = r10.getCurrentLocale()
            r6.<init>(r7, r8, r9, r10)
            com.bumptech.glide.RequestBuilder r5 = r5.load(r6)
            com.bumptech.glide.request.RequestOptions r6 = r11.requestOptions
            com.bumptech.glide.RequestBuilder r5 = r5.apply(r6)
            r5.into(r2)
        L_0x00e0:
            com.google.android.tvlauncher.settings.SummaryPreferenceCategory r2 = r11.appChannelsGroup
            r2.addPreference(r3)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.settings.ConfigureChannelsFragment.addPreference(android.content.Context, com.google.android.tvlauncher.settings.AppModel$AppInfo):void");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean browsable = (Boolean) newValue;
        if (browsable != null) {
            this.tvDataManager.setChannelBrowsable((long) Integer.parseInt(preference.getKey()), browsable.booleanValue(), true);
        }
        return true;
    }
}
