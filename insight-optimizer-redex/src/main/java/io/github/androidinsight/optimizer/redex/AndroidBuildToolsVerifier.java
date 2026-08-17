package io.github.androidinsight.optimizer.redex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class AndroidBuildToolsVerifier {
    public ValidationResult verifySignature(Path androidSdk, Path apk) {
        Path tool = findTool(androidSdk, "apksigner");
        if (tool == null) return ValidationResult.skipped("未找到 apksigner，已跳过签名验证");
        return run(List.of(tool.toString(), "verify", "--verbose", apk.toAbsolutePath().toString()), "APK 签名验证通过");
    }

    public ValidationResult verifyZipAlignment(Path androidSdk, Path apk) {
        Path tool = findTool(androidSdk, "zipalign");
        if (tool == null) return ValidationResult.skipped("未找到 zipalign，已跳过 ZIP 对齐验证");
        return run(List.of(tool.toString(), "-c", "-v", "4", apk.toAbsolutePath().toString()), "ZIP 对齐验证通过");
    }

    private static ValidationResult run(List<String> command, String successMessage) {
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        StringBuilder output = new StringBuilder();
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() < 4096) output.append(line).append('\n');
                }
            }
            int exit = process.waitFor();
            if (exit == 0) return new ValidationResult(true, true, successMessage);
            return new ValidationResult(true, false, "验证失败（exit=" + exit + "）：" + output.toString().trim());
        } catch (IOException e) {
            return new ValidationResult(true, false, "验证工具执行失败：" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ValidationResult(true, false, "验证被中断");
        }
    }

    private static Path findTool(Path androidSdk, String name) {
        if (androidSdk == null) return null;
        Path buildTools = androidSdk.resolve("build-tools");
        if (!Files.isDirectory(buildTools)) return null;
        String executable = isWindows() ? name + ".bat" : name;
        try (var stream = Files.list(buildTools)) {
            return stream.filter(Files::isDirectory)
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(dir -> dir.resolve(executable))
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
