package com.google.android.tvlauncher.phenotype;

import com.google.android.gms.phenotype.PhenotypeFlag;
import com.google.android.gsf.GservicesKeys;

public class Flags {
    private static final PhenotypeFlag.Factory LAUNCH_TO_OEM_FLAG_FACTORY = new PhenotypeFlag.Factory(PHENOTYPE_SHARED_PREFS_NAME).withPhenotypePrefix("LaunchToOem__").withGservicePrefix(GservicesKeys.TV_LAUNCHER_PREFIX);
    static final String PHENOTYPE_SHARED_PREFS_NAME = "phenotype_flags";
    public static final PhenotypeFlag<Boolean> launchOemOnWakeFeatureFlag = LAUNCH_TO_OEM_FLAG_FACTORY.createFlag("launch_oem_app_on_wake_feature_flag", true);

    public static boolean getLaunchOemOnWakeFeatureFlag() {
        return launchOemOnWakeFeatureFlag.get().booleanValue();
    }
}
