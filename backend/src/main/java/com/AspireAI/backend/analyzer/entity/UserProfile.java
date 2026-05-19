package com.AspireAI.backend.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores the analyzed profile for a user.
 * skillsJson and gapReportJson are stored as JSON columns in MySQL —
 * this avoids creating 10 normalized tables for data that is naturally
 * document-shaped and read as a whole unit, never queried field-by-field.
 *
 * For a production system you'd normalize more, but for MVP this
 * is dramatically simpler and still fast.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;           

    @Column(nullable = false)
    private String targetCompany;  

    @Column(nullable = false)
    private Integer overallReadiness; 

    private String githubUsername;

    private String linkedinUrl;
    
    private String resumeUrl;       

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String skillsJson;       

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String gapReportJson;    

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}