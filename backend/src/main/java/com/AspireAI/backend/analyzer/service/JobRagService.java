package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.entity.JobPosting;
import com.AspireAI.backend.analyzer.repoitory.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobRagService {

    private final VectorStore vectorStore;
    private final JobPostingRepository jobPostingRepo;

    public void ingestJobPosting(JobPosting posting, String fullDescription) {
        if (posting.getId() == null) posting = jobPostingRepo.save(posting);

        String textToEmbed = String.format("Company: %s\nRole: %s\nLocation: %s\nDescription: %s\n",
                posting.getCompanyName(), posting.getRoleTitle(), posting.getLocation(), fullDescription);

        Map<String, Object> metadata = Map.of(
                "company", posting.getCompanyName(), "role", posting.getRoleTitle(),
                "location", posting.getLocation(), "source", posting.getSourcePlatform(),
                "date", posting.getScrapedAt().toString(), "job_id", posting.getId()
        );

        Document doc = new Document(textToEmbed, metadata);
        vectorStore.add(List.of(doc));

        posting.setVectorStoreId(doc.getId());
        jobPostingRepo.save(posting);
        log.info("Ingested job posting: {} - {} [vectorId={}]", posting.getCompanyName(), posting.getRoleTitle(), doc.getId());
    }

    public String retrieveJobContext(String skillsSummary, String targetCompany, String preparationType, int topK) {
        log.info("RAG retrieval for company={} type={} query={}", targetCompany, preparationType, skillsSummary);
        boolean isField = "FIELD".equalsIgnoreCase(preparationType);

        long count = isField ? jobPostingRepo.count() : jobPostingRepo.countByCompanyNameContainingIgnoreCase(targetCompany);
        if (count == 0) return "";

        String query = isField ? String.format("Software engineer job requiring %s skills in India", skillsSummary)
                : String.format("Software engineer job requiring %s skills at %s India", skillsSummary, targetCompany);

        SearchRequest.Builder builder = SearchRequest.builder().query(query).topK(topK).similarityThreshold(0.65);
        if (!isField) builder.filterExpression(String.format("company == '%s'", targetCompany));

        List<Document> results = vectorStore.similaritySearch(builder.build());
        if (results.isEmpty()) return "";

        return JobRagFormatter.formatContext(results, targetCompany);
    }

    public String retrieveJobContext(String skillsSummary, String targetCompany, int topK) {
        return retrieveJobContext(skillsSummary, targetCompany, "COMPANY", topK);
    }

    public String retrieveProjectValidationContext(String projectDescription) {
        SearchRequest request = SearchRequest.builder().query("project using " + projectDescription)
                .topK(3).similarityThreshold(0.60).build();
        List<Document> docs = vectorStore.similaritySearch(request);
        return JobRagFormatter.formatValidationContext(docs);
    }

    public static String buildSkillsSummary(List<String> skillNames) {
        return String.join(", ", skillNames);
    }
}
