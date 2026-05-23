package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.entity.InterviewQuestion;
import com.AspireAI.backend.interview.repositories.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionIngester {

    private final VectorStore vectorStore;
    private final InterviewQuestionRepository questionRepo;

    public void ingestQuestion(InterviewQuestion question) {
        if (question.getId() == null) question = questionRepo.save(question);

        String textToEmbed = String.format(
                "Interview question | Company: %s | Skill: %s | Type: %s | Difficulty: %d/5 | Question: %s | Key Concepts: %s",
                question.getCompanyTarget() != null ? question.getCompanyTarget() : "ANY",
                question.getSkillArea(), question.getQuestionType(), question.getDifficultyLevel(),
                question.getQuestionText(), question.getKeyConcepts() != null ? question.getKeyConcepts() : ""
        );

        Document doc = new Document(textToEmbed, Map.of(
                "source_type", "question_bank", "question_id", question.getId().toString(),
                "skill_area", question.getSkillArea(), "question_type", question.getQuestionType(),
                "difficulty", String.valueOf(question.getDifficultyLevel()),
                "company", question.getCompanyTarget() != null ? question.getCompanyTarget() : "ANY"
        ));

        vectorStore.add(List.of(doc));
        question.setVectorStoreId(doc.getId());
        questionRepo.save(question);
        log.debug("Ingested question id={} skill={}", question.getId(), question.getSkillArea());
    }
}
