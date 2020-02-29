package com.google.android.libraries.performance.primes.hprof;

import com.google.android.libraries.performance.primes.hprof.collect.IntIntMap;
import com.google.android.libraries.stitch.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public final class ParseContext {
    private static final int OBJECT = 2;
    private final ByteBuffer buffer;
    private final ByteBuffer duplicate;
    private final int idSize;
    private final IntIntMap rootTagSizes;
    private final int[] typeSizes;

    public ParseContext(ByteBuffer buffer2) {
        buffer2.rewind();
        buffer2.order(ByteOrder.BIG_ENDIAN);
        this.buffer = buffer2;
        this.duplicate = buffer2.duplicate();
        do {
        } while (buffer2.get() != 0);
        this.idSize = buffer2.getInt();
        Preconditions.checkState(this.idSize > 0);
        buffer2.getLong();
        this.typeSizes = Hprofs.getTypesSizes(this.idSize);
        this.rootTagSizes = new IntIntMap();
        Hprofs.addRootTagSizes(this.idSize, new ParseContext$$Lambda$0(this));
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$ParseContext(int tag, int size) {
        this.rootTagSizes.putIfAbsent(tag, size);
    }

    public static ParseContext prepareContext(File hprofFile) throws IOException {
        FileInputStream inputStream = null;
        FileChannel fileChannel = null;
        try {
            inputStream = new FileInputStream(hprofFile);
            fileChannel = inputStream.getChannel();
            ParseContext parseContext = new ParseContext(fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()));
            if (fileChannel != null) {
                fileChannel.close();
            }
            inputStream.close();
            return parseContext;
        } catch (Throwable th) {
            if (fileChannel != null) {
                fileChannel.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public int getIdSize() {
        return this.idSize;
    }

    public String readString(int stringPosition) {
        Preconditions.checkArgument(stringPosition >= 0);
        int i = this.duplicate.getInt(stringPosition);
        int i2 = this.idSize;
        byte[] bytes = new byte[(i - i2)];
        this.duplicate.position(stringPosition + 4 + i2);
        this.duplicate.get(bytes);
        return new String(bytes, Charset.defaultCharset());
    }

    public int getStringLength(int stringPosition) {
        return this.buffer.getInt(stringPosition) - this.idSize;
    }

    public int getStringBytesPos(int stringPosition) {
        return stringPosition + 4 + this.idSize;
    }

    public int getTypeSize(int type) {
        int typeSize = this.typeSizes[type];
        Preconditions.checkState(typeSize > 0);
        return typeSize;
    }

    public boolean isRootTag(int tag) {
        return this.rootTagSizes.containsKey(tag);
    }

    public int getRootTagSize(int rootTag) {
        return this.rootTagSizes.get(rootTag);
    }

    public void skipBytes(int numBytes) {
        Preconditions.checkArgument(numBytes >= 0);
        int newPosition = this.buffer.position() + numBytes;
        if (newPosition <= this.buffer.limit()) {
            this.buffer.position(newPosition);
            return;
        }
        throw new BufferUnderflowException();
    }

    public int readId() {
        int i = this.idSize;
        if (i == 1) {
            return this.buffer.get();
        }
        if (i == 2) {
            return this.buffer.getShort();
        }
        if (i == 4) {
            return this.buffer.getInt();
        }
        throw new IllegalStateException();
    }

    public int readId(int position) {
        int i = this.idSize;
        if (i == 1) {
            return this.buffer.get(position);
        }
        if (i == 2) {
            return this.buffer.getShort(position);
        }
        if (i == 4) {
            return this.buffer.getInt(position);
        }
        throw new IllegalStateException();
    }

    public byte readByte(int position) {
        return this.buffer.get(position);
    }

    public int readInt(int position) {
        return this.buffer.getInt(position);
    }

    public boolean isObjectType(int type) {
        return type == 2;
    }
}
