package io.github.androidinsight.report;

import java.util.Locale;

final class ComparisonMath {
    private ComparisonMath() {}

    static long delta(long baseline, long current) {
        return current - baseline;
    }

    static String signedHuman(long deltaBytes) {
        if (deltaBytes == 0) return "0 B";
        return (deltaBytes > 0 ? "+" : "-") + SizeFormatter.human(Math.abs(deltaBytes));
    }

    static double percentChange(long baseline, long current) {
        if (baseline <= 0) return current == 0 ? 0.0 : Double.NaN;
        return (current - baseline) * 100.0 / baseline;
    }

    static String signedPercent(long baseline, long current) {
        if (baseline <= 0) return current == 0 ? "0.00%" : "n/a";
        double percent = (current - baseline) * 100.0 / baseline;
        return String.format(Locale.ROOT, "%+.2f%%", percent);
    }
}
