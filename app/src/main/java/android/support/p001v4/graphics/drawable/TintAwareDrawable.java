package android.support.p001v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;

/* renamed from: android.support.v4.graphics.drawable.TintAwareDrawable */
public interface TintAwareDrawable {
    void setTint(int i);

    void setTintList(ColorStateList colorStateList);

    void setTintMode(PorterDuff.Mode mode);
}
