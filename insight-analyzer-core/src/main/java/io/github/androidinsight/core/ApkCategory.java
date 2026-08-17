package io.github.androidinsight.core;

public enum ApkCategory {
    DEX("DEX"),
    NATIVE("Native"),
    RESOURCES("Resources"),
    ASSETS("Assets"),
    META_INF("META-INF"),
    OTHER("Other");

    private final String displayName;

    ApkCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
