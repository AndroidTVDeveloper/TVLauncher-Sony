package com.google.android.libraries.phenotype.client;

import com.google.common.base.Supplier;

final /* synthetic */ class PhenotypeFlag$$Lambda$0 implements Supplier {
    static final Supplier $instance = new PhenotypeFlag$$Lambda$0();

    private PhenotypeFlag$$Lambda$0() {
    }

    public Object get() {
        return new HermeticFileOverridesReader().readFromFileIfEligible(PhenotypeFlag.context);
    }
}
