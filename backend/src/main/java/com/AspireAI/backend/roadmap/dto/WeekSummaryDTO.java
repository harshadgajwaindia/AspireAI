package com.AspireAI.backend.roadmap.dto;

// Groups items by week for the frontend calendar view.

public record WeekSummaryDTO(
        int weekNumber,
        String theme,               // "DSA Week: Trees & Graphs"
        String focusSkills,         // "Binary Trees, BFS/DFS, Lowest Common Ancestor"
        int totalMinutes,
        java.util.List<RoadmapItemDTO> items
) {}
