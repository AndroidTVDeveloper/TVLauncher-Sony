package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;
import java.io.File;

public final class HardwareConfigState {
    public static final int DEFAULT_MAXIMUM_FDS_FOR_HARDWARE_CONFIGS = 700;
    private static final File FD_SIZE_LIST = new File("/proc/self/fd");
    private static final int MINIMUM_DECODES_BETWEEN_FD_CHECKS = 50;
    static final int MIN_HARDWARE_DIMENSION = 128;
    private static volatile int fdSizeLimit = 700;
    private static volatile HardwareConfigState instance;
    private int decodesSinceLastFdCheck;
    private boolean isFdSizeBelowHardwareLimit = true;
    private final boolean isHardwareConfigAllowedByDeviceModel = isHardwareConfigAllowedByDeviceModel();

    public static HardwareConfigState getInstance() {
        if (instance == null) {
            synchronized (HardwareConfigState.class) {
                if (instance == null) {
                    instance = new HardwareConfigState();
                }
            }
        }
        return instance;
    }

    public static void setFdSizeLimit(int fdSizeLimit2) {
        if (Build.VERSION.SDK_INT >= 28) {
            fdSizeLimit = fdSizeLimit2;
        }
    }

    HardwareConfigState() {
    }

    public boolean isHardwareConfigAllowed(int targetWidth, int targetHeight, boolean isHardwareConfigAllowed, boolean isExifOrientationRequired) {
        return isHardwareConfigAllowed && this.isHardwareConfigAllowedByDeviceModel && Build.VERSION.SDK_INT >= 26 && !isExifOrientationRequired && targetWidth >= 128 && targetHeight >= 128 && isFdSizeBelowHardwareLimit();
    }

    /* access modifiers changed from: package-private */
    public boolean setHardwareConfigIfAllowed(int targetWidth, int targetHeight, BitmapFactory.Options optionsWithScaling, boolean isHardwareConfigAllowed, boolean isExifOrientationRequired) {
        boolean result = isHardwareConfigAllowed(targetWidth, targetHeight, isHardwareConfigAllowed, isExifOrientationRequired);
        if (result) {
            optionsWithScaling.inPreferredConfig = Bitmap.Config.HARDWARE;
            optionsWithScaling.inMutable = false;
        }
        return result;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static boolean isHardwareConfigAllowedByDeviceModel() {
        char c;
        if (Build.MODEL == null || Build.MODEL.length() < 7) {
            return true;
        }
        String substring = Build.MODEL.substring(0, 7);
        switch (substring.hashCode()) {
            case -1398613787:
                if (substring.equals("SM-A520")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1398431166:
                if (substring.equals("SM-G930")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1398431161:
                if (substring.equals("SM-G935")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1398431073:
                if (substring.equals("SM-G960")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1398431068:
                if (substring.equals("SM-G965")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1398343746:
                if (substring.equals("SM-J720")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1398222624:
                if (substring.equals("SM-N935")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return Build.VERSION.SDK_INT != 26;
            default:
                return true;
        }
    }

    private synchronized boolean isFdSizeBelowHardwareLimit() {
        int i = this.decodesSinceLastFdCheck + 1;
        this.decodesSinceLastFdCheck = i;
        if (i >= 50) {
            boolean z = false;
            this.decodesSinceLastFdCheck = 0;
            int currentFds = FD_SIZE_LIST.list().length;
            if (currentFds < fdSizeLimit) {
                z = true;
            }
            this.isFdSizeBelowHardwareLimit = z;
            if (!this.isFdSizeBelowHardwareLimit && Log.isLoggable("Downsampler", 5)) {
                int i2 = fdSizeLimit;
                StringBuilder sb = new StringBuilder((int) ClientAnalytics.LogRequest.LogSource.CLASSROOM_VALUE);
                sb.append("Excluding HARDWARE bitmap config because we're over the file descriptor limit, file descriptors ");
                sb.append(currentFds);
                sb.append(", limit ");
                sb.append(i2);
                Log.w("Downsampler", sb.toString());
            }
        }
        return this.isFdSizeBelowHardwareLimit;
    }
}
