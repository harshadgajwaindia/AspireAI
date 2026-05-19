// src/store/interviewStore.js
import { create } from "zustand";

/**
 * Global interview state — shared between InterviewPage components.
 *
 * Why Zustand instead of local state?
 * The interview has 3 "phases" spread across components:
 *   SessionSetup → QuestionCard + AnswerInput → FeedbackPanel
 * State needs to flow between all of them without prop drilling.
 *
 * Zustand gives us a clean global store without the boilerplate of Redux.
 */
export const useInterviewStore = create((set, get) => ({
  // ── Session state ────────────────────────────────────────────────────────
  sessionId: null,
  totalQuestions: 5,
  targetCompany: "",
  interviewType: "TECHNICAL",

  // ── Current turn ──────────────────────────────────────────────────────────
  currentQuestion: null,    // QuestionDTO
  questionNumber: 0,
  phase: "SETUP",           // "SETUP" | "QUESTION" | "FEEDBACK" | "COMPLETE"

  // ── Latest feedback ───────────────────────────────────────────────────────
  lastFeedback: null,       // TurnFeedbackDTO

  // ── History (for progress bar) ────────────────────────────────────────────
  completedTurns: [],       // [{questionNumber, score, grade}]

  // ── Loading states ─────────────────────────────────────────────────────────
  isStarting: false,
  isEvaluating: false,

  // ── Timing ────────────────────────────────────────────────────────────────
  questionStartTime: null,

  // ── Actions ───────────────────────────────────────────────────────────────
  setStarting: (v) => set({ isStarting: v }),
  setEvaluating: (v) => set({ isEvaluating: v }),

  sessionStarted: (data) => set({
    sessionId: data.sessionId,
    targetCompany: data.targetCompany,
    interviewType: data.interviewType,
    totalQuestions: data.totalQuestions,
    currentQuestion: data.firstQuestion,
    questionNumber: 1,
    phase: "QUESTION",
    completedTurns: [],
    questionStartTime: Date.now(),
    isStarting: false,
  }),

  feedbackReceived: (feedback) => set((state) => ({
    lastFeedback: feedback,
    phase: feedback.nextQuestion ? "FEEDBACK" : "COMPLETE",
    isEvaluating: false,
    completedTurns: [
      ...state.completedTurns,
      {
        questionNumber: feedback.questionNumber,
        score: feedback.score,
        grade: feedback.grade,
      },
    ],
  })),

  nextQuestion: () => set((state) => ({
    currentQuestion: state.lastFeedback?.nextQuestion,
    questionNumber: state.questionNumber + 1,
    phase: "QUESTION",
    lastFeedback: null,
    questionStartTime: Date.now(),
  })),

  reset: () => set({
    sessionId: null,
    currentQuestion: null,
    questionNumber: 0,
    phase: "SETUP",
    lastFeedback: null,
    completedTurns: [],
    isStarting: false,
    isEvaluating: false,
    questionStartTime: null,
  }),
}));