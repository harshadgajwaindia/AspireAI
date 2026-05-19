export function Badge({ children, variant = "default", className = "" }) {
    const variants = {
      default:   "bg-slate-800/80 text-slate-300 border border-slate-700/50",
      brand:     "bg-brand-500/10 text-brand-300 border border-brand-500/20 shadow-sm",
      success:   "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 shadow-sm",
      warning:   "bg-amber-500/10 text-amber-400 border border-amber-500/20 shadow-sm",
      danger:    "bg-rose-500/10 text-rose-400 border border-rose-500/20 shadow-sm",
      info:      "bg-sky-500/10 text-sky-400 border border-sky-500/20 shadow-sm",
      technical: "bg-violet-500/10 text-violet-400 border border-violet-500/20 shadow-sm",
      hr:        "bg-pink-500/10 text-pink-400 border border-pink-500/20 shadow-sm",
      mixed:     "bg-amber-500/10 text-amber-400 border border-amber-500/20 shadow-sm",
    };
    return (
      <span className={`inline-flex items-center gap-1 text-[11px] font-semibold px-2.5 py-0.5 rounded-full
                        font-body ${variants[variant]} ${className}`}>
        {children}
      </span>
    );
  }