package android.support.p001v4.p003os;

import android.os.Parcel;

/* renamed from: android.support.v4.os.ParcelCompat */
public final class ParcelCompat {
    public static boolean readBoolean(Parcel in) {
        return in.readInt() != 0;
    }

    public static void writeBoolean(Parcel out, boolean value) {
        out.writeInt(value);
    }

    private ParcelCompat() {
    }
}
