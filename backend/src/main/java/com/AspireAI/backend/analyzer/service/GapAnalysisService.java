package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.SkillGapDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.analyzer.dto.SkillProfileDTO;
import com.AspireAI.backend.analyzer.entity.CompanySkillRequirement;
import com.AspireAI.backend.analyzer.repoitory.CompanySkillRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GapAnalysisService {

    private final CompanySkillRequirementRepository requirementRepo;
    private final GapAnalysisRAGExtractor extractor;
    private final GapAnalysisCalculator calculator;

    public SkillGapReportDTO analyze(
            SkillProfileDTO userProfile,
            String targetCompany,
            String preparationType) {

        log.info("Starting Gap Analysis for targetCompany={} type={} role={}",
                targetCompany, preparationType, userProfile.targetRole());

        List<CompanySkillRequirement> staticReqs = requirementRepo
                .findByCompanyNameIgnoreCase(targetCompany);

        List<GapAnalysisRAGExtractor.DynamicSkillRequirement> dynamicReqs =
                extractor.extractDynamicRequirements(userProfile, targetCompany, preparationType);

        List<SkillGapDTO> gaps = calculator.calculateGaps(userProfile.skills(), staticReqs, dynamicReqs);

        gaps.sort((a, b) -> {
            int p1 = a.priority().equals("HIGH") ? 3 : a.priority().equals("MEDIUM") ? 2 : 1;
            int p2 = b.priority().equals("HIGH") ? 3 : b.priority().equals("MEDIUM") ? 2 : 1;
            if (p1 != p2) return Integer.compare(p2, p1);
            return Integer.compare(b.gapScore(), a.gapScore());
        });

        int readinessScore = calculator.calculateReadiness(gaps);

        log.info("Gap Analysis complete. Found {} gaps. Readiness: {}/100", gaps.size(), readinessScore);

        return new SkillGapReportDTO(
                targetCompany,
                preparationType,
                readinessScore,
                gaps,
                userProfile,
                LocalDateTime.now()
        );
    }
}