package io.github.androidinsight.diagnosis;

public enum Severity {
    HIGH(15),
    MEDIUM(8),
    LOW(3);

    private final int scorePenalty;

    Severity(int scorePenalty) {
        this.scorePenalty = scorePenalty;
    }

    public int scorePenalty() {
        return scorePenalty;
    }
}
