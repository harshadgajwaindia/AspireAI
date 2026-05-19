package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.SkillEntryDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.analyzer.dto.SkillProfileDTO;
import com.AspireAI.backend.analyzer.entity.CompanySkillRequirement;
import com.AspireAI.backend.analyzer.repoitory.CompanySkillRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares the student's merged skill profile against what a target
 * company actually requires. Produces a ranked list of gaps and
 * an overall readiness percentage.
 *
 * Readiness formula:
 * - Start at 100
 * - For each mandatory skill gap: subtract (gapSize * 1.5) — weighted heavier
 * - For each optional skill gap: subtract (gapSize * 1.0)
 * - Floor at 0
 *
 * This means a student missing a mandatory skill (like DSA for TCS NQT)
 * is penalized more than missing an optional one (like Docker).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GapAnalysisService {

    private final CompanySkillRequirementRepository companyRepo;
    private final JobRagService jobRagService;
    private final ChatClient chatClient;

    public SkillGapReportDTO analyze(SkillProfileDTO profile, String targetCompany) {
        log.info("RAG-enhanced gap analysis for target: {}", targetCompany);

        // ── Layer 1: Static requirements from DB ──────────────────────────
        List<CompanySkillRequirement> staticRequirements =
                companyRepo.findByCompanyName(targetCompany);

        log.info("Found {} static requirements for {}", staticRequirements.size(), targetCompany);

        // ── Layer 2: RAG-derived dynamic requirements ─────────────────────
        List<DynamicSkillRequirement> dynamicRequirements =
                extractDynamicRequirements(profile, targetCompany);

        log.info("Derived {} dynamic requirements from RAG", dynamicRequirements.size());

        // ── Merge both layers ─────────────────────────────────────────────
        Map<String, Integer> mergedRequirements = mergeRequirements(
                staticRequirements, dynamicRequirements
        );

        // ── Student skill scores ──────────────────────────────────────────
        Map<String, Integer> studentScores = profile.skills().stream()
                .collect(Collectors.toMap(
                        s -> s.name().toLowerCase(),
                        SkillEntryDTO::score,
                        Math::max
                ));

        // ── Calculate gaps ────────────────────────────────────────────────
        // Build mandatory flags from static requirements
        Set<String> mandatorySkills = staticRequirements.stream()
                .filter(CompanySkillRequirement::getIsMandatory)
                .map(r -> r.getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        List<SkillGapDTO> gaps = mergedRequirements.entrySet().stream()
                .map(entry -> {
                    String skillName = entry.getKey();
                    int required = entry.getValue();
                    int studentScore = studentScores.getOrDefault(skillName.toLowerCase(), 0);
                    int gap = Math.max(0, required - studentScore);
                    boolean mandatory = mandatorySkills.contains(skillName.toLowerCase());
                    return new SkillGapDTO(
                            skillName, studentScore, required, gap,
                            classifyPriority(gap, mandatory)
                    );
                })
                .filter(g -> g.gapSize() > 0)
                .sorted(Comparator.comparingInt(SkillGapDTO::gapSize).reversed())
                .toList();

        int readiness = calculateReadiness(gaps, mergedRequirements, mandatorySkills);

        log.info("Gap analysis complete. Readiness={}%, Gaps={}", readiness, gaps.size());

        return new SkillGapReportDTO(
                targetCompany, readiness, gaps, profile, LocalDateTime.now()
        );
    }

    // ── RAG-derived dynamic requirements ──────────────────────────────────

    /**
     * Uses RAG + LLM to extract skill requirements from real job postings.
     *
     * We ask Gemini: "Given these real job postings, what skills are required
     * and at what level?" — this extracts structured data from unstructured
     * job description text, dynamically.
     */
    private List<DynamicSkillRequirement> extractDynamicRequirements(
            SkillProfileDTO profile, String targetCompany) {

        String skillsSummary = JobRagService.buildSkillsSummary(
                profile.skills().stream().map(SkillEntryDTO::name).toList()
        );

        String jobContext = jobRagService.retrieveJobContext(
                skillsSummary, targetCompany, 6
        );

        if (jobContext.isEmpty()) {
            log.info("No RAG context for dynamic requirements — skipping");
            return List.of();
        }

        // Ask Gemini to extract structured requirements from the job postings
        BeanOutputConverter<DynamicRequirementsWrapper> converter =
                new BeanOutputConverter<>(DynamicRequirementsWrapper.class);

        String prompt = """
            Based ONLY on the job postings below, extract the technical skills required.
            Do NOT use your general knowledge — only what these actual postings mention.
            
            For each skill found, estimate a minimum score (0-100) based on how
            prominently it's mentioned:
            - Mentioned in every posting → 65-75
            - Mentioned in most postings → 50-65
            - Mentioned in some postings → 40-50
            
            {format}
            
            JOB POSTINGS:
            {job_context}
            """;

        try {
            String response = chatClient.prompt()
                    .user(u -> u
                            .text(prompt)
                            .param("format", converter.getFormat())
                            .param("job_context", jobContext))
                    .call()
                    .content();

            DynamicRequirementsWrapper wrapper = converter.convert(response);
            return wrapper != null && wrapper.requirements() != null
                    ? wrapper.requirements()
                    : List.of();

        } catch (Exception e) {
            log.warn("Dynamic requirement extraction failed: {}. Continuing with static only.",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Merges static DB requirements with RAG-derived dynamic requirements.
     * Returns a unified map: skillName → required score.
     */
    private Map<String, Integer> mergeRequirements(
            List<CompanySkillRequirement> staticReqs,
            List<DynamicSkillRequirement> dynamicReqs) {

        // Start with static requirements
        Map<String, Integer> merged = new LinkedHashMap<>();
        for (CompanySkillRequirement req : staticReqs) {
            merged.put(req.getSkillName(), req.getMinimumScore());
        }

        // Merge dynamic requirements
        for (DynamicSkillRequirement dyn : dynamicReqs) {
            String key = dyn.skillName();
            if (merged.containsKey(key)) {
                // Both layers agree on this skill → take the higher requirement
                merged.put(key, Math.max(merged.get(key), dyn.minimumScore()));
            } else {
                // New skill from RAG — add it with a slight discount (not yet confirmed)
                // We multiply by 0.85 to signal it's a trend, not a confirmed requirement
                merged.put(key, (int) (dyn.minimumScore() * 0.85));
            }
        }

        return merged;
    }

    // ── Scoring helpers ────────────────────────────────────────────────────

    private String classifyPriority(int gapSize, boolean mandatory) {
        if (mandatory && gapSize > 20) return "HIGH";
        if (gapSize > 30) return "HIGH";
        if (gapSize > 15) return "MEDIUM";
        return "LOW";
    }

    private int calculateReadiness(List<SkillGapDTO> gaps,
                                   Map<String, Integer> requirements,
                                   Set<String> mandatorySkills) {
        if (requirements.isEmpty()) return 50;

        double totalPenalty = 0;
        double maxPenalty = 0;

        for (SkillGapDTO gap : gaps) {
            boolean mandatory = mandatorySkills.contains(gap.skillName().toLowerCase());
            double weight = mandatory ? 1.5 : 1.0;
            totalPenalty += gap.gapSize() * weight;
        }

        for (Map.Entry<String, Integer> req : requirements.entrySet()) {
            boolean mandatory = mandatorySkills.contains(req.getKey().toLowerCase());
            double weight = mandatory ? 1.5 : 1.0;
            maxPenalty += req.getValue() * weight;
        }

        if (maxPenalty == 0) return 100;
        return Math.max(0, Math.min(100, (int)(100 * (1 - totalPenalty / maxPenalty))));
    }

    // ── Inner records for LLM structured output ───────────────────────────

    /** Wrapper needed because BeanOutputConverter can't target a raw List */
    public record DynamicRequirementsWrapper(
            List<DynamicSkillRequirement> requirements
    ) {}

    public record DynamicSkillRequirement(
            String skillName,
            String category,
            int minimumScore,
            String reason  // e.g. "Mentioned in 4/5 job postings"
    ) {}
}