import { Badge } from '../shared/index.jsx'

export function HistoryCard({ session, onClick }) {
  const gradeVariant = session.overallGrade === 'Ready' ? 'success'
    : session.overallGrade === 'Almost Ready' ? 'warning' : 'danger'

  return (
    <button type="button" onClick={onClick} className="card p-5 w-full text-left hover:border-brand-500/30 transition-all duration-300 hover:shadow-glow hover:shadow-brand-500/5 group">
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
