package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.diagnosis.DiagnosisReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReportWriter {
    private final JsonReportRenderer json = new JsonReportRenderer();
    private final HtmlReportRenderer html = new HtmlReportRenderer();

    public void write(
            ApkAnalysis analysis,
            DiagnosisReport diagnosis,
            String variantName,
            Path outputDir,
            boolean writeJson,
            boolean writeHtml) throws IOException {
        Files.createDirectories(outputDir);
        String baseName = stripApk(analysis.apkPath().getFileName().toString());
        if (writeJson) {
            Files.writeString(outputDir.resolve(baseName + ".json"), json.render(analysis, variantName, diagnosis), StandardCharsets.UTF_8);
        }
        if (writeHtml) {
            Files.writeString(outputDir.resolve(baseName + ".html"), html.render(analysis, variantName, diagnosis), StandardCharsets.UTF_8);
        }
    }

    private static String stripApk(String name) {
        return name.toLowerCase().endsWith(".apk") ? name.substring(0, name.length() - 4) : name;
    }
}
