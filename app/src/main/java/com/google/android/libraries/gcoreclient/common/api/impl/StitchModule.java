package com.google.android.libraries.gcoreclient.common.api.impl;

import android.content.Context;
import com.google.android.libraries.gcoreclient.common.api.GcoreGoogleApiClient;
import com.google.android.libraries.gcoreclient.common.api.GcoreScope;
import com.google.android.libraries.gcoreclient.common.api.impl.GcoreGoogleApiClientImpl;
import com.google.android.libraries.gcoreclient.common.api.support.GcoreScopeImpl;
import com.google.android.libraries.stitch.binder.Binder;

@Deprecated
public class StitchModule {

    public final class Adapter {
        public static final String GCOREGOOGLEAPICLIENT_BUILDER = GcoreGoogleApiClient.Builder.class.getName();
        public static final String GCOREGOOGLEAPICLIENT_BUILDERFACTORY = GcoreGoogleApiClient.BuilderFactory.class.getName();
        public static final String GCORESCOPE_BUILDER = GcoreScope.Builder.class.getName();
        private static StitchModule module;

        public static void bindGcoreGoogleApiClient_Builder(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreGoogleApiClient.Builder.class, module.gcoreGoogleApiClientBuilder(context));
        }

        public static void bindGcoreGoogleApiClient_BuilderFactory(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreGoogleApiClient.BuilderFactory.class, module.gcoreGoogleApiClientBuilderFactory());
        }

        public static void bindGcoreScope_Builder(Context context, Binder binder) {
            synchronized (Adapter.class) {
                if (module == null) {
                    module = new StitchModule();
                }
            }
            binder.bind(GcoreScope.Builder.class, module.gcoreScopeBuilder());
        }
    }

    public GcoreGoogleApiClient.Builder gcoreGoogleApiClientBuilder(Context context) {
        return new GcoreGoogleApiClientImpl.Builder(context);
    }

    public GcoreGoogleApiClient.BuilderFactory gcoreGoogleApiClientBuilderFactory() {
        return new GcoreGoogleApiClientImpl.BuilderFactory();
    }

    public GcoreScope.Builder gcoreScopeBuilder() {
        return new GcoreScopeImpl.Builder();
    }
}
