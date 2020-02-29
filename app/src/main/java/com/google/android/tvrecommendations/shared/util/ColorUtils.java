package com.google.android.tvrecommendations.shared.util;

import android.graphics.Color;
import android.graphics.ColorFilter;
import androidx.leanback.graphics.ColorFilterCache;

public class ColorUtils {
    public static int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        hsv[2] = hsv[2] * factor;
        return Color.HSVToColor(hsv);
    }

    public static ColorFilter getColorFilter(int color, float alpha) {
        return ColorFilterCache.getColorFilterCache(color).getFilterForLevel(alpha);
    }
}
