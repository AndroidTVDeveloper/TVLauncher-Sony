package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer2.C0847C;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.inputs.RefreshTifInputsTask;
import com.google.android.tvlauncher.inputs.TvInputEntry;
import com.google.android.tvlauncher.util.OemConfiguration;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TifInputsManager implements InputsManager {
    private static final boolean DEBUG = false;
    static final String META_LABEL_SORT_KEY = "input_sort_key";
    private static final int MSG_INPUT_ADDED = 2;
    private static final int MSG_INPUT_MODIFIED = 4;
    private static final int MSG_INPUT_REMOVED = 3;
    private static final int MSG_INPUT_UPDATED = 1;
    static final String PERMISSION_ACCESS_ALL_EPG_DATA = "com.android.providers.tv.permission.ACCESS_ALL_EPG_DATA";
    private static final String TAG = "TifInputsManager";
    private static TifInputsManager inputsManager = null;
    private static final String[] physicalTunerBlackList = {"com.google.android.videos", "com.google.android.youtube.tv"};
    private final TvInputEntry.InputsComparator comp;
    /* access modifiers changed from: private */
    public final Context context;
    private boolean disableDisconnectedInputs;
    private boolean getStateIconFromTVInput;
    /* access modifiers changed from: private */
    public final InputsHandler handler = new InputsHandler(this);
    /* access modifiers changed from: private */
    public Map<String, TvInputEntry> inputs = new HashMap();
    private final InputCallback inputsCallback = new InputCallback();
    /* access modifiers changed from: private */
    public boolean inputsLoaded;
    /* access modifiers changed from: private */
    public boolean isBundledTunerVisible;
    private List<OnInputsChangedListener> listeners = new ArrayList(2);
    private RefreshTifInputsTask.LoadedDataCallback loadedDataCallback = new RefreshTifInputsTask.LoadedDataCallback() {
        public void onDataLoaded(Map<String, TvInputInfo> newPhysicalTunerInputs, Map<String, TvInputInfo> newVirtualTunerInputs, List<TvInputEntry> newVisibleInputs, Map<String, TvInputEntry> newInputs, boolean newIsBundledTunerVisible) {
            Map unused = TifInputsManager.this.physicalTunerInputs = newPhysicalTunerInputs;
            Map unused2 = TifInputsManager.this.virtualTunerInputs = newVirtualTunerInputs;
            List unused3 = TifInputsManager.this.visibleInputs = newVisibleInputs;
            Map unused4 = TifInputsManager.this.inputs = newInputs;
            boolean unused5 = TifInputsManager.this.isBundledTunerVisible = newIsBundledTunerVisible;
            for (TvInputEntry entry : TifInputsManager.this.visibleInputs) {
                entry.preloadIcon(TifInputsManager.this.context);
            }
            TifInputsManager.this.notifyInputsChanged();
            boolean unused6 = TifInputsManager.this.inputsLoaded = true;
        }
    };
    /* access modifiers changed from: private */
    public Map<String, TvInputInfo> physicalTunerInputs = new LinkedHashMap();
    private AsyncTask<Void, Void, Void> refreshTask;
    private boolean showPhysicalTunersSeparately;
    private final TvInputManager tvManager;
    private Map<Integer, Integer> typePriorities;
    /* access modifiers changed from: private */
    public Map<String, TvInputInfo> virtualTunerInputs = new HashMap();
    /* access modifiers changed from: private */
    public List<TvInputEntry> visibleInputs = new ArrayList();

    private static class InputsHandler extends Handler {
        private final WeakReference<TifInputsManager> tifInputsManager;

        InputsHandler(TifInputsManager tifInputsManager2) {
            this.tifInputsManager = new WeakReference<>(tifInputsManager2);
        }

        public void handleMessage(Message msg) {
            TifInputsManager tifInputsManager2 = this.tifInputsManager.get();
            if (tifInputsManager2 != null) {
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            if (i == 4) {
                                if (tifInputsManager2.isRefreshTaskRunning()) {
                                    tifInputsManager2.refreshInputs();
                                } else {
                                    tifInputsManager2.inputEntryModified((TvInputInfo) msg.obj);
                                }
                            }
                        } else if (tifInputsManager2.isRefreshTaskRunning()) {
                            tifInputsManager2.refreshInputs();
                        } else {
                            tifInputsManager2.inputRemoved((String) msg.obj);
                        }
                    } else if (tifInputsManager2.isRefreshTaskRunning()) {
                        tifInputsManager2.refreshInputs();
                    } else {
                        tifInputsManager2.inputAdded((String) msg.obj);
                    }
                } else if (tifInputsManager2.isRefreshTaskRunning()) {
                    tifInputsManager2.refreshInputs();
                } else {
                    tifInputsManager2.inputStateUpdated((String) msg.obj, msg.arg1);
                }
            }
        }
    }

    public static TifInputsManager getInstance(Context context2) {
        if (inputsManager == null) {
            inputsManager = new TifInputsManager(context2.getApplicationContext());
        }
        return inputsManager;
    }

    static void setInstance(TifInputsManager inputsManager2) {
        inputsManager = inputsManager2;
    }

    TifInputsManager(Context context2) {
        OemConfiguration oemConfiguration = OemConfiguration.get(context2);
        this.getStateIconFromTVInput = oemConfiguration.getStateIconFromTVInput();
        this.disableDisconnectedInputs = oemConfiguration.shouldDisableDisconnectedInputs();
        this.showPhysicalTunersSeparately = oemConfiguration.shouldShowPhysicalTunersSeparately();
        this.context = context2;
        this.comp = new TvInputEntry.InputsComparator(context2);
        this.tvManager = (TvInputManager) context2.getSystemService("tv_input");
        OemConfiguration.get(context2).addConfigurationPackageChangeListener(new OemConfiguration.OemConfigurationPackageChangeListener() {
            public void onOemConfigurationPackageChanged() {
                TifInputsManager.this.refreshInputsFromOemConfig();
            }

            public void onOemConfigurationPackageRemoved() {
                TifInputsManager.this.refreshInputsFromOemConfig();
            }
        });
        setupDeviceTypePriorities();
    }

    /* access modifiers changed from: private */
    public void refreshInputs() {
        AsyncTask<Void, Void, Void> asyncTask = this.refreshTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        this.refreshTask = new RefreshTifInputsTask(this.tvManager, this.context, this.loadedDataCallback).execute();
    }

    public void loadInputs() {
        refreshInputs();
    }

    public void loadInputsIfNeeded() {
        if (!this.inputsLoaded) {
            refreshInputs();
        }
    }

    public void registerOnChangedListener(OnInputsChangedListener listener) {
        TvInputManager tvInputManager;
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            if (this.listeners.size() == 1 && (tvInputManager = this.tvManager) != null) {
                tvInputManager.registerCallback(this.inputsCallback, this.handler);
            }
        }
    }

    public void unregisterOnChangedListener(OnInputsChangedListener listener) {
        TvInputManager tvInputManager;
        this.listeners.remove(listener);
        if (this.listeners.isEmpty() && (tvInputManager = this.tvManager) != null) {
            tvInputManager.unregisterCallback(this.inputsCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRefreshTaskRunning() {
        AsyncTask<Void, Void, Void> asyncTask = this.refreshTask;
        return asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDisableDisconnectedInputs() {
        return this.disableDisconnectedInputs;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowPhysicalTunersSeparately() {
        return this.showPhysicalTunersSeparately;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldGetStateIconFromTVInput() {
        return this.getStateIconFromTVInput;
    }

    public int getInputType(int position) {
        return this.visibleInputs.get(position).getType();
    }

    public void launchInputActivity(int position) {
        TvInputEntry entry = this.visibleInputs.get(position);
        if (!entry.isDisconnected() || !this.disableDisconnectedInputs) {
            try {
                Intent intent = entry.getLaunchIntent();
                intent.addFlags(C0847C.ENCODING_PCM_MU_LAW);
                this.context.startActivity(intent);
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
        return this.visibleInputs.get(position).getId();
    }

    public int getItemCount() {
        return this.visibleInputs.size();
    }

    public boolean hasInputs() {
        return getItemCount() > 0;
    }

    public Drawable getIcon(int position) {
        TvInputEntry entry = this.visibleInputs.get(position);
        return entry.getImageDrawable(this.context, entry.getState());
    }

    public Uri getIconUri(int position) {
        return this.visibleInputs.get(position).getIconUri();
    }

    public Uri getSelectedIconUri(int position) {
        return null;
    }

    public Uri getActiveIconUri(int position) {
        return null;
    }

    public Uri getSelectedActiveIconUri(int position) {
        return null;
    }

    public String getGroupId(int position) {
        return null;
    }

    public String getLabel(int position) {
        return this.visibleInputs.get(position).getLabel();
    }

    public String getParentLabel(int position) {
        return this.visibleInputs.get(position).getParentLabel();
    }

    public int getInputState(int position) {
        TvInputEntry entry = this.visibleInputs.get(position);
        if (entry.isConnected()) {
            if (entry.isStandby()) {
                return 1;
            }
            return 0;
        } else if (entry.isDisconnected()) {
            return 2;
        } else {
            return entry.getState();
        }
    }

    /* access modifiers changed from: package-private */
    public Handler getHandler() {
        return this.handler;
    }

    /* access modifiers changed from: private */
    public void notifyInputsChanged() {
        for (OnInputsChangedListener listener : this.listeners) {
            listener.onInputsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshInputsFromOemConfig() {
        OemConfiguration oemConfiguration = OemConfiguration.get(this.context);
        this.getStateIconFromTVInput = oemConfiguration.getStateIconFromTVInput();
        this.disableDisconnectedInputs = oemConfiguration.shouldDisableDisconnectedInputs();
        this.showPhysicalTunersSeparately = oemConfiguration.shouldShowPhysicalTunersSeparately();
        setupDeviceTypePriorities();
        refreshInputs();
        notifyInputsChanged();
    }

    private void hideBundledTunerInput() {
        if (this.isBundledTunerVisible) {
            this.isBundledTunerVisible = false;
            boolean isVisuallyChanged = false;
            for (int i = this.visibleInputs.size() - 1; i >= 0; i--) {
                if (this.visibleInputs.get(i).isBundledTuner()) {
                    this.visibleInputs.remove(i);
                    isVisuallyChanged = true;
                }
            }
            if (isVisuallyChanged) {
                notifyInputsChanged();
            }
        }
    }

    private void showBundledTunerInput() {
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
            bundledTuner.preloadIcon(this.context);
            insertEntryIntoSortedList(bundledTuner, this.visibleInputs);
            notifyInputsChanged();
            this.isBundledTunerVisible = true;
        }
    }

    private void addInputEntry(TvInputInfo input) {
        int parentIndex;
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
                    insertEntryIntoSortedList(entry, this.visibleInputs);
                    if (!(entry.getParentEntry() == null || (parentIndex = getIndexInVisibleList(entry.getParentEntry().getId())) == -1)) {
                        this.visibleInputs.remove(parentIndex);
                    }
                    notifyInputsChanged();
                }
            }
        } catch (IllegalArgumentException e) {
            String valueOf = String.valueOf(input.getId());
            Log.e(TAG, valueOf.length() != 0 ? "Failed to get state for Input, dropping entry. Id = ".concat(valueOf) : "Failed to get state for Input, dropping entry. Id = ");
        }
    }

    private int getIndexInVisibleList(String id) {
        for (int i = 0; i < this.visibleInputs.size(); i++) {
            TvInputInfo info = this.visibleInputs.get(i).getInfo();
            if (info != null && TextUtils.equals(info.getId(), id)) {
                return i;
            }
        }
        return -1;
    }

    private void insertEntryIntoSortedList(TvInputEntry entry, List<TvInputEntry> list) {
        int i = 0;
        while (i < list.size() && this.comp.compare(entry, list.get(i)) > 0) {
            i++;
        }
        if (!list.contains(entry)) {
            list.add(i, entry);
        }
    }

    /* access modifiers changed from: private */
    public void inputStateUpdated(String id, int state) {
        TvInputEntry entry = this.inputs.get(id);
        if (entry != null) {
            entry.setState(state);
            if (getIndexInVisibleList(id) >= 0) {
                notifyInputsChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void inputEntryModified(TvInputInfo inputInfo) {
        String id;
        TvInputEntry entry;
        if (inputInfo.isHidden(this.context)) {
            refreshInputs();
            return;
        }
        CharSequence customLabel = inputInfo.loadCustomLabel(this.context);
        if (customLabel != null && (entry = this.inputs.get((id = inputInfo.getId()))) != null) {
            entry.setLabel(customLabel.toString());
            if (getIndexInVisibleList(id) >= 0) {
                notifyInputsChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void inputAdded(String id) {
        TvInputManager tvInputManager = this.tvManager;
        if (tvInputManager != null) {
            inputAdded(tvInputManager.getTvInputInfo(id));
        }
    }

    private void inputAdded(TvInputInfo info) {
        if (info == null) {
            return;
        }
        if (info.isPassthroughInput()) {
            addInputEntry(info);
        } else if (isPhysicalTuner(this.context.getPackageManager(), info)) {
            this.physicalTunerInputs.put(info.getId(), info);
            if (this.showPhysicalTunersSeparately) {
                addInputEntry(info);
            } else if (!info.isHidden(this.context)) {
                showBundledTunerInput();
            }
        } else {
            this.virtualTunerInputs.put(info.getId(), info);
            if (!info.isHidden(this.context)) {
                showBundledTunerInput();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void inputRemoved(String id) {
        TvInputEntry entry = this.inputs.get(id);
        if (entry == null || entry.getInfo() == null || !entry.getInfo().isPassthroughInput()) {
            removeTuner(id);
        } else {
            removeEntry(id);
        }
    }

    private void removeTuner(String id) {
        removeEntry(id);
        this.virtualTunerInputs.remove(id);
        this.physicalTunerInputs.remove(id);
        if (!this.virtualTunerInputs.isEmpty()) {
            return;
        }
        if (this.physicalTunerInputs.isEmpty() || this.showPhysicalTunersSeparately) {
            hideBundledTunerInput();
        }
    }

    private void removeEntry(String id) {
        TvInputEntry entry = this.inputs.get(id);
        if (entry != null) {
            boolean isVisuallyChanged = false;
            Iterator<Map.Entry<String, TvInputEntry>> iterator = this.inputs.entrySet().iterator();
            while (iterator.hasNext()) {
                TvInputEntry child = (TvInputEntry) iterator.next().getValue();
                if (child.getParentEntry() != null && TextUtils.equals(child.getParentEntry().getId(), entry.getId())) {
                    iterator.remove();
                }
            }
            for (int i = this.visibleInputs.size() - 1; i >= 0; i--) {
                TvInputEntry children = this.visibleInputs.get(i);
                if (children.getParentEntry() != null && TextUtils.equals(children.getParentEntry().getId(), id)) {
                    this.visibleInputs.remove(i);
                    isVisuallyChanged = true;
                }
            }
            this.inputs.remove(id);
            int index = getIndexInVisibleList(id);
            if (index != -1) {
                this.visibleInputs.remove(index);
                isVisuallyChanged = true;
            }
            TvInputEntry parent = entry.getParentEntry();
            if (parent != null) {
                parent.setNumChildren(Math.max(0, parent.getNumChildren() - 1));
                if (parent.getNumChildren() == 0 && getIndexInVisibleList(parent.getId()) == -1 && !parent.getInfo().isHidden(this.context)) {
                    insertEntryIntoSortedList(parent, this.visibleInputs);
                    isVisuallyChanged = true;
                }
            }
            if (isVisuallyChanged) {
                notifyInputsChanged();
            }
        }
    }

    private class InputCallback extends TvInputManager.TvInputCallback {
        private InputCallback() {
        }

        public void onInputStateChanged(String inputId, int state) {
            TifInputsManager.this.handler.sendMessage(TifInputsManager.this.handler.obtainMessage(1, state, 0, inputId));
        }

        public void onInputAdded(String inputId) {
            TifInputsManager.this.handler.sendMessage(TifInputsManager.this.handler.obtainMessage(2, inputId));
        }

        public void onInputRemoved(String inputId) {
            TifInputsManager.this.handler.sendMessage(TifInputsManager.this.handler.obtainMessage(3, inputId));
        }

        public void onTvInputInfoUpdated(TvInputInfo inputInfo) {
            TifInputsManager.this.handler.sendMessage(TifInputsManager.this.handler.obtainMessage(4, inputInfo));
        }
    }

    static boolean isPhysicalTuner(PackageManager pkgMan, TvInputInfo input) {
        if (Arrays.asList(physicalTunerBlackList).contains(input.getServiceInfo().packageName) || input.createSetupIntent() == null) {
            return false;
        }
        if (pkgMan.checkPermission(PERMISSION_ACCESS_ALL_EPG_DATA, input.getServiceInfo().packageName) == 0) {
            return true;
        }
        try {
            return (pkgMan.getApplicationInfo(input.getServiceInfo().packageName, 0).flags & 129) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int getPriorityForType(int type) {
        Integer priority;
        Map<Integer, Integer> map = this.typePriorities;
        if (map == null || (priority = map.get(Integer.valueOf(type))) == null) {
            return Integer.MAX_VALUE;
        }
        return priority.intValue();
    }

    private void setupDeviceTypePriorities() {
        this.typePriorities = InputsManagerUtil.getInputsOrderMap(OemConfiguration.get(this.context).getHomeScreenInputsOrdering());
    }
}
