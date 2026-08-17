package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkComparison;
import io.github.androidinsight.core.ComparedApk;

public final class ComparisonHtmlReportRenderer {
    public String render(ApkComparison comparison, String variantName) {
        ApkAnalysis baseline = comparison.baseline();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>Android Insight · APK 对比</title>")
                .append("<style>body{font-family:-apple-system,BlinkMacSystemFont,'PingFang SC','Microsoft YaHei',Segoe UI,sans-serif;max-width:1150px;margin:40px auto;padding:0 20px;color:#222}table{border-collapse:collapse;width:100%;margin:16px 0 34px}th,td{border-bottom:1px solid #ddd;padding:10px;text-align:left}th{background:#f7f7f7}.muted{color:#666}.good{color:#16803a;font-weight:600}.bad{color:#b42318;font-weight:600}.pill{display:inline-block;padding:3px 8px;border-radius:999px;background:#f1f3f5}</style></head><body>");
        html.append("<h1>Android Insight · APK 对比报告</h1><p class=\"muted\">Variant: ").append(esc(variantName)).append(" · 基线: ").append(esc(comparison.baselineLabel())).append(" (").append(esc(baseline.apkPath().getFileName().toString())).append(")</p>");
        html.append("<h2>总览</h2><table><tr><th>APK</th><th>大小</th><th>变化量</th><th>变化率</th><th>DEX</th><th>Classes</th><th>Method ID Entries</th></tr>");
        row(html, comparison.baselineLabel(), baseline, null);
        for (ComparedApk item : comparison.candidates()) row(html, item.label(), item.analysis(), baseline);
        html.append("</table>");
        html.append("<h2>分类体积变化</h2>");
        for (ComparedApk item : comparison.candidates()) {
            html.append("<h3>").append(esc(item.label())).append("</h3><table><tr><th>分类</th><th>优化前</th><th>优化后</th><th>变化量</th><th>变化率</th></tr>");
            for (ApkCategory category : ApkCategory.values()) {
                long before = baseline.category(category).compressedBytes();
                long after = item.analysis().category(category).compressedBytes();
                long delta = after - before;
                html.append("<tr><td>").append(esc(category.displayName())).append("</td><td>").append(SizeFormatter.human(before)).append("</td><td>").append(SizeFormatter.human(after)).append("</td><td class=\"").append(css(delta)).append("\">").append(ComparisonMath.signedHuman(delta)).append("</td><td class=\"").append(css(delta)).append("\">").append(ComparisonMath.signedPercent(before, after)).append("</td></tr>");
            }
            html.append("</table>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    private static void row(StringBuilder html, String label, ApkAnalysis current, ApkAnalysis baseline) {
        Long delta = baseline == null ? null : current.apkBytes() - baseline.apkBytes();
        html.append("<tr><td><span class=\"pill\">").append(esc(label)).append("</span></td><td>").append(SizeFormatter.human(current.apkBytes())).append("</td><td");
        if (delta != null) html.append(" class=\"").append(css(delta)).append("\"");
        html.append('>').append(delta == null ? "-" : ComparisonMath.signedHuman(delta)).append("</td><td");
        if (delta != null) html.append(" class=\"").append(css(delta)).append("\"");
        html.append('>').append(delta == null ? "-" : ComparisonMath.signedPercent(baseline.apkBytes(), current.apkBytes())).append("</td><td>").append(current.dexCount()).append("</td><td>").append(current.totalDexClassDefs()).append("</td><td>").append(current.totalDexMethodIdEntries()).append("</td></tr>");
    }

    private static String css(long delta) { return delta < 0 ? "good" : delta > 0 ? "bad" : ""; }
    private static String esc(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
}
