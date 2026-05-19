package com.AspireAI.backend.analyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SkillGapReportDTO(
        String targetCompany,
        int overallReadiness,
        List<SkillGapDTO> topGaps,
        SkillProfileDTO skillProfile,
        LocalDateTime analyzedAt
) {}