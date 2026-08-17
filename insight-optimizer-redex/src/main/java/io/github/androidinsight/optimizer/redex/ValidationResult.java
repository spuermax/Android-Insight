package io.github.androidinsight.optimizer.redex;

public record ValidationResult(boolean attempted, boolean success, String message) {
    public static ValidationResult skipped(String message) {
        return new ValidationResult(false, false, message);
    }
}
