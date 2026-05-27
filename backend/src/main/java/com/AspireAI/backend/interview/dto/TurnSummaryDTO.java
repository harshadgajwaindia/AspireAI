package com.AspireAI.backend.interview.dto;


public record TurnSummaryDTO(
        Integer questionNumber,
        String skillArea,
        String questionText,
        String studentAnswer,
        Integer score,
        String grade,
        String whatWasMissed                        
) {}