package com.google.android.libraries.phenotype.client;

interface FlagLoader {

    interface BinderAwareFunction<V> {
        V execute();
    }

    Object getFlag(String str);
}
