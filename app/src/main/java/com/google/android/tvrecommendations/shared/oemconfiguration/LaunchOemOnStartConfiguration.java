package com.google.android.tvrecommendations.shared.oemconfiguration;

public interface LaunchOemOnStartConfiguration {
    int getMinimumSleepForLaunchOnWakeSeconds();

    String getPackageNameLaunchAfterBoot();

    boolean shouldForceLaunchPackageAfterBoot();

    boolean shouldLaunchOemUseMainIntent();

    boolean shouldLaunchOnWake();
}
