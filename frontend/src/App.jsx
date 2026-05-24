import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { Header } from './components/layout/Header.jsx'
import AnalyzerPage from './pages/AnalyzerPage.jsx'
import RoadmapPage from './pages/RoadmapPage.jsx'
import InterviewPage from './pages/InterviewPage.jsx'
import InterviewResultPage from './pages/InterviewResultPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import SignupPage from './pages/SignupPage.jsx'
import { useAuthStore } from './store/authStore';
import RecommendedJobsPage from './pages/RecommendedJobsPage.jsx';

const ProtectedRoute = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return isAuthenticated ? children : <Navigate to="/login" />;
};

export default function App() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          style: { fontFamily: "'DM Sans', sans-serif", fontSize: '14px' },
          success: { iconTheme: { primary: '#10b981', secondary: 'white' } },
          error: { iconTheme: { primary: '#ef4444', secondary: 'white' } },
        }}
      />
      {isAuthenticated && <Header />}
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" />} />
        <Route path="/signup" element={!isAuthenticated ? <SignupPage /> : <Navigate to="/" />} />
        
        <Route path="/" element={<ProtectedRoute><AnalyzerPage /></ProtectedRoute>} />
        <Route path="/roadmap" element={<ProtectedRoute><RoadmapPage /></ProtectedRoute>} />
        <Route path="/interview" element={<ProtectedRoute><InterviewPage /></ProtectedRoute>} />
        <Route path="/interview/result/:sessionId" element={<ProtectedRoute><InterviewResultPage /></ProtectedRoute>} />
        <Route path="/jobs/recommended" element={<ProtectedRoute><RecommendedJobsPage /></ProtectedRoute>} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
    
      </Routes>
    </BrowserRouter>
  )
}
