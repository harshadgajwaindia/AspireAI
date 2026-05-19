package com.AspireAI.backend.analyzer.dto;

import java.util.List;

public record ExtractedSkillsDTO(
        String githubUsername,      
        String linkedinUrl,
        String targetRole,        
        List<SkillEntryDTO> skills, 
        List<String> projectNames,  
        String summary              
) {}