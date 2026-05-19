package com.AspireAI.backend.roadmap.service;


import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.roadmap.dto.ProgressSummaryDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapItemDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapPlanDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapRequestDTO;
import com.AspireAI.backend.roadmap.dto.WeekSummaryDTO;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RoadmapAgentService — orchestrates the full roadmap creation pipeline.
 *
 * Called by:
 * - Controller (direct API call from frontend "Build Roadmap" button)
 * - Orchestrator (after Analyzer completes, auto-triggers roadmap generation)
 *
 * Also handles:
 * - Marking items complete/skipped
 * - Fetching today's tasks for the dashboard
 * - Rebuilding/refreshing a plan when Content Agent detects new trends
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapAgentService {

    private final RoadmapGeneratorService generator;
    private final RoadmapPlanRepository planRepo;
    private final RoadmapItemRepository itemRepo;

    // ── Plan generation ────────────────────────────────────────────────────

    /**
     * Main entry point. Creates a new roadmap plan from a skill gap report.
     *
     * If the user already has an ACTIVE plan for the same company,
     * we archive it and create a fresh one (re-analysis may have
     * changed the gap picture).
     */
    @Transactional
    public RoadmapPlanDTO generatePlan(RoadmapRequestDTO request) {
        log.info("=== Roadmap Agent START | user={} target={} days={} ===",
                request.userId(), request.targetCompany(), request.durationDays());

        // Archive any existing active plan for this company
        planRepo.findFirstByUserIdAndTargetCompanyAndStatus(
                request.userId(), request.targetCompany(), RoadmapPlan.PlanStatus.ACTIVE
        ).ifPresent(old -> {
            old.setStatus(RoadmapPlan.PlanStatus.PAUSED);
            old.setLastUpdatedBy("ROADMAP_AGENT");
            planRepo.save(old);
            log.info("Archived previous plan: {}", old.getId());
        });

        // Create the plan shell (no items yet)
        RoadmapPlan plan = RoadmapPlan.builder()
                .userId(request.userId())
                .targetCompany(request.targetCompany())
                .totalDays(request.durationDays())
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusDays(request.durationDays()))
                .status(RoadmapPlan.PlanStatus.GENERATING)
                .baselineReadiness(request.skillGapReport().overallReadiness())
                .lastUpdatedBy("ROADMAP_AGENT")
                .build();

        plan = planRepo.save(plan);
        log.info("Plan shell created: {}", plan.getId());

        // Generate all items (the expensive step — calls Gemini + RAG)
        List<RoadmapItem> items = generator.generateItems(
                plan,
                request.skillGapReport(),
                request.dailyStudyMinutes()
        );

        // Bulk save items
        plan.getItems().addAll(items);
        plan.setStatus(RoadmapPlan.PlanStatus.ACTIVE);
        plan.setLastUpdatedBy("ROADMAP_AGENT");
        planRepo.save(plan);

        log.info("=== Roadmap Agent COMPLETE | {} items generated ===", items.size());

        return toPlanDTO(plan, items);
    }

    // ── Item completion ────────────────────────────────────────────────────

    @Transactional
    public void markItemComplete(UUID itemId) {
        int updated = itemRepo.markItem(itemId, RoadmapItem.ItemStatus.COMPLETED);
        if (updated == 0) throw new RuntimeException("Item not found: " + itemId);
        log.info("Item {} marked COMPLETED", itemId);
    }

    @Transactional
    public void markItemSkipped(UUID itemId) {
        int updated = itemRepo.markItem(itemId, RoadmapItem.ItemStatus.SKIPPED);
        if (updated == 0) throw new RuntimeException("Item not found: " + itemId);
        log.info("Item {} marked SKIPPED", itemId);
    }

    // ── Dashboard queries ──────────────────────────────────────────────────

    /**
     * Returns today's pending tasks for the dashboard "Today's Focus" widget.
     */
    public List<RoadmapItemDTO> getTodaysTasks(UUID userId) {
        return itemRepo.findTodaysPendingItems(userId, LocalDate.now())
                .stream()
                .map(this::toItemDTO)
                .toList();
    }

    /**
     * Returns a full plan with all items grouped by week.
     */
    public RoadmapPlanDTO getPlan(UUID planId) {
        RoadmapPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
        List<RoadmapItem> items = itemRepo.findByPlan_IdOrderByDayNumberAsc(planId);
        return toPlanDTO(plan, items);
    }

    // ── Content Agent hook ─────────────────────────────────────────────────

    /**
     * Called by the Content Agent when it detects a trending topic
     * that's not well-covered in the current plan.
     *
     * Re-prioritizes pending items related to that skill by moving them
     * to earlier days and boosting their gapImpact score.
     *
     * This is how the roadmap stays "live" — the Content Agent updates
     * it without requiring the student to regenerate from scratch.
     */
    @Transactional
    public void boostSkillPriority(UUID planId, String skillName, String reason) {
        List<RoadmapItem> items = itemRepo.findPendingItemsForSkill(planId, skillName);
        if (items.isEmpty()) {
            log.info("No pending items for skill {} in plan {}", skillName, planId);
            return;
        }

        // Boost gap impact of existing items
        for (RoadmapItem item : items) {
            item.setGapImpact(Math.min(10, item.getGapImpact() + 2));
            item.setRagSourceIds(
                    (item.getRagSourceIds() != null ? item.getRagSourceIds() + "," : "")
                            + "TREND:" + reason
            );
        }
        itemRepo.saveAll(items);
        log.info("Boosted {} items for skill {} (reason: {})", items.size(), skillName, reason);

        // Update plan's lastUpdatedBy to track agent intervention
        planRepo.findById(planId).ifPresent(plan -> {
            plan.setLastUpdatedBy("CONTENT_AGENT");
            planRepo.save(plan);
        });
    }

    // ── DTO mapping ────────────────────────────────────────────────────────

    private RoadmapPlanDTO toPlanDTO(RoadmapPlan plan, List<RoadmapItem> items) {
        // Group items into weeks
        Map<Integer, List<RoadmapItem>> byWeek = items.stream()
                .collect(Collectors.groupingBy(i -> ((i.getDayNumber() - 1) / 7) + 1));

        List<WeekSummaryDTO> weeks = byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<RoadmapItem> weekItems = entry.getValue();
                    String theme = inferWeekTheme(weekItems);
                    String focusSkills = weekItems.stream()
                            .map(RoadmapItem::getSkillName)
                            .distinct()
                            .collect(Collectors.joining(", "));
                    int totalMinutes = weekItems.stream()
                            .mapToInt(RoadmapItem::getEstimatedMinutes)
                            .sum();

                    return new WeekSummaryDTO(
                            entry.getKey(), theme, focusSkills, totalMinutes,
                            weekItems.stream().map(this::toItemDTO).toList()
                    );
                })
                .toList();

        ProgressSummaryDTO progress = buildProgress(items, plan);

        return new RoadmapPlanDTO(
                plan.getId(), plan.getUserId(), plan.getTargetCompany(),
                plan.getTotalDays(), plan.getStartDate(), plan.getTargetDate(),
                plan.getBaselineReadiness(), plan.getStatus().name(),
                weeks, progress
        );
    }

    private ProgressSummaryDTO buildProgress(List<RoadmapItem> items, RoadmapPlan plan) {
        long completed = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.COMPLETED).count();
        long skipped   = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.SKIPPED).count();
        long pending   = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.PENDING).count();

        double pct = items.isEmpty() ? 0 : (double) completed / items.size() * 100;

        // Streak: consecutive days with at least one completion
        int streak = (int) itemRepo.countCompletedDaysSince(
                plan.getUserId(), LocalDate.now().minusDays(30)
        );

        // Project readiness: baseline + (completion% * gap to fill)
        int gapToFill = 100 - plan.getBaselineReadiness();
        int projected = plan.getBaselineReadiness() + (int)(pct / 100 * gapToFill * 0.8);

        return new ProgressSummaryDTO(
                items.size(), (int) completed, (int) skipped, (int) pending,
                Math.round(pct * 10.0) / 10.0, streak, Math.min(100, projected)
        );
    }

    private RoadmapItemDTO toItemDTO(RoadmapItem item) {
        String ragInsight = item.getRagSourceIds() != null && !item.getRagSourceIds().isBlank()
                ? "Relevant to recent " + item.getPlan().getTargetCompany() + " postings"
                : null;

        return new RoadmapItemDTO(
                item.getId(), item.getDayNumber(), item.getScheduledDate(),
                item.getSkillName(), item.getCategory(), item.getTaskTitle(),
                item.getTaskDescription(), item.getEstimatedMinutes(),
                item.getDifficultyLevel(), item.getResourceUrl(), item.getResourceType(),
                item.getGapImpact(), item.getCompletionStatus().name(), ragInsight
        );
    }

    private String inferWeekTheme(List<RoadmapItem> items) {
        // Find the most common category in this week's items
        return items.stream()
                .collect(Collectors.groupingBy(RoadmapItem::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> capitalize(e.getKey()) + " Week")
                .orElse("Study Week");
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
