package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.roadmap.dto.GeneratedTask;
import com.AspireAI.backend.roadmap.dto.GeneratedTaskList;
import com.AspireAI.backend.roadmap.entity.RoadmapItem;
import com.AspireAI.backend.roadmap.entity.RoadmapPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapGeneratorService {

    private final ChatClient chatClient;
    private final LearningResourceRagService resourceRag;
    private final JobTrendRagService jobTrendRag;

    public List<RoadmapItem> generateItems(RoadmapPlan plan, SkillGapReportDTO gapReport, int dailyMinutes) {
        log.info("Generating roadmap: {} days, {} min/day for {}", plan.getTotalDays(), dailyMinutes, plan.getTargetCompany());

        String trendingTopics = jobTrendRag.getTrendingTopics(plan.getTargetCompany());
        String resourceContext = buildResourceContext(gapReport);
        List<GeneratedTask> tasks = generateTasksFromGemini(plan, gapReport, trendingTopics, resourceContext, dailyMinutes);

        log.info("Gemini generated {} tasks", tasks.size());

        return tasks.stream().map(task -> buildRoadmapItem(task, plan)).collect(Collectors.toList());
    }

    private List<GeneratedTask> generateTasksFromGemini(RoadmapPlan plan, SkillGapReportDTO gapReport,
            String trendingTopics, String resourceContext, int dailyMinutes) {

        BeanOutputConverter<GeneratedTaskList> converter = new BeanOutputConverter<>(GeneratedTaskList.class);

        String gapsSummary = gapReport.topGaps().stream()
                .map(g -> String.format("- %s: student=%d, need=%d, gap=%d [%s]",
                        g.skillName(), g.studentScore(), g.requiredScore(), g.gapSize(), g.priority()))
                .collect(Collectors.joining("\n"));

        String prompt = RoadmapPromptTemplates.GENERATE_ROADMAP_PROMPT.formatted(
                plan.getTargetCompany(), gapsSummary, plan.getTargetCompany().toUpperCase(),
                trendingTopics, resourceContext, plan.getTotalDays(), dailyMinutes,
                plan.getTotalDays(), converter.getFormat()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            GeneratedTaskList result = converter.convert(response);
            return result != null && result.tasks() != null ? result.tasks() : List.of();
        } catch (Exception e) {
            log.error("Gemini task generation failed: {}", e.getMessage());
            return RoadmapRuleBasedGenerator.generateRuleBasedTasks(gapReport, plan.getTotalDays(), dailyMinutes);
        }
    }

    private RoadmapItem buildRoadmapItem(GeneratedTask task, RoadmapPlan plan) {
        return RoadmapItem.builder()
                .plan(plan).dayNumber(task.dayNumber())
                .scheduledDate(plan.getStartDate().plusDays(task.dayNumber() - 1))
                .skillName(task.skillName()).category(task.category())
                .taskTitle(task.taskTitle()).taskDescription(task.taskDescription())
                .estimatedMinutes(task.estimatedMinutes()).difficultyLevel(task.difficultyLevel())
                .gapImpact(task.gapImpact())
                .resourceUrl(task.referenceUrl() != null ? task.referenceUrl() : "https://wikipedia.org")
                .resourceType(task.resourceType() != null ? task.resourceType() : "DOCUMENTATION")
                .ragSourceIds(task.ragSourceIds()).completionStatus(RoadmapItem.ItemStatus.PENDING)
                .build();
    }

    private String buildResourceContext(SkillGapReportDTO gapReport) {
        return gapReport.topGaps().stream().limit(3)
                .map(gap -> resourceRag.getResourceContext(gap.skillName(), gap.skillName().split("-")[0].toLowerCase()))
                .filter(ctx -> !ctx.isEmpty()).collect(Collectors.joining("\n"));
    }
}
