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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobRagService {

    private final VectorStore vectorStore;
    private final JobPostingRepository jobPostingRepo;

    // ── INGESTION ──────────────────────────────────────────────────────────

    /**
     * Called by the scraper service when a new job posting is fetched.
     * Converts the job description to a vector and stores it in PgVector.
     *
     * The "metadata" map lets us filter documents during retrieval without
     * doing a full scan. Think of it as indexed columns on the vector table.
     */
    public void ingestJobPosting(JobPosting posting, String fullDescription) {
        // Save first to generate UUID
        if (posting.getId() == null) {
            posting = jobPostingRepo.save(posting);
        }

        // Build the text we want to embed.
        // Include title + description because the title carries a lot of signal.
        String textToEmbed = String.format("""
            Company: %s
            Role: %s
            Location: %s
            Description: %s
            """,
                posting.getCompanyName(),
                posting.getRoleTitle(),
                posting.getLocation(),
                fullDescription
        );

        // Metadata stored alongside the vector — used for pre-filtering
        Map<String, Object> metadata = Map.of(
                "company",   posting.getCompanyName(),
                "role",      posting.getRoleTitle(),
                "location",  posting.getLocation(),
                "source",    posting.getSourcePlatform(),
                "date",      posting.getScrapedAt().toString(),
                "job_id",    posting.getId()
        );

        Document doc = new Document(textToEmbed, metadata);

        // Spring AI handles embedding generation + storage in one call
        vectorStore.add(List.of(doc));

        // Store the document ID back in MySQL for cross-referencing
        posting.setVectorStoreId(doc.getId());
        jobPostingRepo.save(posting);

        log.info("Ingested job posting: {} - {} [vectorId={}]",
                posting.getCompanyName(), posting.getRoleTitle(), doc.getId());
    }

    // ── RETRIEVAL ──────────────────────────────────────────────────────────

    /**
     * Main RAG retrieval method used by the Analyzer.
     *
     * Given a student's skill list and a target company, finds the most
     * semantically relevant recent job descriptions and formats them
     * as a context block ready to inject into a Gemini prompt.
     *
     * @param skillsSummary  e.g. "Java, Spring Boot, MySQL, DSA, React"
     * @param targetCompany  e.g. "TCS Digital"
     * @param topK           how many documents to retrieve (4-6 is usually ideal)
     * @return formatted string to inject into the LLM prompt
     */
    public String retrieveJobContext(String skillsSummary,
                                     String targetCompany,
                                     int topK) {
        log.info("RAG retrieval for company={} query={}", targetCompany, skillsSummary);

        // Check if we have enough data for this company
        long count = jobPostingRepo.countByCompanyNameContainingIgnoreCase(targetCompany);
        if (count == 0) {
            log.warn("No job postings found for {}. RAG context will be empty.", targetCompany);
            return "";
        }

        // Build the query: combine skills + company name for better retrieval
        // The query is embedded and compared against all stored job vectors
        String query = String.format(
                "Software engineer job requiring %s skills at %s India",
                skillsSummary, targetCompany
        );

        // FilterExpression limits the search to documents for this company only.
        // Without this filter, we'd get results from ALL companies — not useful.
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(
                        String.format("company == '%s'", targetCompany)
                )
                .similarityThreshold(0.65)  // only return docs with >65% similarity
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        if (results.isEmpty()) {
            log.warn("Vector search returned no results for {}", targetCompany);
            return "";
        }

        log.info("Retrieved {} relevant job postings for RAG context", results.size());

        // Format the retrieved documents into a readable context block
        return formatContext(results, targetCompany);
    }

    /**
     * Also used during project authenticity verification.
     * Finds job postings that use similar technologies to what's on the resume,
     * letting us verify if the claimed tech stack is plausible.
     *
     * e.g. "Built e-commerce app with Spring Boot + React + MySQL"
     * → finds real job descriptions that require this stack
     * → confirms the skill combination is realistic, not copy-pasted
     */
    public String retrieveProjectValidationContext(String projectDescription) {
        String query = "project using " + projectDescription;

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.60)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs.isEmpty()) return "";

        return docs.stream()
                .map(doc -> "- " + doc.getText().substring(0, Math.min(300, doc.getText().length())))
                .collect(Collectors.joining("\n"));
    }

    // ── HELPERS ────────────────────────────────────────────────────────────

    private String formatContext(List<Document> docs, String company) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "=== REAL %s JOB POSTINGS (retrieved from live data) ===\n\n",
                company.toUpperCase()
        ));

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            sb.append(String.format("--- Job Posting %d ---\n", i + 1));

            // Include metadata fields for additional context
            if (doc.getMetadata().containsKey("role")) {
                sb.append("Role: ").append(doc.getMetadata().get("role")).append("\n");
            }
            if (doc.getMetadata().containsKey("date")) {
                sb.append("Posted: ").append(doc.getMetadata().get("date")).append("\n");
            }

            // Truncate very long descriptions to control token count
            String text = doc.getText();
            sb.append(text, 0, Math.min(600, text.length()));
            if (text.length() > 600) sb.append("...");
            sb.append("\n\n");
        }

        sb.append("=== END OF JOB POSTINGS ===\n");
        return sb.toString();
    }

    /**
     * Utility: build a skill summary string from a list of skill names.
     * Used to construct the RAG query vector.
     */
    public static String buildSkillsSummary(List<String> skillNames) {
        return String.join(", ", skillNames);
    }

}
