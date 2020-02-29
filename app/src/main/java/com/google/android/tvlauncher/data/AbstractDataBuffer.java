package com.google.android.tvlauncher.data;

import android.database.Cursor;

abstract class AbstractDataBuffer<T> {
    protected Cursor cursor;

    public abstract T get(int i);

    AbstractDataBuffer(Cursor cursor2) {
        this.cursor = cursor2;
    }

    public void release() {
        this.cursor.close();
    }

    public boolean isReleased() {
        return this.cursor.isClosed();
    }

    public long getLong(int row, int column) {
        checkNotReleased();
        this.cursor.moveToPosition(row);
        return this.cursor.getLong(column);
    }

    public int getInt(int row, int column) {
        checkNotReleased();
        this.cursor.moveToPosition(row);
        return this.cursor.getInt(column);
    }

    public String getString(int row, int column) {
        checkNotReleased();
        this.cursor.moveToPosition(row);
        return this.cursor.getString(column);
    }

    public byte[] getBlob(int row, int column) {
        checkNotReleased();
        this.cursor.moveToPosition(row);
        return this.cursor.getBlob(column);
    }

    public int getCount() {
        checkNotReleased();
        return this.cursor.getCount();
    }

    private void checkNotReleased() {
        if (isReleased()) {
            throw new IllegalArgumentException("Buffer is released.");
        }
    }
}
