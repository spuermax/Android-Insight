package io.github.androidinsight.core;

public record DexInfo(
        String name,
        long compressedBytes,
        long uncompressedBytes,
        int stringIds,
        int typeIds,
        int protoIds,
        int fieldIds,
        int methodIds,
        int classDefs) {
}
