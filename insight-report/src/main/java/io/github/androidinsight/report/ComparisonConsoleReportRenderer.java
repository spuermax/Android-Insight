package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkComparison;
import io.github.androidinsight.core.ComparedApk;

public final class ComparisonConsoleReportRenderer {
    public String render(ApkComparison comparison, String variantName) {
        ApkAnalysis baseline = comparison.baseline();
        StringBuilder out = new StringBuilder();
        out.append(System.lineSeparator());
        out.append("================ Android Insight · APK 对比 ================").append(System.lineSeparator());
        out.append("Variant        : ").append(variantName).append(System.lineSeparator());
        out.append("基线 APK       : ").append(comparison.baselineLabel()).append(" (").append(baseline.apkPath().getFileName()).append(")").append(System.lineSeparator());
        out.append(String.format("%-14s %12s %12s %11s %8s %11s %18s%n", "APK", "大小", "变化量", "变化率", "DEX", "Classes", "Method ID Entries"));
        out.append(String.format("%-14s %12s %12s %11s %8d %11d %18d%n", comparison.baselineLabel(), SizeFormatter.human(baseline.apkBytes()), "-", "-", baseline.dexCount(), baseline.totalDexClassDefs(), baseline.totalDexMethodIdEntries()));
        for (ComparedApk item : comparison.candidates()) {
            ApkAnalysis candidate = item.analysis();
            long delta = ComparisonMath.delta(baseline.apkBytes(), candidate.apkBytes());
            out.append(String.format("%-14s %12s %12s %11s %8d %11d %18d%n", item.label(), SizeFormatter.human(candidate.apkBytes()), ComparisonMath.signedHuman(delta), ComparisonMath.signedPercent(baseline.apkBytes(), candidate.apkBytes()), candidate.dexCount(), candidate.totalDexClassDefs(), candidate.totalDexMethodIdEntries()));
        }
        out.append(System.lineSeparator()).append("分类体积变化（APK 内压缩后）").append(System.lineSeparator());
        for (ComparedApk item : comparison.candidates()) {
            out.append("  ").append(item.label()).append(System.lineSeparator());
            for (ApkCategory category : ApkCategory.values()) {
                long before = baseline.category(category).compressedBytes();
                long after = item.analysis().category(category).compressedBytes();
                out.append(String.format("    %-12s %10s -> %10s  %10s  %9s%n", category.displayName(), SizeFormatter.human(before), SizeFormatter.human(after), ComparisonMath.signedHuman(after - before), ComparisonMath.signedPercent(before, after)));
            }
        }
        out.append("===========================================================").append(System.lineSeparator());
        return out.toString();
    }
}
