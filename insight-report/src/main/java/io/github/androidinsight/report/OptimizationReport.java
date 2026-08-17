package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.diagnosis.DiagnosisReport;
import io.github.androidinsight.optimizer.redex.RedexRunResult;
import io.github.androidinsight.optimizer.redex.RedexStrategy;
import io.github.androidinsight.optimizer.redex.ValidationResult;

public record OptimizationReport(
        String variantName,
        RedexStrategy strategy,
        ApkAnalysis original,
        DiagnosisReport originalDiagnosis,
        ApkAnalysis optimized,
        DiagnosisReport optimizedDiagnosis,
        RedexRunResult redexRun,
        ValidationResult zipAlignment,
        ValidationResult signature,
        boolean originalPreserved) {
}
