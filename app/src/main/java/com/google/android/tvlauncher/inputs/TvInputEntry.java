package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.net.Uri;
import android.support.p001v4.content.ContextCompat;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.tvlauncher.C1167R;
import java.util.Comparator;
import java.util.Objects;

public class TvInputEntry {
    private final HdmiDeviceInfo hdmiInfo;
    private Drawable icon;
    private int iconState;
    private final Uri iconUri;

    /* renamed from: id */
    private final String f159id;
    /* access modifiers changed from: private */
    public final TvInputInfo info;
    /* access modifiers changed from: private */
    public String label;
    private int numChildren;
    private final TvInputEntry parentEntry;
    private String parentLabel;
    /* access modifiers changed from: private */
    public int priority;
    /* access modifiers changed from: private */
    public int sortKey;
    /* access modifiers changed from: private */
    public String sortingParentLabel;
    /* access modifiers changed from: private */
    public int state;
    /* access modifiers changed from: private */
    public final int type;

    TvInputEntry(String id, int type2, String label2, Uri iconUri2) {
        this.f159id = id;
        this.parentEntry = null;
        this.info = null;
        this.hdmiInfo = null;
        this.type = type2;
        this.iconUri = iconUri2;
        this.iconState = 0;
        this.state = 0;
        this.label = label2 != null ? label2 : "";
        this.sortKey = Integer.MAX_VALUE;
    }

    TvInputEntry(TvInputInfo info2, TvInputEntry parent, int state2) {
        this.f159id = info2.getId();
        this.parentEntry = parent;
        this.info = info2;
        this.type = getTypeFromTifInput(info2);
        this.iconUri = null;
        if (info2.getType() == 1007) {
            this.hdmiInfo = info2.getHdmiDeviceInfo();
        } else {
            this.hdmiInfo = null;
        }
        this.state = state2;
    }

    /* access modifiers changed from: package-private */
    public void init(Context context) {
        this.priority = TifInputsManager.getInstance(context).getPriorityForType(this.type);
        TvInputInfo tvInputInfo = this.info;
        if (tvInputInfo != null) {
            CharSequence infoLabel = tvInputInfo.loadCustomLabel(context);
            if (TextUtils.isEmpty(infoLabel)) {
                infoLabel = this.info.loadLabel(context);
            }
            if (infoLabel != null) {
                this.label = infoLabel.toString();
            } else {
                this.label = "";
            }
            this.sortKey = this.info.getServiceInfo().metaData.getInt("input_sort_key", Integer.MAX_VALUE);
            TvInputEntry tvInputEntry = this.parentEntry;
            if (tvInputEntry != null) {
                this.parentLabel = tvInputEntry.getLabel();
                this.sortingParentLabel = this.parentLabel;
            } else {
                this.sortingParentLabel = this.label;
            }
            this.icon = getImageDrawable(context, this.state);
        }
    }

    /* access modifiers changed from: package-private */
    public void preloadIcon(Context context) {
        if (this.iconUri != null) {
            int iconMaxSize = context.getResources().getDimensionPixelSize(C1167R.dimen.input_icon_view_size);
            Glide.with(context).load(this.iconUri).apply((BaseRequestOptions<?>) ((RequestOptions) ((RequestOptions) new RequestOptions().override(iconMaxSize, iconMaxSize)).centerInside())).preload();
        }
    }

    /* access modifiers changed from: package-private */
    public String getId() {
        return this.f159id;
    }

    /* access modifiers changed from: package-private */
    public TvInputEntry getParentEntry() {
        return this.parentEntry;
    }

    /* access modifiers changed from: package-private */
    public TvInputInfo getInfo() {
        return this.info;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.type;
    }

    /* access modifiers changed from: package-private */
    public Uri getIconUri() {
        return this.iconUri;
    }

    /* access modifiers changed from: package-private */
    public int getState() {
        return this.state;
    }

    /* access modifiers changed from: package-private */
    public void setState(int state2) {
        this.state = state2;
    }

    /* access modifiers changed from: package-private */
    public String getLabel() {
        HdmiDeviceInfo hdmiDeviceInfo = this.hdmiInfo;
        if (hdmiDeviceInfo == null || TextUtils.isEmpty(hdmiDeviceInfo.getDisplayName())) {
            return this.label;
        }
        return this.hdmiInfo.getDisplayName();
    }

    /* access modifiers changed from: package-private */
    public void setLabel(String label2) {
        this.label = label2;
    }

    /* access modifiers changed from: package-private */
    public int getNumChildren() {
        return this.numChildren;
    }

    /* access modifiers changed from: package-private */
    public void setNumChildren(int numChildren2) {
        this.numChildren = numChildren2;
    }

    /* access modifiers changed from: package-private */
    public String getParentLabel() {
        return this.parentLabel;
    }

    /* access modifiers changed from: package-private */
    public Drawable getIcon() {
        return this.icon;
    }

    /* access modifiers changed from: package-private */
    public boolean isBundledTuner() {
        return this.type == -3;
    }

    /* access modifiers changed from: package-private */
    public boolean isConnected() {
        return isCustomTifInput() || this.state != 2;
    }

    /* access modifiers changed from: package-private */
    public boolean isStandby() {
        return this.state == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisconnected() {
        return !isConnected();
    }

    /* access modifiers changed from: package-private */
    public Drawable getImageDrawable(Context context, int newState) {
        Drawable drawable = this.icon;
        if (drawable != null && this.state == this.iconState) {
            return drawable;
        }
        this.iconState = newState;
        if (this.info != null) {
            this.icon = loadIcon(context);
            Drawable drawable2 = this.icon;
            if (drawable2 != null) {
                return drawable2;
            }
        }
        Integer drawableId = InputsManagerUtil.getIconResourceId(this.type);
        if (drawableId == null) {
            drawableId = Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_hdmi);
        }
        Drawable drawable3 = ContextCompat.getDrawable(context, drawableId.intValue());
        this.icon = drawable3;
        return drawable3;
    }

    /* access modifiers changed from: package-private */
    public Intent getLaunchIntent() {
        TvInputInfo tvInputInfo = this.info;
        if (tvInputInfo == null) {
            int i = this.type;
            if (i == -7) {
                return new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME");
            }
            if (i == -3) {
                return new Intent("android.intent.action.VIEW", TvContract.Channels.CONTENT_URI);
            }
            return null;
        } else if (tvInputInfo.isPassthroughInput()) {
            return new Intent("android.intent.action.VIEW", TvContract.buildChannelUriForPassthroughInput(this.info.getId()));
        } else {
            return new Intent("android.intent.action.VIEW", TvContract.buildChannelsUriForInput(this.info.getId()));
        }
    }

    private Drawable loadIcon(Context context) {
        Drawable icon2;
        if (!TifInputsManager.getInstance(context).shouldGetStateIconFromTVInput() || (icon2 = this.info.loadIcon(context, this.state)) == null) {
            return this.info.loadIcon(context);
        }
        return icon2;
    }

    private int getTypeFromTifInput(TvInputInfo info2) {
        if (info2.getHdmiDeviceInfo() != null && info2.getHdmiDeviceInfo().isCecDevice()) {
            int cecDeviceType = info2.getHdmiDeviceInfo().getDeviceType();
            if (cecDeviceType == 0) {
                return -8;
            }
            if (cecDeviceType == 1) {
                return -4;
            }
            if (cecDeviceType == 3) {
                return -10;
            }
            if (cecDeviceType == 4) {
                return -5;
            }
            if (cecDeviceType != 5) {
                return -2;
            }
            return -9;
        } else if (info2.getHdmiDeviceInfo() == null || !info2.getHdmiDeviceInfo().isMhlDevice()) {
            return info2.getType();
        } else {
            return -6;
        }
    }

    private boolean isCustomTifInput() {
        int i = this.type;
        return i == -3 || i == -7;
    }

    public boolean equals(Object o) {
        TvInputInfo tvInputInfo;
        if (o == this) {
            return true;
        }
        if (!(o instanceof TvInputEntry)) {
            return false;
        }
        TvInputEntry obj = (TvInputEntry) o;
        if (isCustomTifInput() && obj.isCustomTifInput() && this.type == obj.type) {
            return true;
        }
        TvInputInfo tvInputInfo2 = this.info;
        if (tvInputInfo2 == null || (tvInputInfo = obj.info) == null || !tvInputInfo2.equals(tvInputInfo)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.type), this.info);
    }

    static class InputsComparator implements Comparator<TvInputEntry> {
        private Context context;

        InputsComparator(Context context2) {
            this.context = context2;
        }

        public int compare(TvInputEntry lhs, TvInputEntry rhs) {
            boolean lIsPhysical;
            boolean disconnectedR = false;
            if (rhs == null) {
                if (lhs == null) {
                    return 0;
                }
                return -1;
            } else if (lhs == null) {
                return 1;
            } else {
                if (TifInputsManager.getInstance(this.context).shouldDisableDisconnectedInputs()) {
                    boolean disconnectedL = lhs.state == 2;
                    if (rhs.state == 2) {
                        disconnectedR = true;
                    }
                    if (disconnectedL != disconnectedR) {
                        if (disconnectedL) {
                            return 1;
                        }
                        return -1;
                    }
                }
                if (lhs.priority != rhs.priority) {
                    return lhs.priority - rhs.priority;
                }
                if (lhs.type == 0 && rhs.type == 0 && TifInputsManager.isPhysicalTuner(this.context.getPackageManager(), rhs.info) != (lIsPhysical = TifInputsManager.isPhysicalTuner(this.context.getPackageManager(), lhs.info))) {
                    if (lIsPhysical) {
                        return -1;
                    }
                    return 1;
                } else if (lhs.sortKey != rhs.sortKey) {
                    return rhs.sortKey - lhs.sortKey;
                } else {
                    if (!TextUtils.equals(lhs.sortingParentLabel, rhs.sortingParentLabel)) {
                        return lhs.sortingParentLabel.compareToIgnoreCase(rhs.sortingParentLabel);
                    }
                    return lhs.label.compareToIgnoreCase(rhs.label);
                }
            }
        }
    }
}
