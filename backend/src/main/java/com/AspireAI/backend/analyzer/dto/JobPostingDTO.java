package com.AspireAI.backend.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JobPostingDTO {
    private String id;
    private String companyName;
    private String roleTitle;
    private String location;
    private String requiredSkills;
    private String sourceUrl;
    private LocalDateTime scrapedAt;
}
