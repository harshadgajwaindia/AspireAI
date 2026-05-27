package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.dto.JobPostingDTO;
import com.AspireAI.backend.analyzer.entity.JobPosting;
import com.AspireAI.backend.analyzer.repoitory.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class JobRecommendationService {

    private final JobPostingRepository jobPostingRepository;

   
    public List<JobPostingDTO> getRecentJobRecommendations(int limit) {
        int capped = Math.min(limit, 50);
        List<JobPosting> postings = jobPostingRepository.findAllByOrderByScrapedAtDesc(PageRequest.of(0, capped));
        return postings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private JobPostingDTO toDto(JobPosting posting) {
        return JobPostingDTO.builder()
                .id(posting.getId())
                .companyName(posting.getCompanyName())
                .roleTitle(posting.getRoleTitle())
                .location(posting.getLocation())
                .requiredSkills(posting.getRequiredSkills())
                .sourceUrl(posting.getSourceUrl())
                .build();
    }
}
