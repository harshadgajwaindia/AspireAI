package com.AspireAI.backend.interview.dto;

/**
 * What Gemini returns when evaluating a student's answer.
 * BeanOutputConverter maps Gemini's JSON → this record.
 */
public record AnswerEvaluationDTO(
        Integer score,
        String grade,
        String whatWasGood,
        String whatWasMissed,
        String idealAnswer,
        String followUpHint
) {}
