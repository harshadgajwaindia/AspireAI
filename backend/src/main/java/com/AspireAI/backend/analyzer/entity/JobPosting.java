package com.AspireAI.backend.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings", indexes = {
        @Index(name = "idx_company_date", columnList = "company_name, scraped_at"),
        @Index(name = "idx_location", columnList = "location")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_name", nullable = false)
    private String companyName;          // "TCS", "Infosys"

    @Column(name = "role_title", nullable = false)
    private String roleTitle;            // "Digital Trainee", "Specialist Programmer"

    @Column(nullable = false)
    private String location;             // "Bhopal", "Bangalore", "Remote"

    @Column(name = "required_skills", columnDefinition = "text")
    private String requiredSkills;       // comma-separated skill tags for quick filtering

    @Column(name = "experience_range")
    private String experienceRange;      // "0-2 years", "Fresher"

    @Column(name = "salary_range")
    private String salaryRange;          // "6-12 LPA"

    @Column(name = "source_url")
    private String sourceUrl;            // original Naukri/LinkedIn URL

    @Column(name = "source_platform")
    private String sourcePlatform;       // "naukri", "linkedin"

    /**
     * This is the foreign key into PgVector's vector_store table.
     * Spring AI stores documents with metadata, and we use the job posting id
     * as the document id in PgVector so we can cross-reference.
     */
    @Column(name = "vector_store_id")
    private String vectorStoreId;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

    @PrePersist
    protected void onCreate() {
        if (scrapedAt == null) scrapedAt = LocalDateTime.now();
    }
}
