package com.google.android.tvlauncher.inputs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gtalkservice.GTalkServiceConstants;
import com.google.android.tvlauncher.util.Util;

public class CustomTvInputEntry implements Comparable<CustomTvInputEntry> {
    public static String[] PROJECTION = {"input_id", GTalkServiceConstants.EXTRA_INTENT_STATE, "type", "parent_id", "title", TvContractCompat.PreviewProgramColumns.COLUMN_INTENT_URI, "icon_uri", "selected_icon_uri", "active_icon_uri", "selected_active_icon_uri", "group_id"};
    private Uri activeIconUri;
    private int finalSortIndex;
    private String groupId;
    private Uri iconUri;

    /* renamed from: id */
    private String f157id;
    private int initialSortIndex;
    private Intent intent;
    private String intentUri;
    private String label;
    private String parentId;
    private String parentLabel;
    private Uri selectedActiveIconUri;
    private Uri selectedIconUri;
    private int state;
    private String type;

    private CustomTvInputEntry(String id, int state2, String type2, String parentId2, String label2, String intentUri2, Uri iconUri2, Uri selectedIconUri2, Uri activeIconUri2, Uri selectedActiveIconUri2, String groupId2) {
        this.f157id = id;
        this.state = state2;
        this.type = type2;
        this.parentId = parentId2;
        this.label = label2;
        this.intentUri = intentUri2;
        this.iconUri = iconUri2;
        this.selectedIconUri = selectedIconUri2;
        this.activeIconUri = activeIconUri2;
        this.selectedActiveIconUri = selectedActiveIconUri2;
        this.groupId = groupId2;
    }

    /* access modifiers changed from: package-private */
    public void preloadIcon(Context context, RequestOptions requestOptions) {
        if (this.iconUri != null) {
            Glide.with(context).load(this.iconUri).apply((BaseRequestOptions<?>) requestOptions).preload();
        }
        if (this.selectedIconUri != null) {
            Glide.with(context).load(this.selectedIconUri).apply((BaseRequestOptions<?>) requestOptions).preload();
        }
        if (this.activeIconUri != null) {
            Glide.with(context).load(this.activeIconUri).apply((BaseRequestOptions<?>) requestOptions).preload();
        }
        if (this.selectedActiveIconUri != null) {
            Glide.with(context).load(this.selectedActiveIconUri).apply((BaseRequestOptions<?>) requestOptions).preload();
        }
    }

    /* JADX INFO: Multiple debug info for r1v6 java.lang.String: [D('iconUri' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r2v7 java.lang.String: [D('index' int), D('selectedIconUri' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r3v1 java.lang.String: [D('activeIconUri' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r4v1 java.lang.String: [D('index' int), D('selectedActiveIconUri' java.lang.String)] */
    public static CustomTvInputEntry fromCursor(Context context, Cursor cursor) {
        int index = 0 + 1;
        int index2 = index + 1;
        Builder state2 = new Builder().setId(cursor.getString(0)).setState(cursor.getInt(index));
        int index3 = index2 + 1;
        Builder type2 = state2.setType(cursor.getString(index2));
        int index4 = index3 + 1;
        Builder parentId2 = type2.setParentId(cursor.getString(index3));
        int index5 = index4 + 1;
        Builder label2 = parentId2.setLabel(cursor.getString(index4));
        int index6 = index5 + 1;
        Builder builder = label2.setIntentUri(cursor.getString(index5));
        int index7 = index6 + 1;
        int index8 = index7 + 1;
        int index9 = index8 + 1;
        int index10 = index9 + 1;
        builder.setIconUri(Util.getUri(cursor.getString(index6))).setSelectedIconUri(Util.getUri(cursor.getString(index7))).setActiveIconUri(Util.getUri(cursor.getString(index8))).setSelectedActiveIconUri(Util.getUri(cursor.getString(index9))).setGroupId(cursor.getString(index10));
        return builder.build(context);
    }

    public String getId() {
        return this.f157id;
    }

    public int getState() {
        return this.state;
    }

    public String getType() {
        return this.type;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getLabel() {
        return this.label;
    }

    public String getIntentUri() {
        return this.intentUri;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public void setIntent(Intent intent2) {
        this.intent = intent2;
    }

    public Uri getIconUri() {
        return this.iconUri;
    }

    public Uri getSelectedIconUri() {
        return this.selectedIconUri;
    }

    public Uri getActiveIconUri() {
        return this.activeIconUri;
    }

    public Uri getSelectedActiveIconUri() {
        return this.selectedActiveIconUri;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getParentLabel() {
        return this.parentLabel;
    }

    public void setParentLabel(String parentLabel2) {
        this.parentLabel = parentLabel2;
    }

    public void setInitialSortIndex(int initialSortIndex2) {
        this.initialSortIndex = initialSortIndex2;
    }

    public int getInitialSortIndex() {
        return this.initialSortIndex;
    }

    public void setFinalSortIndex(int finalSortIndex2) {
        this.finalSortIndex = finalSortIndex2;
    }

    public int compareTo(CustomTvInputEntry o) {
        int compare = Integer.compare(this.finalSortIndex, o.finalSortIndex);
        if (compare == 0) {
            return Integer.compare(this.initialSortIndex, o.initialSortIndex);
        }
        return compare;
    }

    public String toString() {
        return this.label;
    }

    public static class Builder {
        private Uri activeIconUri;
        private String groupId;
        private Uri iconUri;

        /* renamed from: id */
        private String f158id;
        private String intentUri;
        private String label;
        private String parentId;
        private Uri selectedActiveIconUri;
        private Uri selectedIconUri;
        private int state;
        private String type;

        public Builder setId(String id) {
            this.f158id = id;
            return this;
        }

        public Builder setState(int state2) {
            this.state = state2;
            return this;
        }

        public Builder setType(String type2) {
            this.type = type2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setParentId(String parentId2) {
            this.parentId = parentId2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setLabel(String label2) {
            this.label = label2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setIntentUri(String intentUri2) {
            this.intentUri = intentUri2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setIconUri(Uri iconUri2) {
            this.iconUri = iconUri2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setSelectedIconUri(Uri selectedIconUri2) {
            this.selectedIconUri = selectedIconUri2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setActiveIconUri(Uri activeIconUri2) {
            this.activeIconUri = activeIconUri2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setSelectedActiveIconUri(Uri selectedActiveIconUri2) {
            this.selectedActiveIconUri = selectedActiveIconUri2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setGroupId(String groupId2) {
            this.groupId = groupId2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public CustomTvInputEntry build(Context context) {
            if (this.iconUri == null) {
                Integer type2 = InputsManagerUtil.getType(this.type);
                if (type2 != null) {
                    this.iconUri = Util.getDrawableUri(context, InputsManagerUtil.getIconResourceId(type2.intValue()).intValue());
                }
            }
            return new CustomTvInputEntry(this.f158id, this.state, this.type, this.parentId, this.label, this.intentUri, this.iconUri, this.selectedIconUri, this.activeIconUri, this.selectedActiveIconUri, this.groupId);
        }
    }
}
