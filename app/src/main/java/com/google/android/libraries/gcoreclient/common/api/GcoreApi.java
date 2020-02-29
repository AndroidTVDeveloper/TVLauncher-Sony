package com.google.android.libraries.gcoreclient.common.api;

import android.accounts.Account;
import com.google.android.libraries.gcoreclient.common.api.GcoreApi.GcoreApiOptions;

@Deprecated
public interface GcoreApi<O extends GcoreApiOptions> {

    interface GcoreApiOptions {

        interface GcoreHasAccountOptions extends GcoreHasOptions, GcoreNotRequiredOptions {
            Account getAccount();
        }

        interface GcoreHasOptions extends GcoreApiOptions {
        }

        interface GcoreNoOptions extends GcoreNotRequiredOptions {
        }

        interface GcoreNotRequiredOptions extends GcoreApiOptions {
        }

        interface GcoreOptional extends GcoreHasOptions, GcoreNotRequiredOptions {
        }
    }
}
