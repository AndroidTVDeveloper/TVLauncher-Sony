package com.google.android.tvlauncher.doubleclick;

import android.util.Log;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.util.Arrays;

public class DirectAdConfigSerializer implements AdConfigSerializer {
    private static final String TAG = "DirectAdConfigSerializer";

    public byte[] serialize(AdConfig.AdAsset adAsset) {
        return new byte[0];
    }

    public AdConfig.AdAsset deserialize(byte[] serialized) {
        try {
            AdConfig.AdAsset adAsset = new AdConfig.AdAsset();
            adAsset.setDirectAdConfig((AdConfig.DirectAdConfig) MessageNano.mergeFrom(new AdConfig.DirectAdConfig(), serialized));
            return adAsset;
        } catch (InvalidProtocolBufferNanoException ex) {
            String arrays = Arrays.toString(serialized);
            StringBuilder sb = new StringBuilder(arrays.length() + 47);
            sb.append("Could not deserialize: ");
            sb.append(arrays);
            sb.append(" into AdConfig.AdAsset: ");
            Log.e(TAG, sb.toString(), ex);
            return null;
        }
    }
}
