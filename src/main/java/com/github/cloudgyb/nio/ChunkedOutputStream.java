package com.github.cloudgyb.nio;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author cloudgyb
 * @since 2025/3/15 16:40
 */
public class ChunkedOutputStream extends FilterOutputStream {
    private static final int DEFAULT_CHUNK_SIZE = 4096;
    private final int chunkSize = DEFAULT_CHUNK_SIZE;
    private final int offset = 4 + 2;
    private int pos = offset;
    private final ByteBuffer buffer;
    private int count;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field {@code this.out} for later use, or
     *            {@code null} if this instance is to be
     *            created without an underlying stream.
     */
    public ChunkedOutputStream(OutputStream out) {
        this(false, out);
    }

    public ChunkedOutputStream(boolean isUseDirectBuffer, OutputStream out) {
        super(out);
        int capacity = offset + chunkSize + 2;
        if (isUseDirectBuffer) {
            buffer = ByteBuffer.allocateDirect(capacity);
        } else {
            buffer = ByteBuffer.allocate(capacity);
        }
    }

    @Override
    public void write(int b) throws java.io.IOException {
        buffer.put(pos++, (byte) b);
        count++;
        if (count == chunkSize) {
            writeChunk();
        }
    }

    private void writeChunk() throws IOException {
        String hexString = Integer.toHexString(count);
        char[] chars = hexString.toCharArray();
        int p = 4 - chars.length;
        buffer.putInt(0, 0); // length clear
        for (char c : chars) {
            buffer.put(p++, (byte) c);
        }
        buffer.put(p++, (byte) '\r');
        buffer.put(p, (byte) '\n');
        buffer.put(pos++, (byte) '\r');
        buffer.put(pos++, (byte) '\n');
        buffer.position(pos);
        // buffer.limit(pos);
        buffer.flip();
        while (buffer.hasRemaining()) {
            super.write(buffer.get());
        }
        flush();
        buffer.clear();
        count = 0;
        pos = offset;
    }

    @Override
    public void close() throws IOException {
        if (count > 0) {
            writeChunk();
        }
        writeChunk();
        super.close();
        buffer.clear();
    }
}
