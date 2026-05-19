export function Loader({ label = "Loading…", size = "md" }) {
    const sizes = { sm: "w-5 h-5", md: "w-8 h-8", lg: "w-12 h-12" };
    return (
      <div className="flex flex-col items-center justify-center gap-3 py-12">
        <div className={`relative ${sizes[size]}`}>
          <div className={`${sizes[size]} rounded-full border-4 border-white/5`} />
          <div className={`absolute inset-0 ${sizes[size]} rounded-full border-4 border-transparent
                           border-t-brand-500 animate-spin`} />
        </div>
        {label && <p className="text-sm text-slate-400 font-semibold font-body">{label}</p>}
      </div>
    );
  }