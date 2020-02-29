package com.google.android.tvlauncher.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public abstract class DataLoader<T> extends AsyncTaskLoader<T> {
    private static final String TAG = "DataLoader";
    private ContentObserver contentObserver;
    T data;
    private Uri uri;

    public abstract T loadData();

    DataLoader(Context context, Uri contentUri) {
        super(context);
        this.uri = contentUri;
    }

    public T loadInBackground() {
        return loadData();
    }

    /* access modifiers changed from: protected */
    public void onStartLoading() {
        T t = this.data;
        if (t != null) {
            deliverResult(t);
        }
        if (this.contentObserver == null && this.uri != null) {
            this.contentObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    DataLoader.this.onContentChanged();
                }

                public void onChange(boolean selfChange, Uri changeUri) {
                    onChange(selfChange);
                }
            };
            try {
                getContext().getContentResolver().registerContentObserver(this.uri, true, this.contentObserver);
            } catch (SecurityException e) {
                String valueOf = String.valueOf(this.uri);
                String message = e.getMessage();
                StringBuilder sb = new StringBuilder(valueOf.length() + 55 + String.valueOf(message).length());
                sb.append("Failed to register content observer for URI: ");
                sb.append(valueOf);
                sb.append(".\nReason: ");
                sb.append(message);
                Log.i(TAG, sb.toString());
                this.contentObserver = null;
            }
        }
        if (takeContentChanged() || this.data == null) {
            forceLoad();
        }
    }

    /* access modifiers changed from: protected */
    public void onStopLoading() {
        cancelLoad();
    }

    /* access modifiers changed from: protected */
    public void onReset() {
        onStopLoading();
        this.data = null;
        if (this.contentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(this.contentObserver);
            this.contentObserver = null;
        }
    }
}
