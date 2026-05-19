package com.AspireAI.backend.analyzer.dto;

import java.util.Map;

public record GitHubProfileDTO(
        String username,
        int repoCount,
        int totalStars,
        int totalForks,
        Map<String, Integer> languageFrequency 
) {
    public static GitHubProfileDTO empty() {
        return new GitHubProfileDTO(null, 0, 0, 0, Map.of());
    }

    public boolean hasData() {
        return username != null && repoCount > 0;
    }
}