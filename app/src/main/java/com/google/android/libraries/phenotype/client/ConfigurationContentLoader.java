package com.google.android.libraries.phenotype.client;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.StrictMode;
import android.support.p001v4.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationContentLoader implements FlagLoader {
    static final int ARRAYMAP_THRESHOLD = 256;
    public static final String[] COLUMNS = {"key", "value"};
    private static final String TAG = "ConfigurationContentLoader";
    private static final Map<Uri, ConfigurationContentLoader> loadersByUri = new ArrayMap();
    private final Object cacheLock = new Object();
    private volatile Map<String, String> cachedFlags;
    private final List<ConfigurationUpdatedListener> listeners = new ArrayList();
    private final ContentObserver observer = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            ConfigurationContentLoader.this.invalidateCache();
        }
    };
    private final ContentResolver resolver;
    private final Uri uri;

    private ConfigurationContentLoader(ContentResolver resolver2, Uri uri2) {
        this.resolver = resolver2;
        this.uri = uri2;
        resolver2.registerContentObserver(uri2, false, this.observer);
    }

    public static void invalidateAllCaches() {
        synchronized (ConfigurationContentLoader.class) {
            for (ConfigurationContentLoader loader : loadersByUri.values()) {
                loader.invalidateCache();
            }
        }
    }

    public static ConfigurationContentLoader getLoader(ContentResolver resolver2, Uri uri2) {
        ConfigurationContentLoader loader;
        synchronized (ConfigurationContentLoader.class) {
            loader = loadersByUri.get(uri2);
            if (loader == null) {
                try {
                    loader = new ConfigurationContentLoader(resolver2, uri2);
                    loadersByUri.put(uri2, loader);
                } catch (SecurityException e) {
                }
            }
        }
        return loader;
    }

    public Map<String, String> getFlags() {
        Map<String, String> flags = this.cachedFlags;
        if (flags == null) {
            synchronized (this.cacheLock) {
                flags = this.cachedFlags;
                if (flags == null) {
                    flags = readFlagsFromContentProvider();
                    this.cachedFlags = flags;
                }
            }
        }
        return flags != null ? flags : Collections.emptyMap();
    }

    public String getFlag(String flagName) {
        return getFlags().get(flagName);
    }

    public void invalidateCache() {
        synchronized (this.cacheLock) {
            this.cachedFlags = null;
            PhenotypeFlag.invalidateProcessCache();
        }
        notifyConfigurationUpdatedListeners();
    }

    public static void invalidateCache(Uri uri2) {
        synchronized (ConfigurationContentLoader.class) {
            ConfigurationContentLoader loader = loadersByUri.get(uri2);
            if (loader != null) {
                loader.invalidateCache();
            }
        }
    }

    public void addConfigurationUpdatedListener(ConfigurationUpdatedListener listener) {
        synchronized (this) {
            this.listeners.add(listener);
        }
    }

    public void removeConfigurationUpdatedListener(ConfigurationUpdatedListener listener) {
        synchronized (this) {
            this.listeners.remove(listener);
        }
    }

    private Map<String, String> readFlagsFromContentProvider() {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            return (Map) FlagLoader$$CC.executeBinderAware$$STATIC$$(new ConfigurationContentLoader$$Lambda$0(this));
        } catch (SQLiteException | IllegalStateException | SecurityException e) {
            Log.e(TAG, "PhenotypeFlag unable to load ContentProvider, using default values");
            return null;
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ Map lambda$readFlagsFromContentProvider$0$ConfigurationContentLoader() {
        Map<String, String> flags;
        Cursor cursor = this.resolver.query(this.uri, COLUMNS, null, null, null);
        if (cursor == null) {
            return Collections.emptyMap();
        }
        try {
            int count = cursor.getCount();
            if (count == 0) {
                return Collections.emptyMap();
            }
            if (count <= 256) {
                flags = new ArrayMap<>(count);
            } else {
                flags = new HashMap<>(count, 1.0f);
            }
            while (cursor.moveToNext()) {
                flags.put(cursor.getString(0), cursor.getString(1));
            }
            cursor.close();
            return flags;
        } finally {
            cursor.close();
        }
    }

    private void notifyConfigurationUpdatedListeners() {
        synchronized (this) {
            for (ConfigurationUpdatedListener listener : this.listeners) {
                listener.onConfigurationUpdated();
            }
        }
    }

    static synchronized void clearLoaderMap() {
        synchronized (ConfigurationContentLoader.class) {
            for (ConfigurationContentLoader loader : loadersByUri.values()) {
                loader.resolver.unregisterContentObserver(loader.observer);
            }
            loadersByUri.clear();
        }
    }
}
