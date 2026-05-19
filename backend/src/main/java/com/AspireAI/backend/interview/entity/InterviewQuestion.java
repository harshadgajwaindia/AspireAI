package com.AspireAI.backend.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Curated interview questions stored in MySQL (metadata) + PgVector (semantic search).
 *
 * WHY STORE QUESTIONS IN A VECTOR STORE?
 *
 * The Mock Interviewer needs to ask questions that are:
 * 1. Relevant to the student's WEAK AREAS (from gap report)
 * 2. Aligned with what THIS COMPANY actually asks in interviews
 * 3. At the right difficulty for the student's current level
 * 4. Not repeated within the same session
 *
 * Exact keyword search fails at #1 and #2.
 * "The student is weak at Binary Trees" needs to find questions like:
 *   - "Explain BFS vs DFS with examples"
 *   - "Write code to find the LCA of two nodes"
 *   - "What is the height of a balanced BST with n nodes?"
 * These all have different keywords but the same semantic intent.
 *
 * RAG solves this: embed the gap description, find semantically
 * similar questions from the bank.
 *
 * The question bank is seeded with:
 * - NQT-style questions (TCS, Infosys, Wipro patterns)
 * - Standard DSA questions by topic
 * - HR/behavioral questions by competency
 * - System design starters (for 12 LPA+ targets)
 */
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