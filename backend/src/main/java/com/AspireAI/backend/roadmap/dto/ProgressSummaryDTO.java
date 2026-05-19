package com.AspireAI.backend.roadmap.dto;

public record ProgressSummaryDTO(
        int totalItems,
        int completed,
        int skipped,
        int pending,
        double completionPercent,
        int streakDays,
        int projectedReadiness
) {}
