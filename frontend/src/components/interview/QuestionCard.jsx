import { useEffect, useState } from "react";
import { Badge } from "../shared/index.jsx";
import { useInterviewStore } from "../../store/interviewStore.js";
 
const QUESTION_TYPE_META = {
  CONCEPTUAL:  { label: "Conceptual",  color: "technical" },
  CODING:      { label: "Coding",      color: "brand" },
  BEHAVIORAL:  { label: "Behavioral",  color: "hr" },
  SITUATIONAL: { label: "Situational", color: "warning" },
};
 
const DIFF_DOTS = [1, 2, 3, 4, 5];
 
export function QuestionCard({ question }) {
  const { completedTurns, totalQuestions } = useInterviewStore();
  const [revealed, setRevealed] = useState(false);
 
  // Trigger reveal animation on each new question
  useEffect(() => {
    setRevealed(false);
    const t = setTimeout(() => setRevealed(true), 100);
    return () => clearTimeout(t);
  }, [question?.turnId]);
 
  if (!question) return null;
 
  const meta = QUESTION_TYPE_META[question.questionType] || QUESTION_TYPE_META.CONCEPTUAL;
  const progress = (question.questionNumber / totalQuestions) * 100;
 
  return (
    <div className={`card p-8 transition-all duration-500 ${revealed ? "opacity-100 translate-y-0" : "opacity-0 translate-y-4"}`}>
      {/* Progress bar */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-3.5">
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-widest font-body">
            Question {question.questionNumber} of {totalQuestions}
          </span>
          <div className="flex gap-1.5">
            {Array.from({ length: totalQuestions }, (_, i) => (
              <div
                key={i}
                className={`h-1.5 rounded-full transition-all duration-300 ${
                  i < completedTurns.length
                    ? completedTurns[i]?.score >= 7 ? "bg-emerald-500" : "bg-amber-400"
                    : i === completedTurns.length
                    ? "bg-brand-500 w-6"
                    : "bg-white/10 w-3"
                }`}
                style={{ width: i === completedTurns.length ? "24px" : "12px" }}
              />
            ))}
          </div>
        </div>
        <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-brand-500 to-indigo-600 rounded-full transition-all duration-700"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>
 
      {/* Metadata row */}
      <div className="flex flex-wrap items-center gap-2 mb-5">
        <Badge variant={meta.color} className="px-2 py-0.5">{meta.label}</Badge>
        {question.skillArea && (
          <Badge variant="default" className="px-2 py-0.5">{question.skillArea}</Badge>
        )}
        {/* Difficulty dots */}
        <div className="flex items-center gap-1 ml-auto">
          <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wide font-body mr-1">Difficulty</span>
          {DIFF_DOTS.map((d) => (
            <div
              key={d}
              className={`w-2 h-2 rounded-full transition-colors ${
                d <= (question.difficultyLevel || 2)
                  ? "bg-brand-500"
                  : "bg-white/10"
              }`}
            />
          ))}
        </div>
      </div>
 
      {/* Question text */}
      <div className="bg-[#151720]/80 rounded-xl p-6 border border-white/5 mb-5 shadow-inner">
        <p className="font-body text-slate-100 leading-relaxed text-base">
          {question.questionText}
        </p>
      </div>
 
      {/* RAG insight */}
      {question.ragInsight && (
        <div className="flex items-start gap-2.5 text-xs text-brand-300 font-semibold font-body bg-brand-500/10 border border-brand-500/10 p-3 rounded-lg leading-relaxed shadow-sm">
          <svg className="w-4 h-4 flex-shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <div>{question.ragInsight}</div>
        </div>
      )}
    </div>
  );
}