package com.AspireAI.backend.analyzer.dto;

public record SkillGapDTO(
        String skillName,
        String category,
        int requiredScore,
        int studentScore,
        int gapSize,
        String priority,
        String reason
) {
    public int gapScore() {
        return gapSize;
    }
}