package com.google.android.libraries.performance.primes;

public final class PrimesGlobalConfigurations {
    private final ComponentNameSupplier componentNameSupplier;

    public interface ComponentNameSupplier extends Supplier<NoPiiString> {
        NoPiiString get();
    }

    private PrimesGlobalConfigurations(ComponentNameSupplier componentNameSupplier2) {
        this.componentNameSupplier = componentNameSupplier2;
    }

    /* access modifiers changed from: package-private */
    public ComponentNameSupplier getComponentNameSupplier() {
        return this.componentNameSupplier;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private ComponentNameSupplier componentNameSupplier;

        private Builder() {
        }

        public Builder setComponentName(final NoPiiString componentName) {
            this.componentNameSupplier = new ComponentNameSupplier(this) {
                public NoPiiString get() {
                    return componentName;
                }
            };
            return this;
        }

        public Builder setComponentNameSupplier(ComponentNameSupplier componentNameSupplier2) {
            this.componentNameSupplier = componentNameSupplier2;
            return this;
        }

        public PrimesGlobalConfigurations build() {
            return new PrimesGlobalConfigurations(this.componentNameSupplier);
        }
    }
}
