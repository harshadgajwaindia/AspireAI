package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.SkillEntryDTO;
import com.AspireAI.backend.analyzer.dto.SkillProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GapAnalysisRAGExtractor {

    private final JobRagService jobRagService;
    private final ChatClient chatClient;

    public List<DynamicSkillRequirement> extractDynamicRequirements(
            SkillProfileDTO profile, String targetCompany, String preparationType) {

        String skillsSummary = JobRagService.buildSkillsSummary(
                profile.skills().stream().map(SkillEntryDTO::name).toList()
        );

        String jobContext = jobRagService.retrieveJobContext(skillsSummary, targetCompany, 6);

        if (jobContext.isEmpty()) {
            log.info("No RAG context for dynamic requirements — skipping");
            return List.of();
        }

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
            
            %s
            
            JOB POSTINGS:
            %s
            """.formatted(converter.getFormat(), jobContext);

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            DynamicRequirementsWrapper wrapper = converter.convert(response);
            return wrapper != null && wrapper.requirements() != null ? wrapper.requirements() : List.of();
        } catch (Exception e) {
            log.warn("Dynamic requirement extraction failed: {}. Continuing with static only.", e.getMessage());
            return List.of();
        }
    }

    public record DynamicRequirementsWrapper(List<DynamicSkillRequirement> requirements) {}

    public record DynamicSkillRequirement(String skillName, String category, int minimumScore, String reason) {}
}
