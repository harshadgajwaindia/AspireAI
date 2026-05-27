package com.AspireAI.backend.interview.dto;

import java.util.UUID;


public record QuestionDTO(
        UUID turnId,
        Integer questionNumber,
        Integer totalQuestions,
        String skillArea,
        String questionText,
        String questionType,                        
        Integer difficultyLevel,
        String ragInsight                         
) {}