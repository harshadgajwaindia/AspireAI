package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.roadmap.dto.RoadmapItemDTO;
import com.AspireAI.backend.roadmap.entity.RoadmapItem;
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
public class RoadmapItemManager {

    private final RoadmapItemRepository itemRepo;
    private final RoadmapPlanRepository planRepo;
    private final RoadmapDTOMapper mapper;

    @Transactional
    public void markItemComplete(UUID itemId) {
        if (itemRepo.markItem(itemId, RoadmapItem.ItemStatus.COMPLETED) == 0)
            throw new RuntimeException("Item not found: " + itemId);
        log.info("Item {} marked COMPLETED", itemId);
    }

    @Transactional
    public void markItemSkipped(UUID itemId) {
        if (itemRepo.markItem(itemId, RoadmapItem.ItemStatus.SKIPPED) == 0)
            throw new RuntimeException("Item not found: " + itemId);
        log.info("Item {} marked SKIPPED", itemId);
    }

    public List<RoadmapItemDTO> getTodaysTasks(UUID userId) {
        return itemRepo.findTodaysPendingItems(userId, LocalDate.now()).stream().map(mapper::toItemDTO).toList();
    }

    @Transactional
    public void boostSkillPriority(UUID planId, String skillName, String reason) {
        List<RoadmapItem> items = itemRepo.findPendingItemsForSkill(planId, skillName);
        if (items.isEmpty()) return;

        for (RoadmapItem item : items) {
            item.setGapImpact(Math.min(10, item.getGapImpact() + 2));
            item.setRagSourceIds((item.getRagSourceIds() != null ? item.getRagSourceIds() + "," : "") + "TREND:" + reason);
        }
        itemRepo.saveAll(items);

        planRepo.findById(planId).ifPresent(plan -> {
            plan.setLastUpdatedBy("CONTENT_AGENT");
            planRepo.save(plan);
        });
        log.info("Boosted {} items for skill {} (reason: {})", items.size(), skillName, reason);
    }
}
