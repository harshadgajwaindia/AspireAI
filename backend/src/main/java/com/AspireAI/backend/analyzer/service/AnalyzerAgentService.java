package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.ExtractedSkillsDTO;
import com.AspireAI.backend.analyzer.dto.GitHubProfileDTO;
import com.AspireAI.backend.analyzer.dto.SkillEntryDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.analyzer.dto.SkillProfileDTO;
import com.AspireAI.backend.analyzer.entity.UserProfile;
import com.AspireAI.backend.analyzer.repoitory.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * This is the entry point for the Analyzer Agent.
 * It coordinates all the sub-services in the right order.
 *
 * Step 1: Parse the PDF — fast, local
 * Step 2: Extract skills via Gemini — slow, ~3s
 * Step 3: Enrich with GitHub — medium, ~0.5s, cached
 * Step 4: Merge the two data sources
 * Step 5: Run gap analysis against target company
 * Step 6: Persist to DB so Roadmap Agent can use it
 *
 * In a future iteration, steps 2 and 3 can be parallelized
 * using CompletableFuture — but only after step 2 has extracted
 * the GitHub username. For now, sequential is fine for MVP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerAgentService {

    private final ResumeParserService resumeParser;
    private final SkillExtractionService skillExtractor;
    private final GitHubEnrichmentService githubEnricher;
    private final SkillMergeService skillMerger;
    private final GapAnalysisService gapAnalyzer;
    private final UserProfileRepository profileRepo;
    private final ObjectMapper objectMapper;

    public SkillGapReportDTO analyze(UUID userId,
                                     MultipartFile resumeFile,
                                     String targetCompany) {
        log.info("=== Analyzer Agent START | user={} target={} ===", userId, targetCompany);

        // Step 1: PDF → text
        String resumeText = resumeParser.extractText(resumeFile);
        log.info("Step 1 ✓ — {} chars extracted", resumeText.length());

        // Step 2+3: RAG retrieval + Gemini skill extraction
        // targetCompany is now passed so RAG can fetch relevant postings first
        ExtractedSkillsDTO extracted = skillExtractor.extractSkills(
                resumeText,
                targetCompany
                // skill names list is empty on first pass — RAG uses generic query
        );
        log.info("Step 2+3 ✓ — {} skills extracted (RAG-grounded)", extracted.skills().size());

        // Step 4: GitHub enrichment (cached in Redis)
        GitHubProfileDTO github = githubEnricher.enrich(extracted.githubUsername());
        log.info("Step 4 ✓ — GitHub: {} repos", github.repoCount());

        // Step 5: Merge
        List<String> skillNames = extracted.skills().stream()
                .map(SkillEntryDTO::name)
                .toList();
        SkillProfileDTO merged = skillMerger.merge(extracted, github);
        log.info("Step 5 ✓ — Skills merged");

        // Step 6+7: RAG dynamic requirements + gap analysis
        // GapAnalysisService now internally does its own RAG retrieval
        // using the same job postings for requirement extraction
        SkillGapReportDTO report = gapAnalyzer.analyze(merged, targetCompany);
        log.info("Step 6+7 ✓ — Readiness={}%, Gaps={}", report.overallReadiness(), report.topGaps().size());

        // Step 8: Persist
        persistProfile(userId, report, resumeFile.getOriginalFilename());
        log.info("Step 8 ✓ — Profile persisted");

        log.info("=== Analyzer Agent COMPLETE | Readiness={}% ===", report.overallReadiness());
        return report;
    }

    private void persistProfile(UUID userId, SkillGapReportDTO report, String filename) {
        try {
            String skillsJson  = objectMapper.writeValueAsString(report.skillProfile().skills());
            String reportJson  = objectMapper.writeValueAsString(report);

            UserProfile profile = profileRepo.findByUserId(userId)
                    .orElse(UserProfile.builder().userId(userId).build());

            profile.setTargetCompany(report.targetCompany());
            profile.setOverallReadiness(report.overallReadiness());
            profile.setGithubUsername(report.skillProfile().githubProfile().username());
            profile.setResumeUrl(filename);
            profile.setSkillsJson(skillsJson);
            profile.setGapReportJson(reportJson);

            profileRepo.save(profile);
        } catch (JsonProcessingException e) {
            log.error("Profile persist failed for userId={}: {}", userId, e.getMessage());
        }
    }
}