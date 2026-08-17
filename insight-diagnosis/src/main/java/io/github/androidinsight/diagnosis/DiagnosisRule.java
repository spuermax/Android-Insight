package io.github.androidinsight.diagnosis;

import io.github.androidinsight.core.ApkAnalysis;

import java.util.Optional;

public interface DiagnosisRule {
    Optional<DiagnosisIssue> evaluate(ApkAnalysis analysis);
}
