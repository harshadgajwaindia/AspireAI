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


@Slf4j
@RestController
@RequestMapping("/api/v1/analyzer")
@RequiredArgsConstructor
public class AnalyzerController {

    private final AnalyzerAgentService analyzerAgent;
    private final ArbeitnowJobFetcherService jobFetcherService;

  
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

   
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analyzer Agent is running");
    }
}