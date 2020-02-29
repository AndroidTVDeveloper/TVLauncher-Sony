package com.google.android.tvlauncher.model;

import android.text.TextUtils;
import java.util.Objects;

public class ChannelPackage {
    private int channelCount;
    private boolean onlyChannelBrowsable;
    private String onlyChannelDisplayName;
    private boolean onlyChannelEmpty;
    private long onlyChannelId;
    private boolean onlyChannelRemovable = true;
    private String packageName;

    public ChannelPackage(String packageName2, int channelCount2) {
        this.packageName = packageName2;
        this.channelCount = channelCount2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getChannelCount() {
        return this.channelCount;
    }

    public void setOnlyChannelAttributes(Channel onlyChannel) {
        this.onlyChannelDisplayName = onlyChannel.getDisplayName();
        this.onlyChannelId = onlyChannel.getId();
        this.onlyChannelBrowsable = onlyChannel.isBrowsable();
        this.onlyChannelEmpty = onlyChannel.isEmpty();
        this.onlyChannelRemovable = onlyChannel.canRemove();
    }

    public String getOnlyChannelDisplayName() {
        return this.onlyChannelDisplayName;
    }

    public long getOnlyChannelId() {
        return this.onlyChannelId;
    }

    public boolean isOnlyChannelBrowsable() {
        return this.onlyChannelBrowsable;
    }

    public boolean isOnlyChannelEmpty() {
        return this.onlyChannelEmpty;
    }

    public boolean isOnlyChannelRemovable() {
        return this.onlyChannelRemovable;
    }

    public String toString() {
        String str = this.packageName;
        int i = this.channelCount;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 56);
        sb.append("ChannelPackage{packageName='");
        sb.append(str);
        sb.append('\'');
        sb.append(", channelCount=");
        sb.append(i);
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object obj) {
        return (obj instanceof ChannelPackage) && TextUtils.equals(this.packageName, ((ChannelPackage) obj).getPackageName());
    }

    public int hashCode() {
        return Objects.hashCode(this.packageName);
    }
}
