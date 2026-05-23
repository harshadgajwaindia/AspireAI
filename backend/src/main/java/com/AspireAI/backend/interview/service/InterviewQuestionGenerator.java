package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.GeneratedQuestionDTO;
import com.AspireAI.backend.interview.entity.InterviewQuestion;
import com.AspireAI.backend.interview.entity.InterviewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionGenerator {

    private final ChatClient chatClient;
    private final InterviewRagService ragService;

    public GeneratedQuestionDTO generateQuestion(
            InterviewSession session,
            int questionNumber,
            List<String> alreadyAskedTexts,
            String skillGapContext,
            String githubContext) {

        String targetSkill = pickSkillArea(session, questionNumber, skillGapContext);
        int difficulty = mapQuestionToDifficulty(questionNumber, session.getTotalQuestions());

        List<InterviewQuestion> candidates = ragService.findRelevantQuestions(
                targetSkill, session.getTargetCompany(), difficulty, alreadyAskedTexts
        );

        String interviewContext = ragService.retrieveInterviewContext(
                session.getTargetCompany(), session.getInterviewType().name()
        );

        String ragInsight = ragService.buildRagInsight(targetSkill, session.getTargetCompany());

        String candidateContext = candidates.isEmpty() ? ""
                : "QUESTION BANK CANDIDATES:\n" + candidates.stream().limit(3)
                        .map(q -> "- " + q.getQuestionText()).collect(Collectors.joining("\n"));

        BeanOutputConverter<GeneratedQuestionDTO> converter =
                new BeanOutputConverter<>(GeneratedQuestionDTO.class);

        String prompt = InterviewPromptTemplates.QUESTION_PROMPT.formatted(
                session.getInterviewType().name().toLowerCase(), session.getTargetCompany(),
                questionNumber, session.getTotalQuestions(),
                skillGapContext != null ? skillGapContext : "General CS fresher",
                githubContext != null && !githubContext.isBlank() ? githubContext : "No public GitHub data available.",
                interviewContext.isEmpty() ? "No specific data available." : interviewContext,
                candidateContext, questionNumber, targetSkill, difficulty,
                alreadyAskedTexts.isEmpty() ? "none" : String.join(", ", alreadyAskedTexts),
                converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            GeneratedQuestionDTO generated = converter.convert(response);
            if (generated == null) throw new RuntimeException("Gemini returned null question");
            log.info("Generated question #{} for skill={} difficulty={}", questionNumber, targetSkill, difficulty);
            return generated;
        } catch (Exception e) {
            log.error("Question generation failed: {}. Using fallback.", e.getMessage());
            return fallbackQuestion(targetSkill, questionNumber);
        }
    }

    private String pickSkillArea(InterviewSession session, int questionNumber, String skillGapContext) {
        if (session.getInterviewType() == InterviewSession.InterviewType.HR) return "HR-Behavioral";
        if (session.getInterviewType() == InterviewSession.InterviewType.MIXED
                && questionNumber > session.getTotalQuestions() - 2) return "HR-Behavioral";
        if (skillGapContext == null || skillGapContext.isBlank()) return "DSA-General";
        return "DSA-" + (questionNumber % 3 == 0 ? "Dynamic Programming"
                : questionNumber % 3 == 1 ? "Trees & Graphs" : "Arrays & Strings");
    }

    private int mapQuestionToDifficulty(int questionNumber, int total) {
        double ratio = (double) questionNumber / total;
        if (ratio <= 0.33) return 2;
        if (ratio <= 0.66) return 3;
        return 4;
    }

    private GeneratedQuestionDTO fallbackQuestion(String skillArea, int questionNumber) {
        return new GeneratedQuestionDTO(
                "Explain the key concepts of " + skillArea + " and give a real-world example.",
                "CONCEPTUAL", skillArea, 2, null
        );
    }
}
