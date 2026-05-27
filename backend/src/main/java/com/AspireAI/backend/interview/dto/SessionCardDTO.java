package com.AspireAI.backend.interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;


public record SessionCardDTO(
        UUID sessionId,
        String targetCompany,
        String interviewType,
        Integer overallScore,
        String overallGrade,
        Integer totalQuestions,
        LocalDateTime startedAt
) {}