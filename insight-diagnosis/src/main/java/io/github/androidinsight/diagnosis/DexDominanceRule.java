package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;

import java.util.List;
import java.util.Optional;

public final class DexDominanceRule implements DiagnosisRule {
    private final DiagnosisThresholds thresholds;

    public DexDominanceRule(DiagnosisThresholds thresholds) { this.thresholds = thresholds; }

    @Override
    public Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis) {
        long dexBytes = analysis.category(ApkCategory.DEX).compressedBytes();
        double share = DiagnosisMath.share(dexBytes, analysis.apkBytes());
        if (share < thresholds.dexMediumShare()) return Optional.empty();
        Severity severity = share >= thresholds.dexHighShare() ? Severity.HIGH : Severity.MEDIUM;
        return Optional.of(new DiagnosisIssue(
                "DEX_DOMINATES_APK",
                severity,
                IssueCategory.DEX,
                "DEX 占 APK 体积过高",
                "APK 的主要体积来自 DEX 字节码，代码压缩、调试信息剥离和 DEX 级优化可能带来较明显收益。",
                List.of(
                        "DEX 占比: " + DiagnosisMath.percent(share),
                        "DEX 压缩后体积: " + DiagnosisMath.bytes(dexBytes),
                        "APK 体积: " + DiagnosisMath.bytes(analysis.apkBytes())),
                List.of(
                        "Release 构建优先确认 R8 / code shrinking 是否正确开启。",
                        "在使用 StripDebugInfoPass 前先测量可移除的调试元数据。",
                        "ReDex 会修改最终字节码，优化后必须进行安装、启动和核心功能回归测试。")));
    }
}
