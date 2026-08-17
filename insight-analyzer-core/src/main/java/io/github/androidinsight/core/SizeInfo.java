package io.github.androidinsight.core;

public record SizeInfo(long compressedBytes, long uncompressedBytes) {
    public SizeInfo add(long compressed, long uncompressed) {
        return new SizeInfo(compressedBytes + compressed, uncompressedBytes + uncompressed);
    }
}
