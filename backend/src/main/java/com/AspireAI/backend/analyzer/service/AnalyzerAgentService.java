package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.*;
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

    public SkillGapReportDTO analyze(UUID userId, MultipartFile resumeFile, String targetCompany, String preparationType) {
        log.info("=== Analyzer Agent START | user={} target={} type={} ===", userId, targetCompany, preparationType);

        String resumeText = resumeParser.extractText(resumeFile);
        ExtractedSkillsDTO extracted = skillExtractor.extractSkills(resumeText, targetCompany);
        GitHubProfileDTO github = githubEnricher.enrich(extracted.githubUsername());
        SkillProfileDTO merged = skillMerger.merge(extracted, github);
        SkillGapReportDTO report = gapAnalyzer.analyze(merged, targetCompany, preparationType);

        persistProfile(userId, report, resumeFile.getOriginalFilename());
        log.info("=== Analyzer Agent COMPLETE | Readiness={}% ===", report.overallReadiness());
        return report;
    }

    public SkillGapReportDTO analyze(UUID userId, MultipartFile resumeFile, String targetCompany) {
        return analyze(userId, resumeFile, targetCompany, "COMPANY");
    }

    private void persistProfile(UUID userId, SkillGapReportDTO report, String filename) {
        try {
            UserProfile profile = profileRepo.findByUserId(userId).orElse(UserProfile.builder().userId(userId).build());
            profile.setTargetCompany(report.targetCompany());
            profile.setOverallReadiness(report.overallReadiness());
            profile.setGithubUsername(report.skillProfile().githubProfile().username());
            profile.setResumeUrl(filename);
            profile.setSkillsJson(objectMapper.writeValueAsString(report.skillProfile().skills()));
            profile.setGapReportJson(objectMapper.writeValueAsString(report));
            profileRepo.save(profile);
        } catch (JsonProcessingException e) {
            log.error("Profile persist failed for userId={}: {}", userId, e.getMessage());
        }
    }
}