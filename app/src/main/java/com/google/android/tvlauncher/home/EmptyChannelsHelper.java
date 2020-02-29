package com.google.android.tvlauncher.home;

import android.os.Handler;
import android.os.Message;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.home.view.ChannelView;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class EmptyChannelsHelper {
    private static final int EMPTY_CHANNEL_TIMER_CODE = 99;
    private static final long EMPTY_CHANNEL_TIMER_DELAY = 30000;
    /* access modifiers changed from: private */
    public TvDataManager dataManager;
    private EmptyChannelTimerHandler emptyChannelHandler = new EmptyChannelTimerHandler();
    /* access modifiers changed from: private */
    public Map<Long, Long> messageChannelIds = new HashMap();

    EmptyChannelsHelper(TvDataManager tvDataManager) {
        this.dataManager = tvDataManager;
    }

    /* access modifiers changed from: package-private */
    public void onChannelsChange() {
        Iterator<Map.Entry<Long, Long>> messageChannelIdsItr = this.messageChannelIds.entrySet().iterator();
        while (messageChannelIdsItr.hasNext()) {
            Map.Entry<Long, Long> channelIdEntry = messageChannelIdsItr.next();
            if (!this.dataManager.isHomeChannel(((Long) channelIdEntry.getKey()).longValue())) {
                this.emptyChannelHandler.removeMessages(99, this.messageChannelIds.get(channelIdEntry.getValue()));
                messageChannelIdsItr.remove();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onChannelEmptyStatusChange(long channelId) {
        if (this.dataManager.isChannelEmpty(channelId)) {
            startTimerIfNeeded(channelId);
        } else {
            stopTimerIfNeeded(channelId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onStart() {
        for (Map.Entry<Long, Long> entry : this.messageChannelIds.entrySet()) {
            EmptyChannelTimerHandler emptyChannelTimerHandler = this.emptyChannelHandler;
            emptyChannelTimerHandler.sendMessageDelayed(emptyChannelTimerHandler.obtainMessage(99, entry.getValue()), 30000);
        }
    }

    /* access modifiers changed from: package-private */
    public void onStop() {
        this.emptyChannelHandler.removeMessages(99);
    }

    /* access modifiers changed from: package-private */
    public void setChannelSelected(long channelId, boolean isSelected) {
        if (isSelected) {
            stopTimerIfNeeded(channelId);
        } else {
            startTimerIfNeeded(channelId);
        }
    }

    /* access modifiers changed from: package-private */
    public int getEmptyChannelState(int nonEmptyState) {
        int emptyState = nonEmptyState;
        switch (nonEmptyState) {
            case 0:
                return 16;
            case 1:
                return 17;
            case 2:
                return 18;
            case 3:
                return 19;
            case 4:
                return 20;
            case 5:
                return 21;
            case 6:
                return 22;
            case 7:
                return 23;
            case 8:
                return 24;
            case 9:
                return 25;
            case 10:
                return 26;
            case 11:
            case 13:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                String stateToString = ChannelView.stateToString(nonEmptyState);
                StringBuilder sb = new StringBuilder(stateToString.length() + 47);
                sb.append("Unsupported ChannelView state ");
                sb.append(stateToString);
                sb.append(" when it is empty");
                throw new IllegalStateException(sb.toString());
            case 12:
                return 27;
            case 14:
                return 28;
            case 15:
                return 29;
            default:
                return emptyState;
        }
    }

    private void startTimerIfNeeded(long channelId) {
        if (!this.messageChannelIds.containsKey(Long.valueOf(channelId))) {
            Long boxedChannelId = Long.valueOf(channelId);
            this.messageChannelIds.put(Long.valueOf(channelId), boxedChannelId);
            EmptyChannelTimerHandler emptyChannelTimerHandler = this.emptyChannelHandler;
            emptyChannelTimerHandler.sendMessageDelayed(emptyChannelTimerHandler.obtainMessage(99, boxedChannelId), 30000);
        }
    }

    private void stopTimerIfNeeded(long channelId) {
        if (this.messageChannelIds.containsKey(Long.valueOf(channelId))) {
            this.emptyChannelHandler.removeMessages(99, this.messageChannelIds.get(Long.valueOf(channelId)));
            this.messageChannelIds.remove(Long.valueOf(channelId));
        }
    }

    /* access modifiers changed from: package-private */
    public Map<Long, Long> getMessageChannelIds() {
        return this.messageChannelIds;
    }

    private static class EmptyChannelTimerHandler extends Handler {
        private final WeakReference<EmptyChannelsHelper> emptyChannelHelper;

        private EmptyChannelTimerHandler(EmptyChannelsHelper emptyChannelsHelper) {
            this.emptyChannelHelper = new WeakReference<>(emptyChannelsHelper);
        }

        public void handleMessage(Message msg) {
            EmptyChannelsHelper emptyChannelsHelper = this.emptyChannelHelper.get();
            if (emptyChannelsHelper != null && msg != null && msg.what == 99 && msg.obj != null) {
                long channelId = ((Long) msg.obj).longValue();
                emptyChannelsHelper.messageChannelIds.remove(Long.valueOf(channelId));
                emptyChannelsHelper.dataManager.hideEmptyChannel(channelId);
            }
        }
    }
}
