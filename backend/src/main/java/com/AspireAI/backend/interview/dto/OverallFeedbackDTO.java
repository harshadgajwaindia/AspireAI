package com.AspireAI.backend.interview.dto;

import java.util.List;



public record OverallFeedbackDTO(
        Integer overallScore,                       
        String overallGrade,                        
        String executiveSummary,                    
        List<String> strengths,                    
        List<String> areasToImprove,                
        List<String> recommendedTopics,            
        String confidenceLevel                      
) {}
