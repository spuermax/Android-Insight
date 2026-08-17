package io.github.androidinsight.optimizer.redex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BuiltinRedexConfigs {
    private BuiltinRedexConfigs() {}

    public static Path materialize(RedexStrategy strategy, Path directory) throws IOException {
        Files.createDirectories(directory);
        String resource = switch (strategy) {
            case STRIP_DEBUG_INFO -> "strip.config";
            case STRIP_AND_INTERDEX -> "strip-interdex.config";
        };
        Path target = directory.resolve(resource);
        try (InputStream in = BuiltinRedexConfigs.class.getResourceAsStream(resource)) {
            if (in == null) throw new IOException("Missing built-in ReDex config resource: " + resource);
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }
}
