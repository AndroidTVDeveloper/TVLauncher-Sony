package com.google.android.libraries.gcoreclient.common;

import android.os.Bundle;

@Deprecated
public interface GooglePlayServicesClient {

    public interface ConnectionCallbacks {
        void onConnected(Bundle bundle);

        void onDisconnected();
    }

    public interface OnConnectionFailedListener {
        void onConnectionFailed(GcoreConnectionResult gcoreConnectionResult);
    }
}
