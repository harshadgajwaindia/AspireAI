package com.AspireAI.backend.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "interview_question_bank", indexes = {
        @Index(name = "idx_company_type", columnList = "company_target, question_type"),
        @Index(name = "idx_skill_diff", columnList = "skill_area, difficulty_level")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_target")
    private String companyTarget;           // "TCS Digital", "ANY"

    @Column(name = "skill_area", nullable = false)
    private String skillArea;               // "DSA-Trees", "Spring Boot", "HR-Leadership"

    @Column(nullable = false, columnDefinition = "text")
    private String questionText;

    @Column(name = "question_type", nullable = false)
    private String questionType;            // "CONCEPTUAL", "CODING", "BEHAVIORAL", "SITUATIONAL"

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;        // 1-5

    @Column(name = "ideal_answer_hint", columnDefinition = "text")
    private String idealAnswerHint;         // Used by Gemini when evaluating student answers

    @Column(name = "key_concepts")
    private String keyConcepts;             // "BFS, queue, level order" — comma separated

    @Column(name = "times_used", nullable = false)
    private Integer timesUsed;

    @Column(name = "avg_score")
    private Double avgScore;                // average score students get on this question

    @Column(name = "vector_store_id")
    private String vectorStoreId;           // link to PgVector document

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timesUsed == null) timesUsed = 0;
    }
}