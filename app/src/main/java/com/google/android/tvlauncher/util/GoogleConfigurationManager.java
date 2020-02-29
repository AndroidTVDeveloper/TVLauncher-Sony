package com.google.android.tvlauncher.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.google.android.tvlauncher.util.ChannelConfigContract;
import com.google.android.tvlauncher.util.ChannelConfigurationInfo;
import com.google.android.tvrecommendations.shared.util.Constants;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleConfigurationManager {
    private static final String CONFIG_THREAD_NAME = "google config";
    private static final int NO_POSITION = -1;
    private static final String TAG = "GoogleConfigManager";
    private static final int TRUE = 1;
    private List<ChannelConfigurationInfo> channelConfigurations = new ArrayList();
    private ContentObserver configurationObserver;
    private Context context;
    /* access modifiers changed from: private */
    public boolean isConfigurationLoaded;
    /* access modifiers changed from: private */
    public final Object lock = new Object();
    private Set<String> sponsoredChannels = new HashSet();

    public GoogleConfigurationManager(Context context2) {
        this.context = context2;
        HandlerThread configThread = new HandlerThread(CONFIG_THREAD_NAME);
        configThread.start();
        this.configurationObserver = new ConfigurationObserver(new Handler(configThread.getLooper()));
        try {
            this.context.getContentResolver().registerContentObserver(ChannelConfigContract.Uris.CHANNEL_CONFIG, true, this.configurationObserver);
        } catch (SecurityException e) {
            String valueOf = String.valueOf(ChannelConfigContract.Uris.CHANNEL_CONFIG);
            StringBuilder sb = new StringBuilder(valueOf.length() + 41);
            sb.append("Cannot register ContentObserver for Uri: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
        }
    }

    GoogleConfigurationManager(Context context2, ConfigurationObserver configurationObserver2) {
        this.context = context2;
        this.configurationObserver = configurationObserver2;
        try {
            this.context.getContentResolver().registerContentObserver(ChannelConfigContract.Uris.CHANNEL_CONFIG, true, this.configurationObserver);
        } catch (SecurityException e) {
            String valueOf = String.valueOf(ChannelConfigContract.Uris.CHANNEL_CONFIG);
            StringBuilder sb = new StringBuilder(valueOf.length() + 41);
            sb.append("Cannot register ContentObserver for Uri: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
        }
    }

    public GoogleConfiguration getChannelConfigs() {
        GoogleConfiguration googleConfiguration;
        synchronized (this.lock) {
            if (!this.isConfigurationLoaded) {
                refreshConfigFromContentProviderLocked();
            }
            if (this.isConfigurationLoaded) {
                googleConfiguration = new GoogleConfiguration(this.channelConfigurations, this.sponsoredChannels);
            } else {
                googleConfiguration = null;
            }
        }
        return googleConfiguration;
    }

    private void refreshConfigFromContentProviderLocked() {
        this.channelConfigurations.clear();
        this.sponsoredChannels.clear();
        Cursor cursor = this.context.getContentResolver().query(ChannelConfigContract.Uris.CHANNEL_CONFIG, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    boolean canMove = cursor.getInt(cursor.getColumnIndex("can_move")) == 1;
                    boolean canHide = cursor.getInt(cursor.getColumnIndex("can_hide")) == 1;
                    if (canMove ^ canHide) {
                        StringBuilder sb = new StringBuilder(113);
                        sb.append("Combination of can_move=");
                        sb.append(canMove);
                        sb.append(" and ");
                        sb.append("can_hide");
                        sb.append("=");
                        sb.append(canHide);
                        sb.append(" is not supported. Only both \"true\" or both \"false\" are supported");
                        Log.e(TAG, sb.toString());
                        canMove = true;
                        canHide = true;
                    }
                    String systemChannelKey = cursor.getString(cursor.getColumnIndex("system_channel_key"));
                    int channelPos = cursor.getInt(cursor.getColumnIndex("position"));
                    boolean isSponsored = cursor.getInt(cursor.getColumnIndex("sponsored")) == 1;
                    if (isSponsored) {
                        this.sponsoredChannels.add(ChannelConfigurationInfo.getUniqueKey(Constants.TVRECOMMENDATIONS_PACKAGE_NAME, systemChannelKey));
                    }
                    if (channelPos != -1) {
                        this.channelConfigurations.add(new ChannelConfigurationInfo.Builder().setPackageName(Constants.TVRECOMMENDATIONS_PACKAGE_NAME).setSystemChannelKey(systemChannelKey).setChannelPosition(channelPos).setSponsored(isSponsored).setIsGoogleConfig(true).setCanMove(canMove).setCanHide(canHide).build());
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th2) {
                            ThrowableExtension.addSuppressed(th, th2);
                        }
                    }
                    throw th;
                }
            }
            this.isConfigurationLoaded = true;
        } else {
            this.isConfigurationLoaded = false;
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    class ConfigurationObserver extends ContentObserver {
        ConfigurationObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (GoogleConfigurationManager.this.lock) {
                boolean unused = GoogleConfigurationManager.this.isConfigurationLoaded = false;
            }
        }
    }
}
