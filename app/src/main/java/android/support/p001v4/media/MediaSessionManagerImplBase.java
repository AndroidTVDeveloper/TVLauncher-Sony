package android.support.p001v4.media;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.p001v4.media.MediaSessionManager;
import android.support.p001v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.util.Log;

/* renamed from: android.support.v4.media.MediaSessionManagerImplBase */
class MediaSessionManagerImplBase implements MediaSessionManager.MediaSessionManagerImpl {
    private static final boolean DEBUG = MediaSessionManager.DEBUG;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String PERMISSION_MEDIA_CONTENT_CONTROL = "android.permission.MEDIA_CONTENT_CONTROL";
    private static final String PERMISSION_STATUS_BAR_SERVICE = "android.permission.STATUS_BAR_SERVICE";
    private static final String TAG = "MediaSessionManager";
    ContentResolver mContentResolver = this.mContext.getContentResolver();
    Context mContext;

    MediaSessionManagerImplBase(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isTrustedForMediaControl(MediaSessionManager.RemoteUserInfoImpl userInfo) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfo(userInfo.getPackageName(), 0) == null) {
                return false;
            }
            if (isPermissionGranted(userInfo, PERMISSION_STATUS_BAR_SERVICE) || isPermissionGranted(userInfo, PERMISSION_MEDIA_CONTENT_CONTROL) || userInfo.getUid() == 1000 || isEnabledNotificationListener(userInfo)) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "Package " + userInfo.getPackageName() + " doesn't exist");
            }
            return false;
        }
    }

    private boolean isPermissionGranted(MediaSessionManager.RemoteUserInfoImpl userInfo, String permission) {
        if (userInfo.getPid() < 0) {
            if (this.mContext.getPackageManager().checkPermission(permission, userInfo.getPackageName()) == 0) {
                return true;
            }
            return false;
        } else if (this.mContext.checkPermission(permission, userInfo.getPid(), userInfo.getUid()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabledNotificationListener(MediaSessionManager.RemoteUserInfoImpl userInfo) {
        String enabledNotifListeners = Settings.Secure.getString(this.mContentResolver, ENABLED_NOTIFICATION_LISTENERS);
        if (enabledNotifListeners == null) {
            return false;
        }
        String[] components = enabledNotifListeners.split(":");
        for (String unflattenFromString : components) {
            ComponentName component = ComponentName.unflattenFromString(unflattenFromString);
            if (component != null && component.getPackageName().equals(userInfo.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /* renamed from: android.support.v4.media.MediaSessionManagerImplBase$RemoteUserInfoImplBase */
    static class RemoteUserInfoImplBase implements MediaSessionManager.RemoteUserInfoImpl {
        private String mPackageName;
        private int mPid;
        private int mUid;

        RemoteUserInfoImplBase(String packageName, int pid, int uid) {
            this.mPackageName = packageName;
            this.mPid = pid;
            this.mUid = uid;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int getPid() {
            return this.mPid;
        }

        public int getUid() {
            return this.mUid;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RemoteUserInfoImplBase)) {
                return false;
            }
            RemoteUserInfoImplBase otherUserInfo = (RemoteUserInfoImplBase) obj;
            if (this.mPid == -1 || otherUserInfo.mPid == -1) {
                if (!TextUtils.equals(this.mPackageName, otherUserInfo.mPackageName) || this.mUid != otherUserInfo.mUid) {
                    return false;
                }
                return true;
            } else if (TextUtils.equals(this.mPackageName, otherUserInfo.mPackageName) && this.mPid == otherUserInfo.mPid && this.mUid == otherUserInfo.mUid) {
                return true;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return ObjectsCompat.hash(this.mPackageName, Integer.valueOf(this.mUid));
        }
    }
}
