import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ScoreRing, Badge, Button, Loader } from "../components/shared/index.jsx";
import { interviewApi } from "../services/api.js";

const GRADE_META = {
  Ready:          { color: "success" },
  "Almost Ready": { color: "warning" },
  "Needs Work":   { color: "danger" },
};

function TurnCard({ turn }) {
  const [open, setOpen] = useState(false);
  const score10 = turn.score ?? 0;
  const pct = (score10 / 10) * 100;
  const color = score10 >= 8 ? "bg-emerald-500" : score10 >= 6 ? "bg-sky-500"
              : score10 >= 4 ? "bg-amber-400"   : "bg-rose-500";

  return (
    <div className="card overflow-hidden transition-all duration-300">
      <button
        onClick={() => setOpen((o) => !o)}
        className="w-full text-left p-5 flex items-center gap-4 hover:bg-white/[0.01]"
      >
        <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center
                        font-display font-bold text-sm text-slate-300 flex-shrink-0 border border-white/5">
          {turn.questionNumber}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center flex-wrap gap-2 mb-1.5">
            <Badge variant="default" className="px-2 py-0.5">{turn.skillArea}</Badge>
            <Badge variant={
              turn.grade === "Excellent" ? "success" :
              turn.grade === "Good" ? "info" :
              turn.grade === "Needs Work" ? "warning" : "danger"
            } className="px-2 py-0.5">
              {turn.grade}
            </Badge>
          </div>
          <p className="text-sm font-semibold text-slate-200 truncate">{turn.questionText}</p>
          <div className="mt-2.5 flex items-center gap-3">
            <div className="flex-1 h-1.5 bg-white/5 rounded-full overflow-hidden">
              <div className={`h-full rounded-full transition-all duration-500 ${color}`} style={{ width: `${pct}%` }} />
            </div>
            <span className="text-xs font-bold text-slate-300 font-display flex-shrink-0">
              {score10}/10
            </span>
          </div>
        </div>
        <svg className={`w-4 h-4 text-slate-400 transition-transform duration-300 flex-shrink-0 ${open ? "rotate-180" : ""}`}
             fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {open && (
        <div className="border-t border-white/5 p-5 space-y-4 bg-[#14161f]/45">
          {turn.studentAnswer && (
            <div>
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 font-body">Your answer</p>
              <p className="text-xs text-slate-300 font-body leading-relaxed">{turn.studentAnswer}</p>
            </div>
          )}
          {turn.whatWasMissed && (
            <div className="bg-amber-500/5 border border-amber-500/10 rounded-xl p-4">
              <p className="text-[10px] font-bold text-amber-400 uppercase tracking-widest mb-1.5 font-body">What was missed</p>
              <p className="text-xs text-slate-300 font-body leading-relaxed">{turn.whatWasMissed}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function InterviewResultPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    interviewApi.getResult(sessionId)
      .then(setResult)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [sessionId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-ink-50 flex items-center justify-center">
        <Loader label="Loading your results…" size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-ink-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-rose-400 font-body mb-4 font-semibold">{error}</p>
          <Button onClick={() => navigate("/interview")} variant="secondary">
            Back to Interviews
          </Button>
        </div>
      </div>
    );
  }

  const fb = result?.overallFeedback;
  const gradeMeta = fb ? (GRADE_META[fb.overallGrade] || GRADE_META["Needs Work"]) : null;

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-4xl mx-auto px-6 py-12 space-y-6">
        <div className="card p-8">
          <div className="flex flex-col sm:flex-row items-center gap-6">
            <ScoreRing score={result.overallScore} size={120} />
            <div className="flex-1 text-center sm:text-left min-w-0">
              <div className="flex items-center justify-center sm:justify-start gap-2 mb-2">
                <Badge variant="default" className="px-2 py-0.5">{result.targetCompany}</Badge>
                <Badge variant={result.interviewType === "TECHNICAL" ? "technical" : "hr"} className="px-2 py-0.5">
                  {result.interviewType}
                </Badge>
              </div>
              <h1 className="font-display font-extrabold text-3xl text-white tracking-tight mb-2">Interview Complete</h1>
              {fb && (
                <>
                  <Badge variant={gradeMeta?.color} className="px-2.5 py-0.5 mb-2">{fb.overallGrade}</Badge>
                  <p className="text-xs text-slate-400 font-body leading-relaxed mt-2">{fb.executiveSummary}</p>
                </>
              )}
            </div>
          </div>
        </div>

        {fb && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="card p-6">
              <p className="text-[11px] font-bold text-emerald-400 uppercase tracking-widest mb-3.5 font-body">Strengths</p>
              <ul className="space-y-3">
                {(fb.strengths || []).map((s, i) => (
                  <li key={i} className="flex items-start gap-2.5 text-xs text-slate-300 font-body leading-relaxed">
                    <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-emerald-400 flex-shrink-0" />
                    {s}
                  </li>
                ))}
              </ul>
            </div>
            <div className="card p-6">
              <p className="text-[11px] font-bold text-amber-400 uppercase tracking-widest mb-3.5 font-body">To Improve</p>
              <ul className="space-y-3">
                {(fb.areasToImprove || []).map((s, i) => (
                  <li key={i} className="flex items-start gap-2.5 text-xs text-slate-300 font-body leading-relaxed">
                    <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-amber-400 flex-shrink-0" />
                    {s}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {fb?.recommendedTopics?.length > 0 && (
          <div className="card p-6">
            <p className="text-[11px] font-bold text-brand-300 uppercase tracking-widest mb-4 font-body">
              Study Before Next Interview
            </p>
            <div className="flex flex-wrap gap-2.5">
              {fb.recommendedTopics.map((t, i) => (
                <span key={i} className="px-3.5 py-2 bg-brand-500/10 border border-brand-500/20 text-brand-300 rounded-xl text-xs font-semibold font-body shadow-sm shadow-brand-500/5">
                  {t}
                </span>
              ))}
            </div>
          </div>
        )}

        <div>
          <h2 className="font-display font-extrabold text-xl text-white tracking-tight mb-4 pl-1">Question Breakdown</h2>
          <div className="space-y-3">
            {(result.turnSummaries || []).map((turn) => (
              <TurnCard key={turn.questionNumber} turn={turn} />
            ))}
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-4 pt-4">
          <Button onClick={() => navigate("/interview")} variant="secondary" className="flex-1 border-white/10 hover:border-white/20 text-slate-300">
            Try Another Interview
          </Button>
          <Button onClick={() => navigate("/roadmap")} variant="brand" className="flex-1">
            Update Roadmap →
          </Button>
        </div>
      </div>
    </div>
  );
}
