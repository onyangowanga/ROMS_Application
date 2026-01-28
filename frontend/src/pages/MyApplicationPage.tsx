import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { candidateApi } from '../api/candidates';
import { Candidate, CandidateDocument } from '../types';

const MyApplicationPage: React.FC = () => {
  const { user } = useAuth();
  const [application, setApplication] = useState<Candidate | null>(null);
  const [documents, setDocuments] = useState<CandidateDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploadingDoc, setUploadingDoc] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyApplication();
  }, []);

  const fetchMyApplication = async () => {
    try {
      setLoading(true);
      const candidates = await candidateApi.getAll();
      // Find candidate by email matching user email
      const myCandidate = candidates.find(c => c.email === user?.email);
      
      if (myCandidate) {
        setApplication(myCandidate);
        const docs = await candidateApi.getDocuments(myCandidate.id);
        setDocuments(docs);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load application');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>, docType: string) => {
    const file = e.target.files?.[0];
    if (!file || !application) return;

    setUploadingDoc(true);
    try {
      await candidateApi.uploadDocument(application.id, file, docType);
      fetchMyApplication();
      alert('Document uploaded successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setUploadingDoc(false);
    }
  };

  const getStatusColor = (status: string) => {
    const colors: { [key: string]: string } = {
      'APPLIED': 'bg-blue-100 text-blue-800',
      'DOCS_SUBMITTED': 'bg-yellow-100 text-yellow-800',
      'INTERVIEWED': 'bg-purple-100 text-purple-800',
      'MEDICAL_PASSED': 'bg-green-100 text-green-800',
      'OFFER_ISSUED': 'bg-indigo-100 text-indigo-800',
      'OFFER_ACCEPTED': 'bg-teal-100 text-teal-800',
      'PLACED': 'bg-green-200 text-green-900',
      'REJECTED': 'bg-red-100 text-red-800',
      'WITHDRAWN': 'bg-gray-100 text-gray-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-xl text-gray-600">Loading your application...</div>
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

  if (!application) {
    return (
      <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
        No application found. Please apply for a job first.
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">My Job Application</h1>

      {/* Application Status */}
      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <h2 className="text-xl font-bold text-gray-700 mb-4">Application Status</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-600">Reference Number</p>
            <p className="font-semibold text-gray-900">{application.internalRefNo}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Current Status</p>
            <span className={`px-3 py-1 inline-flex text-sm leading-5 font-semibold rounded-full ${getStatusColor(application.currentStatus)}`}>
              {application.currentStatus.replace(/_/g, ' ')}
            </span>
          </div>
          <div>
            <p className="text-sm text-gray-600">Medical Status</p>
            <p className="font-semibold text-gray-900">{application.medicalStatus || 'PENDING'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Expected Position</p>
            <p className="font-semibold text-gray-900">{application.expectedPosition || 'N/A'}</p>
          </div>
        </div>
      </div>

      {/* Personal Information */}
      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <h2 className="text-xl font-bold text-gray-700 mb-4">Personal Information</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-600">Full Name</p>
            <p className="font-semibold text-gray-900">{application.firstName} {application.lastName}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Date of Birth</p>
            <p className="font-semibold text-gray-900">{new Date(application.dateOfBirth).toLocaleDateString()}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Passport Number</p>
            <p className="font-semibold text-gray-900">{application.passportNo}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Passport Expiry</p>
            <p className="font-semibold text-gray-900">{new Date(application.passportExpiry).toLocaleDateString()}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Email</p>
            <p className="font-semibold text-gray-900">{application.email}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Phone</p>
            <p className="font-semibold text-gray-900">{application.phoneNumber}</p>
          </div>
          <div className="col-span-2">
            <p className="text-sm text-gray-600">Country</p>
            <p className="font-semibold text-gray-900">{application.country}</p>
          </div>
        </div>
      </div>

      {/* Documents */}
      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <h2 className="text-xl font-bold text-gray-700 mb-4">My Documents</h2>
        
        {documents.length > 0 ? (
          <div className="mb-4">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Document Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">File Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Uploaded</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {documents.map(doc => (
                  <tr key={doc.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {doc.docType.replace(/_/g, ' ')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{doc.fileName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(doc.uploadedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {doc.isVerified ? (
                        <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                          Verified
                        </span>
                      ) : (
                        <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                          Pending
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-600 mb-4">No documents uploaded yet.</p>
        )}

        <div className="mt-4">
          <h3 className="text-lg font-semibold text-gray-700 mb-3">Upload Additional Documents</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-gray-700 text-sm font-bold mb-2">Passport Copy</label>
              <input
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) => handleFileUpload(e, 'PASSPORT')}
                disabled={uploadingDoc}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              />
            </div>
            <div>
              <label className="block text-gray-700 text-sm font-bold mb-2">Photo</label>
              <input
                type="file"
                accept=".jpg,.jpeg,.png"
                onChange={(e) => handleFileUpload(e, 'PHOTO')}
                disabled={uploadingDoc}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              />
            </div>
            <div>
              <label className="block text-gray-700 text-sm font-bold mb-2">Educational Certificate</label>
              <input
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) => handleFileUpload(e, 'EDUCATIONAL_CERTIFICATE')}
                disabled={uploadingDoc}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              />
            </div>
            <div>
              <label className="block text-gray-700 text-sm font-bold mb-2">Medical Certificate</label>
              <input
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) => handleFileUpload(e, 'MEDICAL_CERTIFICATE')}
                disabled={uploadingDoc}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Application Timeline */}
      <div className="bg-white shadow-md rounded-lg p-6">
        <h2 className="text-xl font-bold text-gray-700 mb-4">Application Process</h2>
        <div className="space-y-3">
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${application.currentStatus === 'APPLIED' ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}>
              1
            </div>
            <div className="ml-4">
              <p className="font-semibold">Applied</p>
              <p className="text-sm text-gray-600">Your application has been submitted</p>
            </div>
          </div>
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${['DOCS_SUBMITTED', 'INTERVIEWED', 'MEDICAL_PASSED', 'OFFER_ISSUED', 'OFFER_ACCEPTED', 'PLACED'].includes(application.currentStatus) ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}>
              2
            </div>
            <div className="ml-4">
              <p className="font-semibold">Documents Submitted</p>
              <p className="text-sm text-gray-600">Required documents uploaded and verified</p>
            </div>
          </div>
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${['INTERVIEWED', 'MEDICAL_PASSED', 'OFFER_ISSUED', 'OFFER_ACCEPTED', 'PLACED'].includes(application.currentStatus) ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}>
              3
            </div>
            <div className="ml-4">
              <p className="font-semibold">Interview</p>
              <p className="text-sm text-gray-600">Interview scheduled and completed</p>
            </div>
          </div>
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${['MEDICAL_PASSED', 'OFFER_ISSUED', 'OFFER_ACCEPTED', 'PLACED'].includes(application.currentStatus) ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}>
              4
            </div>
            <div className="ml-4">
              <p className="font-semibold">Medical Clearance</p>
              <p className="text-sm text-gray-600">Medical examination completed</p>
            </div>
          </div>
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${['OFFER_ISSUED', 'OFFER_ACCEPTED', 'PLACED'].includes(application.currentStatus) ? 'bg-blue-500 text-white' : 'bg-gray-300'}`}>
              5
            </div>
            <div className="ml-4">
              <p className="font-semibold">Offer Issued</p>
              <p className="text-sm text-gray-600">Job offer letter issued</p>
            </div>
          </div>
          <div className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${application.currentStatus === 'PLACED' ? 'bg-green-500 text-white' : 'bg-gray-300'}`}>
              6
            </div>
            <div className="ml-4">
              <p className="font-semibold">Placed</p>
              <p className="text-sm text-gray-600">Successfully placed in job</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyApplicationPage;
