package com.AspireAI.backend.roadmap.dto;


public record RoadmapItemDTO(
        java.util.UUID id,
        int dayNumber,
        java.time.LocalDate scheduledDate,
        String skillName,
        String category,
        String taskTitle,
        String taskDescription,
        int estimatedMinutes,
        int difficultyLevel,
        String resourceUrl,
        String resourceType,
        int gapImpact,
        String completionStatus,
        String ragInsight          
) {}