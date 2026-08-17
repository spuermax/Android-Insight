package io.github.androidinsight.gradle;

import com.android.build.api.variant.BuiltArtifacts;
import com.android.build.api.variant.BuiltArtifactsLoader;
import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkAnalyzer;
import io.github.androidinsight.core.ApkComparison;
import io.github.androidinsight.core.ComparedApk;
import io.github.androidinsight.report.ComparisonConsoleReportRenderer;
import io.github.androidinsight.report.ComparisonReportWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CompareApkTask extends DefaultTask {
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getApkFolder();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    @Internal
    public abstract Property<BuiltArtifactsLoader> getBuiltArtifactsLoader();

    @Input
    public abstract Property<String> getVariantName();

    @Input
    public abstract Property<Integer> getTopFiles();

    @Input
    public abstract Property<Boolean> getJsonReport();

    @Input
    public abstract Property<Boolean> getHtmlReport();

    @Input
    public abstract MapProperty<String, String> getComparisonApks();

    @TaskAction
    public void compare() throws Exception {
        BuiltArtifacts artifacts = getBuiltArtifactsLoader().get().load(getApkFolder().get());
        if (artifacts == null || artifacts.getElements().isEmpty()) {
            throw new IllegalStateException("Android Insight could not find APK artifacts for " + getVariantName().get());
        }
        if (artifacts.getElements().size() != 1) {
            throw new IllegalStateException("V0.2 compare expects one baseline APK for variant " + getVariantName().get()
                    + ", but found " + artifacts.getElements().size() + ". Split APK support will be added later.");
        }

        ApkAnalyzer analyzer = new ApkAnalyzer();
        File baselineFile = new File(artifacts.getElements().iterator().next().getOutputFile());
        ApkAnalysis baseline = analyzer.analyze(baselineFile.toPath(), getTopFiles().get());

        List<Map.Entry<String, String>> configured = new ArrayList<>(getComparisonApks().get().entrySet());
        List<ComparedApk> candidates = new ArrayList<>();
        for (Map.Entry<String, String> entry : configured) {
            Path path = Path.of(entry.getValue()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Comparison APK '" + entry.getKey() + "' does not exist: " + path);
            }
            candidates.add(new ComparedApk(entry.getKey(), analyzer.analyze(path, getTopFiles().get())));
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No comparison APKs configured. Add androidInsight { comparisonApks.put(\"name\", \"/path/app.apk\") }");
        }

        ApkComparison comparison = new ApkComparison("original", baseline, candidates);
        getLogger().lifecycle(new ComparisonConsoleReportRenderer().render(comparison, getVariantName().get()));
        new ComparisonReportWriter().write(
                comparison,
                getVariantName().get(),
                getReportDir().get().getAsFile().toPath(),
                getJsonReport().get(),
                getHtmlReport().get());
        getLogger().lifecycle("Android Insight comparison reports: {}", getReportDir().get().getAsFile());
    }
}
