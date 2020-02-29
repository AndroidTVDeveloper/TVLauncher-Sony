package com.google.android.libraries.gcoreclient.tasks;

import java.util.concurrent.Executor;

@Deprecated
public interface GcoreTask<TResult> {
    GcoreTask<TResult> addOnCompleteListener(GcoreOnCompleteListener<TResult> gcoreOnCompleteListener);

    GcoreTask<TResult> addOnCompleteListener(Executor executor, GcoreOnCompleteListener<TResult> gcoreOnCompleteListener);

    GcoreTask<TResult> addOnFailureListener(GcoreOnFailureListener gcoreOnFailureListener);

    GcoreTask<TResult> addOnFailureListener(Executor executor, GcoreOnFailureListener gcoreOnFailureListener);

    GcoreTask<TResult> addOnSuccessListener(GcoreOnSuccessListener<? super TResult> gcoreOnSuccessListener);

    GcoreTask<TResult> addOnSuccessListener(Executor executor, GcoreOnSuccessListener<? super TResult> gcoreOnSuccessListener);

    Throwable getException();

    TResult getResult();

    boolean isComplete();

    boolean isSuccessful();
}
