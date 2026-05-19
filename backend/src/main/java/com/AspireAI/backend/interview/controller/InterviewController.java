package com.AspireAI.backend.interview.controller;

import com.AspireAI.backend.interview.dto.*;
import com.AspireAI.backend.interview.service.InterviewAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Interview Agent.
 *
 * All endpoints:
 *
 * POST  /api/v1/interview/start              → Start session, get first question
 * POST  /api/v1/interview/answer             → Submit answer, get feedback + next Q
 * GET   /api/v1/interview/result/{sessionId} → Full session report
 * GET   /api/v1/interview/history/{userId}   → List of past completed sessions
 * GET   /api/v1/interview/health             → Health check
 *
 * Frontend flow:
 * 1. User clicks "Start Interview" → POST /start → render first QuestionDTO
 * 2. User types answer → POST /answer → render TurnFeedbackDTO
 * 3. If TurnFeedbackDTO.nextQuestion == null → interview complete → redirect to /result
 * 4. GET /result/{sessionId} → render SessionResultDTO on report page
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/interview")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")   // Vite dev server
public class InterviewController {

    private final InterviewAgentService interviewAgent;

    /**
     * Start a new mock interview session.
     * Returns the session ID and the FIRST question immediately.
     *
     * curl -X POST http://localhost:8080/api/v1/interview/start \
     *   -H "Content-Type: application/json" \
     *   -d '{"userId":"...","targetCompany":"TCS Digital","interviewType":"TECHNICAL","totalQuestions":5}'
     */
    @PostMapping("/start")
    public ResponseEntity<SessionStartedDTO> start(
            @RequestBody StartSessionRequestDTO request) {
        log.info("Start interview: user={} company={} type={}",
                request.userId(), request.targetCompany(), request.interviewType());
        return ResponseEntity.ok(interviewAgent.startSession(request));
    }

    /**
     * Submit an answer. Returns feedback on this answer + the next question.
     * If nextQuestion is null in the response → interview is complete.
     */
    @PostMapping("/answer")
    public ResponseEntity<TurnFeedbackDTO> answer(
            @RequestBody SubmitAnswerRequestDTO request) {
        log.info("Answer submitted: session={} turn={}",
                request.sessionId(), request.turnId());
        return ResponseEntity.ok(interviewAgent.submitAnswer(request));
    }

    /**
     * Get the full session report after completion.
     * Used to populate the post-interview review page.
     */
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<SessionResultDTO> result(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(interviewAgent.getResult(sessionId));
    }

    /**
     * Get list of past completed interview sessions for a user.
     * Used on the dashboard "Interview History" section.
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<SessionCardDTO>> history(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(interviewAgent.getPastSessions(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Interview Agent is running");
    }
}