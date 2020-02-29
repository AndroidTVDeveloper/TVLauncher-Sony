package android.support.p001v4.graphics;

import android.graphics.Rect;

/* renamed from: android.support.v4.graphics.Insets */
public final class Insets {
    public static final Insets NONE = new Insets(0, 0, 0, 0);
    public final int bottom;
    public final int left;
    public final int right;
    public final int top;

    private Insets(int left2, int top2, int right2, int bottom2) {
        this.left = left2;
        this.top = top2;
        this.right = right2;
        this.bottom = bottom2;
    }

    /* renamed from: of */
    public static Insets m2of(int left2, int top2, int right2, int bottom2) {
        if (left2 == 0 && top2 == 0 && right2 == 0 && bottom2 == 0) {
            return NONE;
        }
        return new Insets(left2, top2, right2, bottom2);
    }

    /* renamed from: of */
    public static Insets m3of(Rect r) {
        return m2of(r.left, r.top, r.right, r.bottom);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Insets insets = (Insets) o;
        return this.bottom == insets.bottom && this.left == insets.left && this.right == insets.right && this.top == insets.top;
    }

    public int hashCode() {
        return (((((this.left * 31) + this.top) * 31) + this.right) * 31) + this.bottom;
    }

    public String toString() {
        return "Insets{left=" + this.left + ", top=" + this.top + ", right=" + this.right + ", bottom=" + this.bottom + '}';
    }

    public static Insets wrap(android.graphics.Insets insets) {
        return m2of(insets.left, insets.top, insets.right, insets.bottom);
    }
}
