package com.google.android.tvlauncher.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.google.android.tvlauncher.widget.PartnerWidgetInfo;
import com.google.android.tvrecommendations.shared.util.PartnerCustomizationContract;

public class PartnerWidgetInfoLoader extends AsyncTaskLoader<PartnerWidgetInfo> {
    private static final String TAG = "PrtnrWidgetInfoLdr";
    private ContentObserver contentObserver = null;
    private PartnerWidgetInfo partnerWidgetInfo;

    public PartnerWidgetInfoLoader(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void onStartLoading() {
        PartnerWidgetInfo partnerWidgetInfo2 = this.partnerWidgetInfo;
        if (partnerWidgetInfo2 != null) {
            deliverResult(partnerWidgetInfo2);
        }
        if (this.contentObserver == null) {
            this.contentObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    PartnerWidgetInfoLoader.this.onContentChanged();
                }

                public void onChange(boolean selfChange, Uri uri) {
                    onChange(selfChange);
                }
            };
            try {
                getContext().getContentResolver().registerContentObserver(PartnerCustomizationContract.WIDGET_CONTENT_URI, true, this.contentObserver);
            } catch (SecurityException e) {
                String valueOf = String.valueOf(PartnerCustomizationContract.WIDGET_CONTENT_URI);
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
        if (takeContentChanged() || this.partnerWidgetInfo == null) {
            forceLoad();
        }
    }

    /* access modifiers changed from: protected */
    public void onReset() {
        onStopLoading();
        this.partnerWidgetInfo = null;
        if (this.contentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(this.contentObserver);
            this.contentObserver = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onStopLoading() {
        cancelLoad();
    }

    /* JADX INFO: Multiple debug info for r2v5 java.lang.String: [D('iconSource' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r3v4 java.lang.String: [D('title' java.lang.String), D('index' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x007b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x007c, code lost:
        if (r1 != null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0086, code lost:
        throw r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.google.android.tvlauncher.widget.PartnerWidgetInfo loadInBackground() {
        /*
            r10 = this;
            r0 = 0
            r10.partnerWidgetInfo = r0
            android.content.Context r0 = r10.getContext()
            android.content.ContentResolver r1 = r0.getContentResolver()     // Catch:{ Exception -> 0x008d }
            android.net.Uri r2 = com.google.android.tvrecommendations.shared.util.PartnerCustomizationContract.WIDGET_CONTENT_URI     // Catch:{ Exception -> 0x008d }
            java.lang.String[] r3 = com.google.android.tvlauncher.widget.PartnerWidgetInfo.PROJECTION     // Catch:{ Exception -> 0x008d }
            r4 = 0
            r5 = 0
            r6 = 0
            android.database.Cursor r1 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ Exception -> 0x008d }
            if (r1 == 0) goto L_0x0087
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0079 }
            if (r2 == 0) goto L_0x0087
            r2 = 0
            int r3 = r2 + 1
            java.lang.String r2 = r1.getString(r2)     // Catch:{ all -> 0x0079 }
            int r4 = r3 + 1
            java.lang.String r3 = r1.getString(r3)     // Catch:{ all -> 0x0079 }
            java.lang.String r5 = r1.getString(r4)     // Catch:{ all -> 0x0079 }
            r6 = 0
            boolean r7 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x0079 }
            if (r7 != 0) goto L_0x003d
            android.net.Uri r7 = android.net.Uri.parse(r2)     // Catch:{ all -> 0x0079 }
            r6 = r7
        L_0x003d:
            android.content.res.Resources r7 = r0.getResources()     // Catch:{ all -> 0x0079 }
            int r8 = com.google.android.tvlauncher.C1167R.dimen.top_row_button_icon_size     // Catch:{ all -> 0x0079 }
            int r7 = r7.getDimensionPixelSize(r8)     // Catch:{ all -> 0x0079 }
            if (r6 == 0) goto L_0x0087
            android.content.Context r8 = r10.getContext()     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.RequestManager r8 = com.bumptech.glide.Glide.with(r8)     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.RequestBuilder r8 = r8.asDrawable()     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.RequestBuilder r8 = r8.load(r6)     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.load.engine.DiskCacheStrategy r9 = com.bumptech.glide.load.engine.DiskCacheStrategy.NONE     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.request.RequestOptions r9 = com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf(r9)     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.RequestBuilder r8 = r8.apply(r9)     // Catch:{ all -> 0x0079 }
            com.bumptech.glide.request.FutureTarget r8 = r8.submit(r7, r7)     // Catch:{ all -> 0x0079 }
            java.lang.Object r8 = r8.get()     // Catch:{ all -> 0x0079 }
            android.graphics.drawable.Drawable r8 = (android.graphics.drawable.Drawable) r8     // Catch:{ all -> 0x0079 }
            if (r8 == 0) goto L_0x0087
            com.google.android.tvlauncher.widget.PartnerWidgetInfo r9 = new com.google.android.tvlauncher.widget.PartnerWidgetInfo     // Catch:{ all -> 0x0079 }
            r9.<init>(r8, r3, r5)     // Catch:{ all -> 0x0079 }
            r10.partnerWidgetInfo = r9     // Catch:{ all -> 0x0079 }
            goto L_0x0087
        L_0x0079:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x007b }
        L_0x007b:
            r3 = move-exception
            if (r1 == 0) goto L_0x0086
            r1.close()     // Catch:{ all -> 0x0082 }
            goto L_0x0086
        L_0x0082:
            r4 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r2, r4)     // Catch:{ Exception -> 0x008d }
        L_0x0086:
            throw r3     // Catch:{ Exception -> 0x008d }
        L_0x0087:
            if (r1 == 0) goto L_0x008c
            r1.close()     // Catch:{ Exception -> 0x008d }
        L_0x008c:
            goto L_0x0095
        L_0x008d:
            r1 = move-exception
            java.lang.String r2 = "PrtnrWidgetInfoLdr"
            java.lang.String r3 = "Exception in loadInBackground()"
            android.util.Log.e(r2, r3, r1)
        L_0x0095:
            com.google.android.tvlauncher.widget.PartnerWidgetInfo r1 = r10.partnerWidgetInfo
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.data.PartnerWidgetInfoLoader.loadInBackground():com.google.android.tvlauncher.widget.PartnerWidgetInfo");
    }
}
