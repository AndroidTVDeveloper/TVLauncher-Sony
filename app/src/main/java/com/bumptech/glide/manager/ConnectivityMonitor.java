package com.bumptech.glide.manager;

public interface ConnectivityMonitor extends LifecycleListener {

    interface ConnectivityListener {
        void onConnectivityChanged(boolean z);
    }
}
