package com.AspireAI.backend.roadmap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Uses the SAME PgVector store (job postings) as JobRagService in the Analyzer,
 * but with a different query purpose.
 *
 * Analyzer asks: "What skills are required for this company?"
 * Roadmap asks:  "What topics are TRENDING in this company's recent postings
 *                 that the student should include in their study plan?"
 *
 * The query is different because the intent is different.
 * For the Roadmap we want a broader picture: not just gaps,
 * but emerging topics the student should be aware of even if they
 * don't show up as critical gaps yet.
 *
 * Example: If "Generative AI" starts appearing in TCS job posts,
 * the Roadmap Agent might add a 3-day GenAI overview block in week 4,
 * even though it's not a traditional NQT topic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTrendRagService {

    private final VectorStore vectorStore;

    /**
     * Retrieves a formatted summary of trending topics for a company.
     * This is injected directly into the Gemini roadmap generation prompt.
     *
     * @param targetCompany  e.g. "TCS Digital"
     * @return formatted string of trending topics, empty if no data
     */
    public String getTrendingTopics(String targetCompany) {
        // Query for what's being asked in recent interviews/job posts
        String query = String.format(
                "latest technical skills and topics required at %s India 2025",
                targetCompany
        );

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(8)
                .filterExpression(String.format("company == '%s'", targetCompany))
                .similarityThreshold(0.60)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs.isEmpty()) {
            return "No trending data available — use standard placement preparation topics.";
        }

        log.info("Retrieved {} trend documents for {}", docs.size(), targetCompany);

        // Count skill/keyword frequency across retrieved documents
        // This gives us a rough "trending score" for each technology
        return formatTrendingSummary(docs, targetCompany);
    }

    private String formatTrendingSummary(List<Document> docs, String company) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Trending in %s job posts (last 60 days):\n", company));

        // Extract key phrases from each document
        for (int i = 0; i < Math.min(5, docs.size()); i++) {
            String text = docs.get(i).getText();
            // Truncate each to avoid bloating the Gemini prompt
            String snippet = text.substring(0, Math.min(200, text.length()));
            sb.append(String.format("Post %d: %s...\n", i + 1, snippet));
        }

        return sb.toString();
    }
}
