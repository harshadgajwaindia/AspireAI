package com.AspireAI.backend.roadmap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "roadmap_items", indexes = {
        @Index(name = "idx_plan_day", columnList = "plan_id, day_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private RoadmapPlan plan;

    @Column(nullable = false)
    private Integer dayNumber;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private String skillName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String taskTitle;

    @Column(nullable = false, columnDefinition = "text")
    private String taskDescription;

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Integer difficultyLevel;

    private String resourceUrl;

    private String resourceType;

    @Column(nullable = false)
    private Integer gapImpact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus completionStatus;

    @Column(columnDefinition = "text")
    private String ragSourceIds;

    private LocalDateTime completedAt;

    public enum ItemStatus {
        PENDING, COMPLETED, SKIPPED
    }
}
