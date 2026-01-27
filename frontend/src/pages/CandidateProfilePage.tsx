import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { StatusBadge } from '../components/StatusBadge';
import { candidateApi } from '../api/candidates';
import { Candidate, CandidateDocument, CandidateStatus, DocumentType } from '../types';

const WORKFLOW_TRANSITIONS: Record<CandidateStatus, CandidateStatus[]> = {
  APPLIED: ['SHORTLISTED', 'REJECTED'],
  SHORTLISTED: ['INTERVIEW_SCHEDULED', 'REJECTED'],
  INTERVIEW_SCHEDULED: ['SELECTED', 'REJECTED'],
  SELECTED: ['MEDICAL_IN_PROGRESS', 'REJECTED'],
  MEDICAL_IN_PROGRESS: ['MEDICAL_CLEARED', 'REJECTED'],
  MEDICAL_CLEARED: ['DEPLOYED', 'REJECTED'],
  DEPLOYED: ['PLACED', 'WITHDRAWN'],
  PLACED: [],
  REJECTED: [],
  WITHDRAWN: [],
};

const DOCUMENT_TYPES: DocumentType[] = ['PASSPORT', 'MEDICAL', 'OFFER', 'CONTRACT', 'VISA', 'OTHER'];

export const CandidateProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [documents, setDocuments] = useState<CandidateDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Document upload state
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploadDocType, setUploadDocType] = useState<DocumentType>('PASSPORT');
  const [uploadExpiryDate, setUploadExpiryDate] = useState('');
  const [uploadDocNumber, setUploadDocNumber] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  
  // Workflow transition state
  const [selectedTransition, setSelectedTransition] = useState<CandidateStatus | ''>('');
  const [transitioning, setTransitioning] = useState(false);
  const [transitionError, setTransitionError] = useState('');

  useEffect(() => {
    if (id) {
      loadCandidate(parseInt(id));
      loadDocuments(parseInt(id));
    }
  }, [id]);

  const loadCandidate = async (candidateId: number) => {
    try {
      const data = await candidateApi.getById(candidateId);
      setCandidate(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load candidate');
    } finally {
      setLoading(false);
    }
  };

  const loadDocuments = async (candidateId: number) => {
    try {
      const data = await candidateApi.getDocuments(candidateId);
      setDocuments(data);
    } catch (err: any) {
      console.error('Failed to load documents:', err);
    }
  };

  const handleUploadDocument = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadFile || !id) return;

    setUploading(true);
    setUploadError('');

    try {
      await candidateApi.uploadDocument(
        parseInt(id),
        uploadFile,
        uploadDocType,
        uploadExpiryDate || undefined,
        uploadDocNumber || undefined
      );
      
      // Reset form
      setUploadFile(null);
      setUploadExpiryDate('');
      setUploadDocNumber('');
      
      // Reload documents
      loadDocuments(parseInt(id));
      
      alert('Document uploaded successfully!');
    } catch (err: any) {
      setUploadError(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setUploading(false);
    }
  };

  const handleTransition = async () => {
    if (!selectedTransition || !id) return;

    setTransitioning(true);
    setTransitionError('');

    try {
      const updated = await candidateApi.transition(parseInt(id), selectedTransition);
      setCandidate(updated);
      setSelectedTransition('');
      alert(`Candidate transitioned to ${selectedTransition} successfully!`);
    } catch (err: any) {
      // Display backend guard logic error
      const errorMsg = err.response?.data?.message || 'Transition failed';
      setTransitionError(errorMsg);
      alert(`Transition Failed: ${errorMsg}`);
    } finally {
      setTransitioning(false);
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

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-12">Loading candidate...</div>
      </Layout>
    );
  }

  if (error || !candidate) {
    return (
      <Layout>
        <div className="rounded-md bg-red-50 p-4">
          <p className="text-sm text-red-800">{error || 'Candidate not found'}</p>
        </div>
      </Layout>
    );
  }

  const availableTransitions = WORKFLOW_TRANSITIONS[candidate.currentStatus] || [];

  return (
    <Layout>
      <div className="px-4 py-6 sm:px-0">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/candidates')}
            className="text-sm text-primary-600 hover:text-primary-800 mb-2"
          >
            ← Back to Candidates
          </button>
          <h1 className="text-2xl font-bold text-gray-900">
            {candidate.firstName} {candidate.lastName}
          </h1>
          <p className="text-sm text-gray-500">Ref: {candidate.internalRefNo}</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Candidate Info */}
          <div className="lg:col-span-2 space-y-6">
            {/* Basic Info Card */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Candidate Information</h2>
              <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                <div>
                  <dt className="text-sm font-medium text-gray-500">Date of Birth</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {candidate.dateOfBirth ? new Date(candidate.dateOfBirth).toLocaleDateString() : 'N/A'}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Gender</dt>
                  <dd className="mt-1 text-sm text-gray-900">{candidate.gender}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Email</dt>
                  <dd className="mt-1 text-sm text-gray-900">{candidate.email}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Phone</dt>
                  <dd className="mt-1 text-sm text-gray-900">{candidate.phoneNumber}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Country</dt>
                  <dd className="mt-1 text-sm text-gray-900">{candidate.country}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Passport No</dt>
                  <dd className="mt-1 text-sm text-gray-900">{candidate.passportNo}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Passport Expiry</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {candidate.passportExpiry ? new Date(candidate.passportExpiry).toLocaleDateString() : 'N/A'}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Current Status</dt>
                  <dd className="mt-1">
                    <StatusBadge status={candidate.currentStatus} />
                  </dd>
                </div>
                {candidate.medicalStatus && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Medical Status</dt>
                    <dd className="mt-1">
                      <StatusBadge status={candidate.medicalStatus} />
                    </dd>
                  </div>
                )}
                {candidate.expiryFlag && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Document Alert</dt>
                    <dd className="mt-1">
                      <StatusBadge status={candidate.expiryFlag} />
                    </dd>
                  </div>
                )}
              </dl>
            </div>

            {/* Documents Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Documents</h2>
              
              {/* Upload Form */}
              <form onSubmit={handleUploadDocument} className="mb-6 p-4 bg-gray-50 rounded-lg">
                <h3 className="text-sm font-medium text-gray-700 mb-3">Upload Document</h3>
                
                {uploadError && (
                  <div className="mb-3 rounded-md bg-red-50 p-3">
                    <p className="text-sm text-red-800">{uploadError}</p>
                  </div>
                )}
                
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Document Type</label>
                    <select
                      value={uploadDocType}
                      onChange={(e) => setUploadDocType(e.target.value as DocumentType)}
                      className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm rounded-md"
                    >
                      {DOCUMENT_TYPES.map((type) => (
                        <option key={type} value={type}>{type}</option>
                      ))}
                    </select>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700">File</label>
                    <input
                      type="file"
                      required
                      onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                      className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Document Number (Optional)</label>
                    <input
                      type="text"
                      value={uploadDocNumber}
                      onChange={(e) => setUploadDocNumber(e.target.value)}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Expiry Date (Optional)</label>
                    <input
                      type="date"
                      value={uploadExpiryDate}
                      onChange={(e) => setUploadExpiryDate(e.target.value)}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>
                </div>
                
                <div className="mt-4">
                  <button
                    type="submit"
                    disabled={uploading || !uploadFile}
                    className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                  >
                    {uploading ? 'Uploading...' : 'Upload Document'}
                  </button>
                </div>
              </form>

              {/* Documents List */}
              <div className="space-y-2">
                {documents.length === 0 ? (
                  <p className="text-sm text-gray-500">No documents uploaded</p>
                ) : (
                  documents.map((doc) => (
                    <div key={doc.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900">{doc.fileName}</p>
                        <div className="flex items-center space-x-2 mt-1">
                          <StatusBadge status={doc.docType} />
                          <span className="text-xs text-gray-500">
                            {(doc.fileSize / 1024).toFixed(1)} KB
                          </span>
                          {doc.expiryDate && (
                            <span className="text-xs text-gray-500">
                              Expires: {new Date(doc.expiryDate).toLocaleDateString()}
                            </span>
                          )}
                        </div>
                      </div>
                      <button
                        onClick={() => handleDownloadDocument(doc.id, doc.fileName)}
                        className="ml-4 text-sm text-primary-600 hover:text-primary-800"
                      >
                        Download
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>

          {/* Right Column - Workflow */}
          <div>
            <div className="bg-white shadow rounded-lg p-6 sticky top-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Workflow Transitions</h2>
              
              <div className="mb-4">
                <p className="text-sm text-gray-600 mb-2">Current Status:</p>
                <StatusBadge status={candidate.currentStatus} />
              </div>

              {transitionError && (
                <div className="mb-4 rounded-md bg-red-50 p-3">
                  <p className="text-sm text-red-800 font-medium">Guard Logic Error:</p>
                  <p className="text-xs text-red-700 mt-1">{transitionError}</p>
                </div>
              )}

              {availableTransitions.length === 0 ? (
                <p className="text-sm text-gray-500">No transitions available from current status</p>
              ) : (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Transition To:
                  </label>
                  <select
                    value={selectedTransition}
                    onChange={(e) => setSelectedTransition(e.target.value as CandidateStatus)}
                    className="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm rounded-md"
                  >
                    <option value="">-- Select Status --</option>
                    {availableTransitions.map((status) => (
                      <option key={status} value={status}>
                        {status.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>

                  <button
                    onClick={handleTransition}
                    disabled={!selectedTransition || transitioning}
                    className="mt-4 w-full inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                  >
                    {transitioning ? 'Transitioning...' : 'Execute Transition'}
                  </button>
                </div>
              )}

              <div className="mt-6 pt-6 border-t border-gray-200">
                <h3 className="text-sm font-medium text-gray-700 mb-2">Workflow Notes</h3>
                <ul className="text-xs text-gray-600 space-y-1">
                  <li>• Medical clearance required before offer issuance</li>
                  <li>• Interview step is optional</li>
                  <li>• Guard logic enforced by backend</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};
