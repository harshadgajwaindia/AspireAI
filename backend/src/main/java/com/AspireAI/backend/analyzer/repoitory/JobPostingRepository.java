package com.AspireAI.backend.analyzer.repoitory;

import com.AspireAI.backend.analyzer.entity.JobPosting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, String> {

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

    long countByCompanyNameContainingIgnoreCase(String companyName);

    List<JobPosting> findAllByOrderByScrapedAtDesc(Pageable pageable);
}
