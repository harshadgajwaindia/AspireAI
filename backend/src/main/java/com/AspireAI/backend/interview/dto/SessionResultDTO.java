package com.AspireAI.backend.interview.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record SessionResultDTO(
        UUID sessionId,
        String targetCompany,
        String interviewType,
        Integer totalQuestions,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Integer overallScore,
        OverallFeedbackDTO overallFeedback,
        List<TurnSummaryDTO> turnSummaries,
        List<String> suggestedRoadmapAdjustments    
) {}