package android.support.p001v4.location;

import android.location.LocationManager;
import android.os.Build;

/* renamed from: android.support.v4.location.LocationManagerCompat */
public final class LocationManagerCompat {
    public static boolean isLocationEnabled(LocationManager locationManager) {
        if (Build.VERSION.SDK_INT >= 28) {
            return locationManager.isLocationEnabled();
        }
        return locationManager.isProviderEnabled("network") || locationManager.isProviderEnabled("gps");
    }

    private LocationManagerCompat() {
    }
}
