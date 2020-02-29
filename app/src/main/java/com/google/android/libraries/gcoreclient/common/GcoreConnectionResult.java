package com.google.android.libraries.gcoreclient.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender;

@Deprecated
public interface GcoreConnectionResult {
    int API_UNAVAILABLE = 16;
    int CANCELLED = 13;
    @Deprecated
    int DATE_INVALID = 12;
    int DEVELOPER_ERROR = 10;
    @Deprecated
    int DRIVE_EXTERNAL_STORAGE_REQUIRED = 1500;
    int INTERNAL_ERROR = 8;
    int INTERRUPTED = 15;
    int INVALID_ACCOUNT = 5;
    int LICENSE_CHECK_FAILED = 11;
    int NETWORK_ERROR = 7;
    int RESOLUTION_REQUIRED = 6;
    int RESTRICTED_PROFILE = 20;
    int SERVICE_DISABLED = 3;
    int SERVICE_INVALID = 9;
    int SERVICE_MISSING = 1;
    int SERVICE_MISSING_PERMISSION = 19;
    int SERVICE_UPDATING = 18;
    int SERVICE_VERSION_UPDATE_REQUIRED = 2;
    int SIGN_IN_FAILED = 17;
    int SIGN_IN_REQUIRED = 4;
    int SUCCESS = 0;
    int TIMEOUT = 14;

    int getErrorCode();

    PendingIntent getResolution();

    boolean hasResolution();

    boolean isSuccess();

    void startResolutionForResult(Activity activity, int i) throws IntentSender.SendIntentException;

    String toString();
}
