package com.google.android.tvlauncher.data;

import android.content.Context;
import com.google.android.tvlauncher.home.util.SearchWidgetInfoContract;

public class TvSearchSuggestionsLoader extends DataLoader<String[]> {
    private static final String TAG = "TvSearchSuggestionsLdr";

    public TvSearchSuggestionsLoader(Context context) {
        super(context, SearchWidgetInfoContract.SUGGESTIONS_CONTENT_URI);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0044, code lost:
        if (r0 != null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        throw r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String[] loadData() {
        /*
            r7 = this;
            r0 = 0
            r7.data = r0
            android.content.Context r0 = r7.getContext()     // Catch:{ Exception -> 0x0055 }
            android.content.ContentResolver r1 = r0.getContentResolver()     // Catch:{ Exception -> 0x0055 }
            android.net.Uri r2 = com.google.android.tvlauncher.home.util.SearchWidgetInfoContract.SUGGESTIONS_CONTENT_URI     // Catch:{ Exception -> 0x0055 }
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            android.database.Cursor r0 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ Exception -> 0x0055 }
            if (r0 == 0) goto L_0x004f
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0041 }
            if (r1 == 0) goto L_0x004f
            int r1 = r0.getCount()     // Catch:{ all -> 0x0041 }
            java.lang.String r2 = "suggestion"
            int r2 = r0.getColumnIndex(r2)     // Catch:{ all -> 0x0041 }
            java.lang.String[] r3 = new java.lang.String[r1]     // Catch:{ all -> 0x0041 }
            r7.data = r3     // Catch:{ all -> 0x0041 }
            r3 = 0
        L_0x002f:
            if (r3 >= r1) goto L_0x004f
            java.lang.Object r4 = r7.data     // Catch:{ all -> 0x0041 }
            java.lang.String[] r4 = (java.lang.String[]) r4     // Catch:{ all -> 0x0041 }
            java.lang.String r5 = r0.getString(r2)     // Catch:{ all -> 0x0041 }
            r4[r3] = r5     // Catch:{ all -> 0x0041 }
            r0.moveToNext()     // Catch:{ all -> 0x0041 }
            int r3 = r3 + 1
            goto L_0x002f
        L_0x0041:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0043 }
        L_0x0043:
            r2 = move-exception
            if (r0 == 0) goto L_0x004e
            r0.close()     // Catch:{ all -> 0x004a }
            goto L_0x004e
        L_0x004a:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3)     // Catch:{ Exception -> 0x0055 }
        L_0x004e:
            throw r2     // Catch:{ Exception -> 0x0055 }
        L_0x004f:
            if (r0 == 0) goto L_0x0054
            r0.close()     // Catch:{ Exception -> 0x0055 }
        L_0x0054:
            goto L_0x005d
        L_0x0055:
            r0 = move-exception
            java.lang.String r1 = "TvSearchSuggestionsLdr"
            java.lang.String r2 = "Exception in loadInBackground()"
            android.util.Log.e(r1, r2, r0)
        L_0x005d:
            java.lang.Object r0 = r7.data
            java.lang.String[] r0 = (java.lang.String[]) r0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.data.TvSearchSuggestionsLoader.loadData():java.lang.String[]");
    }
}
