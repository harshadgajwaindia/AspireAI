import { useRef } from 'react'
import { Upload, FileText } from 'lucide-react'
import { Button } from '../shared/index.jsx'

const COMPANIES = ['TCS Digital', 'Infosys SP', 'Wipro Elite', 'Cognizant GenC', 'Accenture ASE']
const FIELDS = ['Backend Developer', 'Frontend Developer', 'Full-Stack Developer', 'AI & ML Engineer', 'Data Structures & Algorithms', 'Core Computer Science', 'Custom Role...']

export function AnalyzerForm({
  preparationType, setPreparationType, company, setCompany,
  customRole, setCustomRole, file, setFile, handleAnalyze,
  gapReport, setReport
}) {
  const fileRef = useRef(null)

  const handleFile = (e) => {
    const f = e.target.files?.[0]
    if (!f) return
    if (f.size > 5 * 1024 * 1024) throw new Error('Resume must be under 5MB')
    setFile(f)
  }

  return (
    <div className="card p-8 space-y-6">
      <div className="flex p-1 rounded-xl bg-white/5 border border-white/10">
        <button type="button" onClick={() => { setPreparationType('COMPANY'); setCompany(COMPANIES[0]) }}
          className={`flex-1 py-2.5 text-xs font-bold uppercase tracking-wider rounded-lg transition-all duration-300 ${preparationType === 'COMPANY' ? 'bg-brand-500 text-white shadow-glow shadow-brand-500/10' : 'text-slate-400 hover:text-white'}`}>
          Company-Specific
        </button>
        <button type="button" onClick={() => { setPreparationType('FIELD'); setCompany(FIELDS[0]) }}
          className={`flex-1 py-2.5 text-xs font-bold uppercase tracking-wider rounded-lg transition-all duration-300 ${preparationType === 'FIELD' ? 'bg-brand-500 text-white shadow-glow shadow-brand-500/10' : 'text-slate-400 hover:text-white'}`}>
          Field / Pathway
        </button>
      </div>

      <div>
        <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5">
          {preparationType === 'COMPANY' ? 'Target Company' : 'Target Field / Pathway'}
        </label>
        <div className="relative">
          <select value={company} onChange={(e) => setCompany(e.target.value)}
            className="w-full appearance-none rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm font-semibold text-slate-100 focus:outline-none focus:ring-2 focus:ring-brand-500 cursor-pointer">
            {(preparationType === 'COMPANY' ? COMPANIES : FIELDS).map(c => <option key={c} value={c} className="bg-[#13141c] text-white">{c}</option>)}
          </select>
        </div>
      </div>

      {preparationType === 'FIELD' && company === 'Custom Role...' && (
        <div className="animate-slide-down">
          <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5">Specify Custom Role / Pathway</label>
          <input type="text" value={customRole} onChange={(e) => setCustomRole(e.target.value)} placeholder="e.g. Cloud Architect, Golang Backend Developer..."
            className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm font-semibold text-slate-100 focus:outline-none focus:ring-2 focus:ring-brand-500" />
        </div>
      )}

      <div>
        <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2.5">Resume (PDF)</label>
        <input ref={fileRef} type="file" accept=".pdf,.doc,.docx" className="hidden" onChange={handleFile} />
        <button type="button" onClick={() => fileRef.current?.click()}
          className={`w-full rounded-xl border-2 border-dashed p-10 text-center transition-all duration-300 ${file ? 'border-brand-500 bg-brand-500/10' : 'border-white/10 hover:border-brand-500/50'}`}>
          <Upload className={`w-10 h-10 mx-auto mb-3 transition-colors ${file ? 'text-brand-400' : 'text-slate-500'}`} />
          {file ? (
            <><p className="font-display font-bold text-sm text-white">{file.name}</p><p className="text-xs text-slate-400 mt-1.5">Click to change file</p></>
          ) : (
            <><p className="font-display font-bold text-sm text-slate-200">Drop or click to upload</p><p className="text-xs text-slate-400 mt-1.5">PDF, Word docs (max 5MB)</p></>
          )}
        </button>
      </div>

      <Button onClick={handleAnalyze} disabled={!file} size="lg" variant="brand" className="w-full">Analyze Resume →</Button>
      {gapReport && <button type="button" onClick={() => setReport(gapReport)} className="w-full text-sm text-brand-400 font-semibold hover:underline">View last analysis</button>}
    </div>
  )
}
