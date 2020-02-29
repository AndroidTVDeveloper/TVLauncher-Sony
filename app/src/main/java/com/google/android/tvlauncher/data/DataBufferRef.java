package com.google.android.tvlauncher.data;

abstract class DataBufferRef {
    private final AbstractDataBuffer<?> dataBuffer;
    private int position;

    DataBufferRef(AbstractDataBuffer<?> dataBuffer2, int position2) {
        if (position2 < 0 || position2 >= dataBuffer2.getCount()) {
            StringBuilder sb = new StringBuilder(56);
            sb.append("Position [");
            sb.append(position2);
            sb.append("] is out of bounds [0, ");
            sb.append(dataBuffer2.getCount() - 1);
            sb.append("]");
            throw new IllegalArgumentException(sb.toString());
        }
        this.dataBuffer = dataBuffer2;
        this.position = position2;
    }

    /* access modifiers changed from: protected */
    public long getLong(int column) {
        return this.dataBuffer.getLong(this.position, column);
    }

    /* access modifiers changed from: protected */
    public int getInt(int column) {
        return this.dataBuffer.getInt(this.position, column);
    }

    /* access modifiers changed from: protected */
    public String getString(int column) {
        return this.dataBuffer.getString(this.position, column);
    }

    /* access modifiers changed from: protected */
    public byte[] getBlob(int column) {
        return this.dataBuffer.getBlob(this.position, column);
    }
}
