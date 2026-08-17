package io.github.androidinsight.optimizer.redex;

import java.nio.file.Path;
import java.util.List;

public record RedexRunResult(
        boolean success,
        int exitCode,
        long durationMillis,
        Path outputApk,
        Path logFile,
        List<String> safeCommand) {
}
