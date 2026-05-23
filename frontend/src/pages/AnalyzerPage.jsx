import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { FileText } from 'lucide-react'
import { Button, Loader } from '../components/shared/index.jsx'
import { GapReportView } from '../components/analyzer/GapReportView.jsx'
import { analyzerApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'
import { AnalyzerForm } from '../components/analyzer/AnalyzerForm.jsx'

export default function AnalyzerPage() {
  const navigate = useNavigate()
  const { userId, gapReport, setGapReport } = useAppStore()

  const [preparationType, setPreparationType] = useState('COMPANY')
  const [company, setCompany] = useState('TCS Digital')
  const [customRole, setCustomRole] = useState('')
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [report, setReport] = useState(gapReport)

  const handleAnalyze = async () => {
    if (!file) return toast.error('Please upload your resume')
    const target = preparationType === 'FIELD' && company === 'Custom Role...' ? customRole : company
    if (!target?.trim()) return toast.error('Please specify your target company or pathway')
    
    setLoading(true)
    try {
      const data = await analyzerApi.analyze(file, target, userId, preparationType)
      setReport(data); setGapReport(data); toast.success('Gap analysis complete')
    } catch (err) { toast.error(err.message) }
    finally { setLoading(false) }
  }

  if (loading) return (
    <div className="min-h-screen bg-ink-50">
      <div className="h-1 bg-gradient-to-r from-brand-500 via-brand-600 to-violet-600" />
      <div className="max-w-3xl mx-auto px-4 py-20">
        <Loader label="Analyzing your resume with AI…" size="lg" />
        <p className="text-center text-sm text-slate-400 mt-4">Extracting skills, comparing to requirements…</p>
      </div>
    </div>
  )

  if (report) return (
    <div className="min-h-screen bg-ink-50">
      <div className="h-1 bg-gradient-to-r from-brand-500 via-brand-600 to-violet-600" />
      <div className="max-w-3xl mx-auto px-4 py-10">
        <div className="flex items-center justify-between mb-6">
          <h1 className="font-display font-bold text-2xl text-ink-900">Your Gap Report</h1>
          <Button variant="ghost" size="sm" onClick={() => { setReport(null); setFile(null) }}>Analyze again</Button>
        </div>
        <GapReportView report={report} />
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-ink-50 animate-slide-up">
      <div className="max-w-2xl mx-auto px-6 py-12">
        <div className="mb-10">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-brand-500/10 border border-brand-500/25 text-brand-300 text-xs font-semibold mb-4 animate-pulse-slow">
            <FileText className="w-3.5 h-3.5" /> Resume Gap Analyzer
          </div>
          <h1 className="font-display text-4xl font-extrabold text-white tracking-tight mb-3">Know your gaps</h1>
          <p className="text-slate-400 text-sm leading-relaxed">Upload your resume and we'll compare your skills against what actually requires.</p>
        </div>
        <AnalyzerForm {...{ preparationType, setPreparationType, company, setCompany, customRole, setCustomRole, file, setFile, handleAnalyze, gapReport, setReport }} />
        <p className="text-center text-xs text-slate-400 mt-8">
          Already have a roadmap? <button onClick={() => navigate('/roadmap')} className="text-brand-400 font-semibold hover:underline">Go to Roadmap</button>
        </p>
      </div>
    </div>
  )
}
