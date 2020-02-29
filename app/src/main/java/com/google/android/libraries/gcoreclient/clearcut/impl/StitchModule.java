package com.google.android.libraries.gcoreclient.clearcut.impl;

import android.content.Context;
import com.google.android.libraries.gcoreclient.clearcut.GcoreClearcutApi;
import com.google.android.libraries.gcoreclient.clearcut.GcoreClearcutLoggerFactory;
import com.google.android.libraries.gcoreclient.clearcut.GcoreCountersBucketAliasFactory;
import com.google.android.libraries.gcoreclient.clearcut.GcoreCountersFactory;
import com.google.android.libraries.gcoreclient.clearcut.impl.GcoreClearcutApiImpl;
import com.google.android.libraries.gcoreclient.common.api.support.GcoreWrapper;
import com.google.android.libraries.stitch.binder.Binder;

@Deprecated
class StitchModule {
    StitchModule() {
    }

    /* access modifiers changed from: package-private */
    public GcoreClearcutLoggerFactory getGcoreClearcutLoggerApiFactory() {
        return new GcoreClearcutLoggerFactoryImpl();
    }

    public final class Adapter {
        public static final String GCORECLEARCUTAPI_BUILDER = GcoreClearcutApi.Builder.class.getName();
        public static final String GCORECLEARCUTLOGGERFACTORY = GcoreClearcutLoggerFactory.class.getName();
        public static final String GCORECOUNTERSBUCKETALIASFACTORY = GcoreCountersBucketAliasFactory.class.getName();
        public static final String GCORECOUNTERSFACTORY = GcoreCountersFactory.class.getName();
        private static StitchModule module;

        public static void bindGcoreClearcutApi_Builder(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreClearcutApi.Builder.class, module.getGcoreClearcutApiBuilder());
        }

        public static void bindGcoreClearcutLoggerFactory(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreClearcutLoggerFactory.class, module.getGcoreClearcutLoggerApiFactory());
        }

        public static void bindGcoreCountersBucketAliasFactory(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreCountersBucketAliasFactory.class, module.getGcoreCountersBucketAliasFactory());
        }

        public static void bindGcoreCountersFactory(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreCountersFactory.class, module.getGcoreCountersFactory());
        }
    }

    /* access modifiers changed from: package-private */
    public GcoreClearcutApi.Builder getGcoreClearcutApiBuilder() {
        return new GcoreClearcutApiImpl.Builder();
    }

    /* access modifiers changed from: package-private */
    public GcoreCountersFactory getGcoreCountersFactory() {
        return new GcoreCountersFactoryImpl(new GcoreWrapper());
    }

    /* access modifiers changed from: package-private */
    public GcoreCountersBucketAliasFactory getGcoreCountersBucketAliasFactory() {
        return new GcoreCountersBucketAliasFactoryImpl();
    }
}
