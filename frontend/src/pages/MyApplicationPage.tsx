import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { candidateApi } from '../api/candidates';
import { assignmentsApi } from '../api/assignments';
import { Candidate, CandidateDocument, DocumentType, CandidateStatus } from '../types';
import { Layout } from '../components/Layout';
import { StatusBadge } from '../components/StatusBadge';
import CommissionSummary from '../components/CommissionSummary';

const DOCUMENT_TYPES: DocumentType[] = ['PASSPORT', 'CV', 'EDUCATIONAL_CERTIFICATE', 'POLICE_CLEARANCE', 'MEDICAL_REPORT', 'PHOTO', 'OFFER_LETTER', 'CONTRACT', 'VISA', 'OTHER'];

const MyApplicationPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [applications, setApplications] = useState<Candidate[]>([]);
  const [documents, setDocuments] = useState<{ [key: number]: CandidateDocument[] }>({});
  const [activeAssignments, setActiveAssignments] = useState<{ [key: number]: number | null }>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Document upload state
  const [uploadingFor, setUploadingFor] = useState<number | null>(null);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploadDocType, setUploadDocType] = useState<DocumentType>('PASSPORT');
  const [uploadExpiryDate, setUploadExpiryDate] = useState('');
  const [uploadDocNumber, setUploadDocNumber] = useState('');

  // Expanded application tracking
  const [expandedApp, setExpandedApp] = useState<number | null>(null);

  useEffect(() => {
    loadApplications();
  }, []);

  const loadApplications = async () => {
    try {
      setLoading(true);
      setError('');
      if (!user?.email) {
        setError('User email not found');
        return;
      }

      console.log('Loading applications for user:', user);
      console.log('User email:', user.email);
      
      const apps = await candidateApi.getMyApplications(user.email);
      setApplications(apps);

      // Load documents for all applications
      const docsMap: { [key: number]: CandidateDocument[] } = {};
      const assignmentsMap: { [key: number]: number | null } = {};
      for (const app of apps) {
        try {
          const docs = await candidateApi.getDocuments(app.id);
          docsMap[app.id] = docs;
        } catch (err) {
          docsMap[app.id] = [];
        }

        // Load active assignment if exists
        try {
          const assignments = await assignmentsApi.getAllAssignments();
          const candidateAssignments = assignments.filter((a: any) => a.candidateId === app.id);
          const activeAssignment = candidateAssignments.find((a: any) => a.isActive);
          assignmentsMap[app.id] = activeAssignment?.id || null;
        } catch (err) {
          assignmentsMap[app.id] = null;
        }
      }
      setDocuments(docsMap);
      setActiveAssignments(assignmentsMap);
    } catch (err: any) {
      console.error('Error loading applications:', err);
      console.error('Error response:', err.response);
      if (err.response?.status === 500 || err.response?.status === 404) {
        setApplications([]);
      } else {
        setError(err.response?.data?.message || 'Failed to load applications');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUploadDocument = async (e: React.FormEvent, candidateId: number) => {
    e.preventDefault();
    if (!uploadFile) return;

    setUploadingFor(candidateId);

    try {
      await candidateApi.uploadDocument(
        candidateId,
        uploadFile,
        uploadDocType,
        uploadExpiryDate || undefined,
        uploadDocNumber || undefined
      );

      // Reset form
      setUploadFile(null);
      setUploadExpiryDate('');
      setUploadDocNumber('');

      // Reload documents for this application
      const docs = await candidateApi.getDocuments(candidateId);
      setDocuments(prev => ({ ...prev, [candidateId]: docs }));

      alert('Document uploaded successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setUploadingFor(null);
    }
  };

  const handleDownloadDocument = async (docId: number, fileName: string) => {
    try {
      const blob = await candidateApi.downloadDocument(docId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      alert('Failed to download document');
    }
  };

  const toggleExpanded = (appId: number) => {
    setExpandedApp(expandedApp === appId ? null : appId);
  };

  const getProgressPercentage = (status: CandidateStatus): number => {
    const progressMap: Record<CandidateStatus, number> = {
      APPLIED: 6,
      DOCUMENTS_PENDING: 12,
      DOCUMENTS_UNDER_REVIEW: 18,
      DOCUMENTS_APPROVED: 25,
      INTERVIEW_SCHEDULED: 37,
      INTERVIEW_COMPLETED: 43,
      MEDICAL_IN_PROGRESS: 56,
      MEDICAL_PASSED: 62,
      OFFER_ISSUED: 75,
      OFFER_SIGNED: 81,
      VISA_PROCESSING: 87,
      VISA_APPROVED: 93,
      DEPLOYED: 96,
      PLACED: 100,
      REJECTED: 0,
      WITHDRAWN: 0,
    };
    return progressMap[status] || 0;
  };

  const getStatusLabel = (status: CandidateStatus): string => {
    return status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (l: string) => l.toUpperCase());
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center h-64">
          <div className="text-xl text-gray-600">Loading your applications...</div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-8 py-6">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900">My Applications</h1>
          <button
            onClick={() => navigate('/jobs')}
            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Apply for New Job
          </button>
        </div>

        {/* Applications List */}
        {applications.length === 0 ? (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-8 text-center">
            <svg className="mx-auto h-12 w-12 text-yellow-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 className="text-lg font-medium text-yellow-900 mb-2">No Applications Yet</h3>
            <p className="text-yellow-700 mb-2">
              No job applications found for <strong>{user?.email}</strong>.
            </p>
            <p className="text-yellow-700 mb-4">
              Browse available positions and submit your first application.
            </p>
            <p className="text-xs text-yellow-600 mb-4">
              (Check browser console for detailed debugging information)
            </p>
            <button
              onClick={() => navigate('/jobs')}
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              Browse Jobs
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {applications.map((app) => {
              try {
                return (
              <div key={app.id} className="bg-white shadow rounded-lg overflow-hidden">
                {/* Application Header */}
                <div className="px-6 py-4 border-b border-gray-200">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-4">
                        <h2 className="text-xl font-semibold text-gray-900">
                          {app.firstName} {app.lastName}
                        </h2>
                        <StatusBadge status={app.currentStatus} />
                      </div>
                      <div className="mt-1 flex items-center space-x-4 text-sm text-gray-500">
                        <span>Ref: {app.internalRefNo}</span>
                        <span>•</span>
                        <span>Applied: {app.createdAt ? new Date(app.createdAt).toLocaleDateString() : 'N/A'}</span>
                        {app.jobOrder && (
                          <>
                            <span>•</span>
                            <span>Position: {app.jobOrder.jobTitle || 'N/A'}</span>
                          </>
                        )}
                      </div>
                      
                      {/* Progress Bar */}
                      <div className="mt-3">
                        <div className="flex justify-between text-xs text-gray-600 mb-1">
                          <span>{getStatusLabel(app.currentStatus)}</span>
                          <span>{getProgressPercentage(app.currentStatus)}% Complete</span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                          <div
                            className={`h-2 rounded-full transition-all duration-300 ${
                              app.currentStatus === 'REJECTED' || app.currentStatus === 'WITHDRAWN'
                                ? 'bg-red-500'
                                : app.currentStatus === 'PLACED'
                                ? 'bg-green-500'
                                : 'bg-blue-500'
                            }`}
                            style={{ width: `${getProgressPercentage(app.currentStatus)}%` }}
                          ></div>
                        </div>
                      </div>

                      {/* Interview Information */}
                      {app.interviewDate && (
                        <div className="mt-2 p-2 bg-blue-50 border border-blue-200 rounded">
                          <div className="flex items-center space-x-2 text-sm text-blue-800">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <span className="font-medium">Interview Scheduled:</span>
                            <span>{new Date(app.interviewDate).toLocaleDateString()}</span>
                            {app.interviewTime && <span>at {app.interviewTime}</span>}
                          </div>
                          {app.interviewLocation && (
                            <div className="mt-1 text-sm text-blue-700 flex items-center space-x-2">
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                              </svg>
                              <span>{app.interviewLocation}</span>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                    <button
                      onClick={() => toggleExpanded(app.id)}
                      className="ml-4 text-primary-600 hover:text-primary-800"
                    >
                      {expandedApp === app.id ? (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
                        </svg>
                      ) : (
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      )}
                    </button>
                  </div>
                </div>

                {/* Expanded Content */}
                {expandedApp === app.id && (
                  <div className="px-6 py-4 bg-gray-50">
                    {/* Phase 2B: Commission Summary for Applicants */}
                    {activeAssignments[app.id] && (
                      <div className="mb-6">
                        <CommissionSummary 
                          candidateId={app.id}
                        />
                      </div>
                    )}

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                      {/* Application Details */}
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-3">Application Details</h3>
                        <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
                          <div>
                            <dt className="font-medium text-gray-500">Email</dt>
                            <dd className="text-gray-900">{app.email}</dd>
                          </div>
                          <div>
                            <dt className="font-medium text-gray-500">Phone</dt>
                            <dd className="text-gray-900">{app.phoneNumber}</dd>
                          </div>
                          <div>
                            <dt className="font-medium text-gray-500">Country</dt>
                            <dd className="text-gray-900">{app.country}</dd>
                          </div>
                          <div>
                            <dt className="font-medium text-gray-500">Passport No</dt>
                            <dd className="text-gray-900">{app.passportNo}</dd>
                          </div>
                          <div>
                            <dt className="font-medium text-gray-500">Passport Expiry</dt>
                            <dd className="text-gray-900">
                              {app.passportExpiry ? new Date(app.passportExpiry).toLocaleDateString() : 'N/A'}
                            </dd>
                          </div>
                          {app.medicalStatus && (
                            <div>
                              <dt className="font-medium text-gray-500">Medical Status</dt>
                              <dd><StatusBadge status={app.medicalStatus} /></dd>
                            </div>
                          )}
                        </dl>
                      </div>

                      {/* Documents Section */}
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-3">Documents</h3>
                        
                        {/* Upload Form */}
                        <form onSubmit={(e) => handleUploadDocument(e, app.id)} className="mb-4 p-3 bg-white rounded border border-gray-200">
                          <h4 className="text-sm font-medium text-gray-700 mb-2">Upload Document</h4>
                          <div className="space-y-2">
                            <div>
                              <select
                                value={uploadDocType}
                                onChange={(e) => setUploadDocType(e.target.value as DocumentType)}
                                className="block w-full text-sm border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500"
                              >
                                {DOCUMENT_TYPES.map((type) => (
                                  <option key={type} value={type}>{type}</option>
                                ))}
                              </select>
                            </div>
                            <div>
                              <input
                                type="file"
                                required
                                onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                                className="block w-full text-xs text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:font-semibold file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
                              />
                            </div>
                            <div className="grid grid-cols-2 gap-2">
                              <input
                                type="text"
                                placeholder="Doc Number (Optional)"
                                value={uploadDocNumber}
                                onChange={(e) => setUploadDocNumber(e.target.value)}
                                className="block w-full text-sm border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500"
                              />
                              <input
                                type="date"
                                value={uploadExpiryDate}
                                onChange={(e) => setUploadExpiryDate(e.target.value)}
                                className="block w-full text-sm border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500"
                              />
                            </div>
                            <button
                              type="submit"
                              disabled={uploadingFor === app.id || !uploadFile}
                              className="w-full py-1.5 px-3 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                            >
                              {uploadingFor === app.id ? 'Uploading...' : 'Upload'}
                            </button>
                          </div>
                        </form>

                        {/* Documents List */}
                        <div className="space-y-2 max-h-64 overflow-y-auto">
                          {(!documents[app.id] || documents[app.id].length === 0) ? (
                            <p className="text-sm text-gray-500 text-center py-2">No documents uploaded</p>
                          ) : (
                            documents[app.id].map((doc) => (
                              <div key={doc.id} className="flex items-center justify-between p-2 bg-white rounded border border-gray-200 text-sm">
                                <div className="flex-1 min-w-0">
                                  <p className="font-medium text-gray-900 truncate">{doc.fileName}</p>
                                  <div className="flex items-center space-x-2 mt-0.5">
                                    <span className="text-xs px-2 py-0.5 rounded bg-gray-100 text-gray-700">{doc.docType}</span>
                                    <span className="text-xs text-gray-500">{(doc.fileSize / 1024).toFixed(1)} KB</span>
                                    {doc.expiryDate && (
                                      <span className="text-xs text-gray-500">
                                        Exp: {new Date(doc.expiryDate).toLocaleDateString()}
                                      </span>
                                    )}
                                  </div>
                                </div>
                                <button
                                  onClick={() => handleDownloadDocument(doc.id, doc.fileName)}
                                  className="ml-2 text-primary-600 hover:text-primary-800 text-xs"
                                >
                                  Download
                                </button>
                              </div>
                            ))
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Offer Letter Section */}
                    {app.currentStatus === 'OFFER_ISSUED' && (
                      <div className="mt-4 p-4 bg-yellow-50 border border-yellow-300 rounded-lg">
                        <h4 className="text-sm font-semibold text-yellow-900 mb-2">
                          📄 Offer Letter Issued
                        </h4>
                        <p className="text-xs text-yellow-800 mb-3">
                          Please review your offer letter and accept to proceed with deployment.
                        </p>
                        <button
                          onClick={async () => {
                            if (window.confirm('Do you accept this job offer?')) {
                              try {
                                await candidateApi.acceptOffer(app.id);
                                loadApplications();
                                alert('Offer accepted successfully! Preparing for deployment.');
                              } catch (err: any) {
                                alert(err.response?.data?.message || 'Failed to accept offer');
                              }
                            }
                          }}
                          className="w-full py-2 px-4 bg-green-600 text-white rounded hover:bg-green-700 font-medium"
                        >
                          Accept Offer
                        </button>
                      </div>
                    )}

                    {app.currentStatus === 'OFFER_ACCEPTED' && (
                      <div className="mt-4 p-4 bg-green-50 border border-green-300 rounded-lg">
                        <h4 className="text-sm font-semibold text-green-900 mb-2">
                          ✅ Offer Accepted
                        </h4>
                        <p className="text-xs text-green-800">
                          Preparing for deployment. You will be notified of next steps.
                        </p>
                      </div>
                    )}

                    {/* Final Placement Success */}
                    {app.currentStatus === 'PLACED' && (
                      <div className="mt-4 p-6 bg-gradient-to-r from-green-50 to-blue-50 border-2 border-green-400 rounded-lg">
                        <div className="text-center">
                          <div className="text-4xl mb-3">🎉</div>
                          <h4 className="text-lg font-bold text-green-900 mb-2">
                            Successfully Placed!
                          </h4>
                          <div className="text-sm text-gray-700 space-y-1">
                            <p><strong>Position:</strong> {app.expectedPosition || 'N/A'}</p>
                            <p><strong>Reference:</strong> {app.internalRefNo}</p>
                            <p className="text-xs text-gray-500 mt-2">
                              Congratulations on your successful placement!
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
              } catch (error) {
                console.error('Error rendering application:', error);
                console.error('App data:', app);
                return <div key={app.id} className="bg-red-100 p-4 rounded">Error rendering application</div>;
              }
            })}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default MyApplicationPage;
