import { Button } from '../shared/index.jsx'

const DURATIONS = [
  { days: 30, label: '30 days', desc: 'Intensive sprint' },
  { days: 60, label: '60 days', desc: 'Balanced pace' },
  { days: 90, label: '90 days', desc: 'Deep mastery' },
]

const STUDY_MINUTES = [60, 90, 120, 180]

export function RoadmapGeneratorCard({ duration, setDuration, minutes, setMinutes, handleGenerate }) {
  return (
    <div className="card p-8 space-y-6">
      <div>
        <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3 font-body">Duration</label>
        <div className="grid grid-cols-3 gap-3">
          {DURATIONS.map((d) => (
            <button key={d.days} type="button" onClick={() => setDuration(d.days)}
              className={`p-4 rounded-xl border text-left transition-all duration-300 ${duration === d.days ? 'border-brand-500 bg-brand-500/10 shadow-glow shadow-brand-500/5' : 'border-white/10 hover:border-white/20 bg-white/[0.02]'}`}>
              <div className="font-display font-bold text-sm text-white">{d.label}</div>
              <div className="text-[11px] text-slate-400 font-body mt-1">{d.desc}</div>
            </button>
          ))}
        </div>
      </div>
      <div>
        <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3 font-body">Daily study time</label>
        <div className="flex flex-wrap gap-2.5">
          {STUDY_MINUTES.map((m) => (
            <button key={m} type="button" onClick={() => setMinutes(m)}
              className={`px-5 py-2.5 rounded-xl border text-sm font-bold font-display transition-all duration-300 ${minutes === m ? 'border-brand-500 bg-brand-600 text-white shadow-lg shadow-brand-600/20' : 'border-white/10 text-slate-300 hover:border-white/20 hover:bg-white/5'}`}>
              {m} min
            </button>
          ))}
        </div>
      </div>
      <Button onClick={handleGenerate} size="lg" variant="brand" className="w-full">Generate Roadmap →</Button>
    </div>
  )
}
