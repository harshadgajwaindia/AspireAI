package com.AspireAI.backend.interview.repositories;

import com.AspireAI.backend.interview.entity.InterviewQuestion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository
        extends JpaRepository<InterviewQuestion, Long> {

    // Fallback: get questions by skill area and difficulty when RAG returns empty
    @Query("""
        SELECT q FROM InterviewQuestion q
        WHERE q.skillArea = :skillArea
        AND q.difficultyLevel <= :maxDifficulty
        AND (q.companyTarget = :company OR q.companyTarget = 'ANY')
        ORDER BY q.avgScore DESC
        """)
    List<InterviewQuestion> findBySkillAndCompany(
            @Param("skillArea") String skillArea,
            @Param("maxDifficulty") Integer maxDifficulty,
            @Param("company") String company
    );

    // Increment usage count after a question is selected
    @Modifying
    @Transactional
    @Query("UPDATE InterviewQuestion q SET q.timesUsed = q.timesUsed + 1 WHERE q.id = :id")
    void incrementUsage(@Param("id") Long id);
}
