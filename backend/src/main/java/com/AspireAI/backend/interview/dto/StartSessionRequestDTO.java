package com.AspireAI.backend.interview.dto;

import java.util.UUID;

/**
 * Sent by frontend to start a new interview session.
 * skillGapJson is the serialized output from the Analyzer Agent —
 * the Mock Interviewer uses it to target the student's weak areas.
 */
public record StartSessionRequestDTO(
        UUID userId,
        String targetCompany,                       // "TCS Digital"
        String interviewType,                        // "TECHNICAL", "HR", "MIXED"
        Integer totalQuestions,                      // 5 or 10
        String skillGapJson                          // from Analyzer — optional, improves targeting
) {}