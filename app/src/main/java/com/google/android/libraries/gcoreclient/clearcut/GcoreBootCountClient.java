package com.google.android.libraries.gcoreclient.clearcut;

import android.content.Context;
import com.google.android.libraries.gcoreclient.tasks.GcoreTask;

@Deprecated
public interface GcoreBootCountClient {

    interface Factory {
        GcoreBootCountClient createBootCountClient(Context context);
    }

    GcoreTask<Integer> getBootCount();
}
