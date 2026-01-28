import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { candidateApi } from '../api/candidates';
import { jobsApi } from '../api/jobs';
import { JobOrder } from '../types';

interface ApplicationForm {
  // Account info
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  // Personal info
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  passportNo: string;
  passportExpiry: string;
  phoneNumber: string;
  country: string;
  currentAddress: string;
  // Job application
  jobOrderId: number | null;
  expectedPosition: string;
}

interface DocumentFile {
  file: File;
  docType: string;
  expiryDate?: string;
  documentNumber?: string;
}

const ApplicantRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [jobs, setJobs] = useState<JobOrder[]>([]);
  const [formData, setFormData] = useState<ApplicationForm>({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    gender: 'MALE',
    passportNo: '',
    passportExpiry: '',
    phoneNumber: '',
    country: '',
    currentAddress: '',
    jobOrderId: null,
    expectedPosition: ''
  });
  const [documents, setDocuments] = useState<DocumentFile[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchOpenJobs();
  }, []);

  const fetchOpenJobs = async () => {
    try {
      const allJobs = await jobsApi.getAllJobs();
      const openJobs = allJobs.filter(job => job.status === 'OPEN');
      setJobs(openJobs);
    } catch (err) {
      console.error('Failed to load jobs:', err);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: name === 'jobOrderId' ? Number(value) : value });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>, docType: string) => {
    const file = e.target.files?.[0];
    if (file) {
      setDocuments([...documents.filter(d => d.docType !== docType), { file, docType }]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    if (!formData.jobOrderId) {
      setError('Please select a job to apply for');
      return;
    }

    setLoading(true);
    try {
      // Create candidate with job application
      const candidate = await candidateApi.applyForJob({
        // Account credentials
        username: formData.username,
        email: formData.email,
        password: formData.password,
        // Personal information
        firstName: formData.firstName,
        lastName: formData.lastName,
        dateOfBirth: formData.dateOfBirth,
        gender: formData.gender,
        passportNo: formData.passportNo,
        passportExpiry: formData.passportExpiry,
        phoneNumber: formData.phoneNumber,
        country: formData.country,
        currentAddress: formData.currentAddress,
        expectedPosition: formData.expectedPosition,
        jobOrderId: formData.jobOrderId
      });

      // Upload documents if any
      if (documents.length > 0 && candidate.id) {
        for (const doc of documents) {
          await candidateApi.uploadDocument(
            candidate.id,
            doc.file,
            doc.docType,
            doc.expiryDate,
            doc.documentNumber
          );
        }
      }

      alert('Application submitted successfully! You can now login to track your status.');
      navigate('/login');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Application failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-8">
      <div className="max-w-4xl w-full bg-white shadow-lg rounded-lg p-8">
        <h1 className="text-3xl font-bold text-center text-blue-600 mb-6">
          Job Application
        </h1>
        <p className="text-center text-gray-600 mb-6">
          Apply for a job and create your account to track your application status
        </p>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Job Selection */}
          <div className="mb-6">
            <h2 className="text-xl font-bold text-gray-700 mb-3">Select Job</h2>
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                Available Positions *
              </label>
              <select
                name="jobOrderId"
                value={formData.jobOrderId || ''}
                onChange={handleChange}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                required
              >
                <option value="">-- Select a Job --</option>
                {jobs.map(job => (
                  <option key={job.id} value={job.id}>
                    {job.jobTitle} - {job.location}, {job.country} ({job.currency} {job.salaryMin?.toLocaleString()} - {job.salaryMax?.toLocaleString()})
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Account Information */}
          <div className="mb-6">
            <h2 className="text-xl font-bold text-gray-700 mb-3">Account Information</h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Username *</label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Password *</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                  minLength={8}
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Confirm Password *</label>
                <input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                  minLength={8}
                />
              </div>
            </div>
          </div>

          {/* Personal Information */}
          <div className="mb-6">
            <h2 className="text-xl font-bold text-gray-700 mb-3">Personal Information</h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">First Name *</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Last Name *</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Date of Birth *</label>
                <input
                  type="date"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Gender *</label>
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Passport Number *</label>
                <input
                  type="text"
                  name="passportNo"
                  value={formData.passportNo}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Passport Expiry *</label>
                <input
                  type="date"
                  name="passportExpiry"
                  value={formData.passportExpiry}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Phone Number *</label>
                <input
                  type="tel"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Country *</label>
                <input
                  type="text"
                  name="country"
                  value={formData.country}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="col-span-2">
                <label className="block text-gray-700 text-sm font-bold mb-2">Current Address *</label>
                <input
                  type="text"
                  name="currentAddress"
                  value={formData.currentAddress}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="col-span-2">
                <label className="block text-gray-700 text-sm font-bold mb-2">Expected Position</label>
                <input
                  type="text"
                  name="expectedPosition"
                  value={formData.expectedPosition}
                  onChange={handleChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., Senior Engineer"
                />
              </div>
            </div>
          </div>

          {/* Document Upload */}
          <div className="mb-6">
            <h2 className="text-xl font-bold text-gray-700 mb-3">Documents (Optional)</h2>
            <p className="text-sm text-gray-600 mb-3">Upload your documents now or after logging in</p>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Passport Copy</label>
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, 'PASSPORT')}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Photo</label>
                <input
                  type="file"
                  accept=".jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, 'PHOTO')}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Educational Certificate</label>
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, 'EDUCATIONAL_CERTIFICATE')}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                />
              </div>
              <div>
                <label className="block text-gray-700 text-sm font-bold mb-2">Medical Certificate</label>
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => handleFileChange(e, 'MEDICAL_CERTIFICATE')}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                />
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:bg-gray-400"
          >
            {loading ? 'Submitting Application...' : 'Submit Application'}
          </button>
        </form>

        <div className="mt-4 text-center">
          <p className="text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-500 hover:text-blue-700">
              Login here
            </Link>
          </p>
          <p className="text-gray-600 mt-2">
            Are you an employer?{' '}
            <Link to="/employer/register" className="text-blue-500 hover:text-blue-700">
              Register as Employer
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default ApplicantRegisterPage;
