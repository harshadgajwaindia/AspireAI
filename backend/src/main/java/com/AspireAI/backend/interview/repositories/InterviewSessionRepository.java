package com.AspireAI.backend.interview.repositories;

import com.AspireAI.backend.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface InterviewSessionRepository
        extends JpaRepository<InterviewSession, UUID> {

    
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(UUID userId);

    
    Optional<InterviewSession> findFirstByUserIdAndStatus(
            UUID userId, InterviewSession.SessionStatus status
    );

  
    @Query("""
        SELECT s.targetCompany, AVG(s.overallScore)
        FROM InterviewSession s
        WHERE s.userId = :userId
        AND s.status = 'COMPLETED'
        AND s.overallScore IS NOT NULL
        GROUP BY s.targetCompany
        """)
    List<Object[]> getAvgScoreByCompany(@Param("userId") UUID userId);
}
