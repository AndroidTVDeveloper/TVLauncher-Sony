package com.google.android.tvlauncher.settings;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.p001v4.content.ContextCompat;
import android.util.Pair;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import java.util.ArrayList;
import java.util.Iterator;

public class FavoriteLaunchItemsSelectionFragment extends LeanbackPreferenceFragment implements LaunchItemsManager.AppsViewChangeListener, Preference.OnPreferenceClickListener {
    private LaunchItemsManager launchItemsManager;
    private int maxIconHeight;
    private int maxIconWidth;
    private Drawable placeholderBanner;
    private RequestOptions requestOptions;

    public static FavoriteLaunchItemsSelectionFragment newInstance() {
        return new FavoriteLaunchItemsSelectionFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        this.launchItemsManager = LaunchItemsManagerProvider.getInstance(context);
        this.maxIconHeight = context.getResources().getDimensionPixelSize(C1167R.dimen.favorite_preference_icon_height);
        this.maxIconWidth = context.getResources().getDimensionPixelSize(C1167R.dimen.favorite_preference_icon_width);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(context, C1167R.color.app_banner_background_color));
        this.requestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().placeholder(this.placeholderBanner)).error(this.placeholderBanner)).diskCacheStrategy(DiskCacheStrategy.NONE)).transform(new AddBackgroundColorTransformation(getContext().getColor(C1167R.color.app_banner_background_color), true));
    }

    public void onStart() {
        super.onStart();
        this.launchItemsManager.registerAppsViewChangeListener(this);
        this.launchItemsManager.refreshLaunchItems();
    }

    public void onStop() {
        super.onStop();
        this.launchItemsManager.unregisterAppsViewChangeListener(this);
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext());
        screen.setTitle(C1167R.string.select_favorite_apps_title);
        setPreferenceScreen(screen);
        screen.setOrderingAsAdded(false);
    }

    public void onLaunchItemsLoaded() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        for (LaunchItem item : this.launchItemsManager.getAllNonFavoriteInstalledLaunchItems()) {
            addPreference(screen, item, new FavoriteLaunchItemPreference(getPreferenceManager().getContext()));
        }
        setPreferenceScreen(screen);
    }

    public void onLaunchItemsAddedOrUpdated(ArrayList<LaunchItem> addedOrUpdatedItems) {
        PreferenceScreen screen = getPreferenceScreen();
        Iterator<LaunchItem> it = addedOrUpdatedItems.iterator();
        while (it.hasNext()) {
            LaunchItem item = it.next();
            if (!this.launchItemsManager.isFavorite(item) && !item.isInstalling()) {
                Preference preference = getPreferenceManager().findPreference(item.getPackageName());
                if (preference == null) {
                    preference = new FavoriteLaunchItemPreference(getPreferenceManager().getContext());
                }
                addPreference(screen, item, preference);
            }
        }
        setPreferenceScreen(screen);
    }

    public void onLaunchItemsRemoved(ArrayList<LaunchItem> removedItems) {
        PreferenceScreen screen = getPreferenceScreen();
        Iterator<LaunchItem> it = removedItems.iterator();
        while (it.hasNext()) {
            Preference preference = getPreferenceManager().findPreference(it.next().getPackageName());
            if (preference != null) {
                screen.removePreference(preference);
            }
        }
        setPreferenceScreen(screen);
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> arrayList, boolean isGameItems, Pair<Integer, Integer> pair) {
    }

    public boolean onPreferenceClick(Preference preference) {
        this.launchItemsManager.addToFavorites(preference.getKey());
        getActivity().finish();
        return false;
    }

    private void addPreference(PreferenceScreen screen, LaunchItem item, final Preference preference) {
        RequestBuilder<Drawable> builder;
        preference.setKey(item.getPackageName());
        preference.setTitle(item.getLabel());
        preference.setOnPreferenceClickListener(this);
        SimpleTarget<Drawable> iconDownloadTarget = new SimpleTarget<Drawable>(this, this.maxIconWidth, this.maxIconHeight) {
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
        };
        RequestManager requestManager = Glide.with(getContext());
        if (item.getBannerUri() != null) {
            builder = requestManager.load(item.getBannerUri());
        } else {
            builder = requestManager.load((Object) new LaunchItemImageDataSource(item, PackageImageDataSource.ImageType.BANNER, this.launchItemsManager.getCurrentLocale()));
        }
        builder.apply((BaseRequestOptions<?>) this.requestOptions).into(iconDownloadTarget);
        screen.addPreference(preference);
    }
}
