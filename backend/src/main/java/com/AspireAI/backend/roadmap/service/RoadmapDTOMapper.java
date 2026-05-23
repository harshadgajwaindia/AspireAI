package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.roadmap.dto.*;
import com.AspireAI.backend.roadmap.entity.RoadmapItem;
import com.AspireAI.backend.roadmap.entity.RoadmapPlan;
import com.AspireAI.backend.roadmap.repository.RoadmapItemRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoadmapDTOMapper {

    public RoadmapPlanDTO toPlanDTO(RoadmapPlan plan, List<RoadmapItem> items, RoadmapItemRepository itemRepo) {
        Map<Integer, List<RoadmapItem>> byWeek = items.stream()
                .collect(Collectors.groupingBy(i -> ((i.getDayNumber() - 1) / 7) + 1));

        List<WeekSummaryDTO> weeks = byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<RoadmapItem> weekItems = entry.getValue();
                    String theme = inferWeekTheme(weekItems);
                    String focusSkills = weekItems.stream().map(RoadmapItem::getSkillName)
                            .distinct().collect(Collectors.joining(", "));
                    int totalMinutes = weekItems.stream().mapToInt(RoadmapItem::getEstimatedMinutes).sum();

                    return new WeekSummaryDTO(entry.getKey(), theme, focusSkills, totalMinutes,
                            weekItems.stream().map(this::toItemDTO).toList());
                }).toList();

        ProgressSummaryDTO progress = buildProgress(items, plan, itemRepo);

        return new RoadmapPlanDTO(
                plan.getId(), plan.getUserId(), plan.getTargetCompany(), plan.getTotalDays(),
                plan.getStartDate(), plan.getTargetDate(), plan.getBaselineReadiness(),
                plan.getStatus().name(), weeks, progress
        );
    }

    private ProgressSummaryDTO buildProgress(List<RoadmapItem> items, RoadmapPlan plan, RoadmapItemRepository itemRepo) {
        long completed = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.COMPLETED).count();
        long skipped = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.SKIPPED).count();
        long pending = items.stream().filter(i -> i.getCompletionStatus() == RoadmapItem.ItemStatus.PENDING).count();

        double pct = items.isEmpty() ? 0 : (double) completed / items.size() * 100;
        int streak = (int) itemRepo.countCompletedDaysSince(plan.getUserId(), LocalDate.now().minusDays(30));

        int gapToFill = 100 - plan.getBaselineReadiness();
        int projected = plan.getBaselineReadiness() + (int)(pct / 100 * gapToFill * 0.8);

        return new ProgressSummaryDTO(
                items.size(), (int) completed, (int) skipped, (int) pending,
                Math.round(pct * 10.0) / 10.0, streak, Math.min(100, projected)
        );
    }

    public RoadmapItemDTO toItemDTO(RoadmapItem item) {
        String ragInsight = item.getRagSourceIds() != null && !item.getRagSourceIds().isBlank()
                ? "Relevant to recent " + item.getPlan().getTargetCompany() + " postings" : null;

        return new RoadmapItemDTO(
                item.getId(), item.getDayNumber(), item.getScheduledDate(), item.getSkillName(),
                item.getCategory(), item.getTaskTitle(), item.getTaskDescription(),
                item.getEstimatedMinutes(), item.getDifficultyLevel(), item.getResourceUrl(),
                item.getResourceType(), item.getGapImpact(), item.getCompletionStatus().name(), ragInsight
        );
    }

    private String inferWeekTheme(List<RoadmapItem> items) {
        return items.stream().collect(Collectors.groupingBy(RoadmapItem::getCategory, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> capitalize(e.getKey()) + " Week").orElse("Study Week");
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
