package com.google.android.libraries.gcoreclient.phenotype;

@Deprecated
public interface GcorePhenotypeFlagCommitter {

    interface ConfigurationsHandler {
        void handleConfigurations(GcoreConfigurations gcoreConfigurations);
    }

    boolean commitForUser(String str);

    void setTimeoutMillis(long j);
}
