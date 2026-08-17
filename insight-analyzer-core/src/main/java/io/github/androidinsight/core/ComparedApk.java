package io.github.androidinsight.core;

public record ComparedApk(String label, ApkAnalysis analysis) {
    public ComparedApk {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Comparison label must not be blank");
        }
        if (analysis == null) {
            throw new IllegalArgumentException("Comparison analysis must not be null");
        }
    }
}
