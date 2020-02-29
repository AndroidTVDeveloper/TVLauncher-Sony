package com.google.android.libraries.phenotype.client;

interface FlagLoader {

    public interface BinderAwareFunction<V> {
        V execute();
    }

    Object getFlag(String str);
}
