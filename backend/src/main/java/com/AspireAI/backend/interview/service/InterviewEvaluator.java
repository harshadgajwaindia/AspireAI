package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.AnswerEvaluationDTO;
import com.AspireAI.backend.interview.dto.HolisticFeedbackDTO;
import com.AspireAI.backend.interview.entity.InterviewSession;
import com.AspireAI.backend.interview.entity.InterviewTurn;
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
public class InterviewEvaluator {

    private final ChatClient chatClient;

    public AnswerEvaluationDTO evaluateAnswer(InterviewTurn turn, String studentAnswer, String idealAnswerHint) {
        BeanOutputConverter<AnswerEvaluationDTO> converter =
                new BeanOutputConverter<>(AnswerEvaluationDTO.class);

        String prompt = InterviewPromptTemplates.EVALUATION_PROMPT.formatted(
                turn.getQuestion(), turn.getQuestionType(), turn.getSkillArea(), turn.getDifficultyLevel(),
                studentAnswer, idealAnswerHint != null ? idealAnswerHint : "Standard CS knowledge expected.",
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
            return new AnswerEvaluationDTO(
                    5, "Needs Work", "Answer was received.",
                    "Evaluation temporarily unavailable. Please review your answer.",
                    "A strong answer would clearly explain the concept with an example.",
                    "Can you elaborate on your answer with a real example?"
            );
        }
    }

    public HolisticFeedbackDTO generateHolisticFeedback(InterviewSession session, List<InterviewTurn> turns) {
        BeanOutputConverter<HolisticFeedbackDTO> converter =
                new BeanOutputConverter<>(HolisticFeedbackDTO.class);

        String transcript = turns.stream()
                .map(t -> String.format("Q%d [%s | %s | diff %d]:\nQ: %s\nA: %s\nScore: %d/10\n",
                        t.getQuestionNumber(), t.getSkillArea(), t.getQuestionType(), t.getDifficultyLevel(),
                        t.getQuestion(), t.getStudentAnswer() != null ? t.getStudentAnswer() : "(no answer)",
                        t.getScore() != null ? t.getScore() : 0))
                .collect(Collectors.joining("\n---\n"));

        double avgScore = turns.stream().filter(t -> t.getScore() != null)
                .mapToInt(InterviewTurn::getScore).average().orElse(0);

        String prompt = InterviewPromptTemplates.HOLISTIC_PROMPT.formatted(
                session.getInterviewType().name().toLowerCase(), session.getTargetCompany(),
                transcript, avgScore, session.getTargetCompany(), converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            return converter.convert(response);
        } catch (Exception e) {
            log.error("Holistic feedback failed: {}", e.getMessage());
            int score = (int)(avgScore * 10);
            return new HolisticFeedbackDTO(score,
                    score >= 75 ? "Ready" : score >= 55 ? "Almost Ready" : "Needs Work",
                    "Interview completed. Review individual question feedback for details.",
                    List.of("Completed the interview session"), List.of("Review feedback for each question"),
                    List.of("Practice weak areas identified above"), "Medium"
            );
        }
    }
}
