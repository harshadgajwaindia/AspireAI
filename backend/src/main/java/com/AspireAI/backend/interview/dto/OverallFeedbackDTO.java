package com.AspireAI.backend.interview.dto;

import java.util.List;

/**
 * Holistic session feedback from Gemini — generated at session end
 * after reviewing ALL turns together.
 */
public record OverallFeedbackDTO(
        Integer overallScore,                       // 0-100
        String overallGrade,                        // "Ready", "Almost Ready", "Needs Work"
        String executiveSummary,                    // 2-3 sentence verdict
        List<String> strengths,                     // what the student did consistently well
        List<String> areasToImprove,                // specific, actionable gaps
        List<String> recommendedTopics,             // what to study before next interview
        String confidenceLevel                      // "High", "Medium", "Low" — communication quality
) {}
