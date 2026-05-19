package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.analyzer.dto.SkillGapDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.roadmap.entity.LearningResource;
import com.AspireAI.backend.roadmap.entity.RoadmapItem;
import com.AspireAI.backend.roadmap.entity.RoadmapPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoadmapGeneratorService — the brain of the Roadmap Agent.
 *
 * This is where RAG has the most impact. The generator does 3 things:
 *
 * 1. PRIORITIZE: Use job posting RAG to order topics by current demand.
 *    If "Kafka" is trending in job posts, it gets bumped up in the plan
 *    even if it's not in the student's gap list.
 *
 * 2. SCHEDULE: Distribute topics across available days based on:
 *    - Gap severity (larger gaps get more days)
 *    - Dependency order (learn Arrays before Trees, Trees before Graphs)
 *    - Student's available time per day
 *
 * 3. ATTACH RESOURCES: Use learning resource RAG to find the best
 *    specific resource for each task, not just a category bucket.
 *
 * The output is a list of RoadmapItems ready to be saved to DB.
 *
 * GEMINI USAGE: We call Gemini ONCE per plan generation (not once per item)
 * by asking it to generate ALL items in a single structured JSON response.
 * This keeps API costs manageable — one plan = one Gemini call.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapGeneratorService {

    private final ChatClient chatClient;
    private final LearningResourceRagService resourceRag;
    private final JobTrendRagService jobTrendRag;  // retrieves trending topics from job posts

    /**
     * Generates all RoadmapItems for a plan.
     *
     * @param plan              the newly created (empty) plan entity
     * @param gapReport         from the Analyzer Agent
     * @param dailyMinutes      how many minutes/day the student can study
     */
    public List<RoadmapItem> generateItems(RoadmapPlan plan,
                                           SkillGapReportDTO gapReport,
                                           int dailyMinutes) {
        log.info("Generating roadmap: {} days, {} min/day for {}",
                plan.getTotalDays(), dailyMinutes, plan.getTargetCompany());

        // ── Step 1: Get trending topics from job posting RAG ──────────────
        String trendingTopics = jobTrendRag.getTrendingTopics(plan.getTargetCompany());

        // ── Step 2: Get available resource context ────────────────────────
        String resourceContext = buildResourceContext(gapReport);

        // ── Step 3: Ask Gemini to generate the full structured plan ────────
        List<GeneratedTask> tasks = generateTasksFromGemini(
                plan, gapReport, trendingTopics, resourceContext, dailyMinutes
        );

        log.info("Gemini generated {} tasks", tasks.size());

        // ── Step 4: Attach RAG-retrieved resources to each task ────────────
        return tasks.stream()
                .map(task -> buildRoadmapItem(task, plan))
                .collect(Collectors.toList());
    }

    // ── Gemini call ────────────────────────────────────────────────────────

    private List<GeneratedTask> generateTasksFromGemini(
            RoadmapPlan plan,
            SkillGapReportDTO gapReport,
            String trendingTopics,
            String resourceContext,
            int dailyMinutes) {

        BeanOutputConverter<GeneratedTaskList> converter =
                new BeanOutputConverter<>(GeneratedTaskList.class);

        String gapsSummary = gapReport.topGaps().stream()
                .map(g -> String.format("- %s: student=%d, need=%d, gap=%d [%s]",
                        g.skillName(), g.studentScore(), g.requiredScore(),
                        g.gapSize(), g.priority()))
                .collect(Collectors.joining("\n"));

        String prompt = """
            You are a senior placement coach creating a study roadmap for an Indian
            CS student targeting %s.
 
            STUDENT'S SKILL GAPS (sorted by priority):
            %s
 
            TRENDING TOPICS IN RECENT %s JOB POSTINGS (from live data):
            %s
 
            AVAILABLE LEARNING RESOURCES:
            %s
 
            CONSTRAINTS:
            - Total days: %d
            - Daily study time: %d minutes
            - Do not schedule more than dailyMinutes worth of tasks per day
            - Start with HIGH priority gaps in Week 1-2
            - Include 1 mock/revision session per week (every 7th day)
            - Topics with dependencies must be ordered correctly:
              Arrays → Linked Lists → Trees → Graphs → DP
              Basic SQL → Joins → Indexing → Optimization
 
            Generate a task for EVERY day from day 1 to day %d.
            Each task must be specific and actionable, not vague.
            Good: "Solve 5 medium Binary Tree problems focusing on BFS patterns"
            Bad: "Study trees"
 
            If a trending topic from job postings is not in the gap list but is
            commonly required, include it in the later weeks of the plan.
 
            %s
 
            """.formatted(
                plan.getTargetCompany(), gapsSummary,
                plan.getTargetCompany().toUpperCase(), trendingTopics,
                resourceContext, plan.getTotalDays(), dailyMinutes,
                plan.getTotalDays(), converter.getFormat()
        );

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            GeneratedTaskList result = converter.convert(response);
            return result != null && result.tasks() != null
                    ? result.tasks()
                    : List.of();

        } catch (Exception e) {
            log.error("Gemini task generation failed: {}", e.getMessage());
            // Fall back to rule-based generation
            return generateRuleBasedTasks(gapReport, plan.getTotalDays(), dailyMinutes);
        }
    }

    // ── Resource attachment ────────────────────────────────────────────────

    private RoadmapItem buildRoadmapItem(GeneratedTask task, RoadmapPlan plan) {
        // Use RAG to find the best matching resource for this task
        LearningResource resource = resourceRag.findBestResource(
                task.taskTitle() + " " + task.taskDescription(),
                task.category(),
                task.difficultyLevel()
        );

        return RoadmapItem.builder()
                .plan(plan)
                .dayNumber(task.dayNumber())
                .scheduledDate(plan.getStartDate().plusDays(task.dayNumber() - 1))
                .skillName(task.skillName())
                .category(task.category())
                .taskTitle(task.taskTitle())
                .taskDescription(task.taskDescription())
                .estimatedMinutes(task.estimatedMinutes())
                .difficultyLevel(task.difficultyLevel())
                .gapImpact(task.gapImpact())
                .resourceUrl(resource != null ? resource.getUrl() : null)
                .resourceType(resource != null ? resource.getResourceType() : null)
                .ragSourceIds(task.ragSourceIds())
                .completionStatus(RoadmapItem.ItemStatus.PENDING)
                .build();
    }

    // ── Resource context builder ───────────────────────────────────────────

    private String buildResourceContext(SkillGapReportDTO gapReport) {
        // Build resource context only for the top 3 gaps to keep prompt size manageable
        return gapReport.topGaps().stream()
                .limit(3)
                .map(gap -> resourceRag.getResourceContext(gap.skillName(), gap.skillName().split("-")[0].toLowerCase()))
                .filter(ctx -> !ctx.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    // ── Rule-based fallback ────────────────────────────────────────────────

    /**
     * Fallback when Gemini call fails. Generates basic tasks from gap data.
     * Less intelligent but ensures the user always gets a usable plan.
     */
    private List<GeneratedTask> generateRuleBasedTasks(
            SkillGapReportDTO gapReport, int totalDays, int dailyMinutes) {
        log.warn("Using rule-based fallback for task generation");

        List<GeneratedTask> tasks = new ArrayList<>();
        List<SkillGapDTO> gaps = gapReport.topGaps();
        if (gaps.isEmpty()) return tasks;

        // Distribute days proportionally to gap size
        int totalGap = gaps.stream().mapToInt(SkillGapDTO::gapSize).sum();
        int day = 1;

        for (SkillGapDTO gap : gaps) {
            int daysForSkill = Math.max(1,
                    (int) Math.round((double) gap.gapSize() / totalGap * totalDays)
            );

            for (int d = 0; d < daysForSkill && day <= totalDays; d++, day++) {
                tasks.add(new GeneratedTask(
                        day,
                        gap.skillName(),
                        gap.skillName().split("-")[0].toLowerCase(),
                        "Practice " + gap.skillName() + " — Day " + (d + 1),
                        "Solve problems related to " + gap.skillName()
                                + ". Target: improve from " + gap.studentScore()
                                + " to " + gap.requiredScore(),
                        Math.min(dailyMinutes, 90),
                        gap.priority().equals("HIGH") ? 4 : 3,
                        gap.gapSize() / 3,
                        null
                ));
            }
        }

        return tasks;
    }

    // ── Inner records for LLM structured output ───────────────────────────

    public record GeneratedTaskList(List<GeneratedTask> tasks) {}

    public record GeneratedTask(
            int dayNumber,
            String skillName,
            String category,        // "dsa", "backend", "database"
            String taskTitle,
            String taskDescription,
            int estimatedMinutes,
            int difficultyLevel,    // 1-5
            int gapImpact,          // 1-10
            String ragSourceIds     // comma-separated job posting IDs that inspired this task
    ) {}
}
