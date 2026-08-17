package io.github.androidinsight.diagnosis;

public record DiagnosisThresholds(
        double dexMediumShare,
        double dexHighShare,
        int dexMediumCount,
        int dexHighCount,
        long tinyDexAbsoluteBytes,
        double tinyDexRelativeToLargest,
        long dependencyFileBytes,
        double nativeMediumShare,
        double nativeHighShare,
        double resourcesMediumShare,
        double resourcesHighShare,
        double assetsMediumShare,
        double assetsHighShare) {

    public static DiagnosisThresholds defaults() {
        return new DiagnosisThresholds(
                0.60,
                0.80,
                5,
                10,
                64L * 1024L,
                0.05,
                100L * 1024L,
                0.40,
                0.60,
                0.30,
                0.50,
                0.20,
                0.40);
    }
}
