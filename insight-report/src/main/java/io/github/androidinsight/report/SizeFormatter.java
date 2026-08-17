package io.github.androidinsight.report;

import java.util.Locale;

final class SizeFormatter {
    private SizeFormatter() {}

    static String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.ROOT, "%.2f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.ROOT, "%.2f MB", mb);
        return String.format(Locale.ROOT, "%.2f GB", mb / 1024.0);
    }

    static String percent(long part, long total) {
        if (total <= 0) return "0.00%";
        return String.format(Locale.ROOT, "%.2f%%", part * 100.0 / total);
    }
}
