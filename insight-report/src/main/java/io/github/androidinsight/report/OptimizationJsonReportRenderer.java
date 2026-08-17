package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.optimizer.redex.ValidationResult;

public final class OptimizationJsonReportRenderer {
    public String render(OptimizationReport report) {
        ApkAnalysis before = report.original();
        ApkAnalysis after = report.optimized();
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        field(out, "variant", report.variantName(), true);
        field(out, "strategy", report.strategy().id(), true);
        field(out, "strategyName", report.strategy().displayName(), true);
        out.append("  \"redexDurationMillis\": ").append(report.redexRun().durationMillis()).append(",\n");
        out.append("  \"originalPreserved\": ").append(report.originalPreserved()).append(",\n");
        out.append("  \"original\": "); apk(out, before); out.append(",\n");
        out.append("  \"optimized\": "); apk(out, after); out.append(",\n");
        out.append("  \"deltaBytes\": ").append(after.apkBytes() - before.apkBytes()).append(",\n");
        out.append("  \"changePercent\": ").append(ComparisonMath.percentChange(before.apkBytes(), after.apkBytes())).append(",\n");
        out.append("  \"validation\": {\n");
        validation(out, "zipAlignment", report.zipAlignment(), true);
        validation(out, "signature", report.signature(), false);
        out.append("  },\n");
        field(out, "optimizedApk", after.apkPath().toString(), true);
        field(out, "redexLog", report.redexRun().logFile().toString(), false);
        out.append("}\n");
        return out.toString();
    }

    private static void apk(StringBuilder out, ApkAnalysis apk) {
        out.append("{\"name\":\"").append(escape(apk.apkPath().getFileName().toString())).append("\",\"apkBytes\":").append(apk.apkBytes())
                .append(",\"dexCount\":").append(apk.dexCount())
                .append(",\"classDefs\":").append(apk.totalDexClassDefs())
                .append(",\"methodIdEntries\":").append(apk.totalDexMethodIdEntries()).append('}');
    }

    private static void validation(StringBuilder out, String name, ValidationResult result, boolean comma) {
        out.append("    \"").append(name).append("\": {\"attempted\":").append(result.attempted())
                .append(",\"success\":").append(result.success())
                .append(",\"message\":\"").append(escape(result.message())).append("\"}");
        if (comma) out.append(',');
        out.append('\n');
    }

    private static void field(StringBuilder out, String name, String value, boolean comma) {
        out.append("  \"").append(name).append("\": \"").append(escape(value)).append('"');
        if (comma) out.append(',');
        out.append('\n');
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
