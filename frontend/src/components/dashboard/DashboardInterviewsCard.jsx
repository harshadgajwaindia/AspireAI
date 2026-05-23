import { useNavigate } from 'react-router-dom'
import { Button, Loader } from '../shared/index.jsx'
import { HistoryCard } from './HistoryCard.jsx'

export function DashboardInterviewsCard({ history, loading }) {
  const navigate = useNavigate()

  return (
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
              <HistoryCard key={s.sessionId} session={s} onClick={() => navigate(`/interview/result/${s.sessionId}`)} />
            ))}
          </div>
        )}
      </div>
      <Button variant="brand" className="w-full mt-6" onClick={() => navigate('/interview')}>Start Mock Interview</Button>
    </div>
  )
}
