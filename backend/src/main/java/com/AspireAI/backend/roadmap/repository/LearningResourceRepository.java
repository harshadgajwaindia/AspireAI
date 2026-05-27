package com.AspireAI.backend.roadmap.repository;

import com.AspireAI.backend.roadmap.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningResourceRepository
        extends JpaRepository<LearningResource, Long> {

    List<LearningResource> findBySkillNameAndDifficultyLevel(
            String skillName,
            Integer difficultyLevel
    );

  
    @Query("""
        SELECT r FROM LearningResource r
        WHERE r.skillName = :skillName
        AND r.qualityScore >= 0.7
        ORDER BY r.qualityScore DESC
        """)
    List<LearningResource> findTopResourcesForSkill(
            @Param("skillName") String skillName
    );
}