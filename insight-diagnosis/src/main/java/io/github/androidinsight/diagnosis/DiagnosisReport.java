package io.github.androidinsight.diagnosis;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record DiagnosisReport(
        int healthScore,
        OptimizationPotential optimizationPotential,
        List<DiagnosisIssue> issues) {

    public DiagnosisReport {
        healthScore = Math.max(0, Math.min(100, healthScore));
        if (optimizationPotential == null) optimizationPotential = OptimizationPotential.LOW;
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public List<String> recommendations() {
        Set<String> unique = new LinkedHashSet<>();
        for (DiagnosisIssue issue : issues) {
            unique.addAll(issue.suggestions());
        }
        return List.copyOf(unique);
    }
}
