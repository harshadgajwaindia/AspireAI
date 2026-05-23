package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.analyzer.dto.SkillGapDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapReportDTO;
import com.AspireAI.backend.roadmap.dto.GeneratedTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RoadmapRuleBasedGenerator {

    public static List<GeneratedTask> generateRuleBasedTasks(
            SkillGapReportDTO gapReport, int totalDays, int dailyMinutes) {
        log.warn("Using rule-based fallback for task generation");

        List<GeneratedTask> tasks = new ArrayList<>();
        List<SkillGapDTO> gaps = gapReport.topGaps();
        if (gaps.isEmpty()) return tasks;

        int totalGap = gaps.stream().mapToInt(SkillGapDTO::gapSize).sum();
        int day = 1;

        for (SkillGapDTO gap : gaps) {
            int daysForSkill = Math.max(1, (int) Math.round((double) gap.gapSize() / totalGap * totalDays));

            for (int d = 0; d < daysForSkill && day <= totalDays; d++, day++) {
                tasks.add(new GeneratedTask(
                        day, gap.skillName(), gap.skillName().split("-")[0].toLowerCase(),
                        "Practice " + gap.skillName() + " — Day " + (d + 1),
                        "Solve problems related to " + gap.skillName() + ". Target: improve from " + gap.studentScore() + " to " + gap.requiredScore(),
                        Math.min(dailyMinutes, 90), gap.priority().equals("HIGH") ? 4 : 3,
                        gap.gapSize() / 3, null, "https://wikipedia.org/wiki/" + gap.skillName().replace(" ", "_"),
                        "DOCUMENTATION"
                ));
            }
        }
        return tasks;
    }
}
