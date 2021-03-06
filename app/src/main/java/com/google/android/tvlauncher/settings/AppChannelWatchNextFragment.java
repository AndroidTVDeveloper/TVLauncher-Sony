package com.google.android.tvlauncher.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.p001v4.content.ContextCompat;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.FragmentEventLogger;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import com.google.android.tvlauncher.home.WatchNextPrefs;
import com.google.android.tvlauncher.settings.AppModel;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.OemConfiguration;
import java.util.Collections;
import java.util.List;

public class AppChannelWatchNextFragment extends LeanbackPreferenceFragment implements AppModel.LoadAppsCallback, Preference.OnPreferenceChangeListener {
    private AppModel appModel;
    private final FragmentEventLogger eventLogger = new FragmentEventLogger(this);
    private int maxIconHeight;
    private int maxIconWidth;
    private Drawable placeholderBanner;
    private RequestOptions requestOptions;
    private SummaryPreferenceCategory sourceGroup;

    public static Fragment newInstance() {
        return new AppChannelWatchNextFragment();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean showInWatchNext = (Boolean) newValue;
        SharedPreferences prefs = getContext().getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0);
        if (preference.getKey() != null && preference.getKey().startsWith(WatchNextPrefs.WATCH_NEXT_PACKAGE_KEY_PREFIX)) {
            if (showInWatchNext.booleanValue()) {
                prefs.edit().remove(preference.getKey()).apply();
            } else {
                prefs.edit().putBoolean(preference.getKey(), false).apply();
            }
            return true;
        } else if (!WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY.equals(preference.getKey())) {
            return false;
        } else {
            prefs.edit().putBoolean(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY, showInWatchNext.booleanValue()).apply();
            preference.setTitle(showInWatchNext.booleanValue() ? C1167R.string.f142on : C1167R.string.off);
            this.sourceGroup.setEnabled(showInWatchNext.booleanValue());
            return true;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        Resources res = context.getResources();
        this.maxIconHeight = res.getDimensionPixelSize(C1167R.dimen.preference_item_banner_height);
        this.maxIconWidth = res.getDimensionPixelSize(C1167R.dimen.preference_item_banner_width);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.requestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().placeholder(this.placeholderBanner)).error(this.placeholderBanner)).diskCacheStrategy(DiskCacheStrategy.NONE)).transform(new AddBackgroundColorTransformation(getContext().getColor(C1167R.color.app_banner_background_color), true));
    }

    public void onResume() {
        super.onResume();
        this.appModel.loadApps(this);
    }

    public void onPause() {
        super.onPause();
        this.appModel.onPause();
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context preferenceContext = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(preferenceContext);
        screen.setTitle(C1167R.string.play_next_settings_panel_title);
        setPreferenceScreen(screen);
        this.sourceGroup = new SummaryPreferenceCategory(preferenceContext);
        this.sourceGroup.setTitle(C1167R.string.watch_next_sources_title);
        this.sourceGroup.setSummary(C1167R.string.watch_next_sources_summary_message);
        createWatchNextPreference();
        screen.addPreference(this.sourceGroup);
        this.appModel = new AppModel(preferenceContext);
    }

    private void createWatchNextPreference() {
        PreferenceScreen screen = getPreferenceScreen();
        SwitchPreference switchPreference = new SwitchPreference(getPreferenceManager().getContext());
        switchPreference.setKey(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY);
        switchPreference.setPersistent(false);
        boolean isWatchNextEnabled = getContext().getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0).getBoolean(WatchNextPrefs.SHOW_WATCH_NEXT_ROW_KEY, OemConfiguration.get(getContext()).isWatchNextChannelEnabledByDefault());
        switchPreference.setChecked(isWatchNextEnabled);
        switchPreference.setTitle(isWatchNextEnabled ? C1167R.string.f142on : C1167R.string.off);
        this.sourceGroup.setEnabled(isWatchNextEnabled);
        switchPreference.setOnPreferenceChangeListener(this);
        screen.addPreference(switchPreference);
    }

    public void onAppsLoaded(List<AppModel.AppInfo> applicationInfos) {
        if (isAdded()) {
            Context context = getPreferenceManager().getContext();
            this.sourceGroup.removeAll();
            if (applicationInfos != null && applicationInfos.size() > 0) {
                Collections.sort(applicationInfos);
                for (AppModel.AppInfo applicationInfo : applicationInfos) {
                    addPreference(context, applicationInfo);
                }
            }
            this.eventLogger.log(new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_MANAGE_CHANNELS));
        }
    }

    public void onAppsChanged() {
        this.appModel.loadApps(this);
    }

    private void addPreference(Context context, AppModel.AppInfo applicationInfo) {
        final AppBannerSwitchPreference preference = new AppBannerSwitchPreference(context);
        String key = WatchNextPrefs.WATCH_NEXT_PACKAGE_KEY_PREFIX.concat(applicationInfo.packageName);
        preference.setKey(key);
        preference.setTitle(applicationInfo.title);
        SharedPreferences preferences = getContext().getSharedPreferences(WatchNextPrefs.WATCH_NEXT_PREF_FILE_NAME, 0);
        if (preferences == null || !preferences.contains(key)) {
            preference.setChecked(true);
        } else {
            preference.setChecked(false);
        }
        preference.setPersistent(false);
        Glide.with(getContext()).load((Object) new PackageImageDataSource(applicationInfo.packageName, applicationInfo.resolveInfo, PackageImageDataSource.ImageType.BANNER, LaunchItemsManagerProvider.getInstance(getContext()).getCurrentLocale())).apply((BaseRequestOptions<?>) this.requestOptions).into(new SimpleTarget<Drawable>(this, this.maxIconWidth, this.maxIconHeight) {
            public /* bridge */ /* synthetic */ void onResourceReady(Object obj, Transition transition) {
                onResourceReady((Drawable) obj, (Transition<? super Drawable>) transition);
            }

            public void onResourceReady(Drawable drawable, Transition<? super Drawable> transition) {
                if (drawable != null) {
                    preference.setIcon(drawable);
                }
            }

            public void onLoadStarted(Drawable placeholder) {
                preference.setIcon(placeholder);
            }

            public void onLoadFailed(Drawable errorDrawable) {
                preference.setIcon(errorDrawable);
            }

            public void onLoadCleared(Drawable placeholder) {
                preference.setIcon(placeholder);
            }
        });
        this.sourceGroup.addPreference(preference);
        preference.setOnPreferenceChangeListener(this);
    }
}
