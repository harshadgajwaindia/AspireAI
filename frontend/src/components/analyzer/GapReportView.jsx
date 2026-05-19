import { useNavigate } from 'react-router-dom'
import { ScoreRing, Badge, Button } from '../shared/index.jsx'

function GapRow({ gap }) {
  const pct = gap.requiredScore > 0
    ? Math.min(100, (gap.studentScore / gap.requiredScore) * 100)
    : 0
  const barColor = gap.gapSize <= 1 ? 'bg-emerald-500' : gap.gapSize <= 2 ? 'bg-amber-400' : 'bg-rose-500'

  return (
    <div className="py-4 border-b border-white/5 last:border-0">
      <div className="flex items-center justify-between mb-2">
        <span className="font-display font-bold text-sm text-slate-200">{gap.skillName}</span>
        <Badge variant={
          gap.priority === 'HIGH' ? 'danger' :
          gap.priority === 'MEDIUM' ? 'warning' : 'default'
        } className="px-2 py-0.5">
          {gap.priority}
        </Badge>
      </div>
      <div className="flex items-center gap-3 text-xs text-slate-400 font-body mb-2.5">
        <span>You: <strong className="text-slate-200">{gap.studentScore}</strong></span>
        <span>Required: <strong className="text-slate-200">{gap.requiredScore}</strong></span>
        <span className="text-rose-400 font-bold ml-auto bg-rose-500/10 px-2 py-0.5 rounded-md">Gap: {gap.gapSize}</span>
      </div>
      <div className="h-2 bg-white/5 rounded-full overflow-hidden">
        <div className={`h-full rounded-full ${barColor}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

export function GapReportView({ report }) {
  const navigate = useNavigate()
  const profile = report.skillProfile

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="card p-6">
        <div className="flex flex-col sm:flex-row items-center gap-6">
          <ScoreRing score={report.overallReadiness} size={130} />
          <div className="flex-1 text-center sm:text-left min-w-0">
            <Badge variant="brand" className="mb-2 px-2.5 py-0.5">{report.targetCompany}</Badge>
            <h2 className="font-display font-extrabold text-2xl text-white mb-2 tracking-tight">
              Placement Readiness
            </h2>
            <p className="text-slate-400 font-body text-sm leading-relaxed">
              {profile?.summary || 'Analysis complete. Review your gaps below.'}
            </p>
            {profile?.targetRole && (
              <p className="text-xs text-slate-500 font-body mt-3">
                Target role: <span className="font-semibold text-slate-300">{profile.targetRole}</span>
              </p>
            )}
          </div>
        </div>
      </div>

      {report.topGaps?.length > 0 && (
        <div className="card p-6">
          <h3 className="font-display font-bold text-lg text-white mb-4 border-b border-white/5 pb-2">Top Skill Gaps</h3>
          <div className="divide-y divide-white/5">
            {report.topGaps.map((gap, i) => (
              <GapRow key={`${gap.skillName}-${i}`} gap={gap} />
            ))}
          </div>
        </div>
      )}

      {profile?.skills?.length > 0 && (
        <div className="card p-6">
          <h3 className="font-display font-bold text-lg text-white mb-4 border-b border-white/5 pb-2">Skill Profile</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {profile.skills.map((skill, i) => (
              <div key={i} className="rounded-xl border border-white/5 p-4 bg-white/[0.02] hover:border-white/10 transition-colors duration-200">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-display font-bold text-sm text-slate-200">{skill.name}</span>
                  <span className="text-xs font-bold text-brand-400">{skill.score}/10</span>
                </div>
                <Badge variant="default" className="mb-3 px-2 py-0.5">{skill.category}</Badge>
                {skill.evidence && (
                  <p className="text-xs text-slate-400 font-body leading-relaxed">{skill.evidence}</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {profile?.projectNames?.length > 0 && (
        <div className="card p-6">
          <h3 className="font-display font-bold text-sm text-white mb-4 border-b border-white/5 pb-2">Projects Detected</h3>
          <div className="flex flex-wrap gap-2.5">
            {profile.projectNames.map((p, i) => (
              <span key={i} className="px-3 py-1.5 bg-white/5 border border-white/5 rounded-lg text-xs font-semibold font-body text-slate-300 shadow-sm">
                {p}
              </span>
            ))}
          </div>
        </div>
      )}

      {profile?.githubProfile?.username && (
        <div className="card p-6">
          <h3 className="font-display font-bold text-sm text-white mb-4 border-b border-white/5 pb-2">GitHub</h3>
          <p className="text-sm font-body text-slate-300">
            <span className="text-brand-400 font-bold">@{profile.githubProfile.username}</span> · {profile.githubProfile.repoCount} repositories ·{' '}
            <span className="text-amber-400 font-semibold">{profile.githubProfile.totalStars} ★</span>
          </p>
        </div>
      )}

      <div className="flex flex-col sm:flex-row gap-4 pt-4">
        <Button onClick={() => navigate('/roadmap')} variant="brand" className="flex-1 py-3.5 shadow-md">
          Generate Roadmap →
        </Button>
        <Button onClick={() => navigate('/interview')} variant="secondary" className="flex-1 py-3.5 border-white/10 hover:border-white/20">
          Practice Interview
        </Button>
      </div>
    </div>
  )
}
