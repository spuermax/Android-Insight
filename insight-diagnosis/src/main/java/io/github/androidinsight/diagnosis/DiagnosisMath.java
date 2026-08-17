package io.github.androidinsight.diagnosis;

final class DiagnosisMath {
    private DiagnosisMath() {}

    static double share(long bytes, long total) {
        if (bytes <= 0 || total <= 0) return 0.0;
        return (double) bytes / (double) total;
    }

    static String percent(double share) {
        return String.format("%.2f%%", share * 100.0);
    }

    static String bytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.2f KB", kb);
        return String.format("%.2f MB", kb / 1024.0);
    }
}
