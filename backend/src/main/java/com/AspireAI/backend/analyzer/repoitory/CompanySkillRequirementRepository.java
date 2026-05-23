package com.AspireAI.backend.analyzer.repoitory;

import com.AspireAI.backend.analyzer.entity.CompanySkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanySkillRequirementRepository extends JpaRepository<CompanySkillRequirement, Long> {

    List<CompanySkillRequirement> findByCompanyName(String companyName);

    List<CompanySkillRequirement> findByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyName(String companyName);
}