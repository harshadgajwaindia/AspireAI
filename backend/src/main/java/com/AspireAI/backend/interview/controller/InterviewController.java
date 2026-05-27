package com.AspireAI.backend.interview.controller;

import com.AspireAI.backend.interview.dto.*;
import com.AspireAI.backend.interview.service.InterviewAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/interview")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")   // Vite dev server
public class InterviewController {

    private final InterviewAgentService interviewAgent;

  
    @PostMapping("/start")
    public ResponseEntity<SessionStartedDTO> start(
            @RequestBody StartSessionRequestDTO request) {
        log.info("Start interview: user={} company={} type={}",
                request.userId(), request.targetCompany(), request.interviewType());
        return ResponseEntity.ok(interviewAgent.startSession(request));
    }

   
    @PostMapping("/answer")
    public ResponseEntity<TurnFeedbackDTO> answer(
            @RequestBody SubmitAnswerRequestDTO request) {
        log.info("Answer submitted: session={} turn={}",
                request.sessionId(), request.turnId());
        return ResponseEntity.ok(interviewAgent.submitAnswer(request));
    }

  
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<SessionResultDTO> result(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(interviewAgent.getResult(sessionId));
    }

  
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