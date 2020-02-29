package com.google.android.libraries.phenotype.client;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import com.google.android.libraries.directboot.DirectBootUtils;
import com.google.common.base.Optional;
import java.io.File;

final class HermeticFileOverridesReader {
    static final String OVERRIDES_DIR_NAME = "phenotype_hermetic";
    static final String OVERRIDES_FILE_NAME = "overrides.txt";
    private static final String TAG = "HermeticFileOverrides";

    HermeticFileOverridesReader() {
    }

    /* access modifiers changed from: package-private */
    public HermeticFileOverrides readFromFileIfEligible(Context context) {
        if (!isEligible(Build.TYPE, Build.TAGS, Build.HARDWARE)) {
            return HermeticFileOverrides.createEmpty();
        }
        return readFromFile(DirectBootUtils.getDeviceProtectedStorageContextOrFallback(context));
    }

    /* access modifiers changed from: package-private */
    public boolean isEligible(String buildType, String buildTags, String hardware) {
        if (!buildType.equals("eng") && !buildType.equals("userdebug")) {
            return false;
        }
        if (!hardware.equals("goldfish") && !hardware.equals("ranchu") && !hardware.equals("robolectric")) {
            return false;
        }
        if (buildTags.contains("dev-keys") || buildTags.contains("test-keys")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public HermeticFileOverrides readFromFile(Context context) {
        Optional<File> overridesFile = findOverridesFile(context);
        if (overridesFile.isPresent()) {
            return parseFromFile(overridesFile.get());
        }
        return HermeticFileOverrides.createEmpty();
    }

    private static Optional<File> findOverridesFile(Context context) {
        StrictMode.ThreadPolicy originalThreadPolicy = StrictMode.allowThreadDiskReads();
        try {
            StrictMode.allowThreadDiskWrites();
            File file = new File(context.getDir(OVERRIDES_DIR_NAME, 0), OVERRIDES_FILE_NAME);
            return file.exists() ? Optional.m82of(file) : Optional.absent();
        } catch (RuntimeException e) {
            Log.e(TAG, "no data dir", e);
            return Optional.absent();
        } finally {
            StrictMode.setThreadPolicy(originalThreadPolicy);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0099, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a2, code lost:
        throw r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static com.google.android.libraries.phenotype.client.HermeticFileOverrides parseFromFile(java.io.File r8) {
        /*
            java.io.BufferedReader r0 = new java.io.BufferedReader     // Catch:{ IOException -> 0x00a3 }
            java.io.InputStreamReader r1 = new java.io.InputStreamReader     // Catch:{ IOException -> 0x00a3 }
            java.io.FileInputStream r2 = new java.io.FileInputStream     // Catch:{ IOException -> 0x00a3 }
            r2.<init>(r8)     // Catch:{ IOException -> 0x00a3 }
            r1.<init>(r2)     // Catch:{ IOException -> 0x00a3 }
            r0.<init>(r1)     // Catch:{ IOException -> 0x00a3 }
            java.util.HashMap r1 = new java.util.HashMap     // Catch:{ all -> 0x0097 }
            r1.<init>()     // Catch:{ all -> 0x0097 }
        L_0x0014:
            java.lang.String r2 = r0.readLine()     // Catch:{ all -> 0x0097 }
            r3 = r2
            java.lang.String r4 = "HermeticFileOverrides"
            if (r2 == 0) goto L_0x006c
            java.lang.String r2 = " "
            r5 = 3
            java.lang.String[] r2 = r3.split(r2, r5)     // Catch:{ all -> 0x0097 }
            int r6 = r2.length     // Catch:{ all -> 0x0097 }
            if (r6 == r5) goto L_0x0042
            java.lang.String r5 = "Invalid: "
            java.lang.String r6 = java.lang.String.valueOf(r3)     // Catch:{ all -> 0x0097 }
            int r7 = r6.length()     // Catch:{ all -> 0x0097 }
            if (r7 == 0) goto L_0x0038
            java.lang.String r5 = r5.concat(r6)     // Catch:{ all -> 0x0097 }
            goto L_0x003e
        L_0x0038:
            java.lang.String r6 = new java.lang.String     // Catch:{ all -> 0x0097 }
            r6.<init>(r5)     // Catch:{ all -> 0x0097 }
            r5 = r6
        L_0x003e:
            android.util.Log.e(r4, r5)     // Catch:{ all -> 0x0097 }
            goto L_0x0014
        L_0x0042:
            r4 = 0
            r4 = r2[r4]     // Catch:{ all -> 0x0097 }
            r5 = 1
            r5 = r2[r5]     // Catch:{ all -> 0x0097 }
            java.lang.String r5 = android.net.Uri.decode(r5)     // Catch:{ all -> 0x0097 }
            r6 = 2
            r6 = r2[r6]     // Catch:{ all -> 0x0097 }
            java.lang.String r6 = android.net.Uri.decode(r6)     // Catch:{ all -> 0x0097 }
            boolean r7 = r1.containsKey(r4)     // Catch:{ all -> 0x0097 }
            if (r7 != 0) goto L_0x0061
            java.util.HashMap r7 = new java.util.HashMap     // Catch:{ all -> 0x0097 }
            r7.<init>()     // Catch:{ all -> 0x0097 }
            r1.put(r4, r7)     // Catch:{ all -> 0x0097 }
        L_0x0061:
            java.lang.Object r7 = r1.get(r4)     // Catch:{ all -> 0x0097 }
            java.util.Map r7 = (java.util.Map) r7     // Catch:{ all -> 0x0097 }
            r7.put(r5, r6)     // Catch:{ all -> 0x0097 }
            goto L_0x0014
        L_0x006c:
            java.lang.String r2 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x0097 }
            java.lang.String r5 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x0097 }
            int r5 = r5.length()     // Catch:{ all -> 0x0097 }
            int r5 = r5 + 7
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0097 }
            r6.<init>(r5)     // Catch:{ all -> 0x0097 }
            java.lang.String r5 = "Parsed "
            r6.append(r5)     // Catch:{ all -> 0x0097 }
            r6.append(r2)     // Catch:{ all -> 0x0097 }
            java.lang.String r2 = r6.toString()     // Catch:{ all -> 0x0097 }
            android.util.Log.i(r4, r2)     // Catch:{ all -> 0x0097 }
            com.google.android.libraries.phenotype.client.HermeticFileOverrides r2 = new com.google.android.libraries.phenotype.client.HermeticFileOverrides     // Catch:{ all -> 0x0097 }
            r2.<init>(r1)     // Catch:{ all -> 0x0097 }
            r0.close()     // Catch:{ IOException -> 0x00a3 }
            return r2
        L_0x0097:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0099 }
        L_0x0099:
            r2 = move-exception
            r0.close()     // Catch:{ all -> 0x009e }
            goto L_0x00a2
        L_0x009e:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3)     // Catch:{ IOException -> 0x00a3 }
        L_0x00a2:
            throw r2     // Catch:{ IOException -> 0x00a3 }
        L_0x00a3:
            r0 = move-exception
            java.lang.RuntimeException r1 = new java.lang.RuntimeException
            r1.<init>(r0)
            goto L_0x00ab
        L_0x00aa:
            throw r1
        L_0x00ab:
            goto L_0x00aa
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.phenotype.client.HermeticFileOverridesReader.parseFromFile(java.io.File):com.google.android.libraries.phenotype.client.HermeticFileOverrides");
    }
}
