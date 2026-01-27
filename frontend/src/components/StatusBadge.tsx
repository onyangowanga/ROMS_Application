import React from 'react';
import { CandidateStatus } from '../types';

interface StatusBadgeProps {
  status: CandidateStatus | string;
}

const statusColors: Record<string, string> = {
  APPLIED: 'bg-blue-100 text-blue-800',
  SHORTLISTED: 'bg-purple-100 text-purple-800',
  INTERVIEW_SCHEDULED: 'bg-indigo-100 text-indigo-800',
  SELECTED: 'bg-cyan-100 text-cyan-800',
  MEDICAL_IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  MEDICAL_CLEARED: 'bg-green-100 text-green-800',
  DEPLOYED: 'bg-teal-100 text-teal-800',
  PLACED: 'bg-emerald-100 text-emerald-800',
  REJECTED: 'bg-red-100 text-red-800',
  WITHDRAWN: 'bg-gray-100 text-gray-800',
  
  // Expiry flags
  EXPIRING_SOON: 'bg-yellow-100 text-yellow-800',
  EXPIRED: 'bg-red-100 text-red-800',
  VALID: 'bg-green-100 text-green-800',
  
  // Medical status
  PENDING: 'bg-gray-100 text-gray-800',
  PASSED: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  WAIVED: 'bg-blue-100 text-blue-800',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const colorClass = statusColors[status] || 'bg-gray-100 text-gray-800';
  
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colorClass}`}>
      {status.replace(/_/g, ' ')}
    </span>
  );
};
