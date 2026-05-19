// src/pages/InterviewPage.jsx
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { SessionSetup }  from "../components/interview/SessionSetup.jsx";
import { QuestionCard } from "../components/interview/QuestionCard.jsx";
import { AnswerInput } from "../components/interview/AnswerInput.jsx";
import { FeedbackPanel } from "../components/interview/FeedbackPanel.jsx";
import { Loader }        from "../components/shared/index.jsx";
import { useInterviewStore } from "../store/interviewStore.js";
import { interviewApi } from "../services/api.js";
import { useAppStore } from "../store/useAppStore.js";

export default function InterviewPage() {
  const navigate = useNavigate();
  const { userId, gapReport } = useAppStore();
  const {
    phase, currentQuestion, lastFeedback, questionStartTime,
    totalQuestions, sessionId,
    isStarting, isEvaluating,
    setStarting, setEvaluating,
    sessionStarted, feedbackReceived, nextQuestion, reset,
  } = useInterviewStore();

  // ── Start session ──────────────────────────────────────────────────────────
  const handleStart = async ({ company, type, count }) => {
    setStarting(true);
    try {
      const data = await interviewApi.start({
        userId,
        targetCompany: company,
        interviewType: type,
        totalQuestions: count,
        skillGapJson: gapReport ? JSON.stringify(gapReport) : undefined,
      });
      sessionStarted(data);
    } catch (err) {
      toast.error(err.message);
      setStarting(false);
    }
  };

  // ── Submit answer ──────────────────────────────────────────────────────────
  const handleAnswer = async (answerText, secs) => {
    setEvaluating(true);
    try {
      const feedback = await interviewApi.answer({
        sessionId,
        turnId: currentQuestion.turnId,
        studentAnswer: answerText,
        answerTimeSeconds: secs,
      });
      feedbackReceived(feedback);
    } catch (err) {
      toast.error(err.message);
      setEvaluating(false);
    }
  };

  // ── Next question or go to results ─────────────────────────────────────────
  const handleNext = () => {
    if (!lastFeedback?.nextQuestion) {
      // Interview complete → results page
      navigate(`/interview/result/${sessionId}`);
    } else {
      nextQuestion();
    }
  };

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-2xl mx-auto px-6 py-12">

        {/* ── SETUP phase ── */}
        {phase === "SETUP" && (
          <SessionSetup onStart={handleStart} isLoading={isStarting} />
        )}

        {/* ── Starting loader ── */}
        {isStarting && phase !== "SETUP" && (
          <div className="card p-10 flex items-center justify-center">
            <Loader label="Preparing your interview…" size="lg" />
          </div>
        )}

        {/* ── QUESTION phase ── */}
        {phase === "QUESTION" && currentQuestion && (
          <div className="space-y-6 animate-fade-in">
            <QuestionCard question={currentQuestion} />
            {isEvaluating ? (
              <div className="card p-10 flex items-center justify-center">
                <Loader label="Evaluating your answer…" size="md" />
              </div>
            ) : (
              <AnswerInput
                onSubmit={handleAnswer}
                isSubmitting={isEvaluating}
                questionStartTime={questionStartTime}
              />
            )}
          </div>
        )}

        {/* ── FEEDBACK phase ── */}
        {phase === "FEEDBACK" && lastFeedback && (
          <div className="space-y-6 animate-fade-in">
            {/* Show question context above feedback */}
            <div className="card p-5 opacity-70 bg-white/[0.01]">
              <p className="text-xs text-brand-400 font-body mb-1.5 font-bold">QUESTION {lastFeedback.questionNumber}</p>
              <p className="text-sm text-slate-200 font-body leading-relaxed">{currentQuestion?.questionText}</p>
            </div>
            <FeedbackPanel
              feedback={lastFeedback}
              onNext={handleNext}
              isComplete={!lastFeedback.nextQuestion}
            />
          </div>
        )}

        {/* ── COMPLETE phase ── */}
        {phase === "COMPLETE" && lastFeedback && (
          <div className="space-y-6 animate-fade-in">
            <FeedbackPanel
              feedback={lastFeedback}
              onNext={handleNext}
              isComplete
            />
          </div>
        )}

        {/* Reset button (escape hatch) */}
        {phase !== "SETUP" && !isStarting && (
          <div className="mt-10 flex justify-center">
            <button
              onClick={reset}
              className="text-xs text-slate-400 hover:text-rose-400 font-semibold font-body
                         underline underline-offset-4 transition-colors duration-200"
            >
              Abandon and start over
            </button>
          </div>
        )}
      </div>
    </div>
  );
}