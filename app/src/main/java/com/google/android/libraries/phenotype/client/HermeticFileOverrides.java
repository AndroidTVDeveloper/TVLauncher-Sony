package com.google.android.libraries.phenotype.client;

import android.net.Uri;
import java.util.Map;
import javax.annotation.Nullable;

final class HermeticFileOverrides {
    @Nullable
    private final Map<String, Map<String, String>> map;

    HermeticFileOverrides(@Nullable Map<String, Map<String, String>> map2) {
        this.map = map2;
    }

    static HermeticFileOverrides createEmpty() {
        return new HermeticFileOverrides(null);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public String get(@Nullable Uri providerUri, @Nullable String sharedPrefsName, @Nullable String phenotypePrefix, String flagName) {
        String configPackageKey;
        String mendelFlagName;
        if (this.map == null) {
            return null;
        }
        if (providerUri != null) {
            configPackageKey = providerUri.toString();
        } else if (sharedPrefsName == null) {
            return null;
        } else {
            configPackageKey = sharedPrefsName;
        }
        Map<String, String> configPackageMap = this.map.get(configPackageKey);
        if (configPackageMap == null) {
            return null;
        }
        if (phenotypePrefix != null) {
            String valueOf = phenotypePrefix;
            String valueOf2 = String.valueOf(flagName);
            mendelFlagName = valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf;
        } else {
            mendelFlagName = flagName;
        }
        return (String) configPackageMap.get(mendelFlagName);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.map == null;
    }
}
