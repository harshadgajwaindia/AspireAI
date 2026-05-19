import { useState } from "react";
import { Button, Badge } from "../shared/index.jsx";

const COMPANIES = [
  "General Placement Prep",
  "Full-Stack Developer Path",
  "AI & ML Engineer Guidance",
  "Data Structures & Algorithms Prep",
  "Core Computer Science Freshers",
  "TCS Digital",
  "Infosys SP",
  "Wipro Elite",
  "Cognizant GenC",
  "Accenture ASE"
];
const TYPES = [
  { value: "TECHNICAL", label: "Technical", desc: "DSA, Core CS, Backend", icon: "⚙️" },
  { value: "HR",        label: "HR",         desc: "Behavioral, Situational", icon: "🤝" },
  { value: "MIXED",     label: "Mixed",      desc: "Technical + HR rounds", icon: "⚡" },
];
const QUESTION_COUNTS = [5, 10];

export function SessionSetup({ onStart, isLoading }) {
  const [company, setCompany]   = useState(COMPANIES[0]);
  const [type, setType]         = useState("TECHNICAL");
  const [count, setCount]       = useState(5);

  return (
    <div className="max-w-2xl mx-auto px-6 py-6 animate-slide-up">
      {/* Hero */}
      <div className="mb-10">
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full
                        bg-brand-500/10 border border-brand-500/25 text-brand-300
                        text-xs font-semibold font-body mb-4 shadow-sm shadow-brand-500/5 animate-pulse-slow">
          <span className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-pulse" />
          AI-Powered Mock Interviewer
        </div>
        <h1 className="font-display text-4xl font-extrabold text-white tracking-tight mb-3">
          Ready to practice?
        </h1>
        <p className="text-slate-400 font-body text-sm leading-relaxed">
          Get real-time feedback on every answer. Questions are tailored to your skill gaps, public GitHub projects, and actual interview requirements.
        </p>
      </div>

      <div className="card p-8 space-y-6">
        {/* Company */}
        <div>
          <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5 font-body">
            Target Pathway / Company
          </label>
          <div className="relative">
            <select
              value={company}
              onChange={(e) => setCompany(e.target.value)}
              className="w-full appearance-none rounded-xl border border-white/10 bg-white/5
                         px-4 py-3.5 text-sm font-semibold text-slate-100 font-body
                         focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent cursor-pointer"
            >
              {COMPANIES.map((c) => <option key={c} value={c} className="bg-[#13141c] text-white">{c}</option>)}
            </select>
            <svg className="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none"
                 fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </div>
        </div>

        {/* Interview type */}
        <div>
          <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5 font-body">
            Interview Type
          </label>
          <div className="grid grid-cols-3 gap-3">
            {TYPES.map((t) => (
              <button
                key={t.value}
                onClick={() => setType(t.value)}
                className={`p-4 rounded-xl border text-left transition-all duration-300
                  ${type === t.value
                    ? "border-brand-500 bg-brand-500/10 shadow-glow shadow-brand-500/5"
                    : "border-white/10 hover:border-white/20 bg-white/[0.02]"
                  }`}
              >
                <div className="text-2xl mb-2">{t.icon}</div>
                <div className={`text-xs font-bold uppercase tracking-wide font-display
                  ${type === t.value ? "text-brand-300" : "text-slate-200"}`}>
                  {t.label}
                </div>
                <div className="text-[10px] text-slate-400 font-body mt-1 leading-normal">{t.desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Question count */}
        <div>
          <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5 font-body">
            Number of Questions
          </label>
          <div className="flex gap-3">
            {QUESTION_COUNTS.map((n) => (
              <button
                key={n}
                onClick={() => setCount(n)}
                className={`flex-1 py-3.5 rounded-xl border font-bold font-display text-sm
                            transition-all duration-305
                  ${count === n
                    ? "border-brand-500 bg-brand-600 text-white shadow-lg shadow-brand-600/20"
                    : "border-white/10 text-slate-300 hover:border-white/20 hover:bg-white/5"
                  }`}
              >
                {n} Questions
                <span className={`block text-[10px] font-semibold font-body mt-0.5
                  ${count === n ? "text-brand-200" : "text-slate-400"}`}>
                  ~{n * 5} min
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* What to expect */}
        <div className="bg-white/[0.02] border border-white/5 rounded-xl p-5">
          <p className="text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3 font-body">
            What to expect
          </p>
          <ul className="space-y-2 text-sm text-slate-300 font-body">
            {[
              "Questions grounded in real " + company + " patterns",
              "Instant feedback after each answer",
              "Model answer shown after you respond",
              "Full session report at the end",
            ].map((item, i) => (
              <li key={i} className="flex items-start gap-2.5">
                <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-brand-400 flex-shrink-0" />
                {item}
              </li>
            ))}
          </ul>
        </div>

        <Button
          onClick={() => onStart({ company, type, count })}
          loading={isLoading}
          disabled={isLoading}
          size="lg"
          variant="brand"
          className="w-full py-4 shadow-lg shadow-brand-500/10"
        >
          Begin Interview →
        </Button>
      </div>
    </div>
  );
}