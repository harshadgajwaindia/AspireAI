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
    private String companyName;          

    @Column(name = "role_title", nullable = false)
    private String roleTitle;          

    @Column(nullable = false)
    private String location;             

    @Column(name = "required_skills", columnDefinition = "text")
    private String requiredSkills;       

    @Column(name = "experience_range")
    private String experienceRange;      

    @Column(name = "salary_range")
    private String salaryRange;          

    @Column(name = "source_url")
    private String sourceUrl;           

    @Column(name = "source_platform")
    private String sourcePlatform;      

   
    @Column(name = "vector_store_id")
    private String vectorStoreId;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

    @PrePersist
    protected void onCreate() {
        if (scrapedAt == null) scrapedAt = LocalDateTime.now();
    }
}
