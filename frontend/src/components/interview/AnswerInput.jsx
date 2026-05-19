import { useRef, useState } from "react";
import { Button } from "../shared/index.jsx";
 
export function AnswerInput({ onSubmit, isSubmitting, questionStartTime }) {
  const [answer, setAnswer] = useState("");
  const [charCount, setCharCount] = useState(0);
  const textareaRef = useRef(null);
  const MIN_CHARS = 20;
 
  const handleChange = (e) => {
    setAnswer(e.target.value);
    setCharCount(e.target.value.length);
    // Auto-resize textarea
    textareaRef.current.style.height = "auto";
    textareaRef.current.style.height = textareaRef.current.scrollHeight + "px";
  };
 
  const handleSubmit = () => {
    if (answer.trim().length < MIN_CHARS) return;
    const secs = Math.round((Date.now() - (questionStartTime || Date.now())) / 1000);
    onSubmit(answer.trim(), secs);
    setAnswer("");
    setCharCount(0);
  };
 
  const handleKeyDown = (e) => {
    // Ctrl/Cmd + Enter to submit
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
      handleSubmit();
    }
  };
 
  const canSubmit = answer.trim().length >= MIN_CHARS && !isSubmitting;
 
  return (
    <div className="card p-6">
      <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3.5 font-body">
        Your Answer
      </label>
 
      <div className={`relative rounded-xl border transition-all duration-300
        ${answer.length > 0 ? "border-brand-500 bg-[#0c0d12]" : "border-white/10 bg-white/[0.01]"}`}>
        <textarea
          ref={textareaRef}
          value={answer}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          disabled={isSubmitting}
          placeholder="Type your answer here…&#10;Be specific and give examples where possible."
          rows={5}
          className="w-full resize-none rounded-t-xl px-4 py-3.5 text-sm font-body text-slate-100
                     bg-transparent placeholder-slate-500 focus:outline-none
                     disabled:text-slate-500"
          style={{ minHeight: "140px", maxHeight: "360px" }}
        />
        <div className="flex items-center justify-between px-4 py-2.5 border-t border-white/5
                        rounded-b-xl bg-[#0c0d12]/50">
          <span className={`text-xs font-semibold font-body ${
            charCount < MIN_CHARS ? "text-slate-400" : "text-emerald-400"
          }`}>
            {charCount < MIN_CHARS
              ? `${MIN_CHARS - charCount} more chars needed`
              : `${charCount} characters`
            }
          </span>
          <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider font-body">⌘ + Enter to submit</span>
        </div>
      </div>
 
      <div className="flex flex-col sm:flex-row items-center gap-4 justify-between mt-4">
        <p className="text-xs text-slate-400 font-body leading-relaxed max-w-sm">
          Think out loud — real interviewers want to see your reasoning process.
        </p>
        <Button
          onClick={handleSubmit}
          disabled={!canSubmit}
          loading={isSubmitting}
          variant="brand"
          className="w-full sm:w-auto"
        >
          Submit Answer
        </Button>
      </div>
    </div>
  );
}
 