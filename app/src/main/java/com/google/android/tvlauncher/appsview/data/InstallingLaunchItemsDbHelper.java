package com.google.android.tvlauncher.appsview.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

class InstallingLaunchItemsDbHelper extends SQLiteOpenHelper {
    static final String COLUMN_IS_GAME = "is_game";
    static final int COLUMN_IS_GAME_INDEX = 1;
    static final String COLUMN_PACKAGE_NAME = "package_name";
    static final int COLUMN_PACKAGE_NAME_INDEX = 0;
    static final String COLUMN_PROMISE_ICON = "promise_icon";
    static final int COLUMN_PROMISE_ICON_INDEX = 2;
    private static final String DATABASE_NAME = "InstallingApps.db";
    private static final int DATABASE_VERSION = 1;
    static final String PACKAGE_NAME_SELECTION = "package_name LIKE ?";
    /* access modifiers changed from: private */
    public static final String[] READ_ALL_ITEMS_PROJECTION = {"package_name", COLUMN_IS_GAME, COLUMN_PROMISE_ICON};
    private static final String SQL_CREATE_INSTALLING_APP = "CREATE TABLE IF NOT EXISTS installing_apps (package_name TEXT PRIMARY KEY,is_game INTEGER NOT NULL,promise_icon BLOB NOT NULL)";
    private static final String SQL_DELETE_INSTALLING_APP = "DROP TABLE IF EXISTS installing_apps";
    static final String TABLE_NAME = "installing_apps";
    private static final String TAG = "InstallingLaunchItemsDbHelper";
    private static InstallingLaunchItemsDbHelper installingLaunchItemsDbHelper = null;
    /* access modifiers changed from: private */
    public final Context context;

    interface OnInstallingAppsReadListener {
        void onInstallingAppsRead(ArrayList<InstallingAppStoredData> arrayList);
    }

    interface OnTaskCompletedListener {
        void onTaskCompleted();
    }

    static byte[] getBitmapAsByteArray(Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static InstallingLaunchItemsDbHelper getInstance(Context context2) {
        if (installingLaunchItemsDbHelper == null) {
            synchronized (InstallingLaunchItemsDbHelper.class) {
                if (installingLaunchItemsDbHelper == null) {
                    installingLaunchItemsDbHelper = new InstallingLaunchItemsDbHelper(context2.getApplicationContext());
                }
            }
        }
        return installingLaunchItemsDbHelper;
    }

    private InstallingLaunchItemsDbHelper(Context context2) {
        this(context2, DATABASE_NAME);
    }

    InstallingLaunchItemsDbHelper(Context context2, String databaseName) {
        super(context2, databaseName, (SQLiteDatabase.CursorFactory) null, 1);
        this.context = context2;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INSTALLING_APP);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_INSTALLING_APP);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_INSTALLING_APP);
        onCreate(db);
    }

    /* access modifiers changed from: package-private */
    public void insertInstallingItem(String packageName, boolean isGame, Drawable icon, OnTaskCompletedListener callback) {
        new InsertInstallingItemTask(packageName, isGame, icon, callback).execute();
    }

    /* access modifiers changed from: package-private */
    public void updateInstallingItem(String packageName, boolean isGame, Drawable icon, OnTaskCompletedListener callback) {
        new UpdateInstallingItemTask(packageName, isGame, icon, callback).execute();
    }

    /* access modifiers changed from: package-private */
    public void deleteInstallingItem(String pkgName, OnTaskCompletedListener callback) {
        new DeleteInstallingItemTask(pkgName, callback).execute();
    }

    /* access modifiers changed from: package-private */
    public void readAllInstallingItems(OnInstallingAppsReadListener onInstallingAppsReadListener) {
        new ReadAllInstallingItemsTask(onInstallingAppsReadListener).execute();
    }

    private class InsertInstallingItemTask extends AsyncTask<Void, Void, Void> {
        private final boolean isGame;
        private final OnTaskCompletedListener onTaskCompletedListener;
        private final String packageName;
        private final Drawable promiseIcon;

        public InsertInstallingItemTask(String packageName2, boolean isGame2, Drawable icon, OnTaskCompletedListener listener) {
            this.packageName = packageName2;
            this.isGame = isGame2;
            this.promiseIcon = icon;
            this.onTaskCompletedListener = listener;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            byte[] blob = InstallingLaunchItemsDbHelper.getBitmapAsByteArray(this.promiseIcon);
            ContentValues values = new ContentValues();
            values.put("package_name", this.packageName);
            values.put(InstallingLaunchItemsDbHelper.COLUMN_PROMISE_ICON, blob);
            values.put(InstallingLaunchItemsDbHelper.COLUMN_IS_GAME, Boolean.valueOf(this.isGame));
            SQLiteDatabase db = InstallingLaunchItemsDbHelper.this.getWritableDatabase();
            db.beginTransaction();
            try {
                if (db.insert(InstallingLaunchItemsDbHelper.TABLE_NAME, null, values) >= 0) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    return null;
                }
                throw new SQLiteException("Unable to insert into database.");
            } catch (SQLiteException e) {
                String valueOf = String.valueOf(this.packageName);
                Log.e(InstallingLaunchItemsDbHelper.TAG, valueOf.length() != 0 ? "Could not insert installing app into database : ".concat(valueOf) : "Could not insert installing app into database : ", e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void aVoid) {
            OnTaskCompletedListener onTaskCompletedListener2 = this.onTaskCompletedListener;
            if (onTaskCompletedListener2 != null) {
                onTaskCompletedListener2.onTaskCompleted();
            }
        }
    }

    private class UpdateInstallingItemTask extends AsyncTask<Void, Void, Void> {
        private final boolean isGame;
        private final OnTaskCompletedListener onTaskCompletedListener;
        private final String packageName;
        private final byte[] promiseIcon;

        public UpdateInstallingItemTask(String packageName2, boolean isGame2, Drawable icon, OnTaskCompletedListener listener) {
            this.packageName = packageName2;
            this.isGame = isGame2;
            this.promiseIcon = InstallingLaunchItemsDbHelper.getBitmapAsByteArray(icon);
            this.onTaskCompletedListener = listener;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put(InstallingLaunchItemsDbHelper.COLUMN_PROMISE_ICON, this.promiseIcon);
            values.put(InstallingLaunchItemsDbHelper.COLUMN_IS_GAME, Boolean.valueOf(this.isGame));
            SQLiteDatabase db = InstallingLaunchItemsDbHelper.this.getWritableDatabase();
            db.beginTransaction();
            try {
                if (db.update(InstallingLaunchItemsDbHelper.TABLE_NAME, values, InstallingLaunchItemsDbHelper.PACKAGE_NAME_SELECTION, new String[]{this.packageName}) == 0) {
                    String valueOf = String.valueOf(this.packageName);
                    Log.e(InstallingLaunchItemsDbHelper.TAG, valueOf.length() != 0 ? "Missing package requested for update : ".concat(valueOf) : "Missing package requested for update : ");
                }
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                String valueOf2 = String.valueOf(this.packageName);
                Log.e(InstallingLaunchItemsDbHelper.TAG, valueOf2.length() != 0 ? "Could not update installing app in database : ".concat(valueOf2) : "Could not update installing app in database : ", e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
            db.endTransaction();
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void aVoid) {
            OnTaskCompletedListener onTaskCompletedListener2 = this.onTaskCompletedListener;
            if (onTaskCompletedListener2 != null) {
                onTaskCompletedListener2.onTaskCompleted();
            }
        }
    }

    private class DeleteInstallingItemTask extends AsyncTask<Void, Void, Void> {
        private final OnTaskCompletedListener onTaskCompletedListener;
        private final String packageName;

        public DeleteInstallingItemTask(String pkgName, OnTaskCompletedListener listener) {
            this.packageName = pkgName;
            this.onTaskCompletedListener = listener;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            SQLiteDatabase db = InstallingLaunchItemsDbHelper.this.getWritableDatabase();
            db.beginTransaction();
            try {
                db.delete(InstallingLaunchItemsDbHelper.TABLE_NAME, InstallingLaunchItemsDbHelper.PACKAGE_NAME_SELECTION, new String[]{this.packageName});
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                String valueOf = String.valueOf(this.packageName);
                Log.e(InstallingLaunchItemsDbHelper.TAG, valueOf.length() != 0 ? "Could not delete installing app into database : ".concat(valueOf) : "Could not delete installing app into database : ", e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
            db.endTransaction();
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void aVoid) {
            OnTaskCompletedListener onTaskCompletedListener2 = this.onTaskCompletedListener;
            if (onTaskCompletedListener2 != null) {
                onTaskCompletedListener2.onTaskCompleted();
            }
        }
    }

    private class ReadAllInstallingItemsTask extends AsyncTask<Void, Void, ArrayList<InstallingAppStoredData>> {
        private final OnInstallingAppsReadListener onInstallingAppsReadListener;

        /* access modifiers changed from: protected */
        public /* bridge */ /* synthetic */ void onPostExecute(Object obj) {
            onPostExecute((ArrayList<InstallingAppStoredData>) ((ArrayList) obj));
        }

        public ReadAllInstallingItemsTask(OnInstallingAppsReadListener onInstallingAppsReadListener2) {
            this.onInstallingAppsReadListener = onInstallingAppsReadListener2;
        }

        /* access modifiers changed from: protected */
        public ArrayList<InstallingAppStoredData> doInBackground(Void... params) {
            Cursor cursor = InstallingLaunchItemsDbHelper.this.getReadableDatabase().query(InstallingLaunchItemsDbHelper.TABLE_NAME, InstallingLaunchItemsDbHelper.READ_ALL_ITEMS_PROJECTION, null, null, null, null, null);
            ArrayList<InstallingAppStoredData> storedData = new ArrayList<>();
            while (cursor.moveToNext()) {
                String pkgName = cursor.getString(0);
                boolean z = true;
                int isGame = cursor.getInt(1);
                byte[] icon = cursor.getBlob(2);
                if (isGame == 0) {
                    z = false;
                }
                storedData.add(new InstallingAppStoredData(pkgName, z, new BitmapDrawable(InstallingLaunchItemsDbHelper.this.context.getResources(), BitmapFactory.decodeByteArray(icon, 0, icon.length))));
            }
            return storedData;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ArrayList<InstallingAppStoredData> installingAppStoredData) {
            this.onInstallingAppsReadListener.onInstallingAppsRead(installingAppStoredData);
        }
    }

    static final class InstallingAppStoredData {
        private final Drawable icon;
        private final boolean isGame;
        private final String pkgName;

        InstallingAppStoredData(String pkgName2, boolean isGame2, Drawable icon2) {
            this.pkgName = pkgName2;
            this.isGame = isGame2;
            this.icon = icon2;
        }

        public String getPackageName() {
            return this.pkgName;
        }

        public boolean isGame() {
            return this.isGame;
        }

        public Drawable getIcon() {
            return this.icon;
        }
    }
}
