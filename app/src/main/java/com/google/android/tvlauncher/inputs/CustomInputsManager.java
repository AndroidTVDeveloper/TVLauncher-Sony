package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.C0847C;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.inputs.CustomTvInputEntry;
import com.google.android.tvlauncher.util.OemConfiguration;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomInputsManager implements InputsManager {
    private static final String AUTHORITY = "tvlauncher.inputs";
    public static final Uri CONTENT_URI = Uri.parse("content://tvlauncher.inputs/inputs");
    private static final boolean DEBUG = false;
    private static final int MATCH_INPUT = 1;
    private static final int MATCH_INPUT_ID = 2;
    private static final String PATH_INPUT = "inputs";
    private static final String TAG = "CustomInputsManager";
    private static final String URI_INTENT_SCHEME_STR = "intent";
    private static CustomInputsManager inputsManager = null;
    /* access modifiers changed from: private */
    public static final UriMatcher uriMatcher = new UriMatcher(-1);
    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            int match = CustomInputsManager.uriMatcher.match(uri);
            if (match == 1 || match == 2) {
                CustomInputsManager.this.refreshInputs();
            }
        }
    };
    private final Context context;
    private List<CustomTvInputEntry> inputs = Collections.emptyList();
    private boolean inputsLoaded;
    private List<OnInputsChangedListener> listeners = new ArrayList(2);
    private RefreshInputList.LoadedDataCallback loadedDataCallback = new CustomInputsManager$$Lambda$0(this);
    private AsyncTask<Void, Void, List<CustomTvInputEntry>> refreshTask;
    private boolean registered;

    static {
        uriMatcher.addURI(AUTHORITY, "inputs", 1);
        uriMatcher.addURI(AUTHORITY, "inputs/*", 2);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$CustomInputsManager(List inputList) {
        this.inputs = inputList;
        for (OnInputsChangedListener listener : this.listeners) {
            listener.onInputsChanged();
        }
        this.inputsLoaded = true;
    }

    public static CustomInputsManager getInstance(Context context2) {
        if (inputsManager == null) {
            inputsManager = new CustomInputsManager(context2.getApplicationContext());
        }
        return inputsManager;
    }

    CustomInputsManager(Context context2) {
        this.context = context2;
    }

    public void registerOnChangedListener(OnInputsChangedListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            if (this.listeners.size() == 1) {
                this.registered = true;
                this.context.getContentResolver().registerContentObserver(CONTENT_URI, true, this.contentObserver);
            }
        }
    }

    public void unregisterOnChangedListener(OnInputsChangedListener listener) {
        this.listeners.remove(listener);
        if (this.registered && this.listeners.isEmpty()) {
            this.context.getContentResolver().unregisterContentObserver(this.contentObserver);
            this.registered = false;
        }
    }

    /* access modifiers changed from: private */
    public void refreshInputs() {
        AsyncTask<Void, Void, List<CustomTvInputEntry>> asyncTask = this.refreshTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        this.refreshTask = new RefreshInputList(this.context, this.loadedDataCallback).execute();
    }

    public void loadInputs() {
        refreshInputs();
    }

    public void loadInputsIfNeeded() {
        if (!this.inputsLoaded) {
            refreshInputs();
        }
    }

    public boolean hasInputs() {
        return !this.inputs.isEmpty();
    }

    public int getItemCount() {
        return this.inputs.size();
    }

    public void launchInputActivity(int position) {
        CustomTvInputEntry entry = this.inputs.get(position);
        if (entry.getState() != 2 || !OemConfiguration.get(this.context).shouldDisableDisconnectedInputs()) {
            try {
                this.context.startActivity(entry.getIntent());
            } catch (Throwable e) {
                Log.e(TAG, "Could not perform launch:", e);
                Toast.makeText(this.context, C1167R.string.failed_launch, 0).show();
            }
        } else {
            String toastText = OemConfiguration.get(this.context).getDisconnectedInputToastText();
            if (!TextUtils.isEmpty(toastText)) {
                Toast.makeText(this.context, toastText, 0).show();
            }
        }
    }

    public String getInputId(int position) {
        return this.inputs.get(position).getId();
    }

    public int getInputState(int position) {
        return this.inputs.get(position).getState();
    }

    public int getInputType(int position) {
        return 1000;
    }

    public String getLabel(int position) {
        return this.inputs.get(position).getLabel();
    }

    public String getParentLabel(int position) {
        return this.inputs.get(position).getParentLabel();
    }

    public Drawable getIcon(int position) {
        return null;
    }

    public Uri getIconUri(int position) {
        return this.inputs.get(position).getIconUri();
    }

    public Uri getSelectedIconUri(int position) {
        return this.inputs.get(position).getSelectedIconUri();
    }

    public Uri getActiveIconUri(int position) {
        return this.inputs.get(position).getActiveIconUri();
    }

    public Uri getSelectedActiveIconUri(int position) {
        return this.inputs.get(position).getSelectedActiveIconUri();
    }

    public String getGroupId(int position) {
        return this.inputs.get(position).getGroupId();
    }

    static Intent parseIntentUri(String inputId, String intentUriStr) {
        if (intentUriStr == null) {
            String valueOf = String.valueOf(inputId);
            Log.e(TAG, valueOf.length() != 0 ? "parseIntentUri: intent URI can't be null\nInput ID=".concat(valueOf) : "parseIntentUri: intent URI can't be null\nInput ID=");
            return null;
        }
        Uri uri = Uri.parse(intentUriStr);
        if (!"intent".equals(uri.getScheme())) {
            StringBuilder sb = new StringBuilder(String.valueOf(inputId).length() + 89 + intentUriStr.length());
            sb.append("parseIntentUri: have to use Intent.URI_INTENT_SCHEME for intent URI\nInput ID=");
            sb.append(inputId);
            sb.append("\nIntent Uri=");
            sb.append(intentUriStr);
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            Intent intent = Intent.parseUri(intentUriStr, 1);
            if (TvContractCompat.AUTHORITY.equals(uri.getAuthority()) || intent.getComponent() != null) {
                intent.addFlags(C0847C.ENCODING_PCM_MU_LAW);
                return intent;
            }
            StringBuilder sb2 = new StringBuilder(String.valueOf(inputId).length() + 93 + intentUriStr.length());
            sb2.append("parseIntentUri: Custom input intent URI should contain a component name\nInput ID=");
            sb2.append(inputId);
            sb2.append("\nIntent Uri=");
            sb2.append(intentUriStr);
            Log.e(TAG, sb2.toString());
            return null;
        } catch (URISyntaxException e) {
            StringBuilder sb3 = new StringBuilder(String.valueOf(inputId).length() + 67 + intentUriStr.length());
            sb3.append("parseIntentUri: Cannot parse input intent URI\nInput ID=");
            sb3.append(inputId);
            sb3.append("\nIntent Uri=");
            sb3.append(intentUriStr);
            Log.e(TAG, sb3.toString());
            return null;
        }
    }

    private static class RefreshInputList extends AsyncTask<Void, Void, List<CustomTvInputEntry>> {
        private final LoadedDataCallback callback;
        private final Context context;
        private final RequestOptions imageRequestOptions;

        interface LoadedDataCallback {
            void onDataLoaded(List<CustomTvInputEntry> list);
        }

        /* access modifiers changed from: protected */
        public /* bridge */ /* synthetic */ void onPostExecute(Object obj) {
            onPostExecute((List<CustomTvInputEntry>) ((List) obj));
        }

        RefreshInputList(Context context2, LoadedDataCallback callback2) {
            this.context = context2;
            this.callback = callback2;
            int iconMaxSize = this.context.getResources().getDimensionPixelSize(C1167R.dimen.input_icon_view_size);
            this.imageRequestOptions = (RequestOptions) ((RequestOptions) new RequestOptions().override(iconMaxSize, iconMaxSize)).centerInside();
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00de, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00df, code lost:
            if (r0 != null) goto L_0x00e1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e5, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e6, code lost:
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ea, code lost:
            throw r2;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.util.List<com.google.android.tvlauncher.inputs.CustomTvInputEntry> doInBackground(java.lang.Void... r11) {
            /*
                r10 = this;
                android.content.Context r0 = r10.context
                android.content.ContentResolver r1 = r0.getContentResolver()
                android.net.Uri r2 = com.google.android.tvlauncher.inputs.CustomInputsManager.CONTENT_URI
                java.lang.String[] r3 = com.google.android.tvlauncher.inputs.CustomTvInputEntry.PROJECTION
                r4 = 0
                r5 = 0
                r6 = 0
                android.database.Cursor r0 = r1.query(r2, r3, r4, r5, r6)
                if (r0 == 0) goto L_0x00a7
                java.util.ArrayList r1 = new java.util.ArrayList     // Catch:{ all -> 0x00dc }
                r2 = 20
                r1.<init>(r2)     // Catch:{ all -> 0x00dc }
                java.util.HashMap r3 = new java.util.HashMap     // Catch:{ all -> 0x00dc }
                r3.<init>(r2)     // Catch:{ all -> 0x00dc }
                r2 = r3
                r3 = 0
            L_0x0022:
                boolean r4 = r0.moveToNext()     // Catch:{ all -> 0x00dc }
                if (r4 == 0) goto L_0x0050
                android.content.Context r4 = r10.context     // Catch:{ all -> 0x00dc }
                com.google.android.tvlauncher.inputs.CustomTvInputEntry r4 = com.google.android.tvlauncher.inputs.CustomTvInputEntry.fromCursor(r4, r0)     // Catch:{ all -> 0x00dc }
                java.lang.String r5 = r4.getId()     // Catch:{ all -> 0x00dc }
                java.lang.String r6 = r4.getIntentUri()     // Catch:{ all -> 0x00dc }
                android.content.Intent r5 = com.google.android.tvlauncher.inputs.CustomInputsManager.parseIntentUri(r5, r6)     // Catch:{ all -> 0x00dc }
                if (r5 == 0) goto L_0x004f
                r4.setIntent(r5)     // Catch:{ all -> 0x00dc }
                r1.add(r4)     // Catch:{ all -> 0x00dc }
                java.lang.String r6 = r4.getId()     // Catch:{ all -> 0x00dc }
                r2.put(r6, r4)     // Catch:{ all -> 0x00dc }
                int r6 = r3 + 1
                r4.setInitialSortIndex(r3)     // Catch:{ all -> 0x00dc }
                r3 = r6
            L_0x004f:
                goto L_0x0022
            L_0x0050:
                java.util.HashSet r4 = new java.util.HashSet     // Catch:{ all -> 0x00dc }
                int r5 = r1.size()     // Catch:{ all -> 0x00dc }
                r4.<init>(r5)     // Catch:{ all -> 0x00dc }
                java.util.Iterator r5 = r1.iterator()     // Catch:{ all -> 0x00dc }
            L_0x005d:
                boolean r6 = r5.hasNext()     // Catch:{ all -> 0x00dc }
                if (r6 == 0) goto L_0x0095
                java.lang.Object r6 = r5.next()     // Catch:{ all -> 0x00dc }
                com.google.android.tvlauncher.inputs.CustomTvInputEntry r6 = (com.google.android.tvlauncher.inputs.CustomTvInputEntry) r6     // Catch:{ all -> 0x00dc }
                java.lang.String r7 = r6.getParentId()     // Catch:{ all -> 0x00dc }
                if (r7 == 0) goto L_0x008d
                boolean r8 = r2.containsKey(r7)     // Catch:{ all -> 0x00dc }
                if (r8 == 0) goto L_0x008d
                java.lang.Object r8 = r2.get(r7)     // Catch:{ all -> 0x00dc }
                com.google.android.tvlauncher.inputs.CustomTvInputEntry r8 = (com.google.android.tvlauncher.inputs.CustomTvInputEntry) r8     // Catch:{ all -> 0x00dc }
                r4.add(r7)     // Catch:{ all -> 0x00dc }
                java.lang.String r9 = r8.getLabel()     // Catch:{ all -> 0x00dc }
                r6.setParentLabel(r9)     // Catch:{ all -> 0x00dc }
                int r9 = r8.getInitialSortIndex()     // Catch:{ all -> 0x00dc }
                r6.setFinalSortIndex(r9)     // Catch:{ all -> 0x00dc }
                goto L_0x0094
            L_0x008d:
                int r8 = r6.getInitialSortIndex()     // Catch:{ all -> 0x00dc }
                r6.setFinalSortIndex(r8)     // Catch:{ all -> 0x00dc }
            L_0x0094:
                goto L_0x005d
            L_0x0095:
                com.google.android.tvlauncher.inputs.CustomInputsManager$RefreshInputList$$Lambda$0 r5 = new com.google.android.tvlauncher.inputs.CustomInputsManager$RefreshInputList$$Lambda$0     // Catch:{ all -> 0x00dc }
                r5.<init>(r4)     // Catch:{ all -> 0x00dc }
                r1.removeIf(r5)     // Catch:{ all -> 0x00dc }
                java.util.Collections.sort(r1)     // Catch:{ all -> 0x00dc }
                if (r0 == 0) goto L_0x00a6
                r0.close()
            L_0x00a6:
                return r1
            L_0x00a7:
                java.lang.String r1 = "CustomInputsManager"
                android.net.Uri r2 = com.google.android.tvlauncher.inputs.CustomInputsManager.CONTENT_URI     // Catch:{ all -> 0x00dc }
                java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00dc }
                java.lang.String r3 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00dc }
                int r3 = r3.length()     // Catch:{ all -> 0x00dc }
                int r3 = r3 + 29
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00dc }
                r4.<init>(r3)     // Catch:{ all -> 0x00dc }
                java.lang.String r3 = "The cursor for query "
                r4.append(r3)     // Catch:{ all -> 0x00dc }
                r4.append(r2)     // Catch:{ all -> 0x00dc }
                java.lang.String r2 = " is null"
                r4.append(r2)     // Catch:{ all -> 0x00dc }
                java.lang.String r2 = r4.toString()     // Catch:{ all -> 0x00dc }
                android.util.Log.e(r1, r2)     // Catch:{ all -> 0x00dc }
                if (r0 == 0) goto L_0x00d7
                r0.close()
            L_0x00d7:
                java.util.List r0 = java.util.Collections.emptyList()
                return r0
            L_0x00dc:
                r1 = move-exception
                throw r1     // Catch:{ all -> 0x00de }
            L_0x00de:
                r2 = move-exception
                if (r0 == 0) goto L_0x00e9
                r0.close()     // Catch:{ all -> 0x00e5 }
                goto L_0x00e9
            L_0x00e5:
                r3 = move-exception
                com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r1, r3)
            L_0x00e9:
                goto L_0x00eb
            L_0x00ea:
                throw r2
            L_0x00eb:
                goto L_0x00ea
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.inputs.CustomInputsManager.RefreshInputList.doInBackground(java.lang.Void[]):java.util.List");
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(List<CustomTvInputEntry> customTvInputEntries) {
            for (CustomTvInputEntry entry : customTvInputEntries) {
                entry.preloadIcon(this.context, this.imageRequestOptions);
            }
            this.callback.onDataLoaded(customTvInputEntries);
        }

        private CustomTvInputEntry getHomeInput() {
            return new CustomTvInputEntry.Builder().setId("com.google.android.tvlauncher.input.home").setLabel(this.context.getString(C1167R.string.input_title_home)).setIntentUri(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").toUri(1)).build(this.context);
        }
    }
}
