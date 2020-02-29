package com.google.android.tvlauncher.appsview.data;

import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import java.util.Locale;
import java.util.Objects;

public class PackageImageDataSource {
    private final ImageType imageType;
    private final Locale locale;
    private final String packageName;
    private final ResolveInfo resolveInfo;

    public enum ImageType {
        ICON,
        BANNER
    }

    public PackageImageDataSource(String packageName2, ResolveInfo resolveInfo2, ImageType imageType2, Locale locale2) {
        this.packageName = packageName2;
        this.resolveInfo = resolveInfo2;
        this.imageType = imageType2;
        this.locale = locale2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public ResolveInfo getResolveInfo() {
        return this.resolveInfo;
    }

    public ImageType getImageType() {
        return this.imageType;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PackageImageDataSource)) {
            return false;
        }
        PackageImageDataSource source = (PackageImageDataSource) obj;
        if (!TextUtils.equals(this.packageName, source.getPackageName()) || !this.imageType.equals(source.getImageType()) || !this.locale.equals(source.getLocale())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.packageName, this.imageType, this.locale);
    }

    public String toString() {
        String str = this.packageName;
        String valueOf = String.valueOf(this.imageType);
        String valueOf2 = String.valueOf(getLocale());
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 25 + String.valueOf(valueOf).length() + String.valueOf(valueOf2).length());
        sb.append(str);
        sb.append(", Image Type: ");
        sb.append(valueOf);
        sb.append(", Locale : ");
        sb.append(valueOf2);
        return sb.toString();
    }
}
