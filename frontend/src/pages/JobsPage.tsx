import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { jobsApi } from '../api/jobs';
import { JobOrder } from '../types';
import { useAuth } from '../context/AuthContext';
import JobEditModal from '../components/JobEditModal';

const JobsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [jobs, setJobs] = useState<JobOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingJob, setEditingJob] = useState<JobOrder | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<number | null>(null);

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

  const handleEdit = (job: JobOrder) => {
    setEditingJob(job);
  };

  const handleSaveEdit = async (id: number, data: Partial<JobOrder>) => {
    try {
      await jobsApi.updateJob(id, data);
      setEditingJob(null);
      fetchJobs();
      alert('Job order updated successfully');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update job order');
    }
  };

  const handleDelete = async (jobId: number) => {
    try {
      await jobsApi.deleteJob(jobId);
      setShowDeleteConfirm(null);
      fetchJobs();
      alert('Job order deleted successfully');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete job order');
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
    <div className="px-8">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/dashboard')}
            className="flex items-center text-gray-600 hover:text-gray-900"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span className="ml-2">Back</span>
          </button>
          <h1 className="text-3xl font-bold text-gray-800">Job Orders</h1>
        </div>
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
                      <div className="flex flex-col gap-2">
                        {job.status === 'PENDING_APPROVAL' && (
                          <div className="flex gap-2">
                            <button
                              onClick={() => handleApprove(job.id)}
                              className="text-green-600 hover:text-green-900 font-medium"
                            >
                              Approve
                            </button>
                            <button
                              onClick={() => handleReject(job.id)}
                              className="text-red-600 hover:text-red-900 font-medium"
                            >
                              Reject
                            </button>
                          </div>
                        )}
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleEdit(job)}
                            className="text-blue-600 hover:text-blue-900 font-medium"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => setShowDeleteConfirm(job.id)}
                            className="text-red-600 hover:text-red-900 font-medium"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Edit Modal */}
      {editingJob && (
        <JobEditModal
          job={editingJob}
          isOpen={true}
          onClose={() => setEditingJob(null)}
          onSave={handleSaveEdit}
        />
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-1/3 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <div className="flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mx-auto">
                <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h3 className="text-lg leading-6 font-medium text-gray-900 text-center mt-4">
                Delete Job Order
              </h3>
              <div className="mt-2 px-7 py-3">
                <p className="text-sm text-gray-500 text-center">
                  Are you sure you want to delete this job order? This action cannot be undone.
                  All associated candidates will be unaffected but will no longer be linked to this job.
                </p>
              </div>
              <div className="flex gap-3 px-4 py-3">
                <button
                  onClick={() => setShowDeleteConfirm(null)}
                  className="flex-1 px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300"
                >
                  Cancel
                </button>
                <button
                  onClick={() => handleDelete(showDeleteConfirm)}
                  className="flex-1 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default JobsPage;
