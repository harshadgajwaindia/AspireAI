package com.AspireAI.backend.roadmap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_resources", indexes = {
        @Index(name = "idx_skill_type", columnList = "skill_name, resource_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skillName;           // "DSA-Trees & Graphs"

    @Column(nullable = false)
    private String category;            // "dsa", "backend"

    @Column(nullable = false)
    private String title;               // "Binary Trees Complete Guide"

    @Column(columnDefinition = "text")
    private String description;         // What this resource covers

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String resourceType;        // "VIDEO", "ARTICLE", "LEETCODE_SET", "MOCK"

    @Column(nullable = false)
    private Integer difficultyLevel;    // 1-5

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Double qualityScore;        // 0.0-1.0, updated based on user ratings

    @Column(name = "vector_store_id")
    private String vectorStoreId;       // ID in PgVector for semantic retrieval

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
