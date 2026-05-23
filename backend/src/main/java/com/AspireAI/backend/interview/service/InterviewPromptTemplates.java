package com.AspireAI.backend.interview.service;

public class InterviewPromptTemplates {

    public static final String QUESTION_PROMPT = """
            You are a senior technical interviewer conducting a %s interview targeted towards %s.
            This is question %d of %d.

            STUDENT'S SKILL GAP CONTEXT:
            %s

            STUDENT'S PUBLIC GITHUB PROFILE HIGHLIGHTS (weave these in constructively if relevant to tech/languages):
            %s

            RECENT PATHWAY/COMPANY INTERVIEW PATTERNS (from live job postings):
            %s

            %s

            Generate question %d for skill area: %s (difficulty %d/5).

            Rules:
            - Question must be highly engaging, scenario-based, and practical.
            - If student's GitHub highlights show active languages/projects matching or relating to the skill area, weave it in (e.g. "I noticed your GitHub has repositories in Java. How would you...").
            - For TECHNICAL: present a creative real-world tech scenario with a twist or edge case.
            - For BEHAVIORAL: use STAR format but pose challenging, high-stakes situations.
            - For CODING: describe an interesting, non-standard problem to solve.
            - Do NOT ask a question already asked this session
            - Already asked topics: %s
            - Keep question under 80 words
            - Give it personality, like a seasoned tech mentor challenging the candidate.

            %s
            """;

    public static final String EVALUATION_PROMPT = """
            You are evaluating a mock interview answer for an Indian CS campus placement.

            QUESTION: %s
            QUESTION TYPE: %s
            SKILL AREA: %s
            DIFFICULTY: %d/5

            STUDENT'S ANSWER:
            %s

            IDEAL ANSWER HINTS (what a strong answer should cover):
            %s

            Evaluate the answer strictly but fairly. Provide highly engaging, constructive feedback.
            Adopt a constructive but playful tone, similar to a seasoned tech mentor who challenges candidates to think deeper.
            Indian CS students preparing for placements need ACTIONABLE, insightful feedback.

            Scoring guide:
            9-10: Covers all key concepts, clear communication, good examples
            7-8:  Covers most concepts, minor gaps or unclear explanation
            5-6:  Covers some concepts but missing important points
            3-4:  Partially relevant answer, significant gaps
            1-2:  Largely incorrect or irrelevant
            0:    No meaningful answer

            Grade labels: "Excellent" (9-10), "Good" (7-8),
                          "Needs Work" (5-6), "Insufficient" (0-4)

            For "idealAnswer": write a concise model answer (under 150 words).
            For "whatWasMissed": be specific about WHAT was missing, not just "explain more".
            For "followUpHint": suggest what a real interviewer would probe next.

            %s
            """;

    public static final String HOLISTIC_PROMPT = """
            You are providing comprehensive interview feedback for an Indian CS student
            who just completed a %s mock interview for %s.

            FULL INTERVIEW TRANSCRIPT:
            %s

            AVERAGE SCORE: %.1f / 10

            Provide holistic feedback considering:
            - Consistency across questions (did they improve or decline?)
            - Communication clarity (were answers concise or rambling?)
            - Depth of knowledge vs surface-level answers
            - Whether weak areas are addressable with short-term study

            For "overallScore" (0-100): translate the per-question performance to a
            placement readiness percentage for %s specifically.
            "Ready" = 75+, "Almost Ready" = 55-74, "Needs Work" = below 55.

            For "recommendedTopics": be specific (e.g., "Practice 20 medium Tree problems",
            not just "study trees").

            For "confidenceLevel": assess how confidently they communicated answers
            regardless of correctness.

            %s
            """;
}
