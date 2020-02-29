package com.google.android.libraries.social.analytics.visualelement;

import android.view.View;

public interface VisualElementLogger {
    String TAG = "VisualElementLogger";

    void record(int i, View view);

    void recordMenuAction(int i, VisualElement visualElement, View view);

    void recordSyntheticAction(int i, VisualElement visualElement, View view);
}
