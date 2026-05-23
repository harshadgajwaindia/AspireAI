package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.roadmap.dto.RoadmapItemDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapPlanDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapRequestDTO;
import com.AspireAI.backend.roadmap.entity.RoadmapItem;
import com.AspireAI.backend.roadmap.entity.RoadmapPlan;
import com.AspireAI.backend.roadmap.repository.RoadmapItemRepository;
import com.AspireAI.backend.roadmap.repository.RoadmapPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapAgentService {

    private final RoadmapGeneratorService generator;
    private final RoadmapPlanRepository planRepo;
    private final RoadmapItemRepository itemRepo;
    private final RoadmapDTOMapper mapper;
    private final RoadmapItemManager itemManager;

    @Transactional
    public RoadmapPlanDTO generatePlan(RoadmapRequestDTO request) {
        log.info("=== Roadmap Agent START | user={} target={} ===", request.userId(), request.targetCompany());

        planRepo.findFirstByUserIdAndTargetCompanyAndStatus(request.userId(), request.targetCompany(), RoadmapPlan.PlanStatus.ACTIVE)
                .ifPresent(old -> {
                    old.setStatus(RoadmapPlan.PlanStatus.PAUSED); old.setLastUpdatedBy("ROADMAP_AGENT");
                    planRepo.save(old);
                });

        RoadmapPlan plan = RoadmapPlan.builder().userId(request.userId()).targetCompany(request.targetCompany())
                .totalDays(request.durationDays()).startDate(LocalDate.now()).targetDate(LocalDate.now().plusDays(request.durationDays()))
                .status(RoadmapPlan.PlanStatus.GENERATING).baselineReadiness(request.skillGapReport().overallReadiness())
                .lastUpdatedBy("ROADMAP_AGENT").build();
        plan = planRepo.save(plan);

        List<RoadmapItem> items = generator.generateItems(plan, request.skillGapReport(), request.dailyStudyMinutes());

        plan.getItems().addAll(items);
        plan.setStatus(RoadmapPlan.PlanStatus.ACTIVE);
        plan.setLastUpdatedBy("ROADMAP_AGENT");
        planRepo.save(plan);

        log.info("=== Roadmap Agent COMPLETE | {} items generated ===", items.size());
        return mapper.toPlanDTO(plan, items, itemRepo);
    }

    public RoadmapPlanDTO getPlan(UUID planId) {
        RoadmapPlan plan = planRepo.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        List<RoadmapItem> items = itemRepo.findByPlan_IdOrderByDayNumberAsc(planId);
        return mapper.toPlanDTO(plan, items, itemRepo);
    }

    public void markItemComplete(UUID itemId) { itemManager.markItemComplete(itemId); }
    public void markItemSkipped(UUID itemId) { itemManager.markItemSkipped(itemId); }
    public List<RoadmapItemDTO> getTodaysTasks(UUID userId) { return itemManager.getTodaysTasks(userId); }
    public void boostSkillPriority(UUID planId, String skillName, String reason) { itemManager.boostSkillPriority(planId, skillName, reason); }
}
