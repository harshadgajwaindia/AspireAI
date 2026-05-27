package com.AspireAI.backend.interview.dto;

import java.util.UUID;


public record StartSessionRequestDTO(
        UUID userId,
        String targetCompany,                    
        String interviewType,                        
        Integer totalQuestions,                     
        String skillGapJson                          
) {}