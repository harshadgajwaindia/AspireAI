import { useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { Badge, ScoreRing } from '../shared/index.jsx'

function RoadmapItemRow({ item }) {
  const statusColor = item.completionStatus === 'COMPLETED' ? 'success'
    : item.completionStatus === 'SKIPPED' ? 'default' : 'warning'

  return (
    <div className="flex items-start gap-4 py-4.5 border-b border-white/5 last:border-0 transition-colors duration-200">
      <span className="text-xs font-bold text-slate-400 font-body w-8 pt-0.5">D{item.dayNumber}</span>
      <div className="flex-1 min-w-0">
        <div className="flex items-center flex-wrap gap-2 mb-1.5">
          <span className="text-sm font-display font-bold text-slate-200">{item.taskTitle}</span>
          <Badge variant={statusColor} className="px-2 py-0.5">{item.completionStatus}</Badge>
        </div>
        <p className="text-xs text-slate-400 font-body leading-relaxed mb-2.5">{item.taskDescription}</p>
        <div className="flex items-center gap-3">
          <Badge variant="default" className="px-2 py-0.5">{item.skillName}</Badge>
          <span className="text-[11px] text-slate-500 font-body">{item.estimatedMinutes} min</span>
        </div>
      </div>
    </div>
  )
}

function WeekBlock({ week }) {
  const [open, setOpen] = useState(week.weekNumber === 1)

  return (
    <div className="card overflow-hidden transition-all duration-300">
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        className="w-full flex items-center gap-4 p-5 text-left hover:bg-white/[0.02] transition-colors"
      >
        <div className="w-10 h-10 rounded-xl bg-brand-500/10 border border-brand-500/25 flex items-center justify-center
                        font-display font-bold text-brand-400 text-sm flex-shrink-0">
          W{week.weekNumber}
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="font-display font-bold text-white text-base">{week.theme}</h3>
          <p className="text-xs text-slate-400 font-body truncate mt-0.5">{week.focusSkills}</p>
        </div>
        <span className="text-xs text-slate-400 font-semibold font-body">{week.totalMinutes} min</span>
        <ChevronDown className={`w-4 h-4 text-slate-400 transition-transform duration-300 ${open ? 'rotate-180' : ''}`} />
      </button>
      {open && (
        <div className="border-t border-white/5 px-6 pb-4 bg-[#151720]/30 divide-y divide-white/5">
          {(week.items || []).map((item) => (
            <RoadmapItemRow key={item.id} item={item} />
          ))}
        </div>
      )}
    </div>
  )
}

export function RoadmapPlanView({ plan }) {
  const progress = plan.progress

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="card p-6">
        <div className="flex flex-col sm:flex-row items-center gap-6">
          <ScoreRing score={progress?.projectedReadiness ?? plan.baselineReadiness} size={110} />
          <div className="flex-1 text-center sm:text-left min-w-0">
            <Badge variant="brand" className="mb-2 px-2.5 py-0.5">{plan.targetCompany}</Badge>
            <h2 className="font-display font-extrabold text-2xl text-white tracking-tight mt-1">
              {plan.totalDays}-Day Roadmap
            </h2>
            <p className="text-xs text-slate-400 font-body mt-1">
              {plan.startDate} → {plan.targetDate}
            </p>
            {progress && (
              <div className="mt-4 flex flex-wrap gap-4 text-xs font-semibold font-body text-slate-300">
                <span className="bg-white/5 px-2.5 py-1 rounded-md">{progress.completed}/{progress.totalItems} completed</span>
                <span className="bg-white/5 px-2.5 py-1 rounded-md">{Math.round(progress.completionPercent)}% progress</span>
                <span className="bg-amber-500/10 text-amber-300 px-2.5 py-1 rounded-md">🔥 {progress.streakDays} day streak</span>
              </div>
            )}
          </div>
        </div>
        {progress && (
          <div className="mt-6 h-2 bg-white/5 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-brand-500 to-indigo-600 rounded-full transition-all duration-500"
              style={{ width: `${progress.completionPercent}%` }}
            />
          </div>
        )}
      </div>

      <div className="space-y-4">
        {(plan.weeks || []).map((week) => (
          <WeekBlock key={week.weekNumber} week={week} />
        ))}
      </div>
    </div>
  )
}
