import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Map, Calendar } from 'lucide-react'
import { Button, Loader } from '../components/shared/index.jsx'
import { RoadmapPlanView } from '../components/roadmap/RoadmapPlanView.jsx'
import { TodayTasks } from '../components/roadmap/TodayTasks.jsx'
import { RoadmapGeneratorCard } from '../components/roadmap/RoadmapGeneratorCard.jsx'
import { roadmapApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'

export default function RoadmapPage() {
  const navigate = useNavigate()
  const { userId, gapReport, roadmapPlan, setRoadmapPlan } = useAppStore()

  const [duration, setDuration] = useState(60)
  const [minutes, setMinutes] = useState(90)
  const [loading, setLoading] = useState(false)
  const [plan, setPlan] = useState(roadmapPlan)
  const [taskRefresh, setTaskRefresh] = useState(0)

  const handleGenerate = async () => {
    if (!gapReport) return navigate('/') || toast.error('Run the analyzer first')
    setLoading(true)
    try {
      const data = await roadmapApi.generate({ userId, targetCompany: gapReport.targetCompany, durationDays: duration, dailyStudyMinutes: minutes, skillGapReport: gapReport })
      setPlan(data); setRoadmapPlan(data); toast.success('Roadmap generated')
    } catch (err) { toast.error(err.message) } finally { setLoading(false) }
  }

  const refreshPlan = async () => {
    if (!plan?.planId) return
    try { const data = await roadmapApi.getPlan(plan.planId); setPlan(data); setRoadmapPlan(data); } catch (err) { toast.error(err.message) }
  }

  if (!gapReport) return (
    <div className="min-h-screen bg-ink-50 flex items-center justify-center px-4">
      <div className="card p-8 max-w-md text-center">
        <Map className="w-12 h-12 text-brand-500 mx-auto mb-4" />
        <h1 className="font-display font-bold text-xl text-ink-900 mb-2">No gap report yet</h1>
        <p className="text-slate-500 font-body text-sm mb-6">Upload your resume to generate a personalized study roadmap.</p>
        <Button onClick={() => navigate('/')} className="w-full">Go to Analyzer →</Button>
      </div>
    </div>
  )

  if (loading) return <div className="min-h-screen bg-ink-50 flex items-center justify-center"><Loader label="Building your personalized roadmap…" size="lg" /></div>

  return (
    <div className="min-h-screen bg-ink-50">
      <div className="max-w-5xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <div>
              <h1 className="font-display text-4xl font-extrabold text-white tracking-tight mb-1.5">Study Roadmap</h1>
              <p className="text-slate-400 text-sm">Personalized plan for <strong className="text-white">{gapReport.targetCompany}</strong></p>
            </div>
            {!plan ? <RoadmapGeneratorCard duration={duration} setDuration={setDuration} minutes={minutes} setMinutes={setMinutes} handleGenerate={handleGenerate} /> : (
              <><div className="flex justify-end"><Button variant="secondary" size="sm" onClick={() => setPlan(null)}>Regenerate</Button></div><RoadmapPlanView plan={plan} /></>
            )}
          </div>
          <div className="space-y-6">
            <div className="card p-6">
              <div className="flex items-center gap-2 mb-4 border-b border-white/5 pb-3">
                <Calendar className="w-4.5 h-4.5 text-brand-400" /><h2 className="font-display font-bold text-lg text-white">Today</h2>
              </div>
              {plan ? <TodayTasks userId={userId} refreshKey={taskRefresh} onUpdate={() => { setTaskRefresh(k => k + 1); refreshPlan() }} /> : <p className="text-sm text-slate-400 py-4">Generate a roadmap to see today's tasks.</p>}
            </div>
            {gapReport && (
              <div className="card p-5 bg-brand-500/10 border border-brand-500/20">
                <p className="text-xs font-bold text-brand-300 uppercase tracking-widest mb-1.5 font-body">Baseline readiness</p>
                <p className="font-display font-bold text-3xl text-white shadow-glow shadow-brand-500/5">{gapReport.overallReadiness}%</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
