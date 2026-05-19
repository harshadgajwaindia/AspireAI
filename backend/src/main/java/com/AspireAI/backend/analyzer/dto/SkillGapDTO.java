package com.AspireAI.backend.analyzer.dto;

public record SkillGapDTO(
        String skillName,
        int studentScore,
        int requiredScore,
        int gapSize,
        String priority    
) {}