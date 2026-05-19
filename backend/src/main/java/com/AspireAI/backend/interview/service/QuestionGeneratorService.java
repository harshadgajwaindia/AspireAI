package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.*;
import com.AspireAI.backend.interview.entity.InterviewQuestion;
import com.AspireAI.backend.interview.entity.InterviewSession;
import com.AspireAI.backend.interview.entity.InterviewTurn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The brain of the Mock Interviewer — handles two LLM operations:
 *
 * 1. QUESTION GENERATION
 *    Uses RAG to retrieve a candidate question from the question bank,
 *    then Gemini either uses it verbatim or generates a fresh variation
 *    tailored to the student's specific gap profile.
 *
 *    Why generate instead of just retrieval?
 *    Because RAG gives us the RIGHT TOPIC but Gemini personalizes the wording:
 *    "Given your project uses Spring Boot, explain how you would implement
 *     JWT authentication in it." — more contextual than a canned question.
 *
 * 2. ANSWER EVALUATION
 *    Student submits an answer → Gemini evaluates it against the ideal answer
 *    and the specific question context, returning structured feedback.
 *    This is the most valuable feature — real-time feedback most students never get.
 *
 * 3. HOLISTIC FEEDBACK
 *    After all turns are complete, Gemini reviews the FULL conversation and
 *    gives an overall performance summary — not just per-question scores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGeneratorService {

    private final ChatClient chatClient;
    private final InterviewRagService ragService;

    // ── Question Generation ────────────────────────────────────────────────

    /**
     * Generates the next question for a session turn.
     *
     * Strategy:
     * 1. Pick the skill area to focus on (from gap report or rotation)
     * 2. RAG: find relevant questions from the bank
     * 3. Gemini: generate a personalized variation of the best candidate
     */
    public GeneratedQuestionDTO generateQuestion(
            InterviewSession session,
            int questionNumber,
            List<String> alreadyAskedTexts,
            String skillGapContext,
            String githubContext) {

        // Determine skill area to focus on for this question
        String targetSkill = pickSkillArea(session, questionNumber, skillGapContext);
        int difficulty = mapQuestionToDifficulty(questionNumber, session.getTotalQuestions());

        // RAG: retrieve candidate questions
        List<InterviewQuestion> candidates = ragService.findRelevantQuestions(
                targetSkill,
                session.getTargetCompany(),
                difficulty,
                alreadyAskedTexts
        );

        // RAG job posting context
        String interviewContext = ragService.retrieveInterviewContext(
                session.getTargetCompany(), session.getInterviewType().name()
        );

        String ragInsight = ragService.buildRagInsight(targetSkill, session.getTargetCompany());

        // Format candidate questions for the prompt
        String candidateContext = candidates.isEmpty() ? ""
                : "QUESTION BANK CANDIDATES (use these as inspiration, not verbatim):\n" +
                candidates.stream()
                        .limit(3)
                        .map(q -> "- " + q.getQuestionText())
                        .collect(Collectors.joining("\n"));

        // Call Gemini to generate a personalized question
        BeanOutputConverter<GeneratedQuestionDTO> converter =
                new BeanOutputConverter<>(GeneratedQuestionDTO.class);

        String prompt = """
            You are a senior technical interviewer conducting a %s interview targeted towards %s.
            This is question %d of %d.

            STUDENT'S SKILL GAP CONTEXT:
            %s

            STUDENT'S PUBLIC GITHUB PROFILE HIGHLIGHTS (weave these in constructively if relevant to tech/languages):
            %s

            RECENT PATHWAY/COMPANY INTERVIEW PATTERNS (from live job postings):
            %s

            %s

            Generate question %d for skill area: %s (difficulty %d/5).

            Rules:
            - Question must be highly engaging, scenario-based, and practical.
            - If student's GitHub highlights show active languages/projects matching or relating to the skill area, weave it in (e.g. "I noticed your GitHub has repositories in Java. How would you...").
            - For TECHNICAL: present a creative real-world tech scenario with a twist or edge case.
            - For BEHAVIORAL: use STAR format but pose challenging, high-stakes situations.
            - For CODING: describe an interesting, non-standard problem to solve.
            - Do NOT ask a question already asked this session
            - Already asked topics: %s
            - Keep question under 80 words
            - Give it personality, like a seasoned tech mentor challenging the candidate.

            %s
            """.formatted(
                session.getInterviewType().name().toLowerCase(),
                session.getTargetCompany(),
                questionNumber, session.getTotalQuestions(),
                skillGapContext != null ? skillGapContext : "General CS fresher",
                githubContext != null && !githubContext.isBlank() ? githubContext : "No public GitHub data available.",
                interviewContext.isEmpty() ? "No specific data available." : interviewContext,
                candidateContext,
                questionNumber, targetSkill, difficulty,
                alreadyAskedTexts.isEmpty() ? "none" : String.join(", ", alreadyAskedTexts),
                converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            GeneratedQuestionDTO generated = converter.convert(response);

            if (generated == null) throw new RuntimeException("Gemini returned null question");

            log.info("Generated question #{} for skill={} difficulty={}",
                    questionNumber, targetSkill, difficulty);
            return generated;

        } catch (Exception e) {
            log.error("Question generation failed: {}. Using fallback.", e.getMessage());
            return fallbackQuestion(targetSkill, questionNumber);
        }
    }

    // ── Answer Evaluation ──────────────────────────────────────────────────

    /**
     * Evaluates a student's answer and returns structured feedback.
     *
     * This is called immediately after the student submits — the ~3-5 second
     * wait while Gemini processes is shown as a "Evaluating…" animation.
     *
     * The idealAnswerHint from the question bank is included in the prompt
     * so Gemini knows what a correct answer looks like and can give
     * specific, accurate feedback rather than hedging.
     */
    public AnswerEvaluationDTO evaluateAnswer(
            InterviewTurn turn,
            String studentAnswer,
            String idealAnswerHint) {

        BeanOutputConverter<AnswerEvaluationDTO> converter =
                new BeanOutputConverter<>(AnswerEvaluationDTO.class);

        String prompt = """
            You are evaluating a mock interview answer for an Indian CS campus placement.

            QUESTION: %s
            QUESTION TYPE: %s
            SKILL AREA: %s
            DIFFICULTY: %d/5

            STUDENT'S ANSWER:
            %s

            IDEAL ANSWER HINTS (what a strong answer should cover):
            %s

            Evaluate the answer strictly but fairly. Provide highly engaging, constructive feedback.
            Adopt a constructive but playful tone, similar to a seasoned tech mentor who challenges candidates to think deeper.
            Indian CS students preparing for placements need ACTIONABLE, insightful feedback.

            Scoring guide:
            9-10: Covers all key concepts, clear communication, good examples
            7-8:  Covers most concepts, minor gaps or unclear explanation
            5-6:  Covers some concepts but missing important points
            3-4:  Partially relevant answer, significant gaps
            1-2:  Largely incorrect or irrelevant
            0:    No meaningful answer

            Grade labels: "Excellent" (9-10), "Good" (7-8),
                          "Needs Work" (5-6), "Insufficient" (0-4)

            For "idealAnswer": write a concise model answer (under 150 words).
            For "whatWasMissed": be specific about WHAT was missing, not just "explain more".
            For "followUpHint": suggest what a real interviewer would probe next.

            %s
            """.formatted(
                turn.getQuestion(),
                turn.getQuestionType(),
                turn.getSkillArea(),
                turn.getDifficultyLevel(),
                studentAnswer,
                idealAnswerHint != null ? idealAnswerHint : "Standard CS knowledge expected.",
                converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            AnswerEvaluationDTO eval = converter.convert(response);

            if (eval == null) throw new RuntimeException("Null evaluation from Gemini");

            log.info("Evaluated turn #{}: score={}", turn.getQuestionNumber(), eval.score());
            return eval;

        } catch (Exception e) {
            log.error("Answer evaluation failed: {}", e.getMessage());
            // Fallback evaluation so session doesn't break
            return new AnswerEvaluationDTO(
                    5, "Needs Work",
                    "Answer was received.",
                    "Evaluation temporarily unavailable. Please review your answer.",
                    "A strong answer would clearly explain the concept with an example.",
                    "Can you elaborate on your answer with a real example?"
            );
        }
    }

    // ── Holistic Feedback ──────────────────────────────────────────────────

    /**
     * After all turns are complete, Gemini reviews the full session transcript
     * and generates an overall assessment — not just the average of per-question scores.
     *
     * A student might score 6/10 on each question but show consistent improvement,
     * strong communication, and clear conceptual understanding — this method captures that.
     */
    public HolisticFeedbackDTO generateHolisticFeedback(
            InterviewSession session,
            List<InterviewTurn> turns) {

        BeanOutputConverter<HolisticFeedbackDTO> converter =
                new BeanOutputConverter<>(HolisticFeedbackDTO.class);

        String transcript = turns.stream()
                .map(t -> String.format(
                        "Q%d [%s | %s | diff %d]:\nQ: %s\nA: %s\nScore: %d/10\n",
                        t.getQuestionNumber(),
                        t.getSkillArea(),
                        t.getQuestionType(),
                        t.getDifficultyLevel(),
                        t.getQuestion(),
                        t.getStudentAnswer() != null ? t.getStudentAnswer() : "(no answer)",
                        t.getScore() != null ? t.getScore() : 0
                ))
                .collect(Collectors.joining("\n---\n"));

        double avgScore = turns.stream()
                .filter(t -> t.getScore() != null)
                .mapToInt(InterviewTurn::getScore)
                .average()
                .orElse(0);

        String prompt = """
            You are providing comprehensive interview feedback for an Indian CS student
            who just completed a %s mock interview for %s.

            FULL INTERVIEW TRANSCRIPT:
            %s

            AVERAGE SCORE: %.1f / 10

            Provide holistic feedback considering:
            - Consistency across questions (did they improve or decline?)
            - Communication clarity (were answers concise or rambling?)
            - Depth of knowledge vs surface-level answers
            - Whether weak areas are addressable with short-term study

            For "overallScore" (0-100): translate the per-question performance to a
            placement readiness percentage for %s specifically.
            "Ready" = 75+, "Almost Ready" = 55-74, "Needs Work" = below 55.

            For "recommendedTopics": be specific (e.g., "Practice 20 medium Tree problems",
            not just "study trees").

            For "confidenceLevel": assess how confidently they communicated answers
            regardless of correctness.

            %s
            """.formatted(
                session.getInterviewType().name().toLowerCase(),
                session.getTargetCompany(),
                transcript,
                avgScore,
                session.getTargetCompany(),
                converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            return converter.convert(response);
        } catch (Exception e) {
            log.error("Holistic feedback failed: {}", e.getMessage());
            int score = (int)(avgScore * 10);
            return new HolisticFeedbackDTO(
                    score,
                    score >= 75 ? "Ready" : score >= 55 ? "Almost Ready" : "Needs Work",
                    "Interview completed. Review individual question feedback for details.",
                    List.of("Completed the interview session"),
                    List.of("Review feedback for each question"),
                    List.of("Practice weak areas identified above"),
                    "Medium"
            );
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Decides which skill area to target for this question number.
     * Rotates through gap areas so the interview covers multiple topics.
     * First half = higher priority gaps, second half = secondary gaps + HR.
     */
    private String pickSkillArea(InterviewSession session,
                                 int questionNumber,
                                 String skillGapContext) {
        if (session.getInterviewType() == InterviewSession.InterviewType.HR) {
            return "HR-Behavioral";
        }
        // For MIXED: last 2 questions are HR
        if (session.getInterviewType() == InterviewSession.InterviewType.MIXED
                && questionNumber > session.getTotalQuestions() - 2) {
            return "HR-Behavioral";
        }
        // Fallback to DSA for technical if no gap context
        if (skillGapContext == null || skillGapContext.isBlank()) {
            return "DSA-General";
        }
        // Parse first gap from the skill gap context (simplified)
        return "DSA-" + (questionNumber % 3 == 0 ? "Dynamic Programming"
                : questionNumber % 3 == 1 ? "Trees & Graphs"
                : "Arrays & Strings");
    }

    /**
     * Difficulty ramps up across the interview.
     * First third = easier, middle = medium, last third = harder.
     */
    private int mapQuestionToDifficulty(int questionNumber, int total) {
        double ratio = (double) questionNumber / total;
        if (ratio <= 0.33) return 2;
        if (ratio <= 0.66) return 3;
        return 4;
    }

    private GeneratedQuestionDTO fallbackQuestion(String skillArea, int questionNumber) {
        return new GeneratedQuestionDTO(
                "Explain the key concepts of " + skillArea + " and give a real-world example.",
                "CONCEPTUAL",
                skillArea,
                2,
                null
        );
    }
}