package com.AspireAI.backend.interview.dto;


public record AnswerEvaluationDTO(
        Integer score,
        String grade,
        String whatWasGood,
        String whatWasMissed,
        String idealAnswer,
        String followUpHint
) {}
