package io.github.androidinsight.core;

public record ApkEntryInfo(
        String path,
        ApkCategory category,
        long compressedBytes,
        long uncompressedBytes) {
}
