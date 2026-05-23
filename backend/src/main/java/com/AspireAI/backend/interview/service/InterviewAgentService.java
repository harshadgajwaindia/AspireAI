package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewAgentService {

    private final InterviewSessionManager sessionManager;
    private final InterviewTurnManager turnManager;

    public SessionStartedDTO startSession(StartSessionRequestDTO req) {
        return sessionManager.startSession(req);
    }

    public TurnFeedbackDTO submitAnswer(SubmitAnswerRequestDTO req) {
        return turnManager.submitAnswer(req);
    }

    public SessionResultDTO getResult(UUID sessionId) {
        return sessionManager.getResult(sessionId);
    }

    public List<SessionCardDTO> getPastSessions(UUID userId) {
        return sessionManager.getPastSessions(userId);
    }
}