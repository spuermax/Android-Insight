package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;

import java.util.List;
import java.util.Optional;

public final class DexCountRule implements DiagnosisRule {
    private final DiagnosisThresholds thresholds;
    public DexCountRule(DiagnosisThresholds thresholds) { this.thresholds = thresholds; }

    @Override
    public Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis) {
        int count = analysis.dexCount();
        if (count < thresholds.dexMediumCount()) return Optional.empty();
        Severity severity = count >= thresholds.dexHighCount() ? Severity.HIGH : Severity.MEDIUM;
        return Optional.of(new DiagnosisIssue(
                "MULTIDEX_COUNT_HIGH",
                severity,
                IssueCategory.DEX_LAYOUT,
                "DEX 数量相对较多",
                "大型应用出现多个 DEX 很正常，但 DEX 数量较多通常值得继续检查代码体积和分包布局，不能仅把 DEX 个数本身当成性能问题。",
                List.of("DEX 数量: " + count),
                List.of(
                        "分析哪些依赖贡献了最多的类和方法引用。",
                        "结合启动性能和跨 DEX 引用情况一起判断布局是否需要调整。",
                        "使用 APK 前后对比验证依赖裁剪或 R8 配置调整的收益。")));
    }
}
