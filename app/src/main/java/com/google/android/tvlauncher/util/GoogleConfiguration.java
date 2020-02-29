package com.google.android.tvlauncher.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoogleConfiguration {
    private final List<ChannelConfigurationInfo> channelConfigs;
    private Map<String, ChannelConfigurationInfo> channelInfoMap = new HashMap();
    private final Set<String> sponsoredChannels;

    public GoogleConfiguration(List<ChannelConfigurationInfo> channelConfigs2, Set<String> sponsoredChannels2) {
        this.channelConfigs = channelConfigs2;
        this.sponsoredChannels = sponsoredChannels2;
        for (ChannelConfigurationInfo channelInfo : this.channelConfigs) {
            addChannelToChannelInfoMap(channelInfo);
        }
    }

    public List<ChannelConfigurationInfo> getChannelConfigs() {
        return this.channelConfigs;
    }

    public ChannelConfigurationInfo getChannelInfo(String packageName, String systemChannelKey) {
        return this.channelInfoMap.get(ChannelConfigurationInfo.getUniqueKey(packageName, systemChannelKey));
    }

    public boolean isSponsored(String packageName, String systemChannelKey) {
        return this.sponsoredChannels.contains(ChannelConfigurationInfo.getUniqueKey(packageName, systemChannelKey));
    }

    private void addChannelToChannelInfoMap(ChannelConfigurationInfo channelConfigurationInfo) {
        this.channelInfoMap.put(ChannelConfigurationInfo.getUniqueKey(channelConfigurationInfo.getPackageName(), channelConfigurationInfo.getSystemChannelKey()), channelConfigurationInfo);
    }
}
