package com.AspireAI.backend.interview.dto;

/**
 * Turn summary for the session review page (no full ideal answers — keep it concise).
 */
public record TurnSummaryDTO(
        Integer questionNumber,
        String skillArea,
        String questionText,
        String studentAnswer,
        Integer score,
        String grade,
        String whatWasMissed                        // most actionable feedback point
) {}