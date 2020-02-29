package com.google.android.tvlauncher.data;

import android.content.Context;
import android.net.Uri;

public class HotwordEnabledLoader extends DataLoader<Boolean> {
    private static final Uri HOTWORD_ENABLED_CONTENT_URI = Uri.parse("content://com.google.android.katniss.search.searchapi.VoiceInteractionProvider/sharedvalue");
    private static final String TAG = "HotwordEnabledLdr";

    public HotwordEnabledLoader(Context context) {
        super(context, HOTWORD_ENABLED_CONTENT_URI);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        if (r0 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0045, code lost:
        throw r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Boolean loadData() {
        /*
            r8 = this;
            r0 = 0
            r8.data = r0
            android.content.Context r0 = r8.getContext()     // Catch:{ Exception -> 0x004c }
            android.content.ContentResolver r1 = r0.getContentResolver()     // Catch:{ Exception -> 0x004c }
            android.net.Uri r2 = com.google.android.tvlauncher.data.HotwordEnabledLoader.HOTWORD_ENABLED_CONTENT_URI     // Catch:{ Exception -> 0x004c }
            r3 = 0
            java.lang.String r4 = "key = 'is_listening_for_hotword'"
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r0 = r1.query(r2, r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x004c }
            if (r0 == 0) goto L_0x0046
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0038 }
            if (r1 == 0) goto L_0x0046
            java.lang.String r1 = "value"
            int r1 = r0.getColumnIndex(r1)     // Catch:{ all -> 0x0038 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0038 }
            r2 = 1
            if (r1 != r2) goto L_0x0030
            goto L_0x0031
        L_0x0030:
            r2 = 0
        L_0x0031:
            java.lang.Boolean r1 = java.lang.Boolean.valueOf(r2)     // Catch:{ all -> 0x0038 }
            r8.data = r1     // Catch:{ all -> 0x0038 }
            goto L_0x0046
        L_0x0038:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x003a }
        L_0x003a:
            r2 = move-exception
            if (r0 == 0) goto L_0x0045
            r0.close()     // Catch:{ all -> 0x0041 }
            goto L_0x0045
        L_0x0041:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3)     // Catch:{ Exception -> 0x004c }
        L_0x0045:
            throw r2     // Catch:{ Exception -> 0x004c }
        L_0x0046:
            if (r0 == 0) goto L_0x004b
            r0.close()     // Catch:{ Exception -> 0x004c }
        L_0x004b:
            goto L_0x0054
        L_0x004c:
            r0 = move-exception
            java.lang.String r1 = "HotwordEnabledLdr"
            java.lang.String r2 = "Exception in loadInBackground()"
            android.util.Log.e(r1, r2, r0)
        L_0x0054:
            java.lang.Object r0 = r8.data
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.data.HotwordEnabledLoader.loadData():java.lang.Boolean");
    }
}
