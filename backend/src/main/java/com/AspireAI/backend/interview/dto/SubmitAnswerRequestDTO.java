package com.AspireAI.backend.interview.dto;

import java.util.UUID;


public record SubmitAnswerRequestDTO(
        UUID sessionId,
        UUID turnId,
        String studentAnswer,
        Integer answerTimeSeconds
) {}
