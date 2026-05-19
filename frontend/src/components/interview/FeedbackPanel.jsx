// src/components/interview/FeedbackPanel.jsx
import { useState } from "react";
import { Button, Badge } from "../shared/index.jsx";

const GRADE_META = {
  Excellent:   { cls: "score-excellent", bg: "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 shadow-glow-emerald", emoji: "🎯" },
  Good:        { cls: "score-good",      bg: "bg-sky-500/10 text-sky-400 border border-sky-500/20",     emoji: "👍" },
  "Needs Work":{ cls: "score-needs",     bg: "bg-amber-500/10 text-amber-400 border border-amber-500/20",   emoji: "📝" },
  Insufficient:{ cls: "score-insuf",     bg: "bg-rose-500/10 text-rose-400 border border-rose-500/20 shadow-glow-rose",    emoji: "⚠️" },
};

function ScoreBar({ score }) {
  const pct = (score / 10) * 100;
  const color = score >= 8 ? "bg-emerald-500" : score >= 6 ? "bg-sky-500" : score >= 4 ? "bg-amber-500" : "bg-rose-500";
  return (
    <div className="flex items-center gap-4">
      <div className="flex-1 h-2 bg-white/5 rounded-full overflow-hidden">
        <div className={`h-full rounded-full transition-all duration-1000 ${color}`}
             style={{ width: `${pct}%` }} />
      </div>
      <span className="font-display font-bold text-white text-lg w-12 text-right leading-none">
        {score}<span className="text-xs text-slate-400 font-body">/10</span>
      </span>
    </div>
  );
}

function FeedbackSection({ icon, title, content, variant = "default" }) {
  const [open, setOpen] = useState(true);
  const variants = {
    default: "bg-white/[0.01] border-white/5",
    good:    "bg-emerald-500/[0.02] border-emerald-500/10 text-emerald-300",
    missed:  "bg-amber-500/[0.02] border-amber-500/10 text-amber-300",
    ideal:   "bg-brand-500/[0.02] border-brand-500/10 text-brand-300",
    hint:    "bg-violet-500/[0.02] border-violet-500/10 text-violet-300",
  };
  return (
    <div className={`rounded-xl border p-4.5 transition-all duration-300 ${variants[variant]}`}>
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center justify-between w-full text-left"
      >
        <div className="flex items-center gap-2.5">
          <span className="text-base">{icon}</span>
          <span className="text-sm font-bold text-slate-200 font-display">{title}</span>
        </div>
        <svg
          className={`w-4 h-4 text-slate-400 transition-transform duration-300 ${open ? "rotate-180" : ""}`}
          fill="none" viewBox="0 0 24 24" stroke="currentColor"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {open && (
        <p className="mt-3.5 text-xs text-slate-300 font-body leading-relaxed pl-1">{content}</p>
      )}
    </div>
  );
}

export function FeedbackPanel({ feedback, onNext, isComplete }) {
  if (!feedback) return null;

  const meta = GRADE_META[feedback.grade] || GRADE_META["Needs Work"];

  return (
    <div className="card p-8 space-y-6 animate-slide-up">
      {/* Score header */}
      <div className="flex items-center gap-5 pb-5 border-b border-white/5">
        <div className={`w-14 h-14 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center text-3xl shadow-lg shadow-black/10`}>
          {meta.emoji}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2.5 mb-1.5">
            <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full font-body ${meta.cls}`}>
              {feedback.grade}
            </span>
            <span className="text-xs text-slate-400 font-body">
              Question {feedback.questionNumber}
            </span>
          </div>
          <ScoreBar score={feedback.score} />
        </div>
      </div>

      {/* Feedback sections */}
      <div className="space-y-3">
        {feedback.whatWasGood && (
          <FeedbackSection
            icon="✅" title="What was good"
            content={feedback.whatWasGood} variant="good"
          />
        )}
        {feedback.whatWasMissed && (
          <FeedbackSection
            icon="📌" title="What was missed"
            content={feedback.whatWasMissed} variant="missed"
          />
        )}
        {feedback.idealAnswer && (
          <FeedbackSection
            icon="💡" title="Model answer"
            content={feedback.idealAnswer} variant="ideal"
          />
        )}
        {feedback.followUpHint && (
          <FeedbackSection
            icon="🔮" title="A real interviewer would follow up with…"
            content={feedback.followUpHint} variant="hint"
          />
        )}
      </div>

      {/* CTA */}
      {isComplete ? (
        <div className="bg-[#12131a] border border-white/5 rounded-xl p-5 flex items-center justify-between shadow-glow shadow-brand-500/5">
          <div className="min-w-0 pr-4">
            <p className="font-display font-bold text-white text-base">Interview Complete!</p>
            <p className="text-slate-400 text-xs font-body mt-1 leading-normal">
              View your full performance report
            </p>
          </div>
          <Button onClick={onNext} variant="brand" size="sm" className="flex-shrink-0">
            See Results →
          </Button>
        </div>
      ) : (
        <Button onClick={onNext} variant="brand" className="w-full py-3.5 shadow-md shadow-brand-500/10" size="md">
          Next Question →
        </Button>
      )}
    </div>
  );
}