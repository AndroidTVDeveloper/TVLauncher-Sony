package com.bumptech.glide.load.engine.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import com.bumptech.glide.util.Preconditions;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;

public final class MemorySizeCalculator {
    static final int BYTES_PER_ARGB_8888_PIXEL = 4;
    private static final int LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR = 2;
    private static final String TAG = "MemorySizeCalculator";
    private final int arrayPoolSize;
    private final int bitmapPoolSize;
    private final Context context;
    private final int memoryCacheSize;

    interface ScreenDimensions {
        int getHeightPixels();

        int getWidthPixels();
    }

    MemorySizeCalculator(Builder builder) {
        int i;
        Builder builder2 = builder;
        this.context = builder2.context;
        if (isLowMemoryDevice(builder2.activityManager)) {
            i = builder2.arrayPoolSizeBytes / 2;
        } else {
            i = builder2.arrayPoolSizeBytes;
        }
        this.arrayPoolSize = i;
        int maxSize = getMaxSize(builder2.activityManager, builder2.maxSizeMultiplier, builder2.lowMemoryMaxSizeMultiplier);
        int screenSize = builder2.screenDimensions.getWidthPixels() * builder2.screenDimensions.getHeightPixels() * 4;
        int targetBitmapPoolSize = Math.round(((float) screenSize) * builder2.bitmapPoolScreens);
        int targetMemoryCacheSize = Math.round(((float) screenSize) * builder2.memoryCacheScreens);
        int availableSize = maxSize - this.arrayPoolSize;
        if (targetMemoryCacheSize + targetBitmapPoolSize <= availableSize) {
            this.memoryCacheSize = targetMemoryCacheSize;
            this.bitmapPoolSize = targetBitmapPoolSize;
        } else {
            float part = ((float) availableSize) / (builder2.bitmapPoolScreens + builder2.memoryCacheScreens);
            this.memoryCacheSize = Math.round(builder2.memoryCacheScreens * part);
            this.bitmapPoolSize = Math.round(builder2.bitmapPoolScreens * part);
        }
        if (Log.isLoggable(TAG, 3)) {
            String mb = toMb(this.memoryCacheSize);
            String mb2 = toMb(this.bitmapPoolSize);
            String mb3 = toMb(this.arrayPoolSize);
            boolean z = targetMemoryCacheSize + targetBitmapPoolSize > maxSize;
            String mb4 = toMb(maxSize);
            int memoryClass = builder2.activityManager.getMemoryClass();
            boolean isLowMemoryDevice = isLowMemoryDevice(builder2.activityManager);
            StringBuilder sb = new StringBuilder(String.valueOf(mb).length() + ClientAnalytics.LogRequest.LogSource.CLEARCUT_DEMO_VALUE + String.valueOf(mb2).length() + String.valueOf(mb3).length() + String.valueOf(mb4).length());
            sb.append("Calculation complete, Calculated memory cache size: ");
            sb.append(mb);
            sb.append(", pool size: ");
            sb.append(mb2);
            sb.append(", byte array size: ");
            sb.append(mb3);
            sb.append(", memory class limited? ");
            sb.append(z);
            sb.append(", max size: ");
            sb.append(mb4);
            sb.append(", memoryClass: ");
            sb.append(memoryClass);
            sb.append(", isLowMemoryDevice: ");
            sb.append(isLowMemoryDevice);
            Log.d(TAG, sb.toString());
        }
    }

    public int getMemoryCacheSize() {
        return this.memoryCacheSize;
    }

    public int getBitmapPoolSize() {
        return this.bitmapPoolSize;
    }

    public int getArrayPoolSizeInBytes() {
        return this.arrayPoolSize;
    }

    private static int getMaxSize(ActivityManager activityManager, float maxSizeMultiplier, float lowMemoryMaxSizeMultiplier) {
        return Math.round(((float) (activityManager.getMemoryClass() * 1024 * 1024)) * (isLowMemoryDevice(activityManager) ? lowMemoryMaxSizeMultiplier : maxSizeMultiplier));
    }

    private String toMb(int bytes) {
        return Formatter.formatFileSize(this.context, (long) bytes);
    }

    static boolean isLowMemoryDevice(ActivityManager activityManager) {
        if (Build.VERSION.SDK_INT >= 19) {
            return activityManager.isLowRamDevice();
        }
        return true;
    }

    public static final class Builder {
        static final int ARRAY_POOL_SIZE_BYTES = 4194304;
        static final int BITMAP_POOL_TARGET_SCREENS = (Build.VERSION.SDK_INT < 26 ? 4 : 1);
        static final float LOW_MEMORY_MAX_SIZE_MULTIPLIER = 0.33f;
        static final float MAX_SIZE_MULTIPLIER = 0.4f;
        static final int MEMORY_CACHE_TARGET_SCREENS = 2;
        ActivityManager activityManager;
        int arrayPoolSizeBytes = 4194304;
        float bitmapPoolScreens = ((float) BITMAP_POOL_TARGET_SCREENS);
        final Context context;
        float lowMemoryMaxSizeMultiplier = LOW_MEMORY_MAX_SIZE_MULTIPLIER;
        float maxSizeMultiplier = MAX_SIZE_MULTIPLIER;
        float memoryCacheScreens = 2.0f;
        ScreenDimensions screenDimensions;

        public Builder(Context context2) {
            this.context = context2;
            this.activityManager = (ActivityManager) context2.getSystemService("activity");
            this.screenDimensions = new DisplayMetricsScreenDimensions(context2.getResources().getDisplayMetrics());
            if (Build.VERSION.SDK_INT >= 26 && MemorySizeCalculator.isLowMemoryDevice(this.activityManager)) {
                this.bitmapPoolScreens = 0.0f;
            }
        }

        public Builder setMemoryCacheScreens(float memoryCacheScreens2) {
            Preconditions.checkArgument(memoryCacheScreens2 >= 0.0f, "Memory cache screens must be greater than or equal to 0");
            this.memoryCacheScreens = memoryCacheScreens2;
            return this;
        }

        public Builder setBitmapPoolScreens(float bitmapPoolScreens2) {
            Preconditions.checkArgument(bitmapPoolScreens2 >= 0.0f, "Bitmap pool screens must be greater than or equal to 0");
            this.bitmapPoolScreens = bitmapPoolScreens2;
            return this;
        }

        public Builder setMaxSizeMultiplier(float maxSizeMultiplier2) {
            Preconditions.checkArgument(maxSizeMultiplier2 >= 0.0f && maxSizeMultiplier2 <= 1.0f, "Size multiplier must be between 0 and 1");
            this.maxSizeMultiplier = maxSizeMultiplier2;
            return this;
        }

        public Builder setLowMemoryMaxSizeMultiplier(float lowMemoryMaxSizeMultiplier2) {
            Preconditions.checkArgument(lowMemoryMaxSizeMultiplier2 >= 0.0f && lowMemoryMaxSizeMultiplier2 <= 1.0f, "Low memory max size multiplier must be between 0 and 1");
            this.lowMemoryMaxSizeMultiplier = lowMemoryMaxSizeMultiplier2;
            return this;
        }

        public Builder setArrayPoolSize(int arrayPoolSizeBytes2) {
            this.arrayPoolSizeBytes = arrayPoolSizeBytes2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setActivityManager(ActivityManager activityManager2) {
            this.activityManager = activityManager2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setScreenDimensions(ScreenDimensions screenDimensions2) {
            this.screenDimensions = screenDimensions2;
            return this;
        }

        public MemorySizeCalculator build() {
            return new MemorySizeCalculator(this);
        }
    }

    private static final class DisplayMetricsScreenDimensions implements ScreenDimensions {
        private final DisplayMetrics displayMetrics;

        DisplayMetricsScreenDimensions(DisplayMetrics displayMetrics2) {
            this.displayMetrics = displayMetrics2;
        }

        public int getWidthPixels() {
            return this.displayMetrics.widthPixels;
        }

        public int getHeightPixels() {
            return this.displayMetrics.heightPixels;
        }
    }
}
