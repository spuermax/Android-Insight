package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.optimizer.redex.ValidationResult;

public final class OptimizationHtmlReportRenderer {
    public String render(OptimizationReport report) {
        ApkAnalysis before = report.original();
        ApkAnalysis after = report.optimized();
        long delta = after.apkBytes() - before.apkBytes();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>Android Insight · ReDex 优化报告</title>")
                .append("<style>body{font-family:-apple-system,BlinkMacSystemFont,'PingFang SC','Microsoft YaHei',Segoe UI,sans-serif;max-width:1120px;margin:40px auto;padding:0 20px;color:#222}table{border-collapse:collapse;width:100%;margin:16px 0 30px}th,td{border-bottom:1px solid #ddd;padding:10px;text-align:left}th{background:#f7f7f7}.muted{color:#666}.hero{display:flex;gap:14px;flex-wrap:wrap;margin:24px 0}.card{border:1px solid #e2e5e9;border-radius:12px;padding:16px;min-width:180px}.card b{font-size:27px;display:block}.good{color:#16803a;font-weight:700}.bad{color:#b42318;font-weight:700}.ok{color:#16803a}.warn{color:#b26a00}</style></head><body>");
        html.append("<h1>Android Insight · ReDex 优化报告</h1><p class=\"muted\">Variant: ").append(esc(report.variantName()))
                .append(" · 策略: ").append(esc(report.strategy().displayName())).append("</p>");
        html.append("<div class=\"hero\">");
        card(html, "APK 优化幅度", ComparisonMath.signedPercent(before.apkBytes(), after.apkBytes()), delta < 0 ? "good" : delta > 0 ? "bad" : "");
        card(html, "DEX", before.dexCount() + " → " + after.dexCount(), after.dexCount() <= before.dexCount() ? "good" : "bad");
        card(html, "健康分", report.originalDiagnosis().healthScore() + " → " + report.optimizedDiagnosis().healthScore(), "");
        card(html, "ReDex 耗时", String.format("%.2f s", report.redexRun().durationMillis() / 1000.0), "");
        html.append("</div>");

        html.append("<h2>优化前后</h2><table><tr><th>指标</th><th>优化前</th><th>优化后</th><th>变化</th></tr>");
        row(html, "APK 大小", SizeFormatter.human(before.apkBytes()), SizeFormatter.human(after.apkBytes()), ComparisonMath.signedPercent(before.apkBytes(), after.apkBytes()), delta);
        row(html, "DEX 数量", String.valueOf(before.dexCount()), String.valueOf(after.dexCount()), signed(after.dexCount() - before.dexCount()), after.dexCount() - before.dexCount());
        row(html, "Class Defs", String.valueOf(before.totalDexClassDefs()), String.valueOf(after.totalDexClassDefs()), signed(after.totalDexClassDefs() - before.totalDexClassDefs()), after.totalDexClassDefs() - before.totalDexClassDefs());
        row(html, "DEX Method ID Entries", String.valueOf(before.totalDexMethodIdEntries()), String.valueOf(after.totalDexMethodIdEntries()), signed(after.totalDexMethodIdEntries() - before.totalDexMethodIdEntries()), after.totalDexMethodIdEntries() - before.totalDexMethodIdEntries());
        row(html, "健康分", String.valueOf(report.originalDiagnosis().healthScore()), String.valueOf(report.optimizedDiagnosis().healthScore()), signed(report.optimizedDiagnosis().healthScore() - report.originalDiagnosis().healthScore()), report.originalDiagnosis().healthScore() - report.optimizedDiagnosis().healthScore());
        html.append("</table>");

        html.append("<h2>验证结果</h2><table><tr><th>检查项</th><th>结果</th><th>说明</th></tr>");
        validationRow(html, "原始 APK 未被覆盖", true, report.originalPreserved() ? "原始构建产物保持不变" : "原始 APK 发生变化");
        validationRow(html, "ZIP 对齐", report.zipAlignment());
        validationRow(html, "APK 签名", report.signature());
        html.append("</table>");

        html.append("<h2>产物</h2><p><b>优化 APK：</b>").append(esc(after.apkPath().toString())).append("</p>")
                .append("<p><b>ReDex 日志：</b>").append(esc(report.redexRun().logFile().toString())).append("</p>")
                .append("<p class=\"muted\">V0.4 不会替换 Android Gradle Plugin 生成的原始 APK。优化产物需要完成安装和核心功能回归后，再决定是否进入发布流程。</p>");
        html.append("</body></html>");
        return html.toString();
    }

    private static void row(StringBuilder html, String name, String before, String after, String change, long delta) {
        html.append("<tr><td>").append(esc(name)).append("</td><td>").append(esc(before)).append("</td><td>").append(esc(after))
                .append("</td><td class=\"").append(delta < 0 ? "good" : delta > 0 ? "bad" : "").append("\">").append(esc(change)).append("</td></tr>");
    }

    private static void validationRow(StringBuilder html, String name, ValidationResult result) {
        String resultText = !result.attempted() ? "跳过" : result.success() ? "通过" : "失败";
        html.append("<tr><td>").append(esc(name)).append("</td><td class=\"").append(result.success() ? "ok" : "warn").append("\">")
                .append(resultText).append("</td><td>").append(esc(result.message())).append("</td></tr>");
    }

    private static void validationRow(StringBuilder html, String name, boolean success, String message) {
        html.append("<tr><td>").append(esc(name)).append("</td><td class=\"").append(success ? "ok" : "warn").append("\">")
                .append(success ? "通过" : "失败").append("</td><td>").append(esc(message)).append("</td></tr>");
    }

    private static void card(StringBuilder html, String label, String value, String css) {
        html.append("<div class=\"card\"><b class=\"").append(css).append("\">").append(esc(value)).append("</b>").append(esc(label)).append("</div>");
    }

    private static String signed(long value) { return value > 0 ? "+" + value : String.valueOf(value); }
    private static String esc(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
}
