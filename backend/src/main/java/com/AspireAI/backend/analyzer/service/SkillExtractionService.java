package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.ExtractedSkillsDTO;
import com.AspireAI.backend.analyzer.exception.AnalyzerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * Sends the resume text to Gemini and gets back structured skill data.
 *
 * How BeanOutputConverter works:
 * 1. It inspects ExtractedSkillsDTO and generates a JSON schema from it
 * 2. That schema is injected into the prompt as {format}
 * 3. Gemini sees the schema and returns JSON matching it
 * 4. BeanOutputConverter deserializes the JSON back to ExtractedSkillsDTO
 *
 * This is far more reliable than asking Gemini to "return JSON" without
 * a schema, because it tells Gemini EXACTLY what fields you expect.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final ChatClient chatClient;
    private final JobRagService jobRagService;  // ← NEW dependency

    /**
     * RAG-augmented prompt template.
     *
     * The {job_context} block is what RAG adds — it's the retrieved
     * job descriptions. When this block is populated, Gemini grades
     * the student's skills against actual requirements instead of guessing.
     *
     * When {job_context} is empty (no postings in DB yet), Gemini falls
     * back to general knowledge — same as v1 behavior.
     */
    private static final String EXTRACTION_PROMPT = """
        You are an expert technical recruiter for Indian CS campus placements.
        You must analyze a resume and score skills based on EVIDENCE, not just mentions.
 
        {job_context}
 
        IMPORTANT: If real job postings are provided above, use them to calibrate
        your scoring. Score skills relative to what those actual job postings require.
        If a skill appears frequently in the job postings but is weak on the resume,
        that is a high-priority gap. If the job postings don't mention it, it's lower priority.
 
        SCORING RULES:
        - 80-100: Skill has dedicated project with clear implementation details
        - 60-79: Skill used in project but details are vague
        - 40-59: Listed in skills section but no project evidence
        - 0-39:  Barely mentioned or appears copy-pasted
 
        CATEGORIES: dsa | backend | frontend | database | devops | core_cs
 
        For each skill, the "evidence" field must quote the specific line from
        the resume that proves this skill. If job postings are available and reveal
        this skill is in demand, append "[In demand: X job posts mention this]"
        to the evidence string.
 
        For targetRole: infer from resume content.
        For githubUsername: extract only the username, not the full URL.
 
        {format}
 
        RESUME:
        {resume}
        """;

    /**
     * Main entry point. Now takes targetCompany to enable RAG retrieval.
     *
     * @param resumeText    extracted text from the PDF
     * @param targetCompany e.g. "TCS Digital" — used to retrieve relevant job posts
     * @param skillNames    quick list of skills found in first pass (optional, improves retrieval)
     */
    public ExtractedSkillsDTO extractSkills(String resumeText,
                                            String targetCompany,
                                            java.util.List<String> skillNames) {
        log.info("RAG-augmented skill extraction for target: {}", targetCompany);

        // ── Step 1: Retrieve RAG context ──────────────────────────────────
        // Build a query from the skill names we already know + target company
        String skillsSummary = skillNames.isEmpty()
                ? "software engineering Java DSA Spring Boot"  // fallback query
                : JobRagService.buildSkillsSummary(skillNames);

        String jobContext = jobRagService.retrieveJobContext(
                skillsSummary, targetCompany, 5
        );

        if (jobContext.isEmpty()) {
            log.info("No RAG context available for {}. Proceeding with general knowledge.", targetCompany);
        } else {
            log.info("Injecting RAG context ({} chars) into prompt", jobContext.length());
        }

        // ── Step 2: Build RAG-augmented prompt ────────────────────────────
        BeanOutputConverter<ExtractedSkillsDTO> converter =
                new BeanOutputConverter<>(ExtractedSkillsDTO.class);

        // Format the job context block — empty string if no data
        String jobContextBlock = jobContext.isEmpty()
                ? "Note: No recent job posting data available for this company. "
                + "Use general knowledge about Indian placement requirements."
                : jobContext;

        try {
            String response = chatClient.prompt()
                    .user(u -> u
                            .text(EXTRACTION_PROMPT)
                            .param("job_context", jobContextBlock)
                            .param("format", converter.getFormat())
                            .param("resume", resumeText))
                    .call()
                    .content();

            ExtractedSkillsDTO result = converter.convert(response);

            if (result == null || result.skills() == null || result.skills().isEmpty()) {
                throw new AnalyzerException("Gemini returned empty skill data.");
            }

            log.info("Extracted {} skills. RAG grounded: {}",
                    result.skills().size(), !jobContext.isEmpty());

            return result;

        } catch (Exception e) {
            log.error("Skill extraction failed", e);
            throw new AnalyzerException("AI skill extraction failed: " + e.getMessage());
        }
    }

    /**
     * Overload for backward compatibility — when no skill names are known yet.
     * Passes an empty list and lets the RAG service use a generic query.
     */
    public ExtractedSkillsDTO extractSkills(String resumeText, String targetCompany) {
        return extractSkills(resumeText, targetCompany, java.util.List.of());
    }
}