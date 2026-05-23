package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.entity.InterviewQuestion;
import com.AspireAI.backend.interview.repositories.InterviewQuestionRepository;
import com.AspireAI.backend.analyzer.entity.JobPosting;
import com.AspireAI.backend.analyzer.repoitory.JobPostingRepository;
import com.AspireAI.backend.analyzer.service.JobRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewQuestionSeeder implements ApplicationRunner {

    private final InterviewQuestionRepository questionRepo;
    private final InterviewRagService ragService;
    private final JobPostingRepository jobPostingRepo;
    private final JobRagService jobRagService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedQuestions();
        seedJobPostings();
    }

    private void seedQuestions() {
        if (questionRepo.count() == 0) {
            log.info("=== Seeding Aspire AI general guidance questions into MySQL & PgVector ===");
            List<InterviewQuestion> seedQuestions = InterviewSeedData.getQuestionsToSeed();
            for (InterviewQuestion q : seedQuestions) {
                try {
                    ragService.ingestQuestion(q);
                } catch (Exception e) {
                    log.error("Failed to ingest question '{}' into vector store: {}", q.getQuestionText(), e.getMessage());
                }
            }
            log.info("=== Questions seed complete. Ingested {} questions ===", seedQuestions.size());
        } else {
            log.info("Database question bank is already populated.");
        }
    }

    private void seedJobPostings() {
        if (jobPostingRepo.count() == 0) {
            log.info("=== Seeding Aspire AI placement & career pathway job postings into MySQL & PgVector ===");
            List<InterviewSeedData.JobPostingSeed> seeds = InterviewSeedData.getJobPostingsToSeed();
            for (InterviewSeedData.JobPostingSeed seed : seeds) {
                try {
                    JobPosting jp = JobPosting.builder()
                            .companyName(seed.companyName).roleTitle(seed.roleTitle).location(seed.location)
                            .requiredSkills(seed.requiredSkills).experienceRange(seed.experienceRange)
                            .salaryRange(seed.salaryRange).sourceUrl(seed.sourceUrl).sourcePlatform("aspire")
                            .scrapedAt(LocalDateTime.now()).build();
                    jobRagService.ingestJobPosting(jp, seed.description);
                } catch (Exception e) {
                    log.error("Failed to ingest job posting '{}': {}", seed.companyName, e.getMessage());
                }
            }
            log.info("=== Job postings seed complete. Ingested {} postings ===", seeds.size());
        } else {
            log.info("Database job postings are already populated.");
        }
    }
}
