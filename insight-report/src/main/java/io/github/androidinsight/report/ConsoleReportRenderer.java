package io.github.androidinsight.report;

import io.github.androidinsight.core.ApkAnalysis;
import io.github.androidinsight.core.ApkCategory;
import io.github.androidinsight.core.ApkEntryInfo;
import io.github.androidinsight.core.DexInfo;
import io.github.androidinsight.diagnosis.DiagnosisIssue;
import io.github.androidinsight.diagnosis.DiagnosisReport;

public final class ConsoleReportRenderer {
    public String render(ApkAnalysis analysis, String variantName, DiagnosisReport diagnosis) {
        StringBuilder out = new StringBuilder();
        out.append(System.lineSeparator());
        out.append("================ Android Insight ================").append(System.lineSeparator());
        out.append("Variant              : ").append(variantName).append(System.lineSeparator());
        out.append("APK                  : ").append(analysis.apkPath().getFileName()).append(System.lineSeparator());
        out.append("APK 大小             : ").append(SizeFormatter.human(analysis.apkBytes())).append(System.lineSeparator());
        out.append("ZIP 文件项           : ").append(analysis.entryCount()).append(System.lineSeparator());
        out.append("DEX 数量             : ").append(analysis.dexCount()).append(System.lineSeparator());
        out.append("Class Defs           : ").append(analysis.totalDexClassDefs()).append(System.lineSeparator());
        out.append("DEX Method ID Entries: ").append(analysis.totalDexMethodIdEntries()).append(System.lineSeparator());

        out.append(System.lineSeparator()).append("自动诊断").append(System.lineSeparator());
        out.append("  健康分             : ").append(diagnosis.healthScore()).append(" / 100").append(System.lineSeparator());
        out.append("  优化潜力           : ").append(ReportText.potential(diagnosis.optimizationPotential())).append(System.lineSeparator());
        out.append("  问题数量           : ").append(diagnosis.issues().size()).append(System.lineSeparator());
        if (diagnosis.issues().isEmpty()) {
            out.append("  当前默认规则未发现明显问题。").append(System.lineSeparator());
        } else {
            for (DiagnosisIssue issue : diagnosis.issues()) {
                out.append(System.lineSeparator())
                        .append("  [").append(ReportText.severity(issue.severity())).append("] ")
                        .append(issue.id()).append(" - ").append(issue.title()).append(System.lineSeparator());
                out.append("    ").append(issue.message()).append(System.lineSeparator());
                for (String evidence : issue.evidence()) out.append("    依据: ").append(evidence).append(System.lineSeparator());
                for (String suggestion : issue.suggestions()) out.append("    建议: ").append(suggestion).append(System.lineSeparator());
            }
        }

        out.append(System.lineSeparator()).append("APK 体积分布（APK 内压缩后）").append(System.lineSeparator());
        for (ApkCategory category : ApkCategory.values()) {
            long bytes = analysis.category(category).compressedBytes();
            out.append(String.format("  %-12s %10s  %8s%n", category.displayName(), SizeFormatter.human(bytes), SizeFormatter.percent(bytes, analysis.apkBytes())));
        }

        out.append(System.lineSeparator()).append("DEX 分布").append(System.lineSeparator());
        for (DexInfo dex : analysis.dexFiles()) {
            out.append(String.format("  %-14s %10s  classes=%-7d methodIdEntries=%-7d types=%d%n",
                    dex.name(), SizeFormatter.human(dex.compressedBytes()), dex.classDefs(), dex.methodIds(), dex.typeIds()));
        }

        out.append(System.lineSeparator()).append("体积最大的文件").append(System.lineSeparator());
        int index = 1;
        for (ApkEntryInfo entry : analysis.topFiles()) {
            out.append(String.format("  %2d. %10s  %-9s %s%n", index++, SizeFormatter.human(entry.compressedBytes()), entry.category().displayName(), entry.path()));
        }
        out.append("=================================================").append(System.lineSeparator());
        return out.toString();
    }
}
