package com.google.android.tvlauncher.settings;

import android.content.Context;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;
import com.google.android.tvlauncher.C1167R;

public class SummaryPreferenceCategory extends PreferenceCategory {
    private boolean enabled;

    SummaryPreferenceCategory(Context context) {
        super(context);
        setLayoutResource(C1167R.layout.preference_category_with_summary);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled2) {
        this.enabled = enabled2;
        super.setEnabled(enabled2);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setEnabled(isEnabled());
    }
}
