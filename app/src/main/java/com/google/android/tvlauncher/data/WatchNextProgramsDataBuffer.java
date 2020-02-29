package com.google.android.tvlauncher.data;

import android.database.Cursor;

public class WatchNextProgramsDataBuffer extends AbstractDataBuffer<ProgramRef> {
    private int startIndex = 0;

    public /* bridge */ /* synthetic */ byte[] getBlob(int i, int i2) {
        return super.getBlob(i, i2);
    }

    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    public WatchNextProgramsDataBuffer(Cursor cursor) {
        super(cursor);
        calculateStartIndex();
    }

    public ProgramRef get(int position) {
        return new ProgramRef(this, position);
    }

    public long getLong(int row, int column) {
        return super.getLong(this.startIndex + row, column);
    }

    public int getInt(int row, int column) {
        return super.getInt(this.startIndex + row, column);
    }

    public String getString(int row, int column) {
        return super.getString(this.startIndex + row, column);
    }

    public int getCount() {
        return super.getCount() - this.startIndex;
    }

    /* access modifiers changed from: package-private */
    public boolean refresh() {
        int oldStartIndex = this.startIndex;
        calculateStartIndex();
        return oldStartIndex != this.startIndex;
    }

    private void calculateStartIndex() {
        if (this.cursor == null || this.cursor.getCount() == 0) {
            this.startIndex = 0;
            return;
        }
        long currentTime = System.currentTimeMillis();
        int savedCursorPos = this.cursor.getPosition();
        this.cursor.moveToFirst();
        int pos = 0;
        while (Long.valueOf(this.cursor.getLong(42)).compareTo(Long.valueOf(currentTime)) > 0) {
            pos++;
            if (!this.cursor.moveToNext()) {
                break;
            }
        }
        this.cursor.moveToPosition(savedCursorPos);
        this.startIndex = pos;
    }
}
