import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { StatusBadge } from '../components/StatusBadge';
import CommissionManagement from '../components/CommissionManagement';
import WorkflowLockBanner from '../components/WorkflowLockBanner';
import { candidateApi } from '../api/candidates';
import { jobsApi } from '../api/jobs';
import { assignmentsApi } from '../api/assignments';
import { Candidate, CandidateDocument, CandidateStatus, DocumentType, Assignment, JobOrder } from '../types';
import { useAuth } from '../context/AuthContext';

const DOCUMENT_TYPES: DocumentType[] = ['PASSPORT', 'CV', 'EDUCATIONAL_CERTIFICATE', 'POLICE_CLEARANCE', 'MEDICAL_REPORT', 'PHOTO', 'OFFER_LETTER', 'CONTRACT', 'VISA', 'OTHER'];

export const CandidateProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [availableTransitions, setAvailableTransitions] = useState<CandidateStatus[]>([]);
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

  // Interview scheduling state
  const [interviewDate, setInterviewDate] = useState('');
  const [interviewTime, setInterviewTime] = useState('');
  const [interviewLocation, setInterviewLocation] = useState('');
  const [interviewNotes, setInterviewNotes] = useState('');
  const [savingInterview, setSavingInterview] = useState(false);

  // Assignment state
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [openJobs, setOpenJobs] = useState<JobOrder[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | ''>('');
  const [assignmentNotes, setAssignmentNotes] = useState('');
  const [assigning, setAssigning] = useState(false);
  const [assignmentError, setAssignmentError] = useState('');


  useEffect(() => {
    if (id) {
      loadCandidate(parseInt(id));
      loadDocuments(parseInt(id));
      loadAssignments(parseInt(id));
      loadOpenJobs();
      loadAllowedTransitions(parseInt(id));
    }
  }, [id]);

  // Refetch allowed transitions when candidate status changes
  useEffect(() => {
    if (id && candidate) {
      loadAllowedTransitions(parseInt(id));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [candidate?.currentStatus]);

  const loadAllowedTransitions = async (candidateId: number) => {
    try {
      const transitions = await candidateApi.getAllowedTransitions(candidateId);
      setAvailableTransitions(transitions);
    } catch (err) {
      setAvailableTransitions([]);
    }
  };

  useEffect(() => {
    // Populate interview fields when candidate data loads
    if (candidate) {
      setInterviewDate(candidate.interviewDate || '');
      setInterviewTime(candidate.interviewTime || '');
      setInterviewLocation(candidate.interviewLocation || '');
      setInterviewNotes(candidate.interviewNotes || '');
    }
  }, [candidate]);

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

  const handleDeleteDocument = async (docId: number, fileName: string) => {
    if (!confirm(`Are you sure you want to delete "${fileName}"? This action cannot be undone.`)) {
      return;
    }

    try {
      await candidateApi.deleteDocument(docId);
      // Reload documents after successful deletion
      if (id) {
        loadDocuments(parseInt(id));
      }
      alert('Document deleted successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete document');
    }
  };

  const handleSaveInterview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;

    setSavingInterview(true);

    try {
      const updated = await candidateApi.update(parseInt(id), {
        interviewDate: interviewDate || undefined,
        interviewTime: interviewTime || undefined,
        interviewLocation: interviewLocation || undefined,
        interviewNotes: interviewNotes || undefined,
      });
      setCandidate(updated);
      alert('Interview details saved successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save interview details');
    } finally {
      setSavingInterview(false);
    }
  };

  const loadAssignments = async (candidateId: number) => {
    try {
      const data = await assignmentsApi.getAssignmentsByCandidate(candidateId);
      setAssignments(data);
    } catch (err: any) {
      console.error('Failed to load assignments:', err);
    }
  };

  const loadOpenJobs = async () => {
    try {
      const jobs = await jobsApi.getAllJobs();
      const openJobs = jobs.filter(job => job.status === 'OPEN');
      setOpenJobs(openJobs);
    } catch (err: any) {
      console.error('Failed to load jobs:', err);
    }
  };

  const handleCreateAssignment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedJobId || !id) return;

    setAssigning(true);
    setAssignmentError('');

    try {
      await assignmentsApi.createAssignment({
        candidateId: parseInt(id),
        jobOrderId: Number(selectedJobId),
        notes: assignmentNotes || undefined
      });
      
      // Reload assignments and candidate
      await loadAssignments(parseInt(id));
      await loadCandidate(parseInt(id));
      
      // Reset form
      setSelectedJobId('');
      setAssignmentNotes('');
      alert('Assignment created successfully!');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to create assignment';
      setAssignmentError(errorMessage);
      alert(errorMessage);
    } finally {
      setAssigning(false);
    }
  };

  const handleCancelAssignment = async (assignmentId: number) => {
    if (!window.confirm('Are you sure you want to cancel this assignment?')) return;

    try {
      await assignmentsApi.cancelAssignment(assignmentId);
      
      // Reload assignments and candidate
      if (id) {
        await loadAssignments(parseInt(id));
        await loadCandidate(parseInt(id));
      }
      alert('Assignment cancelled successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel assignment');
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
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleDownloadDocument(doc.id, doc.fileName)}
                          className="text-sm text-primary-600 hover:text-primary-800"
                        >
                          Download
                        </button>
                        {user?.role === 'SUPER_ADMIN' && (
                          <button
                            onClick={() => handleDeleteDocument(doc.id, doc.fileName)}
                            className="text-sm text-red-600 hover:text-red-800"
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Interview Scheduling Section - Only show when status is DOCUMENTS_APPROVED or later */}
            {(candidate.currentStatus === 'DOCUMENTS_APPROVED' ||
              candidate.currentStatus === 'INTERVIEW_SCHEDULED' ||
              candidate.currentStatus === 'INTERVIEW_PASSED' ||
              ['MEDICAL_PENDING', 'MEDICAL_PASSED', 'VISA_PROCESSING', 'OFFER_ISSUED', 'OFFER_ACCEPTED', 'DEPLOYMENT_PENDING', 'PLACED'].includes(candidate.currentStatus)) && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">Interview Scheduling</h2>
                
                <form onSubmit={handleSaveInterview}>
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Interview Date</label>
                      <input
                        type="date"
                        value={interviewDate}
                        onChange={(e) => setInterviewDate(e.target.value)}
                        className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                      />
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Interview Time</label>
                      <input
                        type="time"
                        value={interviewTime}
                        onChange={(e) => setInterviewTime(e.target.value)}
                        className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                      />
                    </div>
                    
                    <div className="sm:col-span-2">
                      <label className="block text-sm font-medium text-gray-700">Interview Location</label>
                      <input
                        type="text"
                        value={interviewLocation}
                        placeholder="e.g., Conference Room A, 123 Main St, or Zoom Meeting"
                        onChange={(e) => setInterviewLocation(e.target.value)}
                        className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                      />
                    </div>
                    
                    <div className="sm:col-span-2">
                      <label className="block text-sm font-medium text-gray-700">Interview Notes</label>
                      <textarea
                        value={interviewNotes}
                        onChange={(e) => setInterviewNotes(e.target.value)}
                        rows={3}
                        placeholder="Additional details, instructions, or meeting link..."
                        className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                      />
                    </div>
                  </div>
                  
                  <div className="mt-4">
                    <button
                      type="submit"
                      disabled={savingInterview}
                      className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                    >
                      {savingInterview ? 'Saving...' : 'Save Interview Details'}
                    </button>
                  </div>
                </form>

                {candidate.interviewDate && (
                  <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <h3 className="text-sm font-medium text-blue-900 mb-2">Scheduled Interview</h3>
                    <div className="text-sm text-blue-800 space-y-1">
                      <p><strong>Date:</strong> {new Date(candidate.interviewDate).toLocaleDateString()}</p>
                      {candidate.interviewTime && <p><strong>Time:</strong> {candidate.interviewTime}</p>}
                      {candidate.interviewLocation && <p><strong>Location:</strong> {candidate.interviewLocation}</p>}
                      {candidate.interviewNotes && <p><strong>Notes:</strong> {candidate.interviewNotes}</p>}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Assignment Panel - Job Order Assignment */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Job Order Assignment</h2>
              
              {/* Current Active Assignment */}
              {assignments.filter(a => a.isActive).map((assignment) => (
                <div key={assignment.id} className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-sm font-medium text-green-900">Active Assignment</h3>
                      <div className="mt-2 text-sm text-green-800 space-y-1">
                        <p><strong>Job:</strong> {assignment.jobTitle}</p>
                        <p><strong>Ref:</strong> {assignment.jobOrderRef}</p>
                        <p><strong>Status:</strong> {assignment.status}</p>
                        <p><strong>Assigned:</strong> {new Date(assignment.assignedAt).toLocaleDateString()}</p>
                        {assignment.offerIssuedAt && (
                          <p><strong>Offer Issued:</strong> {new Date(assignment.offerIssuedAt).toLocaleDateString()}</p>
                        )}
                        {assignment.placementConfirmedAt && (
                          <p><strong>Placement Confirmed:</strong> {new Date(assignment.placementConfirmedAt).toLocaleDateString()}</p>
                        )}
                        {assignment.notes && <p><strong>Notes:</strong> {assignment.notes}</p>}
                      </div>
                    </div>
                    <button
                      onClick={() => handleCancelAssignment(assignment.id)}
                      className="text-red-600 hover:text-red-800 text-sm font-medium"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ))}

              {/* Create New Assignment */}
              {!assignments.some(a => a.isActive) && (
                <div>
                  {assignmentError && (
                    <div className="mb-4 rounded-md bg-red-50 p-3">
                      <p className="text-sm text-red-800">{assignmentError}</p>
                    </div>
                  )}

                  <form onSubmit={handleCreateAssignment}>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Select Open Job Order
                        </label>
                        <select
                          value={selectedJobId}
                          onChange={(e) => setSelectedJobId(e.target.value ? Number(e.target.value) : '')}
                          required
                          className="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm rounded-md"
                        >
                          <option value="">-- Select Job Order --</option>
                          {openJobs.map((job) => (
                            <option key={job.id} value={job.id}>
                              {job.jobOrderRef} - {job.jobTitle} ({job.headcountFilled || 0}/{job.headcountRequired})
                            </option>
                          ))}
                        </select>
                        {openJobs.length === 0 && (
                          <p className="mt-1 text-sm text-gray-500">No open job orders available</p>
                        )}
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Assignment Notes (Optional)
                        </label>
                        <textarea
                          value={assignmentNotes}
                          onChange={(e) => setAssignmentNotes(e.target.value)}
                          rows={3}
                          placeholder="Add any notes about this assignment..."
                          className="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        />
                      </div>

                      <button
                        type="submit"
                        disabled={!selectedJobId || assigning}
                        className="w-full inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                      >
                        {assigning ? 'Creating Assignment...' : 'Assign to Job Order'}
                      </button>
                    </div>
                  </form>
                </div>
              )}

              {/* Assignment History */}
              {assignments.filter(a => !a.isActive).length > 0 && (
                <div className="mt-6 pt-6 border-t border-gray-200">
                  <h3 className="text-sm font-medium text-gray-700 mb-3">Assignment History</h3>
                  <div className="space-y-2">
                    {assignments.filter(a => !a.isActive).map((assignment) => (
                      <div key={assignment.id} className="text-sm p-3 bg-gray-50 rounded">
                        <p className="font-medium">{assignment.jobTitle}</p>
                        <p className="text-gray-600">{assignment.jobOrderRef} - {assignment.status}</p>
                        <p className="text-gray-500 text-xs mt-1">
                          {new Date(assignment.assignedAt).toLocaleDateString()} - 
                          {assignment.cancelledAt && ` Cancelled: ${new Date(assignment.cancelledAt).toLocaleDateString()}`}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Right Column - Workflow */}
          <div className="space-y-6">
            {/* Phase 2B: Payment Workflow Locks */}
            {assignments.filter(a => a.isActive).length > 0 && (
              <WorkflowLockBanner
                assignmentId={assignments.find(a => a.isActive)!.id}
                currentStatus={candidate.currentStatus}
              />
            )}

            <div className="bg-white shadow rounded-lg p-6">
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

              {/* Document Review Actions */}
              {candidate.currentStatus === 'UNDER_REVIEW' && (
                <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                  <h3 className="text-sm font-medium text-blue-900 mb-2">Document Review</h3>
                  <p className="text-xs text-blue-700 mb-3">
                    Click below to automatically evaluate documents and transition status
                  </p>
                  <button
                    onClick={async () => {
                      try {
                        await candidateApi.reviewDocuments(parseInt(id!));
                        await loadCandidate(parseInt(id!));
                        await loadAllowedTransitions(parseInt(id!));
                        alert('Documents reviewed successfully!');
                      } catch (err: any) {
                        alert(err.response?.data?.message || 'Failed to review documents');
                      }
                    }}
                    className="w-full py-2 px-4 bg-blue-600 text-white rounded hover:bg-blue-700"
                  >
                    Review Documents Now
                  </button>
                </div>
              )}

              {/* Proceed After Document Approval */}
              {candidate.currentStatus === 'DOCUMENTS_APPROVED' && (
                <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <h3 className="text-sm font-medium text-green-900 mb-2">Documents Approved</h3>
                  <p className="text-xs text-green-700 mb-3">
                    Proceed to next stage (Interview or Medical based on job requirements)
                  </p>
                  <button
                    onClick={async () => {
                      try {
                        await candidateApi.proceedAfterDocuments(parseInt(id!));
                        await loadCandidate(parseInt(id!));
                        await loadAllowedTransitions(parseInt(id!));
                        alert('Proceeding to next stage!');
                      } catch (err: any) {
                        alert(err.response?.data?.message || 'Failed to proceed');
                      }
                    }}
                    className="w-full py-2 px-4 bg-green-600 text-white rounded hover:bg-green-700"
                  >
                    Proceed to Next Stage
                  </button>
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
                  <li>• <strong>Downpayment required before visa processing</strong></li>
                  <li>• <strong>Full payment required before placement</strong></li>
                  <li>• Guard logic enforced by backend</li>
                </ul>
              </div>
            </div>

            {/* Phase 2B: Commission Management (Staff Only) */}
            {(user?.role === 'SUPER_ADMIN' || user?.role === 'OPERATIONS_STAFF') && 
             assignments.filter(a => a.isActive).length > 0 && (
              <CommissionManagement
                candidateId={candidate.id}
                assignmentId={assignments.find(a => a.isActive)!.id}
                onUpdate={() => loadCandidate(parseInt(id!))}
              />
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default CandidateProfilePage;
