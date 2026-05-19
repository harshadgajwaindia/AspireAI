package com.AspireAI.backend.interview.dto;

import java.util.UUID;

/**
 * A single question delivered to the student.
 * Does NOT include the ideal answer (that comes only in TurnFeedbackDTO after they answer).
 */
public record QuestionDTO(
        UUID turnId,
        Integer questionNumber,
        Integer totalQuestions,
        String skillArea,
        String questionText,
        String questionType,                        // "CONCEPTUAL", "CODING", "BEHAVIORAL"
        Integer difficultyLevel,
        String ragInsight                           // "This pattern appeared in 3 recent TCS interviews"
) {}