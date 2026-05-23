package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.entity.InterviewQuestion;
import com.AspireAI.backend.interview.repositories.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewRagService {

    private final VectorStore vectorStore;
    private final InterviewQuestionRepository questionRepo;
    private final InterviewQuestionIngester ingester;

    public void ingestQuestion(InterviewQuestion question) {
        ingester.ingestQuestion(question);
    }

    public List<InterviewQuestion> findRelevantQuestions(String skillArea, String targetCompany, int difficultyLevel, List<String> alreadyAsked) {
        String query = String.format("%s interview question at difficulty %d for %s India placement", skillArea, difficultyLevel, targetCompany);
        String filter = String.format("source_type == 'question_bank' && difficulty == '%d'", difficultyLevel);

        SearchRequest request = SearchRequest.builder().query(query).topK(8).filterExpression(filter).similarityThreshold(0.55).build();
        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs.isEmpty()) {
            log.info("No RAG results for skill={}, falling back to DB query", skillArea);
            return questionRepo.findBySkillAndCompany(skillArea, difficultyLevel, targetCompany);
        }

        return docs.stream()
                .map(doc -> {
                    String qId = (String) doc.getMetadata().get("question_id");
                    return qId != null ? questionRepo.findById(Long.parseLong(qId)).orElse(null) : null;
                })
                .filter(q -> q != null && !alreadyAsked.contains(q.getQuestionText()))
                .limit(5).collect(Collectors.toList());
    }

    public String retrieveInterviewContext(String targetCompany, String interviewType) {
        String query = String.format("%s interview questions asked in %s campus placement India", interviewType.toLowerCase(), targetCompany);
        SearchRequest request = SearchRequest.builder().query(query).topK(5).filterExpression("source_type == 'job_posting'").similarityThreshold(0.58).build();
        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs.isEmpty()) return "";
        return docs.stream().map(doc -> doc.getText().substring(0, Math.min(250, doc.getText().length()))).collect(Collectors.joining("\n---\n"));
    }

    public String buildRagInsight(String skillArea, String targetCompany) {
        String query = skillArea + " " + targetCompany + " interview requirement";
        SearchRequest request = SearchRequest.builder().query(query).topK(3).filterExpression("source_type == 'job_posting'").similarityThreshold(0.62).build();
        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs.isEmpty()) return null;
        return String.format("This topic appeared in %d recent %s job postings", docs.size(), targetCompany);
    }
}