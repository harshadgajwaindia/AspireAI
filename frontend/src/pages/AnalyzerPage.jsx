import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Upload, FileText } from 'lucide-react'
import { Button, Loader } from '../components/shared/index.jsx'
import { GapReportView } from '../components/analyzer/GapReportView.jsx'
import { analyzerApi } from '../services/api.js'
import { useAppStore } from '../store/useAppStore.js'

const COMPANIES = [
  'General Placement Prep',
  'Full-Stack Developer Path',
  'AI & ML Engineer Guidance',
  'Data Structures & Algorithms Prep',
  'Core Computer Science Freshers',
  'TCS Digital',
  'Infosys SP',
  'Wipro Elite',
  'Cognizant GenC',
  'Accenture ASE',
]

export default function AnalyzerPage() {
  const navigate = useNavigate()
  const { userId, gapReport, setGapReport } = useAppStore()
  const fileRef = useRef(null)

  const [company, setCompany] = useState(COMPANIES[0])
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [report, setReport] = useState(gapReport)

  const handleFile = (e) => {
    const f = e.target.files?.[0]
    if (!f) return
    if (f.size > 5 * 1024 * 1024) {
      toast.error('Resume must be under 5MB')
      return
    }
    setFile(f)
  }

  const handleAnalyze = async () => {
    if (!file) {
      toast.error('Please upload your resume')
      return
    }
    setLoading(true)
    try {
      const data = await analyzerApi.analyze(file, company, userId)
      setReport(data)
      setGapReport(data)
      toast.success('Gap analysis complete')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-ink-50">
        <div className="h-1 bg-gradient-to-r from-brand-500 via-brand-600 to-violet-600" />
        <div className="max-w-3xl mx-auto px-4 py-20">
          <Loader label="Analyzing your resume with AI…" size="lg" />
          <p className="text-center text-sm text-slate-400 font-body mt-4">
            Extracting skills, comparing to {company} requirements…
          </p>
        </div>
      </div>
    )
  }

  if (report) {
    return (
      <div className="min-h-screen bg-ink-50">
        <div className="h-1 bg-gradient-to-r from-brand-500 via-brand-600 to-violet-600" />
        <div className="max-w-3xl mx-auto px-4 py-10">
          <div className="flex items-center justify-between mb-6">
            <h1 className="font-display font-bold text-2xl text-ink-900">Your Gap Report</h1>
            <Button variant="ghost" size="sm" onClick={() => { setReport(null); setFile(null) }}>
              Analyze again
            </Button>
          </div>
          <GapReportView report={report} />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-ink-50 animate-slide-up">
      <div className="max-w-2xl mx-auto px-6 py-12">
        <div className="mb-10">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full
                          bg-brand-500/10 border border-brand-500/25 text-brand-300
                          text-xs font-semibold font-body mb-4 shadow-sm shadow-brand-500/5 animate-pulse-slow">
            <FileText className="w-3.5 h-3.5" />
            Resume Gap Analyzer
          </div>
          <h1 className="font-display text-4xl font-extrabold text-white tracking-tight mb-3">
            Know your gaps
          </h1>
          <p className="text-slate-400 font-body text-sm leading-relaxed">
            Upload your resume and we&apos;ll compare your skills against what{' '}
            <span className="font-semibold text-white">{company}</span> actually requires.
          </p>
        </div>

        <div className="card p-8 space-y-6">
          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5 font-body">
              Target Pathway / Company
            </label>
            <div className="relative">
              <select
                value={company}
                onChange={(e) => setCompany(e.target.value)}
                className="w-full appearance-none rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm font-semibold text-slate-100 font-body
                           focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent cursor-pointer"
              >
                {COMPANIES.map((c) => (
                  <option key={c} value={c} className="bg-[#13141c] text-white">{c}</option>
                ))}
              </select>
              <svg className="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none"
                   fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5 font-body">
              Resume (PDF)
            </label>
            <input ref={fileRef} type="file" accept=".pdf,.doc,.docx" className="hidden" onChange={handleFile} />
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              className={`w-full rounded-xl border-2 border-dashed p-10 text-center transition-all duration-300
                ${file ? 'border-brand-500 bg-brand-500/10 shadow-glow shadow-brand-500/5' : 'border-white/10 hover:border-brand-500/50 hover:bg-white/[0.02]'}`}
            >
              <Upload className={`w-10 h-10 mx-auto mb-3 transition-colors ${file ? 'text-brand-400' : 'text-slate-500'}`} />
              {file ? (
                <>
                  <p className="font-display font-bold text-sm text-white">{file.name}</p>
                  <p className="text-xs text-slate-400 font-body mt-1.5">Click to change file</p>
                </>
              ) : (
                <>
                  <p className="font-display font-bold text-sm text-slate-200">Drop or click to upload</p>
                  <p className="text-xs text-slate-400 font-body mt-1.5">PDF, Word docs (max 5MB)</p>
                </>
              )}
            </button>
          </div>

          <Button onClick={handleAnalyze} disabled={!file} size="lg" variant="brand" className="w-full">
            Analyze Resume →
          </Button>

          {gapReport && (
            <button
              type="button"
              onClick={() => setReport(gapReport)}
              className="w-full text-sm text-brand-400 font-semibold font-body hover:underline hover:text-brand-300 transition-colors"
            >
              View last analysis
            </button>
          )}
        </div>

        <p className="text-center text-xs text-slate-400 font-body mt-8">
          Already have a roadmap?{' '}
          <button type="button" onClick={() => navigate('/roadmap')} className="text-brand-400 font-semibold hover:underline">
            Go to Roadmap
          </button>
        </p>
      </div>
    </div>
  )
}
