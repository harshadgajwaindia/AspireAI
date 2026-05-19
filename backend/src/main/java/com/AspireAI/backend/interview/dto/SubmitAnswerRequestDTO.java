package com.AspireAI.backend.interview.dto;

import java.util.UUID;

/**
 * Sent when the student submits their answer to a question.
 */
public record SubmitAnswerRequestDTO(
        UUID sessionId,
        UUID turnId,
        String studentAnswer,
        Integer answerTimeSeconds
) {}
