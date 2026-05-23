package com.AspireAI.backend.roadmap.dto;

public record GeneratedTask(
        int dayNumber,
        String skillName,
        String category,
        String taskTitle,
        String taskDescription,
        int estimatedMinutes,
        int difficultyLevel,
        int gapImpact,
        String ragSourceIds,
        String referenceUrl,
        String resourceType
) {}
