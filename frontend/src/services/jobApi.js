import { useQuery } from '@tanstack/react-query';
import api from '../lib/api.js';

export const fetchRecommendedJobs = async (limit = 10) => {
  const response = await api.get(`/v1/analyzer/jobs/recommended?limit=${limit}`);
  return response.data;
};

export const useRecommendedJobs = (limit = 10) => {
  return useQuery({
    queryKey: ['recommendedJobs', limit],
    queryFn: () => fetchRecommendedJobs(limit),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
