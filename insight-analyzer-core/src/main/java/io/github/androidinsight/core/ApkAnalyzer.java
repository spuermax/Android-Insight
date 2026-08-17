package io.github.androidinsight.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ApkAnalyzer {
    private static final Pattern DEX_NAME = Pattern.compile("classes(?:\\d+)?\\.dex");

    public ApkAnalysis analyze(Path apkPath, int topFileCount) throws IOException {
        if (apkPath == null || !Files.isRegularFile(apkPath)) {
            throw new IOException("APK does not exist: " + apkPath);
        }

        Map<ApkCategory, SizeInfo> categories = new EnumMap<>(ApkCategory.class);
        for (ApkCategory category : ApkCategory.values()) {
            categories.put(category, new SizeInfo(0, 0));
        }

        List<DexInfo> dexFiles = new ArrayList<>();
        List<ApkEntryInfo> entries = new ArrayList<>();
        int entryCount = 0;

        try (ZipFile zip = new ZipFile(apkPath.toFile())) {
            var enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                entryCount++;
                String name = entry.getName();
                long compressed = nonNegative(entry.getCompressedSize());
                long uncompressed = nonNegative(entry.getSize());
                ApkCategory category = categoryOf(name);

                categories.compute(category, (key, current) ->
                        (current == null ? new SizeInfo(0, 0) : current).add(compressed, uncompressed));

                entries.add(new ApkEntryInfo(name, category, compressed, uncompressed));

                if (isDex(name)) {
                    DexHeaderParser.Counts counts;
                    try (InputStream input = zip.getInputStream(entry)) {
                        counts = DexHeaderParser.parse(input);
                    }
                    dexFiles.add(new DexInfo(
                            name,
                            compressed,
                            uncompressed,
                            counts.stringIds(),
                            counts.typeIds(),
                            counts.protoIds(),
                            counts.fieldIds(),
                            counts.methodIds(),
                            counts.classDefs()));
                }
            }
        }

        dexFiles.sort(Comparator.comparingInt(dex -> dexIndex(dex.name())));
        entries.sort(Comparator.comparingLong(ApkEntryInfo::compressedBytes).reversed());
        int limit = Math.max(0, Math.min(topFileCount, entries.size()));

        return new ApkAnalysis(
                apkPath.toAbsolutePath(),
                Files.size(apkPath),
                entryCount,
                categories,
                dexFiles,
                entries.subList(0, limit));
    }

    public ApkAnalysis analyze(Path apkPath) throws IOException {
        return analyze(apkPath, 20);
    }

    private static boolean isDex(String path) {
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        return DEX_NAME.matcher(fileName).matches();
    }

    private static int dexIndex(String name) {
        if ("classes.dex".equals(name)) {
            return 1;
        }
        int start = "classes".length();
        int end = name.length() - ".dex".length();
        try {
            return Integer.parseInt(name.substring(start, end));
        } catch (RuntimeException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private static ApkCategory categoryOf(String path) {
        if (isDex(path)) {
            return ApkCategory.DEX;
        }
        if (path.startsWith("lib/")) {
            return ApkCategory.NATIVE;
        }
        if (path.startsWith("res/") || path.equals("resources.arsc")) {
            return ApkCategory.RESOURCES;
        }
        if (path.startsWith("assets/")) {
            return ApkCategory.ASSETS;
        }
        if (path.startsWith("META-INF/")) {
            return ApkCategory.META_INF;
        }
        return ApkCategory.OTHER;
    }

    private static long nonNegative(long value) {
        return Math.max(0L, value);
    }
}
