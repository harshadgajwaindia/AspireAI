package com.AspireAI.backend.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "interview_sessions", indexes = {
        @Index(name = "idx_user_status", columnList = "user_id, status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_company", nullable = false)
    private String targetCompany;           // "TCS Digital"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType interviewType;    // TECHNICAL, HR, MIXED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;         // how many Q&A rounds (5, 10)

    @Column(name = "overall_score")
    private Integer overallScore;           // 0-100, populated at end

    @Column(name = "overall_feedback_json", columnDefinition = "text")
    private String overallFeedbackJson;     // Gemini's holistic session review

    // RAG context used for this session — stored so we know which
    // job postings influenced the questions asked
    @Column(name = "rag_context_summary", columnDefinition = "text")
    private String ragContextSummary;

    // Skill profile snapshot from the Analyzer — tells the Mock Interviewer
    // which topics to focus on (the student's weakest areas)
    @Column(name = "skill_gap_snapshot", columnDefinition = "text")
    private String skillGapSnapshot;        // serialized SkillGapReportDTO

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionNumber ASC")
    @Builder.Default
    private List<InterviewTurn> turns = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (status == null) status = SessionStatus.CREATED;
    }

    public enum InterviewType { TECHNICAL, HR, MIXED }

    public enum SessionStatus { CREATED, IN_PROGRESS, COMPLETED, ABANDONED }
}