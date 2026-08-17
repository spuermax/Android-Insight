package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkEntryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LargeDependencyFilesRule implements DiagnosisRule {
    private final DiagnosisThresholds thresholds;
    public LargeDependencyFilesRule(DiagnosisThresholds thresholds) { this.thresholds = thresholds; }

    @Override
    public Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis) {
        List<ApkEntryInfo> candidates = analysis.topFiles().stream()
                .filter(entry -> entry.category() == ApkCategory.OTHER)
                .filter(entry -> looksLikeDependencyPath(entry.path()))
                .filter(entry -> entry.compressedBytes() >= thresholds.dependencyFileBytes())
                .toList();
        if (candidates.isEmpty()) return Optional.empty();

        long total = candidates.stream().mapToLong(ApkEntryInfo::compressedBytes).sum();
        List<String> evidence = new ArrayList<>();
        for (ApkEntryInfo entry : candidates.stream().limit(5).toList()) {
            evidence.add(entry.path() + ": " + DiagnosisMath.bytes(entry.compressedBytes()));
        }
        evidence.add("当前可见的大型依赖文件合计: " + DiagnosisMath.bytes(total));

        return Optional.of(new DiagnosisIssue(
                "LARGE_DEPENDENCY_FILES",
                total >= 512L * 1024L ? Severity.MEDIUM : Severity.LOW,
                IssueCategory.DEPENDENCY,
                "依赖中包含较大的非 DEX 文件",
                "APK 中存在位于 Java 风格包路径下的大型非 DEX 文件。ReDex 不会删除这类文件，因此依赖裁剪可能比 DEX 优化更有效。",
                evidence,
                List.of(
                        "确认这些文件所属的依赖功能是否真的被业务使用。",
                        "如果依赖提供更小的模块化 artifact，优先使用更精简的 artifact。",
                        "不要期待 ReDex 能缩减这些非 DEX 文件。")));
    }

    private static boolean looksLikeDependencyPath(String path) {
        return path.startsWith("org/") || path.startsWith("com/") || path.startsWith("io/")
                || path.startsWith("net/") || path.startsWith("kotlin/") || path.startsWith("META-INF/services/");
    }
}
