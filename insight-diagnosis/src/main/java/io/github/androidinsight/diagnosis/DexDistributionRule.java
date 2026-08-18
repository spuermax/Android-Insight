package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.DexInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DexDistributionRule implements DiagnosisRule {
    private final DiagnosisThresholds thresholds;
    public DexDistributionRule(DiagnosisThresholds thresholds) { this.thresholds = thresholds; }

    @Override
    public Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis) {
        if (analysis.dexFiles().size() < 2) return Optional.empty();
        long largest = analysis.dexFiles().stream().mapToLong(DexInfo::compressedBytes).max().orElse(0L);
        if (largest <= 0) return Optional.empty();
        long relativeLimit = Math.max(1L, Math.round(largest * thresholds.tinyDexRelativeToLargest()));
        long tinyLimit = Math.min(thresholds.tinyDexAbsoluteBytes(), relativeLimit);
        List<DexInfo> tiny = analysis.dexFiles().stream()
                .filter(dex -> dex.compressedBytes() > 0 && dex.compressedBytes() <= tinyLimit)
                .sorted(Comparator.comparingLong(DexInfo::compressedBytes))
                .toList();
        if (tiny.isEmpty()) return Optional.empty();

        List<String> evidence = new ArrayList<>();
        evidence.add("最大 DEX: " + DiagnosisMath.bytes(largest));
        for (DexInfo dex : tiny.stream().limit(5).toList()) {
            evidence.add(dex.name() + ": " + DiagnosisMath.bytes(dex.compressedBytes()) + " (classes=" + dex.classDefs() + ")");
        }
        return Optional.of(new DiagnosisIssue(
                "DEX_DISTRIBUTION_IMBALANCE",
                Severity.MEDIUM,
                IssueCategory.DEX_LAYOUT,
                "DEX 分布不均衡",
                "存在一个或多个明显小于主 DEX 的极小 DEX，说明当前分包布局值得进一步检查；但 DEX 数量更少并不自动等于启动更快。",
                evidence,
                List.of(
                        "先确认为什么当前 variant 会产生极小 DEX。",
                        "对调整前后的 APK 使用 compare task 做定量对比。",
                        "任何 DEX 重排后都要验证启动行为和运行时正确性。")));
    }
}
