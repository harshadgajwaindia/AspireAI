import axios from "axios";
import { useAuthStore } from "../store/authStore";

// Determine base URL: use VITE_API_BASE_URL for production, fall back to /api for dev proxy
const getBaseURL = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  return "/api";
};

// Single axios instance — base URL handled by environment or Vite proxy in dev
const api = axios.create({
  baseURL: getBaseURL(),
  headers: { "Content-Type": "application/json" },
  timeout: 1200000, 
});

// Request interceptor for adding the bearer token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Global error interceptor
api.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error("API Error Response:", err.response);
    if (err.response && err.response.status === 401) {
      useAuthStore.getState().logout();
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }

    const message =
      err.response?.data?.detail ||
      err.response?.data?.message ||
      err.message ||
      "Something went wrong";
    return Promise.reject(new Error(message));
  }
);

// ── Analyzer ──────────────────────────────────────────────────────────────────
export const analyzerApi = {
  analyze: (file, targetCompany, userId, preparationType) => {
    const form = new FormData();
    form.append("resume", file);
    form.append("targetCompany", targetCompany);
    form.append("userId", userId);
    form.append("preparationType", preparationType || "COMPANY");
    return api.post("/v1/analyzer/analyze", form, {
      headers: { "Content-Type": "multipart/form-data" },
    }).then((r) => r.data);
  },
};

// ── Roadmap ───────────────────────────────────────────────────────────────────
export const roadmapApi = {
  generate: (payload) => api.post("/v1/roadmap/generate", payload).then((r) => r.data),
  getPlan:  (planId) =>  api.get(`/v1/roadmap/${planId}`).then((r) => r.data),
  getToday: (userId) =>  api.get(`/v1/roadmap/today/${userId}`).then((r) => r.data),
  complete: (itemId) =>  api.patch(`/v1/roadmap/items/${itemId}/complete`),
  skip:     (itemId) =>  api.patch(`/v1/roadmap/items/${itemId}/skip`),
};

// ── Interview ─────────────────────────────────────────────────────────────────
export const interviewApi = {
  start:      (payload)    => api.post("/v1/interview/start", payload).then((r) => r.data),
  answer:     (payload)    => api.post("/v1/interview/answer", payload).then((r) => r.data),
  getResult:  (sessionId)  => api.get(`/v1/interview/result/${sessionId}`).then((r) => r.data),
  getHistory: (userId)     => api.get(`/v1/interview/history/${userId}`).then((r) => r.data),
};

export default api;