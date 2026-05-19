package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.*;
import com.AspireAI.backend.interview.entity.*;
import com.AspireAI.backend.interview.repositories.InterviewSessionRepository;
import com.AspireAI.backend.interview.repositories.InterviewTurnRepository;
import com.AspireAI.backend.analyzer.repoitory.UserProfileRepository;
import com.AspireAI.backend.analyzer.service.GitHubEnrichmentService;
import com.AspireAI.backend.analyzer.dto.GitHubProfileDTO;
import com.AspireAI.backend.analyzer.entity.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * InterviewAgentService — main orchestrator for the Mock Interviewer.
 *
 * Exposes 4 operations to the controller:
 *
 * 1. startSession()     → Creates session, generates first question
 * 2. submitAnswer()     → Saves answer, evaluates it, generates next question
 * 3. getResult()        → Returns full session report after completion
 * 4. getPastSessions()  → Dashboard list of previous interviews
 *
 * Each operation is a single HTTP request.
 * The interview flows as:
 *   POST /start → SessionStartedDTO (has first question)
 *   POST /answer → TurnFeedbackDTO (has feedback + next question OR completion signal)
 *   GET  /result/{sessionId} → SessionResultDTO (full report)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewAgentService {

    private final InterviewSessionRepository sessionRepo;
    private final InterviewTurnRepository turnRepo;
    private final QuestionGeneratorService questionGenerator;
    private final ObjectMapper objectMapper;
    private final UserProfileRepository profileRepo;
    private final GitHubEnrichmentService githubEnricher;

    // ── Start Session ──────────────────────────────────────────────────────

    @Transactional
    public SessionStartedDTO startSession(StartSessionRequestDTO req) {
        log.info("=== Interview Agent START | user={} company={} type={} ===",
                req.userId(), req.targetCompany(), req.interviewType());

        // Abandon any existing active session for this user
        sessionRepo.findFirstByUserIdAndStatus(req.userId(), InterviewSession.SessionStatus.IN_PROGRESS)
                .ifPresent(old -> {
                    old.setStatus(InterviewSession.SessionStatus.ABANDONED);
                    sessionRepo.save(old);
                    log.info("Abandoned previous session: {}", old.getId());
                });

        // Create new session
        InterviewSession session = InterviewSession.builder()
                .userId(req.userId())
                .targetCompany(req.targetCompany())
                .interviewType(InterviewSession.InterviewType.valueOf(req.interviewType()))
                .totalQuestions(req.totalQuestions() != null ? req.totalQuestions() : 5)
                .status(InterviewSession.SessionStatus.IN_PROGRESS)
                .skillGapSnapshot(req.skillGapJson())
                .build();

        session = sessionRepo.save(session);

        String githubContext = buildGitHubContext(req.userId());

        // Generate first question
        GeneratedQuestionDTO genQ = questionGenerator.generateQuestion(
                session, 1, List.of(), req.skillGapJson(), githubContext
        );

        String ragInsight = genQ.ragInsight();

        // Save the turn (question only — answer comes later)
        InterviewTurn turn = InterviewTurn.builder()
                .session(session)
                .questionNumber(1)
                .skillArea(genQ.skillArea())
                .question(genQ.questionText())
                .questionType(genQ.questionType())
                .difficultyLevel(genQ.difficultyLevel())
                .ragSourceIds(ragInsight)
                .build();

        turnRepo.save(turn);

        log.info("Session {} started. First question generated.", session.getId());

        return new SessionStartedDTO(
                session.getId(),
                session.getTargetCompany(),
                session.getInterviewType().name(),
                session.getTotalQuestions(),
                toQuestionDTO(turn, session.getTotalQuestions())
        );
    }

    // ── Submit Answer ──────────────────────────────────────────────────────

    @Transactional
    public TurnFeedbackDTO submitAnswer(SubmitAnswerRequestDTO req) {
        log.info("Submitting answer for turn={}", req.turnId());

        // Load turn and session
        InterviewTurn turn = turnRepo.findById(req.turnId())
                .orElseThrow(() -> new RuntimeException("Turn not found: " + req.turnId()));
        InterviewSession session = turn.getSession();

        // Save the answer
        turn.setStudentAnswer(req.studentAnswer());
        turn.setAnswerTimeSeconds(req.answerTimeSeconds());
        turn.setAnsweredAt(LocalDateTime.now());

        // Evaluate with Gemini
        AnswerEvaluationDTO eval = questionGenerator.evaluateAnswer(
                turn, req.studentAnswer(), null  // idealAnswerHint loaded by generator
        );

        // Persist evaluation
        turn.setScore(eval.score());
        try {
            turn.setFeedbackJson(objectMapper.writeValueAsString(eval));
        } catch (Exception e) {
            log.warn("Could not serialize feedback JSON: {}", e.getMessage());
        }
        turnRepo.save(turn);

        int nextQNum = turn.getQuestionNumber() + 1;
        boolean isLastQuestion = nextQNum > session.getTotalQuestions();

        // If last question → complete session + generate holistic feedback
        if (isLastQuestion) {
            return completeSession(session, eval);
        }

        // Otherwise → generate next question
        List<String> askedTexts = turnRepo.findAskedQuestions(session.getId());

        String githubContext = buildGitHubContext(session.getUserId());

        GeneratedQuestionDTO nextGen = questionGenerator.generateQuestion(
                session, nextQNum, askedTexts, session.getSkillGapSnapshot(), githubContext
        );

        InterviewTurn nextTurn = InterviewTurn.builder()
                .session(session)
                .questionNumber(nextQNum)
                .skillArea(nextGen.skillArea())
                .question(nextGen.questionText())
                .questionType(nextGen.questionType())
                .difficultyLevel(nextGen.difficultyLevel())
                .ragSourceIds(nextGen.ragInsight())
                .build();

        turnRepo.save(nextTurn);

        return new TurnFeedbackDTO(
                turn.getId(),
                turn.getQuestionNumber(),
                eval.score(),
                eval.grade(),
                eval.whatWasGood(),
                eval.whatWasMissed(),
                eval.idealAnswer(),
                eval.followUpHint(),
                toQuestionDTO(nextTurn, session.getTotalQuestions())
        );
    }

    // ── Complete session ───────────────────────────────────────────────────

    private TurnFeedbackDTO completeSession(InterviewSession session,
                                            AnswerEvaluationDTO lastEval) {
        log.info("Completing session: {}", session.getId());

        List<InterviewTurn> allTurns = turnRepo.findBySessionIdOrderByQuestionNumberAsc(
                session.getId()
        );

        // Generate holistic feedback
        HolisticFeedbackDTO holistic = questionGenerator.generateHolisticFeedback(
                session, allTurns
        );

        // Persist session completion
        session.setStatus(InterviewSession.SessionStatus.COMPLETED);
        session.setOverallScore(holistic.overallScore());
        session.setCompletedAt(LocalDateTime.now());
        try {
            session.setOverallFeedbackJson(objectMapper.writeValueAsString(holistic));
        } catch (Exception e) {
            log.warn("Could not serialize holistic feedback: {}", e.getMessage());
        }
        sessionRepo.save(session);

        log.info("Session {} completed. Overall score: {}", session.getId(), holistic.overallScore());

        // Return last turn's feedback with null nextQuestion (signals completion to frontend)
        return new TurnFeedbackDTO(
                allTurns.get(allTurns.size() - 1).getId(),
                session.getTotalQuestions(),
                lastEval.score(),
                lastEval.grade(),
                lastEval.whatWasGood(),
                lastEval.whatWasMissed(),
                lastEval.idealAnswer(),
                lastEval.followUpHint(),
                null   // ← null signals "interview complete" to the frontend
        );
    }

    // ── Get Result ─────────────────────────────────────────────────────────

    public SessionResultDTO getResult(UUID sessionId) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<InterviewTurn> turns = turnRepo.findBySessionIdOrderByQuestionNumberAsc(sessionId);

        OverallFeedbackDTO overall = null;
        if (session.getOverallFeedbackJson() != null) {
            try {
                HolisticFeedbackDTO h = objectMapper.readValue(
                        session.getOverallFeedbackJson(), HolisticFeedbackDTO.class
                );
                overall = new OverallFeedbackDTO(
                        h.overallScore(), h.overallGrade(), h.executiveSummary(),
                        h.strengths(), h.areasToImprove(), h.recommendedTopics(),
                        h.confidenceLevel()
                );
            } catch (Exception e) {
                log.warn("Could not deserialize holistic feedback: {}", e.getMessage());
            }
        }

        List<TurnSummaryDTO> summaries = turns.stream()
                .map(this::toTurnSummary)
                .collect(Collectors.toList());

        List<String> roadmapSuggestions = overall != null
                ? overall.recommendedTopics()
                : List.of();

        return new SessionResultDTO(
                session.getId(),
                session.getTargetCompany(),
                session.getInterviewType().name(),
                session.getTotalQuestions(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getOverallScore(),
                overall,
                summaries,
                roadmapSuggestions
        );
    }

    // ── Past Sessions ──────────────────────────────────────────────────────

    public List<SessionCardDTO> getPastSessions(UUID userId) {
        return sessionRepo.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .filter(s -> s.getStatus() == InterviewSession.SessionStatus.COMPLETED)
                .map(s -> new SessionCardDTO(
                        s.getId(), s.getTargetCompany(), s.getInterviewType().name(),
                        s.getOverallScore(),
                        gradeLabel(s.getOverallScore()),
                        s.getTotalQuestions(), s.getStartedAt()
                ))
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private QuestionDTO toQuestionDTO(InterviewTurn turn, int total) {
        return new QuestionDTO(
                turn.getId(), turn.getQuestionNumber(), total,
                turn.getSkillArea(), turn.getQuestion(),
                turn.getQuestionType(), turn.getDifficultyLevel(),
                turn.getRagSourceIds()
        );
    }

    private TurnSummaryDTO toTurnSummary(InterviewTurn turn) {
        String whatWasMissed = null;
        if (turn.getFeedbackJson() != null) {
            try {
                AnswerEvaluationDTO fb = objectMapper.readValue(
                        turn.getFeedbackJson(), AnswerEvaluationDTO.class
                );
                whatWasMissed = fb.whatWasMissed();
            } catch (Exception ignored) {}
        }
        return new TurnSummaryDTO(
                turn.getQuestionNumber(), turn.getSkillArea(), turn.getQuestion(),
                turn.getStudentAnswer(), turn.getScore(),
                gradeLabel(turn.getScore() != null ? turn.getScore() * 10 : 0),
                whatWasMissed
        );
    }

    private String buildGitHubContext(UUID userId) {
        try {
            UserProfile profile = profileRepo.findByUserId(userId).orElse(null);
            if (profile != null && profile.getGithubUsername() != null && !profile.getGithubUsername().isBlank()) {
                GitHubProfileDTO gitProfile = githubEnricher.enrich(profile.getGithubUsername());
                if (gitProfile.hasData()) {
                    return String.format(
                        "GitHub Username: %s | Total Repositories: %d | Total Stars: %d | Language Breakdown: %s",
                        gitProfile.username(),
                        gitProfile.repoCount(),
                        gitProfile.totalStars(),
                        gitProfile.languageFrequency().keySet().toString()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to build GitHub context for user {}: {}", userId, e.getMessage());
        }
        return "";
    }

    private String gradeLabel(Integer score) {
        if (score == null) return "N/A";
        if (score >= 80) return "Excellent";
        if (score >= 65) return "Good";
        if (score >= 45) return "Needs Work";
        return "Insufficient";
    }
}