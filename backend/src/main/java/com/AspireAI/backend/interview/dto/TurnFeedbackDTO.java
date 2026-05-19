package com.AspireAI.backend.interview.dto;

import java.util.UUID;

/**
 * Gemini's structured feedback on one answer — returned immediately after submission.
 *
 * The frontend shows this as an "Answer Review" panel with collapsible sections.
 */
public record TurnFeedbackDTO(
        UUID turnId,
        Integer questionNumber,
        Integer score,                              // 0-10
        String grade,                               // "Excellent", "Good", "Needs Work", "Insufficient"
        String whatWasGood,
        String whatWasMissed,
        String idealAnswer,
        String followUpHint,                        // hint for a natural follow-up question
        QuestionDTO nextQuestion                    // null if this was the last question
) {}