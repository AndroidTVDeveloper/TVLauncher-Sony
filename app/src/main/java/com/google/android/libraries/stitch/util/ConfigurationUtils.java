package com.google.android.libraries.stitch.util;

import android.content.res.Configuration;
import android.os.Build;

public final class ConfigurationUtils {
    private static final int TABLET_MIN_DPS = 600;

    public static boolean isTablet(Configuration configuration) {
        if (Build.VERSION.SDK_INT < 13) {
            int screenSize = configuration.screenLayout & 15;
            return screenSize == 3 || screenSize == 4;
        } else return configuration.smallestScreenWidthDp >= 600;
    }

    private ConfigurationUtils() {
    }
}
