package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.entity.JobPosting;
import com.AspireAI.backend.config.ApiKeyRotator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingsFilterService {

    private final ApiKeyRotator apiKeyRotator;

    public record FilterResult(boolean isGenuineTechJob, String reason) {}

    /**
     * Uses Gemini via a rotated API key to verify if the job posting is a genuine tech/software engineering role.
     * Rejects generic/spam/non-technical postings.
     */
    public boolean isGenuineTechJob(String title, String description) {
        log.info("AI-Gating Check for job title: '{}'", title);

        ChatClient chatClient = apiKeyRotator.getChatClient();
        BeanOutputConverter<FilterResult> converter = new BeanOutputConverter<>(FilterResult.class);

        String prompt = """
            You are an AI-powered job posting gatekeeper for a technical software engineering preparation platform.
            Your task is to analyze the job title and description and determine if it is a genuine, active software engineering or technical IT job posting (e.g., Backend, Frontend, Fullstack, DevOps, Mobile, QA/Testing, AI/ML, Data Engineer, Cyber Security, etc.).
            
            Strictly reject:
            - Spam, scam, or fake job postings.
            - Positions that are completely non-technical (e.g., generic HR, digital marketing, sales, content writers, finance, administrative).
            - Pure management roles without technical CS requirements (e.g., generic project managers with no coding).
            
            {format}
            
            Job Title: {title}
            Job Description: {description}
            """;

        try {
            String response = chatClient.prompt()
                    .user(u -> u
                            .text(prompt)
                            .param("format", converter.getFormat())
                            .param("title", title)
                            .param("description", description != null ? description.substring(0, Math.min(2000, description.length())) : ""))
                    .call()
                    .content();

            FilterResult result = converter.convert(response);
            if (result != null) {
                log.info("AI-Gating Decision for '{}': genuine={}, reason='{}'", title, result.isGenuineTechJob(), result.reason());
                return result.isGenuineTechJob();
            }
        } catch (Exception e) {
            log.error("AI-Gating filtering failed, defaulting to accept to avoid blocking ingestion. Error: {}", e.getMessage());
            return true; // Default fallback to avoid blocking pipeline completely
        }

        return true;
    }
}
