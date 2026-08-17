package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkEntryInfo;
import io.github.androidinsight.core.DexInfo;
import io.github.androidinsight.diagnosis.DiagnosisIssue;
import io.github.androidinsight.diagnosis.DiagnosisReport;

public final class HtmlReportRenderer {
    public String render(ApkAnalysis analysis, String variantName, DiagnosisReport diagnosis) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>Android Insight · APK 体检报告</title>")
                .append("<style>body{font-family:-apple-system,BlinkMacSystemFont,'PingFang SC','Microsoft YaHei',Segoe UI,sans-serif;max-width:1100px;margin:40px auto;padding:0 20px;color:#222}table{border-collapse:collapse;width:100%;margin:16px 0 32px}th,td{border-bottom:1px solid #ddd;padding:9px;text-align:left}th{background:#f7f7f7}.muted{color:#666}.metric{display:inline-block;margin-right:28px;margin-bottom:12px}.metric b{font-size:22px;display:block}.hero{display:flex;gap:14px;flex-wrap:wrap;margin:24px 0}.card{border:1px solid #e2e5e9;border-radius:12px;padding:16px;min-width:180px}.card b{font-size:28px;display:block}.issue{border:1px solid #e2e5e9;border-left-width:5px;border-radius:10px;padding:16px;margin:14px 0}.HIGH{border-left-color:#b42318}.MEDIUM{border-left-color:#b26a00}.LOW{border-left-color:#52616b}.tag{display:inline-block;padding:3px 8px;border-radius:999px;background:#f1f3f5;font-size:12px;font-weight:700}.evidence,.suggestion{margin:6px 0}.evidence{color:#555}.suggestion{color:#164e2f}</style></head><body>");
        html.append("<h1>Android Insight · APK 体检报告</h1><p class=\"muted\">Variant: ").append(esc(variantName)).append(" · APK: ").append(esc(analysis.apkPath().getFileName().toString())).append("</p>");

        html.append("<div class=\"hero\">");
        card(html, "健康分", diagnosis.healthScore() + " / 100");
        card(html, "优化潜力", ReportText.potential(diagnosis.optimizationPotential()));
        card(html, "发现问题", String.valueOf(diagnosis.issues().size()));
        html.append("</div>");

        html.append("<h2>自动诊断</h2>");
        if (diagnosis.issues().isEmpty()) {
            html.append("<p>当前默认规则未发现明显问题。</p>");
        } else {
            for (DiagnosisIssue issue : diagnosis.issues()) {
                html.append("<section class=\"issue ").append(issue.severity()).append("\">")
                        .append("<span class=\"tag\">").append(ReportText.severity(issue.severity())).append("</span> ")
                        .append("<span class=\"tag\">").append(esc(issue.category().name())).append("</span>")
                        .append("<h3>").append(esc(issue.title())).append("</h3>")
                        .append("<p>").append(esc(issue.message())).append("</p>");
                for (String evidence : issue.evidence()) html.append("<div class=\"evidence\"><b>依据：</b> ").append(esc(evidence)).append("</div>");
                for (String suggestion : issue.suggestions()) html.append("<div class=\"suggestion\"><b>建议：</b> ").append(esc(suggestion)).append("</div>");
                html.append("</section>");
            }
        }

        html.append("<h2>APK 指标</h2>");
        metric(html, "APK 大小", SizeFormatter.human(analysis.apkBytes()));
        metric(html, "DEX 数量", String.valueOf(analysis.dexCount()));
        metric(html, "Class Defs", String.valueOf(analysis.totalDexClassDefs()));
        metric(html, "DEX Method ID Entries", String.valueOf(analysis.totalDexMethodIdEntries()));

        html.append("<h2>APK 体积分布</h2><table><tr><th>分类</th><th>压缩后</th><th>占比</th><th>解压后</th></tr>");
        for (ApkCategory category : ApkCategory.values()) {
            var size = analysis.category(category);
            html.append("<tr><td>").append(esc(category.displayName())).append("</td><td>").append(SizeFormatter.human(size.compressedBytes())).append("</td><td>")
                    .append(SizeFormatter.percent(size.compressedBytes(), analysis.apkBytes())).append("</td><td>").append(SizeFormatter.human(size.uncompressedBytes())).append("</td></tr>");
        }
        html.append("</table>");

        html.append("<h2>DEX 分布</h2><table><tr><th>DEX</th><th>压缩后</th><th>Classes</th><th>Method ID Entries</th><th>Types</th><th>Fields</th></tr>");
        for (DexInfo dex : analysis.dexFiles()) {
            html.append("<tr><td>").append(esc(dex.name())).append("</td><td>").append(SizeFormatter.human(dex.compressedBytes()))
                    .append("</td><td>").append(dex.classDefs()).append("</td><td>").append(dex.methodIds())
                    .append("</td><td>").append(dex.typeIds()).append("</td><td>").append(dex.fieldIds()).append("</td></tr>");
        }
        html.append("</table>");

        html.append("<h2>体积最大的文件</h2><table><tr><th>#</th><th>压缩后</th><th>分类</th><th>路径</th></tr>");
        int index = 1;
        for (ApkEntryInfo entry : analysis.topFiles()) {
            html.append("<tr><td>").append(index++).append("</td><td>").append(SizeFormatter.human(entry.compressedBytes()))
                    .append("</td><td>").append(esc(entry.category().displayName())).append("</td><td>").append(esc(entry.path())).append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    private static void metric(StringBuilder html, String label, String value) { html.append("<span class=\"metric\"><b>").append(esc(value)).append("</b>").append(esc(label)).append("</span>"); }
    private static void card(StringBuilder html, String label, String value) { html.append("<div class=\"card\"><b>").append(esc(value)).append("</b>").append(esc(label)).append("</div>"); }
    private static String esc(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
}
