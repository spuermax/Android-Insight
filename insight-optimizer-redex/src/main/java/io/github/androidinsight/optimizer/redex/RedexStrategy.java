package io.github.androidinsight.optimizer.redex;

import java.util.Locale;

public enum RedexStrategy {
    STRIP_DEBUG_INFO("strip", "StripDebugInfoPass"),
    STRIP_AND_INTERDEX("strip-interdex", "StripDebugInfoPass + InterDexPass");

    private final String id;
    private final String displayName;

    RedexStrategy(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static RedexStrategy parse(String value) {
        if (value == null || value.isBlank()) return STRIP_AND_INTERDEX;
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        for (RedexStrategy strategy : values()) {
            if (strategy.id.equals(normalized)) return strategy;
        }
        return switch (normalized) {
            case "stripdebuginfo", "strip-debug-info" -> STRIP_DEBUG_INFO;
            case "combo", "strip+interdex", "strip-and-interdex" -> STRIP_AND_INTERDEX;
            default -> throw new IllegalArgumentException(
                    "Unsupported ReDex strategy '" + value + "'. Supported: strip, strip-interdex");
        };
    }
}
