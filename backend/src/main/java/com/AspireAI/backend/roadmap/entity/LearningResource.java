package com.AspireAI.backend.roadmap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_resources", indexes = {
        @Index(name = "idx_skill_type", columnList = "skill_name, resource_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skillName;          

    @Column(nullable = false)
    private String category;            

    @Column(nullable = false)
    private String title;             

    @Column(columnDefinition = "text")
    private String description;        

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String resourceType;        

    @Column(nullable = false)
    private Integer difficultyLevel;    

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Double qualityScore; 
          
    @Column(name = "vector_store_id")
    private String vectorStoreId;      

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
