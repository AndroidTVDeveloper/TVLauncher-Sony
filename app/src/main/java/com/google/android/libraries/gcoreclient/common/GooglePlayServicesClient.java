package com.google.android.libraries.gcoreclient.common;

import android.os.Bundle;

@Deprecated
public interface GooglePlayServicesClient {

    interface ConnectionCallbacks {
        void onConnected(Bundle bundle);

        void onDisconnected();
    }

    interface OnConnectionFailedListener {
        void onConnectionFailed(GcoreConnectionResult gcoreConnectionResult);
    }
}
