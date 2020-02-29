package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.inputs.TvInputEntry;
import com.google.android.tvlauncher.util.OemConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RefreshTifInputsTask extends AsyncTask<Void, Void, Void> {
    private static final boolean DEBUG = false;
    private static final String TAG = "RefreshTifInputsTask";
    private final TvInputEntry.InputsComparator comp;
    private final Context context;
    private final Map<String, TvInputEntry> inputs = new HashMap();
    private boolean isBundledTunerVisible;
    private final LoadedDataCallback loadedDataCallback;
    private final Map<String, TvInputInfo> physicalTunerInputs = new LinkedHashMap();
    private final TvInputManager tvManager;
    private final Map<String, TvInputInfo> virtualTunerInputs = new HashMap();
    private final List<TvInputEntry> visibleInputs = new ArrayList();

    interface LoadedDataCallback {
        void onDataLoaded(Map<String, TvInputInfo> map, Map<String, TvInputInfo> map2, List<TvInputEntry> list, Map<String, TvInputEntry> map3, boolean z);
    }

    RefreshTifInputsTask(TvInputManager tvInputManager, Context context2, LoadedDataCallback loadedDataCallback2) {
        this.tvManager = tvInputManager;
        this.context = context2;
        this.loadedDataCallback = loadedDataCallback2;
        this.comp = new TvInputEntry.InputsComparator(this.context);
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(Void... voids) {
        TvInputManager tvInputManager;
        List<TvInputInfo> serviceInputs;
        if (!(isCancelled() || (tvInputManager = this.tvManager) == null || (serviceInputs = tvInputManager.getTvInputList()) == null)) {
            for (int i = 0; i < serviceInputs.size() && !isCancelled(); i++) {
                inputAddedInternal(serviceInputs.get(i));
            }
            this.visibleInputs.removeIf(RefreshTifInputsTask$$Lambda$0.$instance);
            this.visibleInputs.sort(this.comp);
        }
        return null;
    }

    static final /* synthetic */ boolean lambda$doInBackground$0$RefreshTifInputsTask(TvInputEntry input) {
        return input.getNumChildren() > 0;
    }

    private void inputAddedInternal(TvInputInfo info) {
        if (info == null) {
            return;
        }
        if (info.isPassthroughInput()) {
            addInputEntryInternal(info);
        } else if (TifInputsManager.isPhysicalTuner(this.context.getPackageManager(), info)) {
            this.physicalTunerInputs.put(info.getId(), info);
            if (TifInputsManager.getInstance(this.context).shouldShowPhysicalTunersSeparately()) {
                addInputEntryInternal(info);
            } else if (!info.isHidden(this.context)) {
                showBundledTunerInputInternal();
            }
        } else {
            this.virtualTunerInputs.put(info.getId(), info);
            if (!info.isHidden(this.context)) {
                showBundledTunerInputInternal();
            }
        }
    }

    private void addInputEntryInternal(TvInputInfo input) {
        TvInputInfo parentInfo;
        try {
            int state = this.tvManager.getInputState(input.getId());
            TvInputEntry parentEntry = null;
            if (this.inputs.get(input.getId()) == null) {
                if (!(input.getParentId() == null || (parentInfo = this.tvManager.getTvInputInfo(input.getParentId())) == null)) {
                    parentEntry = this.inputs.get(parentInfo.getId());
                    if (parentEntry == null) {
                        parentEntry = new TvInputEntry(parentInfo, null, this.tvManager.getInputState(parentInfo.getId()));
                        parentEntry.init(this.context);
                        this.inputs.put(parentInfo.getId(), parentEntry);
                    }
                    parentEntry.setNumChildren(parentEntry.getNumChildren() + 1);
                }
                TvInputEntry entry = new TvInputEntry(input, parentEntry, state);
                entry.init(this.context);
                this.inputs.put(input.getId(), entry);
                if (entry.getInfo().isHidden(this.context)) {
                    return;
                }
                if (parentEntry == null || !parentEntry.getInfo().isHidden(this.context)) {
                    this.visibleInputs.add(entry);
                    if (parentEntry != null && parentEntry.getInfo().getParentId() == null && !this.visibleInputs.contains(parentEntry)) {
                        this.visibleInputs.add(parentEntry);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            String valueOf = String.valueOf(input.getId());
            Log.e(TAG, valueOf.length() != 0 ? "Failed to get state for Input, dropping entry. Id = ".concat(valueOf) : "Failed to get state for Input, dropping entry. Id = ");
        }
    }

    private void addHomeInput() {
        TvInputEntry homeInput = new TvInputEntry("com.google.android.tvlauncher.input.home", -7, this.context.getString(C1167R.string.input_title_home), null);
        homeInput.init(this.context);
        this.visibleInputs.add(homeInput);
    }

    private void showBundledTunerInputInternal() {
        String label;
        if (!this.isBundledTunerVisible) {
            String label2 = OemConfiguration.get(this.context).getBundledTunerTitle();
            if (!TextUtils.isEmpty(label2)) {
                label = label2;
            } else {
                label = this.context.getResources().getString(C1167R.string.input_title_bundled_tuner);
            }
            TvInputEntry bundledTuner = new TvInputEntry("com.google.android.tvlauncher.input.bundled_tuner", -3, label, OemConfiguration.get(this.context).getBundledTunerBannerUri());
            bundledTuner.init(this.context);
            this.visibleInputs.add(bundledTuner);
            this.isBundledTunerVisible = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Void aVoid) {
        LoadedDataCallback loadedDataCallback2 = this.loadedDataCallback;
        if (loadedDataCallback2 != null) {
            loadedDataCallback2.onDataLoaded(this.physicalTunerInputs, this.virtualTunerInputs, this.visibleInputs, this.inputs, this.isBundledTunerVisible);
        }
    }
}
