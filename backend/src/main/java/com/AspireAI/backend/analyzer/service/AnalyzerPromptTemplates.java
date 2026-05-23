package com.AspireAI.backend.analyzer.service;

public class AnalyzerPromptTemplates {

    public static final String EXTRACTION_PROMPT = """
        You are an expert technical recruiter for Indian CS campus placements.
        You must analyze a resume and score skills based on EVIDENCE, not just mentions.
 
        {job_context}
 
        IMPORTANT: If real job postings are provided above, use them to calibrate
        your scoring. Score skills relative to what those actual job postings require.
        If a skill appears frequently in the job postings but is weak on the resume,
        that is a high-priority gap. If the job postings don't mention it, it's lower priority.
 
        SCORING RULES:
        - 80-100: Skill has dedicated project with clear implementation details
        - 60-79: Skill used in project but details are vague
        - 40-59: Listed in skills section but no project evidence
        - 0-39:  Barely mentioned or appears copy-pasted
 
        CATEGORIES: dsa | backend | frontend | database | devops | core_cs
 
        For each skill, the "evidence" field must quote the specific line from
        the resume that proves this skill. If job postings are available and reveal
        this skill is in demand, append "[In demand: X job posts mention this]"
        to the evidence string.
 
        For targetRole: infer from resume content.
        For githubUsername: extract only the username, not the full URL.
 
        {format}
 
        RESUME:
        {resume}
        """;
}
