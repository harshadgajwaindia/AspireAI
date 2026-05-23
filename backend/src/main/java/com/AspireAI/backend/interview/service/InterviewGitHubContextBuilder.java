package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.analyzer.dto.GitHubProfileDTO;
import com.AspireAI.backend.analyzer.entity.UserProfile;
import com.AspireAI.backend.analyzer.repoitory.UserProfileRepository;
import com.AspireAI.backend.analyzer.service.GitHubEnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewGitHubContextBuilder {
    private final UserProfileRepository profileRepo;
    private final GitHubEnrichmentService githubEnricher;

    public String buildGitHubContext(UUID userId) {
        try {
            UserProfile profile = profileRepo.findByUserId(userId).orElse(null);
            if (profile != null && profile.getGithubUsername() != null && !profile.getGithubUsername().isBlank()) {
                GitHubProfileDTO gitProfile = githubEnricher.enrich(profile.getGithubUsername());
                if (gitProfile.hasData()) {
                    return String.format(
                        "GitHub Username: %s | Total Repositories: %d | Total Stars: %d | Language Breakdown: %s",
                        gitProfile.username(), gitProfile.repoCount(), gitProfile.totalStars(),
                        gitProfile.languageFrequency().keySet().toString()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to build GitHub context for user {}: {}", userId, e.getMessage());
        }
        return "";
    }
}
