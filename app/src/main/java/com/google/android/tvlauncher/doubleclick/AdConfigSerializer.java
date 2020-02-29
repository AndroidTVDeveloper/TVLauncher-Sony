package com.google.android.tvlauncher.doubleclick;

import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;

public interface AdConfigSerializer {
    AdConfig.AdAsset deserialize(byte[] bArr);

    byte[] serialize(AdConfig.AdAsset adAsset);
}
