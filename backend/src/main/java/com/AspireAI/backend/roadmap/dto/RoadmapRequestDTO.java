package com.AspireAI.backend.roadmap.dto;


import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;

import java.util.UUID;

public record RoadmapRequestDTO(

        UUID userId,

        String targetCompany,

        Integer durationDays,          

        Integer dailyStudyMinutes,   

        SkillGapReportDTO skillGapReport

) {}
