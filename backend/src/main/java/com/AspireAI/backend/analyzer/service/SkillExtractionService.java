package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.ExtractedSkillsDTO;
import com.AspireAI.backend.analyzer.exception.AnalyzerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final ChatClient chatClient;
    private final JobRagService jobRagService;

    public ExtractedSkillsDTO extractSkills(String resumeText, String targetCompany, List<String> skillNames) {
        log.info("RAG-augmented skill extraction for target: {}", targetCompany);

        String skillsSummary = skillNames.isEmpty() ? "software engineering Java DSA Spring Boot"
                : JobRagService.buildSkillsSummary(skillNames);

        String jobContext = jobRagService.retrieveJobContext(skillsSummary, targetCompany, 5);
        if (jobContext.isEmpty()) {
            log.info("No RAG context available for {}. Proceeding with general knowledge.", targetCompany);
        } else {
            log.info("Injecting RAG context ({} chars) into prompt", jobContext.length());
        }

        BeanOutputConverter<ExtractedSkillsDTO> converter = new BeanOutputConverter<>(ExtractedSkillsDTO.class);
        String jobContextBlock = jobContext.isEmpty()
                ? "Note: No recent job posting data available for this company. Use general knowledge about Indian placement requirements."
                : jobContext;

        try {
            String response = chatClient.prompt()
                    .user(u -> u.text(AnalyzerPromptTemplates.EXTRACTION_PROMPT)
                            .param("job_context", jobContextBlock)
                            .param("format", converter.getFormat())
                            .param("resume", resumeText))
                    .call().content();

            ExtractedSkillsDTO result = converter.convert(response);
            if (result == null || result.skills() == null || result.skills().isEmpty()) {
                throw new AnalyzerException("Gemini returned empty skill data.");
            }

            log.info("Extracted {} skills. RAG grounded: {}", result.skills().size(), !jobContext.isEmpty());
            return result;

        } catch (Exception e) {
            log.error("Skill extraction failed", e);
            throw new AnalyzerException("AI skill extraction failed: " + e.getMessage());
        }
    }


    public ExtractedSkillsDTO extractSkills(String resumeText, String targetCompany) {
        return extractSkills(resumeText, targetCompany, List.of());
    }
}