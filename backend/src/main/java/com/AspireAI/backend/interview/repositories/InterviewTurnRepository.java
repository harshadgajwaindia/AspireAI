package com.AspireAI.backend.interview.repositories;

import com.AspireAI.backend.interview.entity.InterviewTurn;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewTurnRepository
        extends JpaRepository<InterviewTurn, UUID> {

    List<InterviewTurn> findBySessionIdOrderByQuestionNumberAsc(UUID sessionId);

   
    @Query("""
        SELECT t FROM InterviewTurn t
        WHERE t.session.id = :sessionId
        AND t.studentAnswer IS NULL
        ORDER BY t.questionNumber ASC
        """)
    Optional<InterviewTurn> findFirstUnansweredTurn(@Param("sessionId") UUID sessionId);

    // Questions already asked in this session — used to avoid repeats in RAG
    @Query("""
        SELECT t.question FROM InterviewTurn t
        WHERE t.session.id = :sessionId
        """)
    List<String> findAskedQuestions(@Param("sessionId") UUID sessionId);

    // Mark turn answered + set score in one query
    @Modifying
    @Transactional
    @Query("""
        UPDATE InterviewTurn t
        SET t.studentAnswer = :answer,
            t.answerTimeSeconds = :secs,
            t.answeredAt = CURRENT_TIMESTAMP
        WHERE t.id = :turnId
        """)
    int saveAnswer(
            @Param("turnId") UUID turnId,
            @Param("answer") String answer,
            @Param("secs") Integer secs
    );
}
