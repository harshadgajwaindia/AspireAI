package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.*;
import com.AspireAI.backend.interview.entity.InterviewSession;
import com.AspireAI.backend.interview.entity.InterviewTurn;
import com.AspireAI.backend.interview.repositories.InterviewSessionRepository;
import com.AspireAI.backend.interview.repositories.InterviewTurnRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionManager {

    private final InterviewSessionRepository sessionRepo;
    private final InterviewTurnRepository turnRepo;
    private final InterviewQuestionGenerator questionGenerator;
    private final InterviewGitHubContextBuilder githubContextBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public SessionStartedDTO startSession(StartSessionRequestDTO req) {
        sessionRepo.findFirstByUserIdAndStatus(req.userId(), InterviewSession.SessionStatus.IN_PROGRESS)
                .ifPresent(old -> {
                    old.setStatus(InterviewSession.SessionStatus.ABANDONED);
                    sessionRepo.save(old);
                });

        InterviewSession session = InterviewSession.builder().userId(req.userId())
                .targetCompany(req.targetCompany())
                .interviewType(InterviewSession.InterviewType.valueOf(req.interviewType()))
                .totalQuestions(req.totalQuestions() != null ? req.totalQuestions() : 5)
                .status(InterviewSession.SessionStatus.IN_PROGRESS)
                .skillGapSnapshot(req.skillGapJson()).build();

        session = sessionRepo.save(session);
        String githubContext = githubContextBuilder.buildGitHubContext(req.userId());

        GeneratedQuestionDTO genQ = questionGenerator.generateQuestion(
                session, 1, List.of(), req.skillGapJson(), githubContext
        );

        InterviewTurn turn = InterviewTurn.builder().session(session).questionNumber(1)
                .skillArea(genQ.skillArea()).question(genQ.questionText())
                .questionType(genQ.questionType()).difficultyLevel(genQ.difficultyLevel())
                .ragSourceIds(genQ.ragInsight()).build();
        turnRepo.save(turn);

        return new SessionStartedDTO(
                session.getId(), session.getTargetCompany(), session.getInterviewType().name(),
                session.getTotalQuestions(),
                new QuestionDTO(turn.getId(), turn.getQuestionNumber(), session.getTotalQuestions(),
                        turn.getSkillArea(), turn.getQuestion(), turn.getQuestionType(),
                        turn.getDifficultyLevel(), turn.getRagSourceIds())
        );
    }

    public SessionResultDTO getResult(UUID sessionId) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<InterviewTurn> turns = turnRepo.findBySessionIdOrderByQuestionNumberAsc(sessionId);
        OverallFeedbackDTO overall = null;

        if (session.getOverallFeedbackJson() != null) {
            try {
                HolisticFeedbackDTO h = objectMapper.readValue(session.getOverallFeedbackJson(), HolisticFeedbackDTO.class);
                overall = new OverallFeedbackDTO(h.overallScore(), h.overallGrade(), h.executiveSummary(),
                        h.strengths(), h.areasToImprove(), h.recommendedTopics(), h.confidenceLevel());
            } catch (Exception e) {}
        }

        List<TurnSummaryDTO> summaries = turns.stream().map(this::toTurnSummary).collect(Collectors.toList());
        List<String> roadmapSuggestions = overall != null ? overall.recommendedTopics() : List.of();

        return new SessionResultDTO(
                session.getId(), session.getTargetCompany(), session.getInterviewType().name(),
                session.getTotalQuestions(), session.getStartedAt(), session.getCompletedAt(),
                session.getOverallScore(), overall, summaries, roadmapSuggestions
        );
    }

    public List<SessionCardDTO> getPastSessions(UUID userId) {
        return sessionRepo.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(s -> s.getStatus() == InterviewSession.SessionStatus.COMPLETED)
                .map(s -> new SessionCardDTO(s.getId(), s.getTargetCompany(), s.getInterviewType().name(),
                        s.getOverallScore(), gradeLabel(s.getOverallScore()), s.getTotalQuestions(), s.getStartedAt()))
                .collect(Collectors.toList());
    }

    private TurnSummaryDTO toTurnSummary(InterviewTurn turn) {
        String whatWasMissed = null;
        if (turn.getFeedbackJson() != null) {
            try {
                AnswerEvaluationDTO fb = objectMapper.readValue(turn.getFeedbackJson(), AnswerEvaluationDTO.class);
                whatWasMissed = fb.whatWasMissed();
            } catch (Exception ignored) {}
        }
        return new TurnSummaryDTO(
                turn.getQuestionNumber(), turn.getSkillArea(), turn.getQuestion(),
                turn.getStudentAnswer(), turn.getScore(),
                gradeLabel(turn.getScore() != null ? turn.getScore() * 10 : 0), whatWasMissed
        );
    }

    private String gradeLabel(Integer score) {
        if (score == null) return "N/A";
        if (score >= 80) return "Excellent";
        if (score >= 65) return "Good";
        if (score >= 45) return "Needs Work";
        return "Insufficient";
    }
}
