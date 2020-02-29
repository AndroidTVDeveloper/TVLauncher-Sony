package com.google.android.tvlauncher.util;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OemAppBase {
    private final String appName;
    private final String bannerUri;
    private final String category;
    private final String dataUri;
    private final String description;
    private final String developer;

    /* renamed from: id */
    private String f164id = constructId();
    private final boolean isGame;
    private final boolean isVirtualApp;
    private final String packageName;
    private List<String> screenshotUris;

    OemAppBase(Builder<?> builder) {
        this.appName = builder.appName;
        this.packageName = builder.packageName;
        this.bannerUri = builder.bannerUri;
        this.dataUri = builder.dataUri;
        this.developer = builder.developer;
        this.category = builder.category;
        this.description = builder.description;
        this.isGame = builder.isGame;
        this.isVirtualApp = builder.isVirtualApp;
        this.screenshotUris = builder.screenshotUris;
    }

    public static abstract class Builder<T extends Builder<T>> {
        String appName;
        String bannerUri;
        String category;
        String dataUri;
        String description;
        String developer;
        boolean isGame;
        boolean isVirtualApp;
        String packageName;
        List<String> screenshotUris = new ArrayList();

        public abstract OemAppBase build();

        public T setAppName(String appName2) {
            this.appName = appName2;
            return this;
        }

        public T setPackageName(String packageName2) {
            this.packageName = packageName2;
            return this;
        }

        public T setBannerUri(String bannerUri2) {
            this.bannerUri = bannerUri2;
            return this;
        }

        public T setDataUri(String dataUri2) {
            this.dataUri = dataUri2;
            return this;
        }

        public T setDeveloper(String developer2) {
            this.developer = developer2;
            return this;
        }

        public T setCategory(String category2) {
            this.category = category2;
            return this;
        }

        public T setDescription(String description2) {
            this.description = description2;
            return this;
        }

        public T setGame(boolean game) {
            this.isGame = game;
            return this;
        }

        public T setVirtualApp(boolean virtualApp) {
            this.isVirtualApp = virtualApp;
            return this;
        }

        public T setScreenshotUris(List<String> screenshotUris2) {
            this.screenshotUris = screenshotUris2;
            return this;
        }
    }

    public String getAppName() {
        return this.appName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getBannerUri() {
        return this.bannerUri;
    }

    public String getDataUri() {
        return this.dataUri;
    }

    public String getDeveloper() {
        return this.developer;
    }

    public String getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.f164id;
    }

    public boolean isGame() {
        return this.isGame;
    }

    public boolean isVirtualApp() {
        return this.isVirtualApp;
    }

    public List<String> getScreenshotUris() {
        return Collections.unmodifiableList(this.screenshotUris);
    }

    public void addScreenshotUri(String uri) {
        this.screenshotUris.add(uri);
    }

    public void addScreenshotUris(List<String> uris) {
        this.screenshotUris.addAll(uris);
    }

    private String constructId() {
        if (this.f164id == null) {
            this.f164id = this.packageName;
            if (this.isVirtualApp) {
                this.f164id = String.valueOf(this.f164id).concat(":");
                if (this.dataUri != null) {
                    String valueOf = this.f164id;
                    String valueOf2 = this.dataUri;
                    this.f164id = valueOf2.length() != 0 ? valueOf.concat(valueOf2) : valueOf;
                }
            }
        }
        return this.f164id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof OemAppBase) {
            return TextUtils.equals(getId(), ((OemAppBase) obj).getId());
        }
        return false;
    }

    public int hashCode() {
        return getId().hashCode();
    }
}
