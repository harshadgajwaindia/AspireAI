package com.AspireAI.backend.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_skill_requirements",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"company_name", "skill_name"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySkillRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;     

    @Column(name = "skill_name", nullable = false)
    private String skillName;      

    @Column(nullable = false)
    private String category;       

    @Column(name = "minimum_score", nullable = false)
    private Integer minimumScore;  

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;   
}