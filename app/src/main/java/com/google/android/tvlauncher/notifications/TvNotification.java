package com.google.android.tvlauncher.notifications;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;

public class TvNotification {
    public static final int COLUMN_INDEX_BIG_PICTURE = 11;
    public static final int COLUMN_INDEX_CHANNEL = 7;
    public static final int COLUMN_INDEX_CONTENT_BUTTON_LABEL = 12;
    public static final int COLUMN_INDEX_DISMISSIBLE = 4;
    public static final int COLUMN_INDEX_DISMISS_BUTTON_LABEL = 13;
    public static final int COLUMN_INDEX_HAS_CONTENT_INTENT = 10;
    public static final int COLUMN_INDEX_KEY = 0;
    public static final int COLUMN_INDEX_NOTIF_TEXT = 3;
    public static final int COLUMN_INDEX_NOTIF_TITLE = 2;
    public static final int COLUMN_INDEX_ONGOING = 5;
    public static final int COLUMN_INDEX_PACKAGE_NAME = 1;
    public static final int COLUMN_INDEX_PROGRESS = 8;
    public static final int COLUMN_INDEX_PROGRESS_MAX = 9;
    public static final int COLUMN_INDEX_SMALL_ICON = 6;
    public static final int COLUMN_INDEX_TAG = 14;
    public static final String[] PROJECTION = {"sbn_key", "package_name", "title", "text", NotificationsContract.COLUMN_DISMISSIBLE, NotificationsContract.COLUMN_ONGOING, NotificationsContract.COLUMN_SMALL_ICON, "channel", "progress", NotificationsContract.COLUMN_PROGRESS_MAX, NotificationsContract.COLUMN_HAS_CONTENT_INTENT, NotificationsContract.COLUMN_BIG_PICTURE, NotificationsContract.COLUMN_CONTENT_BUTTON_LABEL, NotificationsContract.COLUMN_DISMISS_BUTTON_LABEL, NotificationsContract.COLUMN_TAG};
    private Bitmap bigPicture;
    private int channel;
    private String contentButtonLabel;
    private String dismissButtonLabel;
    private boolean dismissible;
    private boolean hasContentIntent;
    private boolean isOngoing;
    private String notificationKey;
    private String packageName;
    private int progress;
    private int progressMax;
    private Icon smallIcon;
    private String tag;
    private String text;
    private String title;

    public TvNotification(String key, String packageName2, String title2, String text2, boolean dismissible2, boolean ongoing, Icon smallIcon2, int channel2, int progress2, int progressMax2, boolean hasContentIntent2, Bitmap bigPicture2, String contentButtonLabel2, String dismissButtonLabel2, String tag2) {
        this.notificationKey = key;
        this.packageName = packageName2;
        this.title = title2;
        this.text = text2;
        this.dismissible = dismissible2;
        this.isOngoing = ongoing;
        this.smallIcon = smallIcon2;
        this.channel = channel2;
        this.progress = progress2;
        this.progressMax = progressMax2;
        this.hasContentIntent = hasContentIntent2;
        this.bigPicture = bigPicture2;
        this.contentButtonLabel = contentButtonLabel2;
        this.dismissButtonLabel = dismissButtonLabel2;
        this.tag = tag2;
    }

    public String getNotificationKey() {
        return this.notificationKey;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getTitle() {
        return this.title;
    }

    public String getText() {
        return this.text;
    }

    public boolean isDismissible() {
        return this.dismissible;
    }

    public boolean isOngoing() {
        return this.isOngoing;
    }

    public Icon getSmallIcon() {
        return this.smallIcon;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getProgressMax() {
        return this.progressMax;
    }

    public boolean hasContentIntent() {
        return this.hasContentIntent;
    }

    public Bitmap getBigPicture() {
        return this.bigPicture;
    }

    public String getContentButtonLabel() {
        return this.contentButtonLabel;
    }

    public String getDismissButtonLabel() {
        return this.dismissButtonLabel;
    }

    public String getTag() {
        return this.tag;
    }

    /* JADX INFO: Multiple debug info for r1v1 java.lang.String: [D('key' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r2v1 java.lang.String: [D('index' int), D('packageName' java.lang.String)] */
    public static TvNotification fromCursor(Cursor cursor) {
        Cursor cursor2 = cursor;
        int index = 0 + 1;
        String key = cursor2.getString(0);
        int index2 = index + 1;
        String packageName2 = cursor2.getString(index);
        int index3 = index2 + 1;
        String title2 = cursor2.getString(index2);
        int index4 = index3 + 1;
        String text2 = cursor2.getString(index3);
        int index5 = index4 + 1;
        boolean dismissible2 = cursor2.getInt(index4) != 0;
        int index6 = index5 + 1;
        boolean ongoing = cursor2.getInt(index5) != 0;
        int index7 = index6 + 1;
        Icon smallIcon2 = getIconFromBytes(cursor2.getBlob(index6));
        int index8 = index7 + 1;
        int channel2 = cursor2.getInt(index7);
        int index9 = index8 + 1;
        int progress2 = cursor2.getInt(index8);
        int index10 = index9 + 1;
        int progressMax2 = cursor2.getInt(index9);
        int index11 = index10 + 1;
        boolean hasContentIntent2 = cursor2.getInt(index10) != 0;
        int index12 = index11 + 1;
        Bitmap bigPicture2 = getBitmapFromBytes(cursor2.getBlob(index11));
        int index13 = index12 + 1;
        return new TvNotification(key, packageName2, title2, text2, dismissible2, ongoing, smallIcon2, channel2, progress2, progressMax2, hasContentIntent2, bigPicture2, cursor2.getString(index12), cursor2.getString(index13), cursor2.getString(index13 + 1));
    }

    private static Bitmap getBitmapFromBytes(byte[] blob) {
        if (blob != null) {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return null;
    }

    /* JADX WARN: Type inference failed for: r2v3, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.graphics.drawable.Icon getIconFromBytes(byte[] r4) {
        /*
            android.os.Parcel r0 = android.os.Parcel.obtain()
            r1 = 0
            if (r4 == 0) goto L_0x001c
            int r2 = r4.length
            r3 = 0
            r0.unmarshall(r4, r3, r2)
            r0.setDataPosition(r3)
            java.lang.Class<android.graphics.drawable.Icon> r2 = android.graphics.drawable.Icon.class
            java.lang.ClassLoader r2 = r2.getClassLoader()
            android.os.Parcelable r2 = r0.readParcelable(r2)
            r1 = r2
            android.graphics.drawable.Icon r1 = (android.graphics.drawable.Icon) r1
        L_0x001c:
            r0.recycle()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.notifications.TvNotification.getIconFromBytes(byte[]):android.graphics.drawable.Icon");
    }

    /* JADX INFO: Multiple debug info for r1v1 java.lang.String: [D('key' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r2v1 java.lang.String: [D('index' int), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r3v1 java.lang.String: [D('title' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r4v1 java.lang.String: [D('index' int), D('text' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v1 int: [D('index' int), D('dismissible' int)] */
    /* JADX INFO: Multiple debug info for r6v1 int: [D('index' int), D('ongoing' int)] */
    /* JADX INFO: Multiple debug info for r7v1 byte[]: [D('index' int), D('smallIconData' byte[])] */
    /* JADX INFO: Multiple debug info for r8v1 int: [D('index' int), D('channel' int)] */
    /* JADX INFO: Multiple debug info for r9v1 int: [D('progress' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r10v1 int: [D('progressMax' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r11v1 int: [D('hasContentIntent' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r12v1 byte[]: [D('bigPictureData' byte[]), D('index' int)] */
    /* JADX INFO: Multiple debug info for r13v1 java.lang.String: [D('index' int), D('contentButtonLabel' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r14v1 java.lang.String: [D('index' int), D('dismissButtonLabel' java.lang.String)] */
    public static Object[] getRowFromCursor(Cursor cursor) {
        Cursor cursor2 = cursor;
        if (cursor2 == null) {
            return null;
        }
        int index = 0 + 1;
        String key = cursor2.getString(0);
        int index2 = index + 1;
        String packageName2 = cursor2.getString(index);
        int index3 = index2 + 1;
        String title2 = cursor2.getString(index2);
        int index4 = index3 + 1;
        String text2 = cursor2.getString(index3);
        int index5 = index4 + 1;
        int dismissible2 = cursor2.getInt(index4);
        int index6 = index5 + 1;
        int ongoing = cursor2.getInt(index5);
        int index7 = index6 + 1;
        byte[] smallIconData = cursor2.getBlob(index6);
        int index8 = index7 + 1;
        int channel2 = cursor2.getInt(index7);
        int index9 = index8 + 1;
        int index10 = cursor2.getInt(index8);
        int index11 = index9 + 1;
        int index12 = cursor2.getInt(index9);
        int index13 = index11 + 1;
        int index14 = cursor2.getInt(index11);
        int index15 = index13 + 1;
        int index16 = index15 + 1;
        int index17 = index16 + 1;
        return new Object[]{key, packageName2, title2, text2, Integer.valueOf(dismissible2), Integer.valueOf(ongoing), smallIconData, Integer.valueOf(channel2), Integer.valueOf(index10), Integer.valueOf(index12), Integer.valueOf(index14), cursor2.getBlob(index13), cursor2.getString(index15), cursor2.getString(index16), cursor2.getString(index17)};
    }
}
