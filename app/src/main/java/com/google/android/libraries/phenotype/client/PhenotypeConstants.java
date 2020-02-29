package com.google.android.libraries.phenotype.client;

import android.content.Context;
import android.net.Uri;
import android.support.p001v4.util.ArrayMap;

public final class PhenotypeConstants {
    public static final String ACTION_UPDATE = "com.google.android.gms.phenotype.UPDATE";
    public static final String ALL_USERS = "ALL_USERS";
    public static final String CONFIGURATION_VERSION_FLAG_NAME = "__phenotype_configuration_version";
    public static final String CONTENT_PROVIDER_AUTHORITY = "com.google.android.gms.phenotype";
    public static final String EMPTY_ALTERNATE = "com.google.EMPTY";
    public static final String EXTRA_PACKAGE_NAME = "com.google.android.gms.phenotype.PACKAGE_NAME";
    public static final String EXTRA_UPDATE_REASON = "com.google.android.gms.phenotype.UPDATE_REASON";
    public static final String EXTRA_URGENT_UPDATE = "com.google.android.gms.phenotype.URGENT";
    public static final String LOGGED_OUT_USER = "";
    public static final String SERVER_TOKEN_FLAG_NAME = "__phenotype_server_token";
    public static final String SNAPSHOT_TOKEN_FLAG_NAME = "__phenotype_snapshot_token";
    private static final ArrayMap<String, Uri> uriByConfigPackageName = new ArrayMap<>();

    public static synchronized Uri getContentProviderUri(String configPackageName) {
        Uri uri;
        synchronized (PhenotypeConstants.class) {
            uri = uriByConfigPackageName.get(configPackageName);
            if (uri == null) {
                String valueOf = String.valueOf(Uri.encode(configPackageName));
                uri = Uri.parse(valueOf.length() != 0 ? "content://com.google.android.gms.phenotype/".concat(valueOf) : "content://com.google.android.gms.phenotype/");
                uriByConfigPackageName.put(configPackageName, uri);
            }
        }
        return uri;
    }

    public static String getSharedPreferencesName(String configPackageName) {
        String valueOf = String.valueOf(configPackageName);
        return valueOf.length() != 0 ? "phenotype__".concat(valueOf) : "phenotype__";
    }

    public static String getSubpackagedName(Context context, String configPackageName) {
        return getSubpackagedName(context, configPackageName, false);
    }

    public static String getSubpackagedName(Context context, String configPackageName, boolean multiCommit) {
        if (configPackageName.contains("#")) {
            String valueOf = configPackageName;
            throw new IllegalArgumentException(valueOf.length() != 0 ? "The passed in package cannot already have a subpackage: ".concat(valueOf) : "The passed in package cannot already have a subpackage: ");
        }
        String str = multiCommit ? "@" : "";
        String packageName = context.getPackageName();
        StringBuilder sb = new StringBuilder(configPackageName.length() + 1 + str.length() + String.valueOf(packageName).length());
        sb.append(configPackageName);
        sb.append("#");
        sb.append(str);
        sb.append(packageName);
        return sb.toString();
    }

    public static boolean isMultiCommitPackage(String configPackageName) {
        int index = configPackageName.indexOf("#");
        return index >= 0 && index + 1 < configPackageName.length() && configPackageName.charAt(index + 1) == '@';
    }

    private PhenotypeConstants() {
    }
}
