import { Link } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'

export function StatCard({ icon: Icon, label, value, sub, to, accent = 'brand' }) {
  const accentMap = {
    brand: 'from-brand-500 to-brand-600 shadow-brand-500/10',
    emerald: 'from-emerald-500 to-emerald-600 shadow-emerald-500/10',
    violet: 'from-violet-500 to-violet-600 shadow-violet-500/10',
  }

  return (
    <Link to={to} className="card p-6 hover:shadow-glow hover:border-brand-500/20 group block">
      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${accentMap[accent]} flex items-center justify-center mb-5 shadow-lg`}>
        <Icon className="w-5.5 h-5.5 text-white" />
      </div>
      <p className="text-xs font-bold text-slate-400 uppercase tracking-widest font-body mb-2">{label}</p>
      <p className="font-display font-bold text-3xl text-white">{value}</p>
      {sub && <p className="text-xs text-slate-400 font-body mt-2">{sub}</p>}
      <span className="inline-flex items-center gap-1.5 text-xs text-brand-400 font-bold font-body mt-4 group-hover:gap-2.5 transition-all">
        Open <ArrowRight className="w-3.5 h-3.5" />
      </span>
    </Link>
  )
}
