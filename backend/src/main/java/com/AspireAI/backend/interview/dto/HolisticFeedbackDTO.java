package com.AspireAI.backend.interview.dto;

import java.util.List;

/**
 * Gemini's holistic session review output.
 */
public record HolisticFeedbackDTO(
        Integer overallScore,
        String overallGrade,
        String executiveSummary,
        List<String> strengths,
        List<String> areasToImprove,
        List<String> recommendedTopics,
        String confidenceLevel
) {}
