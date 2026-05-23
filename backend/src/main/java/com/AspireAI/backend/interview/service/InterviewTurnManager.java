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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewTurnManager {

    private final InterviewSessionRepository sessionRepo;
    private final InterviewTurnRepository turnRepo;
    private final InterviewQuestionGenerator questionGenerator;
    private final InterviewEvaluator evaluator;
    private final ObjectMapper objectMapper;
    private final InterviewGitHubContextBuilder githubContextBuilder;

    @Transactional
    public TurnFeedbackDTO submitAnswer(SubmitAnswerRequestDTO req) {
        InterviewTurn turn = turnRepo.findById(req.turnId())
                .orElseThrow(() -> new RuntimeException("Turn not found: " + req.turnId()));
        InterviewSession session = turn.getSession();

        turn.setStudentAnswer(req.studentAnswer());
        turn.setAnswerTimeSeconds(req.answerTimeSeconds());
        turn.setAnsweredAt(LocalDateTime.now());
        turnRepo.save(turn);

        int nextQNum = turn.getQuestionNumber() + 1;
        if (nextQNum > session.getTotalQuestions()) {
            return completeSession(session);
        }

        List<String> askedTexts = turnRepo.findAskedQuestions(session.getId());
        String githubContext = githubContextBuilder.buildGitHubContext(session.getUserId());

        GeneratedQuestionDTO nextGen = questionGenerator.generateQuestion(
                session, nextQNum, askedTexts, session.getSkillGapSnapshot(), githubContext
        );

        InterviewTurn nextTurn = InterviewTurn.builder().session(session).questionNumber(nextQNum)
                .skillArea(nextGen.skillArea()).question(nextGen.questionText())
                .questionType(nextGen.questionType()).difficultyLevel(nextGen.difficultyLevel())
                .ragSourceIds(nextGen.ragInsight()).build();
        turnRepo.save(nextTurn);

        return new TurnFeedbackDTO(
                turn.getId(), turn.getQuestionNumber(), null, null, null, null, null, null,
                new QuestionDTO(nextTurn.getId(), nextTurn.getQuestionNumber(), session.getTotalQuestions(),
                        nextTurn.getSkillArea(), nextTurn.getQuestion(), nextTurn.getQuestionType(),
                        nextTurn.getDifficultyLevel(), nextTurn.getRagSourceIds())
        );
    }

    private TurnFeedbackDTO completeSession(InterviewSession session) {
        List<InterviewTurn> allTurns = turnRepo.findBySessionIdOrderByQuestionNumberAsc(session.getId());
        for (InterviewTurn t : allTurns) {
            if (t.getStudentAnswer() != null && t.getScore() == null) {
                try {
                    AnswerEvaluationDTO eval = evaluator.evaluateAnswer(t, t.getStudentAnswer(), null);
                    t.setScore(eval.score());
                    t.setFeedbackJson(objectMapper.writeValueAsString(eval));
                    turnRepo.save(t);
                } catch (Exception e) {
                    log.error("Failed to evaluate turn {}: {}", t.getId(), e.getMessage());
                }
            }
        }
        List<InterviewTurn> evaluatedTurns = turnRepo.findBySessionIdOrderByQuestionNumberAsc(session.getId());
        HolisticFeedbackDTO holistic = evaluator.generateHolisticFeedback(session, evaluatedTurns);

        session.setStatus(InterviewSession.SessionStatus.COMPLETED);
        session.setOverallScore(holistic.overallScore());
        session.setCompletedAt(LocalDateTime.now());
        try {
            session.setOverallFeedbackJson(objectMapper.writeValueAsString(holistic));
        } catch (Exception e) {}
        sessionRepo.save(session);

        InterviewTurn lastTurn = evaluatedTurns.get(evaluatedTurns.size() - 1);
        AnswerEvaluationDTO lastEval = null;
        try {
            if (lastTurn.getFeedbackJson() != null)
                lastEval = objectMapper.readValue(lastTurn.getFeedbackJson(), AnswerEvaluationDTO.class);
        } catch (Exception e) {}

        return new TurnFeedbackDTO(
                lastTurn.getId(), session.getTotalQuestions(),
                lastEval != null ? lastEval.score() : null, lastEval != null ? lastEval.grade() : null,
                lastEval != null ? lastEval.whatWasGood() : null, lastEval != null ? lastEval.whatWasMissed() : null,
                lastEval != null ? lastEval.idealAnswer() : null, lastEval != null ? lastEval.followUpHint() : null, null
        );
    }
}
