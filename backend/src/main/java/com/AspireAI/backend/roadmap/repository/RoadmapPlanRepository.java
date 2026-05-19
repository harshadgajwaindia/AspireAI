package com.AspireAI.backend.roadmap.repository;

import com.AspireAI.backend.roadmap.entity.RoadmapPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapPlanRepository extends JpaRepository<RoadmapPlan, UUID> {

    Optional<RoadmapPlan> findFirstByUserIdAndTargetCompanyAndStatus(
            UUID userId, String targetCompany, RoadmapPlan.PlanStatus status
    );

    List<RoadmapPlan> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
