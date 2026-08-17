package io.github.androidinsight.optimizer.redex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RedexRunner {
    public RedexRunResult run(RedexRunRequest request, Consumer<String> liveLog)
            throws IOException, InterruptedException {
        validate(request);
        Files.createDirectories(request.outputApk().toAbsolutePath().getParent());
        Files.createDirectories(request.logFile().toAbsolutePath().getParent());
        Files.deleteIfExists(request.outputApk());

        List<String> command = buildCommand(request);
        List<String> safeCommand = redact(command, request.keyPass());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Map<String, String> env = builder.environment();
        env.putAll(request.environment());

        Instant start = Instant.now();
        int exit;
        try (BufferedWriter writer = Files.newBufferedWriter(
                request.logFile(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("Android Insight ReDex runner\n");
            writer.write("Command: " + String.join(" ", safeCommand) + "\n\n");
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    if (liveLog != null) liveLog.accept(line);
                }
            }
            exit = process.waitFor();
        }
        long duration = Duration.between(start, Instant.now()).toMillis();
        boolean success = exit == 0 && Files.isRegularFile(request.outputApk());
        return new RedexRunResult(success, exit, duration, request.outputApk(), request.logFile(), safeCommand);
    }

    private static List<String> buildCommand(RedexRunRequest request) {
        List<String> command = new ArrayList<>();
        if (request.redexScript() != null) {
            command.add(nonBlank(request.pythonExecutable(), "python3"));
            command.add(request.redexScript().toAbsolutePath().toString());
        } else {
            command.add(nonBlank(request.command(), "redex"));
        }

        command.add("-c");
        command.add(request.configFile().toAbsolutePath().toString());
        if (request.redexBinary() != null) {
            command.add("--redex-binary");
            command.add(request.redexBinary().toAbsolutePath().toString());
        }
        if (request.androidSdkPath() != null) {
            command.add("--android-sdk-path");
            command.add(request.androidSdkPath().toAbsolutePath().toString());
        }
        if (request.proguardConfig() != null) {
            command.add("-P");
            command.add(request.proguardConfig().toAbsolutePath().toString());
        }
        if (request.sign()) {
            command.add("--sign");
            if (request.keystore() != null) {
                command.add("-s");
                command.add(request.keystore().toAbsolutePath().toString());
            }
            if (request.keyAlias() != null && !request.keyAlias().isBlank()) {
                command.add("-a");
                command.add(request.keyAlias());
            }
            if (request.keyPass() != null && !request.keyPass().isBlank()) {
                command.add("-p");
                command.add(request.keyPass());
            }
        } else {
            command.add("--no-sign");
        }
        command.add("--always-clean-up");
        command.add(request.inputApk().toAbsolutePath().toString());
        command.add("-o");
        command.add(request.outputApk().toAbsolutePath().toString());
        return command;
    }

    private static void validate(RedexRunRequest request) {
        requireFile(request.inputApk(), "Input APK");
        requireFile(request.configFile(), "ReDex config");
        if (request.redexScript() != null) requireFile(request.redexScript(), "redex.py");
        if (request.redexBinary() != null) requireFile(request.redexBinary(), "redex-all");
        if (request.proguardConfig() != null) requireFile(request.proguardConfig(), "ProGuard config");
        if (request.sign()) {
            if (request.keystore() == null) {
                throw new IllegalArgumentException("ReDex signing is enabled but no keystore is configured.");
            }
            requireFile(request.keystore(), "Keystore");
        }
    }

    private static void requireFile(Path path, String label) {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(label + " does not exist: " + path);
        }
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static List<String> redact(List<String> command, String secret) {
        if (secret == null || secret.isBlank()) return List.copyOf(command);
        List<String> copy = new ArrayList<>(command.size());
        for (String arg : command) copy.add(secret.equals(arg) ? "******" : arg);
        return List.copyOf(copy);
    }
}
