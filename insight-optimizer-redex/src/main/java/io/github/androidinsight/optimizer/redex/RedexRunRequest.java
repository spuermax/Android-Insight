package io.github.androidinsight.optimizer.redex;

import java.nio.file.Path;
import java.util.Map;

public record RedexRunRequest(
        Path inputApk,
        Path outputApk,
        Path configFile,
        Path proguardConfig,
        String command,
        String pythonExecutable,
        Path redexScript,
        Path redexBinary,
        Path androidSdkPath,
        boolean sign,
        Path keystore,
        String keyAlias,
        String keyPass,
        Map<String, String> environment,
        Path logFile) {

    public RedexRunRequest {
        environment = environment == null ? Map.of() : Map.copyOf(environment);
    }
}
