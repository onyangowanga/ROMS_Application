import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { jobsApi } from '../api/jobs';

interface JobForm {
  jobOrderRef: string;
  jobTitle: string;
  headcountRequired: number;
  salaryMin: number;
  salaryMax: number;
  currency: string;
  location: string;
  country: string;
  contractDurationMonths: number;
  requiredSkills: string;
  description: string;
}

const JobNewPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<JobForm>({
    jobOrderRef: '',
    jobTitle: '',
    headcountRequired: 1,
    salaryMin: 0,
    salaryMax: 0,
    currency: 'USD',
    location: '',
    country: '',
    contractDurationMonths: 24,
    requiredSkills: '',
    description: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData({ 
      ...formData, 
      [name]: ['headcountRequired', 'salaryMin', 'salaryMax', 'contractDurationMonths'].includes(name) 
        ? Number(value) 
        : value 
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.salaryMax < formData.salaryMin) {
      setError('Maximum salary must be greater than minimum salary');
      return;
    }

    setLoading(true);
    try {
      await jobsApi.createJob(formData);
      alert('Job order created successfully! It is pending approval.');
      navigate('/jobs');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create job order');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Create New Job Order</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-lg p-6">
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Job Order Reference *
            </label>
            <input
              type="text"
              name="jobOrderRef"
              value={formData.jobOrderRef}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
              placeholder="e.g., JO-001"
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Job Title *
            </label>
            <input
              type="text"
              name="jobTitle"
              value={formData.jobTitle}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Headcount Required *
            </label>
            <input
              type="number"
              name="headcountRequired"
              value={formData.headcountRequired}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
              min="1"
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Contract Duration (Months) *
            </label>
            <input
              type="number"
              name="contractDurationMonths"
              value={formData.contractDurationMonths}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
              min="1"
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Currency *
            </label>
            <select
              name="currency"
              value={formData.currency}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
            >
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
              <option value="GBP">GBP</option>
              <option value="AED">AED</option>
              <option value="SAR">SAR</option>
            </select>
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Minimum Salary *
            </label>
            <input
              type="number"
              name="salaryMin"
              value={formData.salaryMin}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
              min="0"
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Maximum Salary *
            </label>
            <input
              type="number"
              name="salaryMax"
              value={formData.salaryMax}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
              min="0"
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Location *
            </label>
            <input
              type="text"
              name="location"
              value={formData.location}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
            />
          </div>

          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Country *
            </label>
            <input
              type="text"
              name="country"
              value={formData.country}
              onChange={handleChange}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              required
            />
          </div>
        </div>

        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Required Skills *
          </label>
          <input
            type="text"
            name="requiredSkills"
            value={formData.requiredSkills}
            onChange={handleChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            required
            placeholder="e.g., AutoCAD, 3+ years experience"
          />
        </div>

        <div className="mb-6">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Job Description *
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            required
            rows={4}
          />
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={loading}
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:bg-gray-400"
          >
            {loading ? 'Creating...' : 'Create Job Order'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/jobs')}
            className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default JobNewPage;
