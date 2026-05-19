package com.AspireAI.backend.analyzer.dto;

import java.util.List;

public record SkillProfileDTO(
        String targetRole,
        List<SkillEntryDTO> skills,
        List<String> projectNames,
        GitHubProfileDTO githubProfile,
        String summary
) {}