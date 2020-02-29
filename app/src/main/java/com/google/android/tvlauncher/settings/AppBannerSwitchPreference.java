package com.google.android.tvlauncher.settings;

import android.content.Context;
import android.widget.ImageView;
import androidx.preference.PreferenceViewHolder;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.Util;

public class AppBannerSwitchPreference extends CustomSwitchPreference {
    private float disabledAlpha;
    private boolean showIcon;

    AppBannerSwitchPreference(Context context) {
        super(context);
        setLayoutResource(C1167R.layout.appchannel_app_banner);
        this.disabledAlpha = Util.getFloat(context.getResources(), C1167R.dimen.preference_app_banner_disabled_alpha);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView icon = (ImageView) holder.findViewById(16908294);
        icon.setClipToOutline(true);
        icon.setAlpha(isEnabled() ? 1.0f : this.disabledAlpha);
        holder.findViewById(C1167R.C1170id.icon_container).setVisibility(this.showIcon ? 0 : 8);
    }

    /* access modifiers changed from: package-private */
    public void setShowIcon(boolean showIcon2) {
        this.showIcon = showIcon2;
    }
}
