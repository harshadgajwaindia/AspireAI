package com.AspireAI.backend.interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight card for the "Past Interviews" list on the dashboard.
 */
public record SessionCardDTO(
        UUID sessionId,
        String targetCompany,
        String interviewType,
        Integer overallScore,
        String overallGrade,
        Integer totalQuestions,
        LocalDateTime startedAt
) {}