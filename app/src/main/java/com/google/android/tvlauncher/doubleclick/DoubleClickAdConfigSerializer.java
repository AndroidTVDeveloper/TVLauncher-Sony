package com.google.android.tvlauncher.doubleclick;

import android.util.Log;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.util.Arrays;

public class DoubleClickAdConfigSerializer implements AdConfigSerializer {
    private static final String TAG = "DoubleClickAdConfigSerializer";

    public byte[] serialize(AdConfig.AdAsset adAsset) {
        return MessageNano.toByteArray(adAsset);
    }

    public AdConfig.AdAsset deserialize(byte[] serialized) {
        try {
            return (AdConfig.AdAsset) MessageNano.mergeFrom(new AdConfig.AdAsset(), serialized);
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
