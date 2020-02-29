package com.google.android.tvlauncher.model;

import android.database.Cursor;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.tvrecommendations.shared.util.Constants;

public class HomeChannel {
    public static final String[] PROJECTION = {"_id", TvContractCompat.Channels.COLUMN_DISPLAY_NAME, TvContractCompat.Channels.COLUMN_APP_LINK_INTENT_URI, "system_channel_key", "logo_content_description", "package_name", Constants.COLUMN_SUBTYPE, "internal_provider_data"};
    private boolean canMove;
    private boolean canRemove;
    private String displayName;

    /* renamed from: id */
    private long f161id;
    private boolean isSponsored;
    private String launchUri;
    private boolean legacy;
    private String logoContentDescription;
    private String packageName;
    private int subtype;
    private String systemChannelKey;

    /* JADX INFO: Multiple debug info for r2v7 byte[]: [D('packageNameBlob' byte[]), D('index' int)] */
    public static HomeChannel fromCursor(Cursor cursor) {
        HomeChannel channel = new HomeChannel();
        int index = 0 + 1;
        channel.f161id = cursor.getLong(0);
        int index2 = index + 1;
        channel.displayName = cursor.getString(index);
        int index3 = index2 + 1;
        channel.launchUri = cursor.getString(index2);
        int index4 = index3 + 1;
        channel.systemChannelKey = cursor.getString(index3);
        int index5 = index4 + 1;
        channel.logoContentDescription = cursor.getString(index4);
        int index6 = index5 + 1;
        channel.packageName = cursor.getString(index5);
        int index7 = index6 + 1;
        channel.subtype = cursor.getInt(index6);
        if (Constants.TVRECOMMENDATIONS_PACKAGE_NAME.equals(channel.packageName)) {
            int i = index7 + 1;
            byte[] packageNameBlob = cursor.getBlob(index7);
            if (packageNameBlob != null) {
                String packageName2 = new String(packageNameBlob, 0, packageNameBlob.length - 1);
                if (!Constants.SPONSORED_CHANNEL_LEGACY_PACKAGE_NAME.equals(packageName2)) {
                    channel.packageName = packageName2;
                    channel.legacy = true;
                }
            }
        }
        return channel;
    }

    private HomeChannel() {
        this.canMove = true;
        this.canRemove = true;
        this.legacy = false;
    }

    public HomeChannel(long id, String packageName2, String systemChannelKey2) {
        this.canMove = true;
        this.canRemove = true;
        this.legacy = false;
        this.f161id = id;
        this.packageName = packageName2;
        this.systemChannelKey = systemChannelKey2;
    }

    public HomeChannel(long id, String packageName2) {
        this(id, packageName2, null);
    }

    public long getId() {
        return this.f161id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getLaunchUri() {
        return this.launchUri;
    }

    public void setLaunchUri(String launchUri2) {
        this.launchUri = launchUri2;
    }

    public String getLogoContentDescription() {
        return this.logoContentDescription;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getSystemChannelKey() {
        return this.systemChannelKey;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public void setDisplayName(String displayName2) {
        this.displayName = displayName2;
    }

    public boolean isSponsored() {
        return this.isSponsored;
    }

    public void setSponsored(boolean sponsored) {
        this.isSponsored = sponsored;
    }

    public int getSubtype() {
        return this.subtype;
    }

    public boolean canMove() {
        return this.canMove;
    }

    public void setCanMove(boolean canMove2) {
        this.canMove = canMove2;
    }

    public boolean canRemove() {
        return this.canRemove;
    }

    public void setCanRemove(boolean canRemove2) {
        this.canRemove = canRemove2;
    }

    public boolean equals(Object obj) {
        return (obj instanceof HomeChannel) && this.f161id == ((HomeChannel) obj).getId();
    }

    public int hashCode() {
        return (int) this.f161id;
    }

    public String toString() {
        long j = this.f161id;
        String str = this.displayName;
        String str2 = this.packageName;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 68 + String.valueOf(str2).length());
        sb.append("HomeChannel{id=");
        sb.append(j);
        sb.append(", displayName='");
        sb.append(str);
        sb.append('\'');
        sb.append(", packageName='");
        sb.append(str2);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }
}
