package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;

import java.util.List;
import java.util.Optional;

public final class CategoryShareRule implements DiagnosisRule {
    private final ApkCategory apkCategory;
    private final IssueCategory issueCategory;
    private final String id;
    private final String title;
    private final double medium;
    private final double high;
    private final List<String> suggestions;

    public CategoryShareRule(ApkCategory apkCategory, IssueCategory issueCategory, String id, String title,
                             double medium, double high, List<String> suggestions) {
        this.apkCategory = apkCategory;
        this.issueCategory = issueCategory;
        this.id = id;
        this.title = title;
        this.medium = medium;
        this.high = high;
        this.suggestions = List.copyOf(suggestions);
    }

    @Override
    public Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis) {
        long bytes = analysis.category(apkCategory).compressedBytes();
        double share = DiagnosisMath.share(bytes, analysis.apkBytes());
        if (share < medium) return Optional.empty();
        Severity severity = share >= high ? Severity.HIGH : Severity.MEDIUM;
        return Optional.of(new DiagnosisIssue(
                id, severity, issueCategory, title,
                apkCategory.displayName() + " 在 APK 中占比较高。",
                List.of(apkCategory.displayName() + " 占比: " + DiagnosisMath.percent(share),
                        apkCategory.displayName() + " 压缩后体积: " + DiagnosisMath.bytes(bytes)),
                suggestions));
    }
}
