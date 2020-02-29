package com.google.android.tvlauncher.util.palette;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import com.bumptech.glide.Glide;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.palette.InstallingItemPaletteBitmapContainer;
import com.google.android.tvlauncher.appsview.palette.InstallingLaunchItemTranscoder;
import com.google.android.tvlauncher.util.Util;

public class PaletteUtil {
    private static boolean paletteTranscoderRegistered = false;

    private PaletteUtil() {
    }

    public static void registerGlidePaletteTranscoder(Context context) {
        if (!paletteTranscoderRegistered) {
            Resources resources = context.getResources();
            Glide.get(context).getRegistry().register(Bitmap.class, PaletteBitmapContainer.class, new PaletteBitmapTranscoder(context));
            Glide.get(context).getRegistry().register(Bitmap.class, InstallingItemPaletteBitmapContainer.class, new InstallingLaunchItemTranscoder(context, Util.getFloat(resources, C1167R.dimen.install_banner_darken_factor), Util.getFloat(resources, C1167R.dimen.gray_scale_saturation)));
            paletteTranscoderRegistered = true;
        }
    }
}
