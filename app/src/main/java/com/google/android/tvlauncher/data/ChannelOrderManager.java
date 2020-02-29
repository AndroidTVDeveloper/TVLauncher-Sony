package com.google.android.tvlauncher.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import com.google.android.tvlauncher.model.HomeChannel;
import com.google.android.tvlauncher.util.ChannelConfigurationInfo;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChannelOrderManager {
    private static final boolean DEBUG = false;
    private static final int DIRECTION_DOWN = 1;
    private static final int DIRECTION_UP = -1;
    private static final LongSparseArray<Integer> EMPTY_OOB_CHANNEL_POSITIONS = new LongSparseArray<>(0);
    private static final String KEY_ALL_CHANNELS_POSITIONS = "ALL_CHANNELS_POSITIONS";
    private static final String KEY_FIRST_START_TIMESTAMP = "FIRST_START_TIMESTAMP";
    private static final String KEY_LIVE_TV_CHANNEL_LAST_POSITION = "LIVE_TV_CHANNEL_LAST_POSITION";
    private static final String KEY_LOGGED_CHANNEL_ID_ORDER = "LOGGED_CHANNEL_ID_ORDER";
    private static final String KEY_ORDERED_CHANNEL_IDS = "ORDERED_CHANNEL_IDS";
    private static final String KEY_SPONSORED_GOOGLE_CHANNEL_LAST_OOB_POSITION = "SPONSORED_GOOGLE_CHANNEL_LAST_OOB_POSITION";
    private static final String KEY_SPONSORED_GOOGLE_CHANNEL_LAST_POSITION = "SPONSORED_GOOGLE_CHANNEL_LAST_POSITION";
    private static final String KEY_USER_HAS_MANAGED_CHANNELS = "USER_HAS_MANAGED_CHANNELS";
    private static final long LIVE_TV_CHANNEL_NO_ID = -1;
    private static final int LIVE_TV_CHANNEL_NO_LAST_POSITION = -1;
    private static final int LIVE_TV_CHANNEL_NO_OOB_POSITION = -1;
    private static final int MAX_OOB_CHANNEL_PACKAGES = 20;
    private static final long MAX_TIME_OOB_ORDER_HONORED = 172800000;
    private static final int NO_POSITION = -1;
    private static final String PREF_CHANNEL_ORDER_MANAGER = "CHANNEL_ORDER_MANAGER";
    private static final long SPONSORED_GOOGLE_CHANNEL_NO_ID = -1;
    private static final int SPONSORED_GOOGLE_CHANNEL_NO_LAST_POSITION = -1;
    private static final int SPONSORED_GOOGLE_CHANNEL_NO_OOB_POSITION = -1;
    private static final String TAG = "ChannelOrderManager";
    private LongSparseArray<Integer> allChannelPositions;
    private ChannelComparator channelComparator;
    private LongSparseArray<Integer> channelPositions;
    private List<HomeChannel> channels;
    private List<HomeChannelsObserver> channelsObservers;
    private final Context context;
    private long firstStartTimestamp;
    private boolean isNewChannelAdded;
    private long liveTvChannelId;
    private int liveTvChannelLastPosition;
    private int liveTvChannelOobPosition;
    private Map<ChannelConfigurationInfo, List<Integer>> oobPackageKeyPositions;
    private final List<ChannelConfigurationInfo> outOfBoxPackages;
    private PinnedChannelOrderManager pinnedChannelOrderManager;
    private long sponsoredGoogleChannelId;
    private int sponsoredGoogleChannelLastOobPosition;
    private int sponsoredGoogleChannelLastPosition;
    private int sponsoredGoogleChannelOobPosition;
    private boolean userHasManagedChannels;

    @Retention(RetentionPolicy.SOURCE)
    private @interface Direction {
    }

    ChannelOrderManager(Context context2, List<ChannelConfigurationInfo> outOfBoxPackages2) {
        this(context2, outOfBoxPackages2, -1);
    }

    ChannelOrderManager(Context context2, List<ChannelConfigurationInfo> outOfBoxPackages2, int liveTvChannelOobPosition2) {
        this.channelComparator = new ChannelComparator();
        this.liveTvChannelId = -1;
        this.liveTvChannelLastPosition = -1;
        this.liveTvChannelOobPosition = -1;
        this.sponsoredGoogleChannelId = -1;
        this.sponsoredGoogleChannelLastPosition = -1;
        this.sponsoredGoogleChannelLastOobPosition = -1;
        this.sponsoredGoogleChannelOobPosition = -1;
        this.context = context2.getApplicationContext();
        this.outOfBoxPackages = outOfBoxPackages2;
        this.liveTvChannelOobPosition = liveTvChannelOobPosition2;
        if (outOfBoxPackages2 != null && outOfBoxPackages2.size() > 0) {
            this.oobPackageKeyPositions = new HashMap();
            int i = 0;
            while (i < outOfBoxPackages2.size() && i < 20) {
                ChannelConfigurationInfo key = outOfBoxPackages2.get(i);
                if (this.oobPackageKeyPositions.containsKey(key)) {
                    this.oobPackageKeyPositions.get(key).add(Integer.valueOf(i));
                } else {
                    List<Integer> newList = new ArrayList<>();
                    newList.add(Integer.valueOf(i));
                    this.oobPackageKeyPositions.put(key, newList);
                }
                i++;
            }
        }
        readStateFromStorage();
    }

    public void setChannels(List<HomeChannel> channels2) {
        this.channels = channels2;
    }

    /* access modifiers changed from: package-private */
    public void setChannelsObservers(List<HomeChannelsObserver> channelsObservers2) {
        this.channelsObservers = channelsObservers2;
    }

    /* access modifiers changed from: package-private */
    public void setPinnedChannelOrderManager(PinnedChannelOrderManager pinnedChannelOrderManager2) {
        this.pinnedChannelOrderManager = pinnedChannelOrderManager2;
    }

    /* access modifiers changed from: package-private */
    public void setLiveTvChannelId(long liveTvChannelId2) {
        this.liveTvChannelId = liveTvChannelId2;
    }

    /* access modifiers changed from: package-private */
    public void setSponsoredGoogleChannelId(long sponsoredGoogleChannelId2) {
        this.sponsoredGoogleChannelId = sponsoredGoogleChannelId2;
    }

    /* access modifiers changed from: package-private */
    public void setSponsoredGoogleChannelOobPosition(int sponsoredGoogleChannelOobPosition2) {
        this.sponsoredGoogleChannelOobPosition = sponsoredGoogleChannelOobPosition2;
    }

    private void readStateFromStorage() {
        int position;
        char c = 0;
        SharedPreferences pref = this.context.getSharedPreferences(PREF_CHANNEL_ORDER_MANAGER, 0);
        String[] channelIds = TextUtils.split(pref.getString(KEY_ORDERED_CHANNEL_IDS, ""), ",");
        this.channelPositions = new LongSparseArray<>(channelIds.length);
        int position2 = 0;
        for (String channelId : channelIds) {
            try {
                position = position2 + 1;
                try {
                    this.channelPositions.put(Long.parseLong(channelId), Integer.valueOf(position2));
                } catch (NumberFormatException e) {
                }
            } catch (NumberFormatException e2) {
                position = position2;
                StringBuilder sb = new StringBuilder(String.valueOf(channelId).length() + 44);
                sb.append("Invalid channel ID: ");
                sb.append(channelId);
                sb.append(" at position ");
                sb.append(position);
                Log.e(TAG, sb.toString());
                position2 = position;
            }
            position2 = position;
        }
        String allChannelsString = pref.getString(KEY_ALL_CHANNELS_POSITIONS, "");
        if (allChannelsString.isEmpty()) {
            this.allChannelPositions = this.channelPositions.clone();
        } else {
            String[] channelStrings = TextUtils.split(allChannelsString, ",");
            this.allChannelPositions = new LongSparseArray<>(channelStrings.length);
            int length = channelStrings.length;
            int i = 0;
            while (i < length) {
                String channelString = channelStrings[i];
                try {
                    String[] channelPosition = TextUtils.split(channelString, "=");
                    if (channelPosition.length == 2) {
                        this.allChannelPositions.put(Long.parseLong(channelPosition[c]), Integer.valueOf(Integer.parseInt(channelPosition[1])));
                    } else {
                        StringBuilder sb2 = new StringBuilder(String.valueOf(channelString).length() + 44 + String.valueOf(allChannelsString).length());
                        sb2.append("Error parsing all channel positions ");
                        sb2.append(channelString);
                        sb2.append(" within ");
                        sb2.append(allChannelsString);
                        Log.e(TAG, sb2.toString());
                    }
                } catch (NumberFormatException e3) {
                    String valueOf = String.valueOf(allChannelsString);
                    Log.e(TAG, valueOf.length() != 0 ? "Invalid info in all channel positions ".concat(valueOf) : new String("Invalid info in all channel positions "));
                }
                i++;
                c = 0;
            }
        }
        this.userHasManagedChannels = pref.getBoolean(KEY_USER_HAS_MANAGED_CHANNELS, false);
        this.firstStartTimestamp = pref.getLong(KEY_FIRST_START_TIMESTAMP, -1);
        if (this.firstStartTimestamp == -1) {
            this.firstStartTimestamp = System.currentTimeMillis();
            pref.edit().putLong(KEY_FIRST_START_TIMESTAMP, this.firstStartTimestamp).apply();
        }
        this.liveTvChannelLastPosition = pref.getInt(KEY_LIVE_TV_CHANNEL_LAST_POSITION, -1);
        this.sponsoredGoogleChannelLastPosition = pref.getInt(KEY_SPONSORED_GOOGLE_CHANNEL_LAST_POSITION, -1);
        this.sponsoredGoogleChannelLastOobPosition = pref.getInt(KEY_SPONSORED_GOOGLE_CHANNEL_LAST_OOB_POSITION, -1);
    }

    private void saveChannelOrderToStorage() {
        SharedPreferences.Editor editor = this.context.getSharedPreferences(PREF_CHANNEL_ORDER_MANAGER, 0).edit();
        String channelOrder = "";
        if (this.channelPositions.size() > 0) {
            StringBuilder sb = new StringBuilder(this.channels.size() * 12);
            for (HomeChannel channel : this.channels) {
                sb.append(channel.getId());
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            channelOrder = sb.toString();
        }
        String allChannelOrder = "";
        if (this.allChannelPositions.size() > 0) {
            StringBuilder sb2 = new StringBuilder(this.allChannelPositions.size() * 15);
            for (int i = 0; i < this.allChannelPositions.size(); i++) {
                sb2.append(this.allChannelPositions.keyAt(i));
                sb2.append('=');
                sb2.append(this.allChannelPositions.valueAt(i));
                sb2.append(',');
            }
            sb2.setLength(sb2.length() - 1);
            allChannelOrder = sb2.toString();
        }
        editor.putString(KEY_ORDERED_CHANNEL_IDS, channelOrder).putString(KEY_ALL_CHANNELS_POSITIONS, allChannelOrder).putInt(KEY_LIVE_TV_CHANNEL_LAST_POSITION, this.liveTvChannelLastPosition).putInt(KEY_SPONSORED_GOOGLE_CHANNEL_LAST_POSITION, this.sponsoredGoogleChannelLastPosition).putInt(KEY_SPONSORED_GOOGLE_CHANNEL_LAST_OOB_POSITION, this.sponsoredGoogleChannelLastOobPosition).apply();
        sendChannelOrderChangeLogEventIfChanged(channelOrder);
    }

    private void updateSponsoredGoogleChannelLastOobPosition() {
        SharedPreferences.Editor editor = this.context.getSharedPreferences(PREF_CHANNEL_ORDER_MANAGER, 0).edit();
        this.sponsoredGoogleChannelLastOobPosition = this.sponsoredGoogleChannelOobPosition;
        editor.putInt(KEY_SPONSORED_GOOGLE_CHANNEL_LAST_OOB_POSITION, this.sponsoredGoogleChannelLastOobPosition).apply();
    }

    /* access modifiers changed from: package-private */
    public void notifyUserHasManagedChannels() {
        this.userHasManagedChannels = true;
        this.context.getSharedPreferences(PREF_CHANNEL_ORDER_MANAGER, 0).edit().putBoolean(KEY_USER_HAS_MANAGED_CHANNELS, true).apply();
    }

    private void refreshChannelPositions() {
        List<HomeChannel> list = this.channels;
        if (list != null) {
            LongSparseArray<Integer> newChannelPositions = new LongSparseArray<>(list.size());
            int position = 0;
            for (HomeChannel channel : this.channels) {
                newChannelPositions.put(channel.getId(), Integer.valueOf(position));
                position++;
            }
            this.channelPositions = newChannelPositions;
            if (this.isNewChannelAdded) {
                this.allChannelPositions = this.channelPositions.clone();
                this.isNewChannelAdded = false;
            }
            updateLiveTvChannelLastPosition();
            updateSponsoredGoogleChannelLastPosition();
            saveChannelOrderToStorage();
            return;
        }
        throw new IllegalStateException("Channels must be set");
    }

    /* access modifiers changed from: package-private */
    public void onNewChannelAdded(long addedChannelId) {
        if (!this.isNewChannelAdded && this.allChannelPositions.get(addedChannelId) == null) {
            this.isNewChannelAdded = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onChannelRemoved(long channelId) {
        this.allChannelPositions.remove(channelId);
        refreshChannelPositions();
    }

    /* access modifiers changed from: package-private */
    public void onEmptyChannelHidden(HomeChannel channel) {
        if (!channel.isLegacy()) {
            this.allChannelPositions.remove(channel.getId());
        }
        refreshChannelPositions();
    }

    /* access modifiers changed from: package-private */
    public void applyOrder(List<HomeChannel> channels2) {
        int i;
        LongSparseArray<Integer> outOfBoxChannelPositions = getOutOfBoxChannelPositions(channels2);
        boolean shouldAddBackPinnedChannels = false;
        if (outOfBoxChannelPositions == EMPTY_OOB_CHANNEL_POSITIONS) {
            this.pinnedChannelOrderManager.filterOutPinnedChannels(channels2);
            shouldAddBackPinnedChannels = true;
        }
        if (outOfBoxChannelPositions == EMPTY_OOB_CHANNEL_POSITIONS) {
            long j = this.liveTvChannelId;
            if (j != -1 && this.liveTvChannelLastPosition == -1) {
                this.allChannelPositions.put(j, Integer.valueOf(this.liveTvChannelOobPosition));
            }
            long j2 = this.sponsoredGoogleChannelId;
            if (j2 != -1) {
                if (this.sponsoredGoogleChannelLastPosition == -1) {
                    this.allChannelPositions.put(j2, Integer.valueOf(this.sponsoredGoogleChannelOobPosition));
                } else if (this.sponsoredGoogleChannelOobPosition != this.sponsoredGoogleChannelLastOobPosition) {
                    updateSponsoredGoogleChannelLastOobPosition();
                    if (this.allChannelPositions.get(this.sponsoredGoogleChannelId) == null || (i = this.sponsoredGoogleChannelOobPosition) <= this.sponsoredGoogleChannelLastPosition) {
                        this.allChannelPositions.put(this.sponsoredGoogleChannelId, Integer.valueOf(this.sponsoredGoogleChannelOobPosition));
                    } else {
                        this.allChannelPositions.put(this.sponsoredGoogleChannelId, Integer.valueOf(i + 1));
                    }
                }
            }
        }
        this.channelComparator.setLiveTvChannelId(this.liveTvChannelId);
        this.channelComparator.setSponsoredGoogleChannelId(this.sponsoredGoogleChannelId);
        this.channelComparator.setChannelPositions(this.allChannelPositions);
        this.channelComparator.setOutOfBoxChannelPositions(outOfBoxChannelPositions);
        channels2.sort(this.channelComparator);
        if (shouldAddBackPinnedChannels) {
            this.pinnedChannelOrderManager.applyPinnedChannelOrder(channels2);
        }
        setChannels(channels2);
        refreshChannelPositions();
    }

    private LongSparseArray<Integer> getOutOfBoxChannelPositions(List<HomeChannel> channels2) {
        int i;
        int i2;
        long timePassedSinceStart = System.currentTimeMillis() - this.firstStartTimestamp;
        if (this.userHasManagedChannels || timePassedSinceStart > MAX_TIME_OOB_ORDER_HONORED || this.oobPackageKeyPositions == null) {
            return EMPTY_OOB_CHANNEL_POSITIONS;
        }
        Map<ChannelConfigurationInfo, Iterator<Integer>> oobPackageKeyPositionIterators = new HashMap<>();
        for (ChannelConfigurationInfo key : this.oobPackageKeyPositions.keySet()) {
            oobPackageKeyPositionIterators.put(key, this.oobPackageKeyPositions.get(key).iterator());
        }
        LongSparseArray<Integer> oobChannelPositions = new LongSparseArray<>(Math.min(this.outOfBoxPackages.size(), 20) + 1);
        for (int i3 = 0; i3 < channels2.size() && oobPackageKeyPositionIterators.size() != 0; i3++) {
            HomeChannel channel = channels2.get(i3);
            ChannelConfigurationInfo key2 = new ChannelConfigurationInfo(channel.getPackageName(), channel.getSystemChannelKey());
            if (!oobPackageKeyPositionIterators.containsKey(key2)) {
                key2 = new ChannelConfigurationInfo(channel.getPackageName(), (String) null);
            }
            Iterator<Integer> positionValues = oobPackageKeyPositionIterators.get(key2);
            if (positionValues != null) {
                oobChannelPositions.put(channel.getId(), (Integer) positionValues.next());
                if (!positionValues.hasNext()) {
                    oobPackageKeyPositionIterators.remove(key2);
                }
            }
        }
        long j = this.liveTvChannelId;
        if (!(j == -1 || (i2 = this.liveTvChannelOobPosition) == -1)) {
            oobChannelPositions.put(j, Integer.valueOf(i2));
        }
        long j2 = this.sponsoredGoogleChannelId;
        if (!(j2 == -1 || (i = this.sponsoredGoogleChannelOobPosition) == -1)) {
            oobChannelPositions.put(j2, Integer.valueOf(i));
        }
        return oobChannelPositions;
    }

    /* access modifiers changed from: package-private */
    public Integer getChannelPosition(long channelId) {
        return this.channelPositions.get(channelId);
    }

    private int getNextChannelPositionToMoveTo(long channelId, int direction) {
        Integer position = this.channelPositions.get(channelId);
        if (position == null || this.pinnedChannelOrderManager.isPinnedChannel(channelId)) {
            return -1;
        }
        int i = position.intValue() + direction;
        while (i >= 0 && i < this.channels.size()) {
            if (!this.pinnedChannelOrderManager.isPinnedChannel(this.channels.get(i).getId())) {
                return i;
            }
            i += direction;
        }
        return -1;
    }

    public boolean canMoveChannelUp(long channelId) {
        return getNextChannelPositionToMoveTo(channelId, -1) != -1;
    }

    public boolean canMoveChannelDown(long channelId) {
        return getNextChannelPositionToMoveTo(channelId, 1) != -1;
    }

    public int moveChannelUp(long channelId) {
        int replacementPosition = getNextChannelPositionToMoveTo(channelId, -1);
        if (replacementPosition != -1) {
            return swapChannels(this.channelPositions.get(channelId).intValue(), replacementPosition);
        }
        StringBuilder sb = new StringBuilder(42);
        sb.append("Can't move channel ");
        sb.append(channelId);
        sb.append(" up");
        throw new IllegalArgumentException(sb.toString());
    }

    public int moveChannelDown(long channelId) {
        int replacementPosition = getNextChannelPositionToMoveTo(channelId, 1);
        if (replacementPosition != -1) {
            return swapChannels(this.channelPositions.get(channelId).intValue(), replacementPosition);
        }
        StringBuilder sb = new StringBuilder(44);
        sb.append("Can't move channel ");
        sb.append(channelId);
        sb.append(" down");
        throw new IllegalArgumentException(sb.toString());
    }

    private int swapChannels(int position, int replacementPosition) {
        notifyUserHasManagedChannels();
        HomeChannel channel = this.channels.get(position);
        HomeChannel replacementChannel = this.channels.get(replacementPosition);
        this.channelPositions.put(channel.getId(), Integer.valueOf(replacementPosition));
        this.channelPositions.put(replacementChannel.getId(), Integer.valueOf(position));
        this.channels.set(replacementPosition, channel);
        this.channels.set(position, replacementChannel);
        updateLiveTvChannelLastPosition();
        updateSponsoredGoogleChannelLastPosition();
        notifyChannelMoved(position, replacementPosition);
        this.allChannelPositions = this.channelPositions.clone();
        saveChannelOrderToStorage();
        return replacementPosition;
    }

    private void updateLiveTvChannelLastPosition() {
        Integer liveTvChannelPosition = this.channelPositions.get(this.liveTvChannelId);
        if (liveTvChannelPosition != null) {
            this.liveTvChannelLastPosition = liveTvChannelPosition.intValue();
        }
    }

    private void updateSponsoredGoogleChannelLastPosition() {
        Integer sponsoredGoogleChannelPosition = this.channelPositions.get(this.sponsoredGoogleChannelId);
        if (sponsoredGoogleChannelPosition != null) {
            this.sponsoredGoogleChannelLastPosition = sponsoredGoogleChannelPosition.intValue();
        }
    }

    private void notifyChannelMoved(int currentPosition, int newPosition) {
        List<HomeChannelsObserver> list = this.channelsObservers;
        if (list != null) {
            for (HomeChannelsObserver observer : list) {
                observer.onChannelMove(currentPosition, newPosition);
            }
        }
    }

    private void sendChannelOrderChangeLogEventIfChanged(String channelOrder) {
        SharedPreferences prefs = this.context.getSharedPreferences(PREF_CHANNEL_ORDER_MANAGER, 0);
        String loggedChannelIds = prefs.getString(KEY_LOGGED_CHANNEL_ID_ORDER, null);
        Intent intent = new Intent(Constants.ACTION_CHANNEL_ORDER_CHANGE_LOG_EVENT).putExtra(Constants.EXTRA_CHANNEL_IDS, channelOrder);
        intent.setPackage(Constants.TVRECOMMENDATIONS_PACKAGE_NAME);
        if (loggedChannelIds == null) {
            List<ResolveInfo> receivers = this.context.getPackageManager().queryBroadcastReceivers(intent, 0);
            if (receivers != null && !receivers.isEmpty()) {
                this.context.sendBroadcast(intent);
                prefs.edit().putString(KEY_LOGGED_CHANNEL_ID_ORDER, channelOrder).apply();
            }
        } else if (!TextUtils.equals(channelOrder, loggedChannelIds)) {
            this.context.sendBroadcast(intent);
            prefs.edit().putString(KEY_LOGGED_CHANNEL_ID_ORDER, channelOrder).apply();
        }
    }

    private static class ChannelComparator implements Comparator<HomeChannel> {
        private LongSparseArray<Integer> channelPositions;
        private long liveTvChannelId;
        private LongSparseArray<Integer> oobChannelPositions;
        private long sponsoredGoogleChannelId;

        private ChannelComparator() {
            this.liveTvChannelId = -1;
            this.sponsoredGoogleChannelId = -1;
        }

        /* access modifiers changed from: package-private */
        public void setLiveTvChannelId(long liveTvChannelId2) {
            this.liveTvChannelId = liveTvChannelId2;
        }

        /* access modifiers changed from: package-private */
        public void setSponsoredGoogleChannelId(long sponsoredGoogleChannelId2) {
            this.sponsoredGoogleChannelId = sponsoredGoogleChannelId2;
        }

        /* access modifiers changed from: package-private */
        public void setChannelPositions(LongSparseArray<Integer> channelPositions2) {
            this.channelPositions = channelPositions2;
        }

        /* access modifiers changed from: package-private */
        public void setOutOfBoxChannelPositions(LongSparseArray<Integer> oobChannelPositions2) {
            this.oobChannelPositions = oobChannelPositions2;
        }

        private int compareChannel(long leftChannelId, long rightChannelId, int positionLeft, int positionRight) {
            if (positionLeft != positionRight) {
                return Integer.compare(positionLeft, positionRight);
            }
            long j = this.sponsoredGoogleChannelId;
            if (leftChannelId == j) {
                return -1;
            }
            if (rightChannelId == j) {
                return 1;
            }
            long j2 = this.liveTvChannelId;
            if (leftChannelId == j2) {
                return -1;
            }
            if (rightChannelId == j2) {
                return 1;
            }
            return 0;
        }

        public int compare(HomeChannel channelLeft, HomeChannel channelRight) {
            if (channelLeft == channelRight) {
                return 0;
            }
            Integer positionLeft = this.oobChannelPositions.get(channelLeft.getId());
            Integer positionRight = this.oobChannelPositions.get(channelRight.getId());
            if (positionLeft != null && positionRight != null) {
                return compareChannel(channelLeft.getId(), channelRight.getId(), positionLeft.intValue(), positionRight.intValue());
            }
            if (positionRight != null) {
                return 1;
            }
            if (positionLeft != null) {
                return -1;
            }
            Integer positionLeft2 = this.channelPositions.get(channelLeft.getId());
            Integer positionRight2 = this.channelPositions.get(channelRight.getId());
            if (positionLeft2 != null && positionRight2 != null) {
                return compareChannel(channelLeft.getId(), channelRight.getId(), positionLeft2.intValue(), positionRight2.intValue());
            }
            if (positionRight2 != null) {
                return 1;
            }
            if (positionLeft2 != null) {
                return -1;
            }
            return (channelLeft.getId() > channelRight.getId() ? 1 : (channelLeft.getId() == channelRight.getId() ? 0 : -1));
        }
    }
}
