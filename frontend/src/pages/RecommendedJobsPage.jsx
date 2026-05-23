import { useRecommendedJobs } from '../services/jobApi.js';
import { Loader, Button } from '../components/shared/index.jsx';
import { useNavigate } from 'react-router-dom';

export default function RecommendedJobsPage() {
  const navigate = useNavigate();
  const { data: jobs, isLoading, isError, error } = useRecommendedJobs(10);

  return (
    <div className="min-h-screen bg-ink-50 flex flex-col items-center p-6">
      <div className="max-w-4xl w-full space-y-6">
        <h1 className="font-display text-3xl text-white mb-4">Recommended Job Postings</h1>
        {isLoading && (
          <div className="flex justify-center py-8">
            <Loader label="Loading jobs…" size="lg" />
          </div>
        )}
        {isError && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-400">
            Failed to load jobs: {error?.message || 'Unknown error'}
          </div>
        )}
        {jobs && jobs.length === 0 && (
          <p className="text-slate-400">No job postings found.</p>
        )}
        {jobs && jobs.map((job) => (
          <div key={job.id} className="card p-5 hover:shadow-glow transition-shadow">
            <div className="flex flex-col md:flex-row md:justify-between">
              <div className="flex-1">
                <h2 className="font-display text-lg text-white mb-1">{job.roleTitle}</h2>
                <p className="text-slate-300 text-sm mb-2">{job.companyName}</p>
                <p className="text-slate-400 text-xs">Location: {job.location}</p>
                {job.requiredSkills && (
                  <p className="text-slate-400 text-xs mt-1">Skills: {job.requiredSkills}</p>
                )}
              </div>
              <div className="flex items-center mt-3 md:mt-0">
                <Button
                  onClick={() => window.open(job.sourceUrl, '_blank')}
                  variant="brand"
                  className="ml-2"
                >
                  View Details
                </Button>
              </div>
            </div>
          </div>
        ))}
        <Button onClick={() => navigate('/dashboard')} variant="secondary">Back to Dashboard</Button>
      </div>
    </div>
  );
}
