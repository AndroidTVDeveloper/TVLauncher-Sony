package com.google.android.libraries.gcoreclient.tasks;

@Deprecated
public interface GcoreOnCompleteListener<ResultT> {
    void onComplete(GcoreTask<ResultT> gcoreTask);
}
