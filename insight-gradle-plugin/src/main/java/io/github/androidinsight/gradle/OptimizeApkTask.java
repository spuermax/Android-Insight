package io.github.androidinsight.gradle;

import com.android.build.api.variant.BuiltArtifacts;
import com.android.build.api.variant.BuiltArtifactsLoader;
import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkAnalyzer;
import io.github.androidinsight.diagnosis.DiagnosisEngine;
import io.github.androidinsight.diagnosis.DiagnosisReport;
import io.github.androidinsight.optimizer.redex.AndroidBuildToolsVerifier;
import io.github.androidinsight.optimizer.redex.BuiltinRedexConfigs;
import io.github.androidinsight.optimizer.redex.RedexRunRequest;
import io.github.androidinsight.optimizer.redex.RedexRunResult;
import io.github.androidinsight.optimizer.redex.RedexRunner;
import io.github.androidinsight.optimizer.redex.RedexStrategy;
import io.github.androidinsight.optimizer.redex.ValidationResult;
import io.github.androidinsight.report.OptimizationConsoleReportRenderer;
import io.github.androidinsight.report.OptimizationReport;
import io.github.androidinsight.report.OptimizationReportWriter;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class OptimizeApkTask extends DefaultTask {
    @InputDirectory @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getApkFolder();

    @OutputDirectory public abstract DirectoryProperty getOutputDir();
    @OutputDirectory public abstract DirectoryProperty getReportDir();
    @Internal public abstract Property<BuiltArtifactsLoader> getBuiltArtifactsLoader();

    @Input public abstract Property<String> getVariantName();
    @Input public abstract Property<Integer> getTopFiles();
    @Input public abstract Property<Boolean> getJsonReport();
    @Input public abstract Property<Boolean> getHtmlReport();
    @Input public abstract Property<String> getStrategy();
    @Input public abstract Property<String> getRedexCommand();
    @Input public abstract Property<String> getPythonExecutable();
    @Input public abstract Property<String> getRedexHome();
    @Input public abstract Property<String> getRedexScript();
    @Input public abstract Property<String> getRedexBinary();
    @Input public abstract Property<String> getAndroidSdkPath();
    @Input public abstract Property<String> getPythonPath();
    @Input public abstract Property<String> getConfigFile();
    @Input public abstract Property<String> getProguardConfig();
    @Input public abstract Property<Boolean> getSign();
    @Input public abstract Property<String> getKeystore();
    @Input public abstract Property<String> getKeyAlias();
    @Internal public abstract Property<String> getKeyPass();
    @Input public abstract Property<String> getTrace();

    @TaskAction
    public void optimize() throws Exception {
        BuiltArtifacts artifacts = getBuiltArtifactsLoader().get().load(getApkFolder().get());
        if (artifacts == null || artifacts.getElements().isEmpty()) {
            throw new IllegalStateException("Android Insight 找不到 variant " + getVariantName().get() + " 的 APK 产物");
        }
        if (artifacts.getElements().size() != 1) {
            throw new IllegalStateException("V0.4 optimize 暂时只支持单 APK variant，当前找到 " + artifacts.getElements().size() + " 个 APK。Split APK 后续支持。");
        }

        RedexStrategy strategy = RedexStrategy.parse(getStrategy().get());
        File originalFile = new File(artifacts.getElements().iterator().next().getOutputFile());
        Path original = originalFile.toPath().toAbsolutePath().normalize();
        String originalHash = sha256(original);

        Path outputDir = getOutputDir().get().getAsFile().toPath();
        Path reportDir = getReportDir().get().getAsFile().toPath();
        Files.createDirectories(outputDir);
        Files.createDirectories(reportDir);

        String originalName = original.getFileName().toString();
        String baseName = originalName.toLowerCase().endsWith(".apk") ? originalName.substring(0, originalName.length() - 4) : originalName;
        Path optimized = outputDir.resolve(baseName + "-redex-" + strategy.id() + ".apk");
        Path logFile = reportDir.resolve("redex.log");

        Path config = pathOrNull(getConfigFile().getOrElse(""));
        if (config == null) {
            config = BuiltinRedexConfigs.materialize(strategy, reportDir.resolve("redex-config"));
        }

        Map<String, String> environment = new LinkedHashMap<>();
        if (!getPythonPath().getOrElse("").isBlank()) environment.put("PYTHONPATH", getPythonPath().get());
        if (!getTrace().getOrElse("").isBlank()) environment.put("TRACE", getTrace().get());

        Path redexHome = pathOrNull(getRedexHome().getOrElse(""));
        Path redexScript = pathOrNull(getRedexScript().getOrElse(""));
        Path redexBinary = pathOrNull(getRedexBinary().getOrElse(""));
        if (redexHome != null) {
            if (redexScript == null) redexScript = redexHome.resolve("redex.py");
            if (redexBinary == null) {
                String binaryName = System.getProperty("os.name", "").toLowerCase().contains("win") ? "redex-all.exe" : "redex-all";
                redexBinary = redexHome.resolve(binaryName);
            }
        }

        RedexRunRequest request = new RedexRunRequest(
                original,
                optimized,
                config,
                pathOrNull(getProguardConfig().getOrElse("")),
                getRedexCommand().getOrElse("redex"),
                getPythonExecutable().getOrElse("python3"),
                redexScript,
                redexBinary,
                pathOrNull(getAndroidSdkPath().getOrElse("")),
                getSign().get(),
                pathOrNull(getKeystore().getOrElse("")),
                getKeyAlias().getOrElse(""),
                getKeyPass().getOrElse(""),
                environment,
                logFile);

        getLogger().lifecycle("Android Insight：开始 ReDex 优化 [{}] -> {}", strategy.displayName(), optimized.getFileName());
        RedexRunResult run = new RedexRunner().run(request, line -> getLogger().info("[ReDex] {}", line));
        if (!run.success()) {
            throw new IllegalStateException("ReDex 执行失败（exit=" + run.exitCode() + "），请查看日志：" + logFile);
        }

        boolean originalPreserved = originalHash.equals(sha256(original));
        ApkAnalyzer analyzer = new ApkAnalyzer();
        ApkAnalysis before = analyzer.analyze(original, getTopFiles().get());
        ApkAnalysis after = analyzer.analyze(optimized, getTopFiles().get());
        DiagnosisEngine diagnosisEngine = new DiagnosisEngine();
        DiagnosisReport beforeDiagnosis = diagnosisEngine.diagnose(before);
        DiagnosisReport afterDiagnosis = diagnosisEngine.diagnose(after);

        AndroidBuildToolsVerifier verifier = new AndroidBuildToolsVerifier();
        Path sdk = pathOrNull(getAndroidSdkPath().getOrElse(""));
        ValidationResult alignment = verifier.verifyZipAlignment(sdk, optimized);
        ValidationResult signature = getSign().get()
                ? verifier.verifySignature(sdk, optimized)
                : ValidationResult.skipped("本次优化配置为不签名，因此跳过签名验证");

        OptimizationReport report = new OptimizationReport(
                getVariantName().get(), strategy, before, beforeDiagnosis, after, afterDiagnosis,
                run, alignment, signature, originalPreserved);
        getLogger().lifecycle(new OptimizationConsoleReportRenderer().render(report));
        new OptimizationReportWriter().write(report, reportDir, getJsonReport().get(), getHtmlReport().get());
        getLogger().lifecycle("Android Insight 优化 APK：{}", optimized);
        getLogger().lifecycle("Android Insight 优化报告：{}", reportDir);
    }

    private static Path pathOrNull(String value) {
        return value == null || value.isBlank() ? null : Path.of(value).toAbsolutePath().normalize();
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var in = Files.newInputStream(path)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
