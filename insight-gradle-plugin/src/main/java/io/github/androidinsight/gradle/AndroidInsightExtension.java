package io.github.androidinsight.gradle;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class AndroidInsightExtension {
    private final Property<Boolean> enabled;
    private final Property<Integer> topFiles;
    private final Property<Boolean> jsonReport;
    private final Property<Boolean> htmlReport;
    private final MapProperty<String, String> comparisonApks;

    @Inject
    public AndroidInsightExtension(ObjectFactory objects) {
        enabled = objects.property(Boolean.class).convention(true);
        topFiles = objects.property(Integer.class).convention(20);
        jsonReport = objects.property(Boolean.class).convention(true);
        htmlReport = objects.property(Boolean.class).convention(true);
        comparisonApks = objects.mapProperty(String.class, String.class);
    }

    public Property<Boolean> getEnabled() { return enabled; }
    public Property<Integer> getTopFiles() { return topFiles; }
    public Property<Boolean> getJsonReport() { return jsonReport; }
    public Property<Boolean> getHtmlReport() { return htmlReport; }
    public MapProperty<String, String> getComparisonApks() { return comparisonApks; }
}
