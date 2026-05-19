package com.AspireAI.backend.roadmap.dto;

// ─── RoadmapPlanDTO ───────────────────────────────────────────────────────────
// Full plan returned to the frontend after generation.

public record RoadmapPlanDTO(
        java.util.UUID planId,
        java.util.UUID userId,
        String targetCompany,
        int totalDays,
        java.time.LocalDate startDate,
        java.time.LocalDate targetDate,
        int baselineReadiness,
        String status,
        java.util.List<WeekSummaryDTO> weeks,
        ProgressSummaryDTO progress
) {}

