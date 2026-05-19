package com.AspireAI.backend.interview.dto;

import java.util.UUID;

/**
 * Returned when a session is created — gives the frontend
 * the session ID and the FIRST question immediately.
 */
public record SessionStartedDTO(
        UUID sessionId,
        String targetCompany,
        String interviewType,
        Integer totalQuestions,
        QuestionDTO firstQuestion                   // first Q is generated on session start
) {}