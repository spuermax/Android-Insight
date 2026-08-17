package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkComparison;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ComparisonReportWriter {
    private final ComparisonJsonReportRenderer json = new ComparisonJsonReportRenderer();
    private final ComparisonHtmlReportRenderer html = new ComparisonHtmlReportRenderer();

    public void write(ApkComparison comparison, String variantName, Path outputDir, boolean writeJson, boolean writeHtml) throws IOException {
        Files.createDirectories(outputDir);
        if (writeJson) {
            Files.writeString(outputDir.resolve("comparison.json"), json.render(comparison, variantName), StandardCharsets.UTF_8);
        }
        if (writeHtml) {
            Files.writeString(outputDir.resolve("comparison.html"), html.render(comparison, variantName), StandardCharsets.UTF_8);
        }
    }
}
