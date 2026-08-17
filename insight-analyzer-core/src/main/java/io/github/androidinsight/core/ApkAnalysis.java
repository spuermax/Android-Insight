package io.github.androidinsight.core;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ApkAnalysis {
    private final Path apkPath;
    private final long apkBytes;
    private final int entryCount;
    private final Map<ApkCategory, SizeInfo> categories;
    private final List<DexInfo> dexFiles;
    private final List<ApkEntryInfo> topFiles;

    public ApkAnalysis(
            Path apkPath,
            long apkBytes,
            int entryCount,
            Map<ApkCategory, SizeInfo> categories,
            List<DexInfo> dexFiles,
            List<ApkEntryInfo> topFiles) {
        this.apkPath = apkPath;
        this.apkBytes = apkBytes;
        this.entryCount = entryCount;
        this.categories = Collections.unmodifiableMap(new EnumMap<>(categories));
        this.dexFiles = List.copyOf(dexFiles);
        this.topFiles = List.copyOf(topFiles);
    }

    public Path apkPath() { return apkPath; }
    public long apkBytes() { return apkBytes; }
    public int entryCount() { return entryCount; }
    public Map<ApkCategory, SizeInfo> categories() { return categories; }
    public List<DexInfo> dexFiles() { return dexFiles; }
    public List<ApkEntryInfo> topFiles() { return topFiles; }

    public SizeInfo category(ApkCategory category) {
        return categories.getOrDefault(category, new SizeInfo(0, 0));
    }

    public int dexCount() {
        return dexFiles.size();
    }

    /**
     * Sum of method_id table entries across all DEX files. This is not the number
     * of unique methods defined by the application; the same referenced method
     * may appear in more than one DEX method_id table.
     */
    public long totalDexMethodIdEntries() {
        return dexFiles.stream().mapToLong(DexInfo::methodIds).sum();
    }

    /** @deprecated Use {@link #totalDexMethodIdEntries()} for precise terminology. */
    @Deprecated
    public long totalDexMethodIds() {
        return totalDexMethodIdEntries();
    }

    public long totalDexClassDefs() {
        return dexFiles.stream().mapToLong(DexInfo::classDefs).sum();
    }
}
