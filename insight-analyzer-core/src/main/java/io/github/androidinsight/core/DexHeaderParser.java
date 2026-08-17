package io.github.androidinsight.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class DexHeaderParser {
    private static final int HEADER_BYTES_NEEDED = 0x70;

    private DexHeaderParser() {}

    static Counts parse(InputStream input) throws IOException {
        byte[] header = input.readNBytes(HEADER_BYTES_NEEDED);
        if (header.length < HEADER_BYTES_NEEDED || !isDex(header)) {
            return Counts.EMPTY;
        }
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        return new Counts(
                buffer.getInt(0x38),
                buffer.getInt(0x40),
                buffer.getInt(0x48),
                buffer.getInt(0x50),
                buffer.getInt(0x58),
                buffer.getInt(0x60));
    }

    private static boolean isDex(byte[] header) {
        return header[0] == 'd' && header[1] == 'e' && header[2] == 'x' && header[3] == '\n';
    }

    record Counts(int stringIds, int typeIds, int protoIds, int fieldIds, int methodIds, int classDefs) {
        static final Counts EMPTY = new Counts(0, 0, 0, 0, 0, 0);
    }
}
