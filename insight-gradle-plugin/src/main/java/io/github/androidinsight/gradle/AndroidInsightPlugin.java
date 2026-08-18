package io.github.androidinsight.gradle;

import com.android.build.api.artifact.SingleArtifact;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;

public final class AndroidInsightPlugin implements Plugin<Project> {
    private static final String TASK_GROUP = "android insight";

    @Override
    public void apply(Project project) {
        AndroidInsightExtension extension = project.getExtensions().create(
                "androidInsight",
                AndroidInsightExtension.class);

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

        });
    }
}
