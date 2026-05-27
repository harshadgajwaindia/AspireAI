package com.AspireAI.backend.analyzer.controller;

import com.AspireAI.backend.analyzer.dto.JobPostingDTO;
import com.AspireAI.backend.analyzer.service.JobRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analyzer/jobs")
public class JobRecommendationController {

    private final JobRecommendationService recommendationService;

  
    @GetMapping(value = "/recommended", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JobPostingDTO>> getRecommendedJobs(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("Fetching {} recommended job postings", limit);
        List<JobPostingDTO> jobs = recommendationService.getRecentJobRecommendations(limit);
        return ResponseEntity.ok(jobs);
    }
}
