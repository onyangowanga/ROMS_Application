import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { RoleBasedRedirect } from './components/RoleBasedRedirect';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { CandidatesPage } from './pages/CandidatesPage';
import { CandidateProfilePage } from './pages/CandidateProfilePage';
import ApplicantRegisterPage from './pages/ApplicantRegisterPage';
import EmployerRegisterPage from './pages/EmployerRegisterPage';
import EmployersPage from './pages/EmployersPage';
import JobsPage from './pages/JobsPage';
import JobNewPage from './pages/JobNewPage';
import MyApplicationPage from './pages/MyApplicationPage';
import { UsersPage } from './pages/UsersPage';
import './index.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<ApplicantRegisterPage />} />
          <Route path="/employer/register" element={<EmployerRegisterPage />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER', 'EMPLOYER']}>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/candidates"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER']}>
                <CandidatesPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/candidates/:id"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'OPERATIONS_STAFF']}>
                <CandidateProfilePage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/employers"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'OPERATIONS_STAFF']}>
                <EmployersPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/jobs"
            element={
              <ProtectedRoute>
                <JobsPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/jobs/new"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'EMPLOYER']}>
                <JobNewPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/users"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
                <UsersPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/my-application"
            element={
              <ProtectedRoute allowedRoles={['APPLICANT']}>
                <MyApplicationPage />
              </ProtectedRoute>
            }
          />
          
          <Route path="/" element={<RoleBasedRedirect />} />
          <Route path="*" element={<RoleBasedRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
