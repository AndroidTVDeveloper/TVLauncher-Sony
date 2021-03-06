package com.google.android.libraries.gcoreclient.common.api;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.p001v4.app.FragmentActivity;
import android.view.View;
import com.google.android.libraries.gcoreclient.common.GcoreConnectionResult;
import com.google.android.libraries.gcoreclient.common.api.GcoreApi;
import java.util.concurrent.TimeUnit;

@Deprecated
public interface GcoreGoogleApiClient {

    interface Builder {
        Builder addApi(GcoreApi<? extends GcoreApi.GcoreApiOptions.GcoreNotRequiredOptions> gcoreApi);

        Builder addApi(GcoreApi<? extends GcoreApi.GcoreApiOptions.GcoreHasOptions> gcoreApi, GcoreApi.GcoreApiOptions.GcoreHasOptions gcoreHasOptions);

        Builder addConnectionCallbacks(GcoreConnectionCallbacks gcoreConnectionCallbacks);

        Builder addOnConnectionFailedListener(GcoreOnConnectionFailedListener gcoreOnConnectionFailedListener);

        Builder addScope(GcoreScope gcoreScope);

        GcoreGoogleApiClient build();

        Builder enableAutoManage(FragmentActivity fragmentActivity, int i, GcoreOnConnectionFailedListener gcoreOnConnectionFailedListener);

        Builder enableSignInClientDisconnectFix();

        Builder setAccount(Account account);

        Builder setAccountName(String str);

        Builder setGravityForPopups(int i);

        Builder setHandler(Handler handler);

        Builder setViewForPopups(View view);

        Builder useDefaultAccount();
    }

    interface BuilderFactory {
        Builder newBuilder(Context context);
    }

    interface GcoreConnectionCallbacks {
        int CAUSE_NETWORK_LOST = 2;
        int CAUSE_SERVICE_DISCONNECTED = 1;

        void onConnected(Bundle bundle);

        void onConnectionSuspended(int i);
    }

    interface GcoreOnConnectionFailedListener {
        void onConnectionFailed(GcoreConnectionResult gcoreConnectionResult);
    }

    GcoreConnectionResult blockingConnect();

    GcoreConnectionResult blockingConnect(long j, TimeUnit timeUnit);

    void connect();

    void disconnect();

    Context getContext();

    boolean isConnected();

    boolean isConnecting();

    boolean isConnectionCallbacksRegistered(GcoreConnectionCallbacks gcoreConnectionCallbacks);

    boolean isConnectionFailedListenerRegistered(GcoreOnConnectionFailedListener gcoreOnConnectionFailedListener);

    void reconnect();

    void registerConnectionCallbacks(GcoreConnectionCallbacks gcoreConnectionCallbacks);

    void registerConnectionFailedListener(GcoreOnConnectionFailedListener gcoreOnConnectionFailedListener);

    void stopAutoManage(FragmentActivity fragmentActivity);

    void unregisterConnectionCallbacks(GcoreConnectionCallbacks gcoreConnectionCallbacks);

    void unregisterConnectionFailedListener(GcoreOnConnectionFailedListener gcoreOnConnectionFailedListener);
}
