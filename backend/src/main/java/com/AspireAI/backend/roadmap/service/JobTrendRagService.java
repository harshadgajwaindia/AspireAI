package com.AspireAI.backend.roadmap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class JobTrendRagService {

    private final VectorStore vectorStore;

   
    public String getTrendingTopics(String targetCompany) {
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

    
        return formatTrendingSummary(docs, targetCompany);
    }

    private String formatTrendingSummary(List<Document> docs, String company) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Trending in %s job posts (last 60 days):\n", company));

       
        for (int i = 0; i < Math.min(5, docs.size()); i++) {
            String text = docs.get(i).getText();
            String snippet = text.substring(0, Math.min(200, text.length()));
            sb.append(String.format("Post %d: %s...\n", i + 1, snippet));
        }

        return sb.toString();
    }
}
