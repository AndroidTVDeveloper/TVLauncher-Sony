package com.google.android.tvlauncher.home;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface HomeAppState {
    int CHANNEL_ACTIONS = 6;
    int DEFAULT = 0;
    int DEFAULT_ABOVE_SELECTED_CHANNEL = 1;
    int DEFAULT_SELECTED_CHANNEL = 2;
    int MOVE_CHANNEL = 7;
    int ZOOMED_OUT = 3;
    int ZOOMED_OUT_SELECTED_CHANNEL = 4;
    int ZOOMED_OUT_TOP_ROW_SELECTED = 5;
}
