package io.github.androidinsight.core;

import java.util.List;

public record ApkComparison(String baselineLabel, ApkAnalysis baseline, List<ComparedApk> candidates) {
    public ApkComparison {
        if (baselineLabel == null || baselineLabel.isBlank()) {
            throw new IllegalArgumentException("Baseline label must not be blank");
        }
        if (baseline == null) {
            throw new IllegalArgumentException("Baseline analysis must not be null");
        }
        candidates = List.copyOf(candidates);
    }
}
