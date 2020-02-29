package com.google.android.tvlauncher;

import android.content.Context;
import com.google.android.tvlauncher.BackHomeControllerListeners

public class MainBackHomeController implements BackHomeControllerListeners.OnHomePressedListener, BackHomeControllerListeners.OnBackPressedListener, BackHomeControllerListeners.OnBackNotHandledListener {
    private static MainBackHomeController instance = null;
    private BackHomeControllerListeners.OnBackPressedListener onBackPressedListener;
    private BackHomeControllerListeners.OnHomePressedListener onHomePressedListener;

    public static MainBackHomeController getInstance() {
        if (instance == null) {
            instance = new MainBackHomeController();
        }
        return instance;
    }

    public void setOnHomePressedListener(BackHomeControllerListeners.OnHomePressedListener listener) {
        this.onHomePressedListener = listener;
    }

    public void setOnBackPressedListener(BackHomeControllerListeners.OnBackPressedListener listener) {
        this.onBackPressedListener = listener;
    }

    public void onBackNotHandled(Context c) {
    }

    public void onHomePressed(Context c) {
        BackHomeControllerListeners.OnHomePressedListener onHomePressedListener2 = this.onHomePressedListener;
        if (onHomePressedListener2 != null) {
            onHomePressedListener2.onHomePressed(c);
        }
    }

    public void onBackPressed(Context c) {
        BackHomeControllerListeners.OnBackPressedListener onBackPressedListener2 = this.onBackPressedListener;
        if (onBackPressedListener2 != null) {
            onBackPressedListener2.onBackPressed(c);
        } else {
            onBackNotHandled(c);
        }
    }
}
