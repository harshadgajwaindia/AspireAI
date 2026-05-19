package com.AspireAI.backend.roadmap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmap_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String targetCompany;       // "TCS Digital"

    @Column(nullable = false)
    private Integer totalDays;          // 30, 60, or 90

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate targetDate;       // startDate + totalDays

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status;

    /**
     * Snapshot of readiness score when this plan was generated.
     * Used to measure improvement over time.
     */
    @Column(nullable = false)
    private Integer baselineReadiness;

    /**
     * One plan has many items (daily tasks).
     * cascade=ALL means saving/deleting the plan also saves/deletes its items.
     * orphanRemoval=true means removing an item from the list deletes it from DB.
     */
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<RoadmapItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String lastUpdatedBy;  // "USER" or "CONTENT_AGENT"

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PlanStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PlanStatus {
        GENERATING, ACTIVE, PAUSED, COMPLETED
    }

}
