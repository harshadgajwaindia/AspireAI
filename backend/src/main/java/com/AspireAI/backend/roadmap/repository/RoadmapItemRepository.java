package com.AspireAI.backend.roadmap.repository;

import com.AspireAI.backend.roadmap.entity.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, UUID> {

    List<RoadmapItem> findByPlan_IdOrderByDayNumberAsc(UUID planId);

    @Query("""
        SELECT i FROM RoadmapItem i
        WHERE i.plan.userId = :userId
        AND i.scheduledDate = :date
        AND i.completionStatus = 'PENDING'
        ORDER BY i.gapImpact DESC
        """)
    List<RoadmapItem> findTodaysPendingItems(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT COUNT(DISTINCT i.scheduledDate) FROM RoadmapItem i
        WHERE i.plan.userId = :userId
        AND i.completionStatus = 'COMPLETED'
        AND i.scheduledDate >= :since
        """)
    long countCompletedDaysSince(
            @Param("userId") UUID userId,
            @Param("since") LocalDate since
    );

    @Query("""
        SELECT i FROM RoadmapItem i
        WHERE i.plan.id = :planId
        AND i.skillName = :skillName
        AND i.completionStatus = 'PENDING'
        ORDER BY i.dayNumber ASC
        """)
    List<RoadmapItem> findPendingItemsForSkill(
            @Param("planId") UUID planId,
            @Param("skillName") String skillName
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoadmapItem i SET i.completionStatus = :status,
        i.completedAt = CURRENT_TIMESTAMP
        WHERE i.id = :itemId
        """)
    int markItem(@Param("itemId") UUID itemId,
                 @Param("status") RoadmapItem.ItemStatus status);
}
