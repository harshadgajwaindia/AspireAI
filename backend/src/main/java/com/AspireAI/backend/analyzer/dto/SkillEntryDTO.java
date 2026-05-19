package com.AspireAI.backend.analyzer.dto;

public record SkillEntryDTO(
        String name,        
        String category,   
        int score,          
        String evidence    
) {}