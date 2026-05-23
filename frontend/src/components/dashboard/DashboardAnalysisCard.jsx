import { useNavigate } from 'react-router-dom'
import { FileSearch } from 'lucide-react'
import { ScoreRing, Badge, Button } from '../shared/index.jsx'

export function DashboardAnalysisCard({ gapReport }) {
  const navigate = useNavigate()

  if (!gapReport) {
    return (
      <div className="card p-8 flex flex-col justify-center text-center">
        <div className="w-12 h-12 rounded-full bg-brand-500/10 flex items-center justify-center mx-auto mb-4 border border-brand-500/20">
          <FileSearch className="w-6 h-6 text-brand-400" />
        </div>
        <h2 className="font-display font-bold text-xl text-white mb-2">Get started</h2>
        <p className="text-sm text-slate-400 font-body mb-6 max-w-md mx-auto">Upload your resume to unlock your study roadmap and targeted mock interviews.</p>
        <Button onClick={() => navigate('/')} variant="brand" className="w-full">Analyze Resume →</Button>
      </div>
    )
  }

  return (
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
      <Button variant="secondary" size="md" className="mt-6 w-full" onClick={() => navigate('/')}>View report</Button>
    </div>
  )
}
