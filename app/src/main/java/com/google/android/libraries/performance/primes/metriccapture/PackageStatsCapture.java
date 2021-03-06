package com.google.android.libraries.performance.primes.metriccapture;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Process;
import com.google.android.libraries.performance.primes.PrimesLog;
import com.google.android.libraries.performance.primes.trace.PrimesTrace;
import com.google.android.libraries.stitch.util.ThreadUtil;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class PackageStatsCapture {
    private static final long CALLBACK_TIMEOUT_MS = 15000;
    static final PackageStatsInvocation[] GETTER_INVOCATIONS = {new PackageStatsInvocation("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class}) {
        /* access modifiers changed from: package-private */
        public Object[] params(String packageName, int uid, IPackageStatsObserver callback) {
            return new Object[]{packageName, callback};
        }
    }, new PackageStatsInvocation("getPackageSizeInfo", new Class[]{String.class, Integer.TYPE, IPackageStatsObserver.class}) {
        /* access modifiers changed from: package-private */
        public Object[] params(String packageName, int uid, IPackageStatsObserver callback) {
            return new Object[]{packageName, Integer.valueOf(uid), callback};
        }
    }, new PackageStatsInvocation("getPackageSizeInfoAsUser", new Class[]{String.class, Integer.TYPE, IPackageStatsObserver.class}) {
        /* access modifiers changed from: package-private */
        public Object[] params(String packageName, int uid, IPackageStatsObserver callback) {
            return new Object[]{packageName, Integer.valueOf(uid), callback};
        }
    }};
    private static final String GET_PACKAGE_PERMISSION = "android.permission.GET_PACKAGE_SIZE";
    private static final String TAG = "PackageStatsCapture";

    private PackageStatsCapture() {
    }

    private static final class PackageStatsCallback extends IPackageStatsObserver.Stub {
        private volatile PackageStats packageStats;
        private final Semaphore semaphore;

        private PackageStatsCallback() {
            this.semaphore = new Semaphore(1);
        }

        /* access modifiers changed from: private */
        public void acquireLock() throws InterruptedException {
            this.semaphore.acquire();
        }

        public void onGetStatsCompleted(PackageStats stats, boolean success) {
            if (success) {
                String valueOf = String.valueOf(stats);
                StringBuilder sb = new StringBuilder(valueOf.length() + 30);
                sb.append("Success getting PackageStats: ");
                sb.append(valueOf);
                PrimesLog.m48d(PackageStatsCapture.TAG, sb.toString());
                this.packageStats = stats;
            } else {
                PrimesLog.m56w(PackageStatsCapture.TAG, "Failure getting PackageStats");
            }
            this.semaphore.release();
        }

        /* access modifiers changed from: private */
        public PackageStats waitForStats(long timeoutMs) throws InterruptedException {
            if (this.semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                return this.packageStats;
            }
            PrimesLog.m56w(PackageStatsCapture.TAG, "Timeout while waiting for PackageStats callback");
            return null;
        }
    }

    static abstract class PackageStatsInvocation {
        private final String methodName;
        private final Class<?>[] paramTypes;

        /* access modifiers changed from: package-private */
        public abstract Object[] params(String str, int i, IPackageStatsObserver iPackageStatsObserver);

        PackageStatsInvocation(String methodName2, Class<?>[] paramTypes2) {
            this.methodName = methodName2;
            this.paramTypes = paramTypes2;
        }

        /* access modifiers changed from: package-private */
        public boolean invoke(PackageManager packageManager, String packageName, int uid, IPackageStatsObserver callback) {
            try {
                packageManager.getClass().getMethod(this.methodName, this.paramTypes).invoke(packageManager, params(packageName, uid, callback));
                return true;
            } catch (NoSuchMethodException me) {
                PrimesLog.m47d(PackageStatsCapture.TAG, "PackageStats getter not found", me);
                return false;
            } catch (Error | Exception e) {
                PrimesLog.m52i(PackageStatsCapture.TAG, e.getClass().getSimpleName() + " for " + this.methodName + '(' + Arrays.asList(this.paramTypes) + ") invocation");
                return false;
            }
        }
    }

    private static boolean isCallbackPresent() {
        try {
            return !Modifier.isAbstract(PackageStatsCallback.class.getMethod("onGetStatsCompleted", PackageStats.class, Boolean.TYPE).getModifiers());
        } catch (Error | Exception e) {
            PrimesLog.m47d(TAG, "failure", e);
            return false;
        }
    }

    static PackageStats getPackageStatsUsingInternalAPI(Context context, long callbackTimeoutMs, PackageStatsInvocation... getterInvocations) {
        if (!isCallbackPresent()) {
            PrimesLog.m56w(TAG, "Callback implementation stripped by proguard.");
            return null;
        }
        PackageStatsCallback callback = new PackageStatsCallback();
        try {
            callback.acquireLock();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            int uid = Process.myUid();
            for (PackageStatsInvocation invoker : getterInvocations) {
                if (invoker.invoke(pm, packageName, uid, callback)) {
                    PrimesLog.m52i(TAG, "Success invoking PackageStats capture.");
                    return callback.waitForStats(callbackTimeoutMs);
                }
            }
            PrimesLog.m56w(TAG, "Couldn't capture PackageStats.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private static boolean hasPkgPermission(Context context) {
        return context.getPackageManager().checkPermission(GET_PACKAGE_PERMISSION, context.getPackageName()) == 0 || context.checkCallingOrSelfPermission(GET_PACKAGE_PERMISSION) == 0;
    }

    public static PackageStats getPackageStats(Context context) {
        try {
            PrimesTrace.beginSection("PackageStatsCapture-getPackageStats");
            ThreadUtil.ensureBackgroundThread();
            if (Build.VERSION.SDK_INT >= 26) {
                return PackageStatsCaptureO.getPackageStats(context);
            }
            if (hasPkgPermission(context)) {
                PackageStats packageStatsUsingInternalAPI = getPackageStatsUsingInternalAPI(context, CALLBACK_TIMEOUT_MS, GETTER_INVOCATIONS);
                PrimesTrace.endSection();
                return packageStatsUsingInternalAPI;
            }
            PrimesLog.m56w(TAG, "android.permission.GET_PACKAGE_SIZE required");
            PrimesTrace.endSection();
            return null;
        } finally {
            PrimesTrace.endSection();
        }
    }
}
