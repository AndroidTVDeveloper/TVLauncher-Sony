package com.google.android.tvlauncher.util;

import android.text.TextUtils;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChannelConfigurationInfo {
    private static final String CAN_HIDE = "canHideChannel";
    private static final String CAN_MOVE = "canMoveChannel";
    private static final String CHANNEL_POSITION = "chanPos";
    private static final String IS_GOOGLE_CONFIG = "isGoogleConfig";
    private static final String IS_SPONSORED = "isSponsored";
    private static final String PACKAGE_NAME = "pkgName";
    private static final String SYSTEM_CHANNEL_KEY = "sysChannelKey";
    private static final String TAG = "ChannelInfo";
    private boolean canHide;
    private boolean canMove;
    private int channelPosition;
    private boolean isGoogleConfig;
    private boolean isSponsored;
    private String packageName;
    private String systemChannelKey;

    public ChannelConfigurationInfo(String packageName2, String key) {
        this.canMove = true;
        this.canHide = true;
        this.packageName = packageName2;
        if (TextUtils.isEmpty(key)) {
            this.systemChannelKey = null;
        } else {
            this.systemChannelKey = key;
        }
    }

    private ChannelConfigurationInfo(Builder builder) {
        this.canMove = true;
        this.canHide = true;
        this.packageName = builder.packageName;
        if (TextUtils.isEmpty(builder.systemChannelKey)) {
            this.systemChannelKey = null;
        } else {
            this.systemChannelKey = builder.systemChannelKey;
        }
        this.channelPosition = builder.channelPosition;
        this.isSponsored = builder.isSponsored;
        this.isGoogleConfig = builder.isGoogleConfig;
        this.canMove = builder.canMove;
        this.canHide = builder.canHide;
    }

    public static String getUniqueKey(String packageName2, String systemChannelKey2) {
        if (TextUtils.isEmpty(systemChannelKey2)) {
            return packageName2;
        }
        StringBuilder sb = new StringBuilder(String.valueOf(packageName2).length() + 1 + String.valueOf(systemChannelKey2).length());
        sb.append(packageName2);
        sb.append(":");
        sb.append(systemChannelKey2);
        return sb.toString();
    }

    static JSONArray toJsonArray(List<ChannelConfigurationInfo> channelConfigurationInfoList) {
        JSONArray jsonArray = new JSONArray();
        for (ChannelConfigurationInfo channelConfigurationInfo : channelConfigurationInfoList) {
            jsonArray.put(toJsonObject(channelConfigurationInfo));
        }
        return jsonArray;
    }

    static List<Builder> fromJsonArrayString(String jsonArrayStr) {
        List<Builder> results = new ArrayList<>();
        if (!TextUtils.isEmpty(jsonArrayStr)) {
            try {
                JSONArray jsonArray = new JSONArray(jsonArrayStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    results.add(fromJsonObject(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                String valueOf = String.valueOf(jsonArrayStr);
                Log.e(TAG, valueOf.length() != 0 ? "JSONException in fromJson. Could not deserialize from jsonArrayStr: ".concat(valueOf) : "JSONException in fromJson. Could not deserialize from jsonArrayStr: ");
            }
        }
        return results;
    }

    private static JSONObject toJsonObject(ChannelConfigurationInfo channelConfigurationInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PACKAGE_NAME, channelConfigurationInfo.getPackageName());
            jsonObject.put(SYSTEM_CHANNEL_KEY, channelConfigurationInfo.getSystemChannelKey());
            jsonObject.put(CHANNEL_POSITION, channelConfigurationInfo.getChannelPosition());
            jsonObject.put(IS_SPONSORED, channelConfigurationInfo.isSponsored());
            jsonObject.put(IS_GOOGLE_CONFIG, channelConfigurationInfo.isGoogleConfig());
            jsonObject.put(CAN_MOVE, channelConfigurationInfo.canMove());
            jsonObject.put(CAN_HIDE, channelConfigurationInfo.canHide());
        } catch (JSONException e) {
            String valueOf = String.valueOf(channelConfigurationInfo);
            StringBuilder sb = new StringBuilder(valueOf.length() + 33);
            sb.append("Could not serialize ChannelInfo: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
        }
        return jsonObject;
    }

    private static Builder fromJsonObject(JSONObject jsonObject) {
        try {
            String packageName2 = jsonObject.getString(PACKAGE_NAME);
            String systemChannelKey2 = jsonObject.optString(SYSTEM_CHANNEL_KEY, null);
            int channelPos = jsonObject.getInt(CHANNEL_POSITION);
            boolean isSponsored2 = jsonObject.getBoolean(IS_SPONSORED);
            boolean isGoogleConfig2 = jsonObject.optBoolean(IS_GOOGLE_CONFIG, false);
            boolean canMoveChannel = jsonObject.getBoolean(CAN_MOVE);
            return new Builder().setPackageName(packageName2).setSystemChannelKey(systemChannelKey2).setChannelPosition(channelPos).setSponsored(isSponsored2).setIsGoogleConfig(isGoogleConfig2).setCanMove(canMoveChannel).setCanHide(jsonObject.getBoolean(CAN_HIDE));
        } catch (JSONException e) {
            String valueOf = String.valueOf(jsonObject);
            StringBuilder sb = new StringBuilder(valueOf.length() + 49);
            sb.append("JSONException. Could not deserialize jsonObject: ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString());
            return null;
        }
    }

    static String computeHashCode(List<ChannelConfigurationInfo> channelConfigurationList) {
        if (channelConfigurationList.size() == 0) {
            return null;
        }
        try {
            MessageDigest sh1Digest = MessageDigest.getInstance("SHA-1");
            JSONArray jsonArray = toJsonArray(channelConfigurationList);
            if (jsonArray.length() != 0) {
                return new String(sh1Digest.digest(jsonArray.toString().getBytes(StandardCharsets.UTF_8)));
            }
            throw new RuntimeException("Cannot compute checksum. JsonArray returned 0 values.");
        } catch (NoSuchAlgorithmException ex) {
            String valueOf = String.valueOf(channelConfigurationList);
            StringBuilder sb = new StringBuilder(valueOf.length() + 79);
            sb.append("Exception while computing MessageDigest in computeHashCode for channelConfigs: ");
            sb.append(valueOf);
            throw new RuntimeException(sb.toString(), ex);
        }
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getSystemChannelKey() {
        return this.systemChannelKey;
    }

    public int getChannelPosition() {
        return this.channelPosition;
    }

    public boolean isSponsored() {
        return this.isSponsored;
    }

    public boolean isGoogleConfig() {
        return this.isGoogleConfig;
    }

    public boolean canMove() {
        return this.canMove;
    }

    public boolean canHide() {
        return this.canHide;
    }

    public int hashCode() {
        return Objects.hash(this.packageName, this.systemChannelKey);
    }

    public String toString() {
        String str = this.packageName;
        String str2 = this.systemChannelKey;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 1 + String.valueOf(str2).length());
        sb.append(str);
        sb.append("-");
        sb.append(str2);
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ChannelConfigurationInfo)) {
            return false;
        }
        ChannelConfigurationInfo channelConfigurationInfo = (ChannelConfigurationInfo) obj;
        return TextUtils.equals(this.packageName, channelConfigurationInfo.getPackageName()) && TextUtils.equals(this.systemChannelKey, channelConfigurationInfo.getSystemChannelKey());
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public boolean canHide = true;
        /* access modifiers changed from: private */
        public boolean canMove = true;
        /* access modifiers changed from: private */
        public int channelPosition;
        /* access modifiers changed from: private */
        public boolean isGoogleConfig;
        /* access modifiers changed from: private */
        public boolean isSponsored;
        /* access modifiers changed from: private */
        public String packageName;
        /* access modifiers changed from: private */
        public String systemChannelKey;

        public Builder setPackageName(String packageName2) {
            this.packageName = packageName2;
            return this;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public Builder setSystemChannelKey(String systemChannelKey2) {
            this.systemChannelKey = systemChannelKey2;
            return this;
        }

        public String getSystemChannelKey() {
            return this.systemChannelKey;
        }

        public Builder setChannelPosition(int channelPosition2) {
            this.channelPosition = channelPosition2;
            return this;
        }

        public int getChannelPosition() {
            return this.channelPosition;
        }

        public Builder setSponsored(boolean sponsored) {
            this.isSponsored = sponsored;
            return this;
        }

        public boolean isSponsored() {
            return this.isSponsored;
        }

        public Builder setIsGoogleConfig(boolean isGoogleConfig2) {
            this.isGoogleConfig = isGoogleConfig2;
            return this;
        }

        public Builder setCanMove(boolean canMove2) {
            this.canMove = canMove2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public boolean canMove() {
            return this.canMove;
        }

        public Builder setCanHide(boolean canHide2) {
            this.canHide = canHide2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public boolean canHide() {
            return this.canHide;
        }

        public ChannelConfigurationInfo build() {
            return new ChannelConfigurationInfo(this);
        }
    }
}
