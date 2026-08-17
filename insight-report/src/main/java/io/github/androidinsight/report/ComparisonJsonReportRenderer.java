package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkComparison;
import io.github.androidinsight.core.ComparedApk;

public final class ComparisonJsonReportRenderer {
    public String render(ApkComparison comparison, String variantName) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        string(json, "variant", variantName, true, 1);
        string(json, "baselineLabel", comparison.baselineLabel(), true, 1);
        json.append("  \"baseline\": ");
        appendApk(json, comparison.baseline(), null, comparison.baseline());
        json.append(",\n");
        json.append("  \"candidates\": [\n");
        for (int i = 0; i < comparison.candidates().size(); i++) {
            ComparedApk item = comparison.candidates().get(i);
            json.append("    {");
            json.append("\"label\": \"").append(escape(item.label())).append("\", \"apk\": ");
            appendApk(json, item.analysis(), comparison.baseline(), item.analysis());
            json.append("}");
            if (i + 1 < comparison.candidates().size()) json.append(',');
            json.append('\n');
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private static void appendApk(StringBuilder json, ApkAnalysis analysis, ApkAnalysis baseline, ApkAnalysis current) {
        json.append('{');
        json.append("\"name\": \"").append(escape(analysis.apkPath().getFileName().toString())).append("\", ");
        json.append("\"apkBytes\": ").append(analysis.apkBytes()).append(", ");
        json.append("\"dexCount\": ").append(analysis.dexCount()).append(", ");
        json.append("\"classDefs\": ").append(analysis.totalDexClassDefs()).append(", ");
        json.append("\"methodIdEntries\": ").append(analysis.totalDexMethodIdEntries());
        if (baseline != null) {
            json.append(", \"deltaBytes\": ").append(current.apkBytes() - baseline.apkBytes());
            json.append(", \"changePercent\": ")
                    .append(baseline.apkBytes() <= 0 ? "null" : Double.toString((current.apkBytes() - baseline.apkBytes()) * 100.0 / baseline.apkBytes()));
        }
        json.append(", \"categories\": {");
        ApkCategory[] categories = ApkCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ApkCategory category = categories[i];
            long bytes = analysis.category(category).compressedBytes();
            json.append('\"').append(category.name().toLowerCase()).append("\": ").append(bytes);
            if (i + 1 < categories.length) json.append(", ");
        }
        json.append("}}");
    }

    private static void string(StringBuilder out, String name, String value, boolean comma, int level) {
        out.append("  ".repeat(Math.max(0, level))).append('\"').append(name).append("\": \"")
                .append(escape(value)).append('\"');
        if (comma) out.append(',');
        out.append('\n');
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
