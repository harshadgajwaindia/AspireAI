package com.AspireAI.backend.interview.dto;


/**
 * What Gemini returns when generating a question.
 * Internal — never exposed directly to frontend.
 */
public record GeneratedQuestionDTO(
        String questionText,
        String questionType,
        String skillArea,
        Integer difficultyLevel,
        String ragInsight
) {}
