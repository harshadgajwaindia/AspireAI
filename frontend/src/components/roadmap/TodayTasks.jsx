import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { CheckCircle2, SkipForward, ExternalLink } from 'lucide-react'
import { Badge, Button } from '../shared/index.jsx'
import { roadmapApi } from '../../services/api.js'

export function TodayTasks({ userId, refreshKey = 0, onUpdate }) {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [acting, setActing] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    roadmapApi.getToday(userId)
      .then((data) => { if (!cancelled) setTasks(data || []) })
      .catch((err) => { if (!cancelled) toast.error(err.message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [userId, refreshKey])

  const handleComplete = async (itemId) => {
    setActing(itemId)
    try {
      await roadmapApi.complete(itemId)
      toast.success('Task completed')
      onUpdate?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActing(null)
    }
  }

  const handleSkip = async (itemId) => {
    setActing(itemId)
    try {
      await roadmapApi.skip(itemId)
      toast.success('Task skipped')
      onUpdate?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActing(null)
    }
  }

  if (loading) {
    return <p className="text-sm text-slate-400 font-body py-4">Loading today&apos;s tasks…</p>
  }

  if (tasks.length === 0) {
    return (
      <p className="text-sm text-slate-400 font-body py-4">
        No tasks scheduled for today. Great job staying ahead!
      </p>
    )
  }

  return (
    <div className="space-y-3">
      {tasks.map((item) => (
        <div key={item.id} className="rounded-xl border border-white/5 p-4 bg-white/[0.02] hover:border-white/10 transition-colors duration-250">
          <div className="flex items-start justify-between gap-3 mb-2">
            <div>
              <Badge variant="brand" className="mb-1.5 px-2 py-0.5">{item.skillName}</Badge>
              <h4 className="font-display font-bold text-sm text-slate-200">{item.taskTitle}</h4>
            </div>
            <span className="text-xs text-slate-400 font-body whitespace-nowrap">
              {item.estimatedMinutes} min
            </span>
          </div>
          <p className="text-xs text-slate-400 font-body leading-relaxed mb-3">{item.taskDescription}</p>
          {item.ragInsight && (
            <p className="text-xs text-brand-300 font-semibold font-body mb-3 bg-brand-500/10 p-2 rounded-lg border border-brand-500/10">
              💡 {item.ragInsight}
            </p>
          )}
          <div className="flex flex-wrap gap-2.5 items-center">
            {item.resourceUrl && (
              <a
                href={item.resourceUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1 text-xs text-brand-400 font-semibold font-body hover:text-brand-300 hover:underline"
              >
                <ExternalLink className="w-3.5 h-3.5" />
                Resource
              </a>
            )}
            <div className="flex gap-2 ml-auto">
              <Button size="sm" variant="ghost" disabled={acting === item.id} onClick={() => handleSkip(item.id)}>
                <SkipForward className="w-3.5 h-3.5" />
                Skip
              </Button>
              <Button
                size="sm"
                variant="brand"
                loading={acting === item.id}
                onClick={() => handleComplete(item.id)}
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                Done
              </Button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
