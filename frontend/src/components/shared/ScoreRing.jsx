export function ScoreRing({ score, size = 120, strokeWidth = 10 }) {
    const radius = (size - strokeWidth) / 2;
    const circ = 2 * Math.PI * radius;
    const dash = ((score ?? 0) / 100) * circ;
    
    const gradientId = `scoreGradient-${score}`;
    let stopColorStart = "#f43f5e";
    let stopColorEnd = "#e11d48";
    
    if (score >= 75) {
      stopColorStart = "#34d399";
      stopColorEnd = "#059669";
    } else if (score >= 50) {
      stopColorStart = "#fbbf24";
      stopColorEnd = "#d97706";
    }
   
    return (
      <div className="relative inline-flex items-center justify-center select-none" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="-rotate-90 filter drop-shadow-[0_0_6px_rgba(255,255,255,0.02)]">
          <defs>
            <linearGradient id={gradientId} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor={stopColorStart} />
              <stop offset="100%" stopColor={stopColorEnd} />
            </linearGradient>
          </defs>
          <circle cx={size/2} cy={size/2} r={radius}
                  fill="none" stroke="rgba(255, 255, 255, 0.05)" strokeWidth={strokeWidth} />
          <circle cx={size/2} cy={size/2} r={radius}
                  fill="none" stroke={`url(#${gradientId})`} strokeWidth={strokeWidth}
                  strokeLinecap="round"
                  strokeDasharray={`${dash} ${circ}`}
                  style={{ transition: "stroke-dasharray 1.2s cubic-bezier(.4,0,.2,1)" }} />
        </svg>
        <div className="absolute flex flex-col items-center">
          <span className="font-display font-bold text-white leading-none"
                style={{ fontSize: size * 0.26 }}>
            {score ?? "—"}
          </span>
          <span className="text-slate-400 font-body font-medium mt-1.5" style={{ fontSize: size * 0.08 }}>/ 100</span>
        </div>
      </div>
    );
  }