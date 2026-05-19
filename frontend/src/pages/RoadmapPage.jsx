import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Map, Calendar } from 'lucide-react'
import { Button, Loader } from '../components/shared/index.jsx'
import { RoadmapPlanView } from '../components/roadmap/RoadmapPlanView.jsx'
import { TodayTasks } from '../components/roadmap/TodayTasks.jsx'
import { roadmapApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'

const DURATIONS = [
  { days: 30, label: '30 days', desc: 'Intensive sprint' },
  { days: 60, label: '60 days', desc: 'Balanced pace' },
  { days: 90, label: '90 days', desc: 'Deep mastery' },
]

const STUDY_MINUTES = [60, 90, 120, 180]

export default function RoadmapPage() {
  const navigate = useNavigate()
  const { userId, gapReport, roadmapPlan, setRoadmapPlan } = useAppStore()

  const [duration, setDuration] = useState(60)
  const [minutes, setMinutes] = useState(90)
  const [loading, setLoading] = useState(false)
  const [plan, setPlan] = useState(roadmapPlan)
  const [taskRefresh, setTaskRefresh] = useState(0)

  const handleGenerate = async () => {
    if (!gapReport) {
      toast.error('Run the analyzer first')
      navigate('/')
      return
    }
    setLoading(true)
    try {
      const data = await roadmapApi.generate({
        userId,
        targetCompany: gapReport.targetCompany,
        durationDays: duration,
        dailyStudyMinutes: minutes,
        skillGapReport: gapReport,
      })
      setPlan(data)
      setRoadmapPlan(data)
      toast.success('Roadmap generated')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const refreshPlan = async () => {
    if (!plan?.planId) return
    try {
      const data = await roadmapApi.getPlan(plan.planId)
      setPlan(data)
      setRoadmapPlan(data)
    } catch (err) {
      toast.error(err.message)
    }
  }

  if (!gapReport) {
    return (
      <div className="min-h-screen bg-ink-50 flex items-center justify-center px-4">
        <div className="card p-8 max-w-md text-center">
          <Map className="w-12 h-12 text-brand-500 mx-auto mb-4" />
          <h1 className="font-display font-bold text-xl text-ink-900 mb-2">No gap report yet</h1>
          <p className="text-slate-500 font-body text-sm mb-6">
            Upload your resume in the Analyzer to generate a personalized study roadmap.
          </p>
          <Button onClick={() => navigate('/')} className="w-full">
            Go to Analyzer →
          </Button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-ink-50 flex items-center justify-center">
        <Loader label="Building your personalized roadmap…" size="lg" />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-5xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <div>
              <h1 className="font-display text-4xl font-extrabold text-white tracking-tight mb-1.5">Study Roadmap</h1>
              <p className="text-slate-400 font-body text-sm">
                Personalized plan for <strong className="text-white">{gapReport.targetCompany}</strong>
              </p>
            </div>

            {!plan ? (
              <div className="card p-8 space-y-6">
                <div>
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3 font-body">
                    Duration
                  </label>
                  <div className="grid grid-cols-3 gap-3">
                    {DURATIONS.map((d) => (
                      <button
                        key={d.days}
                        type="button"
                        onClick={() => setDuration(d.days)}
                        className={`p-4 rounded-xl border text-left transition-all duration-300
                          ${duration === d.days
                            ? 'border-brand-500 bg-brand-500/10 shadow-glow shadow-brand-500/5'
                            : 'border-white/10 hover:border-white/20 bg-white/[0.02]'
                          }`}
                      >
                        <div className="font-display font-bold text-sm text-white">{d.label}</div>
                        <div className="text-[11px] text-slate-400 font-body mt-1">{d.desc}</div>
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3 font-body">
                    Daily study time
                  </label>
                  <div className="flex flex-wrap gap-2.5">
                    {STUDY_MINUTES.map((m) => (
                      <button
                        key={m}
                        type="button"
                        onClick={() => setMinutes(m)}
                        className={`px-5 py-2.5 rounded-xl border text-sm font-bold font-display transition-all duration-300
                          ${minutes === m
                            ? 'border-brand-500 bg-brand-600 text-white shadow-lg shadow-brand-600/20'
                            : 'border-white/10 text-slate-300 hover:border-white/20 hover:bg-white/5'
                          }`}
                      >
                        {m} min
                      </button>
                    ))}
                  </div>
                </div>

                <Button onClick={handleGenerate} size="lg" variant="brand" className="w-full">
                  Generate Roadmap →
                </Button>
              </div>
            ) : (
              <>
                <div className="flex justify-end">
                  <Button variant="secondary" size="sm" onClick={() => { setPlan(null) }} className="border-white/10 hover:border-white/20 text-slate-300">
                    Regenerate
                  </Button>
                </div>
                <RoadmapPlanView plan={plan} />
              </>
            )}
          </div>

          <div className="space-y-6">
            <div className="card p-6">
              <div className="flex items-center gap-2 mb-4 border-b border-white/5 pb-3">
                <Calendar className="w-4.5 h-4.5 text-brand-400" />
                <h2 className="font-display font-bold text-lg text-white">Today</h2>
              </div>
              {plan ? (
                <TodayTasks
                  userId={userId}
                  refreshKey={taskRefresh}
                  onUpdate={() => {
                    setTaskRefresh((k) => k + 1)
                    refreshPlan()
                  }}
                />
              ) : (
                <p className="text-sm text-slate-400 font-body py-4 leading-relaxed">
                  Generate a roadmap to see today&apos;s tasks.
                </p>
              )}
            </div>

            {gapReport && (
              <div className="card p-5 bg-brand-500/10 border border-brand-500/20">
                <p className="text-xs font-bold text-brand-300 uppercase tracking-widest mb-1.5 font-body">
                  Baseline readiness
                </p>
                <p className="font-display font-bold text-3xl text-white shadow-glow shadow-brand-500/5">
                  {gapReport.overallReadiness}%
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
