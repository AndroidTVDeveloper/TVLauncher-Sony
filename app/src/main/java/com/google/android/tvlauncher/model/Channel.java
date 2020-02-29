package com.google.android.tvlauncher.model;

import android.database.Cursor;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.tvrecommendations.shared.util.Constants;

public class Channel implements Comparable<Channel> {
    public static final String[] PROJECTION = {"_id", TvContractCompat.Channels.COLUMN_DISPLAY_NAME, "browsable", "system_channel_key", "configuration_display_order", "logo_content_description", "package_name", "internal_provider_data"};
    private boolean browsable;
    private boolean canRemove = true;
    private int configurationDisplayOrder;
    private String displayName;

    /* renamed from: id */
    private long f160id;
    private boolean isEmpty;
    private boolean isSponsored;
    private String logoContentDescription;
    private String packageName;
    private String systemChannelKey;

    /* JADX INFO: Multiple debug info for r2v7 byte[]: [D('packageNameBlob' byte[]), D('index' int)] */
    public static Channel fromCursor(Cursor cursor) {
        Channel channel = new Channel();
        int index = 0 + 1;
        channel.f160id = cursor.getLong(0);
        int index2 = index + 1;
        channel.displayName = cursor.getString(index);
        int index3 = index2 + 1;
        channel.browsable = cursor.getInt(index2) == 1;
        int index4 = index3 + 1;
        channel.systemChannelKey = cursor.getString(index3);
        int index5 = index4 + 1;
        channel.configurationDisplayOrder = cursor.getInt(index4);
        int index6 = index5 + 1;
        channel.logoContentDescription = cursor.getString(index5);
        int index7 = index6 + 1;
        channel.packageName = cursor.getString(index6);
        if (Constants.TVRECOMMENDATIONS_PACKAGE_NAME.equals(channel.packageName) && cursor.getBlob(index7) != null) {
            int i = index7 + 1;
            byte[] packageNameBlob = cursor.getBlob(index7);
            String packageName2 = new String(packageNameBlob, 0, packageNameBlob.length - 1);
            if (!Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME.equals(packageName2)) {
                channel.packageName = packageName2;
            }
        }
        return channel;
    }

    public long getId() {
        return this.f160id;
    }

    public void setId(long id) {
        this.f160id = id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName2) {
        this.displayName = displayName2;
    }

    public boolean isBrowsable() {
        return this.browsable;
    }

    public void setBrowsable(boolean browsable2) {
        this.browsable = browsable2;
    }

    public String getSystemChannelKey() {
        return this.systemChannelKey;
    }

    public int getConfigurationDisplayOrder() {
        return this.configurationDisplayOrder;
    }

    public void setConfigurationDisplayOrder(int order) {
        this.configurationDisplayOrder = order;
    }

    public String getLogoContentDescription() {
        return this.logoContentDescription;
    }

    public void setLogoContentDescription(String logoContentDescription2) {
        this.logoContentDescription = logoContentDescription2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public void setIsEmpty(boolean empty) {
        this.isEmpty = empty;
    }

    public boolean isSponsored() {
        return this.isSponsored;
    }

    public void setSponsored(boolean sponsored) {
        this.isSponsored = sponsored;
    }

    public boolean canRemove() {
        return this.canRemove;
    }

    public void setCanRemove(boolean canRemove2) {
        this.canRemove = canRemove2;
    }

    public String toString() {
        long j = this.f160id;
        String str = this.displayName;
        boolean z = this.browsable;
        String str2 = this.packageName;
        int i = this.configurationDisplayOrder;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 121 + String.valueOf(str2).length());
        sb.append("Channel{id=");
        sb.append(j);
        sb.append(", displayName='");
        sb.append(str);
        sb.append('\'');
        sb.append(", browsable=");
        sb.append(z);
        sb.append(", packageName='");
        sb.append(str2);
        sb.append('\'');
        sb.append(", configurationDisplayOrder=");
        sb.append(i);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }

    public int compareTo(Channel o) {
        if (this.displayName == null && o.getDisplayName() == null) {
            return 0;
        }
        if (this.displayName == null) {
            return 1;
        }
        if (o.getDisplayName() == null) {
            return -1;
        }
        return this.displayName.compareToIgnoreCase(o.getDisplayName());
    }
}
