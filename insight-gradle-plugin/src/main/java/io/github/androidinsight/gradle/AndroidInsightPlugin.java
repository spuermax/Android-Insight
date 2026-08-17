package io.github.androidinsight.gradle;

import com.android.build.api.artifact.SingleArtifact;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;

import java.io.FileInputStream;
import java.util.Properties;

public final class AndroidInsightPlugin implements Plugin<Project> {
    private static final String TASK_GROUP = "android insight";

    @Override
    public void apply(Project project) {
        AndroidInsightExtension extension = project.getExtensions().create(
                "androidInsight",
                AndroidInsightExtension.class);

        String detectedSdk = detectAndroidSdk(project);
        if (!detectedSdk.isBlank()) extension.getRedex().getAndroidSdkPath().convention(detectedSdk);

        project.getPluginManager().withPlugin("com.android.application", ignored -> configureAndroid(project, extension));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void configureAndroid(Project project, AndroidInsightExtension extension) {
        ApplicationAndroidComponentsExtension androidComponents =
                project.getExtensions().getByType(ApplicationAndroidComponentsExtension.class);

        androidComponents.onVariants(androidComponents.selector().all(), variant -> {
            String variantName = variant.getName();
            String capitalized = Character.toUpperCase(variantName.charAt(0)) + variantName.substring(1);

            TaskProvider<AnalyzeApkTask> analyzeTask = project.getTasks().register(
                    "analyze" + capitalized + "Apk",
                    AnalyzeApkTask.class,
                    task -> {
                        task.setGroup(TASK_GROUP);
                        task.setDescription("分析并诊断 Android variant " + variantName + " 的 APK。");
                        task.getVariantName().set(variantName);
                        task.getTopFiles().set(extension.getTopFiles());
                        task.getJsonReport().set(extension.getJsonReport());
                        task.getHtmlReport().set(extension.getHtmlReport());
                        task.getReportDir().set(project.getLayout().getBuildDirectory().dir("reports/android-insight/" + variantName));
                        task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
                        task.onlyIf("androidInsight.enabled = true", ignored -> extension.getEnabled().get());
                    });

            variant.getArtifacts().use(analyzeTask).wiredWith(AnalyzeApkTask::getApkFolder).toListenTo(SingleArtifact.APK.INSTANCE);

            TaskProvider<CompareApkTask> compareTask = project.getTasks().register(
                    "compare" + capitalized + "Apk",
                    CompareApkTask.class,
                    task -> {
                        task.setGroup(TASK_GROUP);
                        task.setDescription("比较 Android variant " + variantName + " 的当前 APK 与配置的候选 APK。");
                        task.getVariantName().set(variantName);
                        task.getTopFiles().set(extension.getTopFiles());
                        task.getJsonReport().set(extension.getJsonReport());
                        task.getHtmlReport().set(extension.getHtmlReport());
                        task.getComparisonApks().set(extension.getComparisonApks());
                        task.getInputs().files(extension.getComparisonApks().map(map -> map.values()))
                                .withPropertyName("comparisonApkFiles")
                                .withPathSensitivity(PathSensitivity.RELATIVE);
                        task.getReportDir().set(project.getLayout().getBuildDirectory().dir("reports/android-insight/" + variantName + "/compare"));
                        task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
                        task.onlyIf("androidInsight.enabled = true", ignored -> extension.getEnabled().get());
                    });

            variant.getArtifacts().use(compareTask).wiredWith(CompareApkTask::getApkFolder).toListenTo(SingleArtifact.APK.INSTANCE);

            TaskProvider<OptimizeApkTask> optimizeTask = project.getTasks().register(
                    "optimize" + capitalized + "Apk",
                    OptimizeApkTask.class,
                    task -> {
                        task.setGroup(TASK_GROUP);
                        task.setDescription("使用 ReDex 优化 Android variant " + variantName + "，并生成独立 APK 与前后对比报告（不会覆盖原 APK）。");
                        task.getVariantName().set(variantName);
                        task.getTopFiles().set(extension.getTopFiles());
                        task.getJsonReport().set(extension.getJsonReport());
                        task.getHtmlReport().set(extension.getHtmlReport());
                        task.getStrategy().set(extension.getRedex().getStrategy());
                        task.getRedexCommand().set(extension.getRedex().getCommand());
                        task.getPythonExecutable().set(extension.getRedex().getPythonExecutable());
                        task.getRedexHome().set(extension.getRedex().getRedexHome());
                        task.getRedexScript().set(extension.getRedex().getRedexScript());
                        task.getRedexBinary().set(extension.getRedex().getRedexBinary());
                        task.getAndroidSdkPath().set(extension.getRedex().getAndroidSdkPath());
                        task.getPythonPath().set(extension.getRedex().getPythonPath());
                        task.getConfigFile().set(extension.getRedex().getConfigFile());
                        task.getProguardConfig().set(extension.getRedex().getProguardConfig());
                        task.getSign().set(extension.getRedex().getSign());
                        task.getKeystore().set(extension.getRedex().getKeystore());
                        task.getKeyAlias().set(extension.getRedex().getKeyAlias());
                        task.getKeyPass().set(extension.getRedex().getKeyPass());
                        task.getTrace().set(extension.getRedex().getTrace());
                        task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("outputs/android-insight/" + variantName));
                        task.getReportDir().set(project.getLayout().getBuildDirectory().dir("reports/android-insight/" + variantName + "/optimize"));
                        task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
                        // ReDex is an external tool. V0.4 deliberately re-runs optimization when the task is invoked
                        // instead of trusting Gradle up-to-date checks for an executable that may change in-place.
                        task.getOutputs().upToDateWhen(ignored -> false);
                        task.onlyIf("androidInsight.enabled && androidInsight.redex.enabled", ignored ->
                                extension.getEnabled().get() && extension.getRedex().getEnabled().get());
                    });

            variant.getArtifacts().use(optimizeTask).wiredWith(OptimizeApkTask::getApkFolder).toListenTo(SingleArtifact.APK.INSTANCE);
        });
    }

    private static String detectAndroidSdk(Project project) {
        String env = System.getenv("ANDROID_SDK_ROOT");
        if (env == null || env.isBlank()) env = System.getenv("ANDROID_HOME");
        if (env != null && !env.isBlank()) return env;

        var localProperties = project.getRootProject().file("local.properties");
        if (localProperties.isFile()) {
            Properties properties = new Properties();
            try (FileInputStream in = new FileInputStream(localProperties)) {
                properties.load(in);
                String sdkDir = properties.getProperty("sdk.dir", "").trim();
                if (!sdkDir.isBlank()) return sdkDir;
            } catch (Exception ignored) {
                // The optimization task will provide a clear message later if SDK tools are needed.
            }
        }
        return "";
    }
}
