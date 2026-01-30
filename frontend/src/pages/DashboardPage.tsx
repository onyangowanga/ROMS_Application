import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { candidateApi } from '../api/candidates';
import { dashboardApi, DashboardStats, EmployerDashboardStats } from '../api/dashboard';
import { Candidate, CandidateStatus } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import ApplicantWorkflowTimeline from '../components/ApplicantWorkflowTimeline';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    total: 0,
    statusBreakdown: {} as Record<CandidateStatus, number>,
    expiringSoon: 0,
  });
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  const [employerStats, setEmployerStats] = useState<EmployerDashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  const isEmployer = user?.role === 'EMPLOYER';
  const isStaff = user?.role && ['SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER'].includes(user.role);
  const isApplicant = user?.role === 'APPLICANT';

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      if (isEmployer && user?.email) {
        // Load employer-specific stats
        console.log('Loading employer stats for email:', user.email);
        const empStats = await dashboardApi.getEmployerStats(user.email);
        console.log('Employer stats received:', empStats);
        setEmployerStats(empStats);
      } else if (isStaff) {
        // Load comprehensive stats for staff
        const dashStats = await dashboardApi.getStats();
        setDashboardStats(dashStats);
        
        // Load candidate stats
        const candidates = await candidateApi.getAll();
        
        const breakdown: Record<string, number> = {};
        let expiringCount = 0;

        candidates.forEach((c: Candidate) => {
          breakdown[c.currentStatus] = (breakdown[c.currentStatus] || 0) + 1;
          if (c.expiryFlag === 'EXPIRING_SOON' || c.expiryFlag === 'EXPIRED') {
            expiringCount++;
          }
        });

        setStats({
          total: candidates.length,
          statusBreakdown: breakdown as Record<CandidateStatus, number>,
          expiringSoon: expiringCount,
        });
      }
    } catch (error) {
      console.error('Failed to load dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-12">Loading dashboard...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4 py-6 sm:px-0">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">
          Welcome, {user?.fullName}
        </h1>
        {isApplicant && <ApplicantWorkflowTimeline />}

        {/* Employer Dashboard */}
        {isEmployer && employerStats && (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 mb-8">
            {/* Total Jobs Posted */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Total Jobs Posted</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.totalJobsPosted}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Total Headcount Required */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Total Headcount Required</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.totalHeadcountRequired}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Pending Positions */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Pending Positions Headcount</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.pendingPositionsHeadcount}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Total Applications */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Total Applications Received</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.totalApplicationsReceived}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Filled Positions */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Filled Positions</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.filledPositions}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Total Placed */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Successfully Placed</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{employerStats.totalPlaced}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Staff Dashboard */}
        {isStaff && (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
            {/* Total Candidates */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <svg className="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">Total Candidates</dt>
                    <dd className="text-3xl font-semibold text-gray-900">{dashboardStats?.totalCandidates || stats.total}</dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Total Employers */}
          {dashboardStats && (
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Total Employers</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{dashboardStats.totalEmployers}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Total Job Orders */}
          {dashboardStats && (
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Total Jobs Posted</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{dashboardStats.totalJobOrders}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Open Positions (Total Headcount) */}
          {dashboardStats && (
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Open Positions</dt>
                      <dd className="text-3xl font-semibold text-gray-900">{dashboardStats.openPositions}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Expiring Documents */}
          {stats.expiringSoon > 0 && (
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Expiring Documents</dt>
                      <dd className="text-3xl font-semibold text-yellow-600">{stats.expiringSoon}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
        )}

        {/* Status Breakdown - Staff Only */}
        {isStaff && (
          <div className="bg-white shadow rounded-lg mb-8">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
                Candidates by Status
              </h3>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
                {Object.entries(stats.statusBreakdown).map(([status, count]) => (
                  <div key={status} className="border border-gray-200 rounded-lg p-4">
                    <StatusBadge status={status} />
                    <p className="mt-2 text-2xl font-semibold text-gray-900">{count}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="mt-8">
          {isStaff && (
            <Link
              to="/candidates"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              View All Candidates
            </Link>
          )}
          {isEmployer && (
            <Link
              to="/jobs"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              View My Jobs
            </Link>
          )}
        </div>
      </div>
    </Layout>
  );
};
