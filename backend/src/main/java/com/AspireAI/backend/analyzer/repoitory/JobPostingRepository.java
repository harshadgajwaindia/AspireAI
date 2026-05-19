package com.AspireAI.backend.analyzer.repoitory;

import com.AspireAI.backend.analyzer.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, String> {

    /**
     * Fetch recent postings for a company — used to seed the RAG context
     * before gap analysis. We only want recent ones (last 60 days) because
     * older job posts may reflect outdated skill requirements.
     */
    @Query("""
        SELECT j FROM JobPosting j
        WHERE LOWER(j.companyName) LIKE LOWER(CONCAT('%', :company, '%'))
        AND j.scrapedAt >= :since
        ORDER BY j.scrapedAt DESC
        """)
    List<JobPosting> findRecentByCompany(
            @Param("company") String company,
            @Param("since") LocalDateTime since
    );

    /**
     * Count how many postings we have for a company.
     * Used to decide whether to fall back to generic requirements.
     */
    long countByCompanyNameContainingIgnoreCase(String companyName);

}
