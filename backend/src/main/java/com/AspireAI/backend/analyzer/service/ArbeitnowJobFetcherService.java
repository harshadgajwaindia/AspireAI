package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.entity.JobPosting;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArbeitnowJobFetcherService {

    private final JobPostingsFilterService filterService;
    private final JobRagService jobRagService;
    private final RestClient restClient = RestClient.create();

    public record ArbeitnowJob(
            String slug,
            @JsonProperty("company_name") String companyName,
            String title,
            String description,
            String url,
            String location,
            boolean remote,
            List<String> tags,
            @JsonProperty("job_types") List<String> jobTypes,
            @JsonProperty("created_at") Object createdAt
    ) {}

    public record ArbeitnowResponse(
            List<ArbeitnowJob> data
    ) {}

    /**
     * Fetches job postings from the Arbeitnow public API, filters them via AI-Gating, and ingests them into PgVector.
     */
    public int fetchAndIngestJobs() {
        log.info("Starting Arbeitnow job fetching job...");
        String apiUrl = "https://www.arbeitnow.com/api/job-board-api";

        try {
            ArbeitnowResponse response = restClient.get()
                    .uri(apiUrl)
                    .header("Accept", "application/json")
                    .header("User-Agent", "AspireAI-JobFetcher/1.0")
                    .retrieve()
                    .body(ArbeitnowResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                log.warn("Arbeitnow API returned empty or null data.");
                return 0;
            }

            log.info("Fetched {} jobs from Arbeitnow. Beginning AI-gated ingestion...", response.data().size());
            int ingestedCount = 0;

            for (ArbeitnowJob job : response.data()) {
                // Apply AI-gating filter
                boolean isGenuine = filterService.isGenuineTechJob(job.title(), job.description());
                if (!isGenuine) {
                    log.info("Skipping job '{}' by '{}' - flagged as non-technical/spam", job.title(), job.companyName());
                    continue;
                }

                // Map to our JobPosting entity
                String joinedLocation = job.location();
                if (job.remote()) {
                    joinedLocation += " (Remote)";
                }

                String joinedSkills = job.tags() != null ? String.join(", ", job.tags()) : "";

                JobPosting posting = JobPosting.builder()
                        .companyName(job.companyName())
                        .roleTitle(job.title())
                        .location(joinedLocation)
                        .requiredSkills(joinedSkills)
                        .experienceRange("Not Specified")
                        .salaryRange("Not Specified")
                        .sourceUrl(job.url())
                        .sourcePlatform("arbeitnow")
                        .scrapedAt(LocalDateTime.now())
                        .build();

                // Ingest via JobRagService
                jobRagService.ingestJobPosting(posting, job.description());
                ingestedCount++;
            }

            log.info("Arbeitnow job fetching complete. Ingested {}/{} technical job postings.", ingestedCount, response.data().size());
            return ingestedCount;

        } catch (Exception e) {
            log.error("Failed to fetch or ingest jobs from Arbeitnow API: {}", e.getMessage(), e);
            return 0;
        }
    }
}
