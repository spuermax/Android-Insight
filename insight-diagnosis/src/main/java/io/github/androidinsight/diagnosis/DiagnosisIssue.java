package io.github.androidinsight.diagnosis;

import java.util.List;

public record DiagnosisIssue(
        String id,
        Severity severity,
        IssueCategory category,
        String title,
        String message,
        List<String> evidence,
        List<String> suggestions) {

    public DiagnosisIssue {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Issue id must not be blank");
        if (severity == null) throw new IllegalArgumentException("Severity must not be null");
        if (category == null) throw new IllegalArgumentException("Category must not be null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title must not be blank");
        if (message == null) message = "";
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
    }
}
