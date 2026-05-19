package com.AspireAI.backend.roadmap.dto;

// ─── RoadmapRequestDTO ────────────────────────────────────────────────────────
// What the controller receives when a student requests a roadmap.
// The skillGapReport comes from the Analyzer Agent's previous output.
// If the user already has a saved profile (from a previous analysis),
// we can also just pass userId + targetCompany and load it from DB.

import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;

import java.util.UUID;

public record RoadmapRequestDTO(

        UUID userId,

        String targetCompany,

        Integer durationDays,          // 30, 60, or 90

        Integer dailyStudyMinutes,    // 60-180 mins/day

        SkillGapReportDTO skillGapReport

) {}
