package com.AspireAI.backend.analyzer.controller;

import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.analyzer.service.AnalyzerAgentService;
import com.AspireAI.backend.analyzer.service.ArbeitnowJobFetcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for the Analyzer Agent.
 *
 * POST /api/v1/analyzer/analyze
 * - Multipart request: resume file + targetCompany + userId
 * - Returns SkillGapReportDTO as JSON
 *
 * consumes = MULTIPART_FORM_DATA_VALUE because we're receiving a file.
 * In production you'd also have auth middleware that extracts userId
 * from the JWT token instead of receiving it as a parameter.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analyzer")
@RequiredArgsConstructor
public class AnalyzerController {

    private final AnalyzerAgentService analyzerAgent;
    private final ArbeitnowJobFetcherService jobFetcherService;

    /**
     * Main endpoint — triggers the full analysis pipeline.
     *
     * Example curl:
     * curl -X POST http://localhost:8080/api/v1/analyzer/analyze \
     *   -F "resume=@myresume.pdf" \
     *   -F "targetCompany=TCS Digital" \
     *   -F "userId=550e8400-e29b-41d4-a716-446655440000"
     */
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SkillGapReportDTO> analyze(
            @RequestPart("resume") MultipartFile resumeFile,
            @RequestParam("targetCompany") String targetCompany,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "preparationType", defaultValue = "COMPANY") String preparationType) {
        
        System.out.println("ANALYZE REQUEST RECEIVED for userId: " + userId + ", type: " + preparationType);

        log.info("Received analyze request for userId={} targetCompany={} preparationType={}",
                userId, targetCompany, preparationType);

        SkillGapReportDTO report = analyzerAgent.analyze(userId, resumeFile, targetCompany, preparationType);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/jobs/fetch-arbeitnow")
    public ResponseEntity<String> fetchArbeitnowJobs() {
        log.info("REST request to fetch and ingest Arbeitnow jobs");
        int count = jobFetcherService.fetchAndIngestJobs();
        return ResponseEntity.ok("Successfully fetched and ingested " + count + " technical job postings from Arbeitnow.");
    }

    /**
     * Quick health check — useful during development to confirm the
     * service is up before sending a real resume.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analyzer Agent is running");
    }
}