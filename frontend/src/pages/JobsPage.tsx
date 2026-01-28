import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { jobsApi } from '../api/jobs';
import { JobOrder } from '../types';
import { useAuth } from '../context/AuthContext';

const JobsPage: React.FC = () => {
  const { user } = useAuth();
  const [jobs, setJobs] = useState<JobOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      setLoading(true);
      const data = await jobsApi.getAllJobs();
      setJobs(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load job orders');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (jobId: number) => {
    if (!window.confirm('Approve this job order?')) return;
    
    try {
      await jobsApi.updateJobStatus(jobId, 'OPEN');
      fetchJobs();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve job order');
    }
  };

  const handleReject = async (jobId: number) => {
    if (!window.confirm('Reject this job order?')) return;
    
    try {
      await jobsApi.updateJobStatus(jobId, 'CLOSED');
      fetchJobs();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject job order');
    }
  };

  const getStatusBadge = (status: string) => {
    const colors: { [key: string]: string } = {
      'PENDING_APPROVAL': 'bg-yellow-100 text-yellow-800',
      'OPEN': 'bg-green-100 text-green-800',
      'CLOSED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-xl text-gray-600">Loading job orders...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    );
  }

  const canCreateJob = user?.role === 'EMPLOYER' || user?.role === 'SUPER_ADMIN';
  const canApprove = user?.role === 'SUPER_ADMIN';

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Job Orders</h1>
        {canCreateJob && (
          <Link
            to="/jobs/new"
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            Create Job Order
          </Link>
        )}
      </div>

      <div className="bg-white shadow-md rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Job Order Ref
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Job Title
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Location
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Headcount
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Salary Range
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              {canApprove && (
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {jobs.length === 0 ? (
              <tr>
                <td colSpan={canApprove ? 7 : 6} className="px-6 py-4 text-center text-gray-500">
                  No job orders found
                </td>
              </tr>
            ) : (
              jobs.map((job) => (
                <tr key={job.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {job.jobOrderRef}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900">{job.jobTitle}</div>
                    <div className="text-sm text-gray-500">{job.description}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{job.location}</div>
                    <div className="text-sm text-gray-500">{job.country}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {job.headcountFilled || 0} / {job.headcountRequired}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {job.currency} {job.salaryMin?.toLocaleString()} - {job.salaryMax?.toLocaleString()}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadge(job.status)}`}>
                      {job.status}
                    </span>
                  </td>
                  {canApprove && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {job.status === 'PENDING_APPROVAL' && (
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleApprove(job.id)}
                            className="text-green-600 hover:text-green-900"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleReject(job.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default JobsPage;
