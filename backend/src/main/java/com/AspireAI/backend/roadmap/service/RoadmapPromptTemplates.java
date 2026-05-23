package com.AspireAI.backend.roadmap.service;

public class RoadmapPromptTemplates {

    public static final String GENERATE_ROADMAP_PROMPT = """
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
            
            REFERENCE LINKS GENERATION:
            For each task, you MUST generate a direct, high-quality, free, open-source educational URL (referenceUrl) and a resourceType (ARTICLE, VIDEO, or DOCUMENTATION).
            Examples:
            - DSA/Leetcode tasks: LeetCode tag URLs like "https://leetcode.com/tag/binary-tree/" or "https://leetcode.com/tag/dynamic-programming/"
            - Java/Spring Boot: official Spring Guides like "https://spring.io/guides/gs/rest-service/" or MDN/Oracle Java guides like "https://docs.oracle.com/javase/tutorial/"
            - Web Development: MDN Web Docs like "https://developer.mozilla.org/en-US/docs/Web/JavaScript"
            - Databases/SQL: standard tutorials like "https://www.w3schools.com/sql/" or official PostgreSQL documentation.
            - Others: high-quality Wikipedia pages like "https://en.wikipedia.org/wiki/System_design" or "https://en.wikipedia.org/wiki/Software_architecture".
            Do NOT output generic search links, fake URLs, or placeholder links.
 
            %s
            """;
}
