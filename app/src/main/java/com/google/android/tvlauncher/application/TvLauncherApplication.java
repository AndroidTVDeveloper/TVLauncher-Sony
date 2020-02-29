package com.google.android.tvlauncher.application;

import com.google.android.gms.phenotype.PhenotypeFlag;
import com.google.android.libraries.performance.primes.PrimesStartupMeasure;
import com.google.android.tvlauncher.analytics.ClearcutAppEventLogger;
import com.google.android.tvlauncher.analytics.ClearcutEventLoggerEngine;
import com.google.android.tvlauncher.analytics.NoOpAppEventLogger;
import com.google.android.tvlauncher.doubleclick.AdsManager;
import com.google.android.tvlauncher.doubleclick.AdsUtil;
import com.google.android.tvlauncher.doubleclick.DirectAdConfigSerializer;
import com.google.android.tvlauncher.doubleclick.DoubleClickAdConfigSerializer;
import com.google.android.tvlauncher.doubleclick.DoubleClickAdServer;
import com.google.android.tvlauncher.doubleclick.OutstreamVideoAdFactory;
import com.google.android.tvlauncher.util.TestUtils;

public class TvLauncherApplication extends TvLauncherApplicationBase {
    private static final String TAG = "TvLauncherApplication";
    private AdsManager adsManager;
    private final Object adsManagerLock = new Object();
    private AdsUtil adsUtil;
    private final Object adsUtilLock = new Object();
    private DirectAdConfigSerializer directAdConfigSerializer;
    private final Object directAdConfigSerializerLock = new Object();
    private DoubleClickAdConfigSerializer doubleClickAdConfigSerializer;
    private final Object doubleClickAdConfigSerializerLock = new Object();
    private DoubleClickAdServer doubleClickAdServer;
    private final Object doubleClickAdServerLock = new Object();
    private OutstreamVideoAdFactory outstreamVideoAdFactory;
    private final Object outstreamVideoAdFactoryLock = new Object();

    static {
        PrimesStartupMeasure.get().onAppClassLoaded();
    }

    public void onCreate() {
        super.onCreate();
        PrimesStartupMeasure.get().onAppCreate(this);
        PhenotypeFlag.init(this);
        if (!TestUtils.isRunningInTest()) {
            if (!isRemoteYoutubePlayerProcess()) {
                ClearcutAppEventLogger.init(this, new ClearcutEventLoggerEngine(this));
            }
            SilentFeedbackConfiguration.init(this);
            initPrimes();
        } else if (TestUtils.isRunningInTestHarness()) {
            NoOpAppEventLogger.init();
            initPrimes();
        }
    }

    /* access modifiers changed from: package-private */
    public void initPrimes() {
        PrimesConfiguration.init(this, new PrimesSettings(this));
    }

    public DoubleClickAdServer getDoubleClickAdServer() {
        if (this.doubleClickAdServer == null) {
            synchronized (this.doubleClickAdServerLock) {
                if (this.doubleClickAdServer == null) {
                    this.doubleClickAdServer = new DoubleClickAdServer(this);
                }
            }
        }
        return this.doubleClickAdServer;
    }

    public AdsManager getAdsManager() {
        if (this.adsManager == null) {
            synchronized (this.adsManagerLock) {
                if (this.adsManager == null) {
                    this.adsManager = new AdsManager(this);
                }
            }
        }
        return this.adsManager;
    }

    public DoubleClickAdConfigSerializer getDoubleClickAdConfigSerializer() {
        if (this.doubleClickAdConfigSerializer == null) {
            synchronized (this.doubleClickAdConfigSerializerLock) {
                if (this.doubleClickAdConfigSerializer == null) {
                    this.doubleClickAdConfigSerializer = new DoubleClickAdConfigSerializer();
                }
            }
        }
        return this.doubleClickAdConfigSerializer;
    }

    public DirectAdConfigSerializer getDirectAdConfigSerializer() {
        if (this.directAdConfigSerializer == null) {
            synchronized (this.directAdConfigSerializerLock) {
                if (this.directAdConfigSerializer == null) {
                    this.directAdConfigSerializer = new DirectAdConfigSerializer();
                }
            }
        }
        return this.directAdConfigSerializer;
    }

    public OutstreamVideoAdFactory getOutstreamVideoAdFactory() {
        if (this.outstreamVideoAdFactory == null) {
            synchronized (this.outstreamVideoAdFactoryLock) {
                if (this.outstreamVideoAdFactory == null) {
                    this.outstreamVideoAdFactory = new OutstreamVideoAdFactory();
                }
            }
        }
        return this.outstreamVideoAdFactory;
    }

    public AdsUtil getAdsUtil() {
        if (this.adsUtil == null) {
            synchronized (this.adsUtilLock) {
                if (this.adsUtil == null) {
                    this.adsUtil = new AdsUtil();
                }
            }
        }
        return this.adsUtil;
    }
}
