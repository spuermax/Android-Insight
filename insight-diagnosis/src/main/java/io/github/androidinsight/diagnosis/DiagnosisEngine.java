package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DiagnosisEngine {
    private final List<DiagnosisRule> rules;

    public DiagnosisEngine() {
        this(DiagnosisThresholds.defaults());
    }

    public DiagnosisEngine(DiagnosisThresholds thresholds) {
        this.rules = List.of(
                new DexDominanceRule(thresholds),
                new DexCountRule(thresholds),
                new DexDistributionRule(thresholds),
                new LargeDependencyFilesRule(thresholds),
                new CategoryShareRule(
                        ApkCategory.NATIVE,
                        IssueCategory.NATIVE,
                        "NATIVE_SHARE_HIGH",
                        "Native 库占比较高",
                        thresholds.nativeMediumShare(),
                        thresholds.nativeHighShare(),
                        List.of(
                                "检查 ABI 重复打包，并在产品允许时移除不支持的 ABI。",
                                "单独分析大型 .so 与 native symbols，并检查是否包含不需要的 ABI。")),
                new CategoryShareRule(
                        ApkCategory.RESOURCES,
                        IssueCategory.RESOURCES,
                        "RESOURCE_SHARE_HIGH",
                        "Resources 占比较高",
                        thresholds.resourcesMediumShare(),
                        thresholds.resourcesHighShare(),
                        List.of(
                                "检查图片格式、density 资源和 resource shrinking 配置。",
                                "检查重复资源和已经不再使用但仍被打包的资源。")),
                new CategoryShareRule(
                        ApkCategory.ASSETS,
                        IssueCategory.ASSETS,
                        "ASSET_SHARE_HIGH",
                        "Assets 占比较高",
                        thresholds.assetsMediumShare(),
                        thresholds.assetsHighShare(),
                        List.of(
                                "检查 assets 下的大型媒体、模型和离线数据文件。",
                                "在产品允许时考虑按需下发或压缩。"))
        );
    }

    public DiagnosisReport diagnose(ApkAnalysis analysis) {
        List<DiagnosisIssue> issues = new ArrayList<>();
        for (DiagnosisRule rule : rules) {
            rule.evaluate(analysis).ifPresent(issues::add);
        }
        issues.sort(Comparator
                .comparingInt((DiagnosisIssue issue) -> severityRank(issue.severity()))
                .thenComparing(DiagnosisIssue::id));

        int score = 100;
        int high = 0;
        int medium = 0;
        for (DiagnosisIssue issue : issues) {
            score -= issue.severity().scorePenalty();
            if (issue.severity() == Severity.HIGH) high++;
            if (issue.severity() == Severity.MEDIUM) medium++;
        }

        OptimizationPotential potential;
        if (high >= 2) {
            potential = OptimizationPotential.VERY_HIGH;
        } else if (high >= 1 || medium >= 2) {
            potential = OptimizationPotential.HIGH;
        } else if (medium >= 1 || !issues.isEmpty()) {
            potential = OptimizationPotential.MEDIUM;
        } else {
            potential = OptimizationPotential.LOW;
        }

        return new DiagnosisReport(score, potential, issues);
    }

    private static int severityRank(Severity severity) {
        return switch (severity) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }
}
