package com.google.android.tvlauncher.data;

import android.util.LongSparseArray;
import com.google.android.tvlauncher.model.HomeChannel;
import com.google.android.tvlauncher.util.ChannelConfigurationInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

class PinnedChannelOrderManager {
    private PinnedChannelsComparator pinnedChannelsComparator = new PinnedChannelsComparator();
    private LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig;
    private List<HomeChannel> sortedPinnedChannels;
    private HashSet<Long> tempFilteredOutPinnedChannelIds = new HashSet<>();

    PinnedChannelOrderManager() {
    }

    /* access modifiers changed from: package-private */
    public void setPinnedChannels(List<HomeChannel> pinnedChannels, LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig2) {
        this.pinnedChannelsConfig = pinnedChannelsConfig2;
        this.pinnedChannelsComparator.setPinnedChannelsConfig(pinnedChannelsConfig2);
        pinnedChannels.sort(this.pinnedChannelsComparator);
        this.sortedPinnedChannels = pinnedChannels;
    }

    /* access modifiers changed from: package-private */
    public void filterOutPinnedChannels(List<HomeChannel> channels) {
        if (this.sortedPinnedChannels.size() != 0) {
            List<HomeChannel> filteredChannels = new ArrayList<>(channels.size());
            boolean shouldUpdate = false;
            for (HomeChannel channel : channels) {
                if (this.pinnedChannelsConfig.get(channel.getId()) == null) {
                    filteredChannels.add(channel);
                } else {
                    shouldUpdate = true;
                    this.tempFilteredOutPinnedChannelIds.add(Long.valueOf(channel.getId()));
                }
            }
            if (shouldUpdate) {
                channels.clear();
                channels.addAll(filteredChannels);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyPinnedChannelOrder(List<HomeChannel> channels) {
        for (HomeChannel pinnedChannel : this.sortedPinnedChannels) {
            if (this.tempFilteredOutPinnedChannelIds.contains(Long.valueOf(pinnedChannel.getId()))) {
                int pinnedChannelPosition = this.pinnedChannelsConfig.get(pinnedChannel.getId()).getChannelPosition();
                if (pinnedChannelPosition > channels.size()) {
                    pinnedChannelPosition = channels.size();
                }
                channels.add(pinnedChannelPosition, pinnedChannel);
            }
        }
        this.tempFilteredOutPinnedChannelIds.clear();
    }

    /* access modifiers changed from: package-private */
    public boolean isPinnedChannel(long channelId) {
        return this.pinnedChannelsConfig.get(channelId) != null;
    }

    private static class PinnedChannelsComparator implements Comparator<HomeChannel> {
        private LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig;

        private PinnedChannelsComparator() {
        }

        /* access modifiers changed from: package-private */
        public void setPinnedChannelsConfig(LongSparseArray<ChannelConfigurationInfo> pinnedChannelsConfig2) {
            this.pinnedChannelsConfig = pinnedChannelsConfig2;
        }

        public int compare(HomeChannel channel1, HomeChannel channel2) {
            int leftChannelPosition = this.pinnedChannelsConfig.get(channel1.getId()).getChannelPosition();
            int rightChannelPosition = this.pinnedChannelsConfig.get(channel2.getId()).getChannelPosition();
            if (leftChannelPosition == rightChannelPosition) {
                if (this.pinnedChannelsConfig.get(channel1.getId()).isGoogleConfig()) {
                    return 1;
                }
                if (this.pinnedChannelsConfig.get(channel2.getId()).isGoogleConfig()) {
                    return -1;
                }
            }
            return Integer.compare(leftChannelPosition, rightChannelPosition);
        }
    }
}
