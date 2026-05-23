package com.AspireAI.backend.analyzer.service;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

public class JobRagFormatter {

    public static String formatContext(List<Document> docs, String company) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== REAL %s JOB POSTINGS (retrieved from live data) ===\n\n", company.toUpperCase()));

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            sb.append(String.format("--- Job Posting %d ---\n", i + 1));
            if (doc.getMetadata().containsKey("role")) sb.append("Role: ").append(doc.getMetadata().get("role")).append("\n");
            if (doc.getMetadata().containsKey("date")) sb.append("Posted: ").append(doc.getMetadata().get("date")).append("\n");

            String text = doc.getText();
            sb.append(text, 0, Math.min(600, text.length()));
            if (text.length() > 600) sb.append("...");
            sb.append("\n\n");
        }

        sb.append("=== END OF JOB POSTINGS ===\n");
        return sb.toString();
    }

    public static String formatValidationContext(List<Document> docs) {
        if (docs.isEmpty()) return "";
        return docs.stream()
                .map(doc -> "- " + doc.getText().substring(0, Math.min(300, doc.getText().length())))
                .collect(Collectors.joining("\n"));
    }
}
