import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const RoleBasedRedirect: React.FC = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">Loading...</div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Redirect applicants to their application page
  if (user.role === 'APPLICANT') {
    return <Navigate to="/my-application" replace />;
  }

  // Redirect all other roles to dashboard
  return <Navigate to="/dashboard" replace />;
};
