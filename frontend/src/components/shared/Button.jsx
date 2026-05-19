export function Button({
    children, onClick, disabled, variant = "primary",
    size = "md", className = "", type = "button", loading = false,
  }) {
    const base = "inline-flex items-center justify-center font-display font-bold rounded-xl transition-all duration-300 disabled:cursor-not-allowed select-none active:scale-[.97]";
    const sizes = {
      sm: "px-3.5 py-2 text-xs gap-1.5",
      md: "px-5 py-3 text-sm gap-2",
      lg: "px-7 py-4 text-base gap-2.5",
    };
    const variants = {
      primary:   "bg-white text-[#0a0a0c] hover:bg-slate-100 disabled:bg-slate-800 disabled:text-slate-500 disabled:shadow-none font-bold",
      secondary: "bg-white/5 border border-white/10 text-slate-200 hover:border-white/20 hover:bg-white/10",
      danger:    "bg-rose-600 text-white hover:bg-rose-500 active:scale-[.97]",
      ghost:     "text-slate-400 hover:text-white hover:bg-white/5",
      brand:     "bg-gradient-to-r from-brand-600 to-indigo-600 text-white hover:from-brand-500 hover:to-indigo-500 shadow-lg shadow-brand-600/10 hover:shadow-glow hover:shadow-brand-500/20",
    };
    return (
      <button
        type={type}
        onClick={onClick}
        disabled={disabled || loading}
        className={`${base} ${sizes[size]} ${variants[variant]} ${className}`}
      >
        {loading && (
          <svg className="animate-spin w-4 h-4 mr-1 text-current" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" className="opacity-25"/>
            <path d="M4 12a8 8 0 018-8" stroke="currentColor" strokeWidth="3" strokeLinecap="round" className="opacity-75"/>
          </svg>
        )}
        {children}
      </button>
    );
  }