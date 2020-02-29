package com.google.android.libraries.phenotype.client;

import android.content.Context;
import android.database.ContentObserver;
import android.support.p001v4.content.PermissionChecker;
import android.util.Log;
import com.google.android.gsf.Gservices;
import javax.annotation.Nullable;

final class GservicesLoader implements FlagLoader {
    private static final String TAG = "GservicesLoader";
    private static GservicesLoader loader;
    @Nullable
    private final Context context;
    @Nullable
    private final ContentObserver observer;

    static GservicesLoader getLoader(Context context2) {
        GservicesLoader gservicesLoader;
        synchronized (GservicesLoader.class) {
            if (loader == null) {
                loader = PermissionChecker.checkSelfPermission(context2, Gservices.PERMISSION_READ_GSERVICES) == 0 ? new GservicesLoader(context2) : new GservicesLoader();
            }
            gservicesLoader = loader;
        }
        return gservicesLoader;
    }

    private GservicesLoader(Context context2) {
        this.context = context2;
        this.observer = new ContentObserver(this, null) {
            public void onChange(boolean selfChange) {
                PhenotypeFlag.invalidateProcessCache();
            }
        };
        context2.getContentResolver().registerContentObserver(Gservices.CONTENT_URI, true, this.observer);
    }

    private GservicesLoader() {
        this.context = null;
        this.observer = null;
    }

    public String getFlag(String flagName) {
        if (this.context == null) {
            return null;
        }
        try {
            return (String) FlagLoader$$CC.executeBinderAware$$STATIC$$(new GservicesLoader$$Lambda$0(this, flagName));
        } catch (IllegalStateException | SecurityException e) {
            String valueOf = String.valueOf(flagName);
            Log.e(TAG, valueOf.length() != 0 ? "Unable to read GServices for: ".concat(valueOf) : "Unable to read GServices for: ", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ String lambda$getFlag$0$GservicesLoader(String flagName) {
        return Gservices.getString(this.context.getContentResolver(), flagName, null);
    }

    /* access modifiers changed from: package-private */
    public boolean getBooleanFlagOrFalse(String flagName) {
        String flagValue = getFlag(flagName);
        return flagValue != null && Gservices.TRUE_PATTERN.matcher(flagValue).matches();
    }

    static synchronized void clearLoader() {
        synchronized (GservicesLoader.class) {
            if (!(loader == null || loader.context == null || loader.observer == null)) {
                loader.context.getContentResolver().unregisterContentObserver(loader.observer);
            }
            loader = null;
        }
    }
}
