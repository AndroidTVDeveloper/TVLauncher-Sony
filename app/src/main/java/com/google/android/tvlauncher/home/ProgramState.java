package com.google.android.tvlauncher.home;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ProgramState {
    int CHANNEL_ACTIONS = 8;
    int CHANNEL_ACTIONS_SELECTED_CHANNEL = 9;
    int DEFAULT = 0;
    int DEFAULT_ABOVE_SELECTED_LAST_ROW = 12;
    int DEFAULT_APPS_ROW_SELECTED = 2;
    int DEFAULT_FAST_SCROLLING = 4;
    int DEFAULT_SELECTED_CHANNEL = 3;
    int DEFAULT_TOP_ROW_SELECTED = 1;
    int MOVE_CHANNEL = 10;
    int MOVE_CHANNEL_SELECTED_CHANNEL = 11;
    int ZOOMED_OUT = 5;
    int ZOOMED_OUT_SELECTED_CHANNEL = 6;
    int ZOOMED_OUT_TOP_ROW_SELECTED = 7;
}
