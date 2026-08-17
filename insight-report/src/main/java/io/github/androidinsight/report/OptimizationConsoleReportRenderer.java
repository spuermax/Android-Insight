package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;

public final class OptimizationConsoleReportRenderer {
    public String render(OptimizationReport report) {
        ApkAnalysis before = report.original();
        ApkAnalysis after = report.optimized();
        long delta = after.apkBytes() - before.apkBytes();
        StringBuilder out = new StringBuilder();
        out.append(System.lineSeparator());
        out.append("================ Android Insight · ReDex 优化 ================").append(System.lineSeparator());
        out.append("Variant        : ").append(report.variantName()).append(System.lineSeparator());
        out.append("优化策略       : ").append(report.strategy().displayName()).append(System.lineSeparator());
        out.append("原始 APK       : ").append(before.apkPath().getFileName()).append(System.lineSeparator());
        out.append("优化 APK       : ").append(after.apkPath().getFileName()).append(System.lineSeparator());
        out.append("ReDex 耗时     : ").append(String.format("%.2f s", report.redexRun().durationMillis() / 1000.0)).append(System.lineSeparator());
        out.append(System.lineSeparator());
        out.append(String.format("%-18s %-14s %-14s %-14s%n", "指标", "优化前", "优化后", "变化"));
        out.append(String.format("%-18s %-14s %-14s %-14s%n", "APK 大小", SizeFormatter.human(before.apkBytes()), SizeFormatter.human(after.apkBytes()), ComparisonMath.signedPercent(before.apkBytes(), after.apkBytes())));
        out.append(String.format("%-18s %-14d %-14d %-14d%n", "DEX 数量", before.dexCount(), after.dexCount(), after.dexCount() - before.dexCount()));
        out.append(String.format("%-18s %-14d %-14d %-14d%n", "Class Defs", before.totalDexClassDefs(), after.totalDexClassDefs(), after.totalDexClassDefs() - before.totalDexClassDefs()));
        out.append(String.format("%-18s %-14d %-14d %-14d%n", "DEX Method ID Entries", before.totalDexMethodIdEntries(), after.totalDexMethodIdEntries(), after.totalDexMethodIdEntries() - before.totalDexMethodIdEntries()));
        out.append(String.format("%-18s %-14s %-14s %-14s%n", "健康分", report.originalDiagnosis().healthScore(), report.optimizedDiagnosis().healthScore(), signed(report.optimizedDiagnosis().healthScore() - report.originalDiagnosis().healthScore())));
        out.append(System.lineSeparator()).append("验证").append(System.lineSeparator());
        out.append("  原始 APK 未被覆盖 : ").append(report.originalPreserved() ? "通过" : "失败").append(System.lineSeparator());
        out.append("  ZIP 对齐          : ").append(status(report.zipAlignment())).append(System.lineSeparator());
        out.append("  APK 签名          : ").append(status(report.signature())).append(System.lineSeparator());
        out.append("  ReDex 日志         : ").append(report.redexRun().logFile()).append(System.lineSeparator());
        out.append("  优化收益           : ").append(ComparisonMath.signedHuman(delta)).append(" / ")
                .append(ComparisonMath.signedPercent(before.apkBytes(), after.apkBytes())).append(System.lineSeparator());
        out.append("===============================================================").append(System.lineSeparator());
        return out.toString();
    }

    private static String status(io.github.androidinsight.optimizer.redex.ValidationResult result) {
        if (!result.attempted()) return "跳过 - " + result.message();
        return (result.success() ? "通过 - " : "失败 - ") + result.message();
    }

    private static String signed(long value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }
}
