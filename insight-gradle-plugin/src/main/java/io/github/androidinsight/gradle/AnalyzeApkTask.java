package io.github.androidinsight.gradle;

import com.android.build.api.variant.BuiltArtifacts;
import com.android.build.api.variant.BuiltArtifactsLoader;
import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkAnalyzer;
import io.github.androidinsight.diagnosis.DiagnosisEngine;
import io.github.androidinsight.diagnosis.DiagnosisReport;
import io.github.androidinsight.report.ConsoleReportRenderer;
import io.github.androidinsight.report.ReportWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class AnalyzeApkTask extends DefaultTask {
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

    @TaskAction
    public void analyze() throws Exception {
        BuiltArtifacts artifacts = getBuiltArtifactsLoader().get().load(getApkFolder().get());
        if (artifacts == null || artifacts.getElements().isEmpty()) {
            throw new IllegalStateException("Android Insight could not find APK artifacts for " + getVariantName().get());
        }

        ApkAnalyzer analyzer = new ApkAnalyzer();
        DiagnosisEngine diagnosisEngine = new DiagnosisEngine();
        ConsoleReportRenderer console = new ConsoleReportRenderer();
        ReportWriter reportWriter = new ReportWriter();

        for (var artifact : artifacts.getElements()) {
            File apk = new File(artifact.getOutputFile());
            ApkAnalysis result = analyzer.analyze(apk.toPath(), getTopFiles().get());
            DiagnosisReport diagnosis = diagnosisEngine.diagnose(result);
            getLogger().lifecycle(console.render(result, getVariantName().get(), diagnosis));
            reportWriter.write(
                    result,
                    diagnosis,
                    getVariantName().get(),
                    getReportDir().get().getAsFile().toPath(),
                    getJsonReport().get(),
                    getHtmlReport().get());
        }

        getLogger().lifecycle("Android Insight reports: {}", getReportDir().get().getAsFile());
    }
}
