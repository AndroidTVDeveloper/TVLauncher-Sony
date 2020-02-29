package com.google.android.tvlauncher.appsview.data;

import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import java.util.Locale;
import java.util.Objects;

public final class LaunchItemImageDataSource extends PackageImageDataSource {
    private final LaunchItem launchItem;

    public LaunchItemImageDataSource(LaunchItem item, PackageImageDataSource.ImageType imageType, Locale locale) {
        super(item.getPackageName(), item.getResolveInfo(), imageType, locale);
        this.launchItem = item;
    }

    public LaunchItem getLaunchItem() {
        return this.launchItem;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LaunchItemImageDataSource)) {
            return false;
        }
        LaunchItemImageDataSource source = (LaunchItemImageDataSource) obj;
        return this.launchItem.equals(source.getLaunchItem()) && getImageType().equals(source.getImageType()) && this.launchItem.isInstalling() == source.getLaunchItem().isInstalling() && getLocale().equals(source.getLocale()) && this.launchItem.getLastUpdateTime() == source.getLaunchItem().getLastUpdateTime();
    }

    public int hashCode() {
        return Objects.hash(this.launchItem, getImageType(), Boolean.valueOf(this.launchItem.isInstalling()), getLocale(), Long.valueOf(this.launchItem.getLastUpdateTime()));
    }

    public String toString() {
        String launchItem2 = this.launchItem.toString();
        String valueOf = String.valueOf(getImageType());
        boolean isInstalling = this.launchItem.isInstalling();
        String valueOf2 = String.valueOf(getLocale());
        StringBuilder sb = new StringBuilder(launchItem2.length() + 48 + valueOf.length() + valueOf2.length());
        sb.append(launchItem2);
        sb.append(", Image Type: ");
        sb.append(valueOf);
        sb.append(", Is Installing : ");
        sb.append(isInstalling);
        sb.append(", Locale : ");
        sb.append(valueOf2);
        return sb.toString();
    }
}
