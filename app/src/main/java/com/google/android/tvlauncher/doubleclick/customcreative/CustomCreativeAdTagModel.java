package com.google.android.tvlauncher.doubleclick.customcreative;

public class CustomCreativeAdTagModel {
    private String correlator;
    private String inventoryUnit;
    private boolean shouldUseDelayedImpression;
    private String size;
    private String tile;

    private CustomCreativeAdTagModel(Builder builder) {
        this.size = builder.size;
        this.inventoryUnit = builder.inventoryUnit;
        this.shouldUseDelayedImpression = builder.shouldUseDelayedImpression;
        this.correlator = builder.correlator;
        this.tile = builder.tile;
    }

    /* access modifiers changed from: package-private */
    public String getSize() {
        return this.size;
    }

    /* access modifiers changed from: package-private */
    public String getInventoryUnit() {
        return this.inventoryUnit;
    }

    /* access modifiers changed from: package-private */
    public String getCorrelator() {
        return this.correlator;
    }

    /* access modifiers changed from: package-private */
    public String getTile() {
        return this.tile;
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public String correlator;
        /* access modifiers changed from: private */
        public String inventoryUnit;
        /* access modifiers changed from: private */
        public boolean shouldUseDelayedImpression;
        /* access modifiers changed from: private */
        public String size;
        /* access modifiers changed from: private */
        public String tile;

        public Builder setSize(String size2) {
            this.size = size2;
            return this;
        }

        public Builder setInventoryUnit(String inventoryUnit2) {
            this.inventoryUnit = inventoryUnit2;
            return this;
        }

        public Builder setShouldUseDelayedImpression(boolean shouldUseDelayedImpression2) {
            this.shouldUseDelayedImpression = shouldUseDelayedImpression2;
            return this;
        }

        public Builder setCorrelator(String correlator2) {
            this.correlator = correlator2;
            return this;
        }

        public Builder setTile(String tile2) {
            this.tile = tile2;
            return this;
        }

        public CustomCreativeAdTagModel build() {
            return new CustomCreativeAdTagModel(this);
        }
    }
}
