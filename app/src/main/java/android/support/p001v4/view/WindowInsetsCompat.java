package android.support.p001v4.view;

import android.graphics.Rect;
import android.os.Build;
import android.support.p001v4.graphics.Insets;
import android.support.p001v4.util.ObjectsCompat;
import android.view.WindowInsets;

/* renamed from: android.support.v4.view.WindowInsetsCompat */
public class WindowInsetsCompat {
    private final Object mInsets;

    WindowInsetsCompat(Object insets) {
        this.mInsets = insets;
    }

    public WindowInsetsCompat(WindowInsetsCompat src) {
        WindowInsets windowInsets = null;
        if (Build.VERSION.SDK_INT >= 20) {
            this.mInsets = src != null ? new WindowInsets((WindowInsets) src.mInsets) : windowInsets;
        } else {
            this.mInsets = null;
        }
    }

    public int getSystemWindowInsetLeft() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetLeft();
        }
        return 0;
    }

    public int getSystemWindowInsetTop() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetTop();
        }
        return 0;
    }

    public int getSystemWindowInsetRight() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetRight();
        }
        return 0;
    }

    public int getSystemWindowInsetBottom() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetBottom();
        }
        return 0;
    }

    public boolean hasSystemWindowInsets() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).hasSystemWindowInsets();
        }
        return false;
    }

    public boolean hasInsets() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).hasInsets();
        }
        return false;
    }

    public boolean isConsumed() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).isConsumed();
        }
        return false;
    }

    public boolean isRound() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).isRound();
        }
        return false;
    }

    public WindowInsetsCompat consumeSystemWindowInsets() {
        if (Build.VERSION.SDK_INT >= 20) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).consumeSystemWindowInsets());
        }
        return null;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= 20) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).replaceSystemWindowInsets(left, top, right, bottom));
        }
        return null;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(Rect systemWindowInsets) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).replaceSystemWindowInsets(systemWindowInsets));
        }
        return null;
    }

    public int getStableInsetTop() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetTop();
        }
        return 0;
    }

    public int getStableInsetLeft() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetLeft();
        }
        return 0;
    }

    public int getStableInsetRight() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetRight();
        }
        return 0;
    }

    public int getStableInsetBottom() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetBottom();
        }
        return 0;
    }

    public boolean hasStableInsets() {
        if (Build.VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).hasStableInsets();
        }
        return false;
    }

    public WindowInsetsCompat consumeStableInsets() {
        if (Build.VERSION.SDK_INT >= 21) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).consumeStableInsets());
        }
        return null;
    }

    public DisplayCutoutCompat getDisplayCutout() {
        if (Build.VERSION.SDK_INT >= 28) {
            return DisplayCutoutCompat.wrap(((WindowInsets) this.mInsets).getDisplayCutout());
        }
        return null;
    }

    public WindowInsetsCompat consumeDisplayCutout() {
        if (Build.VERSION.SDK_INT >= 28) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).consumeDisplayCutout());
        }
        return this;
    }

    public Insets getSystemWindowInsets() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Insets.wrap(((WindowInsets) this.mInsets).getSystemWindowInsets());
        }
        return Insets.m2of(getSystemWindowInsetLeft(), getSystemWindowInsetTop(), getSystemWindowInsetRight(), getSystemWindowInsetBottom());
    }

    public Insets getStableInsets() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Insets.wrap(((WindowInsets) this.mInsets).getStableInsets());
        }
        return Insets.m2of(getStableInsetLeft(), getStableInsetTop(), getStableInsetRight(), getStableInsetBottom());
    }

    public Insets getMandatorySystemGestureInsets() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Insets.wrap(((WindowInsets) this.mInsets).getMandatorySystemGestureInsets());
        }
        return getSystemWindowInsets();
    }

    public Insets getTappableElementInsets() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Insets.wrap(((WindowInsets) this.mInsets).getTappableElementInsets());
        }
        return getSystemWindowInsets();
    }

    public Insets getSystemGestureInsets() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Insets.wrap(((WindowInsets) this.mInsets).getSystemGestureInsets());
        }
        return getSystemWindowInsets();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ObjectsCompat.equals(this.mInsets, ((WindowInsetsCompat) o).mInsets);
    }

    public int hashCode() {
        Object obj = this.mInsets;
        if (obj == null) {
            return 0;
        }
        return obj.hashCode();
    }

    public WindowInsets toWindowInsets() {
        return (WindowInsets) this.mInsets;
    }

    public static WindowInsetsCompat toWindowInsetsCompat(WindowInsets insets) {
        insets.getClass();
        return new WindowInsetsCompat(insets);
    }
}
