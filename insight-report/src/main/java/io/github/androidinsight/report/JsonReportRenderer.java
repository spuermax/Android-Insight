package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkEntryInfo;
import io.github.androidinsight.core.DexInfo;
import io.github.androidinsight.diagnosis.DiagnosisIssue;
import io.github.androidinsight.diagnosis.DiagnosisReport;

public final class JsonReportRenderer {
    public String render(ApkAnalysis analysis, String variantName, DiagnosisReport diagnosis) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        field(json, "variant", variantName, true, 1);
        field(json, "apk", analysis.apkPath().getFileName().toString(), true, 1);
        number(json, "apkBytes", analysis.apkBytes(), true, 1);
        number(json, "entryCount", analysis.entryCount(), true, 1);
        number(json, "dexCount", analysis.dexCount(), true, 1);
        number(json, "classDefs", analysis.totalDexClassDefs(), true, 1);
        number(json, "methodIdEntries", analysis.totalDexMethodIdEntries(), true, 1);

        appendDiagnosis(json, diagnosis);
        json.append(",\n");

        indent(json, 1).append("\"categories\": {\n");
        ApkCategory[] categories = ApkCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ApkCategory category = categories[i];
            var size = analysis.category(category);
            indent(json, 2).append('"').append(category.name().toLowerCase()).append("\": {")
                    .append("\"compressedBytes\": ").append(size.compressedBytes()).append(", ")
                    .append("\"uncompressedBytes\": ").append(size.uncompressedBytes()).append("}")
                    .append(i + 1 < categories.length ? "," : "").append('\n');
        }
        indent(json, 1).append("},\n");

        indent(json, 1).append("\"dexFiles\": [\n");
        for (int i = 0; i < analysis.dexFiles().size(); i++) {
            DexInfo dex = analysis.dexFiles().get(i);
            indent(json, 2).append('{')
                    .append("\"name\": \"").append(escape(dex.name())).append("\", ")
                    .append("\"compressedBytes\": ").append(dex.compressedBytes()).append(", ")
                    .append("\"uncompressedBytes\": ").append(dex.uncompressedBytes()).append(", ")
                    .append("\"stringIds\": ").append(dex.stringIds()).append(", ")
                    .append("\"typeIds\": ").append(dex.typeIds()).append(", ")
                    .append("\"protoIds\": ").append(dex.protoIds()).append(", ")
                    .append("\"fieldIds\": ").append(dex.fieldIds()).append(", ")
                    .append("\"methodIdEntries\": ").append(dex.methodIds()).append(", ")
                    .append("\"classDefs\": ").append(dex.classDefs()).append('}')
                    .append(i + 1 < analysis.dexFiles().size() ? "," : "").append('\n');
        }
        indent(json, 1).append("],\n");

        indent(json, 1).append("\"largestFiles\": [\n");
        for (int i = 0; i < analysis.topFiles().size(); i++) {
            ApkEntryInfo entry = analysis.topFiles().get(i);
            indent(json, 2).append('{')
                    .append("\"path\": \"").append(escape(entry.path())).append("\", ")
                    .append("\"category\": \"").append(entry.category().name()).append("\", ")
                    .append("\"compressedBytes\": ").append(entry.compressedBytes()).append(", ")
                    .append("\"uncompressedBytes\": ").append(entry.uncompressedBytes()).append('}')
                    .append(i + 1 < analysis.topFiles().size() ? "," : "").append('\n');
        }
        indent(json, 1).append("]\n");
        json.append("}\n");
        return json.toString();
    }

    private static void appendDiagnosis(StringBuilder json, DiagnosisReport diagnosis) {
        indent(json, 1).append("\"diagnosis\": {\n");
        number(json, "healthScore", diagnosis.healthScore(), true, 2);
        field(json, "optimizationPotential", diagnosis.optimizationPotential().name(), true, 2);
        indent(json, 2).append("\"issues\": [\n");
        for (int i = 0; i < diagnosis.issues().size(); i++) {
            DiagnosisIssue issue = diagnosis.issues().get(i);
            indent(json, 3).append("{\n");
            field(json, "id", issue.id(), true, 4);
            field(json, "severity", issue.severity().name(), true, 4);
            field(json, "category", issue.category().name(), true, 4);
            field(json, "title", issue.title(), true, 4);
            field(json, "message", issue.message(), true, 4);
            stringArray(json, "evidence", issue.evidence(), true, 4);
            stringArray(json, "suggestions", issue.suggestions(), false, 4);
            indent(json, 3).append('}').append(i + 1 < diagnosis.issues().size() ? "," : "").append('\n');
        }
        indent(json, 2).append("]\n");
        indent(json, 1).append('}');
    }

    private static void stringArray(StringBuilder out, String name, java.util.List<String> values, boolean comma, int level) {
        indent(out, level).append('"').append(name).append("\": [");
        for (int i = 0; i < values.size(); i++) {
            out.append('"').append(escape(values.get(i))).append('"');
            if (i + 1 < values.size()) out.append(", ");
        }
        out.append(']');
        if (comma) out.append(',');
        out.append('\n');
    }

    private static void field(StringBuilder out, String name, String value, boolean comma, int level) {
        indent(out, level).append('"').append(name).append("\": \"").append(escape(value)).append('"');
        if (comma) out.append(',');
        out.append('\n');
    }

    private static void number(StringBuilder out, String name, long value, boolean comma, int level) {
        indent(out, level).append('"').append(name).append("\": ").append(value);
        if (comma) out.append(',');
        out.append('\n');
    }

    private static StringBuilder indent(StringBuilder out, int level) {
        return out.append("  ".repeat(Math.max(0, level)));
    }

    private static String escape(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }
}
