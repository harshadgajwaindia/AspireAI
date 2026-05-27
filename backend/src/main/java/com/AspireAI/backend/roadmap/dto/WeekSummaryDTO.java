package com.AspireAI.backend.roadmap.dto;



public record WeekSummaryDTO(
        int weekNumber,
        String theme,               
        String focusSkills,         
        int totalMinutes,
        java.util.List<RoadmapItemDTO> items
) {}
