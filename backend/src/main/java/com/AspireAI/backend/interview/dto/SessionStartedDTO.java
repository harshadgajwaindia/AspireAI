package com.AspireAI.backend.interview.dto;

import java.util.UUID;


public record SessionStartedDTO(
        UUID sessionId,
        String targetCompany,
        String interviewType,
        Integer totalQuestions,
        QuestionDTO firstQuestion                   
) {}