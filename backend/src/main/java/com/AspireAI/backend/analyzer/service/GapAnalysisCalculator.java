package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.SkillEntryDTO;
import com.AspireAI.backend.analyzer.dto.SkillGapDTO;
import com.AspireAI.backend.analyzer.entity.CompanySkillRequirement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GapAnalysisCalculator {

    public List<SkillGapDTO> calculateGaps(
            List<SkillEntryDTO> userSkills,
            List<CompanySkillRequirement> staticReqs,
            List<GapAnalysisRAGExtractor.DynamicSkillRequirement> dynamicReqs) {

        Map<String, Integer> userSkillScores = new HashMap<>();
        for (SkillEntryDTO s : userSkills) {
            userSkillScores.put(s.name().toLowerCase(), s.score());
        }

        Map<String, SkillRequirementMerged> merged = mergeRequirements(staticReqs, dynamicReqs);
        List<SkillGapDTO> gaps = new ArrayList<>();

        for (SkillRequirementMerged req : merged.values()) {
            int currentScore = userSkillScores.getOrDefault(req.skillName.toLowerCase(), 0);
            int requiredScore = req.minimumScore;

            if (currentScore < requiredScore) {
                int gap = requiredScore - currentScore;
                String priority = classifyPriority(gap, currentScore);

                gaps.add(new SkillGapDTO(
                        req.skillName, req.category, requiredScore, currentScore, gap, priority, req.reason
                ));
            }
        }
        return gaps;
    }

    public int calculateReadiness(List<SkillGapDTO> gaps) {
        if (gaps.isEmpty()) return 100;
        int maxDeduction = 60;
        int deduction = 0;
        for (SkillGapDTO gap : gaps) {
            if ("HIGH".equals(gap.priority())) deduction += 10;
            else if ("MEDIUM".equals(gap.priority())) deduction += 5;
            else deduction += 2;
        }
        return Math.max(0, 100 - Math.min(deduction, maxDeduction));
    }

    private Map<String, SkillRequirementMerged> mergeRequirements(
            List<CompanySkillRequirement> staticReqs,
            List<GapAnalysisRAGExtractor.DynamicSkillRequirement> dynamicReqs) {

        Map<String, SkillRequirementMerged> map = new HashMap<>();

        for (CompanySkillRequirement req : staticReqs) {
            map.put(req.getSkillName().toLowerCase(), new SkillRequirementMerged(
                    req.getSkillName(), req.getCategory(), req.getMinimumScore(), "Standard company requirement"
            ));
        }

        for (GapAnalysisRAGExtractor.DynamicSkillRequirement dReq : dynamicReqs) {
            String key = dReq.skillName().toLowerCase();
            if (map.containsKey(key)) {
                SkillRequirementMerged existing = map.get(key);
                if (dReq.minimumScore() > existing.minimumScore) {
                    existing.minimumScore = dReq.minimumScore();
                    existing.reason = "Elevated by recent job postings: " + dReq.reason();
                }
            } else {
                map.put(key, new SkillRequirementMerged(
                        dReq.skillName(), dReq.category(), dReq.minimumScore(), "Identified in recent job postings: " + dReq.reason()
                ));
            }
        }
        return map;
    }

    private String classifyPriority(int gap, int currentScore) {
        if (currentScore == 0 && gap >= 50) return "HIGH";
        if (gap >= 30) return "HIGH";
        if (gap >= 15) return "MEDIUM";
        return "LOW";
    }

    private static class SkillRequirementMerged {
        String skillName; String category; int minimumScore; String reason;
        SkillRequirementMerged(String n, String c, int s, String r) {
            this.skillName = n; this.category = c; this.minimumScore = s; this.reason = r;
        }
    }
}