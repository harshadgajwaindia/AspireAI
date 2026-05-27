package com.AspireAI.backend.interview.dto;

import java.util.UUID;


public record TurnFeedbackDTO(
        UUID turnId,
        Integer questionNumber,
        Integer score,                              
        String grade,                               
        String whatWasGood,
        String whatWasMissed,
        String idealAnswer,
        String followUpHint,                      
        QuestionDTO nextQuestion                    
) {}