package com.google.android.tvlauncher.analytics;

import com.google.android.libraries.social.analytics.visualelement.VisualElementTag;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.common.logging.AncestryVisualElement;
import com.google.common.logging.proto2api.UserActionEnum;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;

public class LogEvent {
    private static final int ROW_INDEX_MULTIPLIER = 10000;
    private AncestryVisualElement.AncestryVisualElementProto.Builder ancestryVisualElement;
    private TvlauncherClientLog.AppLink.Builder appLink;
    private TvlauncherClientLog.Application.Builder application;
    private boolean bypassUsageReportingOptOut;
    private TvlauncherClientLog.Channel.Builder channel;
    private TvlauncherClientLog.ChannelCollection.Builder channelCollection;
    private TvlauncherLogEnum.TvLauncherEventCode eventCode;
    private String[] expectedParameters;
    private TvlauncherClientLog.Input.Builder input;
    private TvlauncherClientLog.InputCollection.Builder inputCollection;
    private TvlauncherClientLog.LaunchItem.Builder launchItem;
    private TvlauncherClientLog.LaunchItemCollection.Builder launchItemCollection;
    private TvlauncherClientLog.TvLauncherClientExtension.Builder logEntry;
    private TvlauncherClientLog.VisualElementMetadata.Builder metadata;
    private TvlauncherClientLog.Notification.Builder notification;
    private TvlauncherClientLog.NotificationCollection.Builder notificationCollection;
    private TvlauncherClientLog.Program.Builder program;
    private long timeoutMillis;
    private TvlauncherClientLog.VisualElementEntry.Builder visualElementsEntry;
    private TvlauncherClientLog.WatchNextChannel.Builder watchNextChannel;

    public LogEvent() {
    }

    public LogEvent(TvlauncherLogEnum.TvLauncherEventCode eventCode2) {
        this.eventCode = eventCode2;
    }

    public LogEvent bypassUsageReportingOptOut() {
        this.bypassUsageReportingOptOut = true;
        return this;
    }

    public boolean shouldBypassUsageReportingOptOut() {
        return this.bypassUsageReportingOptOut;
    }

    public LogEvent expectParameters(String... expectedParameters2) {
        this.expectedParameters = expectedParameters2;
        return this;
    }

    public LogEvent setParameterTimeout(long timeoutMillis2) {
        this.timeoutMillis = timeoutMillis2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public long getParameterTimeout() {
        return this.timeoutMillis;
    }

    /* access modifiers changed from: package-private */
    public String[] getExpectedParameters() {
        return this.expectedParameters;
    }

    public TvlauncherClientLog.TvLauncherClientExtension getClientLogEntry() {
        build();
        return (TvlauncherClientLog.TvLauncherClientExtension) this.logEntry.build();
    }

    public TvlauncherLogEnum.TvLauncherEventCode getEventCode() {
        return this.eventCode;
    }

    public LogEvent setEventCode(TvlauncherLogEnum.TvLauncherEventCode eventCode2) {
        this.eventCode = eventCode2;
        return this;
    }

    public LogEvent setVisualElementTag(VisualElementTag visualElementTag) {
        ensureVisualElements();
        this.ancestryVisualElement.setElementId(visualElementTag.f135id);
        return this;
    }

    public LogEvent setVisualElementIndex(int index) {
        ensureVisualElements();
        setVisualElementRowColumn(this.ancestryVisualElement.getElementIndex() / 10000, index);
        return this;
    }

    public LogEvent setVisualElementRowIndex(int index) {
        ensureVisualElements();
        setVisualElementRowColumn(index, this.ancestryVisualElement.getElementIndex());
        return this;
    }

    private void setVisualElementRowColumn(int row, int column) {
        this.ancestryVisualElement.setElementIndex((row * 10000) + (column % 10000));
    }

    public LogEvent pushParentVisualElementTag(VisualElementTag visualElementTag) {
        ensureVisualElements();
        if (!this.ancestryVisualElement.hasElementId()) {
            this.ancestryVisualElement.setElementId(visualElementTag.f135id);
        } else {
            this.ancestryVisualElement.addPathToRootElementId(visualElementTag.f135id);
        }
        return this;
    }

    public LogEvent setUserAction(UserActionEnum.UserAction userAction) {
        ensureVisualElements();
        this.ancestryVisualElement.setUserAction(userAction);
        return this;
    }

    public TvlauncherClientLog.Channel.Builder getChannel() {
        if (this.channel == null) {
            this.channel = TvlauncherClientLog.Channel.newBuilder();
        }
        return this.channel;
    }

    public boolean hasChannel() {
        return this.channel != null;
    }

    public TvlauncherClientLog.ChannelCollection.Builder getChannelCollection() {
        if (this.channelCollection == null) {
            this.channelCollection = TvlauncherClientLog.ChannelCollection.newBuilder();
        }
        return this.channelCollection;
    }

    public TvlauncherClientLog.Notification.Builder getNotification() {
        if (this.notification == null) {
            this.notification = TvlauncherClientLog.Notification.newBuilder();
        }
        return this.notification;
    }

    public TvlauncherClientLog.NotificationCollection.Builder getNotificationCollection() {
        if (this.notificationCollection == null) {
            this.notificationCollection = TvlauncherClientLog.NotificationCollection.newBuilder();
        }
        return this.notificationCollection;
    }

    public TvlauncherClientLog.Application.Builder getApplication() {
        if (this.application == null) {
            this.application = TvlauncherClientLog.Application.newBuilder();
        }
        return this.application;
    }

    public TvlauncherClientLog.AppLink.Builder getAppLink() {
        if (this.appLink == null) {
            this.appLink = TvlauncherClientLog.AppLink.newBuilder();
        }
        return this.appLink;
    }

    public TvlauncherClientLog.Program.Builder getProgram() {
        if (this.program == null) {
            this.program = TvlauncherClientLog.Program.newBuilder();
        }
        return this.program;
    }

    public TvlauncherClientLog.Input.Builder getInput() {
        if (this.input == null) {
            this.input = TvlauncherClientLog.Input.newBuilder();
        }
        return this.input;
    }

    public TvlauncherClientLog.InputCollection.Builder getInputCollection() {
        if (this.inputCollection == null) {
            this.inputCollection = TvlauncherClientLog.InputCollection.newBuilder();
        }
        return this.inputCollection;
    }

    public TvlauncherClientLog.WatchNextChannel.Builder getWatchNextChannel() {
        if (this.watchNextChannel == null) {
            this.watchNextChannel = TvlauncherClientLog.WatchNextChannel.newBuilder();
        }
        return this.watchNextChannel;
    }

    public TvlauncherClientLog.LaunchItemCollection.Builder getLaunchItemCollection() {
        if (this.launchItemCollection == null) {
            this.launchItemCollection = TvlauncherClientLog.LaunchItemCollection.newBuilder();
        }
        return this.launchItemCollection;
    }

    private void ensureVisualElements() {
        if (this.ancestryVisualElement == null) {
            this.ancestryVisualElement = AncestryVisualElement.AncestryVisualElementProto.newBuilder();
        }
    }

    private void ensureVisualElementsEntry() {
        if (this.visualElementsEntry == null) {
            this.visualElementsEntry = TvlauncherClientLog.VisualElementEntry.newBuilder();
        }
    }

    private void ensureMetadata() {
        if (this.metadata == null) {
            this.metadata = TvlauncherClientLog.VisualElementMetadata.newBuilder();
        }
    }

    private void ensureLaunchItem() {
        if (this.launchItem == null) {
            this.launchItem = TvlauncherClientLog.LaunchItem.newBuilder();
        }
    }

    private void build() {
        if (this.logEntry == null) {
            this.logEntry = TvlauncherClientLog.TvLauncherClientExtension.newBuilder();
            if (this.channel != null) {
                ensureMetadata();
                this.metadata.setChannel(this.channel);
            }
            if (this.channelCollection != null) {
                ensureMetadata();
                this.metadata.setChannelCollection(this.channelCollection);
            }
            if (this.notification != null) {
                ensureMetadata();
                this.metadata.setNotification(this.notification);
            }
            if (this.program != null) {
                ensureMetadata();
                this.metadata.setProgram(this.program);
            }
            if (this.notificationCollection != null) {
                ensureMetadata();
                this.metadata.setNotificationCollection(this.notificationCollection);
            }
            if (this.application != null) {
                ensureLaunchItem();
                this.launchItem.setApp(this.application);
            }
            if (this.appLink != null) {
                ensureLaunchItem();
                this.launchItem.setAppLink(this.appLink);
            }
            if (this.launchItem != null) {
                ensureMetadata();
                this.metadata.setLaunchItem(this.launchItem);
            }
            if (this.launchItemCollection != null) {
                ensureMetadata();
                this.metadata.setLaunchItemCollection(this.launchItemCollection);
            }
            if (this.input != null) {
                ensureMetadata();
                this.metadata.setInput(this.input);
            }
            if (this.inputCollection != null) {
                ensureMetadata();
                this.metadata.setInputCollection(this.inputCollection);
            }
            if (this.watchNextChannel != null) {
                ensureMetadata();
                this.metadata.setWatchNextChannel(this.watchNextChannel);
            }
            if (this.metadata != null && !this.bypassUsageReportingOptOut) {
                ensureVisualElementsEntry();
                this.visualElementsEntry.setVeMetadata(this.metadata);
            }
            if (this.ancestryVisualElement != null) {
                ensureVisualElementsEntry();
                this.visualElementsEntry.setAncestryVisualElement(this.ancestryVisualElement);
            }
            TvlauncherClientLog.VisualElementEntry.Builder builder = this.visualElementsEntry;
            if (builder != null) {
                this.logEntry.setVisualElementEntry(builder);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void mergeFrom(LogEvent event) {
        build();
        this.logEntry.mergeFrom((GeneratedMessageLite) event.getClientLogEntry());
    }

    public static TvlauncherClientLog.Notification.Importance notificationImportance(int importance) {
        if (importance == 1) {
            return TvlauncherClientLog.Notification.Importance.MIN;
        }
        if (importance == 2) {
            return TvlauncherClientLog.Notification.Importance.LOW;
        }
        if (importance == 3) {
            return TvlauncherClientLog.Notification.Importance.DEFAULT;
        }
        if (importance == 4) {
            return TvlauncherClientLog.Notification.Importance.HIGH;
        }
        if (importance != 5) {
            return TvlauncherClientLog.Notification.Importance.DEFAULT;
        }
        return TvlauncherClientLog.Notification.Importance.MAX;
    }

    public static TvlauncherClientLog.Program.Type programType(int programType) {
        switch (programType) {
            case 0:
                return TvlauncherClientLog.Program.Type.MOVIE;
            case 1:
                return TvlauncherClientLog.Program.Type.TV_SERIES;
            case 2:
                return TvlauncherClientLog.Program.Type.TV_SEASON;
            case 3:
                return TvlauncherClientLog.Program.Type.TV_EPISODE;
            case 4:
                return TvlauncherClientLog.Program.Type.CLIP;
            case 5:
                return TvlauncherClientLog.Program.Type.EVENT;
            case 6:
                return TvlauncherClientLog.Program.Type.CHANNEL;
            case 7:
                return TvlauncherClientLog.Program.Type.TRACK;
            case 8:
                return TvlauncherClientLog.Program.Type.ALBUM;
            case 9:
                return TvlauncherClientLog.Program.Type.ARTIST;
            case 10:
                return TvlauncherClientLog.Program.Type.PLAYLIST;
            case 11:
                return TvlauncherClientLog.Program.Type.STATION;
            case 12:
                return TvlauncherClientLog.Program.Type.GAME;
            default:
                return null;
        }
    }

    public static TvlauncherClientLog.Program.InteractionCount.Type interactionType(int type) {
        switch (type) {
            case 0:
                return TvlauncherClientLog.Program.InteractionCount.Type.VIEWS;
            case 1:
                return TvlauncherClientLog.Program.InteractionCount.Type.LISTENS;
            case 2:
                return TvlauncherClientLog.Program.InteractionCount.Type.FOLLOWERS;
            case 3:
                return TvlauncherClientLog.Program.InteractionCount.Type.FANS;
            case 4:
                return TvlauncherClientLog.Program.InteractionCount.Type.LIKES;
            case 5:
                return TvlauncherClientLog.Program.InteractionCount.Type.THUMBS;
            case 6:
                return TvlauncherClientLog.Program.InteractionCount.Type.VIEWERS;
            default:
                return null;
        }
    }

    public static TvlauncherClientLog.Input.Type inputType(int inputType) {
        if (inputType == -10) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE;
        }
        if (inputType == -9) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE;
        }
        if (inputType == -8) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE;
        }
        if (inputType == -6) {
            return TvlauncherClientLog.Input.Type.MHL_MOBILE;
        }
        if (inputType == -5) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE_PLAYBACK;
        }
        if (inputType == -4) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE_RECORDER;
        }
        if (inputType == -3) {
            return TvlauncherClientLog.Input.Type.BUNDLED_TUNER;
        }
        if (inputType == -2) {
            return TvlauncherClientLog.Input.Type.CEC_DEVICE;
        }
        if (inputType == 0) {
            return TvlauncherClientLog.Input.Type.TUNER;
        }
        switch (inputType) {
            case 1001:
                return TvlauncherClientLog.Input.Type.COMPOSITE;
            case 1002:
                return TvlauncherClientLog.Input.Type.SVIDEO;
            case 1003:
                return TvlauncherClientLog.Input.Type.SCART;
            case 1004:
                return TvlauncherClientLog.Input.Type.COMPONENT;
            case 1005:
                return TvlauncherClientLog.Input.Type.VGA;
            case 1006:
                return TvlauncherClientLog.Input.Type.DVI;
            case 1007:
                return TvlauncherClientLog.Input.Type.HDMI;
            case 1008:
                return TvlauncherClientLog.Input.Type.DISPLAY_PORT;
            default:
                return null;
        }
    }
}
