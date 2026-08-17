package io.github.androidinsight.gradle;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class RedexExtension {
    private final Property<Boolean> enabled;
    private final Property<String> strategy;
    private final Property<String> command;
    private final Property<String> pythonExecutable;
    private final Property<String> redexHome;
    private final Property<String> redexScript;
    private final Property<String> redexBinary;
    private final Property<String> androidSdkPath;
    private final Property<String> pythonPath;
    private final Property<String> configFile;
    private final Property<String> proguardConfig;
    private final Property<Boolean> sign;
    private final Property<String> keystore;
    private final Property<String> keyAlias;
    private final Property<String> keyPass;
    private final Property<String> trace;

    @Inject
    public RedexExtension(ObjectFactory objects) {
        enabled = objects.property(Boolean.class).convention(false);
        strategy = objects.property(String.class).convention("strip-interdex");
        command = objects.property(String.class).convention("redex");
        pythonExecutable = objects.property(String.class).convention("python3");
        redexHome = objects.property(String.class).convention("");
        redexScript = objects.property(String.class).convention("");
        redexBinary = objects.property(String.class).convention("");
        androidSdkPath = objects.property(String.class).convention("");
        pythonPath = objects.property(String.class).convention("");
        configFile = objects.property(String.class).convention("");
        proguardConfig = objects.property(String.class).convention("");
        sign = objects.property(Boolean.class).convention(false);
        keystore = objects.property(String.class).convention("");
        keyAlias = objects.property(String.class).convention("");
        keyPass = objects.property(String.class).convention("");
        trace = objects.property(String.class).convention("1");
    }

    public Property<Boolean> getEnabled() { return enabled; }
    public Property<String> getStrategy() { return strategy; }
    public Property<String> getCommand() { return command; }
    public Property<String> getPythonExecutable() { return pythonExecutable; }
    public Property<String> getRedexHome() { return redexHome; }
    public Property<String> getRedexScript() { return redexScript; }
    public Property<String> getRedexBinary() { return redexBinary; }
    public Property<String> getAndroidSdkPath() { return androidSdkPath; }
    public Property<String> getPythonPath() { return pythonPath; }
    public Property<String> getConfigFile() { return configFile; }
    public Property<String> getProguardConfig() { return proguardConfig; }
    public Property<Boolean> getSign() { return sign; }
    public Property<String> getKeystore() { return keystore; }
    public Property<String> getKeyAlias() { return keyAlias; }
    public Property<String> getKeyPass() { return keyPass; }
    public Property<String> getTrace() { return trace; }
}
