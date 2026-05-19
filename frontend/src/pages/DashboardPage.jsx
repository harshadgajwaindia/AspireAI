import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { BarChart3, Map, Mic, FileSearch, ArrowRight } from 'lucide-react'
import { ScoreRing, Badge, Button, Loader } from '../components/shared/index.jsx'
import { interviewApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'

function StatCard({ icon: Icon, label, value, sub, to, accent = 'brand' }) {
  const accentMap = {
    brand: 'from-brand-500 to-brand-600 shadow-brand-500/10',
    emerald: 'from-emerald-500 to-emerald-600 shadow-emerald-500/10',
    violet: 'from-violet-500 to-violet-600 shadow-violet-500/10',
  }

  return (
    <Link to={to} className="card p-6 hover:shadow-glow hover:border-brand-500/20 group block">
      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${accentMap[accent]} flex items-center justify-center mb-5 shadow-lg`}>
        <Icon className="w-5.5 h-5.5 text-white" />
      </div>
      <p className="text-xs font-bold text-slate-400 uppercase tracking-widest font-body mb-2">{label}</p>
      <p className="font-display font-bold text-3xl text-white">{value}</p>
      {sub && <p className="text-xs text-slate-400 font-body mt-2">{sub}</p>}
      <span className="inline-flex items-center gap-1.5 text-xs text-brand-400 font-bold font-body mt-4 group-hover:gap-2.5 transition-all">
        Open <ArrowRight className="w-3.5 h-3.5" />
      </span>
    </Link>
  )
}

function HistoryCard({ session, onClick }) {
  const gradeVariant = session.overallGrade === 'Ready' ? 'success'
    : session.overallGrade === 'Almost Ready' ? 'warning' : 'danger'

  return (
    <button
      type="button"
      onClick={onClick}
      className="card p-5 w-full text-left hover:border-brand-500/30 transition-all duration-300 hover:shadow-glow hover:shadow-brand-500/5 group"
    >
      <div className="flex items-center justify-between gap-4">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2 mb-2">
            <Badge variant="brand" className="px-2 py-0.5">{session.targetCompany}</Badge>
            <Badge variant={session.interviewType === 'TECHNICAL' ? 'technical' : 'hr'} className="px-2 py-0.5">
              {session.interviewType}
            </Badge>
          </div>
          <p className="text-xs text-slate-400 font-body">
            {session.totalQuestions} questions · {new Date(session.startedAt).toLocaleDateString()}
          </p>
        </div>
        <div className="text-right flex-shrink-0">
          <p className="font-display font-bold text-2xl text-white mb-1 leading-none">{session.overallScore}</p>
          <Badge variant={gradeVariant} className="px-2 py-0.5">{session.overallGrade}</Badge>
        </div>
      </div>
    </button>
  )
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const { userId, gapReport, roadmapPlan, clearSession } = useAppStore()
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    interviewApi.getHistory(userId)
      .then(setHistory)
      .catch(() => setHistory([]))
      .finally(() => setLoading(false))
  }, [userId])

  const progress = roadmapPlan?.progress

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-6xl mx-auto px-6 py-12 space-y-10">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 border-b border-white/5 pb-6">
          <div>
            <h1 className="font-display text-4xl font-extrabold text-white tracking-tight">Dashboard</h1>
            <p className="text-slate-400 font-body text-sm mt-1.5">
              Your placement prep at a glance
            </p>
          </div>
          <Button variant="secondary" size="sm" onClick={() => {
            if (confirm('Clear saved analysis and roadmap?')) clearSession()
          }} className="border-rose-500/20 hover:border-rose-500/40 text-rose-400 hover:bg-rose-500/10">
            Reset data
          </Button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard
            icon={FileSearch}
            label="Readiness"
            value={gapReport ? `${gapReport.overallReadiness}%` : '—'}
            sub={gapReport?.targetCompany || 'Run analyzer'}
            to="/"
            accent="emerald"
          />
          <StatCard
            icon={Map}
            label="Roadmap"
            value={progress ? `${Math.round(progress.completionPercent)}%` : roadmapPlan ? 'Active' : '—'}
            sub={roadmapPlan ? `${progress?.completed ?? 0} tasks done` : 'Not started'}
            to="/roadmap"
            accent="brand"
          />
          <StatCard
            icon={Mic}
            label="Interviews"
            value={history.length}
            sub="Sessions completed"
            to="/interview"
            accent="violet"
          />
          <StatCard
            icon={BarChart3}
            label="Streak"
            value={progress?.streakDays ?? 0}
            sub="Study days"
            to="/roadmap"
            accent="emerald"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {gapReport && (
            <div className="card p-6 flex flex-col justify-between">
              <div>
                <h2 className="font-display font-bold text-xl text-white mb-5 border-b border-white/5 pb-3">Latest Analysis</h2>
                <div className="flex items-center gap-6">
                  <ScoreRing score={gapReport.overallReadiness} size={110} />
                  <div className="min-w-0">
                    <Badge variant="brand" className="mb-2">{gapReport.targetCompany}</Badge>
                    <p className="text-sm text-slate-300 font-body leading-relaxed line-clamp-3">
                      {gapReport.skillProfile?.summary || 'Gap analysis on file.'}
                    </p>
                  </div>
                </div>
              </div>
              <Button variant="secondary" size="md" className="mt-6 w-full" onClick={() => navigate('/')}>
                View report
              </Button>
            </div>
          )}

          {!gapReport && (
            <div className="card p-8 flex flex-col justify-center text-center">
              <div className="w-12 h-12 rounded-full bg-brand-500/10 flex items-center justify-center mx-auto mb-4 border border-brand-500/20">
                <FileSearch className="w-6 h-6 text-brand-400" />
              </div>
              <h2 className="font-display font-bold text-xl text-white mb-2">Get started</h2>
              <p className="text-sm text-slate-400 font-body mb-6 max-w-md mx-auto">
                Upload your resume to unlock your study roadmap and targeted mock interviews.
              </p>
              <Button onClick={() => navigate('/')} variant="brand" className="w-full">Analyze Resume →</Button>
            </div>
          )}

          <div className="card p-6 flex flex-col justify-between">
            <div>
              <h2 className="font-display font-bold text-xl text-white mb-5 border-b border-white/5 pb-3">Recent Interviews</h2>
              {loading ? (
                <div className="py-6"><Loader label="Loading history…" size="sm" /></div>
              ) : history.length === 0 ? (
                <p className="text-sm text-slate-400 font-body py-8 text-center">No interviews yet.</p>
              ) : (
                <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
                  {history.slice(0, 5).map((s) => (
                    <HistoryCard
                      key={s.sessionId}
                      session={s}
                      onClick={() => navigate(`/interview/result/${s.sessionId}`)}
                    />
                  ))}
                </div>
              )}
            </div>
            <Button variant="brand" className="w-full mt-6" onClick={() => navigate('/interview')}>
              Start Mock Interview
            </Button>
          </div>
        </div>

        {gapReport?.topGaps?.length > 0 && (
          <div className="card p-6">
            <h2 className="font-display font-bold text-xl text-white mb-4 border-b border-white/5 pb-3">Priority Gaps</h2>
            <div className="flex flex-wrap gap-3">
              {gapReport.topGaps.slice(0, 6).map((g, i) => (
                <span
                  key={i}
                  className="px-4 py-2 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs font-semibold font-body shadow-sm"
                >
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
