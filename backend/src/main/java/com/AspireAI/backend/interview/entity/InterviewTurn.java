package com.AspireAI.backend.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single question-answer-feedback turn within a session.
 *
 * Flow per turn:
 * 1. Mock Interviewer (Gemini) generates a question  → question field
 * 2. Student types/speaks their answer               → studentAnswer
 * 3. Gemini evaluates the answer                     → feedbackJson
 * 4. Score is assigned                               → score (0-10)
 *
 * The feedbackJson contains structured feedback:
 * {
 *   "whatWasGood": "...",
 *   "whatWasMissed": "...",
 *   "idealAnswer": "...",
 *   "followUpHint": "..."
 * }
 *
 * ragSourceIds: which job posting / question bank IDs informed this question.
 * Shown to the student as "This type of question appeared in 3 recent TCS interviews."
 */
@Entity
@Table(name = "interview_turns", indexes = {
        @Index(name = "idx_session_qnum", columnList = "session_id, question_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;         // 1..totalQuestions

    @Column(name = "skill_area")
    private String skillArea;               // "DSA-Trees", "Spring Boot", "HR-Leadership"

    @Column(nullable = false, columnDefinition = "text")
    private String question;                // the actual question text

    @Column(name = "question_type")
    private String questionType;            // "CONCEPTUAL", "CODING", "BEHAVIORAL", "SITUATIONAL"

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;        // 1-5

    @Column(name = "student_answer", columnDefinition = "text")
    private String studentAnswer;           // student's response

    @Column(name = "answer_time_seconds")
    private Integer answerTimeSeconds;      // how long they took

    @Column(name = "score")
    private Integer score;                  // 0-10

    @Column(name = "feedback_json", columnDefinition = "text")
    private String feedbackJson;            // structured Gemini feedback (JSON string)

    @Column(name = "rag_source_ids", columnDefinition = "text")
    private String ragSourceIds;            // job posting / question bank IDs

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        askedAt = LocalDateTime.now();
    }
}