package io.github.androidinsight.report;

import io.github.androidinsight.diagnosis.OptimizationPotential;
import io.github.androidinsight.diagnosis.Severity;

final class ReportText {
    private ReportText() {}

    static String potential(OptimizationPotential value) {
        return switch (value) {
            case LOW -> "低 (LOW)";
            case MEDIUM -> "中 (MEDIUM)";
            case HIGH -> "高 (HIGH)";
            case VERY_HIGH -> "很高 (VERY_HIGH)";
        };
    }

    static String severity(Severity value) {
        return switch (value) {
            case LOW -> "低";
            case MEDIUM -> "中";
            case HIGH -> "高";
        };
    }
}
