package com.AspireAI.backend.roadmap.service;

import com.AspireAI.backend.roadmap.entity.LearningResource;
import com.AspireAI.backend.roadmap.repository.LearningResourceRepository;
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
public class LearningResourceRagService {

    private final VectorStore vectorStore;
    private final LearningResourceRepository resourceRepo;

    // ── INGESTION ──────────────────────────────────────────────────────────

    public void ingestResource(LearningResource resource) {
        String textToEmbed = String.format(
                "Topic: %s | Type: %s | Difficulty: %d/5 | Title: %s | %s",
                resource.getSkillName(),
                resource.getResourceType(),
                resource.getDifficultyLevel(),
                resource.getTitle(),
                resource.getDescription() != null ? resource.getDescription() : ""
        );

        Document doc = new Document(textToEmbed, Map.of(
                "skill",      resource.getSkillName(),
                "category",   resource.getCategory(),
                "type",       resource.getResourceType(),
                "difficulty", resource.getDifficultyLevel().toString(),
                "url",        resource.getUrl(),
                "resource_id", resource.getId().toString()
        ));

        vectorStore.add(List.of(doc));

        resource.setVectorStoreId(doc.getId());
        resourceRepo.save(resource);
        log.debug("Ingested resource: {}", resource.getTitle());
    }

    // ── RETRIEVAL ──────────────────────────────────────────────────────────

    /**
     * Finds the most relevant learning resource for a specific task.
     *
     * @param taskDescription  what the student needs to learn
     * @param category         filter by category (dsa, backend, etc.)
     * @param difficulty       1-5 difficulty filter
     * @return best matching LearningResource, or null if none found
     */
    public LearningResource findBestResource(String taskDescription,
                                             String category,
                                             int difficulty) {
        SearchRequest request = SearchRequest.builder()
                .query(taskDescription)
                .topK(3)
                .filterExpression(String.format(
                        "category == '%s' && difficulty == '%d'",
                        category, difficulty
                ))
                .similarityThreshold(0.60)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        if (results.isEmpty()) {
            // Retry without difficulty filter (more lenient)
            request = SearchRequest.builder()
                    .query(taskDescription)
                    .topK(1)
                    .filterExpression(String.format("category == '%s'", category))
                    .similarityThreshold(0.55)
                    .build();
            results = vectorStore.similaritySearch(request);
        }

        if (results.isEmpty()) return null;

        // Get the DB record for the top result to include URL and full metadata
        String resourceId = (String) results.get(0).getMetadata().get("resource_id");
        if (resourceId == null) return null;

        return resourceRepo.findById(Long.parseLong(resourceId)).orElse(null);
    }

    /**
     * Retrieves context about available resources for a topic.
     * Used by the RoadmapGeneratorService to tell Gemini what resources
     * are available when generating task descriptions.
     */
    public String getResourceContext(String topic, String category) {
        SearchRequest request = SearchRequest.builder()
                .query("learning resource for " + topic)
                .topK(4)
                .filterExpression(String.format("category == '%s'", category))
                .similarityThreshold(0.55)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);
        if (docs.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("Available resources:\n");
        for (Document doc : docs) {
            sb.append("- ").append(doc.getText(), 0,
                    Math.min(150, doc.getText().length())).append("\n");
        }
        return sb.toString();
    }
}

