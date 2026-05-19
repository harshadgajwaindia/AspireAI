package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.GitHubProfileDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fetches public GitHub data for a username.
 *
 * @Cacheable("github-profiles") stores results in Redis for 6 hours.
 * Why? Because:
 * 1. GitHub rate-limits unauthenticated requests to 60/hour
 * 2. A student's GitHub profile doesn't change while they're uploading
 *    resumes multiple times to test different wordings
 *
 * The cache key is the username. If the result is empty (user not found),
 * we still cache it so we don't hammer GitHub with 404s.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubEnrichmentService {

    private final RestClient githubRestClient;

    @Value("${github.api.token:}")
    private String githubToken; // optional — gives 5000 req/hr vs 60

    @Cacheable(value = "github-profiles", key = "#username",
            condition = "#username != null && !#username.isBlank()")
    public GitHubProfileDTO enrich(String username) {
        if (username == null || username.isBlank()) {
            log.info("No GitHub username found in resume — skipping enrichment");
            return GitHubProfileDTO.empty();
        }

        try {
            log.info("Fetching GitHub profile for: {}", username);
            List<GitHubRepoResponse> repos = fetchRepos(username);

            if (repos == null || repos.isEmpty()) {
                return GitHubProfileDTO.empty();
            }

            // Count language frequencies, excluding forked repos
            // (forked repos don't prove the student wrote that language)
            Map<String, Integer> languageFreq = repos.stream()
                    .filter(r -> !r.fork() && r.language() != null)
                    .collect(Collectors.groupingBy(
                            GitHubRepoResponse::language,
                            Collectors.summingInt(r -> 1)
                    ));

            int totalStars = repos.stream()
                    .mapToInt(GitHubRepoResponse::stargazersCount)
                    .sum();

            int totalForks = repos.stream()
                    .mapToInt(GitHubRepoResponse::forksCount)
                    .sum();

            int ownRepoCount = (int) repos.stream()
                    .filter(r -> !r.fork())
                    .count();

            return new GitHubProfileDTO(
                    username, ownRepoCount, totalStars, totalForks, languageFreq
            );

        } catch (RestClientException e) {
            // GitHub username was on resume but profile doesn't exist or API failed
            // We return empty instead of failing the whole analysis
            log.warn("GitHub enrichment failed for {}: {}", username, e.getMessage());
            return GitHubProfileDTO.empty();
        }
    }

    private List<GitHubRepoResponse> fetchRepos(String username) {
        var requestSpec = githubRestClient.get()
                .uri("/users/{username}/repos?per_page=50&sort=updated", username);

        // Add auth token if configured
        if (githubToken != null && !githubToken.isBlank()) {
            requestSpec = requestSpec.header("Authorization", "Bearer " + githubToken);
        }

        return requestSpec
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Internal record for deserializing GitHub API repo response.
     * @JsonIgnoreProperties(ignoreUnknown=true) means we only extract
     * the fields we care about and ignore the other 40+ fields GitHub returns.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubRepoResponse(
            String name,
            String language,
            boolean fork,
            @JsonProperty("stargazers_count") int stargazersCount,
            @JsonProperty("forks_count") int forksCount
    ) {}
}