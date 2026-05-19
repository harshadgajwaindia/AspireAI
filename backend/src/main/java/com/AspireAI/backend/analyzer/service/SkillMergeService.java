package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.ExtractedSkillsDTO;
import com.AspireAI.backend.analyzer.dto.GitHubProfileDTO;
import com.AspireAI.backend.analyzer.dto.SkillEntryDTO;
import com.AspireAI.backend.analyzer.dto.SkillProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Combines the LLM-extracted skills with GitHub corroboration.
 *
 * The core insight: a resume says "I know Java" but GitHub shows
 * 8 Java repositories. That's evidence. We boost the score accordingly.
 *
 * Conversely, if a student claims "I know AWS" but has zero repos
 * with any cloud config files, the score stays as-is (we don't reduce it,
 * because absence of GitHub evidence isn't proof of absence of skill).
 *
 * Score adjustment table:
 * 0 repos in language    → +0
 * 1-2 repos              → +5
 * 3-5 repos              → +10
 * 6+ repos               → +15
 * Score is always capped at 100.
 */
@Slf4j
@Service
public class SkillMergeService {

    // Maps skill names to GitHub language names
    private static final Map<String, String> SKILL_TO_LANGUAGE = Map.ofEntries(
            Map.entry("spring boot", "Java"),
            Map.entry("java", "Java"),
            Map.entry("spring ai", "Java"),
            Map.entry("hibernate", "Java"),
            Map.entry("react", "JavaScript"),
            Map.entry("javascript", "JavaScript"),
            Map.entry("node.js", "JavaScript"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("python", "Python"),
            Map.entry("django", "Python")
    );

    public SkillProfileDTO merge(ExtractedSkillsDTO llmOutput, GitHubProfileDTO github) {
        log.info("Merging LLM skills with GitHub data. GitHub has data: {}",
                github.hasData());

        List<SkillEntryDTO> enrichedSkills = llmOutput.skills().stream()
                .map(skill -> adjustScore(skill, github))
                .toList();

        return new SkillProfileDTO(
                llmOutput.targetRole(),
                enrichedSkills,
                llmOutput.projectNames(),
                github,
                llmOutput.summary()
        );
    }

    private SkillEntryDTO adjustScore(SkillEntryDTO skill, GitHubProfileDTO github) {
        if (!github.hasData()) {
            return skill; // no GitHub data — return as-is
        }

        String githubLang = SKILL_TO_LANGUAGE.get(skill.name().toLowerCase());
        if (githubLang == null) {
            return skill; // skill doesn't map to a GitHub language
        }

        int repoCount = github.languageFrequency().getOrDefault(githubLang, 0);

        int boost = switch (repoCount) {
            case 0 -> 0;
            case 1, 2 -> 5;
            case 3, 4, 5 -> 10;
            default -> 15; // 6+ repos
        };

        if (boost > 0) {
            int boostedScore = Math.min(100, skill.score() + boost);
            log.debug("Boosted {} score: {} → {} ({} {} repos on GitHub)",
                    skill.name(), skill.score(), boostedScore, repoCount, githubLang);

            return new SkillEntryDTO(
                    skill.name(),
                    skill.category(),
                    boostedScore,
                    skill.evidence() + String.format(" [GitHub: %d %s repos]",
                            repoCount, githubLang)
            );
        }

        return skill;
    }
}