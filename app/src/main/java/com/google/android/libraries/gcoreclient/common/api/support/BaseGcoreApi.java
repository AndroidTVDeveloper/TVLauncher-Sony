package com.google.android.libraries.gcoreclient.common.api.support;

import com.google.android.gms.common.api.Api;
import com.google.android.libraries.gcoreclient.common.api.GcoreApi;
import com.google.android.libraries.gcoreclient.common.api.support.BaseGcoreApi.BaseGcoreApiOptions;

@Deprecated
public interface BaseGcoreApi<O extends BaseGcoreApiOptions> extends GcoreApi<O> {

    interface BaseGcoreApiOptions extends GcoreApi.GcoreApiOptions {

        interface BaseGcoreHasOptions extends BaseGcoreApiOptions, GcoreApi.GcoreApiOptions.GcoreHasOptions {
            Api.ApiOptions.HasOptions getApiOptions();
        }

        final class BaseGcoreNoOptions implements BaseGcoreApiOptions, GcoreApi.GcoreApiOptions.GcoreNoOptions {
        }

        interface BaseGcoreNotRequiredOptions extends BaseGcoreApiOptions, GcoreApi.GcoreApiOptions.GcoreNotRequiredOptions {
            Api.ApiOptions.NotRequiredOptions getApiOptions();
        }

        interface BaseGcoreOptional extends BaseGcoreApiOptions, GcoreApi.GcoreApiOptions.GcoreOptional {
            Api.ApiOptions.Optional getApiOptions();
        }
    }

    Api getApi();
}
