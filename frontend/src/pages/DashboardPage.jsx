import { useEffect, useState } from 'react'
import { BarChart3, Map, Mic, FileSearch } from 'lucide-react'
import { Button } from '../components/shared/index.jsx'
import { interviewApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'
import { StatCard } from '../components/dashboard/StatCard.jsx'
import { DashboardAnalysisCard } from '../components/dashboard/DashboardAnalysisCard.jsx'
import { DashboardInterviewsCard } from '../components/dashboard/DashboardInterviewsCard.jsx'

export default function DashboardPage() {
  const { userId, gapReport, roadmapPlan, clearSession } = useAppStore()
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    interviewApi.getHistory(userId)
      .then(setHistory).catch(() => setHistory([])).finally(() => setLoading(false))
  }, [userId])

  const progress = roadmapPlan?.progress

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-6xl mx-auto px-6 py-12 space-y-10">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 border-b border-white/5 pb-6">
          <div>
            <h1 className="font-display text-4xl font-extrabold text-white tracking-tight">Dashboard</h1>
            <p className="text-slate-400 font-body text-sm mt-1.5">Your placement prep at a glance</p>
          </div>
          <Button variant="secondary" size="sm" onClick={() => confirm('Clear saved analysis and roadmap?') && clearSession()} className="border-rose-500/20 hover:border-rose-500/40 text-rose-400 hover:bg-rose-500/10">Reset data</Button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard icon={FileSearch} label="Readiness" value={gapReport ? `${gapReport.overallReadiness}%` : '—'} sub={gapReport?.targetCompany || 'Run analyzer'} to="/" accent="emerald" />
          <StatCard icon={Map} label="Roadmap" value={progress ? `${Math.round(progress.completionPercent)}%` : roadmapPlan ? 'Active' : '—'} sub={roadmapPlan ? `${progress?.completed ?? 0} tasks done` : 'Not started'} to="/roadmap" accent="brand" />
          <StatCard icon={Mic} label="Interviews" value={history.length} sub="Sessions completed" to="/interview" accent="violet" />
          <StatCard icon={BarChart3} label="Streak" value={progress?.streakDays ?? 0} sub="Study days" to="/roadmap" accent="emerald" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <DashboardAnalysisCard gapReport={gapReport} />
          <DashboardInterviewsCard history={history} loading={loading} />
        </div>

        {gapReport?.topGaps?.length > 0 && (
          <div className="card p-6">
            <h2 className="font-display font-bold text-xl text-white mb-4 border-b border-white/5 pb-3">Priority Gaps</h2>
            <div className="flex flex-wrap gap-3">
              {gapReport.topGaps.slice(0, 6).map((g, i) => (
                <span key={i} className="px-4 py-2 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs font-semibold font-body shadow-sm">
                  {g.skillName} (−{g.gapSize})
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
