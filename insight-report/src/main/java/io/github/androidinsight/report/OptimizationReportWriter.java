package io.github.androidinsight.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OptimizationReportWriter {
    public void write(OptimizationReport report, Path outputDir, boolean json, boolean html) throws IOException {
        Files.createDirectories(outputDir);
        if (json) Files.writeString(outputDir.resolve("optimization.json"), new OptimizationJsonReportRenderer().render(report), StandardCharsets.UTF_8);
        if (html) Files.writeString(outputDir.resolve("optimization.html"), new OptimizationHtmlReportRenderer().render(report), StandardCharsets.UTF_8);
    }
}
