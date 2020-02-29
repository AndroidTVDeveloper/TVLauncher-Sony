package com.google.android.tvlauncher.settings;

import android.content.Context;
import android.widget.TextView;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

public class CustomSwitchPreference extends SwitchPreference {
    private boolean dimSummary;
    private boolean showToggle = true;

    CustomSwitchPreference(Context context) {
        super(context);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        boolean z = false;
        holder.findViewById(16908352).setVisibility(this.showToggle ? 0 : 4);
        holder.itemView.setClickable(this.showToggle);
        TextView summary = (TextView) holder.findViewById(16908304);
        if (isEnabled() && !this.dimSummary) {
            z = true;
        }
        summary.setEnabled(z);
    }

    /* access modifiers changed from: package-private */
    public boolean isShowToggle() {
        return this.showToggle;
    }

    /* access modifiers changed from: package-private */
    public void setShowToggle(boolean showToggle2) {
        this.showToggle = showToggle2;
    }

    /* access modifiers changed from: package-private */
    public void setDimSummary(boolean dimSummary2) {
        this.dimSummary = dimSummary2;
    }
}
